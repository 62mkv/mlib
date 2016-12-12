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

package com.redprairie.moca.probes.sessions;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.mad.client.MadGauge;
import com.redprairie.mad.client.MadMetrics;
import com.redprairie.mad.client.MadName;
import com.redprairie.mad.client.MadNameImpl;
import com.redprairie.moca.mad.MonitoringUtils;
import com.redprairie.moca.servlet.WebSessionManager;

import static org.junit.Assert.*;

/**
 * Tests for Moca Session Probes
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TU_MocaSessionProbes {
    
    @BeforeClass
    public static void setupClass() {
        _factToRestore = MadMetrics.getFactory();
         MadMetrics.setFactory(_mockFactory);
    }
    
    @AfterClass
    public static void afterClass() {
        MadMetrics.setFactory(_factToRestore);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testRegisteringProbes() {
        final WebSessionManager manager = Mockito.mock(WebSessionManager.class);
        Mockito.when(manager.getMaxSessions()).thenReturn(1000);
        Mockito.when(manager.getPeakSessionCount()).thenReturn(50);
        Mockito.when(manager.getSessionCount()).thenReturn(5);
        // Mock out the session manager
        MocaSessionProbes probes = new MocaSessionProbes() {
            @Override
            protected WebSessionManager getSessionManager() {
                return manager;
            }
        };
        
        // Mock out MadName generation, should have a better way to do this
        MadName maxName = mockNameGeneration(MocaSessionProbes.MAX_SESSIONS);
        MadName curName = mockNameGeneration(MocaSessionProbes.CURRENT_SESSIONS);
        MadName peakName = mockNameGeneration(MocaSessionProbes.PEAK_SESSIONS);
        
        // Initialize the probes, should register the gauges
        probes.initialize();
        
        // Verify all the Gauges were registered correctly
        // and that they return the expected values.
        ArgumentCaptor<MadGauge> maxGauge = ArgumentCaptor.forClass(MadGauge.class);
        Mockito.verify(_mockFactory).newGauge(Mockito.eq(maxName), maxGauge.capture());
        assertEquals(Integer.valueOf(1000), (Integer)maxGauge.getValue().getValue());
        
        ArgumentCaptor<MadGauge> peakGauge = ArgumentCaptor.forClass(MadGauge.class);
        Mockito.verify(_mockFactory).newGauge(Mockito.eq(peakName), peakGauge.capture());
        assertEquals(Integer.valueOf(50), (Integer)peakGauge.getValue().getValue());
        
        ArgumentCaptor<MadGauge> curGauge = ArgumentCaptor.forClass(MadGauge.class);
        Mockito.verify(_mockFactory).newGauge(Mockito.eq(curName), curGauge.capture());
        assertEquals(Integer.valueOf(5), (Integer)curGauge.getValue().getValue());
    }
    
    private MadName mockNameGeneration(String name) {
        MadName mName = new MadNameImpl(MonitoringUtils.MOCA_GROUP_NAME,
            MocaSessionProbes.PROBE_TYPE, name);
        Mockito.when(_mockFactory.newMadName(Mockito.anyString(),
            Mockito.anyString(), Mockito.eq(name)))
                .thenReturn(mName);
        return mName;
    }

    private static MadFactory _mockFactory = Mockito.mock(MadFactory.class);
    private static MadFactory _factToRestore;
}
