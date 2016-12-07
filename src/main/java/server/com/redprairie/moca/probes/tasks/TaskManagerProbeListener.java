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

package com.redprairie.moca.probes.tasks;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.task.AbstractTaskManagerListener;
import com.redprairie.moca.task.TaskDefinition;
import com.redprairie.moca.task.TaskManager;

/**
 * A Task Manager listener that will register
 * and unregister task related probes when they
 * are added or removed on the Task Manager.
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TaskManagerProbeListener extends AbstractTaskManagerListener {

    /**
     * Registers task summary probes on startup
     * @see com.redprairie.moca.task.TaskManagerListener#onStart(com.redprairie.moca.task.TaskManager)
     */
    @Override
    public void onStart(TaskManager manager) {
        TaskSummaryProbe.registerTaskSummary(manager);
    }

    /**
     * Removes all task related probes on stop
     * @see com.redprairie.moca.task.TaskManagerListener#onStop(com.redprairie.moca.task.TaskManager)
     */
    @Override
    public void onStop(TaskManager manager) {
        _logger.debug("Removing all task related probes");
        TaskSummaryProbe.unregisterTaskSummary();
        for (TaskProbe taskProbe : _taskProbes.values()) {
            taskProbe.unregister();
        }
        
        _taskProbes.clear();
    }
    
    /**
     * Handles when the Task Manager is restarted, updates the Task Definition
     * references accordingly.
     * @see com.redprairie.moca.task.TaskManagerListener#onRestart(com.redprairie.moca.task.TaskManager)
     */
    @Override
    public void onRestart(TaskManager manager) {
        // We need to update the references on the Task Definitions
        // as while the Tasks may be fundamentally equal the underlying
        // objects have changed after a restart.
        List<TaskDefinition> currentTasks = manager.getCurrentTasks();
        for (TaskDefinition task : currentTasks) {
            TaskProbe probe = _taskProbes.get(task.getTaskId());
            if (probe != null) {
                probe.setTaskDefinition(task);
            }
        }
    }

    /**
     * Registers task related probes when the task is added.
     * @see com.redprairie.moca.task.TaskManagerListener#onTaskAdded(com.redprairie.moca.task.TaskDefinition)
     */
    @Override
    public void onTaskAdded(TaskManager manager, TaskDefinition task) {
        addTask(manager, task);
    }
    
    /**
     * Updates task related probes when a task is changed.
     * @see com.redprairie.moca.task.TaskManagerListener#onTaskChanged(com.redprairie.moca.task.TaskManager, com.redprairie.moca.task.TaskDefinition, com.redprairie.moca.task.TaskDefinition)
     */
    @Override
    public void onTaskChanged(TaskManager manager, TaskDefinition oldTask,
                              TaskDefinition newTask) {
        removeTask(oldTask);
        addTask(manager, newTask);
    }

    /**
     * Removes task related probes when a task is removed.
     * @see com.redprairie.moca.task.TaskManagerListener#onTaskRemoved(com.redprairie.moca.task.TaskDefinition)
     */
    @Override
    public void onTaskRemoved(TaskManager manager, TaskDefinition task) {
        removeTask(task);
    }
    
    /**
     * @return Returns the currently registered Task Probes
     */
    protected Map<String, TaskProbe> getTaskProbes() {
        return _taskProbes;
    }
    
    private void addTask(TaskManager manager, TaskDefinition task) {
        TaskProbe probes = new TaskProbe(manager, task);
        _taskProbes.put(task.getTaskId(), probes);
    }
    
    private void removeTask(TaskDefinition task) {
        TaskProbe probes = _taskProbes.remove(task.getTaskId());
        probes.unregister();
    }
    
    private final Map<String, TaskProbe> _taskProbes = new ConcurrentHashMap<String, TaskProbe>();
    private static final Logger _logger = LogManager.getLogger(TaskManagerProbeListener.class);
}
