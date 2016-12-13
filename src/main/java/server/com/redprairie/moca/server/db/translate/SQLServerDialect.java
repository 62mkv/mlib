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

package com.redprairie.moca.server.db.translate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.pool.Validator;
import com.redprairie.moca.server.db.DBType;
import com.redprairie.moca.server.db.jdbc.ConnectionValidators;
import com.redprairie.moca.server.db.jdbc.ConnectionValidator;
import com.redprairie.moca.server.db.jdbc.DefaultDbValidator;
import com.redprairie.moca.server.db.jdbc.PropertyProvider;
import com.redprairie.moca.server.db.translate.filter.AutoBindFilter;
import com.redprairie.moca.server.db.translate.filter.CommentFilter;
import com.redprairie.moca.server.db.translate.filter.CommentHintFilter;
import com.redprairie.moca.server.db.translate.filter.NoBindFilter;
import com.redprairie.moca.server.db.translate.filter.TranslationFilter;
import com.redprairie.moca.server.db.translate.filter.UnbindFilter;
import com.redprairie.moca.server.db.translate.filter.mssql.MSEmptyStringFilter;
import com.redprairie.moca.server.db.translate.filter.mssql.MSForUpdateFilter;
import com.redprairie.moca.server.db.translate.filter.mssql.MSFunctionFilter;
import com.redprairie.moca.server.db.translate.filter.mssql.MSOuterJoinFilter;
import com.redprairie.moca.server.db.translate.filter.mssql.MSRownumFilter;
import com.redprairie.moca.server.db.translate.filter.mssql.MSSimpleFunctionFilter;
import com.redprairie.moca.server.db.translate.filter.mssql.MSSimpleWordFilter;
import com.redprairie.moca.server.db.translate.filter.mssql.NStringLiteralFilter;
import com.redprairie.moca.server.db.translate.filter.mssql.NVarcharFilter;
import com.redprairie.moca.server.db.translate.filter.mssql.StringConcatenationFilter;

