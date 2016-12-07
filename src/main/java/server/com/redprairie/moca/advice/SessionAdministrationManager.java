/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.advice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.redprairie.moca.server.exec.SessionContext;


/**
 * This is the in memory session administration manager that houses all of
 * sessions for use in querying state and such
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class SessionAdministrationManager implements SessionAdministrationManagerBean {
    
    public SessionAdministration createSession(SessionContext sessionContext) {
        // We always create an admin since most calls this context will be
        // unique and we optimize the miss occurance rather than read.
        SessionAdministration sessionAdmin = new SessionAdministration(sessionContext);
        SessionAdministration oldAdmin = _sessionAdmin.putIfAbsent(sessionContext, sessionAdmin);
        return oldAdmin != null ? oldAdmin : sessionAdmin;
    }
    
    public void registerSessionThread(SessionAdministration session, long threadId, 
        ServerContextAdministration bean) {
        ConcurrentMap<Long, ServerContextAdministration> beans = _sessions.get(session);
        
        if (beans == null) {
            beans = new ConcurrentHashMap<Long, ServerContextAdministration>();
            ConcurrentMap<Long, ServerContextAdministration> previous = 
                _sessions.putIfAbsent(session, beans);
            if (previous != null) {
                beans = previous;
            }
        }
        
        beans.put(threadId, bean);
    }
    
    public void unregisterSessionThread(String sessionId, long threadId) {
        SessionAdministrationBean session = getSession(sessionId);
        if (session != null) {
            ConcurrentMap<Long, ServerContextAdministration> beans = _sessions.get(session);
            if (beans != null) {
                beans.remove(threadId);
            }
        }
    }
    
    public void closeSession(String sessionId) {
        SessionAdministrationBean bean = getBeanForName(sessionId);
        if (bean != null) {
            _sessions.remove(bean);
            _sessionAdmin.values().remove(bean);
        }
    }
    
    // @see com.redprairie.moca.advice.SessionAdministrationManagerBean#getSessionBeans(com.redprairie.moca.advice.SessionAdministrationBean)
    @Override
    public Map<Long, ServerContextAdministrationBean> getSessionBeans(SessionAdministrationBean session) {
        ConcurrentMap<Long, ServerContextAdministration> beans = _sessions.get(session);
        if (beans != null) {
            return new HashMap<Long, ServerContextAdministrationBean>(beans);
        }
        else {
            return null;
        }
    }

    // @see com.redprairie.moca.advice.SessionAdministrationManagerBean#getSessions()
    @Override
    public SessionAdministration[] getSessions() {
        // We can't reliably pass a sized array since the backing collection 
        // could change
        return _sessions.keySet().toArray(new SessionAdministration[_sessions.size()]);
    }
    
    // @see com.redprairie.moca.advice.SessionAdministrationManagerBean#getSession(java.lang.String)
    @Override
    public SessionAdministrationBean getSession(String sessionId) {
        return getBeanForName(sessionId);
    }
    
    private SessionAdministrationBean getBeanForName(String sessionName) {
        for (SessionAdministration admin : _sessions.keySet()) {
            if (sessionName.equals(admin._session.getSessionId())) {
                return admin;
            }
        }
        return null;
    }
    
    private final ConcurrentMap<SessionAdministration, ConcurrentMap<Long, ServerContextAdministration>> _sessions = 
        new ConcurrentHashMap<SessionAdministration, ConcurrentMap<Long,ServerContextAdministration>>();
    
    private final ConcurrentMap<SessionContext, SessionAdministration> _sessionAdmin = 
        new ConcurrentHashMap<SessionContext, SessionAdministration>();
}
