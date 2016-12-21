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

package com.sam.moca.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.transaction.TransactionManager;

import org.apache.logging.log4j.LogManager;
import org.jgroups.JChannel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.redprairie.mad.ProbeManager;
import com.redprairie.mad.client.MadUtil;
import com.sam.moca.AsynchronousExecutor;
import com.sam.moca.MocaRegistry;
import com.sam.moca.MocaRuntimeException;
import com.sam.moca.MocaType;
import com.sam.moca.MocaValue;
import com.sam.moca.advice.ForwardingServerContextFactory;
import com.sam.moca.advice.SessionAdministrationManager;
import com.sam.moca.advice.SessionAdministrationManagerBean;
import com.sam.moca.async.JGroupsAsynchronousExecutor;
import com.sam.moca.cluster.jgroups.JGroupsChannelFactory;
import com.sam.moca.cluster.jgroups.MocaExecutionService;
import com.sam.moca.components.security.AuthService;
import com.sam.moca.exceptions.UnexpectedException;
import com.sam.moca.mad.MocaMadServerConfiguration;
import com.sam.moca.mad.MonitoringInformation;
import com.sam.moca.mad.MonitoringUtils;
import com.sam.moca.server.exec.DefaultServerContextFactory;
import com.sam.moca.server.exec.LocalSessionContext;
import com.sam.moca.server.exec.RequestContext;
import com.sam.moca.server.exec.ResultMapper;
import com.sam.moca.server.exec.ServerContext;
import com.sam.moca.server.exec.SessionContext;
import com.sam.moca.server.exec.SessionType;
import com.sam.moca.server.exec.SystemContext;
import com.sam.moca.server.legacy.GenericPointer;
import com.sam.moca.server.legacy.NativeProcessPoolBuilder;
import com.sam.moca.server.log.GlobalTraceState;
import com.sam.moca.server.log.LoggingConfigurator;
import com.sam.moca.server.log.TraceState;
import com.sam.moca.server.log.eventfactory.MocaLogEventFactory;
import com.sam.moca.server.registry.RegistryReader;
import com.sam.moca.server.session.ClassicMocaSessionManager;
import com.sam.moca.server.session.InfinispanMocaSessionManager;
import com.sam.moca.server.session.MocaSessionManager;
import com.sam.moca.server.session.SessionToken;
import com.sam.moca.util.MocaUtils;

