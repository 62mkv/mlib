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

package com.redprairie.moca.job;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import com.redprairie.mad.client.MadSettableGauge;
import com.redprairie.moca.AsynchronousExecutor;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.exceptions.UnexpectedException;
import com.redprairie.moca.job.dao.JobExecutionDAO;
import com.redprairie.moca.util.MocaUtils;

/**
 * A MOCA job that is ran using an asynchronous executor and updates the
 * job execution table.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public abstract class AbstractCommandJob implements Job, InterruptableJob {
    
    public AbstractCommandJob(AsynchronousExecutor asynchronousExecutor, 
        JobExecutionDAO jobExecDAO,
        MadSettableGauge<Integer> executionStatusProbe) {
        _asynchronousExecutor = asynchronousExecutor;
        _jobExecDAO = jobExecDAO;
        _executionStatusProbe = executionStatusProbe;
    }
    
    protected abstract Callable<Void> getJobCallable(JobDefinition job, Map<String,String> env);
    
    // @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
    @SuppressWarnings("unchecked")
    @Override
    synchronized
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        _executionThread.set(Thread.currentThread());
        
        JobDataMap jobParams = ctx.getJobDetail().getJobDataMap();

        JobDefinition job = (JobDefinition) jobParams.get("job");
        if (job == null) {
            throw new JobExecutionException("Missing job object");
        }
        
        _logger.debug(MocaUtils.concat("Running job ", job.getJobId()));
        
        Future<Void> future = null;
        try {
            future = _asynchronousExecutor.executeAsynchronously(
                getJobCallable(job, (Map<String, String>) jobParams.get("env")));
            future.get();
            _executionStatusProbe.setValue(0);
        }
        catch (InterruptedException e) {
            _logger.info("Job : " + job.getJobId() + " interrupted -- aborting");
            future.cancel(true);
        }
        catch (ExecutionException e) {
            Throwable t = e.getCause();
            if (t instanceof MocaException) {
                MocaException mocaExcp = (MocaException)t;
                _executionStatusProbe.setValue(mocaExcp.getErrorCode());
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Job " + job.getJobId() + " error result " +
                            mocaExcp.getErrorCode() + ": " + mocaExcp.getMessage(), 
                            mocaExcp);
                }
                else {
                    _logger.warn("Job " + job.getJobId() + " error result " +
                            mocaExcp.getErrorCode() + ": " + mocaExcp.getMessage());
                }
            }
            else {
                _executionStatusProbe.setValue(UnexpectedException.CODE);
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Job " + job.getJobId() + " error result " +
                            t, t);
                }
                else {
                    _logger.warn("Job " + job.getJobId() + " error result " +
                            t);
                }
            }
        }
        
        _logger.debug(MocaUtils.concat("Done running job ", job.getJobId()));
    }

    // @see org.quartz.InterruptableJob#interrupt()
    @Override
    public void interrupt() throws UnableToInterruptJobException {
        _logger.debug("Interrupting job...");
        Thread thread = _executionThread.get();
        if (thread != null) {
            thread.interrupt();
        }
    }

    protected final AsynchronousExecutor _asynchronousExecutor;
    protected final JobExecutionDAO _jobExecDAO;
    protected final AtomicReference<Thread> _executionThread = 
            new AtomicReference<Thread>();
    protected final MadSettableGauge<Integer> _executionStatusProbe;
    protected final Logger _logger = LogManager.getLogger(this.getClass());
}
