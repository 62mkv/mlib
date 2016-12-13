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

package com.redprairie.moca.server.db;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRuntimeException;
import com.redprairie.moca.TransactionHook;
import com.redprairie.moca.server.exec.MocaTransactionManager;
import com.redprairie.moca.util.MocaUtils;

/**
 * This class holds all the Service Transaction logic that can be shared by
 * database specific transaction classes.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public abstract class AbstractMocaTransaction implements MocaTransaction {
    protected AbstractMocaTransaction(TransactionManager transaction) {
        if (transaction == null) {
            throw new NullPointerException();
        }
        _transactionManager = transaction;
        
        try {
            // If the transaction was already in progress match it up
            if (_transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION) {
                startTransaction();
            }
        }
        catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }
    
    // @see com.redprairie.moca.server.db.MocaTransaction#removeAttribute(java.lang.String)
    public Object removeAttribute(String name) {
        return _transactionAttributes.remove(name);
    }
    
    // @see com.redprairie.moca.server.db.MocaTransaction#getAttribute(java.lang.String)
    public Object getAttribute(String name) {
        return _transactionAttributes.get(name);
    }
    
    // @see com.redprairie.moca.server.db.MocaTransaction#setAttribute(java.lang.String, java.lang.Object)
    public void setAttribute(String name, Object value) {
        // If we were passed a remote tx attribute start the tx
        if (name.equals(MocaTransactionManager.REMOTE_TX_ATTR)) {
            startTransaction();
        }
        _transactionAttributes.put(name, value);
    }
    
    // @see com.redprairie.moca.server.db.MocaTransaction#addHook(com.redprairie.moca.TransactionHook)
    public void addHook(TransactionHook hook) {
        startTransaction();
        _transactionHooks.addFirst(hook);
    }
    
    // @see com.redprairie.moca.server.db.MocaTransaction#hooks()
    public Collection<TransactionHook> hooks() {
        return Collections.unmodifiableCollection(_transactionHooks);
    }
    
    public void addXAResource(XAResource resource) throws SQLException {
        if (resource != null) {
            Transaction transaction = startTransaction();
            try {
                transaction.enlistResource(resource);
            }
            catch (RollbackException e) {
                throw new SQLException(e);
            }
            catch (SystemException e) {
                throw new SQLException(e);
            }
        }
    }
    
    // @see com.redprairie.moca.server.db.MocaTransaction#clearAll()
    public void clearAll() {
        _transactionHooks.clear();
        _transactionAttributes.clear();
    }
    
    protected Transaction startTransaction() {
        try {
            Transaction transaction;
            int status = _transactionManager.getStatus();
            // If the status has no tx or we just finished one, then we have
            // to create a new one
            if (status == Status.STATUS_NO_TRANSACTION) {
                _transactionManager.begin();
                
                transaction = _transactionManager.getTransaction();
            }
            else {
                transaction = _transactionManager.getTransaction();
            }
            
            Transaction previousTransaction = _associatedTransaction.getAndSet(
                transaction);
            
            // To set the transaction it should have been previously null or
            // the same exact thing
            if (previousTransaction != null && previousTransaction != transaction) {
                throw new IllegalStateException(
                    "Using same connection between 2 transactions "
                            + previousTransaction + " and " + transaction);
            }
            // If the previous transaction was null, it means we didn't enlist the
            // synchronization yet
            if (previousTransaction == null) {
                try {
                    transaction.registerSynchronization(new Synchronization() {
                        
                        @Override
                        public void beforeCompletion() {
                            _logger.debug("Calling pre-commit hooks");
                            for (TransactionHook hook : _transactionHooks) {
                                // TODO: get a way to inject this
                                try {
                                    hook.beforeCommit(MocaUtils.currentContext());
                                }
                                catch (MocaException e) {
                                    throw new MocaRuntimeException(e);
                                }
                            }
                        }
                        
                        @Override
                        public void afterCompletion(int status) {
                            for (TransactionHook hook : _transactionHooks) {
                                try {
                                    boolean committed = status == Status.STATUS_COMMITTED;
                                    _logger.debug(MocaUtils.concat(
                                        "Calling post-transaction hooks (", 
                                        committed ? "commit" : "rollback", "mode)"));
                                    // TODO: get a way to inject this
                                    hook.afterCompletion(MocaUtils.currentContext(), 
                                        committed);
                                }
                                catch (MocaException e) {
                                    _logger.error("post-transaction hook threw exception:" + e, e);
                                }
                            }
                        }
                    });
                }
                catch (RollbackException e) {
                    // This should never happen since we are registering right when
                    // the transaction is created
                    throw new RuntimeException(e);
                }
            }
            
            return transaction;
        }
        catch (SystemException e) {
            throw new RuntimeException(e);
        }
        catch (NotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
    
    // @see com.redprairie.moca.server.db.MocaTransaction#commit()
    @Override
    public void commit() throws SQLException {
        try {
            // We have to commit all of the tx's and the nested ones
            while (_transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION) {
                _transactionManager.commit();
            }
        }
        catch (SecurityException e) {
            throw new SQLException(e);
        }
        catch (RollbackException e) {
            _logger.info("Exception committing the transaction.");
            StringBuilder message = new StringBuilder("Transaction could not be completed - " +
                    "please retry - " + e.getMessage());
            if (e.getCause() != null) {
                message.append(" Cause: " + e.getCause().getMessage());
            }
            message.append(" Please contact your system administrator.");
            _logger.info(message.toString(), e);
            throw new SQLException(message.toString()
                    , e);
        }
        catch (HeuristicMixedException e) {
            throw new SQLException(e);
        }
        catch (HeuristicRollbackException e) {
            throw new SQLException(e);
        }
        catch (SystemException e) {
            throw new SQLException(e);
        }
        finally {
            _associatedTransaction.getAndSet(null);
        }
    }
    
    // @see com.redprairie.moca.server.db.MocaTransaction#rollback()
    @Override
    public void rollback() throws SQLException {
        try {
            // We have to rollback all of the tx's and the nested ones
            while (_transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION) {
                _transactionManager.rollback();
            }
        }
        catch (SystemException e) {
            throw new SQLException(e);
        }
        finally {
            _associatedTransaction.getAndSet(null);
        }
    }
    
    // @see com.redprairie.moca.server.db.MocaTransaction#suspend()
    @Override
    public void suspend() throws SQLException {
        try {
            Transaction transaction = _transactionManager.suspend();
            // We compare the associated transaction with the current
            Transaction associated = _associatedTransaction.get();
            
            if (transaction != null && associated != null) {
                if (!transaction.equals(associated)) {
                    throw new IllegalStateException(
                        "Associated moca transaction [" + associated + 
                        "] is not the same as the associated transaction for the manager [" 
                        + transaction + "] - This is most likely a bug");
                }
                _logger.debug("Suspending existing transaction");
            }
            else if (transaction != null || associated != null) {
                throw new IllegalStateException("Associated moca transaction [" + associated + 
                    "] is not the same as the associated transaction for the manager [" 
                    + transaction + "] - This is most likely a bug");
            }
            else {
                _logger.debug("Suspended but no current transaction - okay");
            }
        }
        catch (SystemException e) {
            throw new SQLException(e);
        }
    }

    // @see com.redprairie.moca.server.db.MocaTransaction#resume()
    @Override
    public void resume() throws SQLException {
        Transaction associated = _associatedTransaction.get();
        if (associated != null) {
            try {
                _transactionManager.resume(associated);
            }
            catch (InvalidTransactionException e) {
                throw new SQLException(e);
            }
            catch (SystemException e) {
                throw new SQLException(e);
            }
        }
        // If no transaction is associated just leave it as is.  If the
        // transaction is needed it is started elsewhere
    }
    
    // @see com.redprairie.moca.server.db.MocaTransaction#isOpen()
    @Override
    public boolean isOpen() {
        return _associatedTransaction.get() != null;
    }
    
    // @see java.lang.Object#finalize()
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        rollback();
    }
    
    protected final AtomicReference<Transaction> _associatedTransaction = new AtomicReference<Transaction>();
    protected final TransactionManager _transactionManager;
    private final LinkedList<TransactionHook> _transactionHooks = new LinkedList<TransactionHook>();
    private final Map<String, Object> _transactionAttributes = new HashMap<String, Object>();
    
    private static final Logger _logger = LogManager.getLogger(MocaTransaction.class);
}
