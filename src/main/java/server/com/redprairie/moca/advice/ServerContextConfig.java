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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import com.redprairie.mad.client.MadHistogramWithContext;
import com.redprairie.mad.client.MadMetrics;
import com.redprairie.mad.client.MadTimer;
import com.redprairie.mad.client.MadTimerContext;
import com.redprairie.mad.client.MadTimerWithContext;
import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaConstants;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.TooManyRowsException;
import com.redprairie.moca.db.QueryHook;
import com.redprairie.moca.mad.MonitoringUtils;
import com.redprairie.moca.probes.connections.DatabaseConnectionSummaryProbes;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.BindMode;
import com.redprairie.moca.server.db.DBAdapter;
import com.redprairie.moca.server.db.JDBCAdapter;
import com.redprairie.moca.server.db.MocaTransaction;
import com.redprairie.moca.server.db.nodb.NoDBAdapter;
import com.redprairie.moca.server.dispatch.MessageResolver;
import com.redprairie.moca.server.exec.ArgumentSource;
import com.redprairie.moca.server.exec.DefaultServerContext;
import com.redprairie.moca.server.exec.RemoteConnectionFactory;
import com.redprairie.moca.server.exec.RequestContext;
import com.redprairie.moca.server.exec.ScriptAdapter;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SessionContext;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.legacy.NativeAdapterFactory;
import com.redprairie.moca.server.profile.CommandPath;
import com.redprairie.moca.server.profile.CommandUsage;
import com.redprairie.moca.server.repository.CommandRepository;
import com.redprairie.moca.util.MocaUtils;

