/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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

package com.redprairie.moca.server.db;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.Builder;
import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.PagedResults;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.TooManyRowsException;
import com.redprairie.moca.db.NullQueryHook;
import com.redprairie.moca.db.QueryHook;
import com.redprairie.moca.exceptions.MocaDBException;
import com.redprairie.moca.exceptions.UnexpectedException;
import com.redprairie.moca.exceptions.UniqueConstraintException;
import com.redprairie.moca.pool.PoolException;
import com.redprairie.moca.pool.Validator;
import com.redprairie.moca.pool.validators.BasePoolHandler;
import com.redprairie.moca.pool.validators.ForwardingValidator;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.TransactionManagerUtils;
import com.redprairie.moca.server.db.exceptions.DatabaseInterruptedException;
import com.redprairie.moca.server.db.exceptions.DeadlockException;
import com.redprairie.moca.server.db.exceptions.EndOfChannelException;
import com.redprairie.moca.server.db.exceptions.InvalidCursorException;
import com.redprairie.moca.server.db.exceptions.LockTimeoutException;
import com.redprairie.moca.server.db.exceptions.NotConnectedException;
import com.redprairie.moca.server.db.jdbc.ConnectionManager;
import com.redprairie.moca.server.db.jdbc.ConnectionPoolDataSource;
import com.redprairie.moca.server.db.jdbc.ConnectionPoolStatistics;
import com.redprairie.moca.server.db.jdbc.ConnectionStatistics;
import com.redprairie.moca.server.db.jdbc.DriverDataSource;
import com.redprairie.moca.server.db.jdbc.JDBCTransaction;
import com.redprairie.moca.server.db.jdbc.MocaDataSource;
import com.redprairie.moca.server.db.jdbc.PropertyProvider;
import com.redprairie.moca.server.db.jdbc.SQLPoolException;
import com.redprairie.moca.server.db.translate.BaseDialect;
import com.redprairie.moca.server.db.translate.LimitHandler;
import com.redprairie.moca.server.db.translate.OracleDialect;
import com.redprairie.moca.server.db.translate.SQLDialect;
import com.redprairie.moca.server.db.translate.SQLServerDialect;
import com.redprairie.moca.server.db.translate.TranslationOptions;
import com.redprairie.moca.server.exec.ArgumentSource;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.log.render.SecureLogMessage;
import com.redprairie.moca.server.profile.CommandPath;
import com.redprairie.moca.util.MocaIOException;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.moca.util.NonMocaDaemonThreadFactory;
import com.redprairie.util.ClassUtils;

/**
 * A class used to support MOCA server components access to databases via the
 * dblib construct. This is intended to be an adapter layer between MOCA's dblib
 * and JDBC. MOCA's mocajava library will keep a single instance of this class
 * and use it for all database interactions.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All rights reserved.
 * </pre>
 * 
 * </b>
 * 
 * @author Derek Inksetter
 * @version $Revision$
 */
public class JDBCAdapter implements DBAdapter {
    
