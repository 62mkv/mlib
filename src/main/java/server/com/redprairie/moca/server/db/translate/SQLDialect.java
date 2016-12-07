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

package com.redprairie.moca.server.db.translate;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.redprairie.moca.pool.Validator;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.DBType;
import com.redprairie.moca.server.db.MissingVariableException;
import com.redprairie.moca.server.db.SQLBinder;
import com.redprairie.moca.server.db.jdbc.ConnectionValidator;
import com.redprairie.moca.server.db.jdbc.PropertyProvider;

/**
 * Dialect implementation that provides an abstraction so the database dialect
 * does not have to be known.  Although the database type may be queried using
 * the {@link #getDBType()} method.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author derek
 * @version $Revision$
 */
public interface SQLDialect {

    /**
     * Translate an Oracle-friendly SQL statement into one that works for the
     * native database.
     * 
     * @param sql the SQL statement to be translated.
     * @param args the arguments passed to the statement. This object can be
     *            modified in the course of translation.
     * @param options holds optional parameters that may be used in the
     *            translation.
     * @return a new SQL statement to be executed.
     * @throws TranslationException if a syntax error makes it impossible to
     *             translate the SQL statement.
     */
    public String translateStatement(String sql, BindList args, TranslationOptions options)
            throws TranslationException;
    
    /**
     * Gets the LimitHandler for the SQL Dialect which handles pagination functionality
     * @return The dialect's LimitHandler
     */
    public LimitHandler getLimitHandler();

    /**
     * Gets the next sequence value from the database. In some databases, there
     * is a native sequence construct. For others, the behavior must be
     * simulated with other mechanisms.
     * 
     * @param sequence the name of the sequence/table to be used to generate a
     *            sequence value.
     * @param conn A connection to the database.
     * @return A string representations of a sequence value. Due to the nature
     *         of database sequences, the form of this results can vary.
     * @throws SQLException if an error occurred generating the sequence, or
     *             this database does not support sequences.
     */
    public String getSequenceValue(String sequence, Connection conn) throws SQLException;

    /**
     * Translate Vendor-specific error codes to "known" error codes.
     * 
     * @param e the exception to be translated.
     * @return a SQL Exception with the "correct" error codes
     */
    public SQLException translateSQLException(SQLException e);

    /**
     * Returns the database type.
     * 
     * @return the database type.
     */
    public DBType getDBType();

    /**
     * Returns an instance of Validator, appropriate for this database.  This method will be called only once,
     * when the database connection pool is created.  The validator is responsible for low-level setup of the
     * database connection.
     * @param isolationLevel a string representing the default transaction isolation level for this pool of connections. 
     */
    public Validator<Connection> getValidator(String isolationLevel);
    
    /**
     * Returns an instance of PropertyProvider, appropriate for this database.
     * This provider should then used to tweak properties before creating 
     * a connection to the database
     * @return
     */
    public PropertyProvider getPropertyProvider();

    /**
     * @param conn
     * @param binder
     * @param bindList
     * @return
     * @throws SQLException
     * @throws MissingVariableException
     */
    PreparedStatement prepareStatement(Connection conn, SQLBinder binder, BindList bindList) throws SQLException,
            MissingVariableException;

    /**
     * @param call
     * @param bindList
     * @param binder
     * @throws SQLException
     */
    void updateOutVariables(CallableStatement call, SQLBinder binder, BindList bindList) throws SQLException;
    
    /**
     * Gets the {@link ConnectionValidator} for the dialect which is used to validate
     * {@link Connection}'s in a dialect/DB engine dependent manner.
     * @return The connection validator
     */
    ConnectionValidator getConnectionValidator();

}