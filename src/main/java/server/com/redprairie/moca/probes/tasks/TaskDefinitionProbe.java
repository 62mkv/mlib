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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.redprairie.mad.client.MadName;
import com.redprairie.mad.client.MadUtil;
import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.task.TaskDefinition;

/**
 * Used to expose Task Definitions as a MBean
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TaskDefinitionProbe implements TaskDefinitionMXBean {
 
    public static final Map<String, String> TYPE_TRANSLATOR;
    
    static {
        // Used to translate task type letter to name
        Map<String, String> temp = new HashMap<String, String>(3);
        temp.put("P", "Process");
        temp.put("T", "Thread");
        temp.put("D", "Daemon");
        TYPE_TRANSLATOR = Collections.unmodifiableMap(temp);
    }
    
    /**
     * Registers the Task Definition on the MBean server.
     * If the Task has already been registered then its definition
     * is replaced with the passed in Task Definition.
     * @param name The desired MadName
     * @param task The Task Definition
     */
    protected static void registerTask(MadName name, TaskDefinition task) {
        TaskDefinitionProbe currentProbe = allRegisteredTasks.get(task.getTaskId());
        // Update the exposed Task Definition if it already exists
        if (currentProbe != null) {
            currentProbe._task = task;
        }
        // Otherwise register the MBean
        else {
            TaskDefinitionProbe taskProbe = new TaskDefinitionProbe(task);
            allRegisteredTasks.put(task.getTaskId(), taskProbe);
            MadUtil.registerMBean(name, taskProbe);
        }
    }
    
    /**
     * Registers the Task Definition on the MBean server.
     * @param name The MadName used to register
     */
    protected static void unregisterTask(MadName name) {
        MadUtil.unregisterMBean(name);
        allRegisteredTasks.remove(name.getScope());
    }
    
    // @see com.redprairie.moca.mad.TaskDefinitionMXBean#getTaskId()
    @Override
    public String getTaskId() {
        return _task.getTaskId();
    }

    // @see com.redprairie.moca.mad.TaskDefinitionMXBean#getRole()
    @Override
    public RoleDefinition getRole() {
        return _task.getRole();
    }

    // @see com.redprairie.moca.mad.TaskDefinitionMXBean#getName()
    @Override
    public String getName() {
        return _task.getName();
    }

    // @see com.redprairie.moca.mad.TaskDefinitionMXBean#getCmdLine()
    @Override
    public String getCmdLine() {
        return _task.getCmdLine();
    }

    // @see com.redprairie.moca.mad.TaskDefinitionMXBean#getRunDirectory()
    @Override
    public String getRunDirectory() {
        return _task.getRunDirectory();
    }

    // @see com.redprairie.moca.mad.TaskDefinitionMXBean#getLogFile()
    @Override
    public String getLogFile() {
        return _task.getLogFile();
    }

    // @see com.redprairie.moca.mad.TaskDefinitionMXBean#getTraceLevel()
    @Override
    public String getTraceLevel() {
        return _task.getTraceLevel();
    }

    // @see com.redprairie.moca.mad.TaskDefinitionMXBean#isRestart()
    @Override
    public boolean isRestart() {
        return _task.isRestart();
    }

    // @see com.redprairie.moca.mad.TaskDefinitionMXBean#isAutoStart()
    @Override
    public boolean isAutoStart() {
        return _task.isAutoStart();
    }

    // @see com.redprairie.moca.mad.TaskDefinitionMXBean#getStartDelay()
    @Override
    public Integer getStartDelay() {
        return _task.getStartDelay();
    }

    // @see com.redprairie.moca.mad.TaskDefinitionMXBean#getType()
    @Override
    public String getType() {
        String type = _task.getType();
        return TYPE_TRANSLATOR.get(type);
    }
    
    // @see com.redprairie.moca.probes.tasks.TaskDefinitionMXBean#getEnvironment()
    @Override
    public Map<String, String> getEnvironment() {
        return _task.getEnvironment();
    }
    
    // Allows the Task Definition to be updated
    protected TaskDefinitionProbe(TaskDefinition task) {
        _task = task;
    }

    private TaskDefinition _task;
    private static final Map<String, TaskDefinitionProbe> allRegisteredTasks = new ConcurrentHashMap<String, TaskDefinitionProbe>();
}
