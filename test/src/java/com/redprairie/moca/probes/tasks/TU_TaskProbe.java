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

package com.redprairie.moca.probes.tasks;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.mad.client.MadGauge;
import com.redprairie.mad.client.MadMetrics;
import com.redprairie.mad.client.MadName;
import com.redprairie.mad.client.MadNameImpl;
import com.redprairie.moca.mad.MonitoringUtils;
import com.redprairie.moca.task.TaskDefinition;
import com.redprairie.moca.task.TaskManager;

import static org.junit.Assert.*;

/**
 * Task Probe tests
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TU_TaskProbe {
    @BeforeClass
    public static void setupClass() {
        _factToRestore = MadMetrics.getFactory();
         MadMetrics.setFactory(_mockFactory);
    }
    
    @AfterClass
    public static void afterClass() {
        MadMetrics.setFactory(_factToRestore);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testRegisterAndUnregister() throws MalformedObjectNameException, NullPointerException {
        Mockito.when(_mockFactory.newMadName(MonitoringUtils.MOCA_GROUP_NAME,
            TaskProbe.TYPE_TASKS, "task-configuration", "Task1"))
            .thenReturn(new MadNameImpl(MonitoringUtils.MOCA_GROUP_NAME, TaskProbe.TYPE_TASKS, "task-configuration", "Task1"));
        TaskManager manager = Mockito.mock(TaskManager.class);
        TaskDefinition task = new TaskDefinition();
        task.setTaskId("Task1");
        
        // Mock that the task is running
        Mockito.when(manager.isRunning("Task1")).thenReturn(true);
        
        TaskProbe probe = new TaskProbe(manager, task);
        
        // Verify task configuration data was exported
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        assertTrue(server.isRegistered(new ObjectName("com.redprairie.moca:type=Tasks,scope=Task1,name=task-configuration")));
        
        
        ArgumentCaptor<MadGauge> runningCaptor = ArgumentCaptor.forClass(MadGauge.class);
        // Verify running gauge registered and shows
        // the task running = true
        Mockito.verify(_mockFactory).newGauge(Mockito.any(MadName.class), runningCaptor.capture());
        MadGauge runningGauge = runningCaptor.getValue();
        assertTrue((Boolean)runningGauge.getValue());
        
        // Verify restart counter registered
        Mockito.verify(_mockFactory).newCounter(Mockito.any(MadName.class));
        
        // Unregister
        probe.unregister();
        
        // Validate mbean unregistered and metrics gone
        assertFalse(server.isRegistered(
            new ObjectName("com.redprairie.moca:type=Tasks,scope=Task1,name=task-configuration")));
        Mockito.verify(_mockFactory).removeMetrics(
            MonitoringUtils.MOCA_GROUP_NAME, TaskProbe.TYPE_TASKS, "Task1");
    }
    
    private static MadFactory _mockFactory = Mockito.mock(MadFactory.class);
    private static MadFactory _factToRestore;
}
