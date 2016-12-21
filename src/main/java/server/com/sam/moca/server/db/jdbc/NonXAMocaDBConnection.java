/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.sam.moca.server.db.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.arjuna.ats.jta.resources.LastResourceCommitOptimisation;

/**
 * This is an implementation of a non xa based connection that defines the
 * contract as specificed in the {@link MocaDBConnection} interface.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
class NonXAMocaDBConnection extends AbstractMocaDBConnection {
    
    public NonXAMocaDBConnection(Connection connection, ConnectionValidator connValidator) {
        super(connValidator);
        _connection = connection;
        _xaResource = new NonXAConnectionXAResource(connection);
    }

    // @see com.sam.moca.server.db.jdbc.MocaDBConnection#getConnection()
    @Override
    public Connection getConnection() throws SQLException {
        return _connection;
    }

    // @see com.sam.moca.server.db.jdbc.MocaDBConnection#close()
    @Override
    public void close() throws SQLException {
        try {
            super.close();
        }
        finally {
            _connection.close();
        }
    }
    
    // @see com.sam.moca.server.db.jdbc.AbstractMocaDBConnection#getXAResource()
    @Override
    protected XAResource getXAResource() {
        return _xaResource;
    }
    
    private static class NonXAConnectionXAResource implements LastResourceCommitOptimisation {
        
        public NonXAConnectionXAResource(Connection connection) {
            _connection = connection;
        }

        // @see javax.transaction.xa.XAResource#commit(javax.transaction.xa.Xid, boolean)
        @Override
        public void commit(Xid xid, boolean onePhase) throws XAException {
            _logger.debug("Committing non xa tx - " + xid);
            try {
                _connection.commit();
            }
            catch (SQLException e) {
                String msg = "Error while committing non xa connection";
                _logger.warn(msg, e);
                throw new XAException(XAException.XA_RBCOMMFAIL);
            }
        }

        // @see javax.transaction.xa.XAResource#end(javax.transaction.xa.Xid, int)
        @Override
        public void end(Xid xid, int flags) throws XAException {
            _logger.debug("Called to end non xa tx - " + xid);
        }

        // @see javax.transaction.xa.XAResource#forget(javax.transaction.xa.Xid)
        @Override
        public void forget(Xid xid) throws XAException {
            _logger.debug("Called to forget non xa tx - " + xid);
        }

        // @see javax.transaction.xa.XAResource#getTransactionTimeout()
        @Override
        public int getTransactionTimeout() throws XAException {
            return 0;
        }

        // @see javax.transaction.xa.XAResource#isSameRM(javax.transaction.xa.XAResource)
        @Override
        public boolean isSameRM(XAResource xares) throws XAException {
            if (!(xares instanceof NonXAConnectionXAResource)) {
                return false;
            }
            return this.equals(xares);
        }

        // @see javax.transaction.xa.XAResource#prepare(javax.transaction.xa.Xid)
        @Override
        public int prepare(Xid xid) throws XAException {
            return XA_OK;
        }

        // @see javax.transaction.xa.XAResource#recover(int)
        @Override
        public Xid[] recover(int flag) throws XAException {
            // Non XA cannot recover
            return new Xid[0];
        }

        // @see javax.transaction.xa.XAResource#rollback(javax.transaction.xa.Xid)
        @Override
        public void rollback(Xid xid) throws XAException {
            _logger.debug("Rolling back non xa tx - " + xid);
            try {
                _connection.rollback();
            }
            catch (SQLException e) {
                String msg = "Error while rolling back non xa connection";
                _logger.warn(msg, e);
                throw new XAException(msg + ": " + e);
            }
        }

        // @see javax.transaction.xa.XAResource#setTransactionTimeout(int)
        @Override
        public boolean setTransactionTimeout(int seconds) throws XAException {
            return false;
        }

        // @see javax.transaction.xa.XAResource#start(javax.transaction.xa.Xid, int)
        @Override
        public void start(Xid xid, int flags) throws XAException {
            _logger.debug("Started non xa tx - " + xid);
        }
        
        // @see java.lang.Object#hashCode()
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((_connection == null) ? 0 : _connection.hashCode());
            return result;
        }

        // @see java.lang.Object#equals(java.lang.Object)
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            NonXAConnectionXAResource other = (NonXAConnectionXAResource) obj;
            if (_connection == null) {
                if (other._connection != null) return false;
            }
            else if (!_connection.equals(other._connection)) return false;
            return true;
        }
        
        private final Connection _connection;
    }
    
    private final NonXAConnectionXAResource _xaResource;
    private final Connection _connection;
    
    private static final Logger _logger = LogManager.getLogger(NonXAMocaDBConnection.class);
}
