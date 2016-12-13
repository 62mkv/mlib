/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.job;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.moca.AsynchronousExecutor;
import com.redprairie.moca.job.dao.JobExecutionDAO;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * TODO Class Description
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_JobScheduler {
    @BeforeClass 
    public static void beforeTests() throws SystemConfigurationException {
        ServerUtils.setupDaemonContext(TU_JobScheduler.class.getName(), true);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testJobManagerWithSingleJob() throws JobException, InterruptedException, JobRuntimeException, ParseException {
        final int timer = 1;
        
        JobDefinition jobA = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA.getJobId()).thenReturn("a");
        Mockito.when(jobA.isEnabled()).thenReturn(true);
        Mockito.when(jobA.getTimer()).thenReturn(timer);
        Mockito.when(jobA.getCommand()).thenReturn("do thing");
        
        // We want every invocation to return success
        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class, Mockito.RETURNS_MOCKS);
        JobExecutionDAO jobExecDAO = Mockito.mock(JobExecutionDAO.class);
        AsynchronousExecutor async = Mockito.mock(AsynchronousExecutor.class, Mockito.RETURNS_MOCKS);
        MadFactory mFact = Mockito.mock(MadFactory.class);
        
        JobScheduler scheduler = new JobScheduler(new MocaJobFactory(
            factory, jobExecDAO, async, mFact), Mockito.mock(Map.class));
        
        scheduler.start();
        scheduler.add(jobA);
    
        int timesGoOff = 8; 
        
        // We wait until the (amount of times - 1) * timer goes off so we are 
        // on the last one
        Thread.sleep((long)(timesGoOff - 1) * timer * 1000L);
        // We want it to go off 8 times waiting for the timer for each
        Mockito.verify(async,
                // Now we should be able to wait until the last one goes off
                // thus fullfilling the amount of times it was supposed to go
                // off
                Mockito.timeout(timer * 1000).times(
                        timesGoOff)).executeAsynchronously(Mockito.any(Callable.class));
        
        scheduler.stop();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testJobManagerWithMultipleJobs() throws Exception {
        JobDefinition jobA = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA.getJobId()).thenReturn("a");
        Mockito.when(jobA.isEnabled()).thenReturn(true);
        Mockito.when(jobA.getTimer()).thenReturn(10);
        Mockito.when(jobA.getCommand()).thenReturn("do job A");
        
        JobDefinition jobB = Mockito.mock(JobDefinition.class);
        Mockito.when(jobB.getJobId()).thenReturn("b");
        Mockito.when(jobB.isEnabled()).thenReturn(true);
        Mockito.when(jobB.getStartDelay()).thenReturn(3);
        Mockito.when(jobB.getTimer()).thenReturn(4);
        Mockito.when(jobB.getCommand()).thenReturn("do job B");
        
        final List<String> executedCommands = new ArrayList<String>();
        final List<Long> timestamps = new ArrayList<Long>();
        
        AsynchronousExecutor async = Mockito.mock(AsynchronousExecutor.class, Mockito.RETURNS_MOCKS);
        Mockito.when(async.executeAsynchronously(Mockito.any(Callable.class)))
            .thenAnswer(new Answer<Future<?>>() {

                @Override
                public Future<?> answer(InvocationOnMock invocation)
                        throws Throwable {
                    executedCommands.add(((JobCallable) invocation
                        .getArguments()[0]).getCommand());
                    timestamps.add(System.currentTimeMillis());
                    return Mockito.mock(Future.class);
                }
            });
        
        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class, Mockito.RETURNS_MOCKS);
        JobExecutionDAO jobExecDAO = Mockito.mock(JobExecutionDAO.class);
        MadFactory mFact = Mockito.mock(MadFactory.class);
        
        JobScheduler scheduler = new JobScheduler(new MocaJobFactory(
            factory, jobExecDAO, async, mFact), Mockito.mock(Map.class));
        
        scheduler.add(jobA);
        scheduler.add(jobB);
        
        long startTime = System.currentTimeMillis();
        scheduler.start();
    
        // Hack! sleep for 13 seconds. That should let the commands execute:
        // A - at 0:00
        // B - at 0:03
        // B - at 0:07
        // A - at 0:10
        // B - at 0:11
        Thread.sleep(13000L);
        
        scheduler.stop();
        
        assertEquals(5, executedCommands.size());
        assertEquals("do job A", executedCommands.get(0));
        assertEquals("do job B", executedCommands.get(1));
        assertEquals("do job B", executedCommands.get(2));
        assertEquals("do job A", executedCommands.get(3));
        assertEquals("do job B", executedCommands.get(4));
        
        // We don't assert the first one's time since for some reason
        // quartz can have it run up to 2 seconds later....
        inTolerance(startTime + 3000L, timestamps.get(1));
        inTolerance(startTime + 7000L, timestamps.get(2));
        inTolerance(startTime + 10000L, timestamps.get(3));
        inTolerance(startTime + 11000L, timestamps.get(4));
    }   
    
    @SuppressWarnings("unchecked")
    @Test
    public void testJobManagerStartAndStopJobSchedule() throws Exception {
        JobDefinition jobA = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA.getJobId()).thenReturn("a");
        Mockito.when(jobA.isEnabled()).thenReturn(true);
        Mockito.when(jobA.getTimer()).thenReturn(3);
        Mockito.when(jobA.getCommand()).thenReturn("do job A");
        
        JobDefinition jobB = Mockito.mock(JobDefinition.class);
        Mockito.when(jobB.getJobId()).thenReturn("b");
        Mockito.when(jobB.isEnabled()).thenReturn(true);
        Mockito.when(jobB.getStartDelay()).thenReturn(2);
        Mockito.when(jobB.getTimer()).thenReturn(2);
        Mockito.when(jobB.getCommand()).thenReturn("do job B");
        
        final List<String> executedCommands = new ArrayList<String>();
        final List<Long> timestamps = new ArrayList<Long>();
        
        AsynchronousExecutor async = Mockito.mock(AsynchronousExecutor.class, Mockito.RETURNS_MOCKS);
        Mockito.when(async.executeAsynchronously(Mockito.any(Callable.class)))
            .thenAnswer(new Answer<Future<?>>() {

                @Override
                public Future<?> answer(InvocationOnMock invocation)
                        throws Throwable {
                    executedCommands.add(((JobCallable) invocation
                        .getArguments()[0]).getCommand());
                    timestamps.add(System.currentTimeMillis());
                    return Mockito.mock(Future.class);
                }
            });
        
        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class, Mockito.RETURNS_MOCKS);
        JobExecutionDAO jobExecDAO = Mockito.mock(JobExecutionDAO.class);
        MadFactory mFact = Mockito.mock(MadFactory.class);
        
        JobScheduler scheduler = new JobScheduler(new MocaJobFactory(
            factory, jobExecDAO, async, mFact), Mockito.mock(Map.class));
        
        scheduler.add(jobA);
        scheduler.add(jobB);

        long startTime = System.currentTimeMillis();
        System.out.println("Start time: " + new Date());
        scheduler.start();
        
        // Hack! sleep for 5 seconds. That should let the commands execute:
        // A - at 0:00
        // B - at 0:02
        // A - at 0:03
        // B - at 0:04
        Thread.sleep(5000L);
        
        // Now pause job B
        scheduler.remove(jobB);

        // Sleep then for 5 seconds this should let the commands execute:
        Thread.sleep(3500L);
        // Job A should continue
        // B - at 0:06 (skipped)
        // A - at 0:06
        // B - at 0:08 (skipped)
        
        scheduler.add(jobB);
        
        // Sleep then for 4.5 seconds this should let the commands execute:
        Thread.sleep(4500L);
        // Job B resumes
        // A - at 0:09
        // B - at 0:10.5 (just started)
        // A - at 0:12
        // B - at 0:12.5

        scheduler.stop();
        
        assertEquals(9, executedCommands.size());
        assertEquals("do job A", executedCommands.get(0));
        assertEquals("do job B", executedCommands.get(1));
        assertEquals("do job A", executedCommands.get(2));
        assertEquals("do job B", executedCommands.get(3));
        assertEquals("do job A", executedCommands.get(4));
        assertEquals("do job A", executedCommands.get(5));
        assertEquals("do job B", executedCommands.get(6));
        assertEquals("do job A", executedCommands.get(7));
        assertEquals("do job B", executedCommands.get(8));
        
        // We don't assert the first one's time since for some reason
        // quartz can have it run up to 2 seconds later....
        inTolerance(startTime + 2000L, timestamps.get(1));
        inTolerance(startTime + 3000L, timestamps.get(2));
        inTolerance(startTime + 4000L, timestamps.get(3));
        inTolerance(startTime + 6000L, timestamps.get(4));
        inTolerance(startTime + 9000L, timestamps.get(5));
        inTolerance(startTime + 10500L, timestamps.get(6));
        inTolerance(startTime + 12000L, timestamps.get(7));
        inTolerance(startTime + 12500L, timestamps.get(8));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testJobManagerWithEnvironmentOnJobScheduler() throws Exception {
        final int timer = 1;
        String userId = "SpecialUserX12B";
        final String variable = "usr_id";
        
        JobDefinition jobA = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA.getJobId()).thenReturn("a");
        Mockito.when(jobA.isEnabled()).thenReturn(true);
        Mockito.when(jobA.getTimer()).thenReturn(timer);
        Mockito.when(jobA.getCommand()).thenReturn("do thing");
        
        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class, Mockito.RETURNS_MOCKS);
        AsynchronousExecutor async = Mockito.mock(AsynchronousExecutor.class, Mockito.RETURNS_MOCKS);
        JobExecutionDAO jobExecDAO = Mockito.mock(JobExecutionDAO.class);
        MadFactory mFact = Mockito.mock(MadFactory.class);
        
        // We put an environment variable on the scheduler as well, we want
        // the job to overwrite it
        JobScheduler scheduler = new JobScheduler(new MocaJobFactory(
            factory, jobExecDAO, async, mFact), Collections.singletonMap(variable, userId));
        
        scheduler.start();
        scheduler.add(jobA);
    
        ArgumentCaptor<Callable> captor = ArgumentCaptor.forClass(
            Callable.class);
        // Now we have to wait until it goes off once.
        Mockito.verify(async, Mockito.timeout(timer * 1000))
            .executeAsynchronously(captor.capture());
        
        scheduler.stop();
        
        JobCallable callable = (JobCallable)captor.getValue();
        
        Map<String, String> env = callable.getEnv();
        
        assertEquals(userId, env.get(variable));
    }
    
    /**
     * This test is essentially the same as the previous except the job and
     * scheduler both have an environment variable.  We should grab the
     * environment from the job over the scheduler.
     * @throws Exception
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testJobManagerWithEnvironmentOnJob() throws Exception {
        final int timer = 1;
        String userId = "SpecialUserX12B";
        final String variable = "usr_id";
        
        JobDefinition jobA = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA.getJobId()).thenReturn("a");
        Mockito.when(jobA.isEnabled()).thenReturn(true);
        Mockito.when(jobA.getTimer()).thenReturn(timer);
        Mockito.when(jobA.getCommand()).thenReturn("do thing");
        Mockito.when(jobA.getEnvironment()).thenReturn(
                Collections.singletonMap(variable, userId));
        
        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class, Mockito.RETURNS_MOCKS);
        AsynchronousExecutor async = Mockito.mock(AsynchronousExecutor.class, Mockito.RETURNS_MOCKS);
        JobExecutionDAO jobExecDAO = Mockito.mock(JobExecutionDAO.class);
        MadFactory mFact = Mockito.mock(MadFactory.class);
        
        // We put an environment variable on the scheduler as well, we want
        // the job to overwrite it
        JobScheduler scheduler = new JobScheduler(new MocaJobFactory(
            factory, jobExecDAO, async, mFact), Collections.singletonMap(variable, 
                "not me please"));
        
        scheduler.start();
        scheduler.add(jobA);
    
        ArgumentCaptor<Callable> captor = ArgumentCaptor.forClass(
            Callable.class);
        Mockito.verify(async, Mockito.timeout(timer * 1000))
            .executeAsynchronously(captor.capture());
        
        scheduler.stop();
        
        JobCallable callable = (JobCallable)captor.getValue();
        
        Map<String, String> env = callable.getEnv();
        
        assertEquals(userId, env.get(variable));
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testJobManagerWithEnvironmentOnMultipleJobs() throws Exception {
        final int timer = 2;
        final int startDelay = 1;
        String userIdBase = "SpecialUser";
        String userIdJobA = "SpecialUserX12A";
        String userIdJobB = "SpecialUserX99B";
        final String variable = "usr_id";
        
        JobDefinition jobA = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA.getJobId()).thenReturn("a");
        Mockito.when(jobA.isEnabled()).thenReturn(true);
        Mockito.when(jobA.getTimer()).thenReturn(timer);
        Mockito.when(jobA.getCommand()).thenReturn("do thing");
        Mockito.when(jobA.getEnvironment()).thenReturn(Collections.singletonMap(variable, userIdJobA));
        
        JobDefinition jobB = Mockito.mock(JobDefinition.class);
        Mockito.when(jobB.getJobId()).thenReturn("b");
        Mockito.when(jobB.isEnabled()).thenReturn(true);
        Mockito.when(jobB.getTimer()).thenReturn(timer);
        Mockito.when(jobB.getStartDelay()).thenReturn(startDelay);
        Mockito.when(jobB.getCommand()).thenReturn("do thing");
        Mockito.when(jobB.getEnvironment()).thenReturn(Collections.singletonMap(variable, userIdJobB));
        
        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class, Mockito.RETURNS_MOCKS);
        AsynchronousExecutor async = Mockito.mock(AsynchronousExecutor.class, Mockito.RETURNS_MOCKS);
        JobExecutionDAO jobExecDAO = Mockito.mock(JobExecutionDAO.class);
        MadFactory mFact = Mockito.mock(MadFactory.class);
        
        // We put an environment variable on the scheduler as well, we want
        // the job to overwrite it
        JobScheduler scheduler = new JobScheduler(new MocaJobFactory(
            factory, jobExecDAO, async, mFact), Collections.singletonMap(variable, 
                userIdBase));
        
        scheduler.start();
        scheduler.add(jobA);
        scheduler.add(jobB);
    
        ArgumentCaptor<Callable> captor = ArgumentCaptor.forClass(
            Callable.class);
        
        // Now we have to wait until it goes off twice.
        
        Mockito.verify(async,
            Mockito.timeout((timer + startDelay) * 1000 + 200).times(2))
            .executeAsynchronously(captor.capture());
        
        scheduler.stop();
        
        JobCallable callable = (JobCallable)captor.getAllValues().get(0);
        
        Map<String, String> env = callable.getEnv();
        
        assertEquals(userIdJobA, env.get(variable));
        
        JobCallable callable2 = (JobCallable)captor.getAllValues().get(1);
        
        Map<String, String> env2 = callable2.getEnv();
        
        assertEquals(userIdJobB, env2.get(variable));
    }
    
    @Test(expected=ParseException.class)
    public void testInvalidSchedule() throws JobRuntimeException, JobException, ParseException {
        JobDefinition jobA = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA.getJobId()).thenReturn("a");
        Mockito.when(jobA.isEnabled()).thenReturn(true);
        // Invalid schedule should result in a ParseException when adding
        Mockito.when(jobA.getSchedule()).thenReturn("0 0 * * *");
        Mockito.when(jobA.getCommand()).thenReturn("do thing");
        
        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class, Mockito.RETURNS_MOCKS);
        AsynchronousExecutor async = Mockito.mock(AsynchronousExecutor.class, Mockito.RETURNS_MOCKS);
        JobExecutionDAO jobExecDAO = Mockito.mock(JobExecutionDAO.class);
        MadFactory mFact = Mockito.mock(MadFactory.class);
        
        // We put an environment variable on the scheduler as well, we want
        // the job to overwrite it
        JobScheduler scheduler = new JobScheduler(new MocaJobFactory(
            factory, jobExecDAO, async, mFact), Collections.singletonMap("usr_id", 
                "SUPER"));
        
        scheduler.start();
        // ParseException should be thrown here
        scheduler.add(jobA);
    }
    
    /*
     * Make sure the given expected and actual msecs are within tolerance of each other. 
     */
    private static void inTolerance(long expected, long actual) {
        if (!(Math.abs(expected - actual) < 500L)) {
            DateTimeFormatter dateFormat = DateTimeFormat.forPattern("H:mm:ss,S");

            fail(" Expected: " + dateFormat.print(new Date(expected).getTime()) +
                "     Actual: " + dateFormat.print(new Date(actual).getTime()) + 
                "     Difference: " + Math.abs(expected - actual) + " msecs");
        }
    }
}
