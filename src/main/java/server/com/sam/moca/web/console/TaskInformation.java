/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.sam.moca.web.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.sam.moca.EditableResults;
import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.SimpleResults;
import com.sam.moca.cluster.Node;
import com.sam.moca.cluster.RoleDefinition;
import com.sam.moca.cluster.manager.ClusterRoleManager;
import com.sam.moca.server.InstanceUrl;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.exec.SystemContext;
import com.sam.moca.task.TaskDefinition;
import com.sam.moca.task.TaskManager;

public class TaskInformation {

    public TaskInformation() {    
        SystemContext sys = ServerUtils.globalContext();
        
        // Get the task manager from the system context. If task management
        // is disabled via the "-T" command line argument we won't get a task manager.
        _taskManager = (TaskManager) sys.getAttribute(TaskManager.ATTRIBUTE_NAME);
    }

    public MocaResults getTaskDefinitions() throws MocaException {
        EditableResults res = null;

        // Create a new result set.
        res = new SimpleResults();
        res.addColumn("task_id", MocaType.STRING);
        res.addColumn("role_id", MocaType.STRING);
        res.addColumn("name", MocaType.STRING);
        res.addColumn("cmd_line", MocaType.STRING);
        res.addColumn("run_dir", MocaType.STRING);
        res.addColumn("log_file", MocaType.STRING);
        res.addColumn("task_typ", MocaType.STRING);
        res.addColumn("grp_nam", MocaType.STRING);
        res.addColumn("restart", MocaType.BOOLEAN);
        res.addColumn("trace_level", MocaType.BOOLEAN);
        res.addColumn("auto_start", MocaType.BOOLEAN);
        res.addColumn("start_delay", MocaType.INTEGER);
        res.addColumn("running", MocaType.BOOLEAN);
        res.addColumn("nodes", MocaType.STRING);

        // If a task management is disabled the task manager will be null
        // and we just return an empty result set.
        if (_taskManager == null) 
            return res;

        MocaClusterAdministration clusterAdmin = ServerUtils.globalAttribute(MocaClusterAdministration.class);
        Map<Node, InstanceUrl> nodes = clusterAdmin.getKnownNodes();
        ClusterRoleManager manager = ServerUtils.globalAttribute(
                ClusterRoleManager.class);

        // Get the list of task definitions.
        List<TaskDefinition> allTasks = _taskManager.getAllTasks();
        // Get the Task Cache from the Task Manager.
        Map<Node, Set<TaskDefinition>> clusteredTasks = _taskManager.getClusteredTasks();

        // Iterate through the task definitions.
        for (TaskDefinition task : allTasks) {

            String taskId = task.getTaskId();
            Integer startDelay = task.getStartDelay();
                
            // Add this task to the result set.
            res.addRow();
            res.setStringValue("task_id", taskId);
            RoleDefinition role = task.getRole();
            res.setStringValue("role_id", role == null ? null : role.getRoleId());
            res.setStringValue("name", task.getName());
            res.setStringValue("cmd_line", task.getCmdLine());
            res.setStringValue("run_dir", task.getRunDirectory());
            res.setStringValue("log_file", task.getLogFile());
            res.setStringValue("task_typ", task.getType());
            res.setStringValue("grp_nam", task.getGroupName());
            res.setBooleanValue("restart", task.isRestart());
            
            boolean traceSetting = true;
            String traceValue = task.getTraceLevel();
            if(traceValue == null || traceValue.isEmpty()){
                traceSetting = false;
            }
            
            res.setBooleanValue("trace_level", traceSetting);
            res.setBooleanValue("auto_start", task.isAutoStart());
            
            // Go through the Task Cache to see if the Task is running on at least
            // one of the nodes in the cluster.
            boolean running = false;
            for (Set<TaskDefinition> tasks : clusteredTasks.values()) {
                if (tasks.contains(task)) {
                    running = true;
                }
            }
            res.setBooleanValue("running", running);

            // Get the nodes the task can run on.
            if (role == null) {
                role = new RoleDefinition();
                role.setRoleId(taskId);
            }
            
            List<InstanceUrl> urls = new ArrayList<InstanceUrl>();
            for (Node node : manager.getClusterNodes(role)) {
                urls.add(nodes.get(node));
            }
            res.setStringValue("nodes", Joiner.on(", ").join(urls));

            if (startDelay == null) {
                res.setNull("start_delay");
            }
            else {
                res.setIntValue("start_delay", startDelay);
            }
        }
       
        return res;
    }
    
    public TaskManager getTaskManager() throws MocaException {
        return _taskManager;
    }

    private final TaskManager _taskManager;
}