/**
 * Provides utility methods for server interaction
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class ServerUtils {
    
    /**
     * Returns the current server context for the running thread.
     * This is similar to {@link ServerUtils#getCurrentContextNullable()} 
     * except that this method will try to create a server context if one
     * is not currently associated.  If the server context was not configured
     * through either {@link ServerUtils#setupDaemonContext(String, boolean)}
     * or {@link ServerUtils#setupServletContext(SystemContext)}
     * @return The current server context
     * @throws IllegalStateException This is thrown if the server context was
     *         not setup before calling this.
     */
    synchronized
    public static ServerContext getCurrentContext() 
            throws IllegalStateException {
        ServerContext serverContext = _currentContext.get();
        
        // If the server context isn't set then we want to check the factory
        // to see if it exists, because it could be passed down from our parent
        // thread, whereas the context is only for our thread
        if (serverContext == null) {
            ServerContextFactory factory = _currentServerContextFactory.get();
            RequestContext requestCtx = _currentRequestContext.get();
            SessionContext sessionCtx = _currentSessionContext.get();
            
            if (factory != null) {
                serverContext = factory.newContext(requestCtx, sessionCtx);
            }
            else {
                throw new IllegalStateException("There is no context setup " +
                		"before trying to retrieve the current " + 
                		"context.  You must setup a daemon or servlet " +
                		"context first! @see ServerUtils.setupDaemonContext");
            }
            
            // Since the current thread isn't registered we have to register it
            // being linked to the server context
            registerCurrentThread(serverContext);
            _currentContext.set(serverContext);
        }
        
        return serverContext;
    }
    
    /**
     * Returns the current context to this thread if available.  This will return
     * null if there is not one currently associated.
     * This is similar to {@link ServerUtils#getCurrentContext()} except
     * that this method will not try to create a server context if one
     * is not currently associated and will instead just return null.
     * @return The server context assigned to this thread or null if none.
     */
    public static ServerContext getCurrentContextNullable() {
        return _currentContext.get();
    }
    
    /**
     * Establishes a current server context for the running thread. In
     * situations where a MOCA context spans threads (e.g. RMI server pools,
     * thread pools, etc.), it is important to re-establish a server context for
     * the new thread. It is important to never share the server context among
     * running threads (e.g. the thread pool example), but to make sure that
     * only one thread at a time can have access to this object.
     * 
     * @param serverContext The context for the current thread. If this 
     *        parameter is <code>null</code>, that effectively removes the 
     *        context from the current thread.
     */
    public static void setCurrentContext(
            ServerContext serverContext) {
        
        if (serverContext != null) {
            _currentContext.set(serverContext);
            // Bootstrap the new trace state if present
            SessionContext ctx = serverContext.getSession();
            if (ctx != null) {
                TraceState traceState = ctx.getTraceState();
                
                if (traceState != null) {
                    MocaLogEventFactory._localTraceState.set(traceState);
                }
            }
        
            registerCurrentThread(serverContext);
        }
        else {
            removeCurrentContext();

            // We need to remove the server context factory, since it could
            // be a different request context for the next call
            _currentServerContextFactory.remove();
            _currentRequestContext.remove();
            _currentSessionContext.remove();
            TraceState.clearTraceStateFromThread();
            MocaLogEventFactory._localTraceState.remove();
        }
    }
    
    /**
     * This method is designed only for test purposes.  This will return all
     * the values currently associated with this thread and unassociate them
     * at the same time.
     */
    static CurrentValues takeCurrentValues() {
        CurrentValues values = new CurrentValues(_currentContext.get(), 
            _currentServerContextFactory.get(), _currentRequestContext.get(),
            _currentSessionContext.get(), MocaLogEventFactory._localTraceState.get());
        setCurrentContext(null);
        
        return values;
    }
    
    public static class CurrentValues {
        /**
         * @param _serverContext
         * @param _serverContextFactory
         * @param _requestContext
         * @param _sessionContext
         * @param _traceState
         */
        public CurrentValues(ServerContext serverContext,
                ServerContextFactory serverContextFactory,
                RequestContext requestContext, SessionContext sessionContext,
                TraceState traceState) {
            _serverContext = serverContext;
            _serverContextFactory = serverContextFactory;
            _requestContext = requestContext;
            _sessionContext = sessionContext;
            _traceState = traceState;
        }
        
        /**
         * @return Returns the serverContext.
         */
        public ServerContext getServerContext() {
            return _serverContext;
        }
        /**
         * @return Returns the serverContextFactory.
         */
        public ServerContextFactory getServerContextFactory() {
            return _serverContextFactory;
        }
        /**
         * @return Returns the requestContext.
         */
        public RequestContext getRequestContext() {
            return _requestContext;
        }
        /**
         * @return Returns the sessionContext.
         */
        public SessionContext getSessionContext() {
            return _sessionContext;
        }
        /**
         * @return Returns the traceState.
         */
        public TraceState getTraceState() {
            return _traceState;
        }
        
        ServerContext _serverContext;
        ServerContextFactory _serverContextFactory;
        RequestContext _requestContext;
        SessionContext _sessionContext;
        TraceState _traceState;
    }
    
    /**
     * This method should never be used except by tests.  That is why the
     * declaration is not that friendly as well
     * @param objects
     */
    static void restoreValues(CurrentValues objects) {
        setCurrentContext(objects.getServerContext());
        _currentServerContextFactory.set(objects.getServerContextFactory());
        _currentRequestContext.set(objects.getRequestContext());
        _currentSessionContext.set(objects.getSessionContext());
        MocaLogEventFactory._localTraceState.set(objects.getTraceState());
    }
    
    /**
     * This will unassociate the server context with the current thread.  It
     * will leave the factory, session context and request context as is to 
     * allow for another context to replace it when {@link #getCurrentContext()}
     * is called.
     */
    public synchronized static void removeCurrentContext() {
        ServerContext context = _currentContext.get();
        // We have to unwrap any objects so the correct object refernce is 
        // stored, so it can be referenced for interruption etc.
        ServerContext realContext = context;
        List<WeakReference<Thread>> threadRefs = _contextThreads.remove(
            realContext);
        
        if (threadRefs != null) {
            Thread currentThread = Thread.currentThread();
            // We also have to clear up the hard references to the server 
            // context so it can be correctly garbage collected if the thread
            // no longer exists
            for (WeakReference<Thread> threadRef : threadRefs) {
                Thread thread = threadRef.get();
                
                if (thread != null && thread.equals(currentThread)) {
                    _hardContextReference.remove(thread);
                }
            }
        }
        
        // Then we finally remove the context.
        _currentContext.remove();
    }
    
    /**
     * This will register the current thread with the server context
     * and additionally apply the trace state on the current thread
     * @param serverContext The server context to register with
     */
    private synchronized static void registerCurrentThread(ServerContext serverContext) {
        // We have to unwrap any objects so the correct object reference is 
        // stored, so it can be referenced for interruption etc.
        ServerContext realContext = serverContext;
        List<WeakReference<Thread>> threads = _contextThreads.get(
            realContext);
        
        if (threads == null) {
            threads = new ArrayList<WeakReference<Thread>>();
            _contextThreads.put(realContext, threads);
        }
        
        Thread currentThread = Thread.currentThread();
        boolean found = false;
        // Before we add make sure the thread doesn't exist already.
        for (WeakReference<Thread> ref : threads) {
            if (currentThread.equals(ref.get())) {
                found = true;
                break;
            }
        }
        // We only add if the thread wasn't found in the list before. 
        if (!found) {
            threads.add(new WeakReference<Thread>(currentThread));
        }
        _hardContextReference.put(currentThread, realContext);
        
        // Additionally, apply the trace state as we now may be a newly spawned child thread
        TraceState traceState = MocaLogEventFactory._localTraceState.get();
        if (traceState != null) {
            traceState.applyTraceStateToThread();
        }
    }
    
    /**
     * Returns all the threads currently associated with the server contextb
     * @param serverContext The server context to get the threads for
     * @return The list of threads in the order of which they were associated
     *         with the context
     */
    synchronized
    public static List<Thread> getContextThreads(ServerContext serverContext) {
        // We have to unwrap any objects so the correct object refernce is 
        // stored, so it can be referenced for interruption etc.
        ServerContext realContext = serverContext;
        
        List<WeakReference<Thread>> threadRefs = _contextThreads.get(
            realContext);
        
        if (threadRefs != null) {
            List<Thread> threads = new ArrayList<Thread>(threadRefs.size());
            
            for (WeakReference<Thread> threadRef : threadRefs) {
                Thread thread = threadRef.get();
                
                if (thread != null) {
                    threads.add(thread);
                }
            }
            
            return threads;
        }
        
        return Collections.emptyList();
    }
    
    synchronized
    public static SystemContext globalContext() {
        return globalContext(null);
    }
    
    synchronized
    public static SystemContext globalContext(Reader config) {
        // If configuration has already been done, keep the same config.
        if (_reg == null) {
            // If they've passed a file to be used for configuration, read that.
            Reader in = config;
            
            // If not, look up via system properties and environment variables.
            if (in == null) {
                String regFile = System.getProperty("com.sam.moca.config");
                if (regFile == null) {
                    regFile = System.getenv("MOCA_REGISTRY");
                }
                
                if (regFile == null) {
                    throw new IllegalArgumentException("Invalid Configuration: MOCA_REGISTRY is not set");
                }
                
                try {
                    in = new InputStreamReader(new FileInputStream(regFile), "UTF-8");
                }
                catch (FileNotFoundException e) {
                    throw new IllegalArgumentException("Invalid Configuration: can't find registry file: " + e);
                }
                catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException("Problem with file encoding: " + e, e);
                }
            }

            // Try to read the configuration
            try {
                _reg = new RegistryReader(in);
            }
            catch (SystemConfigurationException e) {
                throw new IllegalArgumentException("Invalid Configuration: " + e, e);
            }
        }
        
        return _reg;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T globalAttribute(Class<T> cls) {
        return (T) globalContext().getAttribute(cls.getName());
    }
    
    /**
     * This will setup a MOCA daemon context.  The name will be the session name
     * for the daemon context.  Also a user can tell if the daemon context is
     * to be single threaded or multi threaded environment.  A single threaded
     * environment will use an in process native adapter, where as a multi 
     * threaded environment will use a native process pool to handle C calls.
     * This method call also assumes that clustering will not be ignored. To ignore
     * clustering use the form of this method with that option.
     * 
     * <br><br><b>
     * NOTE: If you are using a standalone Server Mode application, such as 
     * Msql, then you must only use single threaded.  If it is a Server Side 
     * task that runs with the instance than multi threaded is allowed.
     * </b><br>
     * @param name The name of the daemon context This cannot be null or an
     *        empty string.
     * @param singleThreaded Whether this daemon context is limited to a single
     *        thread 
     * @return The server context
     * @throws SystemConfigurationException If an exception occurs while 
     *         creating the daemon context.  Also thrown if a standalone Server
     *         mode application tries to run multi threaded without an 
     *         accompanying instance.
     */
    public static ServerContext setupDaemonContext(String name,
            boolean singleThreaded) throws SystemConfigurationException {
        return setupDaemonContext(name, singleThreaded, false);
    }
    
    /**
     * This will setup a MOCA daemon context.  The name will be the session name
     * for the daemon context.  Also a user can tell if the daemon context is
     * to be single threaded or multi threaded environment.  A single threaded
     * environment will use an in process native adapter, where as a multi 
     * threaded environment will use a native process pool to handle C calls.
     * Additionally a user can specify if clustering is to be ignored or not.
     * 
     * <br><br><b>
     * NOTE: If you are using a standalone Server Mode application, such as 
     * Msql, then you must only use single threaded.  If it is a Server Side 
     * task that runs with the instance than multi threaded is allowed.
     * </b><br>
     * @param name The name of the daemon context This cannot be null or an
     *        empty string.
     * @param singleThreaded Whether this daemon context is limited to a single
     *        thread 
     * @param ignoreCluster Whether or not to skip cluster initialization.
     * @return The server context
     * @throws SystemConfigurationException If an exception occurs while 
     *         creating the daemon context.  Also thrown if a standalone Server
     *         mode application tries to run multi threaded without an 
     *         accompanying instance.
     */
    public static ServerContext setupDaemonContext(String name, 
            boolean singleThreaded, boolean ignoreCluster) throws SystemConfigurationException {
        ServerContext ctx = _currentContext.get();
        if (ctx == null) {
            if (name == null || name.length() == 0) {
                throw new IllegalArgumentException("The name cannot be null " +
                		"or empty!");
            }
            
            // Create a session context.  This is required to run privileged commands. 
            SessionContext session = new LocalSessionContext(name, 
                SessionType.TASK);
            session.setSessionToken(new SessionToken(name));
            
            // Create a request context.
            Map<String, String> env = new HashMap<String, String>();
            RequestContext request = new RequestContext(env);
            
            ServerContextFactory factory = _currentServerContextFactory.get();
            
            if (factory == null) {
                // Set up logging, if not done already. Tasks don't need
                // to rebuild the runtime-logging.xml as it should already exist
                LoggingConfigurator.configure(false);
                
                // Create a system context.  This will set up MOCA's configuration according to
                // the MOCA registry.
                SystemContext sys = globalContext();
                
                sys.putAttribute(TASK_MODE, true);
                
                SessionAdministrationManagerBean bean = new SessionAdministrationManager();
                sys.putAttribute(SessionAdministrationManagerBean.class.getName(), 
                    bean);
                
                String clusterName = sys
                    .getConfigurationElement(MocaRegistry.REGKEY_CLUSTER_NAME);
                if (!ignoreCluster && clusterName != null) {
                    JChannel channel = JGroupsChannelFactory
                        .getChannelForConfig(sys);
                    MocaExecutionService execService = new MocaExecutionService(
                        channel);
                    JGroupsAsynchronousExecutor asyncExec = new JGroupsAsynchronousExecutor(
                        execService,
                        Integer.valueOf(sys
                            .getConfigurationElement(
                                MocaRegistry.REGKEY_CLUSTER_ASYNC_SUBMIT_CAP,
                                MocaRegistry.REGKEY_CLUSTER_ASYNC_SUBMIT_CAP_DEFAULT)));

                    sys.putAttribute(
                        "cluster-" + AsynchronousExecutor.class.getName(),
                        asyncExec);
                }
                
                configureLogging(sys, session.getTraceState());
                
                System.setProperty("LESDIR", sys.getVariable("LESDIR"));
                
                TransactionManager manager = configureTransactionManager(sys);
                TransactionManagerUtils.registerTransactionManager(manager);
                
                configureAuthManager(sys, false);

                NativeProcessPoolBuilder poolBuilder;
                if (singleThreaded) {
                    poolBuilder = new NativeProcessPoolBuilder().fixedSize(1);
                }
                else {
                    poolBuilder = getPoolBuilder(sys, getPoolName(sys));
                }
                
                // We create the default server context factory.  The in process
                // or pool native process will be handled by caller
                factory = new DefaultServerContextFactory(name, sys, singleThreaded, 
                        poolBuilder);
                
                _currentServerContextFactory.set(factory);
                sys.putAttribute(ServerContextFactory.class.getName(), 
                        factory);
            }
            
            // We have to setup all the thread local stuff after setting up
            // the factory possibly.  This is because factory creation sets
            // up some shutdown hooks and we don't want them inheriting those
            // values
            _currentSessionContext.set(session);
            _currentRequestContext.set(request);
            
            // Create a server context and set it up as the current context for the
            // calling thread.
            ctx = factory.newContext(request, session);
            setCurrentContext(ctx);
            
            // We have to initialize the factory after we setup all the 
            // inheritable thread local values, so that if other products use
            // RMI they will be able to call the moca context
            factory.initialize();
        }
//        else {
//            throw new IllegalStateException("Server Context was already setup!");
//        }
        
        return ctx;
    }
    
    private static void configureLogging(SystemContext system, TraceState state) {
        //Check to see if a trace level has been configured
        String traceLevel = system.getConfigurationElement(MocaRegistry.REGKEY_TRACE_LEVEL);
        
        if (traceLevel == null || traceLevel.length() == 0) {
            traceLevel = system.getVariable("MOCA_TRACE_LEVEL");
        }
        
        GlobalTraceState.setGlobalTraceLevel(traceLevel);
            
        // If we have a task id, means we were created as a task.
        String taskID = system.getVariable("MOCA_TASK_ID");
        
        if (taskID != null) {
            // If we were told to log to console only then ignore the appender.
            // TODO: look into MOCA_LOG_CONSOLE env variable
            //if (system.getVariable("MOCA_LOG_CONSOLE") == null) {
            //}

            // We have to always do this when a mocaserver task was identified
            // in case if the server were to crash.
            final InputStream istream = System.in;

            Thread thread = new Thread() {

                // @see java.lang.Thread#run()
                @Override
                public void run() {
                    try {
                        // If anything is sent back then we just
                        // exit, or if there is an issue
                        istream.read();
                    }
                    catch (IOException e) {

                    }
                    System.exit(0);
                }

            };

            thread.setName("MocaServerWatcher");
            thread.setDaemon(true);
            thread.start();
        }
        else {
            //Check to see if a trace file has been configured
            String traceFileName = system.getConfigurationElement(
                    MocaRegistry.REGKEY_TRACE_FILE);
            
            if (traceFileName != null && !traceFileName.isEmpty()) {
                GlobalTraceState.setGlobalTraceFile(traceFileName);
            }
        }
    }
    
    private static void configureAuthManager(SystemContext context, boolean shared) {
        String myDomain = context.getConfigurationElement(MocaRegistry.REGKEY_SECURITY_DOMAIN);
        String trustedList = context.getConfigurationElement(MocaRegistry.REGKEY_SECURITY_TRUSTED_DOMAINS);
        String[] trustedDomains = null;
        if (trustedList != null) {
            trustedDomains = trustedList.split(" ");
            ;
        }
        
        // Set up session manager
        MocaSessionManager authManager;
        if (shared) {
            String remoteTimeoutStr = context.getConfigurationElement(
                MocaRegistry.REGKEY_SECURITY_SESSION_REMOTE_TIMEOUT, 
                MocaRegistry.REGKEY_SECURITY_SESSION_REMOTE_TIMEOUT_DEFAULT);
            int remoteTimeout = Integer.parseInt(remoteTimeoutStr);
    
            String allowLegacySessionsStr = context.getConfigurationElement(
                MocaRegistry.REGKEY_SECURITY_ALLOW_LEGACY_SESSIONS);
            boolean allowLegacySessions = allowLegacySessionsStr == null ? true : Boolean.parseBoolean(allowLegacySessionsStr);
    
            authManager = new InfinispanMocaSessionManager(myDomain, trustedDomains,
                "moca-sessions", remoteTimeout, allowLegacySessions);
        }
        else {
            authManager = new ClassicMocaSessionManager(myDomain, trustedDomains);
        }
        
        context.putAttribute(MocaSessionManager.class.getName(), authManager);
    }
    
    private static TransactionManager configureTransactionManager(SystemContext system) {
        System.setProperty("com.arjuna.ats.arjuna.objectstore.objectStoreDir", 
            MocaUtils.expandEnvironmentVariables(system, "$LESDIR/data/ArjunaObjectStore"));
        
        String manager = system.getConfigurationElement(
            MocaRegistry.REGKEY_SERVER_TRANSACTION_MANAGER,
            MocaRegistry.REGKEY_SERVER_TRANSACTION_MANAGER_DEFAULT);
        Class<?> managerClass;
        try {
            managerClass = Class.forName(manager);
        }
        catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        
        if (TransactionManager.class.isAssignableFrom(managerClass)) {
            try {
                return (TransactionManager)managerClass.newInstance();
            }
            catch (InstantiationException e) {
                throw new IllegalArgumentException(e);
            }
            catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }
        }
        
        try {
            Method m = managerClass.getMethod("transactionManager");
            
            if (!TransactionManager.class.isAssignableFrom(m.getReturnType())) {
                throw new IllegalArgumentException("Class " + managerClass + 
                    " has method named transactionManager but doesn't return" +
                    "a TransactionManager class instance");
            }
            
            if (Modifier.isStatic(m.getModifiers())) {
                try {
                    return (TransactionManager)m.invoke(null);
                }
                catch (IllegalAccessException e) {
                    throw new IllegalArgumentException(e);
                }
                catch (InvocationTargetException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
        catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
        throw new IllegalArgumentException("Provided class " + managerClass + 
            " either is not an instance of " + TransactionManager.class + 
            " or doesn't have a static method named transactionManager that" +
            " returns a TransactionManager instance");
    }
    
    /**
     * This will setup a server context factory.  This can be used by a servlet
     * or similar code to dispatch new contexts for each request.
     * @param systemContext The system context to use
     * @return The Server Context Factory
     * @throws SystemConfigurationException if there was a problem creating the
     *         server context factory
     */
    public static ServerContextFactory setupServletContext(
            SystemContext systemContext) throws SystemConfigurationException {
        String server = "server";
        
        ServerContextFactory factory = _currentServerContextFactory.get();
        
        if (factory != null) {
            throw new IllegalStateException("Server Context was already setup!");
        }
            
        systemContext.putAttribute(TASK_MODE, false);
        
        SessionAdministrationManagerBean bean = new SessionAdministrationManager();
        systemContext.putAttribute(SessionAdministrationManagerBean.class.getName(), 
            bean);
        
        configureLogging(systemContext, null);

        System.setProperty("LESDIR", systemContext.getVariable("LESDIR"));
        
        // Validate console admin configuration if setup
        AuthService.validateConsoleAdminConfiguration(systemContext);

        // MaD Initialization.
        ProbeManager probeManager = null;
        if (MonitoringUtils.isMonitoringEnabled(systemContext)) {
            ApplicationContext ctx = null;
            ctx = new AnnotationConfigApplicationContext(MocaMadServerConfiguration.class);
            
            // Pass the ApplicationContext to MadUtil, this will initialize
            // the MadFactory and start the MaD server.
            // We want to make sure the MadFactory is setup before tasks, jobs, and components can execute
            // so that probes can properly be registered.
            // Also, we'll defer the initialized probes later until we're done initializing Moca.
            probeManager = MadUtil.initMonitoring(ctx, false);
            
            // Register the monitoring Mbean
            MonitoringInformation.registerMBean();
            
            // Push metrics relevant to the Moca server starting
            MonitoringUtils.pushStartupMetrics();
        }
        
        TransactionManager manager = configureTransactionManager(systemContext);
        TransactionManagerUtils.registerTransactionManager(manager);
        String disableNative = systemContext.getConfigurationElement(
            MocaRegistry.REGKEY_SERVER_TEST_DISABLE_NATIVE,
            MocaRegistry.REGKEY_SERVER_TEST_DISABLE_NATIVE_DEFAULT);

        if (disableNative.equalsIgnoreCase("true")) {
            factory = new DefaultServerContextFactory(server, systemContext, false, null);
        }
        else {
            NativeProcessPoolBuilder poolBuilder = getPoolBuilder(systemContext, getPoolName(systemContext));
            factory = new DefaultServerContextFactory(server, systemContext, false, poolBuilder);
            // We then initialize the factory as normal
            factory.initialize();
        }

        ServerContextFactory factoryProxy = new OurServerContextFactory(factory);
        // We have to set the proxy factory so that if anything spawns a thread
        // before we create a context that they will be able to access the
        // Moca Context - namely RMI
        _currentServerContextFactory.set(factoryProxy);
        systemContext.putAttribute(ServerContextFactory.class.getName(), 
                factoryProxy);
        
        // Create a session context.  This is required to run privileged commands. 
        SessionContext session = new LocalSessionContext(server, 
            SessionType.SERVER);
        session.setSessionToken(new SessionToken(server));
        _currentSessionContext.set(session);
        
        // Create a request context.
        Map<String, String> env = new HashMap<String, String>();
        RequestContext request = new RequestContext(env);
        _currentRequestContext.set(request);
        
        // We configure the authentication manager in case it needs to
        // spawn thread so it can have a context configured
        configureAuthManager(systemContext, true);

        // If monitoring is enabled this will be set, we initialize
        // the Probe Manager after everything else has been setup.
        if (probeManager != null) {
            // Initialize a ProbeManager for scheduling/initializing probes
            try {
                probeManager.findAndInitializeProbes();
            }
            catch (Exception ex) {
                LogManager.getLogger(LogManager.ROOT_LOGGER_NAME).error(
                    "Encountered an error while initializing the MAD Probe Manager", ex);
            }
        }
        
        return factoryProxy;
    }
    
    private static String getPoolName(SystemContext systemContext) {
        // Build a pool name from the environment name and node id.
        String envname = systemContext.getVariable("MOCA_ENVNAME");
        String taskId = systemContext.getVariable("MOCA_TASK_ID");
        // TODO: need something else unique
        String poolName = ((envname != null) ? envname : "Sam") +
                       ((taskId != null) ? "-" + taskId : "");
        
        return poolName;
    }
    
    private static NativeProcessPoolBuilder getPoolBuilder(SystemContext systemContext, String poolName) {
        NativeProcessPoolBuilder poolBuilder = new NativeProcessPoolBuilder();
        
        String poolSizeStr = systemContext.getConfigurationElement(
                MocaRegistry.REGKEY_SERVER_MAX_POOL_SIZE,
                MocaRegistry.REGKEY_SERVER_MAX_POOL_SIZE_DEFAULT);
        int poolSize = Integer.parseInt(poolSizeStr);
        
        String minIdlePoolSizeStr = systemContext.getConfigurationElement(
            MocaRegistry.REGKEY_SERVER_MIN_IDLE_POOL_SIZE,
            MocaRegistry.REGKEY_SERVER_MIN_IDLE_POOL_SIZE_DEFAULT);
        int minPoolSize = Integer.parseInt(minIdlePoolSizeStr);
        
        poolBuilder.size(minPoolSize, poolSize);
        
        String maxCommandSizeStr = systemContext.getConfigurationElement(
                MocaRegistry.REGKEY_SERVER_MAX_SERVER_REQUESTS,
                MocaRegistry.REGKEY_SERVER_MAX_SERVER_REQUESTS_DEFAULT);
        
        // If we were given a max command size then make sure the
        // pool knows this.  This should always happen since we have a default
        // value provided to it.
        if (maxCommandSizeStr != null && maxCommandSizeStr.trim().length() > 0) {
            int maxCommandSize = Integer.parseInt(maxCommandSizeStr);
            
            poolBuilder.maxUsage(maxCommandSize);
        }
        
        return poolBuilder;
    }
     

    public static Object copyArg(MocaValue in, MocaType type) throws TypeMismatchException {
        
        if (in == null || in.getValue() == null) {
            return null;
        }
                      
        if (type == in.getType()) {
            return in.getValue();
        }
                
        switch (type) {
        case STRING:
            return in.asString();
        case INTEGER:
           return in.asInt();
        case DOUBLE:
            return in.asDouble();
        case BOOLEAN:
            if (in.getType() == MocaType.STRING) {
                // To reproduce atol behavior we loop until we find
                // a non whitespace.  If it is a number then it is true 
                // unless the number is zero.  If it was a character then 
                // it becomes 0.
                for (char c : in.asString().toCharArray()) {
                    if (Character.isDigit(c)) {
                        if (c == '0') {
                            return Boolean.valueOf(false);
                        }
                        else {
                            return Boolean.valueOf(true);
                        }
                    }
                    else if (Character.isLetter(c)) {
                        return Boolean.valueOf(false);
                    }
                }
            }
            return in.asBoolean();
        case DATETIME:
            return in.asDate();
        case RESULTS:
            return ResultMapper.createResults(in);
        case OBJECT:
            return in.getValue();
        case GENERIC:
            if (in.getType() == MocaType.INTEGER) {
                return new GenericPointer(in.asInt());
            }
            else if (in.getType() == MocaType.DOUBLE) {
                // This needs to be double->long cast to support some existing behavior.
                // of components initiating commands with "where ptr = %u" and sending a
                // pointer as a (floating point, now) numeric.
                return new GenericPointer((long)in.asDouble());
            }
            else if (in.getType() == MocaType.STRING) {
                String valueAsString = in.asString();
                try {
                    // This is a hack to interpret pointer values being represented as strings.  Although the format is
                    // never guaranteed, most platforms will render pointers (via sprintf("%p")) as a hex string, often
                    // prefixed by 0x.  This approach is by no means safe, but as we don't ever dereference the pointer
                    // except in native code, it's probably safe enough.
                    if (valueAsString.startsWith("0x")) {
                        return new GenericPointer(new BigInteger(valueAsString.substring(2), 16).longValue());
                    }
                    else {
                        return new GenericPointer(new BigInteger(valueAsString, 16).longValue());
                    }
                }
                catch (NumberFormatException e) {
                    throw new TypeMismatchException(type.toString());
                }
            }
            
            throw new TypeMismatchException(type.toString());
        default:
            throw new TypeMismatchException(type.toString());
        }
    }
    
    private static class OurServerContextFactory extends ForwardingServerContextFactory {
        
        /**
         * @param factory
         */
        public OurServerContextFactory(ServerContextFactory factory) {
            super(factory);
        }
        
        // @see com.sam.moca.advice.ForwardingServerContextFactory#newContext(com.sam.moca.server.exec.RequestContext, com.sam.moca.server.exec.SessionContext)
        @Override
        public ServerContext newContext(RequestContext req,
            SessionContext session) {
            // We want to pass ourselves as the factory so we get inherited
            _currentServerContextFactory.set(this);
            _currentRequestContext.set(req);
            _currentSessionContext.set(session);
            return super.newContext(req, session);
        }
        
        // @see com.sam.moca.advice.ForwardingServerContextFactory#associateContext(com.sam.moca.server.exec.RequestContext, com.sam.moca.server.exec.SessionContext)
        @Override
        public void associateContext(RequestContext req, SessionContext session) {
            _currentServerContextFactory.set(this);
            _currentRequestContext.set(req);
            _currentSessionContext.set(session);
            
            super.associateContext(req, session);
        }
    }
    
    
    /**
     * This method is to be called as the first thing in a thread that is to be
     * persisted between request contexts.  This will associate that persistent
     * thread with the new context by taking known values from the Thread passed
     * in to it.  This thread should therefore be the main thread that is used
     * in the context call.
     * @param parentSessionThread The main thread of which to update the context
     *        of the persistent thread.
     */
    public static void associateCurrentThreadWithSession(Thread 
            parentSessionThread) {
        boolean changed = false;
        try {
            final Field inheritableThreadLocals = Thread.class.getDeclaredField(
                    "inheritableThreadLocals");
            
            // We have to set it accessible since we are not in the java.lang
            // package
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    inheritableThreadLocals.setAccessible(true);
                    return null;
                }
                
            });
            
            Object itlMap = inheritableThreadLocals.get(parentSessionThread);
            
            Class<? extends Object> itlClass = itlMap.getClass();
            
            final Method getEntryMethod = itlClass.getDeclaredMethod("getEntry", 
                    ThreadLocal.class);
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    getEntryMethod.setAccessible(true);
                    return null;
                }
                
            });
            
            {
                Object factoryEntry = getEntryMethod.invoke(itlMap, 
                        _currentServerContextFactory);
                
                final Field factoryField = factoryEntry.getClass().getDeclaredField(
                        "value");
                
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    @Override
                    public Void run() {
                        factoryField.setAccessible(true);
                        return null;
                    }
                    
                });
                
                ServerContextFactory parentFactory = (ServerContextFactory) factoryField
                        .get(factoryEntry);
                
                // If the parent factory doesn't equal the current factory
                // then we have to reset it
                if (!parentFactory.equals(_currentServerContextFactory.get())) {
                    _currentServerContextFactory.set(parentFactory);
                    changed = true;
                }
            }
            
            {
                Object factoryEntry = getEntryMethod.invoke(itlMap, 
                        _currentSessionContext);
                
                final Field factoryField = factoryEntry.getClass().getDeclaredField(
                        "value");
                
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    @Override
                    public Void run() {
                        factoryField.setAccessible(true);
                        return null;
                    }
                    
                });
                
                SessionContext parentSession = (SessionContext) factoryField
                        .get(factoryEntry);
                
                SessionContext currentSession = _currentSessionContext.get();
                // If the parent session doesn't equal the current session
                // then we have to reset it
                if (!parentSession.equals(currentSession)) {
                    _currentSessionContext.set(parentSession);
                    MocaLogEventFactory._localTraceState.set(parentSession.getTraceState());
                    changed = true;
                }
            }
            
            {
                Object factoryEntry = getEntryMethod.invoke(itlMap, 
                        _currentRequestContext);
                
                final Field factoryField = factoryEntry.getClass().getDeclaredField(
                        "value");
                
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    @Override
                    public Void run() {
                        factoryField.setAccessible(true);
                        return null;
                    }
                    
                });
                
                RequestContext parentRequest = (RequestContext) factoryField
                        .get(factoryEntry);
                
                // If the parent request doesn't equal the current session
                // then we have to reset it
                if (!parentRequest.equals(_currentRequestContext.get())) {
                    _currentRequestContext.set(parentRequest);
                    changed = true;
                }
            }
        }
        catch (IllegalArgumentException e) {
            rethrowException(e);
        }
        catch (IllegalAccessException e) {
            rethrowException(e);
        }
        catch (InvocationTargetException e) {
            rethrowException(e);
        }
        catch (SecurityException e) {
            rethrowException(e);
        }
        catch (NoSuchFieldException e) {
            rethrowException(e);
        }
        catch (NoSuchMethodException e) {
            rethrowException(e);
        }
        finally {
            // Lastly we remove the current context since we have a new
            // factory, but only if we are changing the factory, request or
            // session
            if (changed) {
                _currentContext.set(null);
            }
        }
    }
    
    private static void rethrowException(Exception e) {
        // If any exception is raised here then we should fail fast, as
        // this is not expected.  It could be that something in the thread
        // class changed definition
        LogManager.getLogger(LogManager.ROOT_LOGGER_NAME).fatal("There was an unexpected exception " +
                    "encountered while setting up context!", e);
        throw new MocaRuntimeException(new UnexpectedException(e));
    }
    
    // A utility method for the convenience of unit tests.
    synchronized
    static void setGlobalContext(SystemContext reg) {
        _reg = reg;
    }
    
    // A utility method for the convenience of unit tests.
    synchronized
    static SystemContext getGlobalContext() {
        return _reg;
    }
    
    /**
     * @param currentThread
     * @return
     */
    static ServerContext getHardContextReferenceForThread(
        Thread currentThread) {
        return _hardContextReference.get(currentThread);
    }
    
    private final static Map<ServerContext, List<WeakReference<Thread>>> _contextThreads = 
        new WeakHashMap<ServerContext, List<WeakReference<Thread>>>();
    
    /**
     * This guys is required to keep our server context having a hard reference
     * as long as the threads that are being used on it are not GC'd 
     */
    private final static Map<Thread, ServerContext> _hardContextReference =
        new WeakHashMap<Thread, ServerContext>();
    private final static ThreadLocal<ServerContext> _currentContext = 
        new ThreadLocal<ServerContext>();
    private final static ThreadLocal<ServerContextFactory> _currentServerContextFactory =
        new InheritableThreadLocal<ServerContextFactory>();
    private final static ThreadLocal<SessionContext> _currentSessionContext = 
        new InheritableThreadLocal<SessionContext>();
    private final static ThreadLocal<RequestContext> _currentRequestContext = 
        new InheritableThreadLocal<RequestContext>();
    private static SystemContext _reg;
    
    public final static String TASK_MODE = "moca-task-mode";
}
