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

package com.redprairie.moca.server.db.translate;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.redprairie.moca.MocaType;
import com.redprairie.moca.pool.Validator;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.DBType;
import com.redprairie.moca.server.db.MissingVariableException;
import com.redprairie.moca.server.db.SQLBinder;
import com.redprairie.moca.server.db.jdbc.ConnectionValidators;
import com.redprairie.moca.server.db.jdbc.ConnectionValidator;
import com.redprairie.moca.server.db.jdbc.DefaultDbValidator;
import com.redprairie.moca.server.db.jdbc.PropertyProvider;

/**
 * Abstract class describing a SQL translator that takes a string and a list
 * of bind variables and returns another string.  If necessary, the bind
 * list can be modified by the translator.
 * 
 * <b><pre>
 * Copyright (c) 2006 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public abstract class BaseDialect implements SQLDialect {
    
    public static final String NOCONV_INDICATOR = "/*NOCONV*/";
    public static final int STD_DEADLOCK_CODE = -60;
    public static final int STD_LOCK_TIMEOUT_CODE = -54;
    public static final int STD_UNIQUE_CONS_CODE = -1;

    /**
     * Constructor that takes an array of filters to be applied in order.
     * The given filters will be applied to the SQL statement in order. 
     * @param filters
     */
    public BaseDialect(DBType dbType) {
        _dbType = dbType;
    }
    
    // @see com.redprairie.moca.server.db.translate.SQLDialect#translateStatement(java.lang.String, com.redprairie.moca.server.db.BindList, com.redprairie.moca.server.db.translate.TranslationOptions)
    @Override
    public String translateStatement(String sql, BindList args, TranslationOptions options)
        throws TranslationException {
        return sql;
    }
    
    // @see com.redprairie.moca.server.db.translate.SQLDialect#translateSQLException(java.sql.SQLException)
    @Override
    public SQLException translateSQLException(SQLException e) {
        return e;
    }
    
    // @see com.redprairie.moca.server.db.translate.SQLDialect#getDBType()
    @Override
    public DBType getDBType() {
        return _dbType;
    }
    
    // @see com.redprairie.moca.server.db.translate.SQLDialect#getPoolListener()
    @Override
    public Validator<Connection> getValidator(String levelString) {
        return new DefaultDbValidator(getConnectionValidator(), 
            getTransactionIsolation(levelString));
    }
    
    // @see com.redprairie.moca.server.db.translate.SQLDialect#getPropertyProvider()
    @Override
    public PropertyProvider getPropertyProvider() {
        return new DefaultPropertyProvider();
    }
    
    @Override
    public PreparedStatement prepareStatement(Connection conn, SQLBinder binder, BindList bindList)  throws SQLException, MissingVariableException {
        PreparedStatement pstmt;
        CallableStatement call = null;
        
        if (!bindList.hasReferences()) {
            pstmt = conn.prepareStatement(binder.getBoundStatement(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        }
        else {
            call = conn.prepareCall(binder.getBoundStatement(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            pstmt = call;
        }
        
        // Go through the passed-in bound variables. If it's a
        // reference type, register it as an in/out parameter.
        // Otherwise, just set its value on the CallableStatement.
        List<String> bindNames = binder.getNames();
        for (int i = 0; i < bindNames.size(); i++) {
            String name = bindNames.get(i);
            Object value = bindList.getValue(name);
            MocaType type = bindList.getType(name);

            // If the variable is not present but used, it's
            // an error.
            if (type == null) {
                throw new MissingVariableException(name);
            }

            // Handle reference types
            if (type.equals(MocaType.INTEGER_REF)) {
                call.registerOutParameter(i + 1, Types.INTEGER);
            }
            else if (type.equals(MocaType.DOUBLE_REF)) {
                call.registerOutParameter(i + 1, Types.DOUBLE);
            }
            else if (type.equals(MocaType.STRING_REF)) {
                call.registerOutParameter(i + 1, Types.VARCHAR);
            }

            // Set input variables
            if (value == null) {
                pstmt.setNull(i + 1, type.getSQLType());
            }
            else if (type == MocaType.DATETIME) {
                if (value instanceof Date) {
                    // For now we have to bind date times as strings
                    // so that we don't have issues with oracle to_date
                    // and to keep people from having to change all their
                    // to_date calls
                    Date timestamp = (Date)value;
                    DateTime dateTime = new DateTime(
                            timestamp.getTime());
                    pstmt.setString(i + 1, dateTime.toString(
                            "YYYYMMddHHmmss"));
                }
                else {
                    pstmt.setString(i + 1, String.valueOf(value));
                }
            }
            else {
                pstmt.setObject(i + 1, value);
            }
        }

        return pstmt;
    }
    
    @Override
    public void updateOutVariables(CallableStatement call, SQLBinder binder, BindList bindList) throws SQLException {
        // Now, spin through the bind variables again, and re-assign
        // the value for the reference variables.
        if (bindList.hasReferences()) {
            List<String> bindNames = binder.getNames();
            for (int i = 0; i < bindNames.size(); i++) {
                String name = bindNames.get(i);
                MocaType type = bindList.getType(name);

                // Handle reference types coming out of a
                // stored procedure call.
                if (type.equals(MocaType.INTEGER_REF)
                        || type.equals(MocaType.DOUBLE_REF)
                        || type.equals(MocaType.STRING_REF)) {
                    Object value = call.getObject(i + 1);
                    bindList.setValue(name, value);
                }
            }
        }
    }
    
    /**
     * Default NooopLimitHandler which indicates limit handling is not supported.
     * Subclasses should override this with their LimitHandler implementation.
     */
    @Override
    public LimitHandler getLimitHandler() {
        return NoopLimitHandler.getInstance();
    }
    
    @Override
    public ConnectionValidator getConnectionValidator() {
        return DEFAULT_CONNECTION_VALIDATOR;
    }
    //
    // Subclass interface
    //
    protected int getTransactionIsolation(String levelName) {
        if (levelName == null) {
            return DefaultDbValidator.UNSPECIFIED_TRANSACTION_ISOLATION;
        }
        
        Integer level = ISOLATION_LEVELS.get(levelName);
        
        if (level == null) {
            return DefaultDbValidator.UNSPECIFIED_TRANSACTION_ISOLATION;
        }
        else {
            return level;
        }
    }
    
    //
    // Implementation
    //
    private DBType _dbType;
    
    private static final ConnectionValidator DEFAULT_CONNECTION_VALIDATOR = 
            ConnectionValidators.newQueryTestValidator("select 'x' from dual where 1=0");
    
    private static final Map<String, Integer> ISOLATION_LEVELS = new HashMap<String, Integer>();
    static {
        ISOLATION_LEVELS.put("read_committed", Connection.TRANSACTION_READ_COMMITTED);
        ISOLATION_LEVELS.put("read_uncommitted", Connection.TRANSACTION_READ_UNCOMMITTED);
        ISOLATION_LEVELS.put("repeatable_read", Connection.TRANSACTION_REPEATABLE_READ);
        ISOLATION_LEVELS.put("serializable", Connection.TRANSACTION_SERIALIZABLE);
        ISOLATION_LEVELS.put("snapshot", 4096);
        ISOLATION_LEVELS.put("default", DefaultDbValidator.UNSPECIFIED_TRANSACTION_ISOLATION);
    }
}
