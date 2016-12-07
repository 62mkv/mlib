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
import java.util.Map;

import javax.sql.DataSource;

/**
 * This is an interface that describes all the methods that a moca pool
 * needs to implement over a normal DataSource
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public interface MocaDataSource extends DataSource {

    /**
     * Close all connections in the pool.  Any SQLException thrown on
     * close by the underlying JDBC connection is ignored.
     */
    public abstract void closeAll();

    /**
     * Returns a Map<Connection, ConnectionStatistics> consisting 
     * of idle database connections.
     */
    public abstract Map<Connection, ConnectionStatistics> getIdleConnections();

    /**
     * Returns a Map<Connection, ConnectionStatistics> consisting 
     * of busy database connections.
     */
    public abstract Map<Connection, ConnectionStatistics> getBusyConnections();

    /**
     * @return a <code>ConnectionPoolStatistics</code> associated with this
     * <code>ConnectionPool</code> .
     */
    public abstract ConnectionPoolStatistics getConnectionPoolStatistics();

    public abstract int getSize();

    /**
     * @return how large this pool can get up to or null if the pool is unbounded
     */
    public abstract Integer getMaxSize();

}