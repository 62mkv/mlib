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

package com.redprairie.moca.job;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.JobFactory;

import com.redprairie.moca.util.MocaUtils;
import com.redprairie.util.EnvironmentExpander;

/**
 * Manage the scheduling of jobs.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author mlange
 */
public class JobScheduler {
 
    public JobScheduler(JobFactory jobFactory, Map<String, String> environment) {       
        _environment = new HashMap<String, String>(environment);

        // Get a Scheduler from the Scheduler factory.  The Scheduler's 
        // configuration will be determined through normal Quartz configuration
        // means.
        try {
            System.setProperty(StdSchedulerFactory.PROP_SCHED_SKIP_UPDATE_CHECK, "true");
            StdSchedulerFactory factory = new StdSchedulerFactory();
            factory.initialize();
            _scheduler = factory.getScheduler();
            _scheduler.setJobFactory(jobFactory);
        }
        catch (SchedulerException e) {
            throw new JobRuntimeException("Unable to create job scheduler" + e);
        }
        }
        
    synchronized
    void start() {            
        try {
            _scheduler.start();
        }
        catch (SchedulerException e) {
            throw new JobRuntimeException("Unable to start job scheduler: " + e);
        }
    }
    
    synchronized
    void stop() {   
        try {   
            _scheduler.standby();
            // TODO: do we want to delete these?  Problem is that if we don't
            // quartz will actually cache the scheduler since it wasn't shutdown
            // so if another person uses it, it will have the job information
            // already present just paused.
            Set<JobKey> keys = _scheduler.getJobKeys(GroupMatcher.jobGroupEquals(MOCA_JOB_GROUP));
            for (JobKey key : keys) {
                _scheduler.interrupt(key);
                _scheduler.deleteJob(key);
            }
        }
        catch (SchedulerException e) {
            throw new JobRuntimeException("Unable to stop job scheduler: ", e);
        }
    }
    
    /**
     * @param job
     * @throws JobException
     * @throws JobRuntimeException This is thrown if a job has an issue being
     *         scheduled.
     * @throws ParseException The job schedule was unable to be parsed
     */
    synchronized
    void add(JobDefinition job) throws JobException, JobRuntimeException, ParseException {
        add(job, 0);   
    }
    
    /**
     * @param job
     * @param quarantineSecs
     * @throws JobException
     * @throws JobRuntimeException This is thrown if a job has an issue being
     *         scheduled.
     * @throws ParseException The job schedule was unable to be parsed
     */
    synchronized
    void add(JobDefinition job, int quarantineSecs) throws JobException, 
            JobRuntimeException, ParseException {
        if (job == null) {
            throw new IllegalArgumentException("Job argument is null");
        }
        
        try {
            // Don't bother if the job is already scheduled.
            if (exists(job)) {  
                return;
            }
            
            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .withIdentity(job.getJobId(), MOCA_JOB_GROUP);
            
            // Create either a cron-based or timer-based trigger.
            if (job.getSchedule() != null) {
                final String schedule = job.getSchedule();
                try {
                    triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(
                        schedule));
                }
                catch (RuntimeException e) {
                    //Quartz no longer throws parse exception
                    if (e.getMessage().toLowerCase().contains("cronexpression")) throw new ParseException(schedule,0);
                    else throw e;
                }
            }
            else if (job.getTimer() != null) {
                int interval = job.getTimer();
                Integer startDelay = job.getStartDelay();
                
                Date startTime;
                if (startDelay == null) {
                    startTime = new Date();
                }
                else {
                    startTime = new Date(System.currentTimeMillis() + (startDelay * 1000L));
                }
                
                triggerBuilder.startAt(startTime)
                    .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(interval));
            }
            else {
                throw new JobException("Neither a timer nor schedule exists for job " + job.getJobId());
            }
            
            // New jobs being added or changed at runtime will have their quarantined flag set.  
            // When we see a job that is quarantined we can override any start time that was set above
            // for the start delay because it's only necessary for jobs that are being started when
            // the server initially starts up. 
            if (quarantineSecs > 0) {
                _logger.info(MocaUtils.concat("Quarantining job ", job.getJobId(), " for ", quarantineSecs, " seconds..."));           
                Date startTime = new Date(System.currentTimeMillis() + (quarantineSecs * 1000L));
                triggerBuilder.startAt(startTime);
            }

