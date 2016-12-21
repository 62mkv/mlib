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

package com.sam.moca.job;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.redprairie.mad.client.MadSettableGauge;
import com.sam.moca.AsynchronousExecutor;
import com.sam.moca.MocaException;
import com.sam.moca.MocaInterruptedException;
import com.sam.moca.job.dao.JobExecutionDAO;
import com.sam.moca.server.ServerContextFactory;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.exec.LocalSessionContext;
import com.sam.moca.server.exec.RequestContext;
import com.sam.moca.server.exec.ServerContext;
import com.sam.moca.server.exec.SessionContext;
import com.sam.moca.server.exec.SessionType;
import com.sam.moca.server.log.TraceState;
import com.sam.moca.server.session.SessionToken;
import com.sam.util.StringReplacer.ReplacementStrategy;
import com.sam.util.VarStringReplacer;

/**
 * Local server based job execution.  Sets up context then just submits the
 * job for execution to the asynchronous executor.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class LocalCommandJob extends AbstractCommandJob {
    
    public LocalCommandJob(AsynchronousExecutor executor, 
        ServerContextFactory factory, JobExecutionDAO jobExecDAO,
        MadSettableGauge<Integer> executionStatusProbe) {
        super(executor, jobExecDAO, executionStatusProbe);
        _factory = factory;
    }

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
        
        RequestContext request = new RequestContext();
        final Map<String, String> environment = 
            (Map<String, String>) jobParams.get("env");
        SessionContext session = (SessionContext) jobParams.get("session");
        
        if (session == null) {
            session = new LocalSessionContext("job-" + job.getJobId(), 
                SessionType.JOB);
        }
        
        // We override the current session environment with the job one then
        for (Entry<String, String> entry : environment.entrySet()) {
            session.putVariable(entry.getKey(), entry.getValue());
        }
        
        // Do variable replacement on the various values in the job definition
        VarStringReplacer envLookup = new VarStringReplacer(new ReplacementStrategy() {
            @Override
            public String lookup(String key) {
                if (environment.containsKey(key)) {
                    return environment.get(key);
                }
                // Finally look up the system environment.
                return System.getenv(key);
            }
        });

        TraceState traceState = session.getTraceState();
        String logFile = job.getLogFile();

        // If logfile was provided then we try to use that first
        // otherwise it will go to the main MOCA output
        if (logFile != null && logFile.trim().length() > 0) {
            logFile = envLookup.translate(logFile);
            traceState.configureLogFileName(logFile);
        }

        String traceLevel = job.getTraceLevel();
        if (traceLevel != null && !traceLevel.isEmpty()) {
            traceState.setLevel(traceLevel);
        }
        
        // Authenticate our session
        session.setSessionToken(new SessionToken(job.getJobId()));

        ServerContext serverContext = _factory.newContext(request, session);
        ServerUtils.setCurrentContext(serverContext);
        try {
            traceState.applyTraceStateToThread();
            
            // Setup the context before firing
            super.execute(ctx);
            serverContext.commit();
            
            jobParams.put("session", session);
        }
        catch (MocaInterruptedException e) {
            _logger.info("Job interrupted -- aborting");
        }
        catch (MocaException e) {
            _logger.warn("There was a problem commiting local job transaction!", e);
        }
        finally {
            serverContext.close();
            // Deassociate the session then
            ServerUtils.setCurrentContext(null);
            
            if (traceState != null) {
                    traceState.closeLogging();
                }
            }
        }

    // @see com.sam.moca.job.AbstractCommandJob#getCallable()
    @Override
    protected Callable<Void> getJobCallable(JobDefinition job,
                                            Map<String, String> env) {
        return new JobCallable(job.getJobId(), job.getCommand(), env,
            _jobExecDAO);
    }
    
    private final ServerContextFactory _factory;
}
