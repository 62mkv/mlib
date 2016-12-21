/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

/**
 * This is an implementation of an xa based connection that defines the
 * contract as specificed in the {@link MocaDBConnection} interface.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
class XAMocaDBConnection extends AbstractMocaDBConnection {
    
    public XAMocaDBConnection(XAConnection connection, ConnectionValidator connValidator) {
        super(connValidator);
        _connection = connection;
    }

    // @see com.sam.moca.server.db.jdbc.MocaConnection#getConnection()
    @Override
    public Connection getConnection() throws SQLException {
        return _connection.getConnection();
    }

    // @see com.sam.moca.server.db.jdbc.MocaConnection#close()
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
    protected XAResource getXAResource() throws SQLException {
        return _connection.getXAResource();
    }
    
    private final XAConnection _connection;
}
