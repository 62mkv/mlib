/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.probes.connections;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.mad.client.MadGauge;
import com.redprairie.mad.client.MadMetrics;
import com.redprairie.mad.client.MadName;
import com.redprairie.mad.client.MadNameImpl;
import com.redprairie.moca.mad.MonitoringUtils;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.db.DBAdapter;
import com.redprairie.moca.server.db.jdbc.ConnectionPoolStatistics;
import com.redprairie.moca.server.exec.SystemContext;

import static org.junit.Assert.*;

/**
 * Tests for Database Connection probes
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TI_DatabaseConnectionSummaryProbes {
    
    @BeforeClass
    public static void beforeClass() {
        // Mock out the ServerContextFactory and DBAdapter
        // Hold a reference to restore
        SystemContext ctx = ServerUtils.globalContext();
        _toRestore = ServerUtils.globalAttribute(ServerContextFactory.class);
        ServerContextFactory _mockFactory = Mockito.mock(ServerContextFactory.class);
        _mockDBAdapter = Mockito.mock(DBAdapter.class);
        Mockito.when(_mockFactory.getDBAdapter()).thenReturn(_mockDBAdapter);
        ctx.putAttribute(ServerContextFactory.class.getName(), _mockFactory);
    }
    
    @AfterClass
    public static void afterClass() {
        // Restore the ServerContextFactory
        SystemContext ctx = ServerUtils.globalContext();
        ctx.putAttribute(ServerContextFactory.class.getName(), _toRestore);
    }
    
    @After
    public void tearDown() {
        // Remove any metrics that were registered
        List<MadName> names = _fact.getMadNames();
        for (MadName name : names) {
            if (name.getType().equals(DatabaseConnectionSummaryProbes.METRIC_TYPE_DB_CONN)
                    && name.getGroup().equals(MonitoringUtils.MOCA_GROUP_NAME)) {
                _fact.removeMetric(name);
            }
        }
    }
    
    @Test
    public void testDatabaseConnectionProbes() {
        Integer current = 5;
        Integer peak = 10;
        Integer max = 15;

        DatabaseConnectionSummaryProbes probes = new DatabaseConnectionSummaryProbes();
        mockConnectionStats(current, peak, max);
        
        probes.initialize();

        // Test reading the metrics
        MadGauge<Integer> actualCurrent = _fact.getGauge(new MadNameImpl(MonitoringUtils.MOCA_GROUP_NAME,
                                      DatabaseConnectionSummaryProbes.METRIC_TYPE_DB_CONN,
                                      DatabaseConnectionSummaryProbes.CURRENT_CONNECTIONS));

        
        MadGauge<Integer> actualPeak = _fact.getGauge(new MadNameImpl(MonitoringUtils.MOCA_GROUP_NAME,
                                       DatabaseConnectionSummaryProbes.METRIC_TYPE_DB_CONN,
                                       DatabaseConnectionSummaryProbes.PEAK_CONNECTIONS));
        
        MadGauge<Integer> actualMax = _fact.getGauge(new MadNameImpl(MonitoringUtils.MOCA_GROUP_NAME,
                                       DatabaseConnectionSummaryProbes.METRIC_TYPE_DB_CONN,
                                       DatabaseConnectionSummaryProbes.MAX_CONNECTIONS));
        
        assertTrue(actualCurrent != null);
        assertEquals(current, actualCurrent.getValue());
        assertTrue(actualPeak != null);
        assertEquals(peak, actualPeak.getValue());
        assertTrue(actualMax != null);
        assertEquals(max, actualMax.getValue());
    }

    private void mockConnectionStats(int current, int peak, int max) {
        ConnectionPoolStatistics mockStats = Mockito.mock(ConnectionPoolStatistics.class);
        Mockito.when(_mockDBAdapter.getConnectionPoolStatistics()).thenReturn(mockStats);
        Mockito.when(mockStats.getCurrentConnections()).thenReturn(current);
        Mockito.when(mockStats.getPeakConnections()).thenReturn(peak);
        Mockito.when(mockStats.getMaxConnections()).thenReturn(max);
    }
    
    
    private static final MadFactory _fact = MadMetrics.getFactory();
    private static DBAdapter _mockDBAdapter;
    private static ServerContextFactory _toRestore;
}
