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

package com.sam.moca.server.db.translate;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;

import oracle.jdbc.OraclePreparedStatement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.sam.moca.MocaType;
import com.sam.moca.pool.Validator;
import com.sam.moca.server.db.BindList;
import com.sam.moca.server.db.DBType;
import com.sam.moca.server.db.MissingVariableException;
import com.sam.moca.server.db.SQLBinder;
import com.sam.moca.server.db.jdbc.PropertyProvider;
import com.sam.moca.server.db.translate.filter.AutoBindFilter;
import com.sam.moca.server.db.translate.filter.CommentHintFilter;
import com.sam.moca.server.db.translate.filter.NoBindFilter;
import com.sam.moca.server.db.translate.filter.TranslationFilter;
import com.sam.moca.server.db.translate.filter.UnbindFilter;

/**
 * Oracle-specific translator. This class performs only minimal SQL translation,
 * automatically turning SQL constants into bind variables. It also sets up the
 * appropriate JDBC pool listener to ensure that JDBC connections are
 * appropriately handled.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class OracleDialect extends AbstractFilterTranslator {
    
    public static final int ORACLE_DEADLOCK = 60;
    public static final int ORACLE_LOCK_TIMEOUT = 54;
    public static final int ORACLE_UNIQUE_CONS = 1;

    /**
     * Default constructor for oracle
     */
    public OracleDialect() {
        super(BIND_FILTERS, BIND_FILTERS, DBType.ORACLE);
        _limitHandler = new OracleLimitHandler();
    }

    @Override
    public Validator<Connection> getValidator(String IsolationLevel) {
        // Ignore isolation level.  Always use default
        return new OracleValidator(getConnectionValidator());
    }
    
    @Override
    public SQLException translateSQLException(SQLException e) {
        int errorCode = e.getErrorCode();

        if (errorCode == ORACLE_DEADLOCK) {
            return new SQLException(e.getMessage(), e.getSQLState(), 
                BaseDialect.STD_DEADLOCK_CODE);
        }
        else if (errorCode == ORACLE_UNIQUE_CONS) {
            return new SQLException(e.getMessage(), e.getSQLState(),
                BaseDialect.STD_UNIQUE_CONS_CODE);
        }
        else if (errorCode == ORACLE_LOCK_TIMEOUT ) {
            return new SQLException(e.getMessage(), e.getSQLState(),
                BaseDialect.STD_LOCK_TIMEOUT_CODE);
        }
        else {
            return e;
        }
    }
    
    // @see com.sam.moca.server.db.translate.SQLDialect#getPropertyProvider()
    @Override
    public PropertyProvider getPropertyProvider() {
        return new OraclePropertyProvider();
    }

    // @see
    // com.sam.moca.db.translate.SQLTranslator#getSequenceValue(java.lang.String,
    // java.sql.Connection)
    @Override
    public String getSequenceValue(String sequence, Connection conn) throws SQLException {

        // In Oracle, the sequence name must correspond to an actual sequence
        // object.
        // So, we prepare a callable statement to get the sequence value

        CallableStatement stmt = null;
        try {
            stmt = conn.prepareCall("begin select " + sequence + ".nextval into ? from dual; end;");

            stmt.registerOutParameter(1, Types.VARCHAR);
            stmt.execute();
            return stmt.getString(1);
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
    
    @Override
    public PreparedStatement prepareStatement(Connection conn, SQLBinder binder, BindList bindList)
            throws SQLException, MissingVariableException {
        
        Collection<String> foundNames = new LinkedHashSet<String>(binder.getNames());
        
        // If using reference variables, we must bind by position.
        if (bindList.hasReferences()) {
            return super.prepareStatement(conn, binder, bindList);
        }
        else {
            // If the statement starts with select, it's probably OK to bind by
            // name.  Otherwise, use the default JDBC positional binding
            // behavior.
            String sql = binder.getOriginalStatement().trim();
            
            if (sql.length() < 6 || !sql.substring(0, 6).equalsIgnoreCase("select")) {
                return super.prepareStatement(conn, binder, bindList);
            }
            
            PreparedStatement pstmt = conn.prepareStatement(binder.getOriginalStatement(), ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            // If for some reason, we don't have an OraclePreparedStatement, then we may be using an alternate driver.  Just
            // set parameters by index.
            if (!(pstmt instanceof OraclePreparedStatement)) {
                pstmt.close();
                return super.prepareStatement(conn, binder, bindList);
            }
            
            // Oracle's extension of PreparedStatement allows us to bind variables by name.
            OraclePreparedStatement ostmt = (OraclePreparedStatement) pstmt;
            
            for (String name : foundNames) {

                // If the variable is not present but used, it's an error.
                if (!bindList.contains(name)) {
                    throw new MissingVariableException(name);
                }

                Object value = bindList.getValue(name);
                MocaType type = bindList.getType(name);
    
                // Set input variables
                if (value == null) {
                    ostmt.setNullAtName(name, type.getSQLType());
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
                        ostmt.setStringAtName(name, dateTime.toString(
                                "YYYYMMddHHmmss"));
                    }
                    else {
                        ostmt.setStringAtName(name, String.valueOf(value));
                    }
                }
                else {
                    ostmt.setObjectAtName(name, value);
                }
            }
    
            return ostmt;
        }
    }
    
    // @see com.sam.moca.server.db.translate.SQLDialect#getLimitHandler()
    @Override
    public LimitHandler getLimitHandler() {
        return _limitHandler;
    }
    
    private final LimitHandler _limitHandler;
    
    private static final TranslationFilter[] BIND_FILTERS = new TranslationFilter[] {
        new CommentHintFilter(), 
        new AutoBindFilter(),
        new NoBindFilter(false),
        new UnbindFilter(false) 
    };
    private final static Logger _logger = LogManager.getLogger(OracleDialect.class);

}
