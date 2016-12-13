/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.TransactionHook;

/**
 * This defines methods that are required for management of a Moca transaction
 * which include hooks, attributes and database interaction.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public interface TransactionManager {
    
    /**
     * Adds an object to the current transaction that will be called during
     * the transaction lifecycle.
     * @param hook a <code>TransactionHook</code> object that will be invoked
     * at the appropriate time during the container transaction.  This argument
     * cannot be null.
     */
    public void addTransactionHook(TransactionHook hook);
    
    /**
     * Checks the current running transaction to see if an attribute has been
     * added of the given name.
     * 
     * @param name the name of the transaction attribute
     * @return an attribute associated with the given attribute name, or
     *         <code>null</code> if no such attribute exists.
     */
    public Object getTransactionAttribute(String name);
    
    /**
     * Adds a named object to the current transaction. When the current running
     * transaction is committed or rolled back, the reference to the attribute
     * is removed as well.
     * 
     * @param name the name of the transaction attribute
     * @param value the value of the transaction attribute
     */
    public void setTransactionAttribute(String name, Object value);
    
    /**
     * Removes the named attribute from the transaction.
     * 
     * @param name the name of the transaction attribute to be removed.
     */
    public void removeTransactionAttribute(String name);
    
    /**
     * Commits just the database transaction. No hooks or remote transactions
     * are affected by this call.
     * 
     * @throws SQLException if a database exception occurs.
     */
    public void commitDB() throws SQLException;

    /**
     * Rolls back just the database transaction. No hooks or rmote transactions
     * are affected by this call.
     * 
     * @throws SQLException
     */
    public void rollbackDB() throws SQLException;
    
    /**
     * Commits the current MOCA transaction. This will commit the current
     * database transaction as well as any outstanding transactions on remote
     * servers that are in flight. Any transaction hooks that are registered
     * with the current transaction are fired as well.
     * 
     * @throws MocaException if an error occurred committing the transaction, or
     *             if some other commit-time failure occurred (e.g. hook
     *             failure, remote failure, etc.)
     */
    public void commit() throws MocaException;

    /**
     * Rolls back the current MOCA transaction. This will roll back the current
     * database transaction as well as any outstanding transactions on remote
     * servers that are in flight. Any transaction hooks that are registered
     * with the current transaction are fired as well.
     * 
     * @throws MocaException if an error occurred while rolling back the
     *             transaction, or if some other rollback-time failure occurred
     *             (e.g. hook failure, remote failure, etc.)
     */
    public void rollback() throws MocaException;
    
    /**
     * Acquire the JDBC connection associated with the current MOCA context.
     * @return an instance of <code>java.sql.Connection</code>.
     */
    public Connection getConnection();
    
    /**
     * Rolls back the database to the named savepoint. The savepoint must have
     * been created using the {@link #setSavepoint(String)} method.
     * Existing transaction hooks will still be in place, and it is suggested 
     * that they are not used when using savepoints.
     * 
     * @param savepoint the name of the savepoint to roll back to.
     * @throws SQLException If the savepoint does not exist or there was some
     *             database error rolling back to the savepoint.
     */
    public void rollbackDB(String savepoint) throws SQLException;

    /**
     * Returns the database type of the main MOCA database connection.
     * 
     * @return the database type, as a <code>String</code>
     */
    public String getDbType();

    /**
     * Returns the next value for a given sequence. This is the primary
     * application mechanism for obtaining the next value for a given database
     * sequence.
     * 
     * @param name the sequence name
     * @return the next sequence value
     * @throws MocaException
     */
    public String getNextSequenceValue(String name) throws MocaException;

    /**
     * Sets up a savepoint. The savepoint is a database-only construct, so it is
     * local to the current transaction. Also, non-database resources are not
     * tracked or rolled back according to the savepoint.
     * 
     * @param savepoint The name of the savepoint to create.
     * @throws SQLException
     */
    public void setSavepoint(String savepoint) throws SQLException;
    
    /**
     * Executes the given SQL and returns the results to this caller.  This is a simple
     * mechanism for executing straightforward queries.  This mechanism is not effective
     * for all situations in which SQL must be executed, and presents some efficiency
     * concerns, but can handle most simple queries without difficulty.
     * 
     * Note that all results are read into memory, so very large queries should use another
     * SQL access mechanism, such as JDBC.
     * 
     * Argument types are determined by looking up the actual class of each named argument.
     * <code>null</code> values are assumed to be STRINGs.
     * 
     * @param sql The SQL query to run.
     * @param args A Map instance, containing named parameters to be passed to the query.
     * @return a MocaResults object, containing all rows returned from the query.
     * @throws MocaException if a SQL or MOCA error occurs executing the query.
     */
    MocaResults executeSQL(String sql, Map<String, ?> args) throws MocaException;

    /**
     * Executes the given SQL and returns the results to this caller.  This is a simple
     * mechanism for executing straightforward queries.  This mechanism is not effective
     * for all situations in which SQL must be executed, and presents some efficiency
     * concerns, but can handle most simple queries without difficulty.
     * 
     * Note that all results are read into memory, so very large queries should use another
     * SQL access mechanism, such as JDBC.
     * 
     * @param sql The SQL query to run.
     * @param args A Map instance, containing named parameters to be passed to the query.
     * @return a MocaResults object, containing all rows returned from the query.
     * @throws MocaException if a SQL or MOCA error occurs executing the query.
     */
    MocaResults executeSQL(String sql, MocaArgument... args) throws MocaException;
}
