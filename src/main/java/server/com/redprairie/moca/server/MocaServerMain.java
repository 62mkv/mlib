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

package com.redprairie.moca.server;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXPrincipal;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.MBeanServerForwarder;
import javax.security.auth.Subject;
import javax.servlet.DispatcherType;

import org.apache.logging.log4j.LogManager;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.deploy.providers.WebAppProvider;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.ssl.SslConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.xml.XmlConfiguration;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.client.NotAuthorizedException;
import com.redprairie.moca.server.db.DBType;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.log.LoggingConfigurator;
import com.redprairie.moca.server.session.MocaSessionManager;
import com.redprairie.moca.server.socket.MocaProtocolServer;
import com.redprairie.moca.servlet.MocaConsoleServlet;
import com.redprairie.moca.servlet.MocaServlet;
import com.redprairie.moca.util.AppUtils;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.moca.util.Options;
import com.redprairie.moca.util.OptionsException;
import com.redprairie.moca.web.console.Authentication;
import com.redprairie.moca.web.console.AuthenticationFilter;

/**
 * The main class that starts a new 
 * MOCA web server process.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MocaServerMain {
       
    static void showUsage() {
        System.out
                .println("Usage: mocaserver [ -JTvh ] [ -p <port number> ]\n"
                       + "                            [ -r <port number> ]\n"
                       + "                            [ -m <memory file> ]\n"
                       + "                            [ -o <trace file> ] [ -t <trace level> ]\n");

        System.out.println(
                  "\t-p <port number>       Port number to listen on\n"
                + "\t-P <port number>       Socket (legacy MOCA) port number to listen on\n"
                + "\t-r <port number>       RMI port number to use\n"
                + "\t-m <memory file>       Location of memory file to use\n"
                + "\t-o <trace file>        Sets the trace file for output\n"
                + "\t-t <trace level>       The trace levels to enable\n"
                + "\t-J                     Do not schedule jobs\n"
                + "\t-T                     Do not start tasks\n"
                + "\t-R                     Inhibit RMI\n"
                + "\t-v                     Show version information\n"
                + "\t-h                     Show this message\n"
                + "\n"
                + "Trace Level Switches\n"
                + "   W - Application Flow Messages\n"
                + "   M - Manager Messages\n"
                + "   R - Performance Statistics\n"
                + "   A - Server Arguments\n"
                + "   X - Server Messages\n"
                + "   S - SQL Calls\n");
    }

    /** 
     * Starts a new MOCA web server process
     * @param args The argument switches used to start the process
     * @throws SystemConfigurationException 
     */
    public static void main(String[] args) throws SystemConfigurationException {
        System.out.println("now start test");
        System.setProperty("com.redprairie.moca.config","F:\\MFC\\mlib\\src\\resource\\82.registry");
        System.setProperty("LESDIR","F:\\MFC\\mlib");
        String lesdir = System.getenv("LESDIR");
        System.out.println(lesdir);
        args = new String[3];
        args[0] = "-R";
        args[1] = "-t*";
        args[2] ="-TJ";
        System.out.println("hehe");
        try {
            setupSystemProperties();
        	bootstrapServer(args);
        }
        catch (Throwable startupException) {
    	    // If an unexpected exception occurs we should explicitly exit
    	    // as there may be some non-daemon threads spawned at this point that would
            // prevent shutdown from occurring.
            try {
                LogManager.getLogger(LogManager.ROOT_LOGGER_NAME)
    	                  .fatal("Server failed to start due to an unexpected error, exiting", startupException);
            }
            catch (Throwable loggingException) {
                // Issue with the logging subsystem so just instead write to standard error
                System.err.println("Server failed to start due to an unexpected error, exiting");
                startupException.printStackTrace();
            }
            finally {
                System.exit(1);
            }
        }
    }
    
    private static void setupSystemProperties() {
        // we have to set this to make sure that jboss logging picks up log4j*1*
        // and we can go through the bridge for logging because otherwise
        // jboss logging will pick up log4j2 as the logging provider but can't actually use it
        // because we are on the log4j2 beta version and the API is out of date
        System.setProperty("org.jboss.logging.provider", "log4j");
    }

    private static void bootstrapServer(String[] args) throws SystemConfigurationException {
    	//Try to get the global context which reads the settings
        SystemContext context = ServerUtils.globalContext();
        Options options = overrideCommandLineArgs(context, args);
        overrideTracing(context, options);
        
        
        //If we have a cluster name from the environment (testing, or otherwise), 
        //we override it in the registry here.  We do this since it's retrieved, 
        //from the registry many times.
        String clusterName = System.getenv("MOCA_CLUSTER_NAME");
        if (clusterName != null) {
            context.overrideConfigurationElement(
                MocaRegistry.REGKEY_CLUSTER_NAME, clusterName);
        }
        
        //Check if we're just printing out the version
        if (options.isSet('v')) {
            System.out.print(AppUtils.getVersionBanner("MLib Server"));
            return;
        }
        
        if (options.isSet('h')) {
            showUsage();
            return;
        }
        
        System.out.print(AppUtils.getStartBanner("MLib Server"));

        //Setup Logging
        LoggingConfigurator.configure();
        final ServerContextFactory factory = ServerUtils.setupServletContext(context);
        
        if (!options.isSet('R')) {
            // First, initialize the RMI Registry on an alternate port
            int rmiPort = Integer.parseInt(context.getConfigurationElement(MocaRegistry.REGKEY_SERVER_RMI_PORT,
                                                                           MocaRegistry.REGKEY_SERVER_RMI_PORT_DEFAULT));
            
            try {
                LocateRegistry.createRegistry(rmiPort);
            }
            catch (RemoteException e) {
                LogManager.getLogger(LogManager.ROOT_LOGGER_NAME).fatal("Server failed to start (RMI Error), exiting.", e);
                System.exit(1);
            }
            
            // We have to start up a connector server so that people can get the
            // know address to look up this mbean server
            try {
                JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:"
                        + context.getConfigurationElement(MocaRegistry.REGKEY_SERVER_RMI_PORT,
                            MocaRegistry.REGKEY_SERVER_RMI_PORT_DEFAULT) + "/admin");
                Map<String, Object> env = new HashMap<String, Object>();
                // Set our authentication object so we can control who attempts
                // to log in remotely.
                env.put(JMXConnectorServer.AUTHENTICATOR, 
                        new MOCAJMXAuthenticator(context, factory));
                
                JMXConnectorServer connectorServer = 
                    JMXConnectorServerFactory.newJMXConnectorServer(url, env, 
                            ManagementFactory.getPlatformMBeanServer());
                // We also set a forwarder, so we can prevent certain
                // operations such as people remotely removing or adding mbeans
                connectorServer.setMBeanServerForwarder(
                        MBSFInvocationHandler.newProxyInstance());
                connectorServer.start();
            }
            catch (MalformedURLException e) {
                LogManager.getLogger(LogManager.ROOT_LOGGER_NAME).warn("Server failed to start (JMX Error), exiting.", e);
            }
            catch (IOException e) {
                LogManager.getLogger(LogManager.ROOT_LOGGER_NAME).warn("Server failed to start (JMX Error), exiting.", e);
            }
        }
        
        File jettyConfigFile = context.getDataFile(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (name.equalsIgnoreCase("jetty.xml")) {
                    return true;
                }
                return false;
            }
            
        });
        
        // Set up the Jetty server.
        Server server = new Server();
        
        if (jettyConfigFile != null && jettyConfigFile.exists()) {
            try {
                XmlConfiguration config = new XmlConfiguration(jettyConfigFile.toURI().toURL());
                config.configure(server);
            }
            catch (Exception e) {
                // Jetty's XmlConfiguration is declared to throw Exception
                LogManager.getLogger(LogManager.ROOT_LOGGER_NAME).fatal("Server failed to start (Jetty config error), exiting.", e);
                System.exit(1);
            }
        }

        String localHostName = null;
        try {
             localHostName = InetAddress.getLocalHost().getCanonicalHostName();
        }
        catch (UnknownHostException e) {
            LogManager.getLogger(LogManager.ROOT_LOGGER_NAME).error("There was a problem acquiring fully " +
            		"qualified host name!", e);
        }
        
        boolean ssl = false;
        int port;
        Connector[] connectors = server.getConnectors();
        // If no connector is configured, try getting the port from the registry and setting up a Simple
        // NIO connector.
        if (connectors == null || connectors.length == 0) {
            port = Integer.parseInt(context.getConfigurationElement(MocaRegistry.REGKEY_SERVER_PORT, 
                                                                        MocaRegistry.REGKEY_SERVER_PORT_DEFAULT));
            SocketConnector connector = new SocketConnector();
            connector.setPort(port);
            server.addConnector(connector);
        }
        else {
            Connector preferredConnector = null;
            for (Connector connector : connectors) {
                if (preferredConnector == null) {
                    preferredConnector = connector;
                }
                if (connector instanceof SslConnector) {
                    preferredConnector = connector;
                    ssl = true;
                    break;
                }
            }
            
            port = preferredConnector.getPort();
        }

        if (localHostName != null) {
            InstanceUrl url = new InstanceUrl(ssl, localHostName, port);
            
            context.putAttribute(InstanceUrl.class.getName(), url);
        }
        
        // Create a servlet context handler.   
        ServletContextHandler servletHandler = new ServletContextHandler(null, 
            "/", ServletContextHandler.SESSIONS | ServletContextHandler.SECURITY);
        
        File jettyHandlerFile = context.getDataFile(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (name.equalsIgnoreCase("jetty-handler.xml")) {
                    return true;
                }
                return false;
            }
            
        });
        
        if (jettyHandlerFile != null && jettyHandlerFile.exists()) {
            try {
                XmlConfiguration config = new XmlConfiguration(jettyHandlerFile.toURI().toURL());
                config.configure(servletHandler);
            }
            catch (Exception e) {
                // Jetty's XmlConfiguration is declared to throw Exception
                LogManager.getLogger(LogManager.ROOT_LOGGER_NAME).fatal("Server failed to start (Jetty handler config error), exiting.", e);
                System.exit(1);
            }
        }
        
        servletHandler.setResourceBase(context.getVariable("MOCADIR") + 
            "/web");
        
        MocaSessionManager authManager = (MocaSessionManager) context
            .getAttribute(MocaSessionManager.class.getName());

        // Add the servlet for the service.
        ServletHolder holder = new ServletHolder(new MocaServlet(context, 
            factory, authManager));
        holder.setInitOrder(0);
        servletHandler.addServlet(holder, "/service");
        servletHandler.setInitParameter(
            "org.eclipse.jetty.servlet.Default.dirAllowed", "false");
        
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        
        DeploymentManager manager = new DeploymentManager();
        manager.setContexts(contexts);
        WebAppProvider provider = new WebAppProvider();
        // We want the WAR libraries to take precendence when classloading.
        provider.setParentLoaderPriority(false);
        provider.setExtractWars(true);
        provider.setScanInterval(10);
        provider.setMonitoredDirName(MocaUtils.expandEnvironmentVariables(
            context, "$LESDIR/webdeploy"));
        
        manager.addAppProvider(provider);
        server.addBean(manager);
        
        // Add the filters for the console.
        servletHandler.addFilter(AuthenticationFilter.class, "/console", 
            EnumSet.allOf(DispatcherType.class));
        servletHandler.addFilter(AuthenticationFilter.class, "/console/*", 
            EnumSet.allOf(DispatcherType.class));
        
        // Add the servlets for the console.
        boolean hasDatabase = (factory.getDBAdapter().getDBType() != DBType.NONE);
        MocaConsoleServlet consoleServlet = new MocaConsoleServlet(factory, hasDatabase);
        holder = new ServletHolder(consoleServlet);
        holder.setInitOrder(Integer.MAX_VALUE);
        servletHandler.addServlet(holder, "/console");
        servletHandler.addServlet(holder, "/console/login.do");
        servletHandler.addServlet(holder, "/console/console.do");
        servletHandler.addServlet(holder, "/console/download");
        servletHandler.addServlet(holder, "/console/profile");
        servletHandler.addServlet(holder, "/console/support");
        
        servletHandler.addServlet(new ServletHolder(DefaultServlet.class), 
            "/");
        
        // Set the handlers for the server.  Now that our ServletContextHandler
        // throws a 404 on anything not handled, their handler has to go first
        // so they can get a chance to run.  Therefore any custom handler
        // must not have a / contextPath.
        Handler[] handlersToUse;
        Handler oldHandler = server.getHandler();
        if (oldHandler != null) {
            if (oldHandler instanceof HandlerCollection) {
                Handler[] oldHandlers = ((HandlerCollection)oldHandler).getChildHandlers();
                handlersToUse = new Handler[2 + oldHandlers.length];
                
                System.arraycopy(oldHandlers, 0, handlersToUse, 0, 
                    oldHandlers.length);
            }
            else {
                handlersToUse = new Handler[3];
                handlersToUse[0] = oldHandler;
            }
        }
        else {
            handlersToUse = new Handler[2];
        }
        
        handlersToUse[handlersToUse.length - 2] = contexts;
        handlersToUse[handlersToUse.length - 1] = servletHandler;
        
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(handlersToUse);
        
        server.setHandler(handlers);
        
        // Start the server.
        try {
            server.start();
        }
        catch (Exception e) {
            LogManager.getLogger(LogManager.ROOT_LOGGER_NAME).fatal("Server failed to start, exiting.", e);
        }
        
        // Start up a legacy MOCA server.
        String classicPort = context.getConfigurationElement(MocaRegistry.REGKEY_SERVER_CLASSIC_PORT);
        if (classicPort != null) {
            int socketPort = Integer.parseInt(classicPort);
            String encoding = context.getConfigurationElement(MocaRegistry.REGKEY_SERVER_CLASSIC_ENCODING);
            String temp = context.getConfigurationElement(MocaRegistry.REGKEY_SERVER_CLASSIC_POOL_SIZE);
            int poolSize = 0;
            if (temp != null) poolSize = Integer.parseInt(temp);
            temp = context.getConfigurationElement(MocaRegistry.REGKEY_SERVER_CLASSIC_IDLE_TIMEOUT);
            int idleTimeout = 0;
            if (temp != null) idleTimeout = Integer.parseInt(temp);
            
            new MocaProtocolServer(socketPort, poolSize, encoding, idleTimeout).start(factory, authManager);
        }
        
        // Last thing we do is install a security manager to make sure no one
        // does anything like System.exit
        System.setSecurityManager(new MocaSecurityManager());
    }
    
    /**
     * This is a simple proxy class to control what methods are able to be
     * invoked remotely on our mbean server.  We don't want someone to remove
     * or add new beans remotely and plus we can block certain operations if
     * it isn't a certain user if needed.
     * 
     * Copyright (c) 2010 Sam Corporation
     * All Rights Reserved
     * 
     * @author wburns
     */
    public static class MBSFInvocationHandler implements InvocationHandler {

        public static MBeanServerForwarder newProxyInstance() {

            final InvocationHandler handler = new MBSFInvocationHandler();

            final Class<?>[] interfaces =
                new Class<?>[] {MBeanServerForwarder.class};

            Object proxy = Proxy.newProxyInstance(
                                 MBeanServerForwarder.class.getClassLoader(),
                                 interfaces,
                                 handler);

            return MBeanServerForwarder.class.cast(proxy);
        }

        public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {

            final String methodName = method.getName();

            if (methodName.equals("getMBeanServer")) {
                return mbs;
            }

            if (methodName.equals("setMBeanServer")) {
                if (args[0] == null)
                    throw new IllegalArgumentException("Null MBeanServer");
                if (mbs != null)
                    throw new IllegalArgumentException("MBeanServer object " +
                                                       "already initialized");
                mbs = (MBeanServer) args[0];
                return null;
            }

            // Retrieve Subject from current AccessControlContext
            AccessControlContext acc = AccessController.getContext();
            Subject subject = Subject.getSubject(acc);

            // Allow operations performed locally on behalf of the connector server itself
            if (subject == null) {
                try {
                    return method.invoke(mbs, args);
                }
                catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }

            // Restrict access to "createMBean" and "unregisterMBean" to any user
            if (methodName.equals("createMBean") || methodName.equals("unregisterMBean")) {
                throw new SecurityException("Access denied");
            }

            // Retrieve JMXPrincipal from Subject
            Set<JMXPrincipal> principals = subject.getPrincipals(JMXPrincipal.class);
            if (principals == null || principals.isEmpty()) {
                throw new SecurityException("Access denied");
            }
            Principal principal = principals.iterator().next();
            String identity = principal.getName();

            // anyone else can perform any operation other than "createMBean" 
            // and "unregisterMBean"
            // TODO: maybe check back with this later to see if we want to change this control
            if (identity != null) {
                try {
                    return method.invoke(mbs, args);
                }
                catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }
            else {
                throw new SecurityException("Access denied");
            }
        }

        private MBeanServer mbs;
    }
    
    /**
     * This class will do our authentication for the JXM Connector Server.  This
     * way we can control what users we want to be able to get into the server
     * to view details.
     * 
     * Copyright (c) 2010 Sam Corporation
     * All Rights Reserved
     * 
     * @author wburns
     */
    private static class MOCAJMXAuthenticator implements JMXAuthenticator {
        
        public MOCAJMXAuthenticator(SystemContext ctx, ServerContextFactory factory) {
            _ctx = ctx;
            _factory = factory;
        }
        
        @Override
        public Subject authenticate(Object credentials) {
            
            if (credentials instanceof Authenticated) {
                return new Subject(true,
                        Collections.singleton(new JMXPrincipal(
                                ((Authenticated)credentials)._name)),
                        Collections.EMPTY_SET,
                        Collections.EMPTY_SET);
            }

            // Verify that credentials is of type String[].
            //
            if (!(credentials instanceof String[])) {
                // Special case for null so we get a more informative message
                if (credentials == null) {
                    throw new SecurityException("Credentials required");
                }
                throw new SecurityException("Credentials should be String[]");
            }

            // Verify that the array contains two elements (username/password).
            final String[] aCredentials = (String[]) credentials;
            if (aCredentials.length != 2) {
                throw new SecurityException("Credentials should have 2 elements");
            }

            // Perform authentication
            //
            String username = (String) aCredentials[0];
            String password = (String) aCredentials[1];

            if (password == null) {
                throw new SecurityException("There was no password provided");
            }

            try {
                String clientRequestAddr = RemoteServer.getClientHost();
                Authentication.login(_ctx, _factory, username, password,
                    Authentication.RequestType.JMX,
                    clientRequestAddr);
                return new Subject(true,
                    Collections.singleton(new JMXPrincipal(username)),
                    Collections.EMPTY_SET, Collections.EMPTY_SET);
            }
            catch (NotAuthorizedException e) {
                throw new SecurityException("User is not authorized for JMX access", e);
            }
            catch (MocaException e) {
                throw new SecurityException("Could not authenticate jmx", e);
            }
            catch (ServerNotActiveException e) {
                // Checked exception off RemoteServer.getClientHost() but it
                // can never happen as we are serving a RMI client request in this thread to authenticate it
                throw new SecurityException("Could not determine client host", e);
            }
        }
        
        private final SystemContext _ctx;
        private final ServerContextFactory _factory;
    }
    
    
    static final class Authenticated implements Serializable {
        private static final long serialVersionUID = -7338606633427886008L;
        
        /**
         * 
         */
        public Authenticated(String name) {
            if (name == null) throw new NullPointerException();
            _name = name;
        }
        
        private final String _name;
    }
    
    /**
     * Processes any command line arguments.
     * @param context The system context
     * @param args The passed command line arguments
     */
    public static Options overrideCommandLineArgs(SystemContext context, String[] args) {
        
        if (args != null) {
            try {
                Options options = Options.parse(CMDLINE_FORMAT, args);
                
                for (CommandLineArgument argument : CMDLINE_ARGS) {
                    
                    if (options.isSet(argument.getArgSwitch())) {
                        String value = options.getArgument(argument.getArgSwitch());
                        if (value == null) {
                            value = argument.getValue();
                        }
                        
                        context.overrideConfigurationElement(argument.getRegistryKey(), value);
                    }
                }
                
                return options;
            }
            catch (OptionsException eOpts) {
                throw new IllegalArgumentException("Invalid command line arguments: " + eOpts, eOpts);
            }
        }
        
        return null;
    }
    
    /***
     * For the new tracing requirements, we no longer want to get custom logging levels. If 
     * tracing is set, we just turn it on.
     * 
     * 
     * @param context
     * @param options
     */
    public static void overrideTracing(SystemContext context, Options options) {
        CommandLineArgument argument = CMDLINE_TRACE_SERVER_LEVEL;
        if(options != null && options.isSet(argument.getArgSwitch())){
            context.overrideConfigurationElement(argument.getRegistryKey(), "*");
        }
    }
    
    // Command Line Argument Mappings
    public static final CommandLineArgument CMDLINE_PORT = new CommandLineArgument('p', MocaRegistry.REGKEY_SERVER_PORT);
    public static final CommandLineArgument CMDLINE_RMI_PORT = new CommandLineArgument('r', MocaRegistry.REGKEY_SERVER_RMI_PORT);
    public static final CommandLineArgument CMDLINE_SOCKET_PORT = new CommandLineArgument('P', MocaRegistry.REGKEY_SERVER_CLASSIC_PORT);
    public static final CommandLineArgument CMDLINE_MEMORY_FILE = new CommandLineArgument('m', MocaRegistry.REGKEY_SERVER_MEMORY_FILE);
    public static final CommandLineArgument CMDLINE_TRACE_SERVER_FILE = new CommandLineArgument('o', MocaRegistry.REGKEY_TRACE_FILE);
    public static final CommandLineArgument CMDLINE_TRACE_SERVER_LEVEL = new CommandLineArgument('t', MocaRegistry.REGKEY_TRACE_LEVEL);
    
    public static final CommandLineArgument CMDLINE_JOBS_INHIBIT = new CommandLineArgument('T', MocaRegistry.REGKEY_SERVER_INHIBIT_TASKS, "true");
    public static final CommandLineArgument CMDLINE_TASKS_INHIBIT = new CommandLineArgument('J', MocaRegistry.REGKEY_SERVER_INHIBIT_JOBS, "true");
    
    public static final String CMDLINE_FORMAT = "p:r:P:m:o:JTt;vhR";
    static final CommandLineArgument[] CMDLINE_ARGS = { CMDLINE_PORT, CMDLINE_RMI_PORT, CMDLINE_SOCKET_PORT, 
            CMDLINE_MEMORY_FILE, CMDLINE_TRACE_SERVER_FILE, 
            CMDLINE_JOBS_INHIBIT, CMDLINE_TASKS_INHIBIT };
}
