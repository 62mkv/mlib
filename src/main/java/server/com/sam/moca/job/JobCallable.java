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

package com.sam.moca.job;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;

import com.redprairie.mad.client.MadMetrics;
import com.redprairie.mad.client.MadNameImpl;
import com.redprairie.mad.client.MadTimer;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.exceptions.UnexpectedException;
import com.sam.moca.job.dao.JobExecutionDAO;
import com.sam.moca.mad.MonitoringUtils;
import com.sam.moca.probes.jobs.JobProbe;
import com.sam.moca.server.InstanceUrl;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.dispatch.ErrorMessageBuilder;
import com.sam.moca.server.exec.ServerContext;
import com.sam.moca.util.MocaUtils;
import com.yammer.metrics.core.Clock;

public class JobCallable implements Callable<Void>{

    /**
     * Constructor only here for jgroups, should not ever be directly called
     */
    public JobCallable() {
        _jobExecDAO = ServerUtils.globalAttribute(JobExecutionDAO.class);
    }

    public JobCallable(String jobId, String command, Map<String, String> env, 
        JobExecutionDAO jobExecDAO) {
        _jobId = jobId;
        _command = command;
        _env = env;
        _jobExecDAO = jobExecDAO;
    }
    // @see java.util.concurrent.Callable#call()
    @Override
    public Void call() throws Exception {
        MocaContext moca = MocaUtils.currentContext();
        
        for (Entry<String, String> entry : _env.entrySet()) {
            moca.putSystemVariable(entry.getKey(), entry.getValue());
        }
        JobDefinition jobDef = new JobDefinition();
        jobDef.setJobId(_jobId);
        InstanceUrl url = ServerUtils.globalAttribute(InstanceUrl.class);
        JobExecution jobExec = new JobExecution(jobDef, url);
        _jobExecDAO.save(jobExec);
        // Need to commit the job exec real quick
        moca.commit();
        // Initialize timer probes
        MadTimer successTimer = MadMetrics.getFactory().newTimer(
            new MadNameImpl(MonitoringUtils.MOCA_GROUP_NAME, JobProbe.TYPE_JOBS, JobProbe.EXECUTIONS_SUCCESSFUL, _jobId),
                TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
        
        MadTimer errorTimer = MadMetrics.getFactory().newTimer(
            new MadNameImpl(MonitoringUtils.MOCA_GROUP_NAME, JobProbe.TYPE_JOBS, JobProbe.EXECUTIONS_ERRORED, _jobId),
            TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
        
        Clock clock = Clock.defaultClock();
        long startTime = clock.getTick();
        try {
            moca.executeCommand(_command);
            successTimer.update(clock.getTick() - startTime, TimeUnit.NANOSECONDS);
            jobExec.setStatus(0);
        }
        catch (Throwable t) {
            errorTimer.update(clock.getTick() - startTime, TimeUnit.NANOSECONDS);
            
            String message;
            if (t instanceof MocaException) {
                MocaException excp = (MocaException)t;
                String mocaExceptionMessage;
                
                ServerContext ctx = ServerUtils.getCurrentContextNullable();
                if (ctx != null) {
                    if (excp.isMessageResolved()) {
                        mocaExceptionMessage = excp.getMessage();
                    }
                    else {
                        mocaExceptionMessage = new ErrorMessageBuilder(
                            excp.getErrorCode(), excp.getMessage(),
                            excp.getArgList(), ctx.getMessageResolver())
                            .getMessage();
                    }
                }
                else {
                    mocaExceptionMessage = excp.getMessage();
                }
                
                _logger.warn("Job " + _jobId + " error result " +
                        excp.getErrorCode() + ": " + mocaExceptionMessage);
                jobExec.setStatus(excp.getErrorCode());
                if (mocaExceptionMessage.length() > 2000) {
                    message = mocaExceptionMessage.substring(0, 2000);
                }
                else {
                    message = mocaExceptionMessage;
                }
            }
            else {
                _logger.error("There was an unexpected exception " +
                        "encountered with job " + _jobId, t);
                jobExec.setStatus(UnexpectedException.CODE);
                String javaExceptionMessage = t.getMessage();
                if (javaExceptionMessage.length() > 2000) {
                    message = javaExceptionMessage.substring(0, 2000);
                }
                else {
                    message = javaExceptionMessage;
                }
            }
            jobExec.setMessage(message);
            try {
                moca.rollback();
            }
            catch (MocaException e) {
                _logger.warn("Error trying to rollback job cluster " +
                        "database, update to status may be incorrect.", e);
            }
            
            // Rethrow the Throwable for the caller to handle
            if (t instanceof Exception) {
                throw (Exception)t;
            }
            else if (t instanceof Error) {
                throw (Error)t;
            }
            else {
                throw new RuntimeException(t);
            }
        }
        finally {
            try {
                jobExec.setEndDate(new Date());
                _jobExecDAO.save(jobExec);
                moca.commit();
            }
            catch (HibernateException e) {
                _logger.warn("Could not save end date.", e);
            }
            catch (MocaException e) {
                _logger.warn("Could not commit end date transaction.", e);
            }
        }

        return null;
    }
    
    /**
     * @return Returns the jobId.
     */
    String getJobId() {
        return _jobId;
    }
    /**
     * @return Returns the command.
     */
    String getCommand() {
        return _command;
    }
    
    /**
     * @return Returns the env.
     */
    Map<String, String> getEnv() {
        return new HashMap<String, String>(_env);
    }
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return "JobCallable [_jobId=" + _jobId + ", _command=" + _command + "]";
    }
    
    protected String _jobId;
    protected String _command;
    protected Map<String, String> _env;
    private JobExecutionDAO _jobExecDAO;
    
    protected static final Logger _logger = LogManager.getLogger(JobCallable.class);
}