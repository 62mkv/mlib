/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.infinispan.Cache;
import org.infinispan.remoting.transport.Address;
import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.cache.infinispan.extension.api.ClusterCaller;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.NodeResponse;
import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.cluster.infinispan.InfinispanNode;
import com.redprairie.moca.job.JobDefinition;
import com.redprairie.moca.job.JobException;
import com.redprairie.moca.job.JobManager;
import com.redprairie.moca.server.InstanceUrl;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.task.TaskDefinition;
import com.redprairie.moca.task.TaskManager;

import static org.junit.Assert.*;

/**
 * Tests for MocaClusterAdministration
 * 
 * Copyright (c) 2013 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TU_MocaClusterAdministration {
    
    @Test
    public void testJobScheduleThatIsNotOnNode() throws JobException {
        NodeResponse response = testJobRunner(null, false, null, true);
        assertEquals(NodeResponse.noActionEmptyResponse(), response);
    }
    
    @Test
    public void testJobScheduleThatIsAlreadyRunning() throws JobException {
        NodeResponse response = testJobRunner(Mockito.mock(JobDefinition.class), true,
                null, true);
        assertEquals(NodeResponse.noActionEmptyResponse(), response);
    }
    
    @Test
    public void testJobScheduleSuccessful() throws JobException {
        NodeResponse response = testJobRunner(Mockito.mock(JobDefinition.class), false,
                null, true);
        assertEquals(NodeResponse.sucessfulEmptyResponse(), response);
    }
    
    /*
     * Test that an exception while scheduling the job bubbles up as an exception response from the node.
     * Also the NodeResponse should return the Exception and it should contains its original message.
     */
    @Test
    public void testJobScheduleException() throws JobException {
        final String message = "can't be scheduled!";
        NodeResponse response = testJobRunner(Mockito.mock(JobDefinition.class), false,
                new JobException(message), true);
        assertEquals(NodeResponse.Status.EXCEPTION, response.getStatus());
        assertTrue(response.getValue() instanceof Exception);
        assertTrue(((Exception)response.getValue()).getMessage().contains(message));
    }
    
    @Test
    public void testJobUnscheduleThatIsNotOnNode() throws JobException {
        NodeResponse response = testJobRunner(null, false, null, false);
        assertEquals(NodeResponse.noActionEmptyResponse(), response);
    }
    
    @Test
    public void testJobUnscheduleThatIsAlreadyStopped() throws JobException {
        NodeResponse response = testJobRunner(Mockito.mock(JobDefinition.class), false, null, false);
        assertEquals(NodeResponse.noActionEmptyResponse(), response);
    }
    
    @Test
    public void testJobUnscheduleSuccessful() throws JobException {
        NodeResponse response = testJobRunner(Mockito.mock(JobDefinition.class), true,
                null, false);
        assertEquals(NodeResponse.sucessfulEmptyResponse(), response);
    }
    
    /*
     * Test that an exception while unscheduling the job bubbles up as an exception response from the node.
     * Also the NodeResponse should return the Exception and it should contains its original message.
     */
    @Test
    public void testJobUnscheduleException() throws JobException {
        final String message = "can't be suncheduled!";
        NodeResponse response = testJobRunner(Mockito.mock(JobDefinition.class), true,
                new JobException(message), false);
        assertEquals(NodeResponse.Status.EXCEPTION, response.getStatus());
        assertTrue(response.getValue() instanceof Exception);
        assertTrue(((Exception)response.getValue()).getMessage().contains(message));
    }
    
    @Test
    public void testStartTaskSucces() {
        NodeResponse response = testTaskRunner(Mockito.mock(TaskDefinition.class), false, TASK_TEST.STARTING);
        assertEquals(NodeResponse.sucessfulEmptyResponse(), response);
    }
    
    @Test
    public void testStartTaskAlreadyRunning() {
        NodeResponse response = testTaskRunner(Mockito.mock(TaskDefinition.class), true, TASK_TEST.STARTING);
        assertEquals(NodeResponse.noActionEmptyResponse(), response);
    }
    
    @Test
    public void testStartTaskNotOnNode() {
        NodeResponse response = testTaskRunner(null, false, TASK_TEST.STARTING);
        assertEquals(NodeResponse.noActionEmptyResponse(), response);
    }
    
    @Test
    public void testStopTaskSuccess() {
        NodeResponse response = testTaskRunner(Mockito.mock(TaskDefinition.class), true, TASK_TEST.STOPPING);
        assertEquals(NodeResponse.sucessfulEmptyResponse(), response);
    }
    
    @Test
    public void testStopTaskAlreadyStopped() {
        NodeResponse response = testTaskRunner(Mockito.mock(TaskDefinition.class), false, TASK_TEST.STOPPING);
        assertEquals(NodeResponse.noActionEmptyResponse(), response);
    }
    
    @Test
    public void testStopTaskNotOnNode() {
        NodeResponse response = testTaskRunner(null, false, TASK_TEST.STOPPING);
        assertEquals(NodeResponse.noActionEmptyResponse(), response);
    }
    
    @Test
    public void testRestartTaskSuccess() {
        NodeResponse response = testTaskRunner(Mockito.mock(TaskDefinition.class), true, TASK_TEST.RESTARTING);
        assertEquals(NodeResponse.sucessfulEmptyResponse(), response);
    }
    
    @Test
    public void testRestartTaskAlreadyStopped() {
        NodeResponse response = testTaskRunner(Mockito.mock(TaskDefinition.class), false, TASK_TEST.RESTARTING);
        assertEquals(NodeResponse.noActionEmptyResponse(), response);
    }
    
    @Test
    public void testRestartTaskNotOnNode() {
        NodeResponse response = testTaskRunner(null, false, TASK_TEST.RESTARTING);
        assertEquals(NodeResponse.noActionEmptyResponse(), response);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testCommandsByInstanceUrl() {
        String instanceOne = "http://host1:4500";
        String instanceTwo = "https://host2:4500";
        SystemContext ctx = Mockito.mock(SystemContext.class);
        Node node = Mockito.mock(Node.class);
        JobManager jobManager = Mockito.mock(JobManager.class);
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        ClusterCaller<Node, InstanceUrl> caller = Mockito.mock(ClusterCaller.class);
        Cache<Node, InstanceUrl> cache = Mockito.mock(Cache.class);
        
        InfinispanNode mockNodeOne = Mockito.mock(InfinispanNode.class);
        Address mockAddressOne = Mockito.mock(Address.class);
        Mockito.when(mockNodeOne.getAddress()).thenReturn(mockAddressOne);
        InstanceUrl instanceUrlOne = InstanceUrl.fromChars(instanceOne);
        
        InfinispanNode mockNodeTwo = Mockito.mock(InfinispanNode.class);
        Address mockAddressTwo = Mockito.mock(Address.class);
        Mockito.when(mockNodeTwo.getAddress()).thenReturn(mockAddressTwo);
        InstanceUrl instanceUrlTwo = InstanceUrl.fromChars(instanceTwo);    
        
        Set<Entry<Node, InstanceUrl>> entrySet = new HashSet<Entry<Node, InstanceUrl>>(2);
        Entry<Node, InstanceUrl> entryOne = Mockito.mock(Entry.class);
        Mockito.when(entryOne.getKey()).thenReturn(mockNodeOne);
        Mockito.when(entryOne.getValue()).thenReturn(instanceUrlOne);
        entrySet.add(entryOne);
        
        Entry<Node, InstanceUrl> entryTwo = Mockito.mock(Entry.class);
        Mockito.when(entryTwo.getKey()).thenReturn(mockNodeTwo);
        Mockito.when(entryTwo.getValue()).thenReturn(instanceUrlTwo);
        entrySet.add(entryTwo);
        
        Mockito.when(cache.entrySet()).thenReturn(entrySet);
        Mockito.when(caller.getCache()).thenReturn(cache);
        List<String> ids = Mockito.mock(List.class);
        
        // Lets verify each command that we expect to support specifying
        // a subset of nodes actually takes them in.
        MocaClusterAdministration admin = new MocaClusterAdministration(ctx, node, jobManager, taskManager, caller, null);
        
        // Schedule jobs
        admin.scheduleJobOnCluster(ids, Arrays.asList(instanceOne, instanceTwo));
        Mockito.verify(caller).scheduleJobsOnCluster(ids, Arrays.asList(mockAddressOne, mockAddressTwo));
        
        // Unschedule jobs
        admin.unscheduleJobOnCluster(ids, Arrays.asList(instanceOne, instanceTwo));
        Mockito.verify(caller).unscheduleJobsOnCluster(ids, Arrays.asList(mockAddressOne, mockAddressTwo));
        
        // Start tasks
        admin.startTaskOnCluster(ids, Arrays.asList(instanceOne, instanceTwo));
        Mockito.verify(caller).startTasksOnCluster(ids, Arrays.asList(mockAddressOne, mockAddressTwo));
        
        // Stop tasks
        admin.stopTaskOnCluster(ids, Arrays.asList(instanceOne, instanceTwo));
        Mockito.verify(caller).stopTasksOnCluster(ids, Arrays.asList(mockAddressOne, mockAddressTwo));
        
        // Restart tasks
        admin.restartTaskOnCluster(ids, Arrays.asList(instanceOne, instanceTwo));
        Mockito.verify(caller).restartTasksOnCluster(ids, Arrays.asList(mockAddressOne, mockAddressTwo));
    }
    
    // Test sending bad values (request a node that doesn't exist)
    @SuppressWarnings("unchecked")
    @Test
    public void testCommandByInstanceUrlThatDoesNotExist() {
        String instanceOne = "http://host1:4500";
        // This second instance doesn't actually exist but we'll request it (causes exception)
        String instanceTwo = "https://host2:4500";
        
        SystemContext ctx = Mockito.mock(SystemContext.class);
        Node node = Mockito.mock(Node.class);
        JobManager jobManager = Mockito.mock(JobManager.class);
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        ClusterCaller<Node, InstanceUrl> caller = Mockito.mock(ClusterCaller.class);
        Cache<Node, InstanceUrl> cache = Mockito.mock(Cache.class);
        
        InfinispanNode mockNodeOne = Mockito.mock(InfinispanNode.class);
        Address mockAddressOne = Mockito.mock(Address.class);
        Mockito.when(mockNodeOne.getAddress()).thenReturn(mockAddressOne);
        InstanceUrl instanceUrlOne = InstanceUrl.fromChars(instanceOne);   
        
        Set<Entry<Node, InstanceUrl>> entrySet = new HashSet<Entry<Node, InstanceUrl>>(1);
        Entry<Node, InstanceUrl> entryOne = Mockito.mock(Entry.class);
        Mockito.when(entryOne.getKey()).thenReturn(mockNodeOne);
        Mockito.when(entryOne.getValue()).thenReturn(instanceUrlOne);
        entrySet.add(entryOne);

        Mockito.when(cache.entrySet()).thenReturn(entrySet);
        Mockito.when(caller.getCache()).thenReturn(cache);
        List<String> jobIds = Mockito.mock(List.class);
        
        MocaClusterAdministration admin = new MocaClusterAdministration(ctx, node, jobManager, taskManager, caller, null);
        try {
            admin.scheduleJobOnCluster(jobIds, Arrays.asList(instanceTwo));
            fail("IllegalArgumentException should have occurred because we request the " +
                 "second instance which doesn't actually exist in the cluster.");
        }
        catch (IllegalArgumentException expected) {
            // Exception should include the bad instance url
            assertTrue(expected.getMessage().contains(instanceTwo));
        }
    }
    
    private NodeResponse testJobRunner(JobDefinition job, boolean isScheduled, JobException expectedException,
            boolean testSchedule) throws JobException {
        String jobId = "job1";
        SystemContext ctx = Mockito.mock(SystemContext.class);
        Node node = Mockito.mock(Node.class);
        JobManager jobManager = Mockito.mock(JobManager.class);
        TaskManager taskManager = Mockito.mock(TaskManager.class);;
        Mockito.when(jobManager.getCurrentJob(jobId)).thenReturn(job);
        Mockito.when(jobManager.isScheduled(jobId)).thenReturn(isScheduled);
        
        // See if the test is setup to throw an exception
        if (expectedException != null) {
            if (testSchedule) {
                Mockito.doThrow(expectedException).when(jobManager).scheduleJob(jobId);
            }
            else {
                Mockito.doThrow(expectedException).when(jobManager).unscheduleJob(jobId);
            }
        }
        
        MocaClusterAdministration admin = new MocaClusterAdministration(ctx, node, jobManager, taskManager, buildMockCaller(), null);
        if (testSchedule) {
            return admin.scheduleJobOnNode(jobId);
        }
        else {
            return admin.unscheduleJobOnNode(jobId);
        }
    }
    
    private NodeResponse testTaskRunner(TaskDefinition taskDef, boolean isStarted, TASK_TEST testType) {
        String taskId = "task1";
        SystemContext ctx = Mockito.mock(SystemContext.class);
        Node node = Mockito.mock(Node.class);
        JobManager jobManager = Mockito.mock(JobManager.class);
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        Mockito.when(taskManager.getCurrentTask(taskId)).thenReturn(taskDef);
        Mockito.when(taskManager.isRunning(taskId)).thenReturn(isStarted);
        
        MocaClusterAdministration admin = new MocaClusterAdministration(ctx, node, jobManager, taskManager, buildMockCaller(), null);
        NodeResponse response = null;
        switch (testType) {
            case STARTING:   
                response = admin.startTaskOnNode(taskId);
                break;
            case STOPPING:
                response = admin.stopTaskOnNode(taskId);
                break;
            case RESTARTING: 
                response = admin.restartTaskOnNode(taskId);
                break;
        }
        
        return response;
    }
    
    private enum TASK_TEST {
        STARTING,
        STOPPING,
        RESTARTING
    }
    
    @SuppressWarnings("unchecked")
	private ClusterCaller<Node, InstanceUrl> buildMockCaller() {
    	ClusterCaller<Node, InstanceUrl> caller = Mockito.mock(ClusterCaller.class);
        Cache<Node, InstanceUrl> mockCache = Mockito.mock(Cache.class);
        Mockito.when(caller.getCache()).thenReturn(mockCache);
        return caller;
    }
}
