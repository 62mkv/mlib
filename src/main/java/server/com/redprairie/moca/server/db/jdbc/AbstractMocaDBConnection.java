/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.server.TransactionManagerUtils;

/**
 * This is a base class that handles the registering of the connection as
 * an XAResource when needed and unregistering and things related there of.
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
abstract class AbstractMocaDBConnection implements MocaDBConnection {
    
    protected AbstractMocaDBConnection(ConnectionValidator connValidator) {
        _synchronization = new AssociatedTransactionSynchronization();
        _connValidator = connValidator;
    }
    
    @Override
    public boolean isValid() throws SQLException {
        // Run the connection validator against the real JDBC connection
        _connValidator.validate(getConnection());
        return true;
    }
    
    // @see com.redprairie.moca.server.db.jdbc.MocaDBConnection#registerTransactionInThread()
    @Override
    public boolean registerTransactionInThread() throws SQLException {
        Transaction transaction = TransactionManagerUtils.getCurrentTransaction();
        // If there is not yet a started tx don't register ourselves
        if (transaction == null) {
            return false;
        }
        Transaction previousTransaction = _associatedTransaction.getAndSet(
            transaction);
        // To set the transaction it should have been previously null or
        // the same exact thing
        if (previousTransaction != null && previousTransaction != transaction) {
            IllegalStateException ex = new IllegalStateException(
                "Using same connection between 2 transactions "
                        + previousTransaction + " and " + transaction);
            _logger.error("The connection is being shared between 2 transactions. " +
                         "This can be caused by improper handling of a MocaContext between threads, " +
                         "see the exception stack for details on the calling component.", ex);
            throw ex;
        }
        // If the previous transaction was null, it means we didn't enlist the
        // resource yet
        if (previousTransaction == null) {
            try {
                _logger.debug("Enlisting connection into transaction");
                boolean registered = transaction.enlistResource(getXAResource());
                
                if (!registered) {
                    String msg = "Could not enlist connection as an xa resource, " +
                            "setting transaction to rollback only! " +
                            "This can be caused by improper handling of a MocaContext between threads, " +
                            "see the exception stack for details on the calling component.";
                    IllegalStateException ex = new IllegalStateException(msg);
                    _logger.error("Unable to enlist the connection into the transaction.", ex);
                    transaction.setRollbackOnly();
                    throw ex;
                }
                
                transaction.registerSynchronization(_synchronization);
            }
            catch (RollbackException e) {
                throw new SQLException(e);
            }
            catch (SystemException e) {
                throw new SQLException(e);
            }
        }
        
        return true;
    }
    
    // @see com.redprairie.moca.server.db.jdbc.MocaDBConnection#close()
    @Override
    public void close() throws SQLException {
        // If we were still associated with a transaction we should
        // delist ourselves
        Transaction transaction = _associatedTransaction.getAndSet(null);
        if (transaction != null) {
            try {
                // We only delist ourselves but leave the tx in an okay state
                transaction.delistResource(getXAResource(), XAResource.TMSUCCESS);
            }
            catch (IllegalStateException e) {
                // Ignore this exception as it just means the resource wasn't listed
            }
            catch (SystemException e) {
                throw new SQLException(e);
            }
        }
    }
    
    protected class AssociatedTransactionSynchronization implements Synchronization {

        // @see javax.transaction.Synchronization#afterCompletion(int)
        @Override
        public void afterCompletion(int arg0) {
            Transaction oldTransaction = _associatedTransaction.getAndSet(null);
            _logger.debug("Unassociated transaction " + oldTransaction);
        }

        // @see javax.transaction.Synchronization#beforeCompletion()
        @Override
        public void beforeCompletion() {
            // Do nothing
        }
    }
    
    protected abstract XAResource getXAResource() throws SQLException;

    protected final Synchronization _synchronization;
    protected final AtomicReference<Transaction> _associatedTransaction = new AtomicReference<Transaction>();
    protected final Logger _logger = LogManager.getLogger(this.getClass());
    private final ConnectionValidator _connValidator;
}
