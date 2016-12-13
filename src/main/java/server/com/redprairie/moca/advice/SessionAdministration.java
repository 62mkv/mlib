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

package com.redprairie.moca.advice;

import java.io.IOException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.SessionContext;
import com.redprairie.moca.server.exec.SessionType;
import com.redprairie.moca.server.log.TraceState;
import com.redprairie.moca.util.MocaUtils;

/**
 * This interface defines the methods available to a session bean that is 
 * exported in JMX.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class SessionAdministration implements SessionAdministrationBean {
    
    public SessionAdministration(SessionContext session) {
        _session = session;
        _startedTime = new DateTime();
    }

    // @see com.redprairie.moca.advice.ServerContextAdministrationMXBean#getConnectedIpAddress()
    @Override
    public String getConnectedIpAddress() {
        if (_clientIpAddress != null) {
            return _clientIpAddress;
        }
        else {
            return "N/A - Moca Server Application";
        }
    }
    
    // @see com.redprairie.moca.advice.ServerContextAdministrationMXBean#getCommand()
    @Override
    public String getLastCommand() {
        return _lastCommand;
    }
    
    // @see com.redprairie.moca.advice.ServerContextAdministrationMXBean#getLastScript()
    @Override
    public String getLastScript() {
        return _lastScript;
    }

    // @see com.redprairie.moca.advice.ServerContextAdministrationMXBean#getLastSqlStatement()
    @Override
    public String getLastSqlStatement() {
        return _lastSqlStatement;
    }
    
    // @see com.redprairie.moca.advice.ServerContextAdministrationMXBean#getLastCommandTime()
    @Override
    public Date getLastCommandTime() {
        if (_lastCommandTime != null) {
            return _lastCommandTime.toDate();
        }
        return null;
    }

    // @see com.redprairie.moca.advice.ServerContextAdministrationMXBean#getLastScriptTime()
    @Override
    public Date getLastScriptTime() {
        if (_lastScriptTime != null) {
            return _lastScriptTime.toDate();
        }
        return null;
    }

    // @see com.redprairie.moca.advice.ServerContextAdministrationMXBean#getLastSqlStatementTime()
    @Override
    public Date getLastSqlStatementTime() {
        if (_lastSqlStatementTime != null) {
            return _lastSqlStatementTime.toDate();
        }
        return null;
    }
    
    // @see com.redprairie.moca.advice.SessionAdministrationBean#startTrace(java.lang.String)
    @Override
    public void startTrace(String fileName, String traceLevel) throws IOException {
        TraceState state = _session.getTraceState();
        
        if (traceLevel == null || traceLevel.isEmpty()) {
            state.closeLogging();
        }
        else {
            if (fileName == null) {
                throw new NullPointerException("File Name cannot be null");
            }
            String expandedPath = MocaUtils.expandEnvironmentVariables(
                ServerUtils.globalContext(), fileName);
            
            state.configureLogFileName(expandedPath);
            state.setLevel(traceLevel);
            state.applyTraceStateToThread();
            }
            }
    
    // @see com.redprairie.moca.advice.SessionAdministrationBean#getStartedTime()
    @Override
    public Date getStartedTime() {
        return _startedTime.toDate();
    }
    
    // @see com.redprairie.moca.advice.SessionAdministrationBean#getTraceFile()
    @Override
    public String getTraceFile() {
        TraceState state = _session.getTraceState();
        
        String name = state.getLogFileName();
        if (name != null) {
            if (_session.isVariableMapped("LESDIR")) {
                name = name.replace(_session.getVariable("LESDIR"), "LESDIR");
            }
            else if (System.getenv("LESDIR") != null) {
                name = name.replace(System.getenv("LESDIR"), "LESDIR");
            }
        }
        return name;
    }
    
    // @see com.redprairie.moca.advice.SessionAdministrationBean#getSessionName()
    @Override
    public String getSessionName() {
        return _session.getSessionId();
    }
    
    // @see com.redprairie.moca.advice.SessionAdministrationBean#getSessionType()
    @Override
    public SessionType getSessionType() {
        return _session.getSessionType();
    }
    
    // @see java.lang.Object#hashCode()
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_session == null) ? 0 : _session.hashCode());
        return result;
    }

    // @see java.lang.Object#equals(java.lang.Object)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SessionAdministration other = (SessionAdministration) obj;
        if (_session == null) {
            if (other._session != null) return false;
        }
        else if (!_session.equals(other._session)) return false;
        return true;
    }

    protected String _clientIpAddress;
    protected String _lastCommand;
    protected DateTime _lastCommandTime;
    protected String _lastSqlStatement;
    protected DateTime _lastSqlStatementTime;
    protected String _lastScript;
    protected DateTime _lastScriptTime;
    
    protected final SessionContext _session;
    protected final DateTime _startedTime;
    
    protected static final Logger _logger = LogManager.getLogger(
            SessionAdministration.class);
}