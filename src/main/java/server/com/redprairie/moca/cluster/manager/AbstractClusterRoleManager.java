/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 */

package com.redprairie.moca.cluster.manager;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaRuntimeException;
import com.redprairie.moca.cache.CacheUtils;
import com.redprairie.moca.cluster.ClusterRoleAware;
import com.redprairie.moca.cluster.ClusterUtils;
import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.cluster.dao.RoleDefinitionDAO;
import com.redprairie.moca.cluster.jgroups.JGroupsLockManager;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.util.DaemonThreadFactory;
import com.redprairie.moca.util.ExceptionSuppressingRunnable;
import com.redprairie.moca.util.MocaUtils;

/**
 * This class is an abstract cluster role manager that provides the default
 * behavior for state transfer and dynamic role balancing as desired.
 * 
 * TODO: We may have to reevaluate updating complete state atomically instead of piece meal we are now
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
@Listener
public abstract class AbstractClusterRoleManager implements ClusterRoleManager,
    ClusterRoleAware {
    /**
     * 
     */
    public AbstractClusterRoleManager(long delay, TimeUnit timeUnit, 
            Iterable<RoleDefinition> excludeRoles, RoleDefinitionDAO dao, 
            JGroupsLockManager lockManager, CacheContainer container, 
            ClusterRoleAware aware, ClusterRoleAware... clusterRoleAwareObjects) {
        _delay = delay;
        if (timeUnit == null) {
            throw new NullPointerException("Time Unit cannot be null");
        }
        _timeUnit = timeUnit;
        _logger = LogManager.getLogger(this.getClass());
        
        // We create an inner linked hash set so we can make it unmodifiable below
        // Linked to preserve the original iterable ordering
        Set<RoleDefinition> excludeRolesCopy = new LinkedHashSet<RoleDefinition>();
        
        for (RoleDefinition excludeRole : excludeRoles) {
            excludeRolesCopy.add(excludeRole);
        }
        
        _excludeRoles = Collections.unmodifiableSet(excludeRolesCopy);
        
        if (aware == null) {
            throw new NullPointerException("ClusterRoleAware object was null!");
        }
        
        _awareObjs = new ClusterRoleAware[clusterRoleAwareObjects.length + 2];
        _awareObjs[0] = this;
        _awareObjs[1] = aware;
        
        for (int i = 0; i < clusterRoleAwareObjects.length; ++i) {
            if (clusterRoleAwareObjects[i] == null) {
                throw new NullPointerException("ClusterRoleAware object was null!");
            }
            _awareObjs[i+2] = clusterRoleAwareObjects[i];
        }
        
        _dao = dao;
        
        _availableRoles.addAll(_dao.readAllWithoutStar());
        _knownRoles.addAll(_availableRoles);
        
        _lockManager = lockManager;
        
        Cache<Node, Set<RoleDefinition>> roleMap = container.getCache("moca-current-roles");
        roleMap.addListener(this);
        _roleMap = roleMap;
        _forcedRoleMap = container.getCache("moca-forced-roles");
        _singleForcedRoles = container.getCache("moca-single-forced-roles");
        
        synchronized (_availableRoles) {
            for (Set<RoleDefinition> usedRoles : _roleMap.values()) {
                _availableRoles.removeAll(usedRoles);
            }
        }
    }
    
    // @see com.redprairie.moca.cluster.manager.ClusterRoleManager#stop()
    public synchronized void stop() {
        if (_roleSynchronizerHandler != null) {
            _roleSynchronizerHandler.cancel(true);
        }
    }
    
    // @see com.redprairie.moca.cluster.manager.ClusterRoleManager#start(com.redprairie.moca.cluster.Node)
    public synchronized void start(Node node) {
        _ourNode = node;
        
        long delay = _timeUnit.toMillis(_delay);
        if (delay <= 0) {
            throw new IllegalArgumentException("Converted delay value is less than or equal to 0 milliseconds");
        }
        _roleSynchronizerHandler = _scheduler.scheduleAtFixedRate(
            new ExceptionSuppressingRunnable(getUpdater(), _logger), delay,
            delay, TimeUnit.MILLISECONDS);
    }
    
    // @see
    // com.redprairie.moca.cluster.MocaClusterMembershipListener#notifyMembership(com.redprairie.moca.cluster.Node,
    // java.util.List, java.util.List)
    @Override
    public void notifyMembership(Node local, List<Node> members,
                                 List<Node> joiningNodes,
                                 List<Node> removedNodes, Boolean isMergeView) {
        _logger.debug(MocaUtils.concat("Node view: ", members));

        _nodeId = members.indexOf(local);

        // If we are the first then remove the now bogus roles
        if (members.get(0).equals(local)) {

            if (removedNodes.size() > 0) {
                _logger.debug(MocaUtils.concat("Removing nodes : ",
                    removedNodes));
            }
            for (Node removedNode : removedNodes) {
                CacheUtils.mapRemove(_roleMap, removedNode, MOCA_CURRENT_ROLES,
                    _nodeId);
                Set<RoleDefinition> forcedRoles = CacheUtils.mapRemove(
                    _forcedRoleMap, removedNode, MOCA_FORCED_ROLES, _nodeId);
                // If the node had forced roles then we also have to
                // remove single forced too
                if (forcedRoles != null) {
                    for (RoleDefinition forcedRole : forcedRoles) {
                        // We want to remove the fact that this was a single
                        // force role now as well, so someone else may take over
                        // with a single force
                        CacheUtils.mapRemove(_singleForcedRoles, forcedRole,
                            MOCA_SINGLE_FORCED_ROLES, _nodeId);
                    }
                }
            }
        }
        if (isMergeView) {
            synchronized (this) {
                // stop role manager so caches are not written to while we are
                // fixing them
                stop();
                // idiom that clears local references
                if (_ourRoles != null) {
                    Iterator<RoleDefinition> it = _ourRoles.iterator();
                    while (it.hasNext()) {
                        RoleDefinition role = it.next();
                        for (ClusterRoleAware aware : _awareObjs) {
                            aware.deactivateRole(role);
                        }
                        _logger.info("Deactivating role " + role);
                        it = _ourRoles.iterator();
                    }
                }

                Lock mergeLock = _lockManager.getClusterMergeLock();
                Condition mergeCondition = mergeLock.newCondition();
                mergeLock.lock();
                try {
                    if (ClusterUtils.isLeader(local, members)) {
                        // have the leader handle the merge first then call
                        // signalAll
                        for (ClusterRoleAware aware : _awareObjs) {
                            aware.handleMerge(members, local);
                        }
                        mergeCondition.signalAll();
                        _logger.debug("leader signal for handling merge");
                    }
                    else {
                        _logger
                            .debug("awaiting leader signal for handling merge");
                        // have non-leaders call await first then handle merge
                        try {
                            if (!mergeCondition.await(
                                CacheUtils.getRetryLimit()
                                        * CacheUtils.getDelayFactor(),
                                TimeUnit.MILLISECONDS)) {
                                _logger
                                    .debug("timeout from waiting for leader to handle merge,"
                                            + " however the leader could have already signalled before we tried to wait");
                            }
                            for (ClusterRoleAware aware : _awareObjs) {
                                aware.handleMerge(members, local);
                            }
                        }
                        catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new MocaInterruptedException(e);
                        }
                    }
                }
                finally {
                    mergeLock.unlock();
                    start(_ourNode);
                }
            }
        }
    }

    /**
     * When an entry is created or modified we have to mark those roles
     * as no longer available.  Also if someone stopped our role by forcibly
     * taking it for example we have to notify our cluster role aware guys.
     * @param event
     */
    @CacheEntryModified
    public void onModification(CacheEntryModifiedEvent<?, ?> event) {
        _logger.debug(MocaUtils.concat("Role Manager received modification event: ", event));
        // We only want events after it has occurred, which shows the new value
        if (!event.isPre()) {
            ConcurrentMap<?, ?> cache = event.getCache();
            if (cache == _roleMap) {
                // This should be a safe cast since these are types stored in this
                // cache
                @SuppressWarnings("unchecked")
                CacheEntryModifiedEvent<Node, Set<RoleDefinition>> casted = 
                        (CacheEntryModifiedEvent<Node, Set<RoleDefinition>>)event;
                Set<RoleDefinition> roles = casted.getValue();
                synchronized (_availableRoles) {
                    _availableRoles.removeAll(roles);
                }
                
                if (casted.getKey().equals(_ourNode)) {
                    _logger.debug(MocaUtils.concat("onModification - Our roles: ", roles));
                    // If it was local then it was an add
                    if (casted.isOriginLocal()) {
                        _logger.debug("Local modification, scanning for new role");
                        for (RoleDefinition newRole : roles) {
                            // If our old set didn't contain it then it is new
                            if (!_ourRoles.contains(newRole)) {
                                for (ClusterRoleAware aware : _awareObjs) {
                                    aware.activateRole(newRole);
                                }
                            }
                        }
                    }
                    // If not local that it was only a removal
                    else {
                        _logger.debug("Remote modification, scanning for removed role");
                        Set<RoleDefinition> rolesToRemove = new HashSet<RoleDefinition>();
                        synchronized (_ourRoles) {
                            for (RoleDefinition prevRole : _ourRoles) {
                                if (!roles.contains(prevRole)) {
                                    rolesToRemove.add(prevRole);
                                }
                            }
                        }
                        for (RoleDefinition role : rolesToRemove) {
                            for (ClusterRoleAware aware : _awareObjs) {
                                aware.deactivateRole(role);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * For role manager, we will have the leader clear all caches and deactivate
     * all roles that are running on the node.
     */

    @Override
    public void handleMerge(List<Node> members, Node local) {
        if (ClusterUtils.isLeader(local, members)) {
            _logger.debug("Clearing role caches...");
            _roleMap.clear();
            _forcedRoleMap.clear();
            _singleForcedRoles.clear();
        }
    }

    /**
     * This is a callback that occurs when a removal from the map occurs.  This
     * should only happen when a node dies and the coordinator updates the map.
     * @param event
     */
    @CacheEntryRemoved
    public void onRemoval(CacheEntryEvent<?, ?> event) {
        _logger.debug(MocaUtils.concat("Role Manager received removal event: ", event));
        // We want it before the event fires, because then we know what
        // roles this key originally had to work on
        if (event.isPre()) {
            ConcurrentMap<?, ?> cache = event.getCache();
            if (cache == _roleMap) {
                // This should be a safe cast since these are types stored in this
                // cache
                @SuppressWarnings("unchecked")
                CacheEntryRemovedEvent<Node, Set<RoleDefinition>> casted = 
                        (CacheEntryRemovedEvent<Node, Set<RoleDefinition>>)event;
                Set<RoleDefinition> nowAvailableRoles = new HashSet<RoleDefinition>();
                Set<RoleDefinition> lostRoles = casted.getValue();
                // lost roles can be null if the event is a merge 
                if(lostRoles != null) {
                    for (RoleDefinition lostRole : lostRoles) {
                        boolean contains = false;
                        for (Entry<Node, Set<RoleDefinition>> entry : _roleMap.entrySet()) {
                            // If the removal event key matches this then we ignore
                            // it since it will be removed
                            if (event.getKey().equals(entry.getKey())) {
                                continue;
                            }
                            Set<RoleDefinition> set = entry.getValue();
                            if (set.contains(lostRole)) {
                                contains = true;
                                break;
                            }
                        }
                        // If the map no longer contains it at all then that means this
                        // role is now available to be taken
                        if (!contains) {
                            nowAvailableRoles.add(lostRole);
                        }
                    }
                    
                    if (nowAvailableRoles.size() > 0) {
                        _logger.debug(MocaUtils.concat("Node is no longer available [", 
                            casted.getKey(), "], new roles available ", 
                            nowAvailableRoles));
                        synchronized (_availableRoles) {
                            _availableRoles.addAll(nowAvailableRoles);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * This method will first lock the role using the lock manager.  Upon
     * acquiring the lock it will then try to acquire the role forcibly
     * using the correct method for whether or not it is a multiple lock or
     * not
     * @param multiple
     * @param def
     * @return Whether or not we acquired the role
     */
    public boolean acquireRoleForcibly(boolean multiple, RoleDefinition def) {
        return acquireRoleForcibly(multiple, _ourNode, def);
    }
    
    boolean acquireRoleForcibly(boolean multiple, Node owner, RoleDefinition def) {
        boolean insert = false;
        Lock roleLock = _lockManager.getLock(def);
        
        roleLock.lock();
        try {
            // If it isn't multiple then we always insert it and make sure
            // other people know this is a non multiple node
            if (multiple) {
                insert = true;
                // If someone had single forced before we have to stop them!
                if (_singleForcedRoles.remove(def) != null) {
                    for (Node node : _roleMap.keySet()) {
                        removeFromMultimapMap(node, def, _roleMap);
                        removeFromMultimapMap(node, def, _forcedRoleMap);
                    }
                }
                // We also have to stop anyone who ran it dynamically and
                // wasn't forced
                else {
                    Node nodeToRemoveFrom = null;
                    for (Entry<Node, Set<RoleDefinition>> entry : _roleMap.entrySet()) {
                        Node node = entry.getKey();
                        if (entry.getValue().contains(def)) {
                            Set<RoleDefinition> forcedRoles = _forcedRoleMap.get(node);
                            if (forcedRoles == null || !forcedRoles.contains(def)) {
                                nodeToRemoveFrom = node;
                            }
                        }
                    }
                    
                    if (nodeToRemoveFrom != null) {
                        removeFromMultimapMap(nodeToRemoveFrom, def, _roleMap);
                    }
                }
            }
            // If this wasn't forced single and no one else forced we can do it
            else if (_singleForcedRoles.put(def, SINGLEFORCEDVALUE) == null) {
                boolean wasForced = false;
                for (Set<RoleDefinition> roles : _forcedRoleMap.values()) {
                    if (roles.contains(def)) {
                        wasForced = true;
                        break;
                    }
                }
                if (!wasForced) {
                    insert = true;
                    // We then have to remove this role from anyone who is running it
                    // normally for us then
                    for (Node node : _roleMap.keySet()) {
                        removeFromMultimapMap(node , def, _roleMap);
                    }
                }
            }
            
            if (insert) {
                addToMultimapMap(owner, def, _roleMap);
                addToMultimapMap(owner, def, _forcedRoleMap);
                _logger.debug(MocaUtils.concat("acquireRoleForcibly - Our roles: ", 
                    _roleMap.get(owner)));
            }
        }
        finally {
            roleLock.unlock();
        }
        
        return insert;
    }
    
    /**
     * This method should only be called from the sync
     * @param def
     * @return
     */
    protected boolean acquireRole(RoleDefinition def) {
        boolean insert = true;
        Lock roleLock = _lockManager.getLock(def);
        
        roleLock.lock();
        try {
            for (Set<RoleDefinition> roles : _roleMap.values()) {
                if (roles.contains(def)) {
                    insert = false;
                    break;
                }
            }
            
            if (insert) {
                addToMultimapMap(_ourNode, def, _roleMap);
                _logger.debug(MocaUtils.concat("Our roles: ", 
                    _roleMap.get(_ourNode)));
            }
        }
        finally {
            roleLock.unlock();
        }
        
        return insert;
    }
    
    private class RoleUpdater implements Runnable {
        // @see java.lang.Runnable#run()
        @Override
        public void run() {
            // If we are on a role check value make sure to check new ones
            if (_checkRolesCounter.incrementAndGet() % _checkRolesOffest == 0) {
                // First lets update all of our roles.
                // We make a copy so that we can modify the list safely
                List<RoleDefinition> allRoles = null;
                try {
                    allRoles = new ArrayList<RoleDefinition>(_dao.readAllWithoutStar());
                    MocaUtils.currentContext().commit();
                }
                catch (MocaRuntimeException e) {
                    _logger.warn("Could not get role definitions.", e);
                    return;
                }
                catch (MocaException e) {
                    _logger.warn(
                            "There was a problem closing transaction for role retrieval",
                            e);
                }
                finally {
                    ServerUtils.getCurrentContext().close();
                    // We then clear our references to the context
                    ServerUtils.removeCurrentContext();
                }
                
                // This should be a hash set since we may do a lot of contains
                // calls on it
                Set<RoleDefinition> removedRoles = new HashSet<RoleDefinition>();
                synchronized (_knownRoles) {
                    for (RoleDefinition role : _knownRoles) {
                        if (!allRoles.contains(role)) {
                            removedRoles.add(role);
                        }
                    }
                }
                
                if (!removedRoles.isEmpty()) {
                    _logger.debug(MocaUtils.concat("Roles have been removed: ", 
                        removedRoles));
                    
                    Set<RoleDefinition> rolesToRemove = new HashSet<RoleDefinition>();
                    // We have to use an iterator for role map in case if
                    // we have the role
                    Set<RoleDefinition> roles = _roleMap.get(_ourNode);
                    for (RoleDefinition role : roles) {
                        if (removedRoles.contains(role)) {
                            // If we have that role forcibly we don't remove
                            // it since we always want to keep it.
                            Set<RoleDefinition> ourForced = _forcedRoleMap.get(_ourNode);
                            if (ourForced != null && ourForced.contains(role)) {
                                _logger.debug(MocaUtils.concat(
                                    "We still maintain running [" + role + 
                                    "] since it was forced "));
                                continue;
                            }
                            rolesToRemove.add(role);
                            // This is a special case normally all local
                            // operations are additions, this is the only
                            // removal - as such we have to manually
                            // deactivate the role
                            _logger.debug(MocaUtils.concat("sync - Our roles: ", 
                                roles));
                            for (ClusterRoleAware aware : _awareObjs) {
                                aware.deactivateRole(role);
                            }
                        }
                    }
                    // If we removed any roles then we update the map
                    if (!rolesToRemove.isEmpty()) {
                        boolean replaced = false;
                        
                        while (!replaced) {
                            // We optimize for the replace to always hit first
                            // time, if it doesn't replace first time it may
                            // have to resize - hard to tell
                            Set<RoleDefinition> newRoles = 
                                    new HashSet<RoleDefinition>(roles.size() - 
                                            rolesToRemove.size());
                            for (RoleDefinition role : roles) {
                                if (!rolesToRemove.contains(role)) {
                                    newRoles.add(role);
                                }
                            }
                            if (_roleMap.replace(_ourNode, roles, newRoles)) {
                                replaced = true;
                                _logger.debug(MocaUtils.concat("sync2 - Our roles: ", 
                                    newRoles));
                            }
                            else {
                                roles = _roleMap.get(_ourNode);
                            }
                        }
                    }
                }
                
                // Then we remove all of the ones we know about already
                // leaving only new roles.
                allRoles.removeAll(_knownRoles);
                // We make the new roles available and also add to our
                // known role list
                synchronized (_availableRoles) {
                    _availableRoles.addAll(allRoles);
                }
                _knownRoles.addAll(allRoles);
            }
            
            // If exclude role doesn't contain all available then we
            // have an available we can grab
             boolean available;
             synchronized (_availableRoles) {
                 available = !_excludeRoles.containsAll(_availableRoles);
             }
            
            if (available) {
                RoleDefinition role = null;
                boolean finished = false;
                
                while (!finished) {
                    synchronized (_availableRoles) {
                        Iterator<RoleDefinition> iter = _availableRoles.iterator();
                        while (iter.hasNext()) {
                            RoleDefinition iterRole = iter.next();
                            
                            if (!_excludeRoles.contains(iterRole)) {
                                role = iterRole;
                                iter.remove();
                                break;
                            }
                        }
                    }
                    // If no free roles left or we are able to acquire one
                    // then complete
                    if (role == null || acquireRole(role)) {
                        finished = true;
                    }
                    else {
                        role = null;
                    }
                }
            }
            else {
                // Then we try to steal one from someone else to even out
                // the counts
                Multiset<Node> multiSetCopy;
                int roleCount = _roleMap.containsKey(_ourNode) ? _roleMap.get(_ourNode).size() : 0;
                
                // Now we create a multiset for each node so we know how
                // many roles each node has
                Multiset<Node> multiSet = HashMultiset.create();
                for (Entry<Node, Set<RoleDefinition>> entry : _roleMap.entrySet()) {
                    multiSet.add(entry.getKey(), entry.getValue().size());
                }
                
                multiSetCopy = ImmutableMultiset.copyOf(multiSet);
                
                // We look for someone who may have more roles than us and
                // steal one away. 
                com.google.common.collect.Multiset.Entry<Node> highEntry = null;
                for (com.google.common.collect.Multiset.Entry<Node> entry : 
                    multiSetCopy.entrySet()) {
                    if (highEntry == null) {
                        highEntry = entry;
                    }
                    else if (highEntry.getCount() < entry.getCount()) {
                        highEntry = entry;
                    }
                }
                
                // If the node with the highest count has more than us + 1 that
                // means we can steal from them
                if (highEntry != null && highEntry.getCount() > roleCount + 1) {
                    Collection<RoleDefinition> roles = _roleMap.get(
                                highEntry.getElement());
                    for (RoleDefinition role : roles) {
                        // By not trying another node if we couldn't steal
                        // or exclude could cause a misbalance, but that should
                        // be okay when you are explicitly using exclude and
                        // manual roles to prevent stealing
                        if (!_excludeRoles.contains(role) && 
                                stealRole(role, highEntry.getElement(), _ourNode)) {
                            break;
                        }
                    }
                }
            }
        }
    }
    
    boolean stealRole(RoleDefinition role, Node target, Node ourNode) {
        // If it was forced we can't steal it
        Set<RoleDefinition> forcedRoles = _forcedRoleMap.get(target);
        if (forcedRoles != null && forcedRoles.contains(role)) {
            return false;
        }
        
        Lock lock = _lockManager.getLock(role);
        // We have to first get the lock so we can try to steal the role
        lock.lock();
        try {
            boolean stolen = removeFromMultimapMap(target, role, _roleMap);
            if (stolen) {
                _logger.debug(MocaUtils.concat("We stole the role ", role, 
                    " from ", target, "."));
                
                addToMultimapMap(ourNode, role, _roleMap);
                _logger.debug(MocaUtils.concat("stealRole - Our roles: ", 
                    _roleMap.get(ourNode)));
            }
            return stolen;
        }
        finally {
            lock.unlock();
        }
    }
    
    public void activateRole(RoleDefinition role) {
        _logger.debug(MocaUtils.concat("Activating role: ", role));
        if (!_ourRoles.add(role)) {
            _logger.warn("Role " + role + " was activated that we already owned!");
        }
    }
    
    public void deactivateRole(RoleDefinition role) {
        _logger.debug(MocaUtils.concat("Deactivating role: ", role));
        if (!_ourRoles.remove(role)) {
            _logger.warn("Role " + role + " was deactivated that we didn't own!");
        }
    }
    
    public void noCluster() {
        // We don't worry about this
    }
    
    /**
     * This method can be overridden if a different implementation of the
     * dynamic role updating is desired.
     * @return
     */
    protected Runnable getUpdater() {
        return new RoleUpdater();
    }
    
    Map<Node, Set<RoleDefinition>> getRoleMap() {
        return _roleMap;
    }
    
    Map<Node, Set<RoleDefinition>> getForcedRoleMap() {
        return _forcedRoleMap;
    }
    
    Set<RoleDefinition> getSingleForcedRoleSet() {
        return _singleForcedRoles.keySet();
    }
    
    public Multimap<Node, RoleDefinition> getClusterRoles() {
        return convertMapToMultimap(_roleMap);
    }
    
    public Set<Node> getClusterNodes(RoleDefinition role) {
        Set<Node> nodes = new HashSet<Node>();
        
        if (role != null && role.getRoleId().equals("*")) {
            return _roleMap.keySet();
        }
        
        for (Entry<Node, Set<RoleDefinition>> entry : _roleMap.entrySet()) {
            if (entry.getValue().contains(role)) {
                nodes.add(entry.getKey());
            }
        }
        
        return nodes;
    }
    
    protected static <K, V>Multimap<K, V> convertMapToMultimap(Map<K, ? extends Iterable<V>> map) {
        Multimap<K, V> multimap = HashMultimap.create();
        for (Entry<K, ? extends Iterable<V>> entry : 
            map.entrySet()) {
            multimap.putAll(entry.getKey(), entry.getValue());
        }
        return multimap;
    }
    
    /**
     * Unfortunately we can't make the second type of the map a 
     * ? extends Collection.  The reason for this is because we create an 
     * ArrayList if the map doesn't contain the collection yet and if the map
     * doesn't support at least Collection or List in the type then we can't put
     * it in there.
     * @param key
     * @param value
     * @param map
     */
    protected static <K, V>void addToMultimapMap(K key, V value, 
        ConcurrentMap<K, Set<V>> map) {
        boolean success = false;
        
        while (!success) {
            Set<V> prevCollection = map.get(key);
            Set<V> collection;
            // We do a copy on write operation so that the sets are thread safe
            if (prevCollection == null) {
                collection = new HashSet<V>();
                collection.add(value);
                success = map.putIfAbsent(key, collection) == null;
            }
            else if (!prevCollection.contains(value)){
                collection = new HashSet<V>(prevCollection);
                collection.add(value);
                success = map.replace(key, prevCollection, collection);
            }
            else {
                // If it was already in the collection we don't need to update anything
                success = true;
            }
            
        }
    }
    
    protected static <K, V>boolean removeFromMultimapMap(K key, V value, 
        ConcurrentMap<K, Set<V>> map) {
        boolean removed = false;
        
        boolean success = false;
        
        while (!success) {
            Set<V> oldValues = map.get(key);
            // We do a copy on write operation so that the sets are thread safe
            if (oldValues != null && oldValues.contains(value)) {
                Set<V> newValues = new HashSet<V>(oldValues);
                // This should be true since the contains check above
                removed = newValues.remove(value);
                success = map.replace(key, oldValues, newValues);
                if (success) {
                    removed = true;
                }
            }
            // If the set no longer contains it we can't remove it anymore
            else {
                success = true;
            }
        }
        return removed;
    }
    
    /**
     * This is how many times the role updater will run before checking for new
     * roles in the database.
     */
    protected final int _checkRolesOffest = 3;
    protected AtomicInteger _checkRolesCounter = new AtomicInteger();

    protected Node _ourNode;
    
    protected final long _delay;
    protected final TimeUnit _timeUnit;
    
    /**
     * This must be protected by itself whenever being referenced
     */
    protected final ConcurrentMap<Node, Set<RoleDefinition>> _roleMap;
    
    /**
     * This must be protected by the _roleMap object monitor
     */
    protected final ConcurrentMap<Node, Set<RoleDefinition>> _forcedRoleMap;
    
    /**
     * This holds what roles were forced, but are only allowed for a single
     * node to run that role.
     * <p>
     * This is really a set so you should always put
     * {@link AbstractClusterRoleManager#SINGLEFORCEDVALUE} in as the value
     */
    protected final ConcurrentMap<RoleDefinition, Object> _singleForcedRoles;
    
    /**
     * This should only be used to insert as the value for 
     * {@link AbstractClusterRoleManager#_singleForcedRoles}
     */
    protected static final Object SINGLEFORCEDVALUE = new Serializable() {
        private static final long serialVersionUID = 748946753981065948L;
    };
    
    protected final JGroupsLockManager _lockManager;
    
    /**
     * All access must be synchronized on itself
     */
    protected final Deque<RoleDefinition> _availableRoles = new ArrayDeque<RoleDefinition>();
    // This set doesn't need to be synchronized since it is only referenced
    // in the RoleUpdater which is only called once at a time
    protected final Set<RoleDefinition> _knownRoles = new HashSet<RoleDefinition>();
    /**
     * All iteration methods must be synchronized on itself
     */
    protected final Set<RoleDefinition> _ourRoles = Collections.synchronizedSet(
        new HashSet<RoleDefinition>());
    
    /**
     * This set of exclude roles cannot be modified and is thread safe
     */
    protected final Set<RoleDefinition> _excludeRoles;
    
    /**
     * This object is safe to use across threads since our dao's will 
     * automatically use the connection associated with the current thread
     */
    protected final RoleDefinitionDAO _dao;
    
    
    protected final ClusterRoleAware[] _awareObjs; 
    
    
    public static final String MOCA_CURRENT_ROLES = "moca-current-roles";
    
    public static final String MOCA_FORCED_ROLES =  "moca-forced-roles";
    
    public static final String MOCA_SINGLE_FORCED_ROLES = "moca-single-forced-roles";
    
    protected int _nodeId;    
    
    protected final Logger _logger;
    
    private final ScheduledExecutorService _scheduler = Executors.newSingleThreadScheduledExecutor(
        new DaemonThreadFactory("ClusterRoleUpdater", false));
    
    private ScheduledFuture<?> _roleSynchronizerHandler;
}
