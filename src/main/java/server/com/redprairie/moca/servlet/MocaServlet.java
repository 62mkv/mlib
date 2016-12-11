/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2006
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

package com.redprairie.moca.servlet;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.redprairie.moca.cache.infinispan.extension.CommandInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.JDBCException;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jgroups.JChannel;
import org.jgroups.blocks.locking.LockService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.redprairie.mad.client.MadHistogramWithContext;
import com.redprairie.mad.client.MadMetrics;
import com.redprairie.mad.client.MadTimerContext;
import com.redprairie.mad.client.MadTimerWithContext;
import com.redprairie.moca.AsynchronousExecutor;
import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaConstants;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaServerHook;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.advice.ServerContextConfig;
import com.redprairie.moca.advice.SessionAdministrationManager;
import com.redprairie.moca.advice.SessionAdministrationManagerBean;
import com.redprairie.moca.async.JGroupsAsynchronousExecutor;
import com.redprairie.moca.async.MocaAsynchronousExecutor;
import com.redprairie.moca.async.MocaExecutionRunner;
import com.redprairie.moca.async.MocaExecutionRunnerController;
import com.redprairie.moca.cache.CacheManager;
import com.redprairie.moca.cache.infinispan.InfinispanCacheProvider;
import com.redprairie.moca.client.ConnectionUtils;
import com.redprairie.moca.client.NormalizedContextDecoder;
import com.redprairie.moca.client.ProtocolException;
import com.redprairie.moca.client.ResponseEncoder;
import com.redprairie.moca.client.ResponseEncoderFactory;
import com.redprairie.moca.client.XMLRequestDecoder;
import com.redprairie.moca.cluster.MocaClusterMembershipListener;
import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.cluster.RoleConfig;
import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.cluster.dao.RoleDefinitionDAO;
import com.redprairie.moca.cluster.infinispan.GlobalListener;
import com.redprairie.moca.cluster.infinispan.InfinispanNode;
import com.redprairie.moca.cluster.jgroups.JGroupsChannelFactory;
import com.redprairie.moca.cluster.jgroups.JGroupsLockManager;
import com.redprairie.moca.cluster.jgroups.MocaExecutionService;
import com.redprairie.moca.cluster.manager.ClusterRoleManager;
import com.redprairie.moca.cluster.manager.DynamicClusterRoleManager;
import com.redprairie.moca.cluster.manager.ManualClusterRoleManager;
import com.redprairie.moca.cluster.manager.NonClusteredRoleManager;
import com.redprairie.moca.cluster.manager.PreferredClusterRoleManager;
import com.redprairie.moca.exceptions.SessionClosedException;
import com.redprairie.moca.exceptions.UnexpectedException;
import com.redprairie.moca.job.JobConfig;
import com.redprairie.moca.job.JobException;
import com.redprairie.moca.job.JobManager;
import com.redprairie.moca.job.cluster.ClusterJobConfig;
import com.redprairie.moca.job.dao.JobExecutionDAO;
import com.redprairie.moca.mad.MonitoringUtils;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SpringTools;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.db.DBType;
import com.redprairie.moca.server.dispatch.CommandDispatcher;
import com.redprairie.moca.server.dispatch.DispatchResult;
import com.redprairie.moca.server.dispatch.RequestDispatcher;
import com.redprairie.moca.server.exec.RequestContext;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SessionContext;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.log.LoggingConfigurator;
import com.redprairie.moca.server.log.TraceState;
import com.redprairie.moca.server.session.MocaSessionManager;
import com.redprairie.moca.server.session.MocaSessionUtils;
import com.redprairie.moca.servlet.WebSessionManager.ClosedSessionCallback;
import com.redprairie.moca.task.TaskConfig;
import com.redprairie.moca.task.TaskManager;
import com.redprairie.moca.task.cluster.ClusterTaskConfig;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.moca.web.console.MocaClusterAdministration;

