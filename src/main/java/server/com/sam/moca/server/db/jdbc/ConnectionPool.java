/*
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
package com.sam.moca.server.db.jdbc;

import java.sql.Connection;

/**
 * An abstract base class for connection-pooling JDBC data sources.  This is
 * the interface that the PooledConnection objects expect to see to release
 * themselves from the pool.
 * 
 * <strong>Note: this is an abstract class rather than an interface because
 * interfaces require making all methods public.</strong>
 *
 * <b><pre>
 * Copyright (c) 2016 Redpoint Technologies LLC
 * Copyright (c) 20167 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */
abstract class ConnectionPool {
     /**
     * Drop a connection that was busy from the pool.
     *
     * @param conn the connection to remove from the connection pool.
     *  This argument cannot be null.
     */
    abstract void _dropFromPool(Connection conn);
    
    /**
     * Returns a connection to the pool.
     *
     * Note: It is the responibility of the caller to determine whether
     *       the connection is still working properly prior to calling
     *       this method to return the connection to the pool.
     *
     * @param conn the connection to return to the pool.
     */
    abstract void _returnToPool(Connection conn);
}
