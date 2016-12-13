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
import java.util.ArrayList;
import java.util.Arrays;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;

import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.mad.probes.AbstractMadFactoryTest;
import com.redprairie.moca.task.TaskDefinition;
import com.redprairie.moca.task.TaskManager;

import static org.junit.Assert.*;

/**
 * Tests for TaskSummaryProbe
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TU_TaskSummaryProbe extends AbstractMadFactoryTest {
    
    public static final String EXPECTED_OBJECT_NAME = 
        "com.redprairie.moca:type=Tasks-Summary,name=summary";
    
    @SuppressWarnings("unchecked")
    @Test
    public void testRegisterAndUnregister() throws AttributeNotFoundException,
           InstanceNotFoundException, MalformedObjectNameException, MBeanException,
           ReflectionException, NullPointerException {
        TaskManager manager = Mockito.mock(TaskManager.class);
        ObjectName objName = new ObjectName(EXPECTED_OBJECT_NAME);
        
        TaskDefinition task1 = new TaskDefinition();
        task1.setName("My Task 1");
        task1.setTaskId("Task1");
        
        TaskDefinition task2 = new TaskDefinition();
        task2.setName("My Task 2");
        task2.setTaskId("Task2");

        Mockito.when(manager.isRunning("Task1")).thenReturn(true);
        Mockito.when(manager.isRunning("Task2")).thenReturn(false);
        // Mock that two tasks are current (one running, one stopped) on the first check
        // Second check will have no tasks (i.e. they've been removed)
        Mockito.when(manager.getCurrentTasks()).thenReturn(Arrays.asList(task1, task2), new ArrayList<TaskDefinition>());
        
        TaskSummaryProbe.registerTaskSummary(manager);
        
        // Get the object from the mbean server
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        CompositeData summary = (CompositeData) server.getAttribute(objName, "Summary");
        
        assertEquals(1, summary.get("runningCount"));
        assertEquals(1, summary.get("stoppedCount"));
        assertEquals(2, summary.get("totalCount"));
        assertEquals("My Task 1 (Task1)", ((String[])summary.get("runningTasks"))[0]);
        assertEquals("My Task 2 (Task2)", ((String[])summary.get("stoppedTasks"))[0]);
        
        // Tasks should all be removed at this point
        summary = (CompositeData) server.getAttribute(objName, "Summary");
        assertEquals(0, summary.get("runningCount"));
        assertEquals(0, summary.get("stoppedCount"));
        assertEquals(0, summary.get("totalCount"));
        assertEquals(0, ((String[])summary.get("runningTasks")).length);
        assertEquals(0, ((String[])summary.get("stoppedTasks")).length);
        
        // Now unregister
        TaskSummaryProbe.unregisterTaskSummary();
        
        // MBean should no longer be present
        assertFalse(server.isRegistered(objName));
    }

}