/**
 * Implementation of the MOCA Web Protocol handler servlet.
 * 
 * <b><pre>
 * Copyright (c) 2006 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MocaServlet extends HttpServlet {
    
    /** Creates a new MOCA server servlet.
     * @param systemContext
     */
    public MocaServlet(SystemContext systemContext, 
                       ServerContextFactory factory,
                       MocaSessionManager authManager) throws SystemConfigurationException {
        _logger.debug("Starting Service Servlet container");
        _systemContext = systemContext;
        _serverContextFactory = factory;
        _authManager = authManager;
        _dispatcher = new RequestDispatcher(_serverContextFactory);
        _sessionManager = initializeSessionManager(_systemContext);
    }
    
    /**
     * Public no-args constructor for use with "normal" servlet containers.
     */
    public MocaServlet() {
    }

    private void logParameterInfo(String method, HttpServletRequest request, HttpServletResponse response) {
    	response.setHeader("Access-Control-Allow-Origin", "*");
    	_logger.debug("Print Header Information:\n");
    	Enumeration<String> em = request.getHeaderNames();
    	while(em.hasMoreElements()) {
    		String name = em.nextElement();
    		_logger.debug(method + " Request head name:" + name);
    		_logger.debug(method + " Request head value:" + request.getHeader(name));
    	}
    	_logger.debug("Print Parameter Information:\n");
    	em = request.getParameterNames();
    	while(em.hasMoreElements()) {
    		String name = em.nextElement();
    		_logger.debug(method + " Request param name:" + name);
    		_logger.debug(method + " Request param value:" + request.getParameter(name));
    	}
    	
    	_logger.debug("\nPrint response Header Information:");
    	Collection<String> em2 = response.getHeaderNames();
    	Iterator<String> it = em2.iterator();
    	while(it.hasNext()) {
    		String name = it.next();
    		_logger.debug(method + " Response head name:" + name);
    		_logger.debug(method + " Response head value:" + response.getHeader(name));
    	}
    }
    // @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	logParameterInfo("doGet", request, response);
        handleFormRequest(request, response);
    }
   
    // @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	logParameterInfo("doPost", request, response);
        if (request.getHeader("content-type").startsWith("application/moca-xml")) {
            handleXMLRequest(request, response);
        }
        else {
            handleFormRequest(request, response);
        }
    }
    
    private void handleFormRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        _logger.debug("Processing incoming request");
        boolean autoCommit = true;
        
        String sessionId = request.getParameter("SessionId");
        _logger.debug(MocaUtils.concat("Validating session id: ", sessionId));
        
        String environment = request.getParameter("Environment");
        Map<String, String> envMap = ConnectionUtils.parseEnvironmentString(environment);
        _logger.debug(MocaUtils.concat("Command Environment: ", envMap));
        String contextEnv = envMap.remove("__CONTEXT__");
        
        // It's possible that a legacy server might send its flattened context as a string.  We need to decode that
        // string and push those values into the dispatcher.
        Collection<MocaArgument> context = null;
        Collection<MocaArgument> args = null;
        if (contextEnv != null) {
            try {
                NormalizedContextDecoder decoder = new NormalizedContextDecoder(
                    contextEnv);
                decoder.decode();
                args = decoder.getArgs();
                context = decoder.getContext();
            }
            catch (ProtocolException e) {
                throw new ServletException("Unable to read context", e);
            }
        }

        String autoCommitStr = request.getParameter("AutoCommit");
        if (autoCommitStr != null) {
            autoCommit = autoCommitStr.equals("1");
        }
        
        boolean remoteMode = false;
        String remoteStr = request.getParameter("Remote");
        if (remoteStr != null) {
            remoteMode = remoteStr.equals("1");
        }
        String query = request.getParameter("Query");
        request.getParameter("Close");

        // Gather the session cookie from the request
        Cookie sessionCookie = getSessionCookie(request);
        _logger.debug("sessionId:" + sessionId);
        _logger.debug("environment:" + environment);
        _logger.debug("autoCommitStr:" + autoCommitStr);
        _logger.debug("remoteStr:" + remoteStr);
        _logger.debug("query:" + query);
        _logger.debug("Close:" + request.getParameter("Close"));
        // Run the request through the request dispatcher.
        dispatchAndRespond(request, response, autoCommit, remoteMode, sessionId,
                           sessionCookie, envMap, context, args, query);
            
        _logger.debug("Processed incoming request");
     }

    private void handleXMLRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        _logger.debug("Processing incoming request");
        
        XMLRequestDecoder xmlRequest;
        try {
            xmlRequest = new XMLRequestDecoder(request.getInputStream());
            xmlRequest.decode();
        }
        catch (ProtocolException e) {
            throw new ServletException("Unable to process request", e);
        }
        
        boolean autoCommit = xmlRequest.isAutoCommit();
        boolean remoteMode = xmlRequest.isRemote();
        String sessionId = xmlRequest.getSessionId();
        
        _logger.debug(MocaUtils.concat("Validating session: ", sessionId));
        String reqSessionId = request.getParameter("msession");
        if (sessionId != null && !sessionId.equals(reqSessionId)) {
            _logger.warn("session IDs do not match");
            throw new ServletException("mismatched session -- request: " + reqSessionId + ", protocol: " + sessionId);
        }
        
        Map<String, String> envMap = xmlRequest.getEnv();
        List<MocaArgument> context = xmlRequest.getContext();
        List<MocaArgument> args = xmlRequest.getArgs();

        String query = xmlRequest.getQuery();

        // Gather the session cookie from the request
        Cookie sessionCookie = getSessionCookie(request);
        
        dispatchAndRespond(request, response, autoCommit, remoteMode, sessionId,
                           sessionCookie, envMap, context, args, query);
            
        _logger.debug("Processed incoming request");
     }

    /**
     * @param request
     * @param response
     * @param autoCommit
     * @param sessionId
     * @param envMap
     * @param query
     * @param closeStr
     * @param session
     * @throws ServletException
     * @throws IOException
     */
    private void dispatchAndRespond(HttpServletRequest request,
                                    HttpServletResponse response,
                                    boolean autoCommit, boolean remoteMode,
                                    String sessionId,
                                    Cookie sessionCookie,
                                    Map<String, String> envMap,
                                    Collection<MocaArgument> context,
                                    Collection<MocaArgument> args, String query)
           throws ServletException, IOException {
        SessionContext session = null;
        boolean sessionActive = false;
        // This value tells whether to mark the session as idle (false) or to
        // close (true) it when finished.
        boolean forceClose = false;
        
        // If a session ID was passed in, then try to find it.
        if (sessionId != null && sessionId.trim().length() > 0) {
            // First try to get the session from the session manager.
            try {
                session = _sessionManager.getIdleSession(sessionId);
            }
            catch (SessionRunningException e) {
                session = e.getSessionContext();
                // If we found an active session, then make sure we don't
                // allow another command execution.
                sessionActive = true;
            }
        }
        
        // If we couldn't find a session at all, then we have to generate
        // a new key.
        if (session == null) {
            try {
                session = _sessionManager.generateNewSessionContext(envMap);
            }
            catch (InterruptedException e) {
                throw new ServletException("Interrupted while generating a " +
                		"new session", e);
            }
            sessionId = session.getSessionId();
        }
        
        DispatchResult errorResult = null;
        if (sessionCookie != null && !sessionId.equals(sessionCookie.getValue())) {
            errorResult = _dispatcher.resolveException(new SessionClosedException());
            query = null;
        }
        
        MocaSessionUtils.validateSession(session, envMap, _authManager);
        
        int status = 0;
        String message = null;
        MocaResults res = null;
        
        try {
            // Send the query.
            if (query != null) {
                // Only if the session is not active do we execute.
                if (!sessionActive) {
                    envMap.put(MocaConstants.WEB_CLIENT_ADDR, request.getRemoteAddr());
                    envMap.put("WEB_SESSIONID", sessionId);
                    RequestContext mocaRequest = new RequestContext(envMap);
                    
                    _logger.debug("Dispatching command");
                    _activeRequests.put(sessionId, Thread.currentThread());
                    MadTimerContext timerCtx = _requestTimer.time(query);
                    try {
                
                        // Call the MOCA command.  Any errors are handled by the dispatcher
                        DispatchResult dispatchResult = _dispatcher.executeCommand(query, context, args, mocaRequest, session, autoCommit, remoteMode);
                        
                        // At this time, the command is done executing and we can 
                        // return the results to the caller.
                        status = dispatchResult.getStatus();
                        message = dispatchResult.getMessage();
                        res = dispatchResult.getResults();
                        
                        // Record the amount of rows returned in the histogram probe
                        // for commands that actually return rows.
                        if (res != null && res.getRowCount() > 0) {
                            _requestResultSizes.update(res.getRowCount(), query); 
                        }
                    }
                    finally {
                        timerCtx.stop();
                        _activeRequests.remove(sessionId);
                        // We want to now mark the session as idle, since our
                        // query is done.
                        forceClose = false;
                    }
                }
                // If we see another query (not a close) and the session is already
                // active then we must error telling the caller that they can't do this.
                else {
                    message = "Attempt to execute a second query on an already running session: " + sessionId;
                    status = UnexpectedException.CODE;
                    _logger.error(message);
                    // TODO: confirm this change works
                    // We also want to write a message to the appender if there
                    // is one for the session that this request was trying to
                    // use
                    TraceState trace = session.getTraceState();
                    trace.applyTraceStateToThread();
                    try {
                        _logger.error(message);
                    }
                    finally {
                        TraceState.clearTraceStateFromThread();
                }
            }
            }
            else {
                // If no query then we want to close the session.
                forceClose = true;
                if (errorResult != null) {
                    res = errorResult.getResults();
                    message = errorResult.getMessage();
                    status = errorResult.getStatus();
                }
            }
        }
        finally {
            ServerContext serverContext = session.getServerContext();

            // Four things will cause us to retain this session.
            // 1. An open transaction
            // 2. A keepalive setting (from C code)
            // 3. An open session trace
            boolean setCookie;
            if (!forceClose) {
                // If trace is enabled or the server has keepalive or a 
                // transaction we have to mark it as idle
                // && takes precedence over ||
                if (session.getTraceState().isEnabled() ||
                        serverContext != null
                        && (serverContext.hasKeepalive() 
                                || serverContext.hasTransaction())) {
                    _sessionManager.registerIdle(session);
                    setCookie = true;
                }
                else {
                    _sessionManager.closeSession(session);
                    setCookie = false;
                }
            }
            else {
                _sessionManager.closeSession(session);
                setCookie = false;
            }

            // Handle three specific scenarios: 
            // 1. No cookie came in, and a new cookie value must be sent out.
            // 2. A cookie came in, but a new cookie value must be sent out.
            // 3. A cookie came in, but it should be deleted.
            if (setCookie) {
                if (sessionCookie == null) {
                    sessionCookie = new Cookie(_cookieName, sessionId);
                    response.addCookie(sessionCookie);
                }
                else if (!sessionCookie.getValue().equals(sessionId)) {
                    sessionCookie.setValue(sessionId);
                    response.addCookie(sessionCookie);
                }
                
                // If we are setting a cookie that means the session is now idle
                // so we just unassociate our session thread
                SessionAdministrationManager manager = 
                    (SessionAdministrationManager) ServerUtils.globalContext().getAttribute(
                        SessionAdministrationManagerBean.class.getName());
                manager.unregisterSessionThread(session.getSessionId(), 
                    Thread.currentThread().getId());
            }
            else {
                if (sessionCookie != null) {
                    sessionCookie.setValue("");
                    sessionCookie.setMaxAge(0);
                    response.addCookie(sessionCookie);
                }
                sessionId = "";
            }

            // Send the response back.
            _logger.debug("Sending response");
            
            boolean useCompression = _useCompression && isCompressable(res);
            ResponseEncoder responseEncoder = ResponseEncoderFactory.getResponseEncoder(request, response, sessionId, useCompression);
            responseEncoder.writeResponse(res, message, status);
            _logger.debug("Sent response");
        }
    }
    
    // Let's not do compression for empty results, or for results with binary columns
    private boolean isCompressable(MocaResults res) {
        if (res == null) return false;
        
        int columns = res.getColumnCount();
        int rows = res.getRowCount();
        
        if (columns == 0 || rows == 0) return false;
        for (int i = 0; i < columns; i++) {
            if (res.getColumnType(i) == MocaType.BINARY) return false;
        }
        
        return true;
    }
    
    private static WebSessionManager initializeSessionManager(SystemContext context) {
        // Since we know that the sleep is normally done in nanoseconds we only
        // want to get the values as an integer so we prevent overflow 
        // possibilities, when converting an int to a long and seconds to nano.
        int timeout = Integer.parseInt(context.getConfigurationElement(
                MocaRegistry.REGKEY_SERVER_SESSION_IDLE_TIMEOUT,
                MocaRegistry.REGKEY_SERVER_SESSION_IDLE_TIMEOUT_DEFAULT));
        
        int sessionMax = Integer.parseInt(context.getConfigurationElement(
                MocaRegistry.REGKEY_SERVER_SESSION_MAX,
                MocaRegistry.REGKEY_SERVER_SESSION_MAX_DEFAULT));
        
        WebSessionManager mgr = new WebSessionManager(timeout, 
            TimeUnit.SECONDS, sessionMax, _sessionCloser);
            
        context.putAttribute(WebSessionManager.class.getName(), mgr);  
        
        return mgr;
    }
    
    // @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        if (_systemContext == null) {
            // If the system context has not been set up, we're being loaded via servlet initialization.
            Reader regReader = null;
            InputStream regStream = getClass().getResourceAsStream(config.getInitParameter("registry"));
            log("Config = " + config.getInitParameter("registry"));
            log("Stream = " + regStream);
            
            if (regStream != null) try {
                regReader = new InputStreamReader(regStream, "UTF-8");
            }
            catch (UnsupportedEncodingException e1) {
                throw new ServletException("Unable to set up system context: " + e1, e1);
            }
            
            try {
                _systemContext = ServerUtils.globalContext(regReader);
                LoggingConfigurator.configure();
                _serverContextFactory = ServerUtils.setupServletContext(_systemContext);
                _dispatcher = new RequestDispatcher(_serverContextFactory);
                _sessionManager = initializeSessionManager(_systemContext);
            }
            catch (SystemConfigurationException e) {
                throw new ServletException("Unable to set up system context: " + e, e);
            }
            finally {
                try {
                    if (regReader != null) {
                        regReader.close();
                    }
                }
                catch (IOException e) {
                    throw new ServletException(
                            "Unable to set up system context: " + e, e);
                }
            }
        }
        
        _useCompression = Boolean.parseBoolean(_systemContext.getConfigurationElement(MocaRegistry.REGKEY_SERVER_COMPRESSION));
        
        String clusterName = _systemContext.getConfigurationElement(MocaRegistry.REGKEY_CLUSTER_NAME);
        try {
            if (clusterName == null || clusterName.isEmpty())
                _cookieName = "msession";
            else
                _cookieName = URLEncoder.encode("msession-" + clusterName, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new ServletException("unexpected encoding error: " + e, e);
        }
        
        Map<String, Object> namedSingletons = new HashMap<String, Object>();
        namedSingletons.put("systemContext", _systemContext);
        namedSingletons.put("serverContextFactory", _serverContextFactory);
        namedSingletons.put("madFactory", MadMetrics.getFactory());
        ApplicationContext parent = 
                SpringTools.getContextForPreinstantiatedSingletons(
                    namedSingletons);
        
        int asyncThread = Integer.parseInt(_systemContext.getConfigurationElement(
                MocaRegistry.REGKEY_SERVER_MAX_ASYNC_THREAD, 
                MocaRegistry.REGKEY_SERVER_MAX_ASYNC_THREAD_DEFAULT));
        
        AsynchronousExecutor executor = new MocaAsynchronousExecutor(
                _serverContextFactory, asyncThread);
        
        _systemContext.putAttribute(AsynchronousExecutor.class.getName(), 
                executor);
        
        _admin = new MocaServerAdministration();
        
        // Add an attribute to the system context.
        _systemContext.putAttribute(MocaServerAdministration.ATTRIBUTE_NAME, _admin);
        
        _admin.addRestartCallback(new MocaServerHook() {
            
            @Override
            public void onRestart(boolean clean) {
                
                try {
                    _serverContextFactory.restart(clean);
                }
                catch (Exception e) {
                    _logger.error("There was a problem restarting the " +
                            "server context factory!", e);
                }

                
                // Clear all the caches in memory as well controlled by MOCA
                CacheManager.clearCaches();

                if (_taskManager != null) {
                    try {
                        _taskManager.restart();
                    }
                    catch (Exception e) {
                        _logger.error("There was a problem restarting the " +
                                        "task manager!", e);
                    }
                }
                
                if (_jobManager != null) {
                    try {
                        _jobManager.restart();
                    }
                    catch (Exception e) {
                        _logger.error("Unable to restart was a problem restarting the " +
                                        "job manager!", e);
                    }
                }
            }
            
        });
        
        // Now we need to find all the hooks.xml files so we can add any custom
        // restart hooks.
        File[] files = _systemContext.getDataFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (name.equalsIgnoreCase("hooks.xml")) {
                    return true;
                }
                return false;
            }
            
        }, true);
        
        ApplicationContext context = null;
        // If there were any hooks.xml files then we need to search them
        if (files.length > 0) {
            String[] fileStrings = new String[files.length];
            
            // Spring xml files need to be in reverse
            for (int i = 0; i < files.length; ++i) {
                fileStrings[i] = files[i].toURI().toString();
            }
            context = new FileSystemXmlApplicationContext(
                    fileStrings);
            
            // Now get all the beans defined as hooks from the map
            Collection<MocaServerHook> hooks = (Collection<MocaServerHook>) context
                    .getBeansOfType(MocaServerHook.class).values();
            
            for (MocaServerHook hook : hooks) {
                _admin.addRestartCallback(hook);
            }
        }
        
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        
        try {
            ObjectName adminMbean = new ObjectName(ADMIN_BEAN_NAME);
            server.registerMBean(_admin, 
                adminMbean);
        }
        catch (InstanceAlreadyExistsException e) {
            _logger.warn(MocaUtils.concat("Attempt to ", 
                    "register bean ", _admin, 
                    " failed, because it was already registered."), e);
        }
        catch (MBeanRegistrationException e) {
            _logger.warn(MocaUtils.concat("Attempt to ", 
                    "register bean ", _admin, 
                    " failed, because it threw an exception."), e);
        }
        catch (NotCompliantMBeanException e) {
            _logger.warn(MocaUtils.concat("Attempt to ", 
                    "register bean ", _admin, 
                    " failed, because it wasn't compliant."), e);
        }
        catch (MalformedObjectNameException e) {
            _logger.warn(MocaUtils.concat("Attempt to ",
                    "register bean ", _admin, 
                    " failed, because the name was malformed."), e);
        }

        
        // We want to backup the old context
        ServerContext oldCtx = ServerUtils.getCurrentContextNullable();
        
        ServerContext runContext = _serverContextFactory.newContext(null, null);
        // DAO's look this up by thread.
        ServerUtils.setCurrentContext(runContext);
        try {

            // We only load job and tasks if we have a database type
            if (runContext.getDbType() != DBType.NONE) {
                // Get the Cache Manager and Server's Local Node.
                EmbeddedCacheManager cacheManager = 
                        InfinispanCacheProvider.getInfinispanCacheManager(_systemContext);
                Node localNode = new InfinispanNode(cacheManager.getAddress());
                
                // Add the Cache Manager and the Local Node to the application context
                Map<String, Object> singletonMap = new HashMap<String, Object>();
                singletonMap.put("cacheManager", cacheManager);
                singletonMap.put("localNode", localNode);
                ApplicationContext newParent = SpringTools.getContextWithParent(parent, singletonMap);
                
                // Set up tasks and jobs
                ApplicationContext appCtx = SpringTools.getContextWithParent(newParent, 
                    clusterName == null? TaskConfig.class : ClusterTaskConfig.class,
                    clusterName == null ? JobConfig.class
                            : ClusterJobConfig.class, RoleConfig.class);
                
                _taskManager = appCtx.getBean(TaskManager.class);
                
                // We set the task manager on the admin if we have one
                // this way we can remotely control the tasks as well.
                _admin.setTaskManager(_taskManager);
                
                // Add an attribute to the system context for the task manager.
                _systemContext.putAttribute(TaskManager.ATTRIBUTE_NAME, _taskManager);
                
                _jobManager = appCtx.getBean(JobManager.class);
                
                // Add an attribute to system context for the job manager.
                _systemContext.putAttribute(JobManager.ATTRIBUTE_NAME, _jobManager);
                
                JobExecutionDAO jobExecDAO = appCtx.getBean(JobExecutionDAO.class);
                
                // Add an attribute to system context for the job execution DAO.
                _systemContext.putAttribute(JobExecutionDAO.class.getName(), jobExecDAO);
                
                if (clusterName != null) {
                    
                    String manualRoles = _systemContext.getConfigurationElement(
                        MocaRegistry.REGKEY_CLUSTER_ROLES);
                    
                    Collection<RoleDefinition> actualManualRoles = 
                        new LinkedHashSet<RoleDefinition>();
                    RoleDefinitionDAO nodeDao = appCtx.getBean(RoleDefinitionDAO.class);
                    
                    if (manualRoles != null) {
                        String[] manualRoleArray = manualRoles.split(",");
                        
                        for (String manualRole : manualRoleArray) {
                            RoleDefinition def = nodeDao.read(manualRole);
                            if (def != null) {
                                actualManualRoles.add(def);
                            }
                        }
                    }
                    
                    String excludeRoles = _systemContext.getConfigurationElement(
                        MocaRegistry.REGKEY_CLUSTER_EXCLUDE_ROLES);
                    
                    Collection<RoleDefinition> actualExcludeRoles = 
                        new LinkedHashSet<RoleDefinition>();
                    
                    if (excludeRoles != null) {
                        String[] excludeRoleArray = excludeRoles.split(",");
                        
                        for (String excludeRole : excludeRoleArray) {
                            RoleDefinition def = nodeDao.read(excludeRole);
                            if (def != null) {
                                actualExcludeRoles.add(def);
                            }
                        }
                    }
                    
                    try {
                        JChannel channel = JGroupsChannelFactory.getChannelForConfig(_systemContext);
                        
                        LockService lockService = new LockService(channel);
                        JGroupsLockManager lockManager = new JGroupsLockManager(
                            lockService); 
                        
                        _systemContext.putAttribute(JChannel.class.getName(), 
                            channel);
                        
                        // TODO: the global listener is never notified of
                        // initial cluster grouping
                        GlobalListener receiver = new GlobalListener();
                        
                        cacheManager.addListener(receiver);
                        
                        MocaClusterAdministration admin = MocaClusterAdministration.newInstance(
                            _systemContext, cacheManager, localNode,
                            _jobManager, _taskManager, lockService);
                        
                        receiver.addMembershipListener(admin);
                        
                        _systemContext.putAttribute(
                            MocaClusterAdministration.class.getName(), admin);
                        // this has to be after MocaClusterAdministration is put in the context
                        CommandInitializer.allowRPC();
                        
                        MocaExecutionService execService = new MocaExecutionService(channel);
                        
                        JGroupsAsynchronousExecutor asyncExec = 
                                new JGroupsAsynchronousExecutor(execService, 
                                    Integer.valueOf(_systemContext.getConfigurationElement(
                                        MocaRegistry.REGKEY_CLUSTER_ASYNC_SUBMIT_CAP, 
                                        MocaRegistry.REGKEY_CLUSTER_ASYNC_SUBMIT_CAP_DEFAULT)));
                        
                        _systemContext.putAttribute("cluster-" + 
                                AsynchronousExecutor.class.getName(), asyncExec);
                        
                        
                        ClusterRoleManager clusterManager;
                        
                        String manager = _systemContext.getConfigurationElement(
                            MocaRegistry.REGKEY_CLUSTER_MANAGER, 
                            MocaRegistry.REGKEY_CLUSTER_MANAGER_DEFAULT);
                        
                        int managerRefreshRate = Integer.parseInt(_systemContext.getConfigurationElement(
                            MocaRegistry.REGKEY_CLUSTER_CHECK_RATE, 
                            MocaRegistry.REGKEY_CLUSTER_CHECK_RATE_DEFAULT));
                        
                        if (manager.equalsIgnoreCase("preferred")) {
                            clusterManager = new PreferredClusterRoleManager(
                                lockManager, managerRefreshRate, TimeUnit.SECONDS, 
                                actualManualRoles, actualExcludeRoles, nodeDao, 
                                cacheManager, _taskManager, _jobManager);
                        }
                        else if (manager.equalsIgnoreCase("fixed")) {
                            clusterManager = new ManualClusterRoleManager(
                                lockManager, managerRefreshRate, TimeUnit.SECONDS, 
                                actualManualRoles, actualExcludeRoles, nodeDao, 
                                cacheManager, _taskManager, _jobManager);
                        }
                        else if (manager.equalsIgnoreCase("dynamic")) {
                            clusterManager = new DynamicClusterRoleManager(
                                lockManager, managerRefreshRate, TimeUnit.SECONDS, 
                                actualManualRoles, actualExcludeRoles, nodeDao, 
                                cacheManager, _taskManager, _jobManager);
                        }
                        else {
                            throw new IllegalArgumentException("Unkown manager type [" + manager + "]");
                        }
                        
                        receiver.addMembershipListener(clusterManager);
                        
                        // Task and Job manager need to know about nodes leaving
                        // to update their cache that tracks what's running
                        receiver.addMembershipListener(_taskManager);
                        receiver.addMembershipListener(_jobManager);
                        
                        channel.connect(clusterName);
                        
                        // The start must be before the getState and after
                        // the connect.  We use the name of the cluster as
                        // the state id, so the clusterManager needs to
                        // be able to pick it up
                        clusterManager.start(localNode);
                        
                        _systemContext.putAttribute(ClusterRoleManager.class.getName(), 
                            clusterManager);
                        
                        MocaExecutionRunner runner = new MocaExecutionRunner(
                            channel, _serverContextFactory);
                        
                        int count = Integer.valueOf(
                            _systemContext.getConfigurationElement(
                                MocaRegistry.REGKEY_CLUSTER_ASYNC_RUNNERS, 
                                MocaRegistry.REGKEY_CLUSTER_ASYNC_RUNNERS_DEFAULT));
                        
                        MocaExecutionRunnerController controller = 
                                new MocaExecutionRunnerController(runner);
                        
                        for (int i = 0; i < count; ++i) {
                            controller.addRunner();
                        }
                        
                        _systemContext.putAttribute(MocaExecutionRunnerController.class.getName(), 
                            controller);
                        
                        if (context != null) {
                            Collection<MocaClusterMembershipListener> members = context
                                .getBeansOfType(MocaClusterMembershipListener.class).values();
                            
                            for (MocaClusterMembershipListener member : members) {
                                receiver.addMembershipListener(member);
                            }
                        }
                    }
                    catch (Exception e) {
                        _logger.warn("There was a problem setting up clustering configuration", e);
                    }
                }
                else {
                    _taskManager.noCluster();
                    _jobManager.noCluster();
                    
                    MocaClusterAdministration admin = MocaClusterAdministration.newInstance(
                        _systemContext, cacheManager, null,
                        _jobManager, _taskManager, null);
                    
                    _systemContext.putAttribute(
                        MocaClusterAdministration.class.getName(), admin);
                    // this has to be after MocaClusterAdministration is put in the context
                    CommandInitializer.allowRPC();

                    _systemContext.putAttribute(ClusterRoleManager.class.getName(),
                        new NonClusteredRoleManager());
                }
                
                try {
                    _taskManager.start();
                }
                catch (JDBCException e) {
                    _logger.error("Unable to start task manager: ", e);
                }
                
                try {
                    _jobManager.start();
                }
                catch (JDBCException e) {
                    _logger.error("Unable to start job manager: ", e);
                }
            }
            else {
                _logger.info("Task and job managers not started since no " +
                		"database is supplied");
            }
        }
        catch (MocaException e) {
            _logger.error("Unable to start job or task Manager: " + e, e);
            throw new ServletException("Unable to start up server", e);
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
        finally {
            runContext.close();
            if (oldCtx != null) {
                ServerUtils.setCurrentContext(oldCtx);
            }
            else {
                ServerUtils.removeCurrentContext();
            }
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        if (_taskManager != null) {
            _taskManager.stop();
        }
        
        
        if (_jobManager != null) {
            try {
                _jobManager.stop();
            }
            catch (JobException e) {
                _logger.warn("Unable to stop the job manager: " + e);
            }
        }
        
        try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(ADMIN_BEAN_NAME));
        }
        catch (Exception e) {
            log("Error unregistering bean: " + e, e);
        }
        
        if (_sessionManager != null) {
            _sessionManager.close();
        }
    }
    
    private static final class ServletSessionCallback implements
            ClosedSessionCallback {
        @Override
        public void onSessionClose(SessionContext sessionContext) {
            String sessionId = sessionContext.getSessionId();
            _logger.debug(MocaUtils.concat("Closing session ", sessionId));
            Thread activeThread = _activeRequests.remove(sessionId);
            if (activeThread != null) {
                _logger.debug(MocaUtils.concat("Interrupting active thread",
                    activeThread.getId()));
                activeThread.interrupt();
            }

            // Make sure the dispatcher cleans up anything on it's side as well.
            RequestDispatcher.cleanupSession(sessionContext);

            ServerContextConfig.unregisterSession(sessionId);
            
            // We want to shut down tracing just in case if it was still on.
            sessionContext.getTraceState().closeLogging();
        }
    }
    
    private Cookie getSessionCookie(HttpServletRequest request) {
        Cookie[] allCookies = request.getCookies();
        if (allCookies == null) return null;
        
        for (Cookie c: allCookies) {
            if (c.getName().equals(_cookieName)) {
                return c;
            }
        }
        return null;
    }
    private static final Map<String, Thread> _activeRequests = 
        new ConcurrentHashMap<String, Thread>();
    
    private transient final MadTimerWithContext _requestTimer = MadMetrics.getFactory().newTimerWithContext(
        MonitoringUtils.MOCA_GROUP_NAME, "Command-Servlet", "all-requests-timer", TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
    
    private transient final MadHistogramWithContext _requestResultSizes = MadMetrics.getFactory().newHistogramWithContext(
        MonitoringUtils.MOCA_GROUP_NAME, "Command-Servlet", "result-set-sizes");
    
    private transient WebSessionManager _sessionManager;
    private transient MocaSessionManager _authManager;
    private transient SystemContext _systemContext;
    private transient ServerContextFactory _serverContextFactory;
    private transient CommandDispatcher _dispatcher;
    private transient TaskManager _taskManager;
    private transient JobManager _jobManager;
    private transient MocaServerAdministration _admin;
    private String _cookieName = "msession";
    private boolean _useCompression = false;
    private static final Logger _logger = LogManager.getLogger(MocaServlet.class);
    private static final long serialVersionUID = 2960471890437828965L;
    private static final String ADMIN_BEAN_NAME = "com.redprairie.moca:type=server";
    private static final ClosedSessionCallback _sessionCloser = new ServletSessionCallback();
}