            // Quartz uses two interfaces to determine whether a job can
            // execute multiple instances.  In general, we don't want that,
            // but sometimes a stateless job is what was intended.
            Class<? extends LocalCommandJob> jobClass =
                job.isOverlap() ? LocalCommandJob.class : LocalCommandStatefulJob.class;
  
            // Get the environment variables for this job.  We can't expand the
            // values until we have all of them in case one environment variable
            // value references another.
            Map<String, String> environment = new LinkedHashMap<String, String>(_environment);
            environment.putAll(job.getEnvironment());
            
            // Expand the environment variable values.
            _envExpander.expand(environment);
            
            // Create the job data map.
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("job", job);
            jobDataMap.put("env", environment);
            
            // Create the job detail with our job information stored in it.
            //
            //   Note: Quartz suggests you use serializable objects for their
            //         JobDataMap values, but since we know we have only an in-
            //         memory job data store, it's safe to use a POJO.
            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(job.getJobId(), MOCA_JOB_GROUP)
                .usingJobData(jobDataMap).build();

            Trigger trigger = triggerBuilder.build();
            // Add and schedule the job with Quartz.
            _scheduler.scheduleJob(jobDetail, trigger);
        }
        catch (SchedulerException e) {
            throw new JobRuntimeException("Unable to schedule job " + job.getJobId() + ": " + e);
        }
    }
    
    synchronized
    void remove(JobDefinition job) throws JobException {
        if (job == null) {
            throw new IllegalArgumentException("Job argument is null");
        }
        
        try {
            // Don't bother if the job is not already scheduled.
            if (!exists(job)) {     
                return;
            }
            
            // Delete the job from Quartz.
            _scheduler.interrupt(JobKey.jobKey(job.getJobId(), MOCA_JOB_GROUP));
            _scheduler.deleteJob(JobKey.jobKey(job.getJobId(), MOCA_JOB_GROUP));
        }
        catch (SchedulerException e) {
            throw new JobRuntimeException("Unable to unschedule job " + job.getJobId() + ": " + e);
        }
    }
    
    /**
     * Determine if a job is currently scheduled.
     * @param jobId The job identifier that is case sensitive. 
     * @throws JobException 
     * @throws SchedulerException 
     * @throws IllegalArgumentException This is thrown if the job isn't  present.
     */
    synchronized
    boolean exists(JobDefinition job) throws JobException {
        String jobId = job.getJobId();
        try {
            TriggerState state = _scheduler.getTriggerState(TriggerKey.triggerKey(
                jobId, MOCA_JOB_GROUP));
            
            switch (state) {
                case NORMAL:
                case BLOCKED:
                case COMPLETE:
                case ERROR:
                case PAUSED:
                    return true;
            default:
                return false;
            }
        }
        catch (SchedulerException e) {
            throw new JobRuntimeException("Unable to determine if " + jobId + " exists: " + e);
        }
    }
    
    /**
     * Gets the next scheduled execution date of a job
     * @param jobId The Job ID
     * @return The next scheduled execution date or null if the job is not scheduled
     * @throws SchedulerException
     */
    Date getNextExecutionDate(String jobId) throws SchedulerException {
        Trigger trigger = _scheduler.getTrigger(TriggerKey.triggerKey(
            jobId, MOCA_JOB_GROUP));
        
        return trigger != null ? trigger.getNextFireTime() : null;
    }
    
    /**
     * Gets the last execution date of a job
     * @param jobId The Job ID
     * @return The last execution date or null if the job is not scheduled
     * @throws SchedulerException
     */
    Date getLastExecutionDate(String jobId) throws SchedulerException {
        Trigger trigger = _scheduler.getTrigger(TriggerKey.triggerKey(
            jobId, MOCA_JOB_GROUP));
        
        return trigger != null ? trigger.getPreviousFireTime() : null;
    }
   
    private final Map<String, String> _environment;
    private final Scheduler _scheduler;
    private final EnvironmentExpander _envExpander = new EnvironmentExpander();
    
    private static final String MOCA_JOB_GROUP = "com.redprairie.moca.jobs";
    private static final Logger _logger = LogManager.getLogger(JobManager.class);
}