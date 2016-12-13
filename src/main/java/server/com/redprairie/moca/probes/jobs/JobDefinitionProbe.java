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

package com.redprairie.moca.probes.jobs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.redprairie.mad.client.MadName;
import com.redprairie.mad.client.MadUtil;
import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.job.JobDefinition;

/**
 * A probe to expose Job Definitions via JMX.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class JobDefinitionProbe implements JobDefinitionMXBean {

    /**
     * Registers the Job Definition on the MBean server.
     * If the Job has already been registered then its definition
     * is replaced with the passed in Job Definition.
     * @param name The desired MadName
     * @param job The Job Definition
     */
    protected static void registerJob(MadName name, JobDefinition job) {
        JobDefinitionProbe currentProbe = allRegisteredJobs.get(job.getJobId());
        // Update the exposed Job Definition if it already exists
        if (currentProbe != null) {
            currentProbe._job = job;
        }
        // Otherwise register the MBean
        else {
            JobDefinitionProbe jobProbe = new JobDefinitionProbe(job);
            allRegisteredJobs.put(job.getJobId(), jobProbe);
            MadUtil.registerMBean(name, jobProbe);
        }
    }
    
    /**
     * Registers the Job Definition on the MBean server.
     * @param name The MadName used to register
     */
    protected static void unregisterJob(MadName name) {
        MadUtil.unregisterMBean(name);
        allRegisteredJobs.remove(name.getScope());
    }
    
    public JobDefinitionProbe(JobDefinition job) {
        _job = job;
    }
    
    // @see com.redprairie.moca.probes.jobs.JobDefinitionMXBean#getJobId()
    @Override
    public String getJobId() {
        return _job.getJobId();
    }

    // @see com.redprairie.moca.probes.jobs.JobDefinitionMXBean#getRole()
    @Override
    public RoleDefinition getRole() {
        return _job.getRole();
    }

    // @see com.redprairie.moca.probes.jobs.JobDefinitionMXBean#getName()
    @Override
    public String getName() {
        return _job.getName();
    }

    // @see com.redprairie.moca.probes.jobs.JobDefinitionMXBean#getCommand()
    @Override
    public String getCommand() {
        return _job.getCommand();
    }

    // @see com.redprairie.moca.probes.jobs.JobDefinitionMXBean#getLogFile()
    @Override
    public String getLogFile() {
        return _job.getLogFile();
    }

    // @see com.redprairie.moca.probes.jobs.JobDefinitionMXBean#getTraceLevel()
    @Override
    public String getTraceLevel() {
        return _job.getTraceLevel();
    }

    // @see com.redprairie.moca.probes.jobs.JobDefinitionMXBean#getSchedule()
    @Override
    public String getSchedule() {
        return _job.getSchedule();
    }

    // @see com.redprairie.moca.probes.jobs.JobDefinitionMXBean#getTimer()
    @Override
    public Integer getTimer() {
        return _job.getTimer();
    }

    // @see com.redprairie.moca.probes.jobs.JobDefinitionMXBean#getStartDelay()
    @Override
    public Integer getStartDelay() {
        return _job.getStartDelay();
    }

    // @see com.redprairie.moca.probes.jobs.JobDefinitionMXBean#isOverlap()
    @Override
    public boolean isOverlap() {
        return _job.isOverlap();
    }

    // @see com.redprairie.moca.probes.jobs.JobDefinitionMXBean#isEnabled()
    @Override
    public boolean isEnabled() {
        return _job.isEnabled();
    }

    // @see com.redprairie.moca.probes.jobs.JobDefinitionMXBean#getType()
    @Override
    public String getType() {
        return _job.getType();
    }

    // @see com.redprairie.moca.probes.jobs.JobDefinitionMXBean#getEnvironment()
    @Override
    public Map<String, String> getEnvironment() {
        return _job.getEnvironment();
    }

    private JobDefinition _job;
    private static Map<String, JobDefinitionProbe> allRegisteredJobs
            = new ConcurrentHashMap<String, JobDefinitionProbe>();
}
