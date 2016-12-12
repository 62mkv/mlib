/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.job;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.redprairie.moca.cluster.ClusterInformation;
import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.job.dao.JobDefinitionDAO;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test class for JobManager. This class uses several mock objects to
 * simulate a running environment.
 * 
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class TU_JobManager {

    @BeforeClass
    public static void beforeTests() throws SystemConfigurationException {
        ServerUtils.setupDaemonContext(TU_JobManager.class.getName(), true);
    }
    
    @Before
    public void beforeEachTest() {
        _clusterInformation = Mockito.mock(ClusterInformation.class);
        Mockito.when(_clusterInformation.isLeader()).thenReturn(false);
    }
    
    @Test
    public void testJobManagerStartAndStopRepeatedlyNotScheduling() throws JobException, JobRuntimeException, ParseException {
        int sync = Integer.MAX_VALUE;
        
        JobScheduler scheduler = Mockito.mock(JobScheduler.class);
        
        JobDefinition mockDefinition = Mockito.mock(JobDefinition.class);
        
        JobDefinitionDAO mockDAO = Mockito.mock(JobDefinitionDAO.class);
        Mockito.when(mockDAO.readForAllAndRoles()).thenReturn(
                Arrays.asList(mockDefinition));
        
        JobManager testJobManager = new JobManager(mockDAO, scheduler, 
            _clusterInformation, sync, false);
        
        int count = 100;
        for (int i = 0; i < count; i++) {
            testJobManager.start();
            testJobManager.stop();
        }
        
        Mockito.verify(scheduler, Mockito.times(count)).start();
        Mockito.verify(scheduler, Mockito.times(count)).stop();
        
        // We shouldn't ever add or remove any jobs since we aren't scheduling
        // and we don't explicitly add one
        Mockito.verify(scheduler, Mockito.never()).add(Mockito.any(JobDefinition.class));
        
        // TODO: should this actually  be called?  Since we didn't schedule anything
        // I would think that we wouldn't need to remove anything
        Mockito.verify(scheduler, Mockito.times(count)).remove(Mockito.any(JobDefinition.class));
    }
    
    @Test
    public void testJobManagerStartAndStopRepeatedlyWithScheduling() throws JobException, JobRuntimeException, ParseException {
        final String jobId = "a";
        int sync = Integer.MAX_VALUE;
        
        JobScheduler scheduler = Mockito.mock(JobScheduler.class);
        
        JobDefinition mockDefinition = Mockito.mock(JobDefinition.class);
        Mockito.when(mockDefinition.isEnabled()).thenReturn(true);
        Mockito.when(mockDefinition.getJobId()).thenReturn(jobId);
        
        JobDefinitionDAO mockDAO = Mockito.mock(JobDefinitionDAO.class);
        Mockito.when(mockDAO.readForAllAndRoles()).thenReturn(
                Arrays.asList(mockDefinition));
        
        JobManager testJobManager = new JobManager(mockDAO, scheduler, _clusterInformation, 
            sync, true);
        
        int count = 100;
        for (int i = 0; i < count; i++) {
            testJobManager.start();
            testJobManager.stop();
        }
        
        Mockito.verify(scheduler, Mockito.times(count)).start();
        Mockito.verify(scheduler, Mockito.times(count)).stop();
        
        Mockito.verify(scheduler, Mockito.times(count)).add(mockDefinition);
        Mockito.verify(scheduler, Mockito.times(count)).remove(mockDefinition);
    }
    
    @Test
    public void testJobManagerSheduleJob() throws Exception {
        final String jobId = "a";
        int sync = Integer.MAX_VALUE;
        
        JobScheduler scheduler = Mockito.mock(JobScheduler.class);
        
        JobDefinition mockDefinition = Mockito.mock(JobDefinition.class);
        Mockito.when(mockDefinition.isEnabled()).thenReturn(true);
        Mockito.when(mockDefinition.getJobId()).thenReturn(jobId);
        
        JobDefinitionDAO mockDAO = Mockito.mock(JobDefinitionDAO.class);
        Mockito.when(mockDAO.readForAllAndRoles()).thenReturn(
                Arrays.asList(mockDefinition));
        
        JobManager testJobManager = new JobManager(mockDAO, scheduler, _clusterInformation, 
            sync, false);
        
        testJobManager.start();
        
        try {
            testJobManager.scheduleJob(jobId);
        
        }
        finally {
            testJobManager.stop();
        }
        
        Mockito.verify(scheduler).start();
        Mockito.verify(scheduler).stop();
        
        Mockito.verify(scheduler).add(mockDefinition);
        Mockito.verify(scheduler).remove(mockDefinition);
    }
    
    @Test
    public void testJobManagerStartAndStopWithCache() throws Exception {
        final String jobId = "a";
        int sync = Integer.MAX_VALUE;
        
        JobScheduler scheduler = Mockito.mock(JobScheduler.class);
        
        // Create a mock Job.
        JobDefinition mockDefinition = Mockito.mock(JobDefinition.class);
        Mockito.when(mockDefinition.isEnabled()).thenReturn(true);
        Mockito.when(mockDefinition.getJobId()).thenReturn(jobId);
        
        JobDefinitionDAO mockDAO = Mockito.mock(JobDefinitionDAO.class);
        Mockito.when(mockDAO.readForAllAndRoles()).thenReturn(
                Arrays.asList(mockDefinition));
        
        // Create a Job manager to test.
        JobManager manager = new JobManager(mockDAO, scheduler, _clusterInformation, 
            sync, true);
        
        // Verify that the Job Cache is initially empty.
        ConcurrentMap<Node, Set<JobDefinition>> jobMap = manager.getClusteredJobs();
        assertEquals(0, jobMap.entrySet().size());
        
        try {
            // Start the Job Manager.
            manager.start();
        
            // Verify that the Cache now has one entry.
            Set<Entry<Node, Set<JobDefinition>>> entries = jobMap.entrySet();
            assertEquals(1, entries.size());
            Set<JobDefinition> jobs = entries.iterator().next().getValue();
            assertEquals(1, jobs.size());
            assertEquals(mockDefinition, jobs.iterator().next());
        }
        finally {
            // Stop the Job Manager.
            manager.stop();
        }
        
        // Verify now that the task Cache doesn't have any entries.
        Set<Entry<Node, Set<JobDefinition>>> entries = jobMap.entrySet();
        assertEquals(1, entries.size());
        Set<JobDefinition> jobs = entries.iterator().next().getValue();
        assertEquals(0, jobs.size());
    }
    
    @Test
    public void testJobManagerScheduleAndUnscheduleWithCache() throws Exception {
        final String jobId = "a";
        int sync = Integer.MAX_VALUE;
        
        JobScheduler scheduler = Mockito.mock(JobScheduler.class);
        
        // Create a mock Job.
        JobDefinition mockDefinition = Mockito.mock(JobDefinition.class);
        Mockito.when(mockDefinition.isEnabled()).thenReturn(true);
        Mockito.when(mockDefinition.getJobId()).thenReturn(jobId);
        
        JobDefinitionDAO mockDAO = Mockito.mock(JobDefinitionDAO.class);
        Mockito.when(mockDAO.readForAllAndRoles()).thenReturn(
                Arrays.asList(mockDefinition));
        
        // Create a Job manager to test.
        JobManager manager = new JobManager(mockDAO, scheduler, _clusterInformation, 
            sync, false);
        
        // Verify that the Job Cache is initially empty.
        ConcurrentMap<Node, Set<JobDefinition>> jobMap = manager.getClusteredJobs();
        assertEquals(0, jobMap.entrySet().size());
        
        try {
            // Start the Job Manager and schedule our test Job.
            manager.start();
            assertEquals(0, jobMap.entrySet().size());
            
            manager.scheduleJob(jobId);
        
            // Verify that the Cache now has one entry.
            Set<Entry<Node, Set<JobDefinition>>> entries = jobMap.entrySet();
            assertEquals(1, entries.size());
            Set<JobDefinition> jobs = entries.iterator().next().getValue();
            assertEquals(1, jobs.size());
            assertEquals(mockDefinition, jobs.iterator().next());
            
            // Unschedule the Job and stop the Job Manager.
            manager.unscheduleJob(jobId);
            
            // Verify now that the task Cache doesn't have any entries.
            entries = jobMap.entrySet();
            assertEquals(1, entries.size());
            jobs = entries.iterator().next().getValue();
            assertEquals(0, jobs.size());
        }
        finally {
            // Finally Stop the Job Manager.
            manager.stop();
        }
        
    }
    
    @Test
    public void testJobManagerScheduleNonExistentJob() throws Exception {
        final String jobId = "a";
        int sync = Integer.MAX_VALUE;
        
        JobScheduler scheduler = Mockito.mock(JobScheduler.class);
        
        JobDefinition mockDefinition = Mockito.mock(JobDefinition.class);
        Mockito.when(mockDefinition.isEnabled()).thenReturn(true);
        Mockito.when(mockDefinition.getJobId()).thenReturn(jobId);
        
        JobDefinitionDAO mockDAO = Mockito.mock(JobDefinitionDAO.class);
        Mockito.when(mockDAO.readForAllAndRoles()).thenReturn(
                Arrays.asList(mockDefinition));
        
        JobManager testJobManager = new JobManager(mockDAO, scheduler, _clusterInformation, 
            sync, false);
        
        testJobManager.start();
        
        try {
            testJobManager.scheduleJob("foo");
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            // Success
        }
        finally {
            testJobManager.stop();
        }
        
        InOrder order = Mockito.inOrder(scheduler);
        
        order.verify(scheduler).start();
        
        order.verify(scheduler).remove(mockDefinition);
        
        order.verify(scheduler).stop();
    }
    
    @Test
    public void testJobManagerUnscheduleJob() throws Exception {
        final String jobId = "a";
        int sync = Integer.MAX_VALUE;
        
        JobScheduler scheduler = Mockito.mock(JobScheduler.class);
        
        JobDefinition mockDefinition = Mockito.mock(JobDefinition.class);
        Mockito.when(mockDefinition.isEnabled()).thenReturn(true);
        Mockito.when(mockDefinition.getJobId()).thenReturn(jobId);
        
        JobDefinitionDAO mockDAO = Mockito.mock(JobDefinitionDAO.class);
        Mockito.when(mockDAO.readForAllAndRoles()).thenReturn(
                Arrays.asList(mockDefinition));
        
        JobManager testJobManager = new JobManager(mockDAO, scheduler, _clusterInformation, 
            sync, false);
        
        testJobManager.start();
        
        testJobManager.scheduleJob("a");
    
        testJobManager.unscheduleJob("a");
        
        testJobManager.stop();
        
        // these calls should be in the specific order.  We first start it
        // then we add the job and then remove it, but then stopping also
        // removes the definition and then we finally stop it.
        InOrder order = Mockito.inOrder(scheduler);
        
        order.verify(scheduler).start();
        
        order.verify(scheduler).add(mockDefinition);
        
        order.verify(scheduler, Mockito.times(2)).remove(mockDefinition);
        
        order.verify(scheduler).stop();
    }
    
    @Test
    public void testJobManagerUnscheduleNonExistentJob() throws Exception {
        final String jobId = "a";
        int sync = Integer.MAX_VALUE;
        
        JobScheduler scheduler = Mockito.mock(JobScheduler.class);
        
        JobDefinition mockDefinition = Mockito.mock(JobDefinition.class);
        Mockito.when(mockDefinition.isEnabled()).thenReturn(true);
        Mockito.when(mockDefinition.getJobId()).thenReturn(jobId);
        
        JobDefinitionDAO mockDAO = Mockito.mock(JobDefinitionDAO.class);
        Mockito.when(mockDAO.readForAllAndRoles()).thenReturn(
                Arrays.asList(mockDefinition));
        
        JobManager testJobManager = new JobManager(mockDAO, scheduler, _clusterInformation, 
            sync, false);
        
        testJobManager.start();
        
        try {
            testJobManager.unscheduleJob("foo");
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            // Success
        }
        finally {
            testJobManager.stop();
        }
        
        InOrder order = Mockito.inOrder(scheduler);
        
        order.verify(scheduler).start();
        
        order.verify(scheduler).remove(mockDefinition);
        
        order.verify(scheduler).stop();
    }
    
    @Test
    public void testJobManagerScheduling() throws Exception {
        final String jobId = "a";
        int sync = Integer.MAX_VALUE;
        
        JobDefinition mockDefinition = Mockito.mock(JobDefinition.class);
        Mockito.when(mockDefinition.isEnabled()).thenReturn(true);
        Mockito.when(mockDefinition.getJobId()).thenReturn(jobId);
        
        JobScheduler scheduler = Mockito.mock(JobScheduler.class);
        Mockito.when(scheduler.exists(mockDefinition)).thenReturn(true);
        
        JobDefinitionDAO mockDAO = Mockito.mock(JobDefinitionDAO.class);
        Mockito.when(mockDAO.readForAllAndRoles()).thenReturn(
                Arrays.asList(mockDefinition));
        
        JobManager testJobManager = new JobManager(mockDAO, scheduler, _clusterInformation, 
            sync, true);
        
        testJobManager.start();
        
        try {
            assertTrue("Expected job wasn't scheduled", 
                    testJobManager.isScheduled(jobId));
            
            Mockito.verify(scheduler).start();
            Mockito.verify(scheduler).add(mockDefinition);
            
            Mockito.verify(scheduler, Mockito.never()).remove(Mockito.any(
                    JobDefinition.class));
            Mockito.verify(scheduler, Mockito.never()).stop();
        }
        finally {
            testJobManager.stop();
        }
        
        Mockito.verify(scheduler).remove(mockDefinition);
        Mockito.verify(scheduler).stop();
    }
    
    @Test
    public void testJobManagerIsScheduledForNonExistentJob() throws Exception {
        final String jobId = "a";
        int sync = Integer.MAX_VALUE;
        
        JobScheduler scheduler = Mockito.mock(JobScheduler.class);
        
        JobDefinition mockDefinition = Mockito.mock(JobDefinition.class);
        Mockito.when(mockDefinition.isEnabled()).thenReturn(true);
        Mockito.when(mockDefinition.getJobId()).thenReturn(jobId);
        
        JobDefinitionDAO mockDAO = Mockito.mock(JobDefinitionDAO.class);
        Mockito.when(mockDAO.readForAllAndRoles()).thenReturn(
                Arrays.asList(mockDefinition));
        
        JobManager testJobManager = new JobManager(mockDAO, scheduler, _clusterInformation, 
            sync, true);
        
        testJobManager.start();
        
        Mockito.verify(scheduler).start();
        Mockito.verify(scheduler).add(mockDefinition);
        
        Mockito.verify(scheduler, Mockito.never()).remove(Mockito.any(
                JobDefinition.class));
        Mockito.verify(scheduler, Mockito.never()).stop();
        
        try {
            testJobManager.isScheduled("foo");
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            // Success
        }
        finally {
            testJobManager.stop();
        }
        
        Mockito.verify(scheduler).remove(mockDefinition);
        Mockito.verify(scheduler).stop();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testJobManagerChangeJobAtRuntime() throws Exception {
        final String jobId = "a";
        int sync = 2;
        
        JobScheduler scheduler = Mockito.mock(JobScheduler.class);
        
        JobDefinition jobA1 = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA1.isEnabled()).thenReturn(true);
        Mockito.when(jobA1.getJobId()).thenReturn(jobId);
        
        // This one is slightly different so should cause a reschedule/sync
        JobDefinition jobA2 = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA2.isEnabled()).thenReturn(true);
        Mockito.when(jobA2.getJobId()).thenReturn(jobId);
        Mockito.when(jobA2.getTimer()).thenReturn(4);
        
        JobDefinitionDAO mockDAO = Mockito.mock(JobDefinitionDAO.class);
        Mockito.when(mockDAO.readForAllAndRoles()).thenReturn(
                Arrays.asList(jobA1), Arrays.asList(jobA2));
        
        JobManager testJobManager = new JobManager(mockDAO, scheduler, _clusterInformation, 
            sync, true);
        
        testJobManager.start();
        try {
            Mockito.verify(scheduler).start();
            Mockito.verify(scheduler).add(jobA1);
            
            // Now we wait for the removal of jobA1 and readdition of jobA2
            Mockito.verify(scheduler, Mockito.timeout(sync * 1000 + 500)).remove(jobA1);
            
            // The job is added with 2 times sync for quarantine
            Mockito.verify(scheduler, Mockito.timeout(50)).add(jobA2, sync * 2);
        }
        finally {
            testJobManager.stop();
        }
        
        Mockito.verify(scheduler).remove(jobA2);
        Mockito.verify(scheduler).stop();
    }
    
    /**
     * This tests makes sure that when a new job is returned with everything
     * the same except the node id that no longer matches, that it will remove 
     * that job from the scheduler.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testJobManagerChangeNodeAtRuntime() throws Exception {
        final RoleDefinition node = Mockito.mock(RoleDefinition.class);
        final String jobId = "a";
        int sync = 2;
        
        JobScheduler scheduler = Mockito.mock(JobScheduler.class);
        
        JobDefinition jobA1 = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA1.isEnabled()).thenReturn(true);
        Mockito.when(jobA1.getJobId()).thenReturn(jobId);
        Mockito.when(jobA1.getRole()).thenReturn(node);
        
        // This one is slightly different so should cause a reschedule/sync
        JobDefinition jobA2 = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA2.isEnabled()).thenReturn(true);
        Mockito.when(jobA2.getJobId()).thenReturn(jobId);
        Mockito.when(jobA2.getTimer()).thenReturn(4);
        RoleDefinition def = Mockito.mock(RoleDefinition.class);
        Mockito.when(jobA2.getRole()).thenReturn(def);
        
        JobDefinitionDAO mockDAO = Mockito.mock(JobDefinitionDAO.class);
        Mockito.when(mockDAO.readForAllAndRoles(node)).thenReturn(
                Arrays.asList(jobA1), Arrays.asList(jobA2));
        
        JobManager testJobManager = new JobManager(mockDAO, scheduler, _clusterInformation, 
            sync, true);
        
        testJobManager.activateRole(node);
        
        testJobManager.start();
        try {
            Mockito.verify(scheduler).start();
            Mockito.verify(scheduler).add(jobA1);
            
            // Now we wait for the removal of jobA1 and readdition of jobA2
            Mockito.verify(scheduler, Mockito.timeout(sync * 1000 + 500)).remove(jobA1);
            Mockito.verify(scheduler).add(jobA2, sync * 2);
        }
        finally {
            testJobManager.stop();
        }
        
        Mockito.verify(scheduler).stop();
    }
    
    /**
     * This test makes sure that when the sync job finds that a job is removed
     * that will remove this job.
     * @throws JobException
     * @throws ParseException 
     * @throws JobRuntimeException 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testJobManagerRemoveJobAtRuntime() throws JobException, JobRuntimeException, ParseException {
        int sync = 2;
        
        JobScheduler scheduler = Mockito.mock(JobScheduler.class);
        
        JobDefinition jobA = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA.isEnabled()).thenReturn(true);
        Mockito.when(jobA.getJobId()).thenReturn("a");
        
        JobDefinition jobB = Mockito.mock(JobDefinition.class);
        Mockito.when(jobB.isEnabled()).thenReturn(true);
        Mockito.when(jobB.getJobId()).thenReturn("b");
        
        
        JobDefinitionDAO mockDAO = Mockito.mock(JobDefinitionDAO.class);
        // The second time we hit this jobA will go away.
        Mockito.when(mockDAO.readForAllAndRoles()).thenReturn(Arrays.asList(jobA, jobB),
                Arrays.asList(jobB));
        
        JobManager testJobManager = new JobManager(mockDAO, scheduler, _clusterInformation, 
            sync, true);
        
        testJobManager.start();
        try {
            Mockito.verify(scheduler).start();
            Mockito.verify(scheduler).add(jobA);
            Mockito.verify(scheduler).add(jobB);
            
            // Now we wait for the removal of jobA
            Mockito.verify(scheduler, Mockito.timeout(sync * 1000 + 500)).remove(jobA);
        }
        finally {
            testJobManager.stop();
        }
        
        Mockito.verify(scheduler).remove(jobB);
        Mockito.verify(scheduler).stop();
    }
    
    /**
     * This tests makes sure that if during a sync a new job is found that it
     * will add this job to the scheduler
     * @throws JobException 
     * @throws ParseException 
     * @throws JobRuntimeException 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testJobManagerAddJobAtRuntime() throws JobException, JobRuntimeException, ParseException {
        int sync = 2;
        
        JobScheduler scheduler = Mockito.mock(JobScheduler.class);
        
        JobDefinition jobA = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA.isEnabled()).thenReturn(true);
        Mockito.when(jobA.getJobId()).thenReturn("a");
        
        JobDefinition jobB = Mockito.mock(JobDefinition.class);
        Mockito.when(jobB.isEnabled()).thenReturn(true);
        Mockito.when(jobB.getJobId()).thenReturn("b");
        
        
        JobDefinitionDAO mockDAO = Mockito.mock(JobDefinitionDAO.class);
        // The second time we hit this jobA will go away.
        Mockito.when(mockDAO.readForAllAndRoles()).thenReturn(Arrays.asList(jobA), 
                Arrays.asList(jobA, jobB));
        
        JobManager testJobManager = new JobManager(mockDAO, scheduler, _clusterInformation, 
            sync, true);
        
        testJobManager.start();
        try {
            Mockito.verify(scheduler).start();
            Mockito.verify(scheduler).add(jobA);
            
            // Now we wait for the removal of jobA
            Mockito.verify(scheduler, Mockito.timeout(sync * 1000 + 500)).add(jobB, sync * 2);
        }
        finally {
            testJobManager.stop();
        }
        
        Mockito.verify(scheduler).remove(jobA);
        Mockito.verify(scheduler).remove(jobB);
        Mockito.verify(scheduler).stop();
    }
    
    @Test
    public void testJobManagerRestartContinuallyWhileSyncing() throws JobException, InterruptedException {
        final int sync = 1;
        
        JobScheduler scheduler = Mockito.mock(JobScheduler.class);
        
        JobDefinitionDAO mockDAO = Mockito.mock(JobDefinitionDAO.class);
        
        // We extend this class to put an artifical wait to easily reproduce the
        // deadlocking issue
        final JobManager testJobManager = new JobManager(mockDAO, scheduler, 
            _clusterInformation, sync, true) {
            // @see com.redprairie.moca.job.JobManager#stop()
            @Override
            public void stop() throws JobException {
                try {
                    Thread.sleep((sync * 2) * 1000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                super.stop();
            }
        };
        
        testJobManager.start();
        Thread thread = new Thread() {
            // @see java.lang.Thread#run()
            @Override
            public void run() {
                // This should now wait until sync happens and get stuck
                try {
                    testJobManager.restart();
                }
                catch (JobException e) {
                    e.printStackTrace();
                }
            }
        };
        
        thread.start();
        
        thread.join((sync * 3) * 1000);
        
        try {
            assertFalse("Thread didn't end properly, sign of deadlock maybe", 
                thread.isAlive());
        }
        catch (Error e) {
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            ThreadInfo[] threadInfos = threadBean.dumpAllThreads(true, true);
            System.out.println(Arrays.toString(threadInfos));
            throw e;
        }
        // We would like to call stop here, but can't if the manager is deadlocked
    }
    
    @Test
    public void testJobManagerStartWhileSyncing() throws JobException, 
            InterruptedException, BrokenBarrierException, TimeoutException, 
            ExecutionException {
        final int sync = 1;
        final String jobName = "test-Job123";
        
        // This barrier is trigger when the sync method is to tell us the sync
        // is now running
        final CyclicBarrier barrier = new CyclicBarrier(2);
        // This latch is so we can then try to start a job then let the sync
        // go, which helps detect possible deadlock conditions
        final CountDownLatch latch = new CountDownLatch(1);
        
        JobDefinitionDAO mockDAO = Mockito.mock(JobDefinitionDAO.class);
        final JobDefinition job = Mockito.mock(JobDefinition.class);
        Mockito.when(job.getJobId()).thenReturn(jobName);
        final AtomicInteger value = new AtomicInteger();
        // Make it so the job changes every time, which should cause a start
        // and stop action to occur
        Mockito.when(job.getCommand()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return String.valueOf(value.getAndIncrement());
            }
        });
        Mockito.when(mockDAO.readAll())
            .thenReturn(Collections.singletonList(job))
            .thenAnswer(new Answer<List<JobDefinition>>() {
                @Override
                public List<JobDefinition> answer(InvocationOnMock invocation)
                        throws Throwable {
                    barrier.await();
                    latch.await();
                    return Collections.singletonList(job);
                }
            });
        JobScheduler scheduler = Mockito.mock(JobScheduler.class);
        
        // We extend this class to put an artifical wait to easily reproduce the
        // deadlocking issue
        final JobManager testJobManager = new JobManager(mockDAO, scheduler, 
            null, sync, true);
        testJobManager.noCluster();
        
        testJobManager.start();
        
        // Wait for sync to happen first
        barrier.await(2, TimeUnit.SECONDS);
        
        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<Void> future = service.submit(new Callable<Void>() {
            // @see java.util.concurrent.Callable#call()
            @Override
            public Void call() throws JobException {
                // This should now wait until sync happens and get stuck
                testJobManager.scheduleJob(jobName);
                return null;
            }
        });
        
        // We now give the thread a chance to get blocked by the sync lock
        // which is expected!
        try {
            future.get(100, TimeUnit.MILLISECONDS);
            fail("Thread should not have completed yet");
        }
        catch (TimeoutException e) {
            // This should happen
        }
        
        // We now release the sync thread to do it's work
        latch.countDown();
        
        try {
         // This should now return properly
            future.get(2, TimeUnit.SECONDS);
        }
        catch (TimeoutException e) {
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            ThreadInfo[] threadInfos = threadBean.dumpAllThreads(true, true);
            System.out.println(Arrays.toString(threadInfos));
            throw e;
        }
        testJobManager.stop();
    }
    
    /**
     * This test makes sure that when the sync job finds that a job is removed
     * that will remove this job.
     * @throws JobException
     * @throws ParseException 
     * @throws JobRuntimeException 
     */
    @Test
    public void testJobManagerNotLeaderBecomesLeaderAddsJob() throws JobException, JobRuntimeException, ParseException {
        int sync = 1;
        
        JobScheduler scheduler = Mockito.mock(JobScheduler.class);
        
        JobDefinition jobA = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA.isEnabled()).thenReturn(true);
        Mockito.when(jobA.getJobId()).thenReturn("a");
        
        JobDefinition jobB = Mockito.mock(JobDefinition.class);
        Mockito.when(jobB.isEnabled()).thenReturn(true);
        Mockito.when(jobB.getJobId()).thenReturn("b");
        
        
        JobDefinitionDAO mockDAO = Mockito.mock(JobDefinitionDAO.class);
        // This method is called when not the leader
        Mockito.when(mockDAO.readForAllAndRoles()).thenReturn(Arrays.asList(jobB));
        // This method is called when we become the leader
        Mockito.when(mockDAO.readForAllNoRoleAndRoles(
            (RoleDefinition[])Mockito.anyVararg())).thenReturn(Arrays.asList(jobA, jobB));
        
        Mockito.when(_clusterInformation.isLeader()).thenReturn(false, true);
        
        JobManager testJobManager = new JobManager(mockDAO, scheduler, _clusterInformation, 
            sync, true);
        
        testJobManager.start();
        try {
            Mockito.verify(scheduler).start();
            Mockito.verify(scheduler).add(jobB);
            
            // Now we wait for the addition of jobA
            Mockito.verify(scheduler, Mockito.timeout((sync * 2) * 1000 + 500)).add(
                Mockito.eq(jobA), Mockito.anyInt());
        }
        finally {
            testJobManager.stop();
        }
        
        Mockito.verify(scheduler).remove(jobA);
        Mockito.verify(scheduler).remove(jobB);
        Mockito.verify(scheduler).stop();
    }
    
    /**
     * This tests makes sure that job notifications are sent to listeners appropriately.
     * @throws JobException 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testJobManagerNotifications() throws JobException {
        int sync = 2;
        
        JobScheduler scheduler = Mockito.mock(JobScheduler.class);
        
        JobDefinition jobA = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA.isEnabled()).thenReturn(true);
        Mockito.when(jobA.getJobId()).thenReturn("a");
        
        // Second job will show up on the sync but it's not enabled
        JobDefinition jobB = Mockito.mock(JobDefinition.class);
        Mockito.when(jobB.isEnabled()).thenReturn(false);
        Mockito.when(jobB.getJobId()).thenReturn("b");
        
        
        JobDefinition jobCEnabled = Mockito.mock(JobDefinition.class);
        Mockito.when(jobCEnabled.isEnabled()).thenReturn(true);
        Mockito.when(jobCEnabled.getJobId()).thenReturn("c");
        
        // Second time job C is turned off
        JobDefinition jobCDisabled = Mockito.mock(JobDefinition.class);
        Mockito.when(jobCDisabled.isEnabled()).thenReturn(false);
        Mockito.when(jobCDisabled.getJobId()).thenReturn("c");
        
        
        JobDefinitionDAO mockDAO = Mockito.mock(JobDefinitionDAO.class);
        // The second time we hit this jobA will go away and jobB will be added
        Mockito.when(mockDAO.readForAllAndRoles()).thenReturn(Arrays.asList(jobA,jobCEnabled), 
                Arrays.asList(jobB,jobCDisabled));
        
        JobManager testJobManager = new JobManager(mockDAO, scheduler, _clusterInformation, 
            sync, true);
        
        // Add a mock listener
        JobManagerListener mockListener = Mockito.mock(JobManagerListener.class);
        testJobManager.addListener(mockListener);
        
        testJobManager.start();
        Mockito.verify(mockListener).onStart(testJobManager);
        Mockito.verify(mockListener).onJobAdded(testJobManager, jobA);
        Mockito.verify(mockListener).onJobScheduled(testJobManager, jobA);
        Mockito.verify(mockListener).onJobAdded(testJobManager, jobCEnabled);
        Mockito.verify(mockListener).onJobScheduled(testJobManager, jobCEnabled);
        try {
            // Now we wait for the removal of jobA and adding of jobB
            Mockito.verify(mockListener, Mockito.timeout(sync * 1000 + 500)).onJobAdded(testJobManager, jobB);
            // JobB should be added but not scheduled
            Mockito.verify(mockListener, Mockito.times(0)).onJobScheduled(testJobManager, jobB);
            // Have to use timeout just incase the sync hasn't finished completely
            Mockito.verify(mockListener, Mockito.timeout(1000)).onJobUnscheduled(testJobManager, jobA);
            Mockito.verify(mockListener).onJobRemoved(testJobManager, jobA);
            
            // JobC should be changed to disabled
            Mockito.verify(mockListener, Mockito.timeout(1000)).onJobUnscheduled(testJobManager, jobCEnabled);
            Mockito.verify(mockListener).onJobChanged(testJobManager, jobCEnabled, jobCDisabled);
        }
        finally {
            testJobManager.stop();
            Mockito.verify(mockListener).onStop(testJobManager);
        }
    }
    
    /**
     * This tests makes sure that job notifications are sent to listeners appropriately
     * during restarts.
     * @throws JobException 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testJobManagerNotificationsOnRestart() throws JobException {
        // Set a high sync so it doesn't actually occur (we're going to restart instead)
        int sync = 600;
        
        JobScheduler scheduler = Mockito.mock(JobScheduler.class);
        
        JobDefinition jobA = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA.isEnabled()).thenReturn(true);
        Mockito.when(jobA.getJobId()).thenReturn("a");
        
        // Second job will show up after restart but it's not enabled
        JobDefinition jobB = Mockito.mock(JobDefinition.class);
        Mockito.when(jobB.isEnabled()).thenReturn(false);
        Mockito.when(jobB.getJobId()).thenReturn("b");
        
        // Third job will show up after restart and it IS enabled
        JobDefinition jobC = Mockito.mock(JobDefinition.class);
        Mockito.when(jobC.isEnabled()).thenReturn(true);
        Mockito.when(jobC.getJobId()).thenReturn("c");
        
        // Fourth job will be removed during restart
        JobDefinition jobD = Mockito.mock(JobDefinition.class);
        Mockito.when(jobD.isEnabled()).thenReturn(true);
        Mockito.when(jobD.getJobId()).thenReturn("d");
        
        
        JobDefinitionDAO mockDAO = Mockito.mock(JobDefinitionDAO.class);
        // The second time we hit this when restarting jobB and jobC will be added with jobD removed.
        Mockito.when(mockDAO.readForAllAndRoles()).thenReturn(Arrays.asList(jobA,jobD), 
                Arrays.asList(jobA,jobB,jobC));
        
        JobManager testJobManager = new JobManager(mockDAO, scheduler, _clusterInformation, 
            sync, true);
        
        // Add a mock listener
        JobManagerListener mockListener = Mockito.mock(JobManagerListener.class);
        testJobManager.addListener(mockListener);
        
        testJobManager.start();
        Mockito.verify(mockListener).onStart(testJobManager);
        Mockito.verify(mockListener).onJobAdded(testJobManager, jobA);
        Mockito.verify(mockListener).onJobScheduled(testJobManager, jobA);
        Mockito.verify(mockListener).onJobAdded(testJobManager, jobD);
        Mockito.verify(mockListener).onJobScheduled(testJobManager, jobD);
        try {
            // Need to mock out the scheduler to show the job is scheduled or not
            Mockito.when(scheduler.exists(jobA)).thenReturn(true);
            Mockito.when(scheduler.exists(jobB)).thenReturn(false);
            Mockito.when(scheduler.exists(jobC)).thenReturn(true);
            
            testJobManager.restart();
            
            // During restart Job A gets unscheduled then rescheduled
            Mockito.verify(mockListener).onJobUnscheduled(testJobManager, jobA);
			// It has been scheduled 2 times total now at this point (once on start and
			// then after restart as well)
            Mockito.verify(mockListener, Mockito.times(2)).onJobScheduled(testJobManager, jobA);
            
            // Job D should have been unscheduled and removed
            Mockito.verify(mockListener).onJobUnscheduled(testJobManager, jobD);
            Mockito.verify(mockListener).onJobRemoved(testJobManager, jobD);
            
            // Job B is new so it's added but NOT scheduled due to not being enabled
            Mockito.verify(mockListener).onJobAdded(testJobManager, jobB);
            Mockito.verify(mockListener, Mockito.times(0)).onJobScheduled(testJobManager, jobB);
            
            // Job C is added and scheduled
            Mockito.verify(mockListener).onJobAdded(testJobManager, jobC);
            Mockito.verify(mockListener).onJobScheduled(testJobManager, jobC);
            
            // Finally verify restart notification
            Mockito.verify(mockListener).onRestart(testJobManager);
        }
        finally {
            testJobManager.stop();
            Mockito.verify(mockListener).onStop(testJobManager);
        }
    }
    
    private ClusterInformation _clusterInformation;
}
