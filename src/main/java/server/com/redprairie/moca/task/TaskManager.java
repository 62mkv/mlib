/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009-2010
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

package com.redprairie.moca.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.cluster.ClusterCachable;
import com.redprairie.moca.cluster.ClusterRoleAware;
import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.server.InstanceUrl;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.task.dao.TaskDefinitionDAO;
import com.redprairie.moca.task.dao.TaskExecutionDAO;
import com.redprairie.moca.util.DaemonThreadFactory;
import com.redprairie.moca.util.ExceptionSuppressingRunnable;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.util.EnvironmentExpander;

/**
 * Manager for running MOCA tasks.
 * 
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class TaskManager extends ClusterCachable<TaskDefinition> implements ClusterRoleAware {
    
    public TaskManager(TaskDefinitionDAO dao, TaskExecutionDAO taskExecDAO,
            ServerContextFactory factory, int syncSeconds,
            Map<String, String> environment, boolean enableTaskStartup) {
        this(Executors.newCachedThreadPool(), dao, taskExecDAO, factory, 
            syncSeconds, environment, enableTaskStartup);
    }
    
    /**
     * This constructor allows the user to supply an executor service that will
     * be used when running new tasks.  This allows the caller to control how
     * many running tasks there and monitor the status of some of them.
     * @param threadPool The executor service to use when running tasks
     * @param sys The system context to get variables from
     * @param dao The dao to read in task information from
     * @param factory The server context factory to use when using Task Threads
     */
    public TaskManager(ExecutorService threadPool, TaskDefinitionDAO dao, 
            TaskExecutionDAO taskExecDAO, ServerContextFactory factory, 
            int syncSeconds, Map<String, String> environment, boolean enableTaskStartup) {
        this(threadPool, dao, taskExecDAO, factory, syncSeconds, environment, enableTaskStartup, null, null, null);
    }
    
    /**
     * This constructor allows for the initialization of the Cluster Caching
     * based on the Local Node in the cluster and TaskDefinitons.
     * 
     * @param threadPool
     * @param dao
     * @param taskExecDAO
     * @param factory
     * @param syncSeconds
     * @param environment
     * @param enableTaskStartup
     * @param clusterCache
     * @param localNode
     */
    public TaskManager(ExecutorService threadPool, TaskDefinitionDAO dao, 
            TaskExecutionDAO taskExecDAO, ServerContextFactory factory, 
            int syncSeconds, Map<String, String> environment, boolean enableTaskStartup,
            ConcurrentMap<Node, Set<TaskDefinition>> clusterCache, Node localNode, String name) {
        super(clusterCache, localNode, name);
        _threadPool = threadPool;
        _dao = dao;
        _taskExecDao = taskExecDAO;
        _factory = factory;
        _syncSeconds = syncSeconds;

        _env = new HashMap<String, String>(environment);
    
        _disableStart = !enableTaskStartup;
        addListener(new ClusterCacheListener());
      
        if (_disableStart) {
            _logger.info("Task auto start is disabled");
        }
    }
        
    public void start() {
        start(_disableStart);
    }
    
    /**
     * Starts all tasks.  It is possible only to load the definitions and not
     * start tasks if desired.
     * @param autoStartTasks If true than the task manager will automatically
     *        start any tasks that are marked as having auto start.  If false
     *        then no tasks will be started and only definitions at this time
     *        will be loaded.
     */
    public void start(boolean disableStart) {
        _syncLock.lock();
        try {
            _logger.info("Starting the task manager");
            
            if (_currentTasks != null) {
                throw new IllegalStateException("Task manager is already running");
            }
            
            // Build the current tasks list. 
            _currentTasks = new TaskMap();
    
            List<TaskDefinition> definedTaskList = getDefinedTasks();
            for (TaskDefinition task : definedTaskList) {
                _currentTasks.put(task.getTaskId(), task);
            }
            
            // We have to make sure that any daemon tasks that are running are shut
            // down if they are not a current task as well.
            Set<String> runningTasks = _runningTasks.keySet();
            for (String runningTask : runningTasks) {
                if (_currentTasks.get(runningTask) == null) {
                	// Try to get the task definition (if it still exists)
                	// otherwise just use a stubbed task definition.
                	TaskDefinition taskDef = _dao.read(runningTask);
                	if (taskDef == null) {
                		taskDef = new TaskDefinition();
                		taskDef.setTaskId(runningTask);
                		taskDef.setType(TaskDefinition.DAEMON_TASK);
                	}
                    stopRunningTask(taskDef);
                }
            }
            
            // If scheduling isn't disabled add enabled jobs to the job scheduler.
            if (!disableStart) {
                for (TaskDefinition task : definedTaskList) {
                    // If the task is auto start and isn't currently running then we
                    // have to start it
                    if (task.isAutoStart() && !_runningTasks.containsKey(
                            task.getTaskId())) {
                        Integer startDelay = task.getStartDelay();
                        if (startDelay == null) {
                            startTask(task.getTaskId(), 0, "AUTOSTART");
                        }
                        else {
                            startTask(task.getTaskId(), startDelay, "AUTOSTART");
                        }
                    }
                }
            }
            
            // Create and start a task synchronizer thread.
            // We don't run a synchronizer thread if our sync seconds is set
            // to 0, which will be the case if we were instantiated from TaskRunnerMain.
            if (!disableStart && _syncSeconds != 0) {
                _taskSynchronizerHandle = _scheduler.scheduleAtFixedRate(
                        new ExceptionSuppressingRunnable(new TaskSynchronizerRunnable(), _logger), 
                        _syncSeconds, _syncSeconds, TimeUnit.SECONDS);
            }
        }
        finally {
            _syncLock.unlock();
        }
        
        notifyStarted();
    }
    
    /**
     * Stop all the running tasks on the server.
     */
    public void stop() {
        _syncLock.lock();
        try {
            _logger.info("Stopping the task manager...");
            
            if (_currentTasks == null) {
                throw new IllegalStateException("Task manager is not running");
            }
            
            Iterator<Entry<String, Future<?>>> iterator = 
                _runningTasks.entrySet().iterator();
            
            // Cancel all the futures which should stop the tasks
            while (iterator.hasNext()) {
                Entry<String, Future<?>> entry = iterator.next();
                String taskId = entry.getKey();
                
                TaskDefinition taskDef = _currentTasks.get(taskId);
                
                // We don't stop daemon tasks
                if (!taskDef.getType().equals(TaskDefinition.DAEMON_TASK)) {
                    Future<?> future = entry.getValue();
                    future.cancel(true);
                    notifyTaskStopped(taskDef);
                    
                    // We also want to remove it from the collection so we don't think
                    // it is running.
                    iterator.remove();
                }
                else {
                    _logger.debug(MocaUtils.concat("Task [", taskId, "] was not stoppped due to " +
                    		"being a daemon task."));
                }
            }
            
            // We have to null out the current tasks which is the signifier that
            // we are indeed stopped.
            _currentTasks = null;
            
            // Stop the task synchronizer schedule.
            if (_taskSynchronizerHandle != null) {
                _taskSynchronizerHandle.cancel(true);
            }
        }
        finally {
            _syncLock.unlock();
        }
        
        notifyStopped();
    }
    
    /**
     * This is to stop and restart all the tasks.
     */
    public void restart() {
        _syncLock.lock();
        
        try {
            // Grab a snapshot of the tasks before restarting
            Map<String, TaskDefinition> oldTasks =
                   new LinkedHashMap<String, TaskDefinition>(_currentTasks._taskMap);
            try {
                _restarting = true;
                
                _logger.info("Restarting the task manager...");
                stop();
                start();
            }
            finally {
                _restarting = false;
                notifyRestarted(oldTasks);
            }
        }
        finally {
            _syncLock.unlock();
        }
    }
    
    /**
     * This method is used to start a specific task.
     * @param taskId The task identifier that is case sensitive.
     * @throws IllegalArgumentException This is thrown if the task isn't 
     *         present.
     * @throws IllegalStateException This occurs if the task is already running.
     */
    public void startTask(String taskId) {
        startTask(taskId, 0, "USERSTART");
    }
    
    private void startTask(String taskId, int startDelay, String reason) 
            throws IllegalArgumentException, IllegalStateException {
        _syncLock.lock();
        try {
            // If we aren't configured
            TaskDefinition task = _currentTasks.get(taskId);
            if (task == null) {
                _logger.warn("Task " + taskId + " was not found!");
                throw new IllegalArgumentException("Task " + taskId + 
                        " was not found!");
            }
            
            // If the task is already running don't try to start it.
            Future<?> future = _runningTasks.get(taskId);
            if (future != null && !future.isDone()) {
                _logger.warn("Task " + taskId + " was already started!");
                throw new IllegalStateException("Task " + taskId + 
                        " was already started!");
            }
            
            Map<String, String> environment = new HashMap<String, String>(_env);
            
            // We have to now overwrite that with the environment from the task
            // and capitalize everything first
            for (Entry<String, String> entry : task.getEnvironment().entrySet()) {
                environment.put(entry.getKey().toUpperCase(), entry.getValue());
            }
            
            // Now we have to expand them all
            _envExpander.expand(environment);
            
            RunningTask running = null;
            
            InstanceUrl url = ServerUtils.globalAttribute(InstanceUrl.class);
            TaskExecution taskExec = new TaskExecution(task, url);
            taskExec.setStartCause(reason);
            
            String type = task.getType();
            if (type.equals(TaskDefinition.PROCESS_TASK) || 
                    type.equals(TaskDefinition.DAEMON_TASK)) {
                running = new ProcessTask(task, taskExec, _taskExecDao, 
                    environment, startDelay);
            }
            else if (type.equals(TaskDefinition.THREAD_TASK)) {
                running = new ThreadTask(task, taskExec, _taskExecDao, 
                    environment, _factory, startDelay);
            }
            else {
                _logger.error("Unsupported task type [" + type + "] for task [" + 
                        task.getTaskId() + "]");
            }
    
            if (running != null) {
                _logger.info("Starting task " + taskId); 
                future = submitRunnableWithListener(task, running);
                _runningTasks.put(taskId, future);
                for (TaskManagerListener listener : _listeners) {
                    listener.onTaskStarted(this, task);
                }
            }       
        }
        finally {
            _syncLock.unlock();
        }
    }
    
    /**
     * This is to stop a specific task.
     * @param taskId The task identifier that is case sensitive. 
     * @throws IllegalArgumentException This is thrown if the task isn't 
     *         present.
     * @throws IllegalStateException This occurs if the task is not running.
     */
    public void stopTask(String taskId) 
            throws IllegalArgumentException, IllegalStateException {
        _syncLock.lock();
        try {
            // If we aren't configured
            TaskDefinition task = _currentTasks.get(taskId);
            if (task == null) {
                _logger.warn("Task " + taskId + " was not found!");
                throw new IllegalArgumentException("Task " + taskId + 
                        " was not found!");
            }
            
            stopRunningTask(task);
        }
        finally {
            _syncLock.unlock();
        }
    }
    
    private void stopRunningTask(TaskDefinition taskDef) {
        // If the task isn't running don't try to stop it
    	final String taskId = taskDef.getTaskId();
        Future<?> future = _runningTasks.get(taskId);
        if (future == null || future.isDone()) {
            _logger.warn("Task " + taskId + " is not running!");
            throw new IllegalStateException("Task " + taskId + 
                    " is not running!");
        }
        
        _logger.info("Stopping task " + taskId);
        _runningTasks.remove(taskId);
        future.cancel(true);
        notifyTaskStopped(taskDef);
    }
    
    /**
     * Determine if a task is currently running.
     * @param taskId The task identifier that is case sensitive. 
     * @throws IllegalArgumentException This is thrown if the task isn't 
     *         present.
     */
    public boolean isRunning(String taskId) {
        _syncLock.lock();
        try {
            // If we aren't configured
            TaskDefinition task = _currentTasks.get(taskId);
            if (task == null) {
                _logger.warn("Task " + taskId + " was not found!");
                throw new IllegalArgumentException("Task " + taskId + 
                        " was not found!");
            }
            
            // If the task isn't running don't try to stop it
            Future<?> future = _runningTasks.get(taskId);
            if (future == null || future.isDone()) {
                return false;
            }
            else {
                return true;
            }
        }
        finally {
            _syncLock.unlock();
        }
    }
    
    /**
     * Returns a copy of the configured environment for the task manager
     * @return copy of the environment
     */
    public Map<String, String> getEnvironment() {
        _syncLock.lock();
        try {
            return new HashMap<String, String>(_env);
        }
        finally {
            _syncLock.unlock();
        }
    }
    
    /**
     * Provides a current list of all tasks.  This includes tasks that are not currently started.
     * 
     * @return a current list of all configured tasks.
     */
    public List<TaskDefinition> getAllTasks() {
        return new ArrayList<TaskDefinition>(_dao.readAll());
    }
    
    /**
     * Provides a Map of the active Tasks throughout the cluster by Node.
     * 
     * @return a Map of all of the active Tasks throughout the cluster by Node.
     */
    public ConcurrentMap<Node, Set<TaskDefinition>> getClusteredTasks() {
        return getClusterCache();
    }
    
    /**
     * Provides a current list of configured tasks.  This includes tasks that are not currently started.
     * 
     * @return a current list of configured tasks.
     */
    public List<TaskDefinition> getCurrentTasks() {
        _syncLock.lock();
        try {
            if (_currentTasks == null) {
                return new ArrayList<TaskDefinition>();
            }
            else {
                return new ArrayList<TaskDefinition>(_currentTasks.values());
            }
        }
        finally {
            _syncLock.unlock();
        }
    }
    
    public TaskDefinition getCurrentTask(String taskId) {
        _syncLock.lock();
        try {
            if (_currentTasks == null) {
                return null;
            }
            else {
                return _currentTasks.get(taskId);
            }
        }
        finally {
            _syncLock.unlock();
        }
    }
    
    /**
     * Registers a Task Manager listener that 
     * responds to notifications.
     * @param listener The Task Manager Listener to register
     */
    public void addListener(TaskManagerListener listener) {
        _listeners.add(listener);
    }
    
    /**
     * Unregisters a Task Manager listener
     * @param listener The Task Manager Listener to unregister
     */
    public void removeListener(TaskManagerListener listener) {
        _listeners.remove(listener);
    }
    
    //
    // Implementation
    //
    private List<TaskDefinition> getDefinedTasks() {
        if (_clusterDisabled) {
            return _dao.readAll();
        }
        else {
            return _dao.readAllTasksForAllAndRoles(_nodeIds.toArray(
                new RoleDefinition[_nodeIds.size()]));
        }
    }
    
    private void sync() throws TaskException {
        _logger.debug("Synchronizing tasks...");
        
        // Build a map of the defined task list.  This is the list of tasks from
        // the task_definition table and could be different than the current task list.
        List<TaskDefinition> definedTaskList = getDefinedTasks();
        Map<String, TaskDefinition> definedTaskMap = new HashMap<String, TaskDefinition>();      
        for (TaskDefinition task : definedTaskList) {  
            definedTaskMap.put(task.getTaskId(), task);
        }
        
        try {
            MocaUtils.currentContext().commit();
        }
        catch (MocaException e) {
            throw new TaskException("Unable to commit transaction: " + e);
        }   
        
        // Build a map of the current task list.  This is the list of tasks that the
        // Task Manager is currently managing.  Any differences between this list and
        // the defined task list will be applied to this list.
        Map<String, TaskDefinition> currentTaskMap = new HashMap<String, TaskDefinition>();    
        
        List<TaskDefinition> currentTaskList = getCurrentTasks();   
        for (TaskDefinition task : currentTaskList) {  
            currentTaskMap.put(task.getTaskId(), task);
        }
        
        for (Map.Entry<String, TaskDefinition> definedTaskEntry : definedTaskMap.entrySet()) {
            String definedTaskId = definedTaskEntry.getKey();
            TaskDefinition definedTask = definedTaskEntry.getValue();
            TaskDefinition currentTask = currentTaskMap.get(definedTaskId);
            
            // Check for any tasks that were added.  Tasks that were added will
            // exist in the defined task list, but not in the current task list.
            if (currentTask == null) {
                _logger.debug(MocaUtils.concat("Adding task ", definedTaskId));
                
                // Add this task to the current tasks list.      
                _currentTasks.put(definedTaskId, definedTask);                   
                
                // If scheduling isn't disabled and this task is enabled add it to the task scheduler.
                if (!_disableStart) {
                    if (definedTask.isAutoStart()) {                       
                        // Start the task, making sure we quarantine it for a bit.
                        startTask(definedTaskId, _syncSeconds * 2, "AUTOSTART");
                    }
                }           
            }
            
            // Check for any tasks that were changed.  Tasks that were changed will
            // exist in both the current and defined task lists, but will not be equal.
            else if (!tasksAreEqual(definedTask, currentTask)) {
                _logger.debug(MocaUtils.concat("Updating task ", definedTaskId));
                
                // If the task is running stop it first
                if (isRunning(currentTask.getTaskId())) {
                    // Stop this task.
                    stopTask(currentTask.getTaskId());
                }

                // Add this task to the current tasks list.
                _currentTasks.put(definedTaskId, definedTask);                   
                
                // If scheduling isn't disabled and this task is enabled add it to the task scheduler.
                if (!_disableStart) {
                    if (definedTask.isAutoStart()) {
                        // Start the task, making sure we quarantine it for a bit.
                        startTask(definedTaskId, _syncSeconds * 2, "AUTOSTART");
                    }
                }
            }  
        }
        
        // Check for any tasks that were removed.  Tasks that were removed
        // will exist in the current task list, but not the defined task list.
        for (Entry<String, TaskDefinition>  currentTaskEntry : currentTaskMap.entrySet()) {
            String currentTaskId = currentTaskEntry.getKey();
            TaskDefinition currentTask = currentTaskEntry.getValue();
            
            if (!definedTaskMap.containsKey(currentTaskId)) {      
                _logger.debug(MocaUtils.concat("Removing task ", currentTaskId));
                
                // If the task is running we have to stop it
                if (isRunning(currentTask.getTaskId())) {
                    // Stop this task.
                    stopTask(currentTask.getTaskId());
                }
                
                // Remove this task from the current tasks list.
                _currentTasks.remove(currentTaskId);       
            }
        }
    }
    
    private boolean tasksAreEqual(TaskDefinition task1, TaskDefinition task2) {    
        if (task1 == task2) return true;
        
        if (!valuesAreEqual(task1.getTaskId(),       task2.getTaskId()))       return false;
        if (!valuesAreEqual(task1.getRole(),       task2.getRole()))       return false;
        if (!valuesAreEqual(task1.getName(),         task2.getName()))         return false;
        if (!valuesAreEqual(task1.getCmdLine(),      task2.getCmdLine()))      return false;
        if (!valuesAreEqual(task1.getRunDirectory(), task2.getRunDirectory())) return false;
        if (!valuesAreEqual(task1.getLogFile(),      task2.getLogFile()))      return false;
        if (!valuesAreEqual(task1.getType(),         task2.getType()))         return false;
        if (!valuesAreEqual(task1.getEnvironment(),  task2.getEnvironment()))  return false;
        if (!valuesAreEqual(task1.getStartDelay(),   task2.getStartDelay()))   return false;
        if (!valuesAreEqual(task1.isRestart(),       task2.isRestart()))       return false;
        if (!valuesAreEqual(task1.isAutoStart(),     task2.isAutoStart()))     return false;
        
        return true;
    }
    
    private boolean valuesAreEqual(Object value1, Object value2) {
        if (value1 != null) {
            if (!value1.equals(value2)) {
                return false;
            }
        } 
        else if (value2 != null) {
            return false;
        }
        
        return true;
    }
    
    private void notifyStarted() {
        // Don't fire this trigger during restarts
        if (_restarting) return;
        
        for (TaskManagerListener listener : _listeners) {
            listener.onStart(this);
        }
    }
    
    private void notifyStopped() {
        // Don't fire this trigger during restarts
        if (_restarting) return;
        
        for (TaskManagerListener listener : _listeners) {
            listener.onStop(this);
        }
    }
    
    private void notifyRestarted(Map<String, TaskDefinition> oldTasks) {
        // A restart effectively causes a sync so we need to see
        // what changed since then.
        // Handle notifications for removed/changed tasks
        for (TaskDefinition taskDef : oldTasks.values()) {
            TaskDefinition currentTask = null;
            // Check if a task was removed
            if ((currentTask = _currentTasks.get(taskDef.getTaskId().toUpperCase())) == null) {
                notifyTaskRemoved(taskDef);
            }
            // Check if the task changed
            else if (!tasksAreEqual(taskDef, currentTask)) {
                notifyTaskChanged(taskDef, currentTask);
            }
        }
        
        // Handle notifications for added tasks
        for (TaskDefinition taskDef : _currentTasks.values()) {
           if (!oldTasks.containsKey(taskDef.getTaskId().toUpperCase())) {
               notifyTaskAdded(taskDef);
           }
        }
        
        // Finally notify that the task manager was restarted
        for (TaskManagerListener listener : _listeners) {
            listener.onRestart(this);
        }
    }
    
    private void notifyTaskAdded(TaskDefinition task) {
        // Don't fire this trigger during restarts
        if (_restarting) return;
        
        for (TaskManagerListener listener : _listeners) {
            listener.onTaskAdded(this, task);
        }
    }
    
    private void notifyTaskChanged(TaskDefinition oldTask, TaskDefinition newTask) {
        for (TaskManagerListener listener : _listeners) {
            listener.onTaskChanged(this, oldTask, newTask);
        }
    }
    
    private void notifyTaskRemoved(TaskDefinition task) {
        for (TaskManagerListener listener : _listeners) {
            listener.onTaskRemoved(this, task);
        }
    }

    private void notifyTaskStopped(TaskDefinition task) {
        for (TaskManagerListener listener : _listeners) {
            listener.onTaskStopped(this, task);
        }
    }
    
    private class TaskSynchronizerRunnable implements Runnable {
        public void run() {
            _syncLock.lock();
            try {
                sync();
            }
            catch (MocaInterruptedException e) {
                _logger.info("TaskSynchronizer Interrupted: " + e);
            }  
            catch (TaskException e) {
                _logger.error("Unable to resync tasks: " + e);
            }
            finally {
                ServerUtils.getCurrentContext().close();
                ServerUtils.removeCurrentContext();
                _syncLock.unlock();
            }
        }
    }
    
    private class TaskMap {
        public TaskMap() {
            _taskMap = new LinkedHashMap<String, TaskDefinition>();
        }

        public TaskDefinition put(String key, TaskDefinition task) {
            String upperCaseKey = key.toUpperCase();
            TaskDefinition oldTask = _taskMap.put(upperCaseKey, task);
            if (oldTask == null) {
                notifyTaskAdded(task);
            }
            else {
                notifyTaskChanged(oldTask, task);
            }
            return oldTask;
        }
        
        public TaskDefinition get(String key) {
            String upperCaseKey = key.toUpperCase();
            return _taskMap.get(upperCaseKey);
        }
        
        public TaskDefinition remove(String key) {
            String upperCaseKey = key.toUpperCase();
            TaskDefinition oldTask = _taskMap.remove(upperCaseKey);
            notifyTaskRemoved(oldTask);
            return oldTask;
        }

        public Collection<TaskDefinition> values() {
            return _taskMap.values();
        }
 
        private final LinkedHashMap<String, TaskDefinition> _taskMap;
    }
    
    // @see com.redprairie.moca.cluster.ClusterRoleAware#activateRole(com.redprairie.moca.cluster.NodeDefinition)
    @Override
    public void activateRole(RoleDefinition role) {
        _syncLock.lock();
        try {
            _nodeIds.add(role);
        }
        finally {
            _syncLock.unlock();
        }
    }

    // @see com.redprairie.moca.cluster.ClusterRoleAware#deactivateRole(com.redprairie.moca.cluster.NodeDefinition)
    @Override
    public void deactivateRole(RoleDefinition role) {
        _syncLock.lock();
        try {
            _nodeIds.remove(role);
        }
        finally {
            _syncLock.unlock();
        }
    }

    // @see com.redprairie.moca.cluster.ClusterRoleAware#noCluster()
    @Override
    public void noCluster() {
        _clusterDisabled = true;
    }

    private Future<?> submitRunnableWithListener(TaskDefinition task, Runnable runnable) {
    	RunnableListener runnableListener = new RunnableListener(task, runnable); 
    	Future<?> future = _threadPool.submit(runnableListener);
        runnableListener.setFuture(future);
        return future;
    }
    
    // Wrapper class so we have visibility on the runnable ending
    private class RunnableListener implements Runnable {
        
        RunnableListener(TaskDefinition task, Runnable inner) {
            _task = task;
            _innerRunnable = inner;
            _futureRef = new AtomicReference<Future<?>>();
        }
        
        @Override
        public void run() {
            try {
                _innerRunnable.run();
            }
            finally {
            	Future<?> future = _futureRef.get();
            	// If the future is null or it hasn't been cancelled
            	// this means the task is ending without being explicitly
            	// stopped (via stopTask) so we want to notify listeners.
            	// Normal stops are notified in stopTask().
            	if (future == null || !future.isCancelled()) {
            		_logger.debug("The task [{}] ended without being explicitly stopped, " +
            				     "notifying listeners.",
            				_task.getTaskId());
	                TaskManager.this.notifyTaskStopped(_task);
            	}
            	else {
            		_logger.debug("The task [{}] ended due to being stopped",
            				_task.getTaskId());
            	}
            }
        }
        
        void setFuture(Future<?> future) {
        	_futureRef.set(future);
        }
        
        private final Runnable _innerRunnable;
        private final TaskDefinition _task;
        private final AtomicReference<Future<?>> _futureRef;
    }
    
    // Used to manage the state of a clustered cache that tracks whether the task
    // has started or stopped.
    private class ClusterCacheListener extends AbstractTaskManagerListener {

        @Override
        public void onTaskStarted(TaskManager manager, TaskDefinition task) {
            TaskManager.this.addToClusterCache(task);
        }

        @Override
        public void onTaskStopped(TaskManager manager, TaskDefinition task) {
            TaskManager.this.removeFromClusterCache(task);
        }
        
    }

	// @see com.redprairie.moca.cluster.ClusterRoleAware#handleMerge(boolean)

	@Override
	public void handleMerge(List<Node> members, Node local) {
		if (!_disableStart) {
			replicate();
		}
	}
    
    /**
     * This lock is to be acquired after the startStopLock if applicable or
     * first if doing a sync operation
     */
    private final Lock _syncLock = new ReentrantLock();
    
    private final ExecutorService _threadPool;
    private final TaskDefinitionDAO _dao;
    private final TaskExecutionDAO _taskExecDao;
    private final Set<RoleDefinition> _nodeIds = new HashSet<RoleDefinition>();
    private final boolean _disableStart;
    private final Map<String, String> _env;
    private final ServerContextFactory _factory;
    private final int _syncSeconds;
    
    private final ScheduledExecutorService _scheduler = Executors.newSingleThreadScheduledExecutor(
            new DaemonThreadFactory("MOCA-TaskSynchronizerThread", false));
    private ScheduledFuture<?> _taskSynchronizerHandle;
    
    private TaskMap _currentTasks;
    private boolean _clusterDisabled = false;
    
    private boolean _restarting = false;
    
    private final Map<String, Future<?>> _runningTasks = new HashMap<String, Future<?>>();
    
    private final List<TaskManagerListener> _listeners = new CopyOnWriteArrayList<TaskManagerListener>();
    
    private final EnvironmentExpander _envExpander = new EnvironmentExpander();
    
    private static final Logger _logger = LogManager.getLogger(TaskManager.class);

    public static final String JAVAVM = "JAVA";
    public static final String JAVAVM32 = "JAVA32";
    
    public static final String ATTRIBUTE_NAME = TaskManager.class.getName();
}