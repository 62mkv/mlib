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

package com.redprairie.moca.job;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Test;
import org.mockito.Mockito;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.TriggerFiredBundle;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.mad.client.MadGenericGauge;
import com.redprairie.mad.client.MadName;
import com.redprairie.mad.client.MadNameImpl;
import com.redprairie.moca.AsynchronousExecutor;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.exceptions.UnexpectedException;
import com.redprairie.moca.job.dao.JobExecutionDAO;
import com.redprairie.moca.mad.MonitoringUtils;
import com.redprairie.moca.probes.jobs.JobProbe;
import com.redprairie.moca.server.ServerContextFactory;

/**
 * Moca Job Factory test unit
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TU_MocaJobFactory {
    
    /**
     * Tests that on a successful execution the status is set to 0 on the execution probe.
     * @throws SchedulerException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testJobStatusOkay() throws SchedulerException {
        // Mock job
        JobDefinition jobA = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA.getJobId()).thenReturn("a");
        Mockito.when(jobA.isEnabled()).thenReturn(true);
        Mockito.when(jobA.getTimer()).thenReturn(15);
        Mockito.when(jobA.getCommand()).thenReturn("do thing");
        Map<String, String> env = Collections.singletonMap("usr_id", "SUPER");
        Mockito.when(jobA.getEnvironment()).thenReturn(env);
        
        // Mock quartz behavior
        TriggerFiredBundle fired = Mockito.mock(TriggerFiredBundle.class);
        Scheduler scheduler = Mockito.mock(Scheduler.class);
        JobDetail detail = Mockito.mock(JobDetail.class);
        JobDataMap dataMap = Mockito.mock(JobDataMap.class);
        Mockito.when(dataMap.get("job")).thenReturn(jobA);
        Mockito.when(dataMap.get("env")).thenReturn(env);

        Mockito.when(fired.getJobDetail()).thenReturn(detail);
        Mockito.doReturn(LocalCommandJob.class).when(detail).getJobClass();
        Mockito.when(detail.getJobDataMap()).thenReturn(dataMap);
        
        JobExecutionContext mockCtx = Mockito.mock(JobExecutionContext.class);
        Mockito.when(mockCtx.getJobDetail()).thenReturn(detail);
        
        // Other mocks
        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class, Mockito.RETURNS_MOCKS);
        AsynchronousExecutor async = Mockito.mock(AsynchronousExecutor.class, Mockito.RETURNS_MOCKS);
        JobExecutionDAO jobExecDAO = Mockito.mock(JobExecutionDAO.class);
        
        // Mock out the MAD Factory and the returned gauge so we can verify its usage
        MadFactory mFact = Mockito.mock(MadFactory.class);
        MadName expectedMadName = new MadNameImpl(MonitoringUtils.MOCA_GROUP_NAME, JobProbe.TYPE_JOBS, JobProbe.LAST_EXECUTION_STATUS, "a");
        Mockito.when(mFact.newMadName(MonitoringUtils.MOCA_GROUP_NAME, JobProbe.TYPE_JOBS, JobProbe.LAST_EXECUTION_STATUS, "a")).thenReturn(expectedMadName);
        MadGenericGauge lastStatusGauge = Mockito.mock(MadGenericGauge.class);
        Mockito.when(mFact.newGauge(Mockito.eq(expectedMadName), Mockito.any(MadGenericGauge.class))).thenReturn(lastStatusGauge);
        
        // Actual test
        MocaJobFactory jobFactory = new MocaJobFactory(factory, jobExecDAO, async, mFact);
        Job createdJob = jobFactory.newJob(fired, scheduler);
        createdJob.execute(mockCtx);
        
        // Last status probe should be set to 0
        Mockito.verify(mFact).newGauge(Mockito.eq(expectedMadName), Mockito.any(MadGenericGauge.class));
        Mockito.verify(lastStatusGauge).setValue(0);
    }
    
    /**
     * Tests that the proper Moca Exception code is set on the execution status probe.
     * @throws SchedulerException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testJobStatusMocaException() throws SchedulerException, InterruptedException, ExecutionException {
        // Mock job
        JobDefinition jobA = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA.getJobId()).thenReturn("a");
        Mockito.when(jobA.isEnabled()).thenReturn(true);
        Mockito.when(jobA.getTimer()).thenReturn(15);
        Mockito.when(jobA.getCommand()).thenReturn("do thing");
        Map<String, String> env = Collections.singletonMap("usr_id", "SUPER");
        Mockito.when(jobA.getEnvironment()).thenReturn(env);
        
        // Mock quartz behavior
        TriggerFiredBundle fired = Mockito.mock(TriggerFiredBundle.class);
        Scheduler scheduler = Mockito.mock(Scheduler.class);
        JobDetail detail = Mockito.mock(JobDetail.class);
        JobDataMap dataMap = Mockito.mock(JobDataMap.class);
        Mockito.when(dataMap.get("job")).thenReturn(jobA);
        Mockito.when(dataMap.get("env")).thenReturn(env);

        Mockito.when(fired.getJobDetail()).thenReturn(detail);
        Mockito.doReturn(LocalCommandJob.class).when(detail).getJobClass();
        Mockito.when(detail.getJobDataMap()).thenReturn(dataMap);
        
        JobExecutionContext mockCtx = Mockito.mock(JobExecutionContext.class);
        Mockito.when(mockCtx.getJobDetail()).thenReturn(detail);
        
        // Other mocks
        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class, Mockito.RETURNS_MOCKS);
        AsynchronousExecutor async = Mockito.mock(AsynchronousExecutor.class, Mockito.RETURNS_MOCKS);
        JobExecutionDAO jobExecDAO = Mockito.mock(JobExecutionDAO.class);
        
        // Mock out the MAD Factory and the returned gauge so we can verify its usage
        MadFactory mFact = Mockito.mock(MadFactory.class);
        MadName expectedMadName = new MadNameImpl(MonitoringUtils.MOCA_GROUP_NAME, JobProbe.TYPE_JOBS, JobProbe.LAST_EXECUTION_STATUS, "a");
        Mockito.when(mFact.newMadName(MonitoringUtils.MOCA_GROUP_NAME, JobProbe.TYPE_JOBS, JobProbe.LAST_EXECUTION_STATUS, "a")).thenReturn(expectedMadName);
        MadGenericGauge lastStatusGauge = Mockito.mock(MadGenericGauge.class);
        Mockito.when(mFact.newGauge(Mockito.eq(expectedMadName), Mockito.any(MadGenericGauge.class))).thenReturn(lastStatusGauge);
        
        // Mock out the future to throw a NotFoundException (MocaException)
        Future mockFuture = Mockito.mock(Future.class);
        Mockito.when(mockFuture.get()).thenThrow(new ExecutionException(new NotFoundException()));
        Mockito.when(async.executeAsynchronously(Mockito.any(Callable.class))).thenReturn(mockFuture);
        
        // Actual test
        MocaJobFactory jobFactory = new MocaJobFactory(factory, jobExecDAO, async, mFact);
        Job createdJob = jobFactory.newJob(fired, scheduler);
        createdJob.execute(mockCtx);
        
        // Last status probe should be set to -1403
        Mockito.verify(mFact).newGauge(Mockito.eq(expectedMadName), Mockito.any(MadGenericGauge.class));
        Mockito.verify(lastStatusGauge).setValue(NotFoundException.DB_CODE);
    }
    
    /**
     * Tests that unexpected exception status is set correctly on execution probe.
     * @throws SchedulerException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testJobStatusUnexpectedException() throws SchedulerException, InterruptedException, ExecutionException {
        // Mock job
        JobDefinition jobA = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA.getJobId()).thenReturn("a");
        Mockito.when(jobA.isEnabled()).thenReturn(true);
        Mockito.when(jobA.getTimer()).thenReturn(15);
        Mockito.when(jobA.getCommand()).thenReturn("do thing");
        Map<String, String> env = Collections.singletonMap("usr_id", "SUPER");
        Mockito.when(jobA.getEnvironment()).thenReturn(env);
        
        // Mock quartz behavior
        TriggerFiredBundle fired = Mockito.mock(TriggerFiredBundle.class);
        Scheduler scheduler = Mockito.mock(Scheduler.class);
        JobDetail detail = Mockito.mock(JobDetail.class);
        JobDataMap dataMap = Mockito.mock(JobDataMap.class);
        Mockito.when(dataMap.get("job")).thenReturn(jobA);
        Mockito.when(dataMap.get("env")).thenReturn(env);

        Mockito.when(fired.getJobDetail()).thenReturn(detail);
        Mockito.doReturn(LocalCommandJob.class).when(detail).getJobClass();
        Mockito.when(detail.getJobDataMap()).thenReturn(dataMap);
        
        JobExecutionContext mockCtx = Mockito.mock(JobExecutionContext.class);
        Mockito.when(mockCtx.getJobDetail()).thenReturn(detail);
        
        // Other mocks
        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class, Mockito.RETURNS_MOCKS);
        AsynchronousExecutor async = Mockito.mock(AsynchronousExecutor.class, Mockito.RETURNS_MOCKS);
        JobExecutionDAO jobExecDAO = Mockito.mock(JobExecutionDAO.class);
        
        // Mock out the MAD Factory and the returned gauge so we can verify its usage
        MadFactory mFact = Mockito.mock(MadFactory.class);
        MadName expectedMadName = new MadNameImpl(MonitoringUtils.MOCA_GROUP_NAME, JobProbe.TYPE_JOBS, JobProbe.LAST_EXECUTION_STATUS, "a");
        Mockito.when(mFact.newMadName(MonitoringUtils.MOCA_GROUP_NAME, JobProbe.TYPE_JOBS, JobProbe.LAST_EXECUTION_STATUS, "a")).thenReturn(expectedMadName);
        MadGenericGauge lastStatusGauge = Mockito.mock(MadGenericGauge.class);
        Mockito.when(mFact.newGauge(Mockito.eq(expectedMadName), Mockito.any(MadGenericGauge.class))).thenReturn(lastStatusGauge);
        
        // Mock out the future to throw a RuntimeException (should be reported as UnexpectedException)
        Future mockFuture = Mockito.mock(Future.class);
        Mockito.when(mockFuture.get()).thenThrow(new ExecutionException(new RuntimeException()));
        Mockito.when(async.executeAsynchronously(Mockito.any(Callable.class))).thenReturn(mockFuture);
        
        // Actual test
        MocaJobFactory jobFactory = new MocaJobFactory(factory, jobExecDAO, async, mFact);
        Job createdJob = jobFactory.newJob(fired, scheduler);
        createdJob.execute(mockCtx);
        
        // Last status probe should be set to 502
        Mockito.verify(mFact).newGauge(Mockito.eq(expectedMadName), Mockito.any(MadGenericGauge.class));
        Mockito.verify(lastStatusGauge).setValue(UnexpectedException.CODE);
    }

}
