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

package com.sam.moca.task;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.mad.client.MadCounter;
import com.redprairie.mad.client.MadMetrics;
import com.sam.moca.mad.MonitoringUtils;
import com.sam.moca.probes.tasks.TaskProbe;
import com.sam.moca.task.dao.TaskExecutionDAO;

/**
 * Maintains information about a running task. This class can (but need not) be
 * run as its own thread (it implements Runnable). When the <code>run</code>
 * method is called, the <code>runTask</code> method will be called on
 * sublasses. When the <code>runTask</code> method finshes, the task is no
 * longer running, and it is not scheduled for retry (or the running thread was
 * interrupted).
 * 
 * In order to kill the task, the thread that's in the <code>run</code> method
 * must be interrupted.
 * 
 * Copyright (c) 2016 Sam Corporation All Rights Reserved
 * @author dinksett
 */
public abstract class RunningTask implements Runnable {
    
    public RunningTask(TaskDefinition task, TaskExecution taskExec, 
            TaskExecutionDAO taskExecDao, Map<String, String> env, Integer startDelay) {
        _task = task;
        _taskExec = taskExec;
        _taskExecDao = taskExecDao;
        _environment = new LinkedHashMap<String, String>(env);
        _startDelay = (startDelay != null) ? startDelay : 0;
    }
    
    public void run() {
        Thread.currentThread().setName("Task " + _task.getTaskId());
        runTask();
    }
    
    protected abstract void runTask();
    
    synchronized
    public int getStartDelay() {
        return _startDelay;
    }
    
    //
    // Implementation
    //
    
    TaskDefinition getTaskDefinition() {
        return _task;
    }

    /**
     * Increments a counter tracking the amount
     * of times this task has been restarted
     */
    protected void incrementRestartCounter() {
        // Using newCounter just incase the counter has not 
        // yet been created. It will return the counter if it already exists.
        MadCounter restartCounter = MadMetrics.getFactory()
            .newCounter(MonitoringUtils.MOCA_GROUP_NAME, TaskProbe.TYPE_TASKS,
                        TaskProbe.TASK_RESTARTS, _task.getTaskId());
        restartCounter.inc();
    }
    
    protected final TaskDefinition _task;
    protected final TaskExecution _taskExec;
    protected final TaskExecutionDAO _taskExecDao;
    protected final Map<String, String> _environment;
    private final int _startDelay;
    protected final static Logger _logger = LogManager.getLogger(RunningTask.class);
}
