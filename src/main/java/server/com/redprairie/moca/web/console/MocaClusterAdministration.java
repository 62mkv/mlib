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

package com.redprairie.moca.web.console;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.cache.CacheUtils;
import com.redprairie.moca.cache.infinispan.extension.api.ClusterCaller;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.ClusterRequestRuntimeException;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.NodeResponse;
import com.redprairie.moca.cluster.MocaClusterMembershipListener;
import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.cluster.infinispan.InfinispanNode;
import com.redprairie.moca.job.JobException;
import com.redprairie.moca.job.JobManager;
import com.redprairie.moca.server.InstanceUrl;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.servlet.MocaServerAdministration;
import com.redprairie.moca.task.TaskManager;
import com.redprairie.moca.util.MocaUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.infinispan.notifications.cachelistener.event.Event;
import org.infinispan.notifications.cachelistener.event.Event.Type;
import org.infinispan.remoting.transport.Address;
import org.jgroups.blocks.locking.LockService;

/**
 * This is the cluster administration piece.  This has functionality as
 * in keeping a reference to all the known node urls.  This also has the
 * ability of initiating a restart server to all server nodes.
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
@Listener
public class MocaClusterAdministration implements MocaClusterMembershipListener {
	
    public static MocaClusterAdministration newInstance(SystemContext system, 
                                                        CacheContainer container, Node localNode,
                                                        JobManager jobManager, TaskManager taskManager,
                                                        LockService lockService) {
        Cache<Node, InstanceUrl> clusterUrls = container.getCache("moca-node-urls");
        ClusterCaller<Node, InstanceUrl> caller = 
                new ClusterCaller<Node, InstanceUrl>(clusterUrls, localNode);

        return newInstanceWithCaller(system,
            localNode, jobManager, taskManager,
            caller, lockService);
    }

    static MocaClusterAdministration newInstanceWithCaller(SystemContext system, 
                                                           Node localNode, JobManager jobManager, TaskManager taskManager,
                                                           ClusterCaller<Node, InstanceUrl> caller,
                                                           LockService lockService) {
        MocaClusterAdministration admin = new MocaClusterAdministration(system,
            localNode, jobManager,
            taskManager, caller, lockService);

        // Hook up the cache listener and register our node with the cache
        // We can't do this in the constructor hence this static factory method.
        admin._clusterUrls.getCache().addListener(admin);
        if (admin._localNode != null) {
            // We then put our url in the map
            _logger.info(String.format("Adding this node [%s] with the URL [%s] to the cluster...", 
                admin._localNode, admin._localUrl));
            CacheUtils.cachePut(admin._clusterUrls.getCache(),
                admin._localNode, admin._localUrl, 0);
        }

        return admin;
    }
   
    MocaClusterAdministration(SystemContext system, 
            Node localNode,
            JobManager jobManager,
            TaskManager taskManager,
            ClusterCaller<Node, InstanceUrl> caller,
            LockService lockService) {
        _localUrl = (InstanceUrl) system.getAttribute(InstanceUrl.class.getName());
        
        _localNode = localNode;
        _clusterUrls = caller;
        _lockService = lockService;
        
        _jobManager = jobManager;
        _taskManager = taskManager;
    }
    
    // @see com.redprairie.moca.servlet.MocaServerAdministrationMBean#restart()
    public void restartCluster() {  
        _logger.info("Restarting the cluster...");
        
        _clusterUrls.performRestartServerOnCluster();
    }
    
    public void restartNode() {
        _logger.info("Restarting the node...");
        
        // Get the administrator from the system context. 
        SystemContext sys = ServerUtils.globalContext();
        MocaServerAdministration admin = (MocaServerAdministration) sys.getAttribute(MocaServerAdministration.ATTRIBUTE_NAME);
        
        // Actually restart the server.
        admin.restart(true);
    }
    
    public void scheduleJobOnCluster(List<String> jobIds, List<String> instanceUrls) {
        List<Address> addresses = translateUrlsToAddresses(instanceUrls);
        _logger.info("Scheduling job(s) [" + jobIds + "] " + getNodeSpecificLogMessage(addresses));
        
        _clusterUrls.scheduleJobsOnCluster(jobIds, addresses);
    }
    
    public NodeResponse scheduleJobOnNode(String jobId) {
        if (_jobManager.getCurrentJob(jobId) == null) {
            _logger.debug(MocaUtils.concat("Job [", jobId, "] isn't present on node"));
            return NodeResponse.noActionEmptyResponse();
        }

        try {
            if (!_jobManager.isScheduled(jobId)) {
                _logger.debug(MocaUtils.concat(
                        "Received a request to schedule job [", jobId, "on this node"));
                _jobManager.scheduleJob(jobId);
                return NodeResponse.sucessfulEmptyResponse();
            }
            else {
                _logger.warn("Received request to schedule job [" 
                        + jobId + "] but it was already scheduled on this node");
                return NodeResponse.noActionEmptyResponse();
            }
        }
        catch (JobException ex) {
            _logger.error("JobExecution occurred while trying to schedule job [" 
                    + jobId + "] on the node", ex);
            return NodeResponse.exceptionResponse(ex);
        }
    }
    
    public void unscheduleJobOnCluster(List<String> jobIds, List<String> instanceUrls) {
        List<Address> addresses = translateUrlsToAddresses(instanceUrls);
        _logger.info("Unscheduling job(s) [" + jobIds + "] " + getNodeSpecificLogMessage(addresses));
        
        _clusterUrls.unscheduleJobsOnCluster(jobIds, addresses);
    }
    
    public NodeResponse unscheduleJobOnNode(String jobId) {
       if (_jobManager.getCurrentJob(jobId) == null) {
            _logger.debug(MocaUtils.concat("Job [", jobId,
                "] isn't present on node"));
            return NodeResponse.noActionEmptyResponse();
        }

        try {
            if (_jobManager.isScheduled(jobId)) {
                _logger.debug(MocaUtils.concat(
                    "Received a request to unschedule job [", jobId, "] on this node"));
                _jobManager.unscheduleJob(jobId);
                return NodeResponse.sucessfulEmptyResponse();
            }
            else {
                _logger.warn("Received request to unschedule job [" 
                        + jobId + "] but it was already unscheduled on this node");
                return NodeResponse.noActionEmptyResponse();
            }
        }
        catch (JobException ex) {
            _logger.error("JobExecution occurred while trying to unschedule job [" 
                    + jobId + "] on the node", ex);
            return NodeResponse.exceptionResponse(ex);
        }
    }
    
    public void startTaskOnCluster(List<String> taskIds, List<String> instanceUrls) {
        List<Address> addresses = translateUrlsToAddresses(instanceUrls);
        _logger.info("Starting task(s) [" + taskIds + "] " + getNodeSpecificLogMessage(addresses));
        
        _clusterUrls.startTasksOnCluster(taskIds, addresses);
    }
    
    public NodeResponse startTaskOnNode(String taskId) {
        if (_taskManager.getCurrentTask(taskId) == null) {
            _logger.debug(MocaUtils.concat("Task [", taskId, "] isn't present on node"));
            return NodeResponse.noActionEmptyResponse();
        }

        try {
            if (!_taskManager.isRunning(taskId)) {
                _logger.debug(MocaUtils.concat(
                    "Received a request to start task [", taskId, "] on this node"));
                _taskManager.startTask(taskId);
                return NodeResponse.sucessfulEmptyResponse();
            }
            else {
                _logger.warn("Received request to start task [" 
                        + taskId + "] but the task was already running on the node");
                return NodeResponse.noActionEmptyResponse();
            }
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            _logger.error("Exception occurred while trying to start task [" 
                    + taskId + "] on the node", e);
            return NodeResponse.exceptionResponse(e);
        }
    }
    
    public void stopTaskOnCluster(List<String> taskIds, List<String> instanceUrls) {
        List<Address> addresses = translateUrlsToAddresses(instanceUrls);
        _logger.info("Stopping task(s) [" + taskIds + "] " + getNodeSpecificLogMessage(addresses));
        
        _clusterUrls.stopTasksOnCluster(taskIds, addresses);
    }
    
    public NodeResponse stopTaskOnNode(String taskId) {
        if (_taskManager.getCurrentTask(taskId) == null) {
            _logger.debug(MocaUtils.concat("Task [", taskId, "] isn't present on node"));
            return NodeResponse.noActionEmptyResponse();
        }

        try {
            if (_taskManager.isRunning(taskId)) {
                _logger.debug(MocaUtils.concat(
                    "Received a request to stop task [", taskId, "] on this node"));
                _taskManager.stopTask(taskId);
                return NodeResponse.sucessfulEmptyResponse();
            }
            else {
                _logger.warn("Received request to stop task [" 
                        + taskId + "] but it was already stopped on this node");
                return NodeResponse.noActionEmptyResponse();
            }
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            _logger.error("Exception occurred while trying to stop task [" 
                    + taskId + "] on the node", e);
            return NodeResponse.exceptionResponse(e);
        }
    }
    
    public void restartTaskOnCluster(List<String> taskIds, List<String> instanceUrls) {
        List<Address> addresses = translateUrlsToAddresses(instanceUrls);
        _logger.info("Restarting task(s) [" + taskIds + "] " + getNodeSpecificLogMessage(addresses));
        
        _clusterUrls.restartTasksOnCluster(taskIds, addresses);
    }
    
    public NodeResponse restartTaskOnNode(String taskId) {
        if (_taskManager.getCurrentTask(taskId) == null) {
            _logger.debug(MocaUtils.concat("Task [", taskId, "] isn't present on node"));
            return NodeResponse.noActionEmptyResponse();
        }

        try {
            if (_taskManager.isRunning(taskId)) {
                _logger.debug(MocaUtils.concat(
                    "Received a request to restart task [", taskId, "] on this node"));
                _taskManager.stopTask(taskId);
                _taskManager.startTask(taskId);
                return NodeResponse.sucessfulEmptyResponse();
            }
            else {
                _logger.warn("Received request to restart task [" 
                        + taskId + "] but it wasn't running already on this node");
                return NodeResponse.noActionEmptyResponse();
            }
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            _logger.error("Exception occurred while trying to restart task [" 
                    + taskId + "] on the node", e);
            return NodeResponse.exceptionResponse(e);
        }
    }
    
    /**
     * Listener for the cluster URLs cache having entries modified (added)
     * or removed for logging purposes so we can log the state of the cluster
     * so it's clear when these events occur.
     * @param event The Infinispan cache event
     */
    @CacheEntryRemoved
    @CacheEntryCreated
    public void onEntryModifiedOrRemoved(Event<?, ?> event) {
        _logger.debug("MocaClusterAdministration received modification/removal event: "
                      + event);
        final Cache<?, ?> cache = event.getCache();
        
        if (cache == _clusterUrls.getCache()) {
            // We have to copy the cache over, since it doesn't
            // implement toString like we want
            final Map<Node, InstanceUrl> map = new HashMap<Node, InstanceUrl>(
                    _clusterUrls.getCache());
            final Type type = event.getType();
            
            
            if (type == Type.CACHE_ENTRY_CREATED && !event.isPre()) {
                @SuppressWarnings("unchecked")
                CacheEntryModifiedEvent<Node, InstanceUrl> castedEvent = 
                        (CacheEntryModifiedEvent<Node, InstanceUrl>)event;
                final Node node = castedEvent.getKey();
                final InstanceUrl mocaUrl = map.get(node);
                
                if (mocaUrl != null) {
                    _logger.info(String.format("Added node [%s] with the URL [%s] to the cluster",
                        node, mocaUrl));
                    logClusterState(map);
                }
            }
            // We have to log remove events on the pre event because Infinispan
            // for some reason doesn't provide the value for the entry removed on the post event.
            else if (type == Type.CACHE_ENTRY_REMOVED && event.isPre()) {
                @SuppressWarnings("unchecked")
                CacheEntryRemovedEvent<Node, InstanceUrl> castedEvent = 
                        (CacheEntryRemovedEvent<Node, InstanceUrl>)event;
                map.remove(castedEvent.getKey());
                _logger.info(String.format("Removed node [%s] from the cluster with the URL [%s]", 
                        castedEvent.getKey(), castedEvent.getValue()));
                logClusterState(map);
            }
        }
    }
    
    // @see
    // com.redprairie.moca.cluster.MocaClusterMembershipListener#notifyMembership(com.redprairie.moca.cluster.Node,
    // java.util.List, java.util.List)
    @Override
    public void notifyMembership(Node local, List<Node> members,
                                 List<Node> joiners, List<Node> leavers,
                                 Boolean isMergeView) {
        if (local.equals(_localNode)) {
            if (isMergeView) {
                _logger.info("Merge event detected");
            }
            rebuildClusterCacheWithLock(members);
        }
        else {
            throw new IllegalStateException("Event local node " + local
                    + " does not match actual local node " + _localNode);
        }
    }
    
    public Map<Node, InstanceUrl> getKnownNodes() {
        return Collections.unmodifiableMap(_clusterUrls.getCache());
    }
    
    public MocaResults getDisplayNamesAndUrls() {
        int port = -1;
        boolean usePorts = false;
        
        // We make a copy so that we have the same values both times
        Collection<InstanceUrl> urls = new ArrayList<InstanceUrl>(_clusterUrls.getCache().values());
        /* See if we'll need to include port numbers in the hostnames that will be displayed
         * in the console.  If all the servers in the cluster are listening on the same port
         * then we don't include the port numbers because it just clutters things up.
         */
        for (InstanceUrl url : urls) {
            int currentPort = url.getPort();
            if (port != -1 && port != currentPort) {
                usePorts = true;
            }
            else {
                port = currentPort;
            }
        }

        EditableResults res = new SimpleResults();
        
        res.addColumn("node", MocaType.STRING);
        res.addColumn("url", MocaType.STRING);
        res.addColumn("current", MocaType.BOOLEAN);
        
        /*
         * Now build the actual list of hostnames and URLs for the console.  We
         * also set a flag so the console can identify which hostname in the list
         * is the hostname that it is currently connected to.
         */
        for (InstanceUrl url : urls) {
            port = url.getPort();
            
            String hostName = url.getHostName().split("\\.")[0].toLowerCase();
            if (usePorts) {
                hostName += " (" + port + ")";
            }
        
            Boolean current = url.equals(_localUrl);
            
            res.addRow();
            res.setStringValue(0, hostName);
            res.setStringValue(1, url.toString());
            res.setBooleanValue(2, current);
        }
        
        return res;
    }
    
    public List<URL> getUrls() {
        ArrayList<URL> urls = new ArrayList<URL>();
        for (InstanceUrl instanceUrl : _clusterUrls.getCache().values()) {
            try {
                urls.add(new URL(instanceUrl.toString()));
            }
            catch (MalformedURLException e) {
                // This should never happen because we already have a known good
                // URL so if this does occur we've got something screwy going on.
                throw new Error(e);
            }          
        }
        
        return urls;
    }
    
    /**
     * Get Instance URLS from the cluster.
     * @return Map of Node to InstanceUrl
     * @throws ClusterRequestRuntimeException if a request failed
     */
    public Map<Node, InstanceUrl> getNodeUrlsOnCluster() throws ClusterRequestRuntimeException {
        // actual call is here
        final Map<Address, NodeResponse> urls = CacheUtils.retryGenericOperation("cluster-cache", "RPC", _rpcCall);
        final Map<Node, InstanceUrl> mapping = new HashMap<Node, InstanceUrl>();

        for (Entry<Address, NodeResponse> entry : urls.entrySet()) {
            final Address address = entry.getKey();
            final NodeResponse urlResponse = entry.getValue();
            
            if (urlResponse.getStatus() == NodeResponse.Status.SUCCESS) {
                Object url = urlResponse.getValue();
                
                if (url instanceof InstanceUrl) {
                    mapping.put(new InfinispanNode(address),(InstanceUrl) url);
                }
                else {
                    throw new ClusterRequestRuntimeException("Invalid response contents: " + url);
                }
            }
            else {
                throw new ClusterRequestRuntimeException("Invalid response contents: " + urlResponse.getStatus());
            }
        }
        
        return mapping;
    }
    
    /**
     * Get local node's Address
     */
    public InstanceUrl getLocalNodeUrl() {
        return _localUrl;
    }
    
    /**
     * Determine if every host within the cluster is in the given cookie domain.
     */
    public boolean inCookieDomain(String domain)  {
        
        // Add each cluster URL.
        List<String> clusterHosts = new ArrayList<String>(); 
        for (URL url : getUrls()) {
            clusterHosts.add(url.getHost());
        }
        
        for (String clusterHost : clusterHosts) {
            int index = clusterHost.indexOf(domain);
            if (index == -1)
                return false;
        }
           
        return true;
    }
    
    /**
     * Get the local Node address.
     * @return Infinispan Address
     */
    public Address getInfinispanLocalAddress() {
        return ((InfinispanNode)_localNode).getAddress();
    }
    
    private boolean needsResync(Collection<Node> jGroupsMembers, Collection<Node> cacheMembers) {
        if (jGroupsMembers == null || cacheMembers == null) {
            throw new IllegalArgumentException("Illegal resync check: null");
        }
        return !(jGroupsMembers.containsAll(cacheMembers) && cacheMembers.containsAll(jGroupsMembers));
    }
    
    private boolean needsResync(Collection<Node> jGroupsMembers) {
        return needsResync(jGroupsMembers, _clusterUrls.getCache().keySet());
    }
    
    /**
     * Rebuild cluster URLS cache from RPC commands.
     * @param members actual members
     */
    synchronized
    private void rebuildClusterCacheWithLock(List<Node> members) {
        final Lock lock = _lockService.getLock(CLUSTER_URLS_LOCK);
        try {
            if (lock.tryLock(10, TimeUnit.SECONDS)) {
                try {
                    if (needsResync(members)) {
                        rebuildClusterUrlsFromRpc(members);
                    }
                }
                finally {
                    lock.unlock();
                }
            }
            else {
                _logger.warn("Unable to acquire cluster lock to rebuild caches!");
            }
        }
        catch (InterruptedException e) {
            _logger.warn("Lock thread interrupted.");
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Rebuild cluster URLS cache from RPC commands.
     * Re-add everyone to prevent bad merge scenario.
     * @param actualMembers actual members, this should come from Infinispan
     */
    synchronized
    private void rebuildClusterUrlsFromRpc(List<Node> actualMembers) {
        try {
            final Cache<Node, InstanceUrl> localClusterCache = _clusterUrls.getCache();
            final Set<Node> extraNodes = new HashSet<Node>(localClusterCache.keySet());
            final Map<Node, InstanceUrl> instanceUrls = getNodeUrlsOnCluster();
            
            // add everyone who should be in the cluster
            CacheUtils.cachePutAll(localClusterCache, instanceUrls, 0);
            
            // look for extra entries that we had and remove them
            extraNodes.removeAll(actualMembers);
            for (Node extraNode : extraNodes) {
                _logger.info("Removing Node URL " + extraNode + " from cluster cache.");
                CacheUtils.cacheRemove(localClusterCache, extraNode, 0);
            }
        }
        catch (ClusterRequestRuntimeException e) {
            _logger.warn("Unable to rebuild cluster URL from RPC: ", e);
        }
    }
    
    // Used to translate the string representation of an InstanceUrl
    // to find the underlying Infinispan Address
    private List<Address> translateUrlsToAddresses(List<String> instanceUrls) {
        if (instanceUrls == null || instanceUrls.isEmpty()) return null;
        
        // Swap the map so we can lookup by InstanceUrl as a string
        Map<Node, InstanceUrl> knownNodes = this.getKnownNodes();
        Map<String, Node> urlToNode = new HashMap<String, Node>(knownNodes.size());
        for (Map.Entry<Node, InstanceUrl> entry : knownNodes.entrySet()) {
            urlToNode.put(entry.getValue().toString(), entry.getKey());
        }
        
        List<Address> addresses = new ArrayList<Address>();
        for (String instanceUrl : instanceUrls) {
            Node node = urlToNode.get(instanceUrl);
            if (node == null) {
                throw new IllegalArgumentException("Provided an instance URL that doesn't exist: " + instanceUrl);
            }
                
            addresses.add(((InfinispanNode)node).getAddress());
        }
        
        return addresses;
    }
    
    private String getNodeSpecificLogMessage(List<Address> addresses) {
        return addresses == null ? "on the cluster" : "on node(s) [" + addresses + "]";
    }
    
    private void logClusterState(Map<Node, InstanceUrl> nodes) {
        int nodesSize = nodes.size();
        String nodeKeyword = nodesSize > 1 ? "nodes" : "node";
        _logger.info(String.format("Current cluster URLs [%d %s] : %s", nodesSize, nodeKeyword, nodes));
    }

    /**
     * RPC call is "delegated" out to a retry mechanism.
     */
    private final CacheUtils.CacheCallable<Map<Address, NodeResponse>> _rpcCall = new CacheUtils.CacheCallable() {
        @Override
        public Map<Address, NodeResponse> call() {
            return _clusterUrls.getNodeUrlsOnCluster();
        }
    };
    
    private final InstanceUrl _localUrl;
    private final Node _localNode;

    private final ClusterCaller<Node, InstanceUrl> _clusterUrls;
    private final JobManager _jobManager;
    private final TaskManager _taskManager;
    private final LockService _lockService;
    
    private static final String CLUSTER_URLS_LOCK = "cluster-urls";
   
    private final static Logger _logger = LogManager.getLogger(MocaClusterAdministration.class);
}