    public JDBCAdapter(String driverClassName, String dbUrl, String dbUser,
                       String dbPassword, SystemContext system,
                       QueryHook queryHook)
            throws SQLException, SystemConfigurationException {

        // Check for database type, based on the URL used.
        _logger.debug(MocaUtils.concat("Connecting to database: ", dbUrl));
        
        String dbDialect = system.getConfigurationElement(MocaRegistry.REGKEY_DB_DIALECT);
        if (dbDialect != null) {
            _dialect = ClassUtils.instantiateClass(dbDialect, SQLDialect.class);
        }
        else {
            _dialect = guessDialect(dbUrl);
        }

        _dbType = _dialect.getDBType();
        
        // Set up the connection pool
        int minIdleConnections;
        String tmp = system.getConfigurationElement(
            MocaRegistry.REGKEY_DB_MIN_IDLE_CONNECTIONS, 
            MocaRegistry.REGKEY_DB_MIN_IDLE_CONNECTIONS_DEFAULT);
        try {
            minIdleConnections = Integer.parseInt(tmp);
        }
        catch (NumberFormatException e) {
            throw new SystemConfigurationException("Unable to parse max connections setting: " + tmp + ": " + e);
        }
        
        int maxConnections;
        tmp = system.getConfigurationElement(MocaRegistry.REGKEY_DB_MAX_CONNECTIONS, 
            MocaRegistry.REGKEY_DB_MAX_CONNECTIONS_DEFAULT);
        try {
            maxConnections = Integer.parseInt(tmp);
        }
        catch (NumberFormatException e) {
            throw new SystemConfigurationException("Unable to parse max connections setting: " + tmp + ": " + e);
        }

        int connTimeout;
        tmp = system.getConfigurationElement(MocaRegistry.REGKEY_DB_CONNECTION_TIMEOUT, "30");
        try {
            connTimeout = Integer.parseInt(tmp);
        }
        catch (NumberFormatException e) {
            throw new SystemConfigurationException("Unable to parse connection timeout setting: " + tmp + ": " + e);
        }
        
        String dbIsolation = system.getConfigurationElement(MocaRegistry.REGKEY_DB_ISOLATION);

        _logger.debug(new SecureLogMessage(
            "Connection Credentials: User(%1$s) Password(%2$s)", dbUser, dbPassword));

        // Build a pool name from the environment name and node id.
        String envname = system.getVariable("MOCA_ENVNAME");
        String taskId = system.getVariable("MOCA_TASK_ID");
        // TODO: need to do something to mark the server since it will always be unique, but maybe do a random value for other processes?  This is because some transaction managers don't allow reuse of names across processes
        final String poolName = ((envname != null) ? envname : "RedPrairie") +
                       ((taskId != null) ? "-" + taskId : "");
        
        // Now we create the data source pool
        Validator<Connection> validator = getPoolValidator(system, _dialect, dbIsolation);
        
        PropertyProvider propProvider = _dialect.getPropertyProvider();
        
        Class<?> driverClass;
        try {
            driverClass = Class.forName(driverClassName);
        }
        catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class " + driverClassName + 
                " not found", e);
        }
        
        ConnectionSettings settings = new ConnectionSettings(driverClassName, 
            dbUrl, dbUser, dbPassword);
        
        String name = _connectionSettings.get(settings);
        boolean shouldRegister = false;
        
        if (name == null) {
            long increment = _connectionNumber.getAndIncrement();
            if (increment == 0) {
                name = "default";
            }
            else {
                name = "default" + increment;
            }
            String previousName = _connectionSettings.putIfAbsent(settings, name);
            if (previousName != null) {
                name = previousName;
            }
            else {
                shouldRegister = true;
            }
        }
        
        String mocaUrl = "jdbc:moca:" + name;
        
        if (shouldRegister) {
            CommonDataSource source;
            try {
                if (XADataSource.class.isAssignableFrom(driverClass)) {
                    source = (XADataSource)driverClass.newInstance();
                }
                else if (DataSource.class.isAssignableFrom(driverClass)) {
                    source = (DataSource)driverClass.newInstance();
                }
                else if (Driver.class.isAssignableFrom(driverClass)) {
                    Driver driver = (Driver)driverClass.newInstance();
                    source = new DriverDataSource(dbUrl, driver);
                }
                else {
                    throw new IllegalArgumentException(
                        "Unsupported class provided for Driver/DataSource - " + 
                        driverClassName);
                }
            }
            catch (InstantiationException e) {
                throw new IllegalArgumentException("Class " + driverClassName + 
                    " constructor threw exception", e);
            }
            catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Class " + driverClassName + 
                    " constructor cannot be accessed", e);
            }
            
            Properties props = new Properties();
            if (dbUser != null) {
                props.put("user", dbUser);
            }
            if (dbPassword != null) {
                props.put("password", dbPassword);
            }
            props.put("URL", dbUrl);
            propProvider.initializeDataSourceProperties(source, props);
        
            ConnectionManager.registerDataSource(name, source,
                propProvider, _dialect.getConnectionValidator());
        }
        
        MocaDataSource dataSource = new ConnectionPoolDataSource(connTimeout, 
            TimeUnit.SECONDS, minIdleConnections, maxConnections, 
            new ConnectionBuilder(propProvider, mocaUrl, poolName), validator);
        
        // Attempt to acquire a connection from the pool and give it back.
        Connection conn = dataSource.getConnection();
        conn.close();
        
        _pool = dataSource;
        
        if (queryHook == null) {
            _queryHook = new NullQueryHook();
        }
        else {
            _queryHook = queryHook;
        }
        
        tmp = system.getConfigurationElement(MocaRegistry.REGKEY_SERVER_BIND_LOG);
        if (tmp != null) {
            try {
                _bindLog = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(tmp), true),
                    Charset.forName("UTF-8")));
            }
            catch (IOException e) {
                // ignore errors .. _bindLog = null;
            }
        }

        tmp = system.getConfigurationElement(MocaRegistry.REGKEY_SERVER_QUERY_LIMIT,
                                             MocaRegistry.REGKEY_SERVER_QUERY_LIMIT_DEFAULT);
        if (tmp != null) {
            try {
                _queryLimit = Long.parseLong(tmp);
            }
            catch (NumberFormatException e) {
            }
        }
        
        WeakReference<JDBCAdapter> ref = new WeakReference<JDBCAdapter>(this);
        
        Runtime.getRuntime().addShutdownHook(new JDBCAdapterShutdownHook(ref));
    }
    
    /**
     * This class has to be static so that creating an instance of this won't
     * keep a reference to the outer JDBCAdapter instance
     */
    private static class JDBCAdapterShutdownHook extends Thread {
        public JDBCAdapterShutdownHook(WeakReference<JDBCAdapter> ref) {
            _ref = ref;
        }
        
        // @see java.lang.Thread#run()
        @Override
        public void run() {
            JDBCAdapter adapter = _ref.get();
            if (adapter != null) {
                adapter.logPerformance();
            }
        }
        
        private final WeakReference<JDBCAdapter> _ref;
    }

    @Override
    public void close() throws SQLException {
    }

    @Override
    public EditableResults executeSQL(ArgumentSource argumentSource, 
        MocaTransaction tx, String sqlStatement, BindList bindList, 
        BindMode mode, boolean ignoreResults, CommandPath commandPath)
            throws SQLException, MocaException {
        EditableResults results;
        try {
            results = executeSQLInternal(argumentSource, tx, sqlStatement, 
                bindList, mode, ignoreResults, commandPath);
        }
        catch (SQLException e) {
            
            MocaDBException mocaDbException = translateException(e);
            
            // If the SQL Exception translates to UniqueConstraintException
            // just throw it right away
            if (mocaDbException instanceof UniqueConstraintException) {
                throw mocaDbException;
            }
            
            // If the error code wasn't one where it would have parsed
            // correctly then we can retry it without autobind
            if ((mode == BindMode.AUTO || mode == BindMode.VAR) &&
                !isNormalException(mocaDbException))
            {
                BindMode newMode;
                if (mode == BindMode.AUTO) {
                    _logger.debug("==> Retrying without auto bind...");
                    newMode = BindMode.NONE;
                }
                else {
                    _logger.debug("==> Retrying with bound variables unbound...");
                    newMode = BindMode.UNBIND;
                }
                
                // We still have to pass in the bind list in case if we had
                // @variables to replace
                results = executeSQLInternal(argumentSource, tx, sqlStatement, 
                        bindList, newMode, ignoreResults, commandPath);
                
                // So, if the retry is successful, we need to log that
                // information
                _logger.debug("==> Retry Successful");
                if (_bindLog != null) {
                    synchronized (this) {
                        try {
                            String lineSeparator = System.getProperty("line.separator");
                            _bindLog.write(lineSeparator);
                            _bindLog.write(new Date().toString());
                            _bindLog.write(lineSeparator);
                            BindFailedEvent event = new BindFailedEvent(
                                sqlStatement, commandPath, e.getErrorCode(),
                                e.getSQLState(), e.getMessage());
                            _bindLog.write(event.toString());
                            _bindLog.write(lineSeparator);
                            _bindLog.flush();
                        }
                        catch (IOException e1) {
                            throw new MocaIOException("Could not write to bind log.", e1);
                        }
                    }
                }
            }
            else {
                throw e;
            }
        }
        return results;
    }
    
    //
    // Implementation
    //
 
    private EditableResults executeSQLInternal(ArgumentSource argumentSource, 
        MocaTransaction tx, String sqlStatement, BindList bindList, 
        BindMode mode, boolean ignoreResults, 
            CommandPath commandPath) throws SQLException, MocaException {
        Statement stmt = null;
        ResultSet res = null;
        TranslationOptions translationOptions = null;
        long completeBegin = System.nanoTime();
        long begin, end;

        _queryCount++;

        try {
            Connection conn = _getConnection(tx, commandPath);

            _logger.debug(MocaUtils.concat("Executing SQL: ", sqlStatement));
            QueryHook.QueryAdvisor advisor = _queryHook.getQueryAdvisor();
            
            sqlStatement = advisor.adviseQuery(sqlStatement, bindList);

            begin = System.nanoTime();
            long queryRowLimit = _queryLimit;
            boolean errorOnOverflow = true;
            boolean findTotal = false;
            boolean pagingUsed = false;
            int startRow = 0;
            try {
                if (_dialect != null) {

                    if (bindList == null) {
                        bindList = new BindList();
                    }

                    translationOptions = new TranslationOptions(mode);

                    sqlStatement = _dialect.translateStatement(sqlStatement,
                        bindList, translationOptions);
                    
                    // Handle row limits, both system configured, as well as 
                    // defined in the query
                    int pageRowLimit = 0;
                    if (translationOptions.hasHint("nolimit")) {
                        queryRowLimit = 0L;
                        errorOnOverflow = false;
                    }
                    else if (translationOptions.hasHint("limit")) {
                        // If row limiting/pagination is defined in the query
                        // then we need to translate the query using the dialects LimitHandler
                        String limit = translationOptions.getHintValue("limit");
                        String[] limitFields = limit.split(",", 3);
                        if (limitFields.length == 3) {
                            findTotal = resolveBooleanArgument(argumentSource, limitFields[2]);
                        }
                        
                        if (limitFields.length >= 2) {
                            Integer startInt = resolveIntArgument(argumentSource, limitFields[0]);
                            if (startInt != null) {
                                startRow = startInt;
                                _logger.debug(MocaUtils.concat(
                                    "Start row hint evaluated to: ", startRow));
                            }
                            else {
                                _logger.debug(MocaUtils.concat(
                                    "Start row hint could not be found: ",
                                    limitFields[0], " starting first row"));
                            }
                            Integer limitInt = resolveIntArgument(argumentSource, limitFields[1]);
                            if (limitInt != null) {
                                pageRowLimit = limitInt;
                                _logger.debug(MocaUtils.concat(
                                    "Row limit hint evaluated to: ", pageRowLimit));
                            }
                            else {
                                _logger.debug(MocaUtils.concat(
                                    "Row limit hint could not be found: ",
                                    limitFields[1], " returning rest of rows"));
                            }
                        }
                        else {
                            pageRowLimit = Integer.parseInt(limit);
                        }
                        errorOnOverflow = false;
                    }

                    // See if paging was enabled and that our dialect
                    // actually supports it before transforming the query
                    if ((startRow > 0 || pageRowLimit > 0)) {
                    	if (_dialect.getLimitHandler().supportsLimit()) {
		                    _logger.debug("Paging limits detected, modifying the query to allow for paging - start row: {}, row limit: {}, calculate total: {}",
		                            startRow, pageRowLimit, findTotal);
		                    sqlStatement = _dialect.getLimitHandler().addLimit(sqlStatement, startRow, pageRowLimit, bindList, findTotal);
		                    pagingUsed = true;
                    	}
                    	else {
                    		// INFO level because we should not see this in production systems (Oracle/SQL Server)
                    		// but it would be good to know if we're testing with H2
                    		_logger.info("Paging is not supported for the dialect");
                    	}
                    }
                    
                    _logger.debug(MocaUtils.concat("XLATE: ", sqlStatement));
                }
            }
            finally {
                end = System.nanoTime();
                _translationTime += (end - begin);
            }

            begin = end;
            try {
                if (translationOptions != null) {
                    Collection<String> preStatements = translationOptions.getPreStatements();
                    if (preStatements != null && preStatements.size() > 0) {
                        stmt = conn.createStatement();
                        for (String preSql : preStatements) {
                            boolean isQuery = stmt.execute(preSql);
                            if (isQuery) {
                                res = stmt.getResultSet();
                                res.close();
                                res = null;
                            }
                        }
                        stmt.close();
                        stmt = null;
                    }
                }
                
                // At this point, we have a SQL statement, optionally with named bind variables embedded, Oracle-style
                // I.e. select * from foo where x = :variable

                SQLBinder binder = null;
                if (bindList != null && !bindList.isEmpty()) {
                    _logger.debug("Found passed-in bind list");
                    binder = new SQLBinder(sqlStatement, bindList);
                    // Trace the unbound version of the SQL statement
                    if (_logger.isDebugEnabled()) {
                        _logger.debug(MocaUtils.concat("UNBIND: ", binder.getUnboundStatement()));
                    }
                }

                // There are three modes of operation here. We could be executing a statement with no parameters,
                // we could be executing a statement with in-only parameters, or we could be executing a statement
                // with in/out parameters.
                boolean isQuery;
                if (binder != null && binder.getNames().size() != 0) {

                    PreparedStatement pstmt = _dialect.prepareStatement(conn, binder, bindList);

                    stmt = pstmt;

                    // Execute the SQL
                    _logger.debug("Executing statement with bound parameters");
                    isQuery = runQuery(new _PreparedStatementExecute(pstmt));
                    
                    if (bindList.hasReferences()) {
                        _dialect.updateOutVariables((CallableStatement)pstmt, binder, bindList);
                    }

                }

                else {
                    stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    _logger.debug("Executing statement without parameters");
                    isQuery = runQuery(new _StatementExecute(stmt, sqlStatement));
                }
                // If this is query, there should be a result set.
                if (isQuery) {
                    res = stmt.getResultSet();
                }
                else {
                    int count = stmt.getUpdateCount();
                    _logger.debug(MocaUtils.concat("Statement affected ", count,
                            " rows."));
                    if (count == 0) {
                        if (_updatePattern.matcher(sqlStatement)
                            .lookingAt()) throw new NotFoundException();
                    }
                }
            }
            finally {
                end = System.nanoTime();
                _executeTime += (end - begin);
            }

            begin = end;
            try {
                if (ignoreResults) {
                    // We still need to check for "no rows" error condition
                    if (res != null && !res.next())
                        throw new NotFoundException();
                    else
                        return null;
                }
                else {
                    EditableResults out;
                    if (res != null) {
                        try {
                            int rows = 0;
                            if (pagingUsed) {
                            	PagedResults pagedRes = getPagedResults(res,
                                    conn, sqlStatement, bindList, startRow,
                                    findTotal, advisor, queryRowLimit,
                                    errorOnOverflow);
                                
                                out = pagedRes;
                                rows = out.getRowCount();
                            }
                            else {
                                out = new SimpleResults();
                                rows = MocaUtils.copySqlResults(out, res, 
                                        advisor, 0, queryRowLimit, errorOnOverflow);
                            }
                            
                            _logger.debug(MocaUtils.concat("Query returned ", 
                                    rows, " rows"));
                            if (rows == 0) {
                                throw new NotFoundException(out);
                            }
                        }
                        catch (TooManyRowsException e) {
                            _logger.warn("Maximum rows exceeded in query.  " +
                                    "This limit can be configured through " +
                                    "query-limit in the SERVER section of" +
                                    " the registry.");
                            throw e;
                        }
                    }
                    else {
                        out = new SimpleResults();
                    }
                    return out;
                }
            }
            finally {
                end = System.nanoTime();
                _fetchTime += (end - begin);
            }
        }
        catch (SQLException e) {
            _failureCount++;
            _logger.debug("SQL Exception", e);
            _logger.debug(MocaUtils.concat("SQL Error Code: [", 
                    e.getErrorCode(), "]"));
            _logger.debug(MocaUtils.concat("SQL State: [", e.getSQLState(), "]"));
            if (_dialect != null) {
                throw _dialect.translateSQLException(e);
            }
            else {
                throw e;
            }
        }
        catch (MocaInterruptedException e) {
            // Let the exception propagate up.
            throw e;
        }
        catch (RuntimeException e) {
            _failureCount++;
            _logger.debug("Unexpected Exception", e);
            throw new UnexpectedException(e);
        }
        catch (NotFoundException e) {
            // Not really an error -- don't mess up the logs
            _logger.debug("Throwing NotFoundException");
            throw e;
        }
        catch (MocaException e) {
            _failureCount++;
            _logger.debug("MOCA Exception", e);
            throw e;
        }
        finally {
            if (res != null) {
                try {
                    res.close();
                }
                catch (SQLException e) {
                    _logger.debug(MocaUtils.concat(
                            "Exception (ignored) closing result set: ", e));
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (SQLException e) {
                    _logger.debug(MocaUtils.concat(
                            "Exception (ignored) closing result set: ", e));
                }
            }

            if (translationOptions != null) {
                Collection<String> postStatements = translationOptions.getPostStatements();
                if (postStatements != null && postStatements.size() > 0) {
                    stmt = null;
                    try {
                        Connection conn = _getConnection(tx, commandPath);
                        stmt = conn.createStatement();
                        for (String postSql : postStatements) {
                            boolean isQuery = stmt.execute(postSql);
                            if (isQuery) {
                                res = stmt.getResultSet();
                                res.close();
                                res = null;
                            }
                        }
                    }
                    catch (SQLException e) {
                        _logger.debug("Error executing post-statement", e);
                    }
                    finally {
                        if (stmt != null) {
                            try {
                                stmt.close();
                            }
                            catch (SQLException ignore) {
                            }
                        }
                    }
                }
            }
            
            end = System.nanoTime();
            _perfLogger.debug(MocaUtils.format("Execute Time: %1$g3 s", 
                    ((end - completeBegin) / 1000000000.0)));
            _logger.debug("SQL execution completed");
        }
    }

    // Handles building the PagedResults from the JDBC ResultSet that
    // may contain the total number possible rows if findTotal=true
    // Also handles the case that the startRow was beyond the number of possible
    // rows therefore we have to re-query to calculate the total.
    private PagedResults getPagedResults(ResultSet res, Connection conn,
                                         String limitedSqlStatement,
                                         BindList bindList, int startRow,
                                         boolean findTotal,
                                         QueryHook.QueryAdvisor advisor,
                                         long queryRowLimit,
                                         boolean errorOnOverflow)
            throws SQLException, MocaException {
        LimitHandler limitHandler = _dialect.getLimitHandler();
        PagedResults pagedRes = MocaUtils.buildSqlPagedResults(res, advisor,
            limitHandler, queryRowLimit, errorOnOverflow, findTotal);
        
        // Handle the case that the query didn't return any rows because the paging startRow
        // was higher than the actual rows in the DB, so we re-query just the first row to
        // calculate the total possible number of rows.
        if (pagedRes.getRowCount() == 0
                && findTotal
                && startRow > 0) {
            _logger.debug(
                "No rows were returned with the given start row of {}. {}",
                startRow,
                "Re-querying to calculate the total possible number of rows for paging.");
            
            // Re-bind the limit bind variables to only return the first row
            limitHandler.rebindLimitVariables(0, 1, bindList);
            SQLBinder binder = new SQLBinder(limitedSqlStatement, bindList);
            try (PreparedStatement pstmt = _dialect.prepareStatement(conn, binder, bindList)) {
                runQuery(new _PreparedStatementExecute(pstmt));
                try (ResultSet newRes = pstmt.getResultSet()) {
                    pagedRes = MocaUtils.buildSqlPagedResults(newRes, null, limitHandler,
                        queryRowLimit, errorOnOverflow, findTotal);
                    // Trim the returned rows as we just want the total possible rows meta data
                    while (pagedRes.next()) {
                        pagedRes.removeRow();
                    }
                }
            }
        }

        return pagedRes;
    }

    /**
     * @param argumentSource
     * @param argument
     * @return The Integer if it evaluates to one.
     * @throws NumberFormatException Ths is thrown if the argument is provided
     *         and it neither is a variable replacement (begins with @) or it
     *         cannot be formatted to a Integer.
     */
    private Integer resolveIntArgument(ArgumentSource argumentSource, 
        String argument) throws NumberFormatException {
        Integer value = null;
        if (argument.startsWith("@")) {
            MocaValue mocaValue = argumentSource.getVariable(argument.substring(1), true);
            if (mocaValue != null) {
                // MOCA value type doesn't support long currently, we may
                // want to change this to long in the future
                value = mocaValue.asInt();
            }
        }
        else {
            value = Integer.parseInt(argument);
        }
        return value;
    }
    
    private boolean resolveBooleanArgument(ArgumentSource argumentSource, 
            final String argument) {
            boolean resolvedValue = false;
            if (argument.startsWith("@")) {
                MocaValue mocaValue = argumentSource.getVariable(argument.substring(1), true);
                if (mocaValue != null) {
                    // Special case so the user can pass the String "false" and it evaluates
                    // correctly because asBoolean() treats any non-empty string as TRUE
                    if (mocaValue.getType() == MocaType.STRING) {
                        resolvedValue = "true".equalsIgnoreCase(mocaValue.asString());
                    }
                    else {
                        resolvedValue = mocaValue.asBoolean();
                    }
                }
            }
            else {
                resolvedValue = "true".equalsIgnoreCase(argument);
            }
            
            return resolvedValue;
        }
   
    // This is a list of exceptions that could normally occur on any command.  Any other, more general
    // exception might trigger a retry.
    private boolean isNormalException(MocaDBException e) {
        return ((e instanceof LockTimeoutException)
                || (e instanceof DeadlockException)
                || (e instanceof InvalidCursorException)
                || (e instanceof DatabaseInterruptedException)
                || (e instanceof EndOfChannelException)
                || (e instanceof NotConnectedException));
    }

    // @see com.redprairie.moca.server.db.DBAdapter#getConnection(com.redprairie.moca.server.db.MocaTransaction)
    public Connection getConnection(MocaTransaction tx, CommandPath commandPath) throws SQLException {
        Connection conn = _getConnection(tx, commandPath);
        Class<?> tmpClass = conn.getClass();

        Class<?>[] interfaces = ClassUtils.getInterfaces(conn.getClass());
        Connection proxiedConn = (Connection) Proxy.newProxyInstance(tmpClass.getClassLoader(),
            interfaces, new _ConnectionHandler(conn));
        return proxiedConn;
    }
    

    // @see com.redprairie.moca.server.db.DBAdapter#getConnectionStatistics()
    public Map<Connection, ConnectionStatistics> getConnectionStatistics() {
        Map<Connection, ConnectionStatistics> connStats = 
            new HashMap<Connection, ConnectionStatistics>();    
        
        // Add statistics for the busy connections.
        Map<Connection, ConnectionStatistics> busyMap = _pool.getBusyConnections();
        connStats.putAll(busyMap);
        
        // Add statistics for the free connections.
        Map<Connection, ConnectionStatistics> idleMap = _pool.getIdleConnections();
        connStats.putAll(idleMap); 
        
        return connStats;
    }
    
    // @see com.redprairie.moca.server.db.DBAdapter#getConnectionPoolStatistics()
    public ConnectionPoolStatistics getConnectionPoolStatistics() {
        return _pool.getConnectionPoolStatistics();
    }

    // @see com.redprairie.moca.server.db.DBAdapter#getDBType()
    public DBType getDBType() {
        return _dbType;
    }

    // @see com.redprairie.moca.server.db.DBAdapter#getNextSequenceValue(com.redprairie.moca.server.db.MocaTransaction, java.lang.String)
    public String getNextSequenceValue(MocaTransaction tx, String sequence, 
            CommandPath commandPath)
            throws SQLException {
        _logger.debug("Generating Sequence for [" + sequence + "]");

        if (_dialect == null) {
            throw new SQLException(
                "Unknown database type...sequences not supported");
        }

        return _dialect.getSequenceValue(sequence, _getConnection(tx, 
                commandPath));
    }

    // @see com.redprairie.moca.server.db.DBAdapter#newTransaction()
    public JDBCTransaction newTransaction() {
        return new JDBCTransaction(_pool, 
            TransactionManagerUtils.getManager());
    }

    // @see com.redprairie.moca.server.db.DBAdapter#logPerformance()
    public void logPerformance() {
        
        _perfLogger.debug(MocaUtils.concat("Total Queries: ", _queryCount));
        _perfLogger.debug(MocaUtils.concat("Error Queries: ", _failureCount));
        _perfLogger.debug(MocaUtils.concat("Translation Time: ", (_translationTime / 1000000.0),
                "ms"));
        _perfLogger.debug(MocaUtils.concat("Execute Time: ", (_executeTime / 1000000.0), "ms"));
        _perfLogger.debug(MocaUtils.concat("Fetch Time: ", (_fetchTime / 1000000.0), "ms"));
        _perfLogger.debug(MocaUtils.concat("Pool Statistics: ", _pool));

        _queryCount = 0;
        _failureCount = 0;
        _translationTime = 0L;
        _executeTime = 0L;
        _fetchTime = 0L;
    }
    
    @Override
    public DataSource getDataSource() {
        return _pool;
    }

    //
    // Implementation
    //
    private static Pattern _updatePattern = Pattern.compile(
        "(/\\*.*?\\*/|\\s)*\\b(update|delete)\\b", Pattern.CASE_INSENSITIVE);

    /**
     * InvocationHandler to ensure that users don't try to close, commit or roll
     * back our Connection object. This handler also performs MOCA tracing for
     * JDBC SQL access, when tracing is enabled.
     */
    private class _ConnectionHandler implements InvocationHandler {
        
        // @see java.lang.Object#hashCode()
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((_conn == null) ? 0 : _conn.hashCode());
            return result;
        }

        // @see java.lang.Object#equals(java.lang.Object)
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            
            // We have to deproxy it if possible
            _ConnectionHandler otherProxy = null;
            if (obj instanceof _ConnectionHandler) {
                otherProxy = (_ConnectionHandler) obj;
            }
            else if (Proxy.isProxyClass(obj.getClass())) {
                InvocationHandler ih = Proxy.getInvocationHandler(obj);
                if (!(ih instanceof _ConnectionHandler)) {
                    return false;
                }
                otherProxy = (_ConnectionHandler) ih;
            }
            else {
                // Not a valid comparison...
                return false;
            }
            
            if (!getOuterType().equals(otherProxy.getOuterType())) return false;
            if (_conn == null) {
                if (otherProxy._conn != null) return false;
            }
            else if (_conn != otherProxy._conn && !(_conn.equals(otherProxy._conn))) return false;
            return true;
        }

        // javadoc inherited from interface
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            String methodName = method.getName();
            if (methodName.equals("close") || methodName.equals("rollback")
                    || methodName.equals("commit")) {
                throw new RuntimeException(
                    "Component attempted to call forbidden method");
            }
            else if (methodName.equals("equals")) {
                // The first argument should be the other one for the equals
                return equals(args[0]);
            }
            else if (methodName.equals("hashCode")) {
                return hashCode();
            }
            else {
                boolean tracing = _logger.isDebugEnabled();
                if (tracing) {
                    _logger.debug(_buildTraceMessage("Connection", method
                        .getName(), args));
                }
                try {
                    long startTime = System.nanoTime();
                    Object result = method.invoke(_conn, args);

                    // If the result of the call is a statement, trace those
                    // methods, too.
                    if (result instanceof Statement) {
                        Class<? extends Object> resultClass = result.getClass();
                        result = Proxy.newProxyInstance(resultClass
                            .getClassLoader(), ClassUtils
                            .getInterfaces(resultClass), new _StatementHandler(result, startTime));
                    }
                    return result;
                }
                catch (InvocationTargetException e) {
                    if (tracing) {
                        _logger.debug("JDBC: Exception: "
                                + e.getTargetException());
                    }
                    throw e.getTargetException();
                }
            }
        }

        private _ConnectionHandler(Connection conn) {
            _conn = conn;
        }

        private final Connection _conn;

        private JDBCAdapter getOuterType() {
            return JDBCAdapter.this;
        }
    }

    /**
     * InvocationHandler to perform MOCA tracing for JDBC SQL access, when
     * tracing is enabled.
     */
    private class _StatementHandler implements InvocationHandler {

        // javadoc inherited from interface
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            boolean tracing = _logger.isDebugEnabled();
            if (tracing) {
                _logger.debug(_buildTraceMessage("...Statement", method
                    .getName(), args));
                _logger.debug("==> J_PERF: " + ((System.nanoTime() - _start)/1e+9));
            }
            try {
                return method.invoke(_target, args);
            }
            catch (InvocationTargetException e) {
                if (tracing) {
                    _logger.debug("JDBC: Exception: " + e.getTargetException());
                }
                throw e.getTargetException();
            }
        }

        private _StatementHandler(Object target, long start) {
            _target = target;
            _start = start;
        }

        private final Object _target;
        private final long _start;
    }
    
    private class _StatementExecute implements Callable<Boolean> {
        private _StatementExecute(Statement s, String sql) {
            _s = s;
            _sql = sql;
        }
        
        // @see java.util.concurrent.Callable#call()
        @Override
        public Boolean call() throws Exception {
            return _s.execute(_sql);
        }
        
        public void cancel() throws SQLException {
            _s.cancel();
        }

        protected final Statement _s;
        private final String _sql;
    }
    
    private class _PreparedStatementExecute extends _StatementExecute  {
        private _PreparedStatementExecute(PreparedStatement s) {
            super(s, null);
        }
        
        // @see java.util.concurrent.Callable#call()
        @Override
        public Boolean call() throws Exception {
            return ((PreparedStatement)_s).execute();
        }
    }
    
    /**
     * Factory method to return a SQL translator implementation.  The url
     * is inspected to determine the correct translator to use.
     * 
     * @param url the database URL used to connect to the database.
     * @return an implementation of SQLTranslator.
     */
    private static SQLDialect guessDialect(String url) {
        if (url.startsWith("jdbc:oracle")) {
            return new OracleDialect();
        }
        else if (url.indexOf("sqlserver") >= 0) {
            return new SQLServerDialect();
        }
        else {
            throw new IllegalArgumentException("Unknown database type: " + url);
        }
    }

    private boolean runQuery(_StatementExecute exec) throws SQLException, MocaException {
        Future<Boolean> execResult = _executor.submit(exec);
        
        try {
            return execResult.get();
        }
        catch (InterruptedException e) {
            _logger.debug("SQL Statement interrupted, attempting to cancel the query");
            exec.cancel();
            throw new MocaInterruptedException(e);
        }
        catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SQLException) {
                throw (SQLException)cause;
            }
            else {
                throw new UnexpectedException(e);
            }
        }
    }
    

    // Builds a trace message for logging JDBC calls and their arguments.
    private String _buildTraceMessage(String className, String method,
                                      Object[] args) {
        StringBuilder traceMsg = new StringBuilder("JDBC: ");
        traceMsg.append(className).append('.').append(method);
        traceMsg.append('(');
        if (args != null) {
            for (int a = 0; a < args.length; a++) {
                if (a > 0) traceMsg.append(", ");
                traceMsg.append(args[a]);
            }
        }
        traceMsg.append(')');
        return traceMsg.toString();
    }

    private Connection _getConnection(MocaTransaction tx, CommandPath path) throws SQLException {
        // We then have to update the stats with the new command path
        Connection proxyConn = tx.getConnection();
        Map<Connection, ConnectionStatistics> stats = getConnectionStatistics();
        
        // If we have a proxy, then we have to unwrap it get access to the
        // map key
        if (Proxy.isProxyClass(proxyConn.getClass())) {
            Object invocHandler = Proxy.getInvocationHandler(proxyConn);
            
            if (invocHandler instanceof BasePoolHandler) {
                @SuppressWarnings("unchecked")
                BasePoolHandler<Connection> actualHandler = 
                        (BasePoolHandler<Connection>)invocHandler;
                Connection realConnection = actualHandler.getRealObject();
                
                stats.get(realConnection).setLastCommandPath(path);
            }
        }
        
        return proxyConn;
    }

    /**
     * Translates the exception to the specific type of MocaDBException
     * @param sqlException The sql exception that we need to translate
     * @return The moca db exception that is wrapping the sql exception
     */
    private MocaDBException translateException(SQLException sqlException) {
        int errorCode = sqlException.getErrorCode();
        // First we check the standard numbers we converted from the translator
        // We currently only translate unique constraint, lock timeout and 
        // dead lock
        switch (errorCode) {
        case BaseDialect.STD_UNIQUE_CONS_CODE:
            return new UniqueConstraintException(sqlException);
        case BaseDialect.STD_LOCK_TIMEOUT_CODE:
            return new LockTimeoutException(sqlException);
        case BaseDialect.STD_DEADLOCK_CODE:
            return new DeadlockException(sqlException);
        default:
            switch (_dbType) {
            // We check some extra ones for oracle specifically.
            case ORACLE:
                switch (errorCode) {
                case 1001:
                    return new InvalidCursorException(sqlException);
                case 1013:
                    return new DatabaseInterruptedException(sqlException);
                case 3113:
                    return new EndOfChannelException(sqlException);
                case 3114:
                    return new NotConnectedException(sqlException);
                default:
                    return new MocaDBException(sqlException);
                }
            default:
                return new MocaDBException(sqlException);
            }         
        } 
    }
    
    private static class ConnectionSettings {
        
        /**
         * @param _driver
         * @param _url
         * @param _user
         * @param _password
         */
        public ConnectionSettings(String driver, String url, String user,
                String password) {
            _driver = driver;
            _url = url;
            _user = user;
            _password = password;
        }
        
        // @see java.lang.Object#hashCode()
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((_driver == null) ? 0 : _driver.hashCode());
            result = prime * result
                    + ((_password == null) ? 0 : _password.hashCode());
            result = prime * result + ((_url == null) ? 0 : _url.hashCode());
            result = prime * result + ((_user == null) ? 0 : _user.hashCode());
            return result;
        }
        // @see java.lang.Object#equals(java.lang.Object)
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            ConnectionSettings other = (ConnectionSettings) obj;
            if (_driver == null) {
                if (other._driver != null) return false;
            }
            else if (!_driver.equals(other._driver)) return false;
            if (_password == null) {
                if (other._password != null) return false;
            }
            else if (!_password.equals(other._password)) return false;
            if (_url == null) {
                if (other._url != null) return false;
            }
            else if (!_url.equals(other._url)) return false;
            if (_user == null) {
                if (other._user != null) return false;
            }
            else if (!_user.equals(other._user)) return false;
            return true;
        }
        
        final String _driver;
        final String _url;
        final String _user;
        final String _password;
    }
    
    private static class ConnectionBuilder implements Builder<NamedConnection> {
        /**
         * @param propertyProvider
         * @param url
         * @param groupName
         */
        public ConnectionBuilder(PropertyProvider propertyProvider,
                String url, String groupName) {
            _propertyProvider = propertyProvider;
            _url = url;
            _groupName = groupName;
        }
        
        // @see com.redprairie.moca.Builder#build()
        @Override
        public NamedConnection build() {
            Properties modifiedProperties = new Properties();
            
            String connectionName = _propertyProvider.getConnectionName(
                _groupName, _connectionId.getAndIncrement());
            modifiedProperties = _propertyProvider.modifyConnectionProperties(
                modifiedProperties, connectionName);
            _logger.debug(MocaUtils.concat("Connecting to ", _url));
            try {
                Connection conn = DriverManager.getConnection(_url, 
                    modifiedProperties);
                return new NamedConnection(conn, connectionName);
            }
            catch (SQLException e) {
                _logger.error("Problem creating database connection.", e);
                return null;
            }
        }
        
        private final AtomicLong _connectionId = new AtomicLong();
        private final PropertyProvider _propertyProvider;
        private final String _url;
        private final String _groupName;
    }
    
    // Gets the dialect's pool validator and optionally augments it
    // to additionally validate the connection on reset if configured,
    // by default this is OFF (see database.pool-validate-on-checkout registry entry)
    private static Validator<Connection> getPoolValidator(SystemContext system, 
            SQLDialect dialect, String dbIsolation) throws SystemConfigurationException {
        Validator<Connection> validator = dialect.getValidator(dbIsolation);
        boolean validateOnCheckout = Boolean.parseBoolean(
                system.getConfigurationElement(MocaRegistry.REGKEY_DB_POOL_VALIDATE_ON_CHECKOUT,
                    MocaRegistry.REGKEY_DB_POOL_VALIDATE_ON_CHECKOUT_DEFAULT));
        if (validateOnCheckout) {
            validator = new ValidateOnCheckoutPoolValidator(validator);
        }
        
        return validator;
    }
    
    // Wrapper class that augments a connection pool validator with validation
    // checks on reset.
    private static class ValidateOnCheckoutPoolValidator extends ForwardingValidator<Connection> {

        ValidateOnCheckoutPoolValidator(Validator<Connection> delegate) {
            super(delegate);
        }
        
        @Override
        public void reset(Connection t) throws PoolException {
            try {
                // Notice we use Connection.isValid here but this will
                // actually be replaced (intercepted via proxy) by the ConnectionValidator
                // used by our dialect as not all JDBC drivers support this.
                if (!t.isValid(0)) {
                    throw new SQLPoolException("Connection was not valid");
                }
                delegate().reset(t);
            }
            catch (SQLException ex) {
                throw new SQLPoolException(ex);
            }
        }
        
    }
    
    private final MocaDataSource _pool;
    private final ExecutorService _executor = Executors.newCachedThreadPool(
        new NonMocaDaemonThreadFactory("JDBC Execute-"));
    private Writer _bindLog;
    
    // private final MocaContext _moca;
    private final DBType _dbType;
    private final SQLDialect _dialect;
    private final QueryHook _queryHook;

    // Performance Counters
    private int _queryCount;
    private int _failureCount;
    private long _queryLimit;
    private long _translationTime;
    private long _executeTime;
    private long _fetchTime;

    private static AtomicLong _connectionNumber = new AtomicLong();
    /**
     * This map is used to store what named url is stored for specific
     * connection settings.  This way we won't have multiple drivers pointing
     * to the same moca jdbc url.
     */
    private static final ConcurrentMap<ConnectionSettings, String> _connectionSettings = 
        new ConcurrentHashMap<ConnectionSettings, String>();
    
    private static final Logger _logger = LogManager.getLogger(JDBCAdapter.class);
    private static final Logger _perfLogger = LogManager.getLogger(
            "com.redprairie.moca.server.Performance");
}
