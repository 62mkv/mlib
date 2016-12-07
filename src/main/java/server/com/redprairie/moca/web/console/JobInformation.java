/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.web.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.cluster.manager.ClusterRoleManager;
import com.redprairie.moca.job.JobDefinition;
import com.redprairie.moca.job.JobManager;
import com.redprairie.moca.server.InstanceUrl;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.SystemContext;

public class JobInformation {

    public JobInformation() {
        SystemContext sys = ServerUtils.globalContext();
        
        // Get the job manager from the system context. If job management
        // is disabled via the "-J" command line argument we won't get a job manager.
        _jobManager = (JobManager) sys.getAttribute(JobManager.ATTRIBUTE_NAME);
    }

    public  MocaResults getJobDefinitions() throws MocaException {
        EditableResults res = null;

        // Create a new result set.
        res = new SimpleResults();
        res.addColumn("job_id", MocaType.STRING);
        res.addColumn("role_id", MocaType.STRING);
        res.addColumn("name", MocaType.STRING);
        res.addColumn("command", MocaType.STRING);
        res.addColumn("log_file", MocaType.STRING);
        res.addColumn("trace_level", MocaType.BOOLEAN);
        res.addColumn("schedule", MocaType.STRING);
        res.addColumn("timer", MocaType.INTEGER);
        res.addColumn("start_delay", MocaType.INTEGER);
        res.addColumn("overlap", MocaType.BOOLEAN);
        res.addColumn("enabled", MocaType.BOOLEAN);
        res.addColumn("type", MocaType.STRING);
        res.addColumn("grp_nam", MocaType.STRING);
        res.addColumn("scheduled", MocaType.BOOLEAN);
        res.addColumn("nodes", MocaType.STRING);

        // If a job management is disabled the job manager will be null
        // and we just return an empty result set.
        if (_jobManager == null) 
            return res;

        MocaClusterAdministration clusterAdmin = ServerUtils.globalAttribute(MocaClusterAdministration.class);
        Map<Node, InstanceUrl> nodes = clusterAdmin.getKnownNodes();
        ClusterRoleManager manager = ServerUtils.globalAttribute(
                ClusterRoleManager.class);

        // Get the list of job definitions.
        List<JobDefinition> allJobs = _jobManager.getAllJobs();
        // Get the Job Cache from the Job Manager.
        Map<Node, Set<JobDefinition>> jobMap = _jobManager.getClusteredJobs();

        // Iterate through the job definitions.
        for (JobDefinition job : allJobs) {

            String jobId = job.getJobId();
            
            // Determine the job type from the schedule.
            String jobType = (job.getSchedule() == null) ? "Timer-Based" : "Schedule Based";
            Integer startDelay = job.getStartDelay();
            Integer timer = job.getTimer();
            
            // Add this job to the result set.
            res.addRow();
            res.setStringValue("job_id", jobId);
            RoleDefinition role = job.getRole();
            res.setStringValue("role_id", role == null ? null : role.getRoleId());
            res.setStringValue("name", job.getName());
            res.setStringValue("command", job.getCommand());
            res.setStringValue("log_file", job.getLogFile());
            
            boolean traceSetting = true;
            String traceValue = job.getTraceLevel();
            if(traceValue == null || traceValue.isEmpty()){
                traceSetting = false;
            }
            
            res.setBooleanValue("trace_level", traceSetting);
            res.setStringValue("schedule", job.getSchedule());
            res.setBooleanValue("overlap", job.isOverlap());
            res.setBooleanValue("enabled", job.isEnabled());
            res.setStringValue("type", jobType);
            res.setStringValue("grp_nam", job.getGroupName());
            
            // Go through the Job Cache to see if the Job is scheduled on at least
            // one node in the cluster.
            boolean scheduled = false;
            for (Set<JobDefinition> jobSet : jobMap.values()) {
                if (jobSet.contains(job)) {
                    scheduled = true;
                }
            }
            res.setBooleanValue("scheduled", scheduled);
            
            String nodeList = ""; 
            if (nodes.size() > 0) {
                if(role == null) {
                    // If the Job has no role and we're clustered the Node
                    // is controlled by the Cluster.
                    nodeList = "Clustered";
                }
                else {
                    List<InstanceUrl> urls = new ArrayList<InstanceUrl>();
                    for (Node node : manager.getClusterNodes(role)) {
                        urls.add(nodes.get(node));
                    }
                    nodeList = Joiner.on(", ").join(urls);
                }
            }
            res.setStringValue("nodes", nodeList);
            
            if (timer == null) {
                res.setNull("timer");
            }
            else {
                res.setIntValue("timer", timer);
            }
            if (startDelay == null) {
                res.setNull("start_delay");
            }
            else {
                res.setIntValue("start_delay", startDelay);
            }
        }
        
        return res;
    }
    
    public JobManager getJobManager() throws MocaException {
        return _jobManager;
    }
    
    private final JobManager _jobManager;
}