/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.server.db.jdbc.ConnectionPoolStatistics;
import com.redprairie.moca.server.db.jdbc.ConnectionStatistics;
import com.redprairie.moca.server.exec.ArgumentSource;
import com.redprairie.moca.server.profile.CommandPath;

/**
 * This interface defines what methods are required for various database
 * adapters.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public interface DBAdapter {

    /**
     * Called to indicate that this adapter's database connection is closed.
     * 
     * @throws SQLException if there is an error closing the database
     *                 connection.
     */
    public void close() throws SQLException;

    /**
     * Execute a SQL statement using optional Oracle-style bind variables. a SQL
     * statement with bind variables will contain named fields of the format
     * <code>:<i>fieldname</i></code>. An example SQL statement would be:
     * <code>select * from blah where field = :field</code>
     * @param argumentSource the source to look up arguments if needed
     * @param sqlStatement the text of the sql statement to be executed.
     * @param bindList a bind list, containing named variables to be substituted
     *                into a prepared statement
     * @param commandPath TODO
     * @param autoBind currently unused.
     * 
     * @return a <code>WrappedResults<code> object with the results of the
     * SQL query.
     * @throws SQLException if a database error occurred executing the query.
     * @throws MocaException if other error conditions occur while executing
     * a query.
     */
    public EditableResults executeSQL(ArgumentSource argumentSource, 
        MocaTransaction tx, String sqlStatement, BindList bindList, 
        BindMode mode, boolean ignoreResults, CommandPath commandPath)
            throws SQLException, MocaException;

    /**
     * Returns the current JDBC connection of this instance.
     * 
     * @return the current JDBC connection that this adapter is using.
     */
    public Connection getConnection(MocaTransaction tx, CommandPath commandPath) throws SQLException;

    /**
     * Returns an enum indicating the type of database this adapter is
     * connected to.
     * 
     * @return 1 if the database is Oracle, 2 if the database is Microsoft SQL
     *         server or 0 otherwise.
     * @throws SQLException if an error occurred communicating to the database.
     */
    public DBType getDBType();

    /**
     * Obtains a sequence value from the database. The behavior of the various
     * implementations of sequence values is left in the hands of the
     * translation code.
     * 
     * @param sequence the name of the "sequence" object in the database.
     * @throws SQLException if an error occurred getting the next sequence value
     *                 from the database.
     */
    public String getNextSequenceValue(MocaTransaction tx, String sequence, 
            CommandPath commandPath)
            throws SQLException;

    /**
     * Returns a list of ConnectionStatistics for all busy and idle database 
     * connections.
     */
    public Map<Connection, ConnectionStatistics> getConnectionStatistics();

    /**
     * Returns a list of ConnectionPoolStatistics for the ConnectionPool.
     */
    public ConnectionPoolStatistics getConnectionPoolStatistics();
    
    /**
     * This will create a new transaction for the database adapter.
     * @return The new transaction
     */
    public MocaTransaction newTransaction();

    /**
     * This will tell the database adapter to log performance statistics
     */
    public void logPerformance();
    
    /**
     * Provides access to the low-level data source for this adapter.  This
     * data source provides shared access to the connection pool, and
     * connections retrieved from the DataSource will not be associated with
     * the current transaction.  All connections retrieved from the data source
     * must be closed in order to be returned to the connection pool.
     * @return
     */
    public DataSource getDataSource();
}