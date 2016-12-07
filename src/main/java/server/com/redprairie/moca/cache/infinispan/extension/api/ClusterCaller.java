/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.cache.infinispan.extension.api;

import java.util.List;
import java.util.Map;

import org.infinispan.Cache;
import org.infinispan.context.InvocationContextContainer;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.interceptors.InterceptorChain;
import org.infinispan.remoting.transport.Address;

import com.redprairie.moca.cache.infinispan.extension.CommandInitializer;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.GetNodeUrlsCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.NodeResponse;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.RestartServerCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.RestartTasksCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.ScheduleJobsCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.StartTasksCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.StopTasksCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.UnscheduleJobsCommand;
import com.redprairie.moca.cache.infinispan.extension.interceptors.MocaInterceptor;
import com.redprairie.moca.cluster.Node;

/**
 * This is a wrapper around an infinispan Cache object to allow for arbitrary
 * method invocations.
 * 
 * Copyright (c) 2012 RedPrairie Corporation All Rights Reserved
 * 
 * @author wburns
 */
public class ClusterCaller<K, V> {
    private final Cache<K, V> cache;
    private final CommandInitializer commandBuilder;
    private final InterceptorChain invoker;
    private final InvocationContextContainer invocationContextContainer;

    public ClusterCaller(Cache<K, V> cache, Node localNode) {
        if (!cache.getStatus().allowInvocations())
            throw new IllegalStateException("Cache instance not running!");
        this.cache = cache;
        ComponentRegistry cr = cache.getAdvancedCache().getComponentRegistry();
        commandBuilder = cr.getComponent(CommandInitializer.class);
        invoker = cr.getComponent(InterceptorChain.class);
        invocationContextContainer = cr
            .getComponent(InvocationContextContainer.class);
        
        MocaInterceptor i = new MocaInterceptor(localNode);
        cr.registerComponent(i, MocaInterceptor.class);
        cache.getAdvancedCache().addInterceptor(i, 1);
    }

    public Cache<K, V> getCache() {
        return cache;
    }

    /**
     * Restart all server cluster nodes
     * @throws Throwable 
     */
    public void performRestartServerOnCluster() {
        // Need to confirm if this does cluster wide or not
        RestartServerCommand c = commandBuilder
            .buildPerformRestartServerCommand();
        invoker.invoke(invocationContextContainer.createInvocationContext(false, 0), c);
    }
    
    /**
     * Schedule the list of jobs across the cluster
     * @param jobIds The list of jobs
     * @param requestAddresses The list of addresses the request should go to
     */
    public void scheduleJobsOnCluster(List<String> jobIds, List<Address> requestAddresses) {
        ScheduleJobsCommand c = commandBuilder.buildScheduleJobsCommand(jobIds,
                requestAddresses);
        invoker.invoke(invocationContextContainer.createInvocationContext(false, 0), c);
    }
    
    /**
     * Unschedule the list of jobs across the cluster
     * @param jobIds The list of jobs
     * @param requestAddresses The list of addresses the request should go to
     */
    public void unscheduleJobsOnCluster(List<String> jobIds, List<Address> requestAddresses) {
        UnscheduleJobsCommand c = commandBuilder.buildUnscheduleJobsCommand(jobIds,
                requestAddresses);
        invoker.invoke(invocationContextContainer.createInvocationContext(false, 0), c);
    }
    
    /**
     * Starts the list of tasks across the cluster
     * @param taskIds The lists of tasks
     * @param requestAddresses The list of addresses the request should go to
     */
    public void startTasksOnCluster(List<String> taskIds, List<Address> requestAddresses) {
        StartTasksCommand c = commandBuilder.buildStartTasksCommand(taskIds,
                requestAddresses);
        invoker.invoke(invocationContextContainer.createInvocationContext(false, 0), c);
    }
    
    /**
     * Stops the list of tasks across the cluster
     * @param taskIds The list of tasks
     * @param requestAddresses The list of addresses the request should go to
     */
    public void stopTasksOnCluster(List<String> taskIds, List<Address> requestAddresses) {
        StopTasksCommand c = commandBuilder.buildStopTasksCommand(taskIds,
                requestAddresses);
        invoker.invoke(invocationContextContainer.createInvocationContext(false, 0), c);
    }
    
    /**
     * Restarts the list of tasks across the cluster
     * @param taskIds The list of tasks
     * @param requestAddresses The list of addresses the request should go to
     */
    public void restartTasksOnCluster(List<String> taskIds, List<Address> requestAddresses) {
        RestartTasksCommand c = commandBuilder.buildRestartTasksCommand(taskIds,
                requestAddresses);
        invoker.invoke(invocationContextContainer.createInvocationContext(false, 0), c);
    }
    
    /**
     * Get the cluster node URLS from the cluster.
     * @return Map of addresses to NodeResponse objects.
     */
    @SuppressWarnings("unchecked")
    public Map<Address, NodeResponse> getNodeUrlsOnCluster() {
        GetNodeUrlsCommand c = commandBuilder.buildGetNodeUrlsCommand();
        return (Map<Address, NodeResponse>) invoker.invoke(invocationContextContainer.createInvocationContext(false, 0), c);
    }
}
