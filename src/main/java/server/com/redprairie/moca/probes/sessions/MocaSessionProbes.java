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

import com.redprairie.mad.annotations.ProbeGroup;
import com.redprairie.mad.annotations.ProbeType;
import com.redprairie.mad.client.MadGauge;
import com.redprairie.mad.probes.InitializedProbe;
import com.redprairie.moca.mad.MonitoringUtils;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.servlet.WebSessionManager;

/**
 * A class to initialize the Session Summary Probes. The probes will track the
 * number of current sessions registered in MOCA, the peak number of sessions
 * registered to MOCA at a single time, and the maximum number of sessions that
 * can be registered to MOCA at once.
 * 
 * Copyright (c) 2012 RedPrairie Corporation All Rights Reserved
 * 
 * @author eknapp
 */
@ProbeGroup(MonitoringUtils.MOCA_GROUP_NAME)
@ProbeType(MocaSessionProbes.PROBE_TYPE)
public class MocaSessionProbes extends InitializedProbe {
    
    public static final String PROBE_TYPE = "Sessions-Summary";
    
    // Probe names
    public static final String CURRENT_SESSIONS = "current-sessions";
    public static final String MAX_SESSIONS = "maximum-sessions";
    public static final String PEAK_SESSIONS = "peak-sessions";

    @Override
    public void initialize() {
        // Current Session Count Probe...
        getFactory().newGauge(getMadName(CURRENT_SESSIONS), new MadGauge<Integer>() {
            @Override
            public Integer getValue() {
                return getSessionManager().getSessionCount();
            }
        });

        // Maximum Amount of Possible Sessions Probe...
        getFactory().newGauge(getMadName(MAX_SESSIONS), new MadGauge<Integer>() {
            @Override
            public Integer getValue() {
                return getSessionManager().getMaxSessions();
            }
        });

        // Peak Number of Sessions as a single instance Probe...
        getFactory().newGauge(getMadName(PEAK_SESSIONS), new MadGauge<Integer>() {
            @Override
            public Integer getValue() {
                return getSessionManager().getPeakSessionCount();
            }
        });
    }
    
    protected WebSessionManager getSessionManager() {
        return (WebSessionManager) ServerUtils.globalContext()
            .getAttribute(WebSessionManager.class.getName());
    }

}
