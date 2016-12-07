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

package com.redprairie.moca.mad.zabbix;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.redprairie.mad.zabbix.jmx.ZabbixDiscoveryHelper;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.task.TaskDefinition;
import com.redprairie.moca.task.TaskManager;

/**
 * The Zabbix Helper provides JMX operations intended to be exposed to Zabbix
 * for uses such as low level discovery when finding dynamically named Metrics
 * in our application.
 * 
 * Copyright (c) 2012 RedPrairie Corporation All Rights Reserved
 * 
 * @author rrupp
 */
public class MocaZabbixDiscoveryHelper extends ZabbixDiscoveryHelper {

    /**
     * Returns a Zabbix low level discovery response
     * with a list of all the TASK_ID macros found for a 
     * specific task type (specified by a regular expression).
     * @param taskTypeRegex A regular expression to match for the appropriate task type filtering.
     * @return The Zabbix low level discovery response of the found tasks with the {#TASK_ID} macro
     */
    public String discoverTaskIds(String taskTypeRegex) {
        return discoverRunningTaskIds(taskTypeRegex, null);
    }

    /**
     * Returns a Zabbix low level discovery response
     * with a list of all the TASK_ID macros found for a 
     * specific task type (specified by a regular expression).
     * Allows the response to be filtered in regards to if the task is
     * running or not.
     * @param taskTypeRegex A regular expression to match for the appropriate task type filtering.
     * @param isRunning Whether the task is running or not
     * @return The Zabbix low level discovery response of the found tasks with the {#TASK_ID} macro
     */
    public String discoverRunningTaskIds(String taskTypeRegex, Boolean isRunning) {
        Pattern pattern = Pattern.compile(taskTypeRegex);
        SystemContext sys = ServerUtils.globalContext();

        TaskManager _taskManager = (TaskManager) sys
            .getAttribute(TaskManager.ATTRIBUTE_NAME);
        List<TaskDefinition> tasks = _taskManager.getCurrentTasks();

        List<String> taskIds = new ArrayList<String>();
        // Look through the tasks and find the types that match and if they're
        // running or not.
        for (TaskDefinition task : tasks) {
            if (pattern.matcher(task.getCmdLine()).matches()
                    && (isRunning == null || _taskManager.isRunning(task
                        .getTaskId()) == isRunning)) {
                taskIds.add(task.getTaskId());
            }
        }

        return buildDiscoveryOutput(MACRO_TASK_ID, taskIds);
    }

    private static final String MACRO_TASK_ID = "TASK_ID";
}
