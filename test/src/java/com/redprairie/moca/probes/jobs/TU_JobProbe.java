/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.probes.jobs;

import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.quartz.SchedulerException;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.mad.client.MadGauge;
import com.redprairie.mad.client.MadMetrics;
import com.redprairie.mad.client.MadName;
import com.redprairie.mad.client.MadNameImpl;
import com.redprairie.moca.job.JobDefinition;
import com.redprairie.moca.job.JobException;
import com.redprairie.moca.job.JobManager;
import com.redprairie.moca.mad.MonitoringUtils;

import static org.junit.Assert.*;

/**
 * Tests for JobProbe
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TU_JobProbe {
    
    @BeforeClass
    public static void setupClass() {
        _factToRestore = MadMetrics.getFactory();
         MadMetrics.setFactory(_mockFactory);
         mockNameGeneration();
    }
    
    @AfterClass
    public static void afterClass() {
        MadMetrics.setFactory(_factToRestore);
    }
    

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testProbeRegisterAndUnregister() throws MalformedObjectNameException, NullPointerException, JobException, SchedulerException {
        JobManager mockManager = Mockito.mock(JobManager.class);
        
        JobDefinition jobA = Mockito.mock(JobDefinition.class);
        Mockito.when(jobA.getJobId()).thenReturn(TEST_JOB);
        Mockito.when(jobA.isEnabled()).thenReturn(true);
        
        Mockito.when(mockManager.isScheduled(TEST_JOB)).thenReturn(true);
        Date dt = new Date();
        Mockito.when(mockManager.getLastExecutionDate(TEST_JOB)).thenReturn(dt);
        Mockito.when(mockManager.getNextExecutionDate(TEST_JOB)).thenReturn(dt);
        
        // Verify job configuration and scheduled gauge registration works
        JobProbe probe = new JobProbe(mockManager, jobA);
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        assertEquals(true,
            server.isRegistered(new ObjectName("com.redprairie.moca:type=Jobs,scope=JOB_A,name=job-configuration")));
        ArgumentCaptor<MadGauge> scheduledCaptor = ArgumentCaptor.forClass(MadGauge.class);
        Mockito.verify(_mockFactory).newGauge(Mockito.eq(SCHEDULED), scheduledCaptor.capture());
        assertEquals(true, (Boolean) scheduledCaptor.getValue().getValue());
        
        // Verify scheduled probes work
        probe.notifyJobScheduled(mockManager);
        
        // Verify next/last date gauges registered
        ArgumentCaptor<MadGauge> nextDate = ArgumentCaptor.forClass(MadGauge.class);
        Mockito.verify(_mockFactory).newGauge(Mockito.eq(NEXT_DATE), nextDate.capture());
        assertEquals(dt, nextDate.getValue().getValue());
        
        ArgumentCaptor<MadGauge> lastDate = ArgumentCaptor.forClass(MadGauge.class);
        Mockito.verify(_mockFactory).newGauge(Mockito.eq(LAST_DATE), lastDate.capture());
        assertEquals(dt, lastDate.getValue().getValue());
        
        // Verify timers registered
        Mockito.verify(_mockFactory).newTimer(
            Mockito.eq(EXECUTIONS_SUCCESS),
                Mockito.any(TimeUnit.class), Mockito.any(TimeUnit.class));
        
        Mockito.verify(_mockFactory).newTimer(
            Mockito.eq(EXECUTIONS_ERRORED),
                Mockito.any(TimeUnit.class), Mockito.any(TimeUnit.class));
        
        // Verify unregister
        probe.unregister();
        
        assertEquals(false,
            server.isRegistered(new ObjectName("com.redprairie.moca:type=Jobs,scope=JOB_A,name=job-configuration")));
        
        Mockito.verify(_mockFactory).removeMetrics(MonitoringUtils.MOCA_GROUP_NAME, JobProbe.TYPE_JOBS, TEST_JOB);
        
    }
    
    private static void mockNameGeneration() {
        Mockito.when(_mockFactory.newMadName(Mockito.anyString(), Mockito.anyString(), Mockito.eq(JobProbe.JOB_CONFIGURATION), Mockito.eq(TEST_JOB)))
            .thenReturn(new MadNameImpl(MonitoringUtils.MOCA_GROUP_NAME, JobProbe.TYPE_JOBS, JobProbe.JOB_CONFIGURATION, TEST_JOB));
        
        Mockito.when(_mockFactory.newMadName(Mockito.anyString(), Mockito.anyString(),
            Mockito.eq(JobProbe.IS_SCHEDULED), Mockito.eq(TEST_JOB)))
                 .thenReturn(SCHEDULED);
        
        Mockito.when(_mockFactory.newMadName(Mockito.anyString(), Mockito.anyString(),
            Mockito.eq(JobProbe.NEXT_EXECUTION_DATE), Mockito.eq(TEST_JOB)))
                 .thenReturn(NEXT_DATE);
        
        Mockito.when(_mockFactory.newMadName(Mockito.anyString(), Mockito.anyString(),
            Mockito.eq(JobProbe.LAST_EXECUTION_DATE), Mockito.eq(TEST_JOB)))
                 .thenReturn(LAST_DATE);
        
        Mockito.when(_mockFactory.newMadName(Mockito.anyString(), Mockito.anyString(),
            Mockito.eq(JobProbe.EXECUTIONS_SUCCESSFUL), Mockito.eq(TEST_JOB)))
                 .thenReturn(EXECUTIONS_SUCCESS);
        
        Mockito.when(_mockFactory.newMadName(Mockito.anyString(), Mockito.anyString(),
            Mockito.eq(JobProbe.EXECUTIONS_ERRORED), Mockito.eq(TEST_JOB)))
                 .thenReturn(EXECUTIONS_ERRORED);
    }
    
    private static final String TEST_JOB = "JOB_A";
    
    private static final MadName SCHEDULED = new MadNameImpl(MonitoringUtils.MOCA_GROUP_NAME,
        JobProbe.TYPE_JOBS, JobProbe.IS_SCHEDULED, TEST_JOB);
    private static final MadName NEXT_DATE = new MadNameImpl(MonitoringUtils.MOCA_GROUP_NAME,
        JobProbe.TYPE_JOBS, JobProbe.NEXT_EXECUTION_DATE, TEST_JOB);
    private static final MadName LAST_DATE = new MadNameImpl(MonitoringUtils.MOCA_GROUP_NAME,
        JobProbe.TYPE_JOBS, JobProbe.LAST_EXECUTION_DATE, TEST_JOB);
    private static final MadName EXECUTIONS_SUCCESS = new MadNameImpl(MonitoringUtils.MOCA_GROUP_NAME,
        JobProbe.TYPE_JOBS, JobProbe.EXECUTIONS_SUCCESSFUL, TEST_JOB);
    private static final MadName EXECUTIONS_ERRORED = new MadNameImpl(MonitoringUtils.MOCA_GROUP_NAME,
        JobProbe.TYPE_JOBS, JobProbe.EXECUTIONS_ERRORED, TEST_JOB);
    
    
    
    private static MadFactory _mockFactory = Mockito.mock(MadFactory.class);
    private static MadFactory _factToRestore;
}
