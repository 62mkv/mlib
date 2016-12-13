/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.redprairie.moca.server.exec;

import java.util.HashMap;
import java.util.Map;

import com.redprairie.moca.server.log.TraceState;
import com.redprairie.moca.server.session.SessionToken;

/**
 * A local, in-process session context implementation.  Nothing is shared
 * across processes or servers.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 * @version $Revision$
 */
public class LocalSessionContext implements SessionContext {
    public LocalSessionContext(String sessionID, SessionType sessionType) {
        this(sessionID, sessionType, new HashMap<String, String>());
    }
    
    public LocalSessionContext(String sessionID, SessionType sessionType, 
        Map<String, String> env) {
        if (env == null) {
            _sessionEnv = new HashMap<String, String>();
        }
        else {
            _sessionEnv = new HashMap<String, String>(env);
        }
        _sessionType = sessionType;
        _sessionAttrs = new HashMap<String, Object>();
        _sessionId = sessionID;
        _traceState = new TraceState(sessionID);
    }
    
    @Override
    synchronized
    public boolean isVariableMapped(String name) {
        return _sessionEnv.containsKey(name.toUpperCase());
    }
    
    @Override
    synchronized
    public String getVariable(String name) {
        return _sessionEnv.get(name.toUpperCase());
    }
    
    @Override
    synchronized
    public String putVariable(String name, String value) {
        return _sessionEnv.put(name.toUpperCase(), value);
    }
    
    @Override
    synchronized
    public void removeVariable(String name) {
        _sessionEnv.remove(name.toUpperCase());
    }
    
    @Override
    synchronized
    public Map<String, String> getAllVariables() {
        return new HashMap<String, String>(_sessionEnv);
    }
    
    @Override
    public TraceState getTraceState() {
        return _traceState;
    }
    
    @Override
    public String getSessionId() {
        return _sessionId;
    }
    
    @Override
    synchronized
    public Object getAttribute(String name) {
        return _sessionAttrs.get(name.toUpperCase());
    }
    
    @Override
    synchronized
    public void putAttribute(String name, Object value) {
        _sessionAttrs.put(name.toUpperCase(), value);
    }
    
    @Override
    synchronized
    public Object removeAttribute(String name) {
        return _sessionAttrs.remove(name.toUpperCase());
    }
    
    @Override
    synchronized
    public ServerContext getServerContext() {
        return _currentContext;
    }
    
    @Override
    synchronized
    public void saveServerContext(ServerContext ctx) {
        _currentContext = ctx;
    }
    
    @Override
    synchronized
    public ServerContext takeServerContext() {
        ServerContext ctx = _currentContext;
        _currentContext = null;
        return ctx;
    }
    
    @Override
    public SessionToken getSessionToken() {
        return _sessionToken;
    }
    
    @Override
    public void setSessionToken(SessionToken token) {
        _sessionToken = token;
    }
    
    @Override
    public String getUserId() {
        return (_sessionToken != null) ? _sessionToken.getUserId() : null;
    }
    
    // @see com.redprairie.moca.server.exec.SessionContext#getSessionType()
    @Override
    public SessionType getSessionType() {
        return _sessionType;
    }
    
    private final String _sessionId;
    private final Map<String, String> _sessionEnv;
    private final Map<String, Object> _sessionAttrs;
    private final TraceState _traceState;
    private ServerContext _currentContext;
    private SessionToken _sessionToken;
    private final SessionType _sessionType;
}