/**
 * SQL Server translator.  Takes Oracle-formatted SQL and translates it into valid
 * SQL Server syntax.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class SQLServerDialect extends AbstractFilterTranslator {

    public static final int SQL_UNIQUE_CONS  = 2627;
    public static final int SQL_UNIQUE_INDEX = 2601;
    public static final int SQL_LOCK_TIMEOUT = 1222;
    public static final int SQL_DEADLOCK     = 1205;
    
    /**
     * Default constructor for SQL Server Translator
     */
    public SQLServerDialect() {
        super(MSSQL_FILTERS, MSSQL_NOCONV_FILTERS, DBType.MSSQL);
        _limitHandler = new SQLServerLimitHandler();
        _connectionValidator = ConnectionValidators.newQueryTestValidator("select 'x' where 1=0");
    }
    
    @Override
    public Validator<Connection> getValidator(String levelName) {
        int isolationLevel = getTransactionIsolation(levelName);
        if (isolationLevel == DefaultDbValidator.UNSPECIFIED_TRANSACTION_ISOLATION) {
            isolationLevel = Connection.TRANSACTION_READ_COMMITTED;
        }
        return new SQLServerValidator(getConnectionValidator(), isolationLevel);
    }


    @Override
    public SQLException translateSQLException(SQLException e) {
        int errorCode = e.getErrorCode();

        if (errorCode == SQL_DEADLOCK) {
            return new SQLException(e.getMessage(), e.getSQLState(), 
                BaseDialect.STD_DEADLOCK_CODE);
        }
        else if (errorCode == SQL_UNIQUE_CONS || errorCode == SQL_UNIQUE_INDEX) {
            return new SQLException(e.getMessage(), e.getSQLState(),
                BaseDialect.STD_UNIQUE_CONS_CODE);
        }
        else if (errorCode == SQL_LOCK_TIMEOUT ) {
            return new SQLException(e.getMessage(), e.getSQLState(),
                BaseDialect.STD_LOCK_TIMEOUT_CODE);
        }
        else {
            return e;
        }
    }
    
    // @see com.redprairie.moca.db.translate.SQLTranslator#getSequenceValue(java.lang.String, java.sql.Connection)
    @Override
    public String getSequenceValue(String sequence, Connection conn)
            throws SQLException {
        
        // In SQL Server, the sequence name must correspond to a "sequence" table
        // as defined by the MOCA database schema.
        
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            //
            // Insert a new row in order to generate a sequence number.
            //
            boolean isQuery = stmt.execute(
                    "insert into " + sequence + " values(NULL, NULL, NULL, NULL);" +
                    "select @@identity currval, seedval, incval, maxval" +
                    "  from " + sequence + " where currval = -1;" +
                    "delete from " + sequence + " where currval = @@identity");
            
            // Should be update, query, update, in that order.
            if (isQuery || stmt.getUpdateCount() != 1) {
                throw new SQLException("Unexpected result of insert");
            }
            
            isQuery = stmt.getMoreResults();
            
            if (!isQuery) {
                throw new SQLException("Unexpected result of select");
            }
            
            ResultSet res = stmt.getResultSet();
            if (!res.next()) {
                throw new SQLException("No rows from sequence query");
            }
                
            BigDecimal value = res.getBigDecimal(1);
            BigDecimal seedval = res.getBigDecimal(2);
            BigDecimal maxval = res.getBigDecimal(4);
            
            isQuery = stmt.getMoreResults();
            if (isQuery || stmt.getUpdateCount() != 1) {
                throw new SQLException("Unexpected result of delete");
            }

            if (value.compareTo(maxval) >= 0) {
                stmt.execute("dbcc checkident('" + sequence + "', reseed, " + seedval.toBigInteger() + ")");
            }
            
            return String.valueOf(value.toBigInteger());
        }
        finally {
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (SQLException e) {
                    _logger.debug("There was an issue closing statement", e);
                }
            }
        }
    }
    
    // @see com.redprairie.moca.db.translate.SQLTranslator#getSequenceValue(java.lang.String, java.sql.Connection)
    public String getCurrentSequenceValue(String sequence, Connection conn)
            throws SQLException {
        
        // In SQL Server, the sequence name must correspond to a "sequence" table
        // as defined by the MOCA database schema.
        
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            //
            // Insert a new row in order to generate a sequence number.
            //
            boolean isQuery = stmt.execute(
                    "select @@identity currval" +
                    "  from " + sequence + " where currval = -1;");
            
            if (!isQuery) {
                throw new SQLException("Unexpected result of select");
            }
            
            ResultSet res = stmt.getResultSet();
            if (!res.next()) {
                throw new SQLException("No rows from sequence query");
            }
                
            BigDecimal value = res.getBigDecimal(1);
            
            return String.valueOf(value.toBigInteger());
        }
        finally {
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (SQLException e) {
                    _logger.debug("There was an issue closing statement", e);
                }
            }
        }
    }
    
    // @see com.redprairie.moca.server.db.translate.SQLDialect#getPropertyProvider()
    @Override
    public PropertyProvider getPropertyProvider() {
        return new SQLServerPropertyProvider();
    }
    
    // @see com.redprairie.moca.server.db.translate.SQLDialect#getLimitHandler()
    @Override
    public LimitHandler getLimitHandler() {
        return _limitHandler;
    }
    
    @Override
    public ConnectionValidator getConnectionValidator() {
        return _connectionValidator;
    }

    private final LimitHandler _limitHandler;
    private final ConnectionValidator _connectionValidator;
    
    private static final TranslationFilter[] MSSQL_FILTERS = new TranslationFilter[] {
        new CommentHintFilter(),
        new AutoBindFilter(),
        new NoBindFilter(true),
        new UnbindFilter(true),
        new StringConcatenationFilter(),
        new MSOuterJoinFilter(),
        new MSFunctionFilter(),
        new MSEmptyStringFilter(),
        new MSRownumFilter(),
        new MSSimpleFunctionFilter(),
        new MSSimpleWordFilter(),
        new MSForUpdateFilter(),
        new NVarcharFilter(),
        new NStringLiteralFilter(),
        new CommentFilter()
    };

    private static final TranslationFilter[] MSSQL_NOCONV_FILTERS = new TranslationFilter[] {
        new AutoBindFilter(),
        new NoBindFilter(true)
    };

    private static final Logger _logger = LogManager.getLogger(SQLServerDialect.class);
}
