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

package com.redprairie.moca.probes.nativeprocesses;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.redprairie.mad.client.MadGauge;
import com.redprairie.mad.client.MadName;
import com.redprairie.mad.client.MadNameImpl;
import com.redprairie.mad.probes.AbstractMadFactoryTest;
import com.redprairie.moca.mad.MonitoringUtils;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.legacy.NativeProcessPool;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A simple class to test the Native Process Summary Probes creation.
 * 
 * Copyright (c) 2012 RedPrairie Corporation All Rights Reserved
 * 
 * @author eknapp
 */
public class TU_NativeProcessSummaryProbes extends AbstractMadFactoryTest {

    public static final String GROUP = MonitoringUtils.MOCA_GROUP_NAME;

    public static final String TYPE = NativeProcessSummaryProbes.METRIC_TYPE_NATIVE_POOL;

    private static final int SIZE = 4;

    private static final int MAX = 20;

    private static final int PEAK = 10;

    @Mock
    private NativeProcessPool mockPool;

    @Mock
    private ServerContextFactory mockFactory;

    private final ServerContextFactory prevFactory = ServerUtils
        .globalAttribute(ServerContextFactory.class);

    @Before
    public void testDataSetup() {
        // Initialize the mocks.
        MockitoAnnotations.initMocks(this);

        // Setup the mocks, so they function properly.
        when(mockFactory.getNativePool()).thenReturn(mockPool);
        when(mockPool.getMaximumSize()).thenReturn(MAX);
        when(mockPool.getPeakSize()).thenReturn(PEAK);
        when(mockPool.getSize()).thenReturn(SIZE);

        // Set the server context factory to be the mock instance.
        ServerUtils.globalContext().putAttribute(ServerContextFactory.class.getName(),
            mockFactory);
    }

    @After
    public void testDataCleanup() {
        // Set the server context factory to be the previous version.
        ServerUtils.globalContext().putAttribute(ServerContextFactory.class.getName(),
            prevFactory);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testCreateProbes() {
        // Setup the mocks for the probes.
        MadName sizeName = mockNameGeneration(NativeProcessSummaryProbes.PROBE_NAME_PROCESSES);
        MadName peakName = mockNameGeneration(NativeProcessSummaryProbes.PROBE_NAME_PEAK_PROC);
        MadName maxName = mockNameGeneration(NativeProcessSummaryProbes.PROBE_NAME_MAX_PROC);

        // Create the native process summary probes and initialize them.
        NativeProcessSummaryProbes probes = new NativeProcessSummaryProbes();
        probes.initialize();

        // Verify that the probes were called to be created and the that gauges
        // do in fact behave as expected.
        ArgumentCaptor<MadGauge> sizeGauge = ArgumentCaptor.forClass(MadGauge.class);
        verify(getFactory()).newGauge(Mockito.eq(sizeName), sizeGauge.capture());
        assertEquals(SIZE, sizeGauge.getValue().getValue());

        ArgumentCaptor<MadGauge> peakGauge = ArgumentCaptor.forClass(MadGauge.class);
        verify(getFactory()).newGauge(Mockito.eq(peakName), peakGauge.capture());
        assertEquals(PEAK, peakGauge.getValue().getValue());

        ArgumentCaptor<MadGauge> maxGauge = ArgumentCaptor.forClass(MadGauge.class);
        verify(getFactory()).newGauge(Mockito.eq(maxName), maxGauge.capture());
        assertEquals(MAX, maxGauge.getValue().getValue());

    }

    private MadName mockNameGeneration(String name) {
        MadName mName = new MadNameImpl(GROUP, TYPE, name);
        Mockito.when(getFactory().newMadName(Mockito.eq(GROUP), Mockito.eq(TYPE), Mockito.eq(name)))
            .thenReturn(mName);
        return mName;
    }

}