/**
 * This controls the server context creation.  It uses IOC to obtain information
 * of the running instance to supply things such as the data stack, the last
 * SQL executed among other things.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class ServerContextConfig {
    
    /**
     * This method controls the type of database adapter returned.  If there
     * is no database driver found in the system context this will return
     * a NoDBAdapter which will error on any non commit/rollback access to
     * the database.
     * @param system The system context to get the configuration values from
     * @return The DBAdapter that was created
     * @throws SystemConfigurationException If there was a problem creating
     *         the Database Adapter
     */
    public static DBAdapter dbAdapter(SystemContext system, QueryHook queryHook) 
            throws SystemConfigurationException {
        
        String dbDriver = system.getConfigurationElement(
                MocaRegistry.REGKEY_DB_DRIVER);
        
        // If the db connection was not provided then use a no db version
        if (dbDriver == null || dbDriver.trim().isEmpty()) {
            return new NoDBAdapter();
        }
        
        try {
            
            return new JDBCAdapter(dbDriver, 
                    system.getConfigurationElement(MocaRegistry.REGKEY_DB_URL), 
                    system.getConfigurationElement(MocaRegistry.REGKEY_DB_USERNAME), 
                    system.getConfigurationElement(MocaRegistry.REGKEY_DB_PASSWORD),
                    system, queryHook);
        }
        catch (SQLException e) {
            throw new SystemConfigurationException(
                "Error initializing JDBC adapter: " + e, e);
        }
    }
    
    /**
     * This method controls what ServerContext is created when retrieving
     * it from the Spring Config class.  It will create a JMX bean that will
     * be able to query into the server context to obtain information pertaining
     * to the running session.  It will also allow us to proxy out other
     * components of the default server context to allow visibility to when
     * things are actually called.
     * @param scriptAdapter The script adapater to handle script calls
     * @param dbAdapter The database adapter to handle database calls
     * @param session The current session context
     * @param sys The current system context
     * @param request The request context
     * @param componentRespository The current component repository
     * @param nativePool The native process pool
     * @param stats The command usage statistics
     * @param blacklistedArgs The list of blacklisted argument names
     * @return A new server context
     * @throws Exception 
     */
    public static ServerContext serverContext(ScriptAdapter scriptAdapter,
            DBAdapter dbAdapter, final SessionContext session, SystemContext sys,
            RequestContext request, CommandRepository componentRespository,
            NativeAdapterFactory nativePool, 
            CommandUsage stats, Collection<String> blacklistedArgs, 
            MessageResolver messageResolver, 
            RemoteConnectionFactory connectionFactory) {
        
        SessionAdministrationManager manager = 
            (SessionAdministrationManager)sys.getAttribute(
                SessionAdministrationManagerBean.class.getName());
        ServerContext proxyContext;
        if (session != null && manager != null) {
            
            final AtomicReference<ServerContextAdministration> reference = 
                new AtomicReference<ServerContextAdministration>();
            
            // We want to proxy around the db adapter so that we can tell
            // when an SQL command is executed
            DBAdapter proxiedDbAdapter = dbAdapter;
            if (dbAdapter != null) {
                _logger.debug(MocaUtils.concat("Wrapping ", dbAdapter));
                proxiedDbAdapter = new ForwardingDBAdapter(dbAdapter) {
                    /**
                     * Override to provide monitoring insight:
                     * 1) Record the last sql statement
                     * 2) TooManyRows Exceptions will send JMX Notifications
                     * 3) Timer around the rate/times of all SQL executions for a high level overview
                     * 4) Histogram around the size of result sets being returned from the database
                     * @see com.redprairie.moca.advice.ForwardingDBAdapter#executeSQL(com.redprairie.moca.server.db.MocaTransaction, java.lang.String, com.redprairie.moca.server.db.BindList, com.redprairie.moca.server.db.BindMode, boolean, com.redprairie.moca.server.profile.CommandPath)
                     */
                    @Override
                    public EditableResults executeSQL(
                        ArgumentSource argumentSource, MocaTransaction tx,
                        String sqlStatement, BindList bindList, BindMode mode,
                        boolean ignoreResults, CommandPath commandPath)
                            throws SQLException, MocaException {
                        ServerContextAdministration admin = reference.get();
                        if (admin != null) {
                            admin.setLastSqlStatement(sqlStatement);
                        }
                        
                        MadTimerContext tCtx = _sqlTimer.time(sqlStatement);
                        EditableResults res = null;
                        try {
                            res = super.executeSQL(argumentSource, tx,
                                sqlStatement, bindList, mode, ignoreResults,
                                commandPath);
                            
                            if (res != null && res.getRowCount() > 0) {
                                _sqlResultSizes.update(res.getRowCount(), sqlStatement);
                            }
                            return res;
                        }
                        catch (TooManyRowsException e) {
                            // Send out a JMX notification about the query row limit
                            MadMetrics.getFactory().sendNotification("moca.query-row-limit",
                                MocaUtils.concat("Limit of ", e.getQueryLimit(), " rows exceeded ",
                                    StringUtils.hasText(session.getUserId()) ? "by user " + session.getUserId() : "",
                                            "\nSQL statement: ", sqlStatement));
                            
                            _sqlResultSizes.update((int) e.getQueryLimit(), sqlStatement);
                            throw e;
                        }
                        finally {
                            tCtx.stop();
                        }
                    }
                    
                    // @see com.redprairie.moca.server.db.DBAdapter#getConnection(com.redprairie.moca.server.db.MocaTransaction, com.redprairie.moca.server.profile.CommandPath)
                    @Override
                    public Connection getConnection(MocaTransaction tx, CommandPath commandPath) throws SQLException {
                        // A metric for the time it takes to get a connection
                        // This won't include timeouts
                        MadTimerContext tCtx = _getConnectionTimer.time();
                        Connection conn = super.getConnection(tx, commandPath);
                        tCtx.stop();
                        return conn;
                    }
                };
            }
            
            DefaultServerContext actualServerContext = new DefaultServerContext(
                    scriptAdapter, proxiedDbAdapter, session, sys, request, 
                    componentRespository, nativePool, stats, blacklistedArgs,
                    messageResolver, connectionFactory);

            _logger.debug(MocaUtils.concat("Wrapping ", actualServerContext));
            
            final String client = request.getVariable(MocaConstants.WEB_CLIENT_ADDR);
            
            // If the client is null that means we are running in server
            // mode, if so we need to capture commands from the Moca Context
            // instead of the Server Context
            if (client == null) {
                _logger.debug("Running in Server Mode, adding additional proxy " +
                                "for Moca Context");
                proxyContext = new OurServerContext(actualServerContext, 
                    reference) {
                    public MocaContext getComponentContext() {
                        return new OurMocaContext(super.getComponentContext(),
                            reference);
                    }
                };
            }
            else {
                proxyContext = new OurServerContext(actualServerContext, 
                    reference);
            }
            
            Thread currentThread = Thread.currentThread();
            
            SessionAdministration sessionAdmin = manager.createSession(session);
            
            ServerContextAdministration dynamicBean = 
                new ServerContextAdministration(proxyContext, sessionAdmin); 
            
            manager.registerSessionThread(sessionAdmin, currentThread.getId(), 
                dynamicBean);
            
            reference.set(dynamicBean);
            
            // Set the client ip
            dynamicBean.setClientIpAddress(client);
        }
        else {
            proxyContext = new DefaultServerContext(
                    scriptAdapter, dbAdapter, session, sys, request, 
                    componentRespository, nativePool, 
                    stats, blacklistedArgs, messageResolver, connectionFactory);
        }
        
        return proxyContext;
    }
    
    public static void unregisterSession(String sessionId) {
        SessionAdministrationManager manager = 
            (SessionAdministrationManager)ServerUtils.globalContext().getAttribute(
                SessionAdministrationManagerBean.class.getName());
        
        manager.closeSession(sessionId);
    }
    
    private static class OurMocaContext extends ForwardingMocaContext {
        
        public OurMocaContext(MocaContext context, 
            AtomicReference<ServerContextAdministration> ref) {
            super(context);
            _reference = ref;
        }

        // @see com.redprairie.moca.advice.ForwardingMocaContext#executeCommand(java.lang.String)
        @Override
        public MocaResults executeCommand(String command)
                throws MocaException {
            handleCommandExecution(command);
            return super.executeCommand(command);
        }

        // @see com.redprairie.moca.advice.ForwardingMocaContext#executeInline(java.lang.String)
        @Override
        public MocaResults executeInline(String command)
                throws MocaException {
            handleCommandExecution(command);
            return super.executeInline(command);
        }

        // @see com.redprairie.moca.advice.ForwardingMocaContext#executeCommand(java.lang.String, java.util.Map)
        @Override
        public MocaResults executeCommand(String command,
            Map<String, ?> args) throws MocaException {
            handleCommandExecution(command);
            return super.executeCommand(command, args);
        }

        // @see com.redprairie.moca.advice.ForwardingMocaContext#executeInline(java.lang.String, java.util.Map)
        @Override
        public MocaResults executeInline(String command,
            Map<String, ?> args) throws MocaException {
            handleCommandExecution(command);
            return super.executeInline(command, args);
        }

        // @see com.redprairie.moca.advice.ForwardingMocaContext#executeCommand(java.lang.String, com.redprairie.moca.MocaArgument[])
        @Override
        public MocaResults executeCommand(String command,
            MocaArgument... args) throws MocaException {
            handleCommandExecution(command);
            return super.executeCommand(command, args);
        }

        // @see com.redprairie.moca.advice.ForwardingMocaContext#executeInline(java.lang.String, com.redprairie.moca.MocaArgument[])
        @Override
        public MocaResults executeInline(String command,
            MocaArgument... args) throws MocaException {
            handleCommandExecution(command);
            return super.executeInline(command, args);
        }
        
        private void handleCommandExecution(String command) {
            ServerContextAdministration admin = _reference.get();
            
            if (admin != null) {
                admin.setLastCommand(command);
            }
        }
        
        private final AtomicReference<ServerContextAdministration> _reference;
    }
    
    private static class OurServerContext extends ForwardingServerContext {

        /**
         * @param context
         */
        public OurServerContext(ServerContext context, 
            AtomicReference<ServerContextAdministration> ref) {
            super(context);
            _ref = ref;
        }

        // @see com.redprairie.moca.advice.ForwardingServerContext#close()
        @Override
        public void close() {
            if (!_closed.getAndSet(true)) {
                ServerContextAdministration admin = _ref.get();
                if (admin != null) {
                    // If it is there force it to be inactive, since we don't want
                    // to rely on the garbage collector
                    admin.setObject(null);
                }
                
                SessionContext session = super.getSession();
                
                if (session != null) {
                    unregisterSession(session.getSessionId());
                }
            }
            
            super.close();
        }
        
        // @see com.redprairie.moca.advice.ForwardingServerContext#executeCommandWithRemoteContext(java.lang.String, java.util.Collection, java.util.Collection)
        @Override
        public MocaResults executeCommandWithRemoteContext(String command,
            Collection<MocaArgument> context, Collection<MocaArgument> args)
                throws MocaException {
            // If the command is longer than 1 MB then trim it down a bit.
            // Each character is 2 bytes so 1024 * 1024 / 2 = 524288
            final String trimmedCommand = command.length() > 524288 ? command.substring(0, 524287) : command;
            
            ServerContextAdministration admin = _ref.get();
            if (admin != null) {
                admin.setLastCommand(trimmedCommand);
            }
            return super.executeCommandWithRemoteContext(command, context, args);
        }
        
        protected void finalize() throws Throwable {
            try{
                close();
            } finally {
                super.finalize();
            }
        };
        
        protected final AtomicReference<ServerContextAdministration> _ref;
        protected final AtomicBoolean _closed = new AtomicBoolean(false);
    }
   
    // Monitoring probes
    private static final MadTimer _getConnectionTimer = MadMetrics.getFactory().newTimer(
        MonitoringUtils.MOCA_GROUP_NAME, DatabaseConnectionSummaryProbes.METRIC_TYPE_DB_CONN,
        "get-connection-timer", "Timers", TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
    
    private static final MadTimerWithContext _sqlTimer = MadMetrics.getFactory().newTimerWithContext(MonitoringUtils.MOCA_GROUP_NAME,
        "SQL-Executions", "all-requests-timer", TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
    
    private static final MadHistogramWithContext _sqlResultSizes = MadMetrics.getFactory().newHistogramWithContext(MonitoringUtils.MOCA_GROUP_NAME,
        "SQL-Executions", "result-set-sizes");
    
    public final static Logger _logger = LogManager.getLogger(
            ServerContextConfig.class);
}
