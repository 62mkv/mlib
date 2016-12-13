/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.probes.connections;

import com.redprairie.mad.annotations.ProbeGroup;
import com.redprairie.mad.annotations.ProbeType;
import com.redprairie.mad.client.MadGauge;
import com.redprairie.mad.probes.InitializedProbe;
import com.redprairie.moca.mad.MonitoringUtils;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.db.jdbc.ConnectionPoolStatistics;

/**
 * Probing for Database Connections statistics
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
@ProbeGroup(MonitoringUtils.MOCA_GROUP_NAME)
@ProbeType(DatabaseConnectionSummaryProbes.METRIC_TYPE_DB_CONN)
public class DatabaseConnectionSummaryProbes extends InitializedProbe {
    
    // Metrics Type
    public static final String METRIC_TYPE_DB_CONN = "Database-Connections";
    
    // Metrics Names
    public static final String CURRENT_CONNECTIONS = "current-connections";
    public static final String PEAK_CONNECTIONS = "peak-connections";
    public static final String MAX_CONNECTIONS = "max-connections";

    // @see com.redprairie.mad.probes.InitializedProbe#initialize()
    @Override
    public void initialize() {
        
        // Current connections gauge
        getFactory().newGauge(getMadName(CURRENT_CONNECTIONS), new MadGauge<Integer>() {

            @Override
            public Integer getValue() {
                return getConnectionStatistics().getCurrentConnections();
            }
            
        });
        
        // Peak connections gauge
        getFactory().newGauge(getMadName(PEAK_CONNECTIONS), new MadGauge<Integer>() {

            @Override
            public Integer getValue() {
                return getConnectionStatistics().getPeakConnections();
            }
            
        });
        
        // Max connections gauge
        getFactory().newGauge(getMadName(MAX_CONNECTIONS), new MadGauge<Integer>() {

            @Override
            public Integer getValue() {
                return getConnectionStatistics().getMaxConnections();
            }
            
        });
    }
    
    protected ConnectionPoolStatistics getConnectionStatistics() {
        return ServerUtils.globalAttribute(ServerContextFactory.class)
                          .getDBAdapter().getConnectionPoolStatistics();
    }
}
