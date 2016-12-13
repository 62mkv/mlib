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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;

import com.redprairie.moca.server.InstanceUrl;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.task.TaskDefinition;
import com.redprairie.moca.task.TaskManager;
import com.redprairie.moca.task.TU_TaskManager.EmptyRunnable;
import com.redprairie.moca.task.dao.TaskDefinitionDAO;
import com.redprairie.moca.task.dao.TaskExecutionDAO;

/**
 * Task Manager Probes Listener tests
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TU_TaskManagerProbesListener {
    
    @BeforeClass
    public static void setupClass() throws SystemConfigurationException {
        ServerUtils.setupDaemonContext(TU_TaskManagerProbesListener.class.getName(), false);
    }
    
    @Before
    public void beforeEachTest() {
        // We reset the exchanger in case if a test failed, so it doesn't
        // make others fail because the thread already exchanged
        resetExchanger();
        _realUrl = ServerUtils.globalAttribute(InstanceUrl.class);
        _mockUrl = Mockito.mock(InstanceUrl.class);
        ServerUtils.globalContext().putAttribute(InstanceUrl.class.getName(), 
            _mockUrl);
    }
    
    @After
    public void afterEachTest() {
        ServerUtils.globalContext().putAttribute(InstanceUrl.class.getName(), _realUrl);
    }
    
    private static void resetExchanger() {
        _exchanger = new Exchanger<Object>();
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testListenerRegisterUnregister() throws InterruptedException {
        // Create some mock task data
        TaskDefinition taskA1 = new TaskDefinition();
        taskA1.setTaskId("Task1");
        taskA1.setName("Task1");
        taskA1.setStartDelay(null);
        taskA1.setAutoStart(true);
        taskA1.setCmdLine(EmptyRunnable.class.getName());
        taskA1.setType(TaskDefinition.THREAD_TASK);
        taskA1.setEnvironment(Collections.EMPTY_MAP);
        
        TaskDefinition taskA2 = new TaskDefinition();
        taskA2.setTaskId("Task2");
        taskA2.setName("Task2");
        taskA2.setStartDelay(null);
        taskA2.setAutoStart(true);
        taskA2.setCmdLine(EmptyRunnable.class.getName());
        taskA2.setType(TaskDefinition.THREAD_TASK);
        taskA2.setEnvironment(Collections.EMPTY_MAP);
        
        TaskDefinition taskA3Original = new TaskDefinition();
        taskA3Original.setTaskId("Task3");
        taskA3Original.setName("Task3");
        taskA3Original.setStartDelay(null);
        taskA3Original.setAutoStart(false);
        taskA3Original.setCmdLine(EmptyRunnable.class.getName());
        taskA3Original.setType(TaskDefinition.THREAD_TASK);
        taskA3Original.setEnvironment(Collections.EMPTY_MAP);
        
        TaskDefinition taskA3Changed = new TaskDefinition();
        taskA3Changed.setTaskId("Task3");
        taskA3Changed.setName("Task3 Set to Autostart");
        taskA3Changed.setStartDelay(null);
        taskA3Changed.setAutoStart(true);
        taskA3Changed.setCmdLine(EmptyRunnable.class.getName());
        taskA3Changed.setType(TaskDefinition.THREAD_TASK);
        taskA3Changed.setEnvironment(Collections.EMPTY_MAP);
        
        TaskDefinition taskA4Unchanged = new TaskDefinition();
        taskA4Unchanged.setTaskId("Task4");
        taskA4Unchanged.setName("Task4");
        taskA4Unchanged.setStartDelay(null);
        taskA4Unchanged.setAutoStart(true);
        taskA4Unchanged.setCmdLine(EmptyRunnable.class.getName());
        taskA4Unchanged.setType(TaskDefinition.THREAD_TASK);
        taskA4Unchanged.setEnvironment(Collections.EMPTY_MAP);
        
        // Mock up a DAO that will return the task on the first call and an
        // the new one on the second and subsequent calls.  Those second 
        // and subsequence calls will be invoked from TaskManager.sync().
        TaskDefinitionDAO mockDAO = Mockito.mock(TaskDefinitionDAO.class);
        Mockito.when(mockDAO.readAllTasksForAllAndRoles()).thenReturn(
            Arrays.asList(taskA1, taskA3Original, taskA4Unchanged),
            Arrays.asList(taskA2, taskA3Changed, taskA4Unchanged));

        // Mock up a ServerContextFactory that can be passed by TaskManager to ThreadTask.
        ServerContextFactory mockFactory = Mockito.mock(
            ServerContextFactory.class, Mockito.RETURNS_MOCKS);

        // Mock up a Future.
        Future mockFuture = Mockito.mock(Future.class);   

        // Mock up an ExecutorService to capture task starts and stops.
        ExecutorService mockService = Mockito.mock(ExecutorService.class);
        Mockito.when(mockService.submit(Mockito.any(Runnable.class))).thenReturn(mockFuture);
        
        TaskExecutionDAO taskExecDao = Mockito.mock(TaskExecutionDAO.class);
        
        // Get a TaskManager to test with a sync interval of 1 second.
        TaskManager testTaskManager = new TaskManager(mockService, mockDAO, 
                taskExecDao, mockFactory, 1, Collections.EMPTY_MAP, true); 
        
        // Our actual test object, add it as a listener
        TaskManagerProbeListener listener = new TaskManagerProbeListener();
        testTaskManager.addListener(listener);
        
        try {
            
            testTaskManager.start(false);
            
            // Validate listener fired off correctly and 3 task probes added
            Map<String, TaskProbe> taskProbes = listener.getTaskProbes();
            assertTrue(taskProbes.size() == 3);
            assertEquals(taskA1, taskProbes.get(taskA1.getTaskId()).getTaskDefinition());
            assertEquals(taskA3Original, taskProbes.get(taskA3Original.getTaskId()).getTaskDefinition());
            assertEquals(taskA4Unchanged, taskProbes.get(taskA4Unchanged.getTaskId()).getTaskDefinition());
            
            // Sleep so that we execute a TaskManager sync once.
            Thread.sleep(1500);
            
            // Task 1 metrics should be removed
            // Task 2 should be added
            // Task 3 should have a changed task definition
            // Task 4 should be unchanged
            taskProbes = listener.getTaskProbes();
            assertEquals(3, taskProbes.size());
            assertFalse(taskProbes.containsKey(taskA1.getTaskId()));
            assertEquals(taskA2, taskProbes.get(taskA2.getTaskId()).getTaskDefinition());
            assertEquals(taskA3Changed, taskProbes.get(taskA3Changed.getTaskId()).getTaskDefinition());
            assertEquals(taskA4Unchanged, taskProbes.get(taskA4Unchanged.getTaskId()).getTaskDefinition());
        }
        finally {
            testTaskManager.stop();
            // Verify all task probes are removed from the listener
            assertEquals(0, listener.getTaskProbes().size());
        }
    }

    static Exchanger<Object> _exchanger;
    private InstanceUrl _realUrl;
    private InstanceUrl _mockUrl;
}
