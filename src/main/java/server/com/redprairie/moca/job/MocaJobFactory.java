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

package com.redprairie.moca.job;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.mad.client.MadGenericGauge;
import com.redprairie.mad.client.MadName;
import com.redprairie.mad.client.MadSettableGauge;
import com.redprairie.moca.AsynchronousExecutor;
import com.redprairie.moca.async.LocalAsynchronousExecutor;
import com.redprairie.moca.job.dao.JobExecutionDAO;
import com.redprairie.moca.mad.MonitoringUtils;
import com.redprairie.moca.probes.jobs.JobProbe;
import com.redprairie.moca.server.ServerContextFactory;

/**
 * Job factory that produces local command jobs based on the class type
 * allows for overlap.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class MocaJobFactory implements JobFactory {
    
    public MocaJobFactory(ServerContextFactory factory, 
        JobExecutionDAO jobExecDAO,
        MadFactory madFactory) {
        _factory = factory;
        _jobExecDAO = jobExecDAO;
        _localAsync = new LocalAsynchronousExecutor();
        _madFactory = madFactory;
    }
    
    MocaJobFactory(ServerContextFactory factory, 
        JobExecutionDAO jobExecDAO, AsynchronousExecutor async,
        MadFactory madFactory) {
        _factory = factory;
        _jobExecDAO = jobExecDAO;
        _localAsync = async;
        _madFactory = madFactory;
    }
    
    // @see org.quartz.spi.JobFactory#newJob(org.quartz.spi.TriggerFiredBundle, org.quartz.Scheduler)
    @Override
    public Job newJob(TriggerFiredBundle fired, Scheduler scheduler)
            throws SchedulerException {
        JobDetail quartzJob = fired.getJobDetail();
        Class<? extends Job> jobClass = quartzJob.getJobClass();
        
        JobDefinition job = (JobDefinition) quartzJob.getJobDataMap().get("job");
        
        if (jobClass == LocalCommandJob.class) {
            return new LocalCommandJob(_localAsync, _factory, _jobExecDAO, getJobExecutionStatusProbe(job));
        }
        else if (jobClass == LocalCommandStatefulJob.class) {
            return new LocalCommandStatefulJob(_localAsync, _factory, 
                _jobExecDAO, getJobExecutionStatusProbe(job));
        }
        else {
            try {
                return jobClass.newInstance();
            }
            catch (InstantiationException e) {
                throw new SchedulerException(e); 
            }
            catch (IllegalAccessException e) {
                throw new SchedulerException(e); 
            }
        }
    }
    
    /**
     * Gets the probe to record the execution status with
     * @param job The Job Definition
     * @return The settable execution status probe
     */
    protected MadSettableGauge<Integer> getJobExecutionStatusProbe(JobDefinition job) {
        MadName mName = _madFactory.newMadName(MonitoringUtils.MOCA_GROUP_NAME,
            JobProbe.TYPE_JOBS, JobProbe.LAST_EXECUTION_STATUS, job.getJobId());
        
        MadSettableGauge<Integer> executionStatusGauge = _madFactory.newGauge(mName, new MadGenericGauge<Integer>());
        return executionStatusGauge;
    }
    
    protected final AsynchronousExecutor _localAsync;
    protected final JobExecutionDAO _jobExecDAO;
    protected final ServerContextFactory _factory;
    protected final MadFactory _madFactory;
}
