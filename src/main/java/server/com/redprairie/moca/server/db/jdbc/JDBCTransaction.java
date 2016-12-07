/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2007
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
import java.sql.Savepoint;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import com.redprairie.moca.server.db.AbstractMocaTransaction;

/**
 * A container for all aspects of a running MOCA transaction. Currently, a
 * transaction can span a single database connection, but we make use of
 * valuable transaction hooks and transaction attributes as well.
 * 
 * Essentially, this class acts as an intermediary between ComponentAdapter and
 * JDBCAdapter. This class, though public, is intended for use only by the MOCA
 * execution engine. No user serviceable parts inside.
 * 
 * <b><pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class JDBCTransaction extends AbstractMocaTransaction {

    /**
     * Constructor that takes the data source pool
     * @param pool
     * @param transaction
     * @throws SystemException
     */
    public JDBCTransaction(DataSource pool, TransactionManager transaction) {
        super(transaction);
        if (pool == null) {
            throw new NullPointerException();
        }
        _pool = pool;
    }
    
    // These methods implement the real transactional behavior.
    
    // @see com.redprairie.moca.server.db.MocaTransaction#getConnection()
    
    synchronized
    public Connection getConnection() throws SQLException {
        if (_conn == null) {
            startTransaction();
            _conn = _pool.getConnection();
            _conn.setAutoCommit(false);
        }
        return _conn;
    }
    
    // @see com.redprairie.moca.server.db.MocaTransaction#isOpen()
    public boolean isOpen() {
        return _conn != null || super.isOpen();
    }
    
    // @see com.redprairie.moca.server.db.MocaTransaction#commit()
    public void commit() throws SQLException {
        _savepoints.clear();
        
        try {
            super.commit();
        }
        finally {
            if (_conn != null) {
                // Ignore exception on close.
                try {
                    _conn.close();
                }
                catch (SQLException e) {
                    // Continue
                }
                finally {
                    _conn = null;
                }
            }
        }
    }
    
    // @see com.redprairie.moca.server.db.MocaTransaction#rollback()
    public void rollback() throws SQLException {
        _savepoints.clear();
        try {
            super.rollback();
        }
        finally {
            if (_conn != null) {
                // Ignore exception on close.
                try {
                    _conn.close();
                }
                catch (SQLException e) {
                    // Continue
                }
                finally {
                    _conn = null;
                }
            }
        }
    }
    
    // @see com.redprairie.moca.server.db.MocaTransaction#setSavepoint(java.lang.String)
    public void setSavepoint(String name) throws SQLException {
        Connection conn = getConnection();
        Savepoint savepoint = conn.setSavepoint();
        _savepoints.put(name, savepoint);
    }
    
    // @see com.redprairie.moca.server.db.MocaTransaction#rollbackToSavepoint(java.lang.String)
    public void rollbackToSavepoint(String name) throws SQLException {
        Savepoint save = (Savepoint) _savepoints.remove(name);
        if (save != null) {
            Connection conn = getConnection();
            conn.rollback(save);
        }
    }
    
    //
    // Implementation
    //
    private Connection _conn;
    private final DataSource _pool;
    private final Map<String, Savepoint> _savepoints = new HashMap<String, Savepoint>();
}
