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

package com.sam.moca.probes.jobs;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;

import com.redprairie.mad.annotations.ProbeGroup;
import com.redprairie.mad.annotations.ProbeType;
import com.redprairie.mad.client.MadFactory;
import com.redprairie.mad.client.MadGauge;
import com.redprairie.mad.probes.Probe;
import com.sam.moca.job.JobDefinition;
import com.sam.moca.job.JobException;
import com.sam.moca.job.JobManager;
import com.sam.moca.mad.MonitoringUtils;

/**
 * A probe that exposes Job related statistics and readings.
 * These include: <br>
 * 1) The job's configuration <br>
 * 2) Timings around successful executions of the job <br>
 * 3) Timings around errored executions of the job <br>
 * 4) Whether the job is scheduled or not <br>
 * 5) The job's last execution date <br>
 * 6) The elapsed time since the job ran <br>
 * 7) The job's last execution status <br>
 * 8) The job's next execution date <br>
 * 9) A countdown to the next execution <br.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
@ProbeGroup(MonitoringUtils.MOCA_GROUP_NAME)
@ProbeType(JobProbe.TYPE_JOBS)
public class JobProbe extends Probe {
    
    // MAD Metric Type
    public static final String TYPE_JOBS = "Jobs";
    
    // Metrics/MBean names
    public static final String JOB_CONFIGURATION = "job-configuration";
    public static final String EXECUTIONS_SUCCESSFUL = "executions-successful";
    public static final String EXECUTIONS_ERRORED = "executions-errored";
    public static final String IS_SCHEDULED = "is-scheduled";
    public static final String NEXT_EXECUTION_DATE = "next-execution-date";
    public static final String NEXT_EXECUTION_DATE_COUNTDOWN = "next-execution-date-countdown";
    public static final String LAST_EXECUTION_DATE = "last-execution-date";
    public static final String LAST_EXECUTION_DATE_ELAPSED = "last-execution-date-elapsed";
    public static final String LAST_EXECUTION_STATUS = "last-execution-status";

    public JobProbe(final JobManager manager, final JobDefinition job) {
        _job = job;
        MadFactory mFact = getFactory();
        
        // Registering configuration MXBean
        registerJobConfiguration(_job);
        
        // A gauge showing whether the job is scheduled or not...
        mFact.newGauge(getMadName(_job.getJobId(), IS_SCHEDULED), new MadGauge<Boolean>(){

            @Override
            public Boolean getValue() {
                try {
                    return manager.isScheduled(_job.getJobId());
                }
                catch (JobException e) {
                    return false;
                }
            }
        });
    }
    
    // When a job gets scheduled then the relevant metrics will be added
    protected void notifyJobScheduled(final JobManager manager) {
        MadFactory mFact = getFactory();
        
        // A gauge showing the next execution date
        mFact.newGauge(getMadName(_job.getJobId(), NEXT_EXECUTION_DATE), new MadGauge<Date>(){

            @Override
            public Date getValue() {
                try {
                    return manager.getNextExecutionDate(_job.getJobId());
                }
                catch (SchedulerException e) {
                    return null;
                }
            }
        });
        
        // A gauge showing the amount of time until the next execution
        mFact.newGauge(getMadName(_job.getJobId(), NEXT_EXECUTION_DATE_COUNTDOWN), new MadGauge<String>() {

            @Override
            public String getValue() {
                try {
                    return manager.getNextExecutionDateCountdown(_job.getJobId());
                }
                catch (SchedulerException e) {
                    return null;
                }
            }
            
        });   
        
        // A gauge showing the last execution date
        mFact.newGauge(getMadName(_job.getJobId(), LAST_EXECUTION_DATE), new MadGauge<Date>(){

            @Override
            public Date getValue() {
                try {
                    return manager.getLastExecutionDate(_job.getJobId());
                }
                catch (SchedulerException e) {
                    return null;
                }
            }
        });
        
        // A gauge showing the elapsed time since the last execution of the job
        mFact.newGauge(getMadName(_job.getJobId(), LAST_EXECUTION_DATE_ELAPSED), new MadGauge<String>() {

            @Override
            public String getValue() {
                try {
                    return manager.getLastExecutionDateElapsed(_job.getJobId());
                }
                catch (SchedulerException e) {
                    return null;
                }
            }
        });

        // Initialize separate timers to be used for successful and errored executions
        mFact.newTimer(getMadName(_job.getJobId(), EXECUTIONS_SUCCESSFUL),
            TimeUnit.MILLISECONDS, TimeUnit.HOURS);
        
        mFact.newTimer(getMadName(_job.getJobId(), EXECUTIONS_ERRORED),
            TimeUnit.MILLISECONDS, TimeUnit.HOURS);
    }
    
    protected void unregister() {
        _logger.debug("Unregistering probes for job " + _job.getJobId());
        JobDefinitionProbe.unregisterJob(getMadName(_job.getJobId(), JOB_CONFIGURATION));
        getFactory().removeMetrics(getGroup(), getType(), _job.getJobId());
    }
    
    protected void setJobDefinition(JobDefinition job) {
        _job = job;
        // Re-export the Job Configuration
        registerJobConfiguration(_job);
    }
    
    private void registerJobConfiguration(JobDefinition job) {
        JobDefinitionProbe.registerJob(getMadName(job.getJobId(), JOB_CONFIGURATION), job);
    }
    
    private JobDefinition _job;
    private static final Logger _logger = LogManager.getLogger(JobProbe.class);
}
