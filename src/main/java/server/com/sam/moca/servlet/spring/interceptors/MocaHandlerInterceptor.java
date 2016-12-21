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

package com.sam.moca.servlet.spring.interceptors;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.sam.moca.DatabaseTool;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.advice.ForwardingDatabaseTool;
import com.sam.moca.advice.ForwardingMocaContext;
import com.sam.moca.advice.ForwardingServerContext;
import com.sam.moca.server.InstanceUrl;
import com.sam.moca.server.ServerContextFactory;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.exec.LocalSessionContext;
import com.sam.moca.server.exec.RequestContext;
import com.sam.moca.server.exec.ServerContext;
import com.sam.moca.server.exec.SessionContext;
import com.sam.moca.server.exec.SessionType;
import com.sam.moca.server.log.MocaTraceMessaging;
import com.sam.moca.server.log.TraceUtils;
import com.sam.moca.server.session.MocaSessionManager;
import com.sam.moca.server.session.SessionData;
import com.sam.moca.server.session.SessionToken;
import com.sam.moca.util.MocaUtils;
import com.sam.moca.util.ResponseUtils;

/**
 * MOCA based handler interceptor for spring.  This sets up context 
 * requirements 
 * TODO: add if we do dynamic view lookup or not
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
@Component
public class MocaHandlerInterceptor implements HandlerInterceptor {
    
    public MocaHandlerInterceptor() {
        ServerContextFactory factory = ServerUtils.globalAttribute(
            ServerContextFactory.class);
        if (factory == null) {
            throw new IllegalStateException("There was no ServerContextFactory " +
                    "configured in global system context");
        }
        _factory = factory;
        
        MocaSessionManager auth = (MocaSessionManager) ServerUtils
                .globalAttribute(MocaSessionManager.class);
        if(auth == null){
            throw new IllegalStateException("There was no SessionManager " +
                    "configured in global system context");
        }
        _sessionManager = auth;
        
        _nodeUrl = ServerUtils.globalAttribute(InstanceUrl.class);
        
    }

    // @see org.springframework.web.servlet.HandlerInterceptor#preHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object)
    @Override
    public boolean preHandle(HttpServletRequest request,
        HttpServletResponse response, Object handler) throws Exception {
        _logger.debug("Firing preHandle HOOK");
        
        Map<String, String> env = new HashMap<String, String>();
        SessionToken token = (SessionToken)request.getAttribute("moca.AuthToken");
        String sessionId = null;
        String userId = null;
        if (token != null) {
            sessionId = token.getSessionId();
            userId = token.getUserId();
        }
        
        if (sessionId == null) {
            sessionId = "UNAUTHENTICATED";
        }
        
        if (userId == null) {
            userId = "NOUSER";
        }
        
        env.put("USR_ID", userId);
        
        RequestContext requestContext = new RequestContext(env);
        SessionContext sessionContext = new LocalSessionContext(
            sessionId, SessionType.WEBSERVICE);
        sessionContext.setSessionToken(token);

        if (token != null) {
            SessionData sd = _sessionManager.getSessionData(token.getSessionId());
            if (sd != null) {
                for (Entry<String, String> entry : sd.getEnvironment()
                    .entrySet()) {
                    sessionContext
                        .putVariable(entry.getKey(), entry.getValue());
                }
            }
        }
        
        insertRequestVariables(request, requestContext);
        
        ServerContext context = _factory.newContext(requestContext, sessionContext);
        ForwardingServerContext forwardingContext = 
                new WebServiceForwardingServerContext(context);
        ServerUtils.setCurrentContext(forwardingContext);
        
        String traceFileName = request.getHeader("moca-tracefile");
        if (traceFileName != null) {
            _logger.debug("Enabling tracing on client");
            String filename = MocaUtils.expandEnvironmentVariables(
                ServerUtils.globalContext(), "$LESDIR/log/" + traceFileName);
            
            // Don't allow clandestine directory navigation using relative directories (e.g. ../src/...)
            if (filename.contains("..")) {
                throw new IOException("Invalid request: " + filename);
            }
            
            File traceFile = new File(filename);
            boolean printLibraryVersions = !traceFile.exists();
            
            TraceUtils.enableSessionTracing(traceFile.getAbsolutePath(), 
                    true, "*");
            if (printLibraryVersions) {
                MocaTraceMessaging.logLibraryVersions(MocaUtils.currentContext());
            }
            
            _logger.debug("WS request " + request.getContextPath() + request.getPathInfo());
        }
        
        //Place node information into the response so that we know
        // which node the request went to.  (tracing)
        response.setHeader("Node", String.valueOf(_nodeUrl));
        
        return true;
    }

    /**
     * Inserts any key=value pairs from the override request header
     * into the environment in the Request Context.
     * 
     * @param request
     * @param requestContext
     */
    private void insertRequestVariables(HttpServletRequest request,
                                 RequestContext requestContext) {
        String overrides = ResponseUtils.decodeHeader(request
            .getHeader("moca-request"));

        if (overrides != null && !overrides.isEmpty()) {
            String[] entries = overrides.split(",");
            for (String entry : entries) {
                String[] pair = entry.split("=", 2);
                if (pair.length == 2) {
                    _logger.debug("Request entry: " + entry);
                    requestContext.putVariable(pair[0], pair[1]);
                }
                else {
                    _logger.info("Invalid request entry: " + entry);
                }
            }
        }
    }

    // @see org.springframework.web.servlet.HandlerInterceptor#postHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.web.servlet.ModelAndView)
    @Override
    public void postHandle(HttpServletRequest request,
        HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception {
        _logger.debug("Firing postHandle HOOK: " + modelAndView);
        ServerContext context = ServerUtils.getCurrentContext();
        context.commit();
    }

    // @see org.springframework.web.servlet.HandlerInterceptor#afterCompletion(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, java.lang.Exception)
    @Override
    public void afterCompletion(HttpServletRequest request,
        HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        _logger.debug("Firing afterCompletion HOOK");
        try {
            String traceFileName = request.getHeader("moca-tracefile");
            if (traceFileName != null) {
                TraceUtils.disableSessionTracing();
                _logger.debug("Disabled tracing on client.");
            }
            ServerContext context = ServerUtils.getCurrentContext();
            // Close the context which will also automatically roll back
            context.close();
        }
        finally {
            ServerUtils.setCurrentContext(null);
        }
    }
    
    private static class WebServiceForwardingServerContext extends ForwardingServerContext {
        /**
         * @param factory
         */
        public WebServiceForwardingServerContext(
                ServerContext context) {
            super(context);
        }
        
        // @see com.sam.moca.advice.ForwardingServerContext#getComponentContext()
        @Override
        public MocaContext getComponentContext() {
            return new WebServiceForwardingMocaContext(
                super.getComponentContext());
        }
    }
    
    private static class WebServiceForwardingMocaContext extends ForwardingMocaContext {
        /**
         * @param context
         */
        public WebServiceForwardingMocaContext(MocaContext context) {
            super(context);
        }
        
        // @see com.sam.moca.advice.ForwardingMocaContext#commit()
        @Override
        public void commit() throws MocaException {
            throw new UnsupportedOperationException("Committing while running web service is not allowed");
        }
        
        // @see com.sam.moca.advice.ForwardingMocaContext#rollback()
        @Override
        public void rollback() throws MocaException {
            throw new UnsupportedOperationException("Rolling back while running web service is not allowed");
        }
        
        // @see com.sam.moca.advice.ForwardingMocaContext#getDb()
        @Override
        public DatabaseTool getDb() {
            return new WebServiceForwardingDatabaseTool(super.getDb());
        }
    }
    
    private static class WebServiceForwardingDatabaseTool extends ForwardingDatabaseTool {

        /**
         * @param databaseTool
         */
        public WebServiceForwardingDatabaseTool(DatabaseTool databaseTool) {
            super(databaseTool);
        }
        
        // @see com.sam.moca.advice.ForwardingDatabaseTool#rollbackDB(java.lang.String)
        @Override
        public void rollbackDB(String savepoint) throws SQLException {
            throw new UnsupportedOperationException("Rolling back to savepoint while running web service is not allowed");
        }
        
        // @see com.sam.moca.advice.ForwardingDatabaseTool#setSavepoint(java.lang.String)
        @Override
        public void setSavepoint(String savepoint) throws SQLException {
            throw new UnsupportedOperationException("Setting savepoint while running web service is not allowed");
        }
    }

    private final ServerContextFactory _factory;
    private final MocaSessionManager _sessionManager;
    private final InstanceUrl _nodeUrl;
    private static final Logger _logger = LogManager.getLogger(
        MocaHandlerInterceptor.class);
}
