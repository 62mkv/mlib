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

package com.sam.moca.server.exec;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Deque;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sam.moca.MocaArgument;
import com.sam.moca.MocaArgumentException;
import com.sam.moca.MocaException;
import com.sam.moca.MocaOperator;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaRuntimeException;
import com.sam.moca.MocaType;
import com.sam.moca.TransactionHook;
import com.sam.moca.exceptions.MocaDBException;
import com.sam.moca.server.TransactionManagerUtils;
import com.sam.moca.server.db.BindList;
import com.sam.moca.server.db.BindMode;
import com.sam.moca.server.db.DBAdapter;
import com.sam.moca.server.db.MocaTransaction;
import com.sam.moca.server.profile.CommandPath;
import com.sam.moca.util.MocaUtils;
import com.sam.util.ArgCheck;

/**
 * This class handles all the transaction related work for the Moca Engine.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MocaTransactionManager implements TransactionManager {

    /**
     * @param adapter
     */
    public MocaTransactionManager(ArgumentSource argumentSource, 
        DBAdapter adapter, Deque<MocaTransaction> transactionList) {
        super();
        ArgCheck.notNull(transactionList);
        _argumentSource = argumentSource;
        _dbAdapter = adapter;
        _transactionStack = transactionList;
    }

    // @see com.sam.moca.DatabaseTool#getConnection()
    @Override
    public Connection getConnection() {
        try {
            if (_dbAdapter != null) {
                return _dbAdapter.getConnection(_currentTransaction(), 
                        _currentPath);
            }
            else {
                return null;
            }
        }
        catch (SQLException e) {
            throw new MocaRuntimeException(new MocaDBException(e));
        }
    }
    
    // @see com.sam.moca.DatabaseTool#commit()
    @Override
    public void commit() throws MocaException {
        _logger.debug("Commit");
        
        MocaTransaction tx = _currentTransaction(false);

        _completeTransaction(tx, true);
    }
    
    // @see com.sam.moca.server.exec.TransactionManager#commitDB()
    @Override
    public void commitDB() throws SQLException {
        _logger.debug("Executing DB commit");

        MocaTransaction tx = _currentTransaction(false);
        
        // If no transaction is in progress, don't do anything
        if (tx == null) {
            _logger.debug("No current transaction");
            return;
        }
        
        try {
            _logger.debug("Calling DB commit");
            tx.commit();
        }
        finally {
            _logger.debug("commit complete");
        }
    }

    // @see com.sam.moca.server.exec.TransactionManager#rollbackDB()
    @Override
    public void rollbackDB() throws SQLException {
        _logger.debug("Executing DB rollback");

        MocaTransaction tx = _currentTransaction(false);
        
        // If no transaction is in progress, don't do anything
        if (tx == null) {
            _logger.debug("No current transaction");
            return;
        }
        
        try {
            _logger.debug("Calling DB rollback");
            tx.rollback();
        }
        finally {
            _logger.debug("rollback complete");
        }
    }

    // @see com.sam.moca.DatabaseTool#getDbType()
    @Override
    public String getDbType() {
        return _dbAdapter.getDBType().toString();
    }

    // @see com.sam.moca.DatabaseTool#getNextSequenceValue(java.lang.String)
    @Override
    public String getNextSequenceValue(String name) throws MocaException {
        try {
            return _dbAdapter.getNextSequenceValue(_currentTransaction(), name, 
                    _currentPath);
        }
        catch (SQLException e) {
            throw new MocaDBException(e);
        }
    }

    // @see com.sam.moca.DatabaseTool#rollback()
    @Override
    public void rollback() throws MocaException {
        _logger.debug("Rollback");

        MocaTransaction tx = _currentTransaction(false);
        
        _completeTransaction(tx, false);
    }
    
    /**
     * @param savepoint
     * @throws SQLException
     */
    @Override
    public void rollbackDB(String savepoint) throws SQLException {
        _logger.debug(MocaUtils.concat("Executing DB rollback (savepoint:", 
                savepoint, ")"));

        MocaTransaction tx = _currentTransaction(false);
        
        // If no transaction is in progress, don't do anything
        if (tx == null) {
            _logger.debug("No current transaction");
            return;
        }
        
        try {
            _logger.debug("Calling DB rollback to savepoint");
            tx.rollbackToSavepoint(savepoint);
            _logger.debug("rollback to savepoint complete");
        }
        catch (SQLException e) {
            _logger.debug(MocaUtils.concat("rollback to savepoint failed: ", e));
            throw e;
        }
    }

    // @see com.sam.moca.DatabaseTool#setSavepoint(java.lang.String)
    @Override
    public void setSavepoint(String savepoint) throws SQLException {
        _logger.debug(MocaUtils.concat("Creating DB savepoint:", savepoint));

        MocaTransaction tx = _currentTransaction(true);

        tx.setSavepoint(savepoint);
    }
    
    // @see com.sam.moca.DatabaseTool#getTransactionAttribute(java.lang.String)
    @Override
    public Object getTransactionAttribute(String name) {
        ArgCheck.notNull(name);
        return _currentTransaction().getAttribute(name);
    }

    // @see com.sam.moca.DatabaseTool#setTransactionAttribute(java.lang.String, java.lang.Object)
    @Override
    public void setTransactionAttribute(String name, Object value) {
        ArgCheck.notNull(name);
        _currentTransaction().setAttribute(name, value);
    }
    
    // @see com.sam.moca.DatabaseTool#removeTransactionAttribute(java.lang.String)
    @Override
    public void removeTransactionAttribute(String name) {
        ArgCheck.notNull(name);
        _currentTransaction().removeAttribute(name);
    }
    
    // @see com.sam.moca.DatabaseTool#executeSQL(java.lang.String, java.util.Map)
    @Override
    public MocaResults executeSQL(String sql, Map<String, ?> args)
            throws MocaException {
        BindList bindList = new BindList();
        if (args != null) {
            for (Map.Entry<String, ?> arg : args.entrySet()) {
                String name = arg.getKey();
                Object value = arg.getValue();
                MocaType type = MocaType.forValue(value);
                
                if (type == MocaType.OBJECT || type == MocaType.UNKNOWN) {
                    throw new MocaArgumentException(
                            "Invalid argument type passed to executeSQL");
                }
                
                bindList.add(name, type, value);
            }
        }
        
        return _executeSQL(sql, bindList, (args == null || args.size() == 0) ? 
                BindMode.AUTO : BindMode.NONE);
    }

    // @see com.sam.moca.DatabaseTool#executeSQL(java.lang.String, com.sam.moca.MocaArgument[])
    @Override
    public MocaResults executeSQL(String sql, MocaArgument... args)
            throws MocaException {
        BindList bindList = new BindList();
        if (args != null) {
            for (MocaArgument arg : args) {
                if (arg.getOper() != MocaOperator.EQ) {
                    throw new MocaArgumentException(
                            "Invalid argument passed to executeSQL");
                }
                bindList.add(arg.getName(), arg.getType(), arg.getValue());
            }
        }
        
        return _executeSQL(sql, bindList, (args == null || args.length == 0) ? 
                BindMode.AUTO : BindMode.NONE);
    }
    
    private MocaResults _executeSQL(String sql, BindList bindList, 
            BindMode mode) throws MocaException {
        MocaTransaction tx = _currentTransaction(true);
        
        try {
            MocaResults res = _dbAdapter.executeSQL(_argumentSource, tx, sql, 
                bindList, mode, false, _currentPath);
            if (res != null) {
                res.reset();
            }
            return res;
        }
        catch (SQLException e) {
            throw new MocaDBException(e);
        }
    }
    
    // @see com.sam.moca.DatabaseTool#addTransactionHook(com.sam.moca.TransactionHook)
    @Override
    public void addTransactionHook(TransactionHook hook) {
        _currentTransaction().addHook(hook);
    }
    
    /**
     * @param okToCommit
     * @throws MocaException
     */
    @SuppressWarnings("unchecked")
    private void _completeTransaction(MocaTransaction tx, boolean okToCommit) 
            throws MocaException {
        // If no transaction is in progress, don't do anything
        if (tx == null) {
            _logger.debug("No current transaction");
            return;
        }
        
        // Get list of remote transactions.
        Map<String, RemoteTransaction> remoteTransactions = 
            (Map<String, RemoteTransaction>)tx.getAttribute(REMOTE_TX_ATTR);
        
        try {
            if (okToCommit && remoteTransactions != null) {
                
                for (Entry<String, RemoteTransaction> remote : remoteTransactions.entrySet()) {
                    String host = remote.getKey();
                    RemoteTransaction remoteTx = remote.getValue();
                    _logger.debug(MocaUtils.concat("remote(", host, ") prepare"));
                    remoteTx.prepare();
                }
            }
        }
        catch (MocaException e) {
            // If any part of the prepare failed, we have to rollback 
            _logger.debug("remote prepare failed: " + e, e);
            okToCommit = false;
            throw e;
        }
        finally {
            try {
                if (tx.isOpen()) {
                    if (okToCommit) {
                        _logger.debug("Calling commit");
                        tx.commit();
                    }
                    else {
                        _logger.debug("Calling rollback");
                        tx.rollback();
                    }
                }
            }
            catch (SQLException e) {
                // If any part of the commit failed, we consider it 
                _logger.debug("commit/rollback failed: " + e, e);
                okToCommit = false;
                throw new MocaDBException(e);
            }
            finally {
                if (remoteTransactions != null) {
                    for (Entry<String, RemoteTransaction> remote : remoteTransactions.entrySet()) {
                        String host = remote.getKey();
                        RemoteTransaction remoteTx = remote.getValue();
                        if (okToCommit) {
                            _logger.debug(MocaUtils.concat("remote(", host, ") commit"));
                            try {
                                remoteTx.commit();
                            }
                            catch (MocaException e) {
                                _logger.error("Partial Transaction Failure: remote(" + host + ") commit failed: " + e, e);
                            }
                        }
                        else {
                            _logger.debug(MocaUtils.concat("remote(", host, ") rollback"));
                            try {
                                remoteTx.rollback();
                            }
                            catch (MocaException e) {
                                _logger.error("Partial Transaction Failure: remote(" + host + ") rollback failed: " + e, e);
                            }
                        }
                        remoteTx.close();
                    }
                }
                tx.clearAll();
            }
        }
    }
    
    private MocaTransaction _currentTransaction() {
        return _currentTransaction(true);
    }
    
    private MocaTransaction _currentTransaction(boolean create) {
        if (_transactionStack.isEmpty()) {
            // If we want to create a new tx or we have a current global tx
            // we have to push a tx on the stack
            if (create || TransactionManagerUtils.getCurrentTransaction() != null) {
                _pushTransaction();
            }
        }
        
        MocaTransaction tx = _transactionStack.peekFirst();
        return tx;
    }
    
    /**
     * This should only be called when there is no tx on the stack
     */
    private void _pushTransaction() {
        MocaTransaction tx = _dbAdapter.newTransaction();
        _transactionStack.addFirst(tx);
    }
    
    public static final String REMOTE_TX_ATTR = "moca.remote";
    
    private static final Logger _logger = LogManager.getLogger(MocaTransactionManager.class);

    private final ArgumentSource _argumentSource;
    private final DBAdapter _dbAdapter;
    private final Deque<MocaTransaction> _transactionStack;
    
    /**
     * This variable is to be used by someone who extends this class to provided
     * a path to the executeSQL commands.
     */
    protected CommandPath _currentPath;
}
