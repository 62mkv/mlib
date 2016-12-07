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

package com.redprairie.moca.server.exec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.client.MocaConnection;
import com.redprairie.moca.exceptions.RemoteSessionClosedException;
import com.redprairie.moca.exceptions.SessionClosedException;

/**
 * A class to hold a remote transaction.
 * 
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class RemoteTransaction {
    
    public RemoteTransaction(MocaConnection conn, boolean inTransaction) {
        _conn = conn;
        _inTransaction = inTransaction;
    }
    
    /**
     * @return Returns the connection.
     */
    public MocaConnection getConn() {
        return _conn;
    }
    
    public void prepare() throws MocaException {
        try {
            _conn.executeCommand("prepare");
        }
        catch (MocaException e) {
            int errorCode = e.getErrorCode();
            if (errorCode == SessionClosedException.CODE) {
                e = new RemoteSessionClosedException();
            }
            if (_inTransaction) {
                throw e;
            }
            else {
                _logger.warn("Remote transaction failure (ignored): " + e);
            }
        }
    }
    
    public void commit() throws MocaException {
        try {
            _conn.executeCommand("commit");
        }
        catch (MocaException e) {
            int errorCode = e.getErrorCode();
            if (errorCode == SessionClosedException.CODE) {
                e = new RemoteSessionClosedException();
            }
            if (_inTransaction) {
                throw e;
            }
            else {
                _logger.warn("Remote commit failure (ignored): " + e);
            }
        }
    }
    
    public void rollback() throws MocaException {
        try {
            _conn.executeCommand("rollback");
        }
        catch (MocaException e) {
            int errorCode = e.getErrorCode();
            // If it was session closed, means the tx was already rolled back
            // which is fine
            if (errorCode != SessionClosedException.CODE) {
                if (_inTransaction) {
                    throw e;
                }
                else {
                    _logger.warn("Remote rollback failure (ignored): " + e);
                }
            }
        }
    }
    
    public void close() {
        _conn.close();
    }

    private static final Logger _logger = 
        LogManager.getLogger(RemoteTransaction.class);

    private final boolean _inTransaction;
    private final MocaConnection _conn;
}
