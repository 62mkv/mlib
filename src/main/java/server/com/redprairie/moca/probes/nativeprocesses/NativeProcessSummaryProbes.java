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

import com.redprairie.mad.annotations.ProbeGroup;
import com.redprairie.mad.annotations.ProbeType;
import com.redprairie.mad.client.MadGauge;
import com.redprairie.mad.probes.InitializedProbe;
import com.redprairie.moca.mad.MonitoringUtils;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.legacy.NativeProcessPool;

/**
 * An initialized Probe class that creates Probes related to the Native Process
 * pool within MOCA. The probes summarize the size, max-size, and peak-size of
 * the Native Process Pool in MOCA.
 * 
 * Copyright (c) 2012 RedPrairie Corporation All Rights Reserved
 * 
 * @author eknapp
 */
@ProbeGroup(MonitoringUtils.MOCA_GROUP_NAME)
@ProbeType(NativeProcessSummaryProbes.METRIC_TYPE_NATIVE_POOL)
public class NativeProcessSummaryProbes extends InitializedProbe {

    public static final String METRIC_TYPE_NATIVE_POOL = "Native-Processes-Summary";

    public static final String PROBE_NAME_MAX_PROC = "maximum-processes";
    public static final String PROBE_NAME_PEAK_PROC = "peak-processes";
    public static final String PROBE_NAME_PROCESSES = "current-processes";
    
    // @see com.redprairie.mad.probes.InitializedProbe#initialize()
    @Override
    public void initialize() {
        getFactory().newGauge(getMadName(PROBE_NAME_MAX_PROC), new MadGauge<Integer>() {
            @Override
            public Integer getValue() {
                ServerContextFactory factory = ServerUtils
                    .globalAttribute(ServerContextFactory.class);
                NativeProcessPool pool = factory.getNativePool();
                return pool.getMaximumSize();
            }
        });

        getFactory().newGauge(getMadName(PROBE_NAME_PEAK_PROC), new MadGauge<Integer>() {
            @Override
            public Integer getValue() {
                ServerContextFactory factory = ServerUtils
                    .globalAttribute(ServerContextFactory.class);
                final NativeProcessPool pool = factory.getNativePool();
                return pool.getPeakSize();
            }
        });

        getFactory().newGauge(getMadName(PROBE_NAME_PROCESSES), new MadGauge<Integer>() {
            @Override
            public Integer getValue() {
                ServerContextFactory factory = ServerUtils
                    .globalAttribute(ServerContextFactory.class);
                final NativeProcessPool pool = factory.getNativePool();
                return pool.getSize();
            }
        });
    }
}
