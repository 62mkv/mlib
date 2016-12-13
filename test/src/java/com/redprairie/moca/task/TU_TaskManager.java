/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.redprairie.moca.task;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UID;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.server.InstanceUrl;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.exec.RequestContext;
import com.redprairie.moca.server.exec.SessionContext;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.task.dao.TaskDefinitionDAO;
import com.redprairie.moca.task.dao.TaskExecutionDAO;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.moca.util.TestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This class is to test the various functions of the TaskManager class
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_TaskManager {
    
    private static final int EXCHANGER_TIMEOUT = TestUtils.getTestTimeout(TU_TaskManager.class, "EXCHANGER", 300);
    private final static String BINDING_NAME = TU_TaskManager.class.getName() + new UID();
    
    private static Logger _logger = LogManager.getLogger();
    private File _logFile;
    
    @BeforeClass
    public static void setupClass() throws SystemConfigurationException {
        ServerUtils.setupDaemonContext(TU_TaskManager.class.getName(), false);
        // Determine timeouts
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
        _logFile = null;
    }
    
    @After
    public void afterEachTest() throws InterruptedException {
        ServerUtils.globalContext().putAttribute(InstanceUrl.class.getName(), _realUrl);
        // End the executor service is used during the test
        if (_service != null) {
            _service.shutdownNow();
            assertTrue(_service.awaitTermination(5, TimeUnit.SECONDS));
            _service = null;
        }
        
        if(_logFile != null && _logFile.isFile()){
            if(!_logFile.delete()){
                _logger.error("ERROR deleting log file: " + _logFile.getAbsolutePath());
                _logFile.deleteOnExit();
            }
        }
    }
    
    private static void resetExchanger() {
        _exchanger = new Exchanger<Object>();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStartedThreadHasEnvironment() {
        String userId = "SpecialUserX12B";
        Map<String, String> environment = new HashMap<String, String>();
        environment.put("usr_id", userId);
        
        TaskDefinition task = new TaskDefinition();
        task.setTaskId("a");
        task.setName("Task A");
        task.setStartDelay(null);
        task.setAutoStart(true);
        task.setCmdLine(EmptyRunnable.class.getName());
        task.setType(TaskDefinition.THREAD_TASK);
        task.setEnvironment(environment);
        
        SystemContext mockSys = Mockito.mock(SystemContext.class);
        Mockito.when(mockSys.getConfigurationElement(
            MocaRegistry.REGKEY_SERVER_INHIBIT_TASKS, "false")).thenReturn("false");
        
        TaskDefinitionDAO mockDAO = Mockito.mock(TaskDefinitionDAO.class);
        Mockito.when(mockDAO.readAllTasksForAllAndRoles()).thenReturn(
                Arrays.asList(task));

        // We only care about a method that is called in verify, so we just
        // return all mocks.
        ServerContextFactory mockFactory = Mockito.mock(
                ServerContextFactory.class, Mockito.RETURNS_MOCKS);
        
        TaskExecutionDAO taskExecDao = Mockito.mock(TaskExecutionDAO.class);

        TaskManager testTaskManager = new TaskManager(mockDAO, taskExecDao, 
            mockFactory, Integer.MAX_VALUE, Collections.EMPTY_MAP, true); 
        
        try {
            testTaskManager.start(false);
            
            // We want to capture the SessionContext since it contains our 
            // environment variable that should have been set while verifying
            // it was called.
            ArgumentCaptor<SessionContext> argument = 
                ArgumentCaptor.forClass(SessionContext.class);
            
            // We wait up to a 100 milliseconds for it to be called.  We have 
            // to do this since the session is generated in the task thread
            Mockito.verify(mockFactory, Mockito.timeout(100)).newContext(
                    Mockito.isA(RequestContext.class), 
                    argument.capture());

            // Here is where we verify if the environment variable was passed
            // or not correctly.
            SessionContext passedContext = argument.getValue();
            assertNotNull("The passed context shouldn't be null", passedContext);
            assertEquals(userId, passedContext.getVariable("usr_id"));
        }
        finally {
            testTaskManager.stop();
        }
    }
    
    /**
     * This test just makes sure that we correctly call the thread run method.
     * TODO This test should probably be broken up into individual tests in the 
     * classes effected.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testStartedThreadCalledCorrectly() throws MocaException {
        TaskDefinition task = new TaskDefinition();
        task.setTaskId("a");
        task.setName("Task A");
        task.setStartDelay(null);
        task.setAutoStart(true);
        task.setCmdLine(TestPublishUserRunnable.class.getName());
        task.setType(TaskDefinition.THREAD_TASK);
        Map<String, String> environment = Collections.emptyMap(); 
        task.setEnvironment(environment);
        
        TaskDefinitionDAO mockDAO = Mockito.mock(TaskDefinitionDAO.class);
        Mockito.when(mockDAO.readAllTasksForAllAndRoles()).thenReturn(
                Arrays.asList(task));
        
        MocaContext mockMoca = Mockito.mock(MocaContext.class);

        // The mock factory will return a mock moca context all the way down
        ServerContextFactory mockFactory = Mockito.mock(
                ServerContextFactory.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(mockFactory.newContext(Mockito.isA(RequestContext.class), 
                Mockito.isA(SessionContext.class)).getComponentContext()).
                thenReturn(mockMoca);
        
        TaskExecutionDAO taskExecDao = Mockito.mock(TaskExecutionDAO.class);
        
        TaskManager testTaskManager = new TaskManager(mockDAO, taskExecDao, 
            mockFactory, Integer.MAX_VALUE, Collections.EMPTY_MAP, true); 
        
        try {
            testTaskManager.start(false);
            
            // Now we wait up to a max of 5 seconds for it to call our command
            Mockito.verify(mockMoca, Mockito.timeout(5000)).executeCommand(
                    Mockito.eq(COMMAND));
        }
        finally {
            testTaskManager.stop();
        }
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testRemoveTaskAtRuntime() {
        // Create a task definition.
        TaskDefinition task = new TaskDefinition();
        task.setTaskId("A");
        task.setName("Task A");
        task.setStartDelay(null);
        task.setAutoStart(true);
        task.setCmdLine(EmptyRunnable.class.getName());
        task.setType(TaskDefinition.THREAD_TASK);
        task.setEnvironment(Collections.EMPTY_MAP);
        
        // Mock up a DAO that will return the task on the first call and an
        // empty collection on the second and subsequent calls.  Those second 
        // and subsequence calls will be invoked from TaskManager.sync().
        TaskDefinitionDAO mockDAO = Mockito.mock(TaskDefinitionDAO.class);
        Mockito.when(mockDAO.readAllTasksForAllAndRoles()).thenReturn(
            Arrays.asList(task), Collections.EMPTY_LIST);

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
        
        try {
            testTaskManager.start(false);
            
            // The task should be submitted right away on start up.
            Mockito.verify(mockService, Mockito.timeout(10)).submit(Mockito.any(Runnable.class));
            
            // After the first sync runs the task should be cancelled.
            Mockito.verify(mockFuture, Mockito.timeout(1200)).cancel(true);
        }
        finally {
            testTaskManager.stop();
        }
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testAddTaskAtRuntime() {
        // Create a task definition.
        TaskDefinition task = new TaskDefinition();
        task.setTaskId("A");
        task.setName("Task A");
        task.setStartDelay(null);
        task.setAutoStart(true);
        task.setCmdLine(EmptyRunnable.class.getName());
        task.setType(TaskDefinition.THREAD_TASK);
        task.setEnvironment(Collections.EMPTY_MAP);
        
        // Mock up a DAO that will return the task on the first call and an
        // empty collection on the second and subsequent calls.  Those second 
        // and subsequence calls will be invoked from TaskManager.sync().
        TaskDefinitionDAO mockDAO = Mockito.mock(TaskDefinitionDAO.class);
        Mockito.when(mockDAO.readAllTasksForAllAndRoles()).thenReturn(
            Collections.EMPTY_LIST, Arrays.asList(task));

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
        
        try {
            testTaskManager.start(false);
            
            // After the first sync runs the task should be submitted.
            Mockito.verify(mockService, Mockito.timeout(1200)).submit(Mockito.any(Runnable.class));
            // The task shouldn't have been cancelled yet.
            Mockito.verify(mockFuture, Mockito.never()).cancel(true);
        }
        finally {
            testTaskManager.stop();
        }
        
        // After the stop the task should have been stopped.
        Mockito.verify(mockFuture).cancel(true);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testTaskManagerStartAndStopWithCache() {
        // Create a test task definition.
        TaskDefinition task = new TaskDefinition();
        task.setTaskId("A");
        task.setName("Task A");
        task.setStartDelay(null);
        task.setAutoStart(true);
        task.setCmdLine(EmptyRunnable.class.getName());
        task.setType(TaskDefinition.THREAD_TASK);
        task.setEnvironment(Collections.EMPTY_MAP);

        // Mock up a DAO that will return the task.
        TaskDefinitionDAO mockDAO = Mockito.mock(TaskDefinitionDAO.class);
        Mockito.when(mockDAO.readAllTasksForAllAndRoles())
            .thenReturn(Arrays.asList(task));

        // Mock up a ServerContextFactory that can be passed by TaskManager to
        // ThreadTask.
        ServerContextFactory mockFactory = Mockito.mock(ServerContextFactory.class,
            Mockito.RETURNS_MOCKS);

        // Mock up a Future.
        Future mockFuture = Mockito.mock(Future.class);

        // Mock up an ExecutorService to capture task starts and stops.
        ExecutorService mockService = Mockito.mock(ExecutorService.class);
        Mockito.when(mockService.submit(Mockito.any(Runnable.class))).thenReturn(
            mockFuture);

        TaskExecutionDAO taskExecDao = Mockito.mock(TaskExecutionDAO.class);

        // Create a test Task Manager
        TaskManager manager = new TaskManager(mockService, mockDAO, taskExecDao,
            mockFactory, 0, Collections.EMPTY_MAP, true);

        // Verify that the Cache is initially empty.
        ConcurrentMap<Node, Set<TaskDefinition>> taskMap = manager.getClusteredTasks();
        assertEquals(0, taskMap.entrySet().size());

        try {
            // Start up the test Task Manager.
            manager.start();

            // Check that the task is now in the Cache.
            Set<Entry<Node, Set<TaskDefinition>>> entries = taskMap.entrySet();
            assertEquals(1, entries.size());
            Set<TaskDefinition> tasks = entries.iterator().next().getValue();
            assertEquals(1, tasks.size());
            assertEquals(task, tasks.iterator().next());
        }
        finally {
            // Stop the Task Manager.
            manager.stop();
        }

        // Now verify that the Cache has one entry with an empty value set.
        Set<Entry<Node, Set<TaskDefinition>>> entries = taskMap.entrySet();
        assertEquals(1, entries.size());
        Set<TaskDefinition> tasks = entries.iterator().next().getValue();
        assertEquals(0, tasks.size());
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testTaskManagerStartTaskAndStopTaskWithCache() {
        // Create a test task definition.
        TaskDefinition task = new TaskDefinition();
        task.setTaskId("A");
        task.setName("Task A");
        task.setStartDelay(null);
        task.setAutoStart(false);
        task.setCmdLine(EmptyRunnable.class.getName());
        task.setType(TaskDefinition.THREAD_TASK);
        task.setEnvironment(Collections.EMPTY_MAP);

        // Mock up a DAO that will return the task.
        TaskDefinitionDAO mockDAO = Mockito.mock(TaskDefinitionDAO.class);
        Mockito.when(mockDAO.readAllTasksForAllAndRoles())
            .thenReturn(Arrays.asList(task));

        // Mock up a ServerContextFactory that can be passed by TaskManager to
        // ThreadTask.
        ServerContextFactory mockFactory = Mockito.mock(ServerContextFactory.class,
            Mockito.RETURNS_MOCKS);

        // Mock up a Future.
        Future mockFuture = Mockito.mock(Future.class);

        // Mock up an ExecutorService to capture task starts and stops.
        ExecutorService mockService = Mockito.mock(ExecutorService.class);
        Mockito.when(mockService.submit(Mockito.any(Runnable.class))).thenReturn(
            mockFuture);

        TaskExecutionDAO taskExecDao = Mockito.mock(TaskExecutionDAO.class);

        // Create a test Task Manager
        TaskManager manager = new TaskManager(mockService, mockDAO, taskExecDao,
            mockFactory, 0, Collections.EMPTY_MAP, true);

        // Verify that the Cache is initially empty.
        ConcurrentMap<Node, Set<TaskDefinition>> taskMap = manager.getClusteredTasks();
        assertEquals(0, taskMap.entrySet().size());

        try {
            // Start up the test Task Manager and start the test task.
            manager.start();
            assertEquals(0, taskMap.entrySet().size());
            
            manager.startTask(task.getTaskId());

            // Check that the task is now in the Cache.
            Set<Entry<Node, Set<TaskDefinition>>> entries = taskMap.entrySet();
            assertEquals(1, entries.size());
            Set<TaskDefinition> tasks = entries.iterator().next().getValue();
            assertEquals(1, tasks.size());
            assertEquals(task, tasks.iterator().next());
            
            // Stop the task.
            manager.stopTask(task.getTaskId());
            
            // Now verify that the Cache has one entry with an empty value set.
            entries = taskMap.entrySet();
            assertEquals(1, entries.size());
            tasks = entries.iterator().next().getValue();
            assertEquals(0, tasks.size());
        }
        finally {
            // Finally stop Task Manager.
            manager.stop();
        }

    }
    
    /**
     * This test makes sure that if a task ends in an exception it is
     * then marked as stopped (defect that was originally found)
     * @throws InterruptedException
     */
    @Test
    public void testTaskEndsInException() throws InterruptedException {
    	testTaskThatEnds(ExceptionRunnable.class);
    }
    
    /**
     * This test makes sure that if a task ends in an exception it is
     * then marked as stopped (defect that was originally found)
     * @throws InterruptedException
     */
    @Test
    public void testTaskEndsOnItsOwn() throws InterruptedException {
        testTaskThatEnds(EmptyRunnable.class);
    }
    
    private void testTaskThatEnds(Class<? extends Runnable> taskClass) throws InterruptedException {
    	    SingleTaskTestData testData = setupAndStartSingleTask(
	                taskClass, true, null);
            TaskManager manager = testData._manager;

            try {
                ConcurrentMap<Node, Set<TaskDefinition>> taskMap = manager.getClusteredTasks();
                
                // Wait for the task to complete
                testData.waitForTaskToStop(10, TimeUnit.SECONDS);
                
                // Now verify that the Cache has one entry with an empty value set.
                Set<Entry<Node, Set<TaskDefinition>>> entries = taskMap.entrySet();
                assertEquals(1, entries.size());
                Set<TaskDefinition> tasks = entries.iterator().next().getValue();
                assertEquals(0, tasks.size());
            }
            finally {
                // Finally stop Task Manager.
                manager.stop();
            }
    }
    
    /**
     * Tests that the cache that keeps track of what tasks are running on this
     * node is kept up to date correctly even if a task takes a little bit to actually
     * stop (regression test for MOCA-6582). Uses a bunch of task restarts (stop/start).
     */
    @Test
    public void testTaskRestartValidateCacheState() {
    	TaskManagerListener mockListener = Mockito.mock(TaskManagerListener.class);
    	SingleTaskTestData testData = setupAndStartSingleTask(
                SlowToStopRunnable.class, true, null, Collections.singletonList(mockListener));
        TaskManager manager = testData._manager;

        try {
        	// Validate the task has started
        	Mockito.verify(mockListener).onTaskStarted(Mockito.eq(manager),
            		Mockito.eq(testData._task));
            validateTasksInCache(manager, testData._task);
                 
            // Mimic a bunch of task restarts validating state
            for (int i = 1; i <= 10; i++) {
	            manager.stopTask(testData._task.getTaskId());
	            manager.startTask(testData._task.getTaskId());
	            
	            // Validate task listener was fired off indicating the task
	            // was stopped and then started (started times = iteration + 1 at this point
	            // because the task was originally started).
	            Mockito.verify(mockListener, Mockito.times(i)).onTaskStopped(Mockito.eq(manager),
	            		Mockito.eq(testData._task));
	            
	            Mockito.verify(mockListener, Mockito.times(i + 1)).onTaskStarted(Mockito.eq(manager),
	            		Mockito.eq(testData._task));
	            
	            // Verify the task is still reported as running via the cache
	            validateTasksInCache(manager, testData._task);
            }
        }
        finally {
            // Finally stop Task Manager.
            manager.stop();
        }
    }
    
    private void validateTasksInCache(TaskManager manager, TaskDefinition... expectedTasks) {
    	ConcurrentMap<Node, Set<TaskDefinition>> taskMap = manager.getClusteredTasks();
    	Set<Entry<Node, Set<TaskDefinition>>> entries = taskMap.entrySet();
        assertEquals(1, entries.size());
        Set<TaskDefinition> tasks = entries.iterator().next().getValue();
        assertEquals(expectedTasks.length, tasks.size());
    }
    
    // Create and start a task manager with the given runnable as the only task
    private <T extends Runnable> SingleTaskTestData setupAndStartSingleTask(
                                                       Class<T> runnableClazz,
                                                       boolean autoStart,
                                                       String additionalArgs) {
    	return setupAndStartSingleTask(runnableClazz,
    			autoStart, additionalArgs,
    			Collections.<TaskManagerListener>emptyList());
    }
    
    // Create and start a task manager with the given runnable as the only task
    private <T extends Runnable> SingleTaskTestData setupAndStartSingleTask(
                                                       Class<T> runnableClazz,
                                                       boolean autoStart,
                                                       String additionalArgs,
                                                       List<TaskManagerListener> listeners) {
        
        List<TaskManagerListener> listenersToAdd = new ArrayList<TaskManagerListener>(listeners);
    	final String cmdLine = runnableClazz.getName()  +
                " " + (additionalArgs != null ? additionalArgs : "");
        
        // Create a test task definition.
        TaskDefinition task = new TaskDefinition();
        task.setTaskId("A");
        task.setName("Task A");
        task.setStartDelay(null);
        task.setAutoStart(autoStart);
        task.setCmdLine(cmdLine);
        task.setType(TaskDefinition.THREAD_TASK);
        task.setEnvironment(Collections.<String, String>emptyMap());

        // Mock up a DAO that will return the task.
        TaskDefinitionDAO mockDAO = Mockito.mock(TaskDefinitionDAO.class);
        Mockito.when(mockDAO.readAllTasksForAllAndRoles())
            .thenReturn(Arrays.asList(task));

        // Mock up a ServerContextFactory that can be passed by TaskManager to
        // ThreadTask.
        ServerContextFactory mockFactory = Mockito.mock(ServerContextFactory.class,
            Mockito.RETURNS_MOCKS);

        _service = Executors.newFixedThreadPool(1);

        TaskExecutionDAO taskExecDao = Mockito.mock(TaskExecutionDAO.class);

        // Create a test Task Manager
        TaskManager manager = new TaskManager(_service, mockDAO, taskExecDao,
            mockFactory, 0, Collections.<String, String>emptyMap(), true);
        
        // What we're doing here is using a latch that will be marked
        // when the task is actually stopped so tests can wait for it
        // as this happens asynchronously 
        final CountDownLatch taskLatch = new CountDownLatch(1);
        listenersToAdd.add(new AbstractTaskManagerListener() {
            
            @Override
            public void onTaskStopped(TaskManager manager, TaskDefinition task) {
                taskLatch.countDown();
            }
        });
        
        for (TaskManagerListener listener : listenersToAdd) {
        	manager.addListener(listener);
        }

        // Verify that the Cache is initially empty.
        ConcurrentMap<Node, Set<TaskDefinition>> taskMap = manager.getClusteredTasks();
        assertEquals(0, taskMap.entrySet().size());
        
        // Start up the test Task Manager
        manager.start();
        if (autoStart) {
            Set<Entry<Node, Set<TaskDefinition>>> entries = taskMap.entrySet();
            assertEquals(1, entries.size());
            Set<TaskDefinition> tasks = entries.iterator().next().getValue();
            assertEquals(1, tasks.size());
            assertEquals(task, tasks.iterator().next());
        }
        else {
            assertEquals(0, taskMap.entrySet().size());
        }
        
        return new SingleTaskTestData(manager, task, taskLatch);
    }
    
    /**
     * Used to represent test data in tests bootstrapped by setupAndStartSingleTask.
     * Also supports waiting for the asynchronous task thread to stop 
     */
    private static class SingleTaskTestData {
        
        SingleTaskTestData(TaskManager manager, TaskDefinition task,
            CountDownLatch taskLatch) {
            _manager = manager;
            _task = task;
            _taskLatch = taskLatch;
        }
        
        // Helper method that uses a latch to wait up to this amount of time
        // for the task to actually stop as it may be in another thread
        public void waitForTaskToStop(long timeout, TimeUnit unit) throws InterruptedException {
            assertTrue("Timed out waiting for task to stop",
                _taskLatch.await(timeout, unit));
        }
        
        private final TaskManager _manager;
		private final TaskDefinition _task;
        private final CountDownLatch _taskLatch;
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testStartAndStopAlot() {
        TaskDefinitionDAO mockDAO = Mockito.mock(TaskDefinitionDAO.class);
        
        ServerContextFactory mockFactory = Mockito.mock(
                ServerContextFactory.class);
        
        TaskExecutionDAO taskExecDao = Mockito.mock(TaskExecutionDAO.class);
        
        TaskManager manager = new TaskManager(mockDAO, taskExecDao, mockFactory, 
                Integer.MAX_VALUE, Collections.EMPTY_MAP, false);
        
        for (int i = 0; i < 100; ++i) {
            manager.start();
            manager.stop();
        }
    }
   
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testChangeTaskAtRuntime() {
        // Create a task definition.
        TaskDefinition taskA1 = new TaskDefinition();
        taskA1.setTaskId("A");
        taskA1.setName("Task A");
        taskA1.setStartDelay(null);
        taskA1.setAutoStart(true);
        taskA1.setCmdLine(EmptyRunnable.class.getName());
        taskA1.setType(TaskDefinition.THREAD_TASK);
        taskA1.setEnvironment(Collections.EMPTY_MAP);
        
        TaskDefinition taskA2 = new TaskDefinition();
        taskA2.setTaskId("A");
        taskA2.setName("Task Foo");  // This is the only change from the task above.
        taskA2.setStartDelay(null);
        taskA2.setAutoStart(true);
        taskA2.setCmdLine(TestPublishUserRunnable.class.getName());
        taskA2.setType(TaskDefinition.THREAD_TASK);
        taskA2.setEnvironment(Collections.EMPTY_MAP);
        
        // Mock up a DAO that will return the task on the first call and an
        // the new one on the second and subsequent calls.  Those second 
        // and subsequence calls will be invoked from TaskManager.sync().
        TaskDefinitionDAO mockDAO = Mockito.mock(TaskDefinitionDAO.class);
        Mockito.when(mockDAO.readAllTasksForAllAndRoles()).thenReturn(
            Arrays.asList(taskA1), Arrays.asList(taskA2));

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
        
        try {
            testTaskManager.start(false);
            
            // The task should be submitted right away on start up.
            Mockito.verify(mockService, Mockito.timeout(50)).submit(Mockito.any(Runnable.class));
            
            // After the first sync runs the task should be cancelled.
            Mockito.verify(mockFuture, Mockito.timeout(1200)).cancel(true);
            
            try {
                Thread.sleep(2400);
            }
            catch (InterruptedException ignore) {
                // Ignore
            }
            
            // And after a quarantine time the task should be re-submitted.
            Mockito.verify(mockService, Mockito.times(2)).submit(Mockito.any(Runnable.class));
        }
        finally {
            testTaskManager.stop();
        }
    }
    
    /**
     * This test makes sure that the <code>TaskManager</code> doesn't start the task twice
     * if the taskid was mixed case.
     * 
     * @throws InterruptedException
     */
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testTaskMixedCase() throws InterruptedException {
        // Create a task definition.
        TaskDefinition taskA1 = new TaskDefinition();
        taskA1.setTaskId("Mixed_Case");
        taskA1.setName("Mixed_Case");
        taskA1.setStartDelay(null);
        taskA1.setAutoStart(true);
        taskA1.setCmdLine(EmptyRunnable.class.getName());
        taskA1.setType(TaskDefinition.THREAD_TASK);
        taskA1.setEnvironment(Collections.EMPTY_MAP);
        
        TaskDefinition taskA2 = new TaskDefinition();
        taskA2.setTaskId("Mixed_Case");
        taskA2.setName("Mixed_Case");
        taskA2.setStartDelay(null);
        taskA2.setAutoStart(true);
        taskA2.setCmdLine(EmptyRunnable.class.getName());
        taskA2.setType(TaskDefinition.THREAD_TASK);
        taskA2.setEnvironment(Collections.EMPTY_MAP);
        
        // Mock up a DAO that will return the task on the first call and an
        // the new one on the second and subsequent calls.  Those second 
        // and subsequence calls will be invoked from TaskManager.sync().
        TaskDefinitionDAO mockDAO = Mockito.mock(TaskDefinitionDAO.class);
        Mockito.when(mockDAO.readAllTasksForAllAndRoles()).thenReturn(
            Arrays.asList(taskA1), Arrays.asList(taskA2));

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
        
        try {
            testTaskManager.start(false);
            
            // Sleep so that we execute a TaskManager sync once.
            Thread.sleep(1500);

            Mockito.verify(mockService, Mockito.atMost(1)).submit(Mockito.any(Runnable.class));
        }
        finally {
            testTaskManager.stop();
        }
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testListenerNotifications() throws InterruptedException {
        // Create a task definition.
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
        taskA3Original.setAutoStart(true);
        taskA3Original.setCmdLine(EmptyRunnable.class.getName());
        taskA3Original.setType(TaskDefinition.THREAD_TASK);
        taskA3Original.setEnvironment(Collections.EMPTY_MAP);
        
        TaskDefinition taskA3Changed = new TaskDefinition();
        taskA3Changed.setTaskId("Task3");
        taskA3Changed.setName("Task3 Name Changed");
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
        
        // Mock up a Listener
        TaskManagerListener listener = Mockito.mock(TaskManagerListener.class);
        testTaskManager.addListener(listener);
        
        try {
            testTaskManager.start(false);
            
            // Validate listener fired off correctly
            Mockito.verify(listener).onStart(testTaskManager);
            Mockito.verify(listener).onTaskAdded(testTaskManager, taskA1);
            Mockito.verify(listener).onTaskAdded(testTaskManager, taskA3Original);
            Mockito.verify(listener).onTaskAdded(testTaskManager, taskA4Unchanged);
            
            // Sleep so that we execute a TaskManager sync once.
            Thread.sleep(1500);
            
            // Validate on sync the original task was removed
            // and the new task was added and listener notified
            // Task3's name was changed so change notification should fire
            Mockito.verify(listener).onTaskRemoved(testTaskManager, taskA1);
            Mockito.verify(listener).onTaskAdded(testTaskManager, taskA2);
            Mockito.verify(listener).onTaskChanged(testTaskManager, taskA3Original, taskA3Changed);
            
            // Make sure added only occurred once and changed event never occurred
            // for the unchanged task 4
            Mockito.verify(listener).onTaskAdded(testTaskManager, taskA4Unchanged);
            Mockito.verify(listener, Mockito.never())
                    .onTaskChanged(Mockito.any(TaskManager.class), 
                        Mockito.eq(taskA4Unchanged), Mockito.any(TaskDefinition.class));
            
        }
        finally {
            testTaskManager.stop();
            // Stop notification should have occurred
            Mockito.verify(listener).onStop(testTaskManager);
        }
    }
    
    /**
     * This is pretty much the same test as testListenerNotifications except instead
     * of waiting for a sync to occur the Task Manager is restarted and we verify the proper
     * notifications occur.
     * @throws InterruptedException
     */
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testListenerNotificationsOnRestart() throws InterruptedException {
        // Create a task definition.
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
        taskA3Original.setAutoStart(true);
        taskA3Original.setCmdLine(EmptyRunnable.class.getName());
        taskA3Original.setType(TaskDefinition.THREAD_TASK);
        taskA3Original.setEnvironment(Collections.EMPTY_MAP);
        
        TaskDefinition taskA3Changed = new TaskDefinition();
        taskA3Changed.setTaskId("Task3");
        taskA3Changed.setName("Task3 Name Changed");
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
        
        // Get a TaskManager to test with a long sync interval so the sync doesn't occur.
        TaskManager testTaskManager = new TaskManager(mockService, mockDAO, 
                taskExecDao, mockFactory, 60 * 60, Collections.EMPTY_MAP, true); 
        
        // Mock up a Listener
        TaskManagerListener listener = Mockito.mock(TaskManagerListener.class);
        testTaskManager.addListener(listener);
        
        try {
            testTaskManager.start(false);
            
            // Validate listener fired off correctly
            Mockito.verify(listener).onStart(testTaskManager);
            Mockito.verify(listener).onTaskAdded(testTaskManager, taskA1);
            Mockito.verify(listener).onTaskAdded(testTaskManager, taskA3Original);
            Mockito.verify(listener).onTaskAdded(testTaskManager, taskA4Unchanged);
            
            testTaskManager.restart();
            
            // Validate on a restart that the original task was removed
            // and the new task was added and listener notified
            // Task3's name was changed so change notification should fire
            Mockito.verify(listener).onTaskRemoved(testTaskManager, taskA1);
            Mockito.verify(listener).onTaskAdded(testTaskManager, taskA2);
            Mockito.verify(listener).onTaskChanged(testTaskManager, taskA3Original, taskA3Changed);
            
            // Make sure added only occurred once and changed event never occurred
            // for the unchanged task 4
            Mockito.verify(listener).onTaskAdded(testTaskManager, taskA4Unchanged);
            Mockito.verify(listener, Mockito.never())
                    .onTaskChanged(Mockito.any(TaskManager.class), 
                        Mockito.eq(taskA4Unchanged), Mockito.any(TaskDefinition.class));
            
            // Verify restart notification went off as well
            Mockito.verify(listener).onRestart(testTaskManager);
            
        }
        finally {
            testTaskManager.stop();
            // Stop notification should have occurred
            // It's important to note here that onStop is
            // only fired off once, it is not fired off during a restart!
            Mockito.verify(listener).onStop(testTaskManager);
        }
    }

    
    public final static String COMMAND = "publish data where user = @@USR_ID";
    
    public static class EmptyRunnable implements Runnable {

        // @see java.lang.Runnable#run()
        @Override
        public void run() {
            // This method does nothing.
        }
        
    }
    
    // Runnable that always throws an exception for testing tasks early failure
    public static class ExceptionRunnable implements Runnable {

        @Override
        public void run() {
            throw new RuntimeException("This runnable always throws an exception");
        }
        
    }
    
    /**
     * 
     * Runnable that sleeps forever but when is cancelled/interrupted it takes
     * a bit to cleanup, this is used to test a regression for restart of tasks
     * see {@link TU_TaskManager#testTaskRestartValidateCacheState()
     */
    public static class SlowToStopRunnable implements Runnable {

        @Override
        public void run() {
            try {
            	Thread.sleep(Long.MAX_VALUE);
            }
            catch (InterruptedException ex) {
            	try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
            }
        }
        
    }
    
    // This class has to be public so it can be created using reflection.
    public static class TestPublishUserRunnable implements Runnable {
        
        public TestPublishUserRunnable(MocaContext moca) {
            _ctx = moca;
        }

        // @see java.lang.Runnable#run()
        @Override
        public void run() {
            try {
                _ctx.executeCommand(COMMAND);
            }
            catch (MocaException e) {
                e.printStackTrace();
            }
        }

        private final MocaContext _ctx;
    }

    /**
     * @param args
     * @throws RemoteException
     * @throws AlreadyBoundException
     * @throws SystemConfigurationException
     * @throws NotBoundException 
     */
    public static void main(String[] args) throws RemoteException, 
    SystemConfigurationException, NotBoundException {

        try{
            if(args.length >= 2){
                // Setup the task log (passed as the 2nd parameter)
                File logFile = new File(args[1]);
                String logFileName = logFile.getName();
                // Need to strip the .log, since it will be automatically appended by the logging configuration
                logFileName = logFileName.replace(".log", "");
                ThreadContext.put("logFilename", logFileName);        
            }

            // NOTE: setting 2nd argument to true results in task failure in 9.0 (works fine in 9.1)
            ServerUtils.setupDaemonContext("Test", false, true);
            _logger.info("Done setting up daemon context.  Java uptime: " + ManagementFactory.getRuntimeMXBean().getUptime());

            String rmiPort = MocaUtils.currentContext().getRegistryValue(
                MocaRegistry.REGKEY_SERVER_RMI_PORT);
            int _rmiPort = rmiPort != null ? Integer.parseInt(rmiPort) : Integer
                    .parseInt(MocaRegistry.REGKEY_SERVER_RMI_PORT_DEFAULT);

            Registry registry = LocateRegistry.getRegistry(_rmiPort);
            TestRmiInterface rmiInter = (TestRmiInterface)registry.lookup(args[0]);

            _logger.info("Done setting up RMI registry");

            MocaContext ctx = MocaUtils.currentContext();

            // Now we execute the command retrieving the environment and returning
            // it.  If there is an exception we return that as well
            try {
                MocaResults result = ctx.executeCommand("publish data where user = @@USR_ID");
                _logger.info("Done Executing Moca Command");
                rmiInter.doRemoteCall(result);
                _logger.info("Done executing RMI call");
            }
            catch (MocaException e) {
                rmiInter.doRemoteCall(e);
            }
        } catch (Throwable e){
            _logger.error("ERROR running task", e);
        } finally {
            _logger.info("Done task.  Java uptime: " + ManagementFactory.getRuntimeMXBean().getUptime());

            // Shutdown logging
            LoggerContext context = (LoggerContext) LogManager.getContext();
            Configurator.shutdown(context);       
        }
    }

    public static interface TestRmiInterface extends Remote {
        public void doRemoteCall(Object obj) throws RemoteException;
    }

    private static class TestRmi implements TestRmiInterface {

        // @see
        // com.redprairie.moca.server.TI_TestRmiCalls.TestRmiInterface#doRemoteCall()
        @Override
        public void doRemoteCall(Object obj) throws RemoteException {
            try {
                _exchanger.exchange(obj);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                System.err.println("We were interruptted when we shouldn't have been!");
            }
        }

    }

    /**
     * This test will export an object in RMI then start up a process that will
     * load up the object in RMI and pass it a string.
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testStartedProcessHasEnvironment() throws Exception {
        _logger.info("Starting testStartedProcessHasEnvironment");
        // First we bind our object to RMI, so when we start up they can access
        // it.
        _logger.info("Setting up RMI binding");
        {
            TestRmi test = new TestRmi();
            
            String rmiPort = MocaUtils.currentContext().getRegistryValue(
                    MocaRegistry.REGKEY_SERVER_RMI_PORT);
            int _rmiPort = rmiPort != null ? Integer.parseInt(rmiPort) : 
                Integer.parseInt(MocaRegistry.REGKEY_SERVER_RMI_PORT_DEFAULT);
            
            Remote stub = UnicastRemoteObject.exportObject(test, 0);
            Registry registry = LocateRegistry.getRegistry(_rmiPort);
            
            // We bind it as a simple name
            registry.rebind(BINDING_NAME, stub);
        }

        _logger.info("Setting up Task Manager");
        
        File logDir = new File(System.getenv("LESDIR"), "log");
        _logFile = File.createTempFile(TU_TaskManager.class.getSimpleName(), ".log", logDir); 
        _logger.info("Log file for the process task: " + _logFile.getAbsolutePath());
        
        // We want to start up ourselves so that we can connect via RMI to
        // report the environment back
        // We have to make sure to double quote everything.
        StringBuilder commandLine = new StringBuilder();
        {
            String[] vmCommandLine = MocaUtils.newVMCommandLine();
    
            for (String vmCommandLineEntry : vmCommandLine) {
                commandLine.append("\"");
                commandLine.append(vmCommandLineEntry);
                commandLine.append("\" ");
            }
            if (commandLine.length() > 0) {
                commandLine.append(' '); 
            }
            commandLine.append('"');
            commandLine.append(this.getClass().getCanonicalName());
            // Pass arguments to the task:
            // - binding name
            // - log file
            commandLine.append("\" \"");
            commandLine.append(BINDING_NAME);
            commandLine.append("\" \"");
            commandLine.append(_logFile.getAbsolutePath());
            commandLine.append("\"");
        }

        String userId = "SpecialUserX12B";
        Map<String, String> environment = new HashMap<String, String>();
        environment.put("usr_id", userId);
        
        TaskDefinition task = new TaskDefinition();
        task.setTaskId("a");
        task.setName("Task A");
        task.setStartDelay(null);
        task.setAutoStart(true);
        task.setRestart(false);
        task.setCmdLine(commandLine.toString());
        task.setType(TaskDefinition.PROCESS_TASK);
        task.setEnvironment(environment);
        
        TaskDefinitionDAO mockDAO = Mockito.mock(TaskDefinitionDAO.class);
        Mockito.when(mockDAO.readAllTasksForAllAndRoles()).thenReturn(
                Arrays.asList(task));
        
        TaskExecutionDAO taskExecDao = Mockito.mock(TaskExecutionDAO.class);
        
        TaskManager testTaskManager = new TaskManager(mockDAO, taskExecDao, null, 
                Integer.MAX_VALUE, Collections.EMPTY_MAP, true); 

        MocaResults res = null;
        final Thread mainThread = Thread.currentThread();
        final AtomicBoolean canInterruptExchanger = new AtomicBoolean(true);
        try {

            testTaskManager.addListener(new AbstractTaskManagerListener() {

                public void onTaskStarted(TaskManager manager,
                                          TaskDefinition task) {
                    _logger.info("TaskManager started task: {}",
                        task.getName());
                }

                @Override
                public void onTaskStopped(TaskManager manager,
                                          TaskDefinition task) {
                    // NOTE: since we know that the task failed to start for
                    // some reason, we can signal the test not to wait
                    // any longer on the exchanger
                    String name = task.getName();
                    _logger.info("Task stopped: {}", name);

                    if (canInterruptExchanger.get()) {
                        _logger.info("Interrupting exchange.");
                        mainThread.interrupt();
                        canInterruptExchanger.set(false);
                    }
                    else {
                        _logger.info("Cannot interrupt exchange.");
                    }
                }
            });
            
            _logger.info("Starting Task Manager");
            testTaskManager.start(false);
            
            _logger.info("Starting exchanger exchange");
            // We give the process 300 seconds to start up
            Object exchanged = _exchanger.exchange(null, EXCHANGER_TIMEOUT, TimeUnit.SECONDS);
            canInterruptExchanger.set(false);
            _logger.info("Done with exchanger exchange");
            
            if (exchanged instanceof Exception) {
                throw (Exception)exchanged;
            }
            if (!(exchanged instanceof MocaResults)) {
                fail("We got something back other than a MocaResults " + 
                        exchanged);
            }
            
            res = (MocaResults)exchanged;
            
            assertTrue("There should be only 1 row", res.next());
            assertEquals(userId, res.getString("user"));
            assertFalse("There should be only 1 row", res.next());
        }
        catch (TimeoutException e) {
            fail("We timed out waiting for the process to come up and respond " +
            		"with the user id.");
        }
        finally {
            if(res != null){
                res.close();
            }

            testTaskManager.stop();
            
            // Print the log from the child process to help troubleshooting timeout failures
            _logger.info("Dumping task log file contents");
            _logger.info("======================================================");
            _logger.info("LOGFILE " + _logFile.getAbsolutePath() +  " contents:");
            TestUtils.dumpFileContentsToLog(_logFile, _logger);
            _logger.info("======================================================");

        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testDaemonRestartNoStop() throws InterruptedException {
        // Create a task definition.
        TaskDefinition task = new TaskDefinition();
        task.setTaskId("A");
        task.setName("Task A");
        task.setStartDelay(null);
        task.setAutoStart(true);
        task.setCmdLine(EmptyRunnable.class.getName());
        task.setType(TaskDefinition.DAEMON_TASK);
        task.setEnvironment(Collections.EMPTY_MAP);
        
        // Mock up a DAO that will return the task on the first call and an
        // empty collection on the second and subsequent calls.  Those second 
        // and subsequence calls will be invoked from TaskManager.sync().
        TaskDefinitionDAO mockDAO = Mockito.mock(TaskDefinitionDAO.class);
        Mockito.when(mockDAO.readAllTasksForAllAndRoles()).thenReturn(
            Arrays.asList(task));

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
                taskExecDao, mockFactory, Integer.MAX_VALUE,
                Collections.EMPTY_MAP, true);
        
        try {
            testTaskManager.start();
            
            // We wait to make sure it was submitted
            Mockito.verify(mockService, Mockito.timeout(500)).submit(
                Mockito.any(Runnable.class));
            
            testTaskManager.restart();
            
            // We give it a little bit to possibly stop our task
            Thread.sleep(200);
            
            // We should have then had it stop.
            Mockito.verify(mockFuture, Mockito.never()).cancel(
                Mockito.anyBoolean());
        }
        finally {
            testTaskManager.stop();
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testDaemonTaskWasRemovedAndShutsDownOnRestart() {
        // Create a task definition.
        TaskDefinition task = new TaskDefinition();
        task.setTaskId("A");
        task.setName("Task A");
        task.setStartDelay(null);
        task.setAutoStart(true);
        task.setCmdLine(EmptyRunnable.class.getName());
        task.setType(TaskDefinition.DAEMON_TASK);
        task.setEnvironment(Collections.EMPTY_MAP);
        
        // Mock up a DAO that will return the task on the first call and an
        // empty collection on the second and subsequent calls.  Those second 
        // and subsequence calls will be invoked from TaskManager.sync().
        TaskDefinitionDAO mockDAO = Mockito.mock(TaskDefinitionDAO.class);
        Mockito.when(mockDAO.readAllTasksForAllAndRoles()).thenReturn(
            Arrays.asList(task), Collections.EMPTY_LIST);
        
        Mockito.when(mockDAO.read(Mockito.eq(task.getTaskId()))).thenReturn(task);

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
        
        try {
            testTaskManager.start();
            
            // We wait to make sure it was submitted
            Mockito.verify(mockService, Mockito.timeout(500)).submit(
                Mockito.any(Runnable.class));
            
            testTaskManager.restart();
            
            // We should have then had it stop.
            Mockito.verify(mockFuture, Mockito.timeout(500)).cancel(true);
        }
        finally {
            testTaskManager.stop();
        }
    }
    
    @Test
    public void testTaskManagerRestartContinuallyWhileSyncing() throws TaskException, InterruptedException {
        final int sync = 1;
        
        TaskDefinitionDAO mockDAO = Mockito.mock(TaskDefinitionDAO.class);
        ServerContextFactory mockFactory = Mockito.mock(
            ServerContextFactory.class, Mockito.RETURNS_MOCKS);
        
        Map<String, String> emptyMap = Collections.emptyMap();
        
        TaskExecutionDAO taskExecDao = Mockito.mock(TaskExecutionDAO.class);
        
        // We extend this class to put an artifical wait to easily reproduce the
        // deadlocking issue
        final TaskManager testTaskManager = new TaskManager(mockDAO, taskExecDao, 
            mockFactory, sync, emptyMap, true) {
            // @see com.redprairie.moca.task.TaskManager#stop()
            @Override
            public void stop() {
                try {
                    Thread.sleep((sync * 2) * 1000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                super.stop();
            }
        };
        
        testTaskManager.start();
        Thread thread = new Thread() {
            // @see java.lang.Thread#run()
            @Override
            public void run() {
                // This should now wait until sync happens and get stuck
                testTaskManager.restart();
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
    public void testTaskManagerStartWhileSyncing() throws TaskException, 
            InterruptedException, BrokenBarrierException, TimeoutException, 
            ExecutionException {
        final int sync = 1;
        final String taskName = "test-Task123";
        
        // This barrier is trigger when the sync method is to tell us the sync
        // is now running
        final CyclicBarrier barrier = new CyclicBarrier(2);
        // This latch is so we can then try to start a task then let the sync
        // go, which helps detect possible deadlock conditions
        final CountDownLatch latch = new CountDownLatch(1);
        
        TaskDefinitionDAO mockDAO = Mockito.mock(TaskDefinitionDAO.class);
        final TaskDefinition task = Mockito.mock(TaskDefinition.class);
        Mockito.when(task.getTaskId()).thenReturn(taskName);
        Mockito.when(task.getType()).thenReturn(TaskDefinition.THREAD_TASK);
        final AtomicInteger value = new AtomicInteger();
        // Make it so the task changes every time, which should cause a start
        // and stop action to occur
        Mockito.when(task.getCmdLine()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return String.valueOf(value.getAndIncrement());
            }
        });
        Mockito.when(mockDAO.readAll())
            .thenReturn(Collections.singletonList(task))
            .thenAnswer(new Answer<List<TaskDefinition>>() {
                @Override
                public List<TaskDefinition> answer(InvocationOnMock invocation)
                        throws Throwable {
                    barrier.await();
                    latch.await();
                    return Collections.singletonList(task);
                }
            });
        ServerContextFactory mockFactory = Mockito.mock(
            ServerContextFactory.class, Mockito.RETURNS_MOCKS);
        
        Map<String, String> emptyMap = Collections.emptyMap();
        ExecutorService executor = Mockito.mock(ExecutorService.class, 
            Mockito.RETURNS_MOCKS);
        
        TaskExecutionDAO taskExecDao = Mockito.mock(TaskExecutionDAO.class);
        
        // We extend this class to put an artifical wait to easily reproduce the
        // deadlocking issue
        final TaskManager testTaskManager = new TaskManager(executor, mockDAO, 
            taskExecDao, mockFactory, sync, emptyMap, true);
        testTaskManager.noCluster();
        
        testTaskManager.start();
        
        // Wait for sync to happen first
        barrier.await(2, TimeUnit.SECONDS);
        
        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<?> future = service.submit(new Runnable() {
            // @see java.lang.Thread#run()
            @Override
            public void run() {
                // This should now wait until sync happens and get stuck
                testTaskManager.startTask(taskName);
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
        testTaskManager.stop();
    }
    
    static Exchanger<Object> _exchanger;
    private ExecutorService _service;
    private InstanceUrl _realUrl;
    private InstanceUrl _mockUrl;
}