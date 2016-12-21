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

package com.sam.moca.server.db.jdbc;

/**
 * Database connection pool statistics information.
 * 
 * Copyright (c) 2010 Sam Corporation All Rights Reserved
 * 
 * @author mlange
 */
public class ConnectionPoolStatistics {

    public ConnectionPoolStatistics(int currentConnections,
            int peakConnections, Integer maxConnections) {
        _currentConnections = currentConnections;
        _peakConnections = peakConnections;
        _maxConnections = maxConnections;
    }

    public int getCurrentConnections() {
        return _currentConnections;
    }

    public int getPeakConnections() {
        return _peakConnections;
    }

    public Integer getMaxConnections() {
        return _maxConnections;
    }

    private final int _currentConnections;
    private final int _peakConnections;
    private final Integer _maxConnections;
}