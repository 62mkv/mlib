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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.mad.annotations.ProbeGroup;
import com.redprairie.mad.annotations.ProbeType;
import com.redprairie.mad.client.MadGauge;
import com.redprairie.mad.probes.Probe;
import com.redprairie.moca.mad.MonitoringUtils;
import com.redprairie.moca.task.TaskDefinition;
import com.redprairie.moca.task.TaskManager;

/**
 * A probe for Tasks. Registers the task definition
 * as a MXBean and adds gauges for how many
 * times the task has been restarted and if it
 * is running or not.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
@ProbeGroup(MonitoringUtils.MOCA_GROUP_NAME)
@ProbeType(TaskProbe.TYPE_TASKS)
public class TaskProbe extends Probe {

    public static final String TYPE_TASKS = "Tasks";
    
    // Task Probe Names
    public static final String TASK_CONFIGURATION = "task-configuration";
    public static final String TASK_RUNNING = "running";
    public static final String TASK_RESTARTS = "number-restarts";

    /**
     * Registers a new TaskProbe. This exports the Task Definition
     * as a MXBean and additionally registers probes that track
     * if the task is running or not and how many times it has been restarted.
     * @param manager The Task Manager
     * @param task The Task's Definition
     */
    public TaskProbe(final TaskManager manager, final TaskDefinition task) {
        _taskDefinition = task;
        _taskId = task.getTaskId();
        _logger.debug("Registering probes for task " + _taskId);
        
        // Wrap the Task Definition and export MBean
        TaskDefinitionProbe.registerTask(getMadName(_taskId, TASK_CONFIGURATION), task);
        
        // Whether the task is running or not
        getFactory().newGauge(getMadName(_taskId, TASK_RUNNING), new MadGauge<Boolean>() {
            @Override
            public Boolean getValue() {
                return manager.isRunning(_taskId);
            }

        });

        // The number of times the task has been restarted
        getFactory().newCounter(getMadName(_taskId, TASK_RESTARTS));
    }
    
    /**
     * Gets the wrapped Task Definition
     * @return The Task Definition
     */
    protected TaskDefinition getTaskDefinition() {
        return _taskDefinition;
    }
    
    /**
     * Allows the underlying Task Definition to be updated
     * @param task The Task Definition
     */
    protected void setTaskDefinition(TaskDefinition task) {
        _taskDefinition = task;
        // Update the exported Task Definition
        TaskDefinitionProbe.registerTask(getMadName(_taskId, TASK_CONFIGURATION), _taskDefinition);
    }
    
    /**
     * Unregisters the task probe.
     * Unregisters the Task Definition's MXBean and
     * all metrics.
     */
    protected void unregister() {
        _logger.debug("Unregistering probes for task " + _taskId);
        TaskDefinitionProbe.unregisterTask(getMadName(_taskId, TASK_CONFIGURATION));
        getFactory().removeMetrics(getGroup(), getType(), _taskId);
    }
    
    private final String _taskId;
    private TaskDefinition _taskDefinition;
    private static final Logger _logger = LogManager.getLogger(TaskProbe.class);
}
