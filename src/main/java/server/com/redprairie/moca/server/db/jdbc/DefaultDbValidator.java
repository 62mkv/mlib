/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.server.db.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import com.redprairie.moca.pool.PoolException;
import com.redprairie.moca.pool.Validator;

/**
 * This is the default db validator base.  Normally any db validator will extend
 * this class
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class DefaultDbValidator implements Validator<Connection> {
    /**
     * An unspecified transaction isolation level value.  This value corresponds to the values from
     * the <code>java.sql.Connection</code> object that represent known transaction isolation
     * levels.  Some levels are vendor-specific, and should be set according to that driver 
     * vendor's specifications.
     */
    public static final int UNSPECIFIED_TRANSACTION_ISOLATION = -1;
    
    /**
     * Creates the DB validator with the given {@link ConnectionValidator} used
     * for validating a connection in conjunction with {@link #isValid(Connection)}.
     * @param connectionValidator The connection validator, may be null which would
     *                            mean no validation of the connections is performed.
     */
    public DefaultDbValidator(ConnectionValidator connectionValidator) {
        this(connectionValidator, UNSPECIFIED_TRANSACTION_ISOLATION);
    }
    
    /**
     * Creates the DB validator with the given {@link ConnectionValidator} used
     * for validating a connection in conjunction with {@link #isValid(Connection)}.
     * The isolationLevel argument must be one of the
     * values accepted by <code>Connection.setTransactionIsolation</code>.
     * @param connectionValidator The connection validator, may be null which would
     *                            mean no validation of the connections is performed.
     * @param isolationLevel a valid transaction isolation level to be passed
     * to Connection.setTransactionIsolation.  If this argument is passed as -1,
     * transaction isolation level is obtained from the first connection
     * object seen.
     */
    public DefaultDbValidator(ConnectionValidator connectionValidator, int isolationLevel) {
        _defaultIsolationLevel = isolationLevel;
        _isAutoCommit = false;
        _connectionValidator = connectionValidator;
    }
    
    // @see com.redprairie.moca.pool.Validator#initialize(java.lang.Object)
    @Override
    public void initialize(Connection conn) throws PoolException {
        if (conn == null) {
            throw new PoolException(-2, "Database connection problem encountered, " +
                    "check trace output!");
        }
    }

    // @see com.redprairie.moca.pool.Validator#reset(java.lang.Object)
    @Override
    public void reset(Connection conn) throws SQLPoolException {
        try {
            conn.setAutoCommit(_isAutoCommit);
            
            // If the isolation level is unspecified, we've never gotten a
            // connection, so learn the default isolation level from the
            // just-created connection.
            if (_defaultIsolationLevel == UNSPECIFIED_TRANSACTION_ISOLATION) {
                _defaultIsolationLevel = conn.getTransactionIsolation();
            }
            conn.setTransactionIsolation(_defaultIsolationLevel);
        }
        catch (SQLException e) {
            throw new SQLPoolException(e);
        }
    }

    // @see com.redprairie.moca.pool.Validator#isValid(java.lang.Object)
    @Override
    public boolean isValid(Connection conn) {
        if (_connectionValidator == null) return true;
        
        try {
            _connectionValidator.validate(conn);
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    // @see com.redprairie.moca.pool.Validator#invalidate(java.lang.Object)
    @Override
    public void invalidate(Connection conn) {
        try {
            conn.close();
        }
        catch (SQLException e) {
            // Ignore error on the close
        }
    }
    
    private final boolean _isAutoCommit;
    private final ConnectionValidator _connectionValidator;
    private int _defaultIsolationLevel;
}
