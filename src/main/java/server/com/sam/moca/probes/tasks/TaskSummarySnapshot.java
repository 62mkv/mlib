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

package com.sam.moca.probes.tasks;

import java.util.ArrayList;
import java.util.List;

import com.sam.moca.task.TaskDefinition;
import com.sam.moca.task.TaskManager;

/**
 * Takes a snapshot of the current state of tasks
 * How many tasks are running/stopped and what are they.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TaskSummarySnapshot {
    
    /**
     * Constructs a Task Summary Snapshot
     * by looking at the tasks state through
     * the Task Manager
     * @param manager The Task Manager
     */
    public TaskSummarySnapshot(TaskManager manager) {
        List<TaskDefinition> tasks = manager.getCurrentTasks();
        _runningTasks = new ArrayList<String>();
        _stoppedTasks = new ArrayList<String>();
        for (TaskDefinition task : tasks) {
            if (manager.isRunning(task.getTaskId())) {
                _runningTasks.add(getTaskFormat(task));
            }
            else {
                _stoppedTasks.add(getTaskFormat(task));
            }
        }
    }
    
    private String getTaskFormat(TaskDefinition task) {
        return String.format("%s (%s)", task.getName(), task.getTaskId());
    }
    
    public int getRunningCount() {
        return _runningTasks.size();
    }
    
    public int getStoppedCount() {
        return _stoppedTasks.size();
    }
    
    public int getTotalCount() {
        return _runningTasks.size() + _stoppedTasks.size();
    }
    
    public List<String> getRunningTasks() {
        return _runningTasks;
    }
    
    public List<String> getStoppedTasks() {
        return _stoppedTasks;
    }

    private List<String> _runningTasks;
    private List<String> _stoppedTasks;
}
