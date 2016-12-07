/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This interface describes an abstraction for a connection that allows for
 * either xa or non xa based connections to be returned from the same call.
 * A caller can then try to register the connection with the current transaction
 * if there is one or retrieve the actual Connection object to operate upon.
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
interface MocaDBConnection {
    
    /**
     * Checks if the underlying {@link Connection} is still valid
     * @return true if the connection is valid, false otherwise
     * @throws SQLException Thrown if an issue occurs checking if the
     *                      connection is valid (which would indicate it
     *                      is indeed invalid).
     */
    public boolean isValid() throws SQLException;
    
    public boolean registerTransactionInThread() throws SQLException;
    
    public Connection getConnection()  throws SQLException;
    
    public void close() throws SQLException;
}
