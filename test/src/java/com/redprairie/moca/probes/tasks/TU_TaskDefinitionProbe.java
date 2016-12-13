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

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ReflectionException;

import org.junit.Test;

import com.redprairie.mad.client.MadName;
import com.redprairie.mad.client.MadNameImpl;
import com.redprairie.mad.client.MadUtil;
import com.redprairie.mad.probes.AbstractMadFactoryTest;
import com.redprairie.moca.task.TaskDefinition;

import static org.junit.Assert.*;

/**
 * Task Definition Probe Tests
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TU_TaskDefinitionProbe extends AbstractMadFactoryTest {
    
    @Test
    public void testTypeTranslation() {
        // Simple test to make sure we translate
        // the type to the correct word.
        TaskDefinition task = new TaskDefinition();
        task.setName("Task1");
        task.setTaskId("TASK_1");
        task.setType(TaskDefinition.DAEMON_TASK);
        
        TaskDefinitionProbe probe = new TaskDefinitionProbe(task);
        assertEquals("Daemon", probe.getType());
        
        task.setType(TaskDefinition.PROCESS_TASK);
        assertEquals("Process", probe.getType());
        
        task.setType(TaskDefinition.THREAD_TASK);
        assertEquals("Thread", probe.getType());
    }
    
    @Test
    public void testRegisterReplace() throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException {
        TaskDefinition task = new TaskDefinition();
        task.setName("Task1");
        task.setTaskId("TASK_1");
        task.setType(TaskDefinition.DAEMON_TASK);
        
        MadName mName = new MadNameImpl("test","test","test","TASK_1");
        TaskDefinitionProbe.registerTask(mName, task);
        
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        assertEquals("Daemon", server.getAttribute(MadUtil.convertToObjectName(mName), "Type").toString());
        
        // Now use a different Task Definition object and re-export it,
        // the existing Task Definition object should be replaced.
        TaskDefinition newTask = new TaskDefinition();
        newTask.setName("Task1");
        newTask.setTaskId("TASK_1");
        newTask.setType(TaskDefinition.THREAD_TASK);
        TaskDefinitionProbe.registerTask(mName, newTask);
        
        assertEquals("Thread", server.getAttribute(MadUtil.convertToObjectName(mName), "Type").toString());
    }

}
