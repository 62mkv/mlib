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

package com.redprairie.moca.job.cluster;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jgroups.util.Streamable;

import com.redprairie.moca.job.JobCallable;
import com.redprairie.moca.job.dao.JobExecutionDAO;
import com.redprairie.moca.server.log.TraceState;
import com.redprairie.moca.server.log.TraceUtils;
import com.redprairie.util.StringReplacer.ReplacementStrategy;
import com.redprairie.util.VarStringReplacer;

/**
 * An extension of JobCallable for a clustered setup.
 * 
 * Copyright (c) 2012 RedPrairie Corporation All Rights Reserved
 * 
 * @author klehrke
 */
public class ClusterJobCallable extends JobCallable implements Streamable {

    public ClusterJobCallable(String jobId, String logFile, String traceLevel,
            String command, Map<String, String> env, JobExecutionDAO jobExecDAO) {
        super(jobId, command, env, jobExecDAO);
        _logFile = logFile;
        _traceLevel = traceLevel;
    }

    /**
     * Constructor only here for jgroups, should not ever be directly called
     */
    public ClusterJobCallable() {
        super();
    }

    @Override
    public Void call() throws Exception {
        TraceState traceState = null;
        try {
            if (_logFile != null) {
                traceState = TraceUtils.getTraceState();
                if (_traceLevel != null) {
                    traceState.setLevel(_traceLevel);
                }
                String logFile = translate(_logFile);
                traceState.configureLogFileName(logFile);
                
                // TODO: need to figure this out
//                Appender appender = new DailyRollingFileAppender(
//                    new MocaLayout(LoggingConfigurator.LOG_PATTERN), logFile,
//                    "'.'yyyy-MM-dd");
//                traceState.setAppender(appender);
                traceState.applyTraceStateToThread();
            }
            super.call();
        }
        finally {
            if (traceState != null) {
                traceState.closeLogging();
            }
        }

        return null;
    }
    
    /***
     * Translate a strings environment variables to the
     * expanded version.  This is a helper method so 
     * we can expand the log file.  This is done here
     * so we get it from the node it's going to run on.
     * This is because we use the JVM environment vars as
     * well.
     * 
     * @param s
     * @return expanded string
     */
    private String translate(String s){
        VarStringReplacer envLookup = new VarStringReplacer(
            new ReplacementStrategy() {
                @Override
                public String lookup(String key){
                    if(_env.containsKey(key)){
                        return _env.get(key);
                    }
                    
                    return System.getenv(key);
                }
            });
        return envLookup.translate(s);
    }

    // @see org.jgroups.util.Streamable#readFrom(java.io.DataInput)
    @Override
    public void readFrom(DataInput in) throws Exception {
        _jobId = in.readUTF();
        _command = in.readUTF();
        int size = in.readInt();
        _env = new HashMap<String, String>(size);
        for (int i = 0; i < size; ++i) {
            _env.put(in.readUTF(), in.readUTF());
        }
        if (in.readBoolean()) {
            _logFile = in.readUTF();
        }
        if (in.readBoolean()) {
            _traceLevel = in.readUTF();
        }
    }

    // @see org.jgroups.util.Streamable#writeTo(java.io.DataOutput)
    @Override
    public void writeTo(DataOutput out) throws Exception {
        out.writeUTF(_jobId);
        out.writeUTF(_command);
        out.writeInt(_env.size());
        
        for (Entry<String, String> entry : _env.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeUTF(entry.getValue());
        }
        boolean writeLogFile = _logFile != null;
        boolean writeTraceLevel = _traceLevel != null;
        out.writeBoolean(writeLogFile);
        if (writeLogFile) {
            out.writeUTF(_logFile);
        }
        out.writeBoolean(writeTraceLevel);
        if (writeTraceLevel) {
            out.writeUTF(_traceLevel);
        }
    }
    
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return "ClusterJobCallable [_logFile=" + _logFile + ", _traceLevel="
                + _traceLevel + ", _jobId=" + _jobId + ", _command=" + _command
                + ", _env=" + _env + "]";
    }

    private String _logFile;
    private String _traceLevel;
    
}
