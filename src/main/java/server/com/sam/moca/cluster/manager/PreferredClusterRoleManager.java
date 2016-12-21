/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
 *  Sam Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by Sam Corporation.
 *
 *  Sam Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by Sam Corporation.
 *
 *  $Copyright-End$
 */

package com.sam.moca.cluster.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.infinispan.manager.CacheContainer;

import com.sam.moca.cluster.ClusterRoleAware;
import com.sam.moca.cluster.Node;
import com.sam.moca.cluster.RoleDefinition;
import com.sam.moca.cluster.dao.RoleDefinitionDAO;
import com.sam.moca.cluster.jgroups.JGroupsLockManager;
import com.sam.moca.util.MocaUtils;

/**
 * This is the preferred cluster role manager.  It will try to obtain preferred
 * roles, however will not if someone else has obtained one through being
 * preferred or a manual acquisition.  This role manager will participate in
 * dynamic role allocation.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class PreferredClusterRoleManager extends AbstractClusterRoleManager {
    
    /**
     * @param factory
     * @param delay
     * @param timeUnit
     * @param dao
     * @param lockManager
     * @param clusterRoleAwareObjects
     */
    public PreferredClusterRoleManager(JGroupsLockManager lockManager, 
        long delay, TimeUnit timeUnit, Iterable<RoleDefinition> preferredRoles, 
            Iterable<RoleDefinition> excludeRoles, RoleDefinitionDAO dao, 
            CacheContainer container, ClusterRoleAware aware, 
            ClusterRoleAware... clusterRoleAwareObjects) {
        super(delay, timeUnit, excludeRoles, dao, lockManager, container, aware,
            clusterRoleAwareObjects);
        
        // We copy this to only take the hit of the CopyOnWriteArrayList once
        List<RoleDefinition> preferredRolesCopy = new ArrayList<RoleDefinition>();
        
        for (RoleDefinition preferredRole : preferredRoles) {
            preferredRolesCopy.add(preferredRole);
        }
        _preferredRoles = new CopyOnWriteArrayList<RoleDefinition>(
                preferredRolesCopy);
    }
    
    /**
     * The channel that was provided <b>must</b> be started before calling this.
     */
    public synchronized void start(Node myNode) {
        _ourNode = myNode;
        
        if (!_preferredRoles.isEmpty()) {
            obtainPreferredRoles();
        }
        
        super.start(myNode);
    }

    // @see
    // com.sam.moca.cluster.manager.AbstractClusterRoleManager#notifyMembership(com.sam.moca.cluster.Node,
    // java.util.List, java.util.List)

    @Override
    public void notifyMembership(Node local, List<Node> members,
                                 List<Node> joiners, List<Node> removedNodes,
                                 Boolean isMergeView) {
        super.notifyMembership(local, members, joiners, removedNodes,
            isMergeView);

        if (!isMergeView) {
            // We don't want to check preferred roles if we haven't started yet
            if (_ourNode == null) {
                return;
            }

            // On a member drop if no one is running this node that means we can
            // actually pick it up now
            for (final RoleDefinition preferredRole : _preferredRoles) {
                boolean someoneRunning = false;
                for (Set<RoleDefinition> roles : _roleMap.values()) {
                    if (roles.contains(preferredRole)) {
                        someoneRunning = true;
                        break;
                    }
                }
                // If no one is now running our preferred role, we try take it
                // over
                if (!someoneRunning) {
                    Thread thread = new Thread() {
                        // @see java.lang.Thread#run()
                        @Override
                        public void run() {
                            acquireRoleForcibly(false, preferredRole);
                        }
                    };
                    thread.start();
                }
            }
        }
    }
    
    // @see com.sam.moca.cluster.manager.AbstractClusterRoleManager#acquireRoleForcibly(com.sam.moca.cluster.Node, com.sam.moca.cluster.RoleDefinition)
    public boolean acquireRoleForcibly(boolean multiple, RoleDefinition def) {
        // This method is not needed, but is here for documentation purposes
        // If this is invoked on a preferred role, then the role will be stopped
        // since it is assumed to be called from a manual role manager.  We still
        // leave the forcible role in the map so that we can pick up the role
        // again if all those manual roles would go down.
        return super.acquireRoleForcibly(multiple, def);
    }
    
    protected void obtainPreferredRoles() {
        // Depending on the lock manager we don't want to mess up
        // the caller's transaction so we spawn a thread to be safe.
        Thread thread = new Thread() {
            // @see java.lang.Thread#run()
            @Override
            public void run() {
                for (RoleDefinition def : _preferredRoles) {
                    boolean someoneForced = false;
                    for (Set<RoleDefinition> forcedRoles : _forcedRoleMap.values()) {
                        if (forcedRoles.contains(def)) {
                            someoneForced = true;
                            break;
                        }
                    }
                    if (!someoneForced) {
                        acquireRoleForcibly(false, def);
                    }
                }
            }
        };
        
        _logger.debug(MocaUtils.concat("Our preferred roles: ", _preferredRoles));
        
        thread.start();
        
        try {
            thread.join(); 
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected final List<RoleDefinition> _preferredRoles;
}
