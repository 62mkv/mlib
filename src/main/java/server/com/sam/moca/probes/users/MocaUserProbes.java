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

package com.sam.moca.probes.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.infinispan.container.entries.InternalCacheEntry;

import com.redprairie.mad.annotations.ProbeGroup;
import com.redprairie.mad.annotations.ProbeType;
import com.redprairie.mad.client.MadGauge;
import com.redprairie.mad.probes.InitializedProbe;
import com.sam.moca.MocaConstants;
import com.sam.moca.mad.MonitoringUtils;
import com.sam.moca.mad.reporters.UserReporter;
import com.sam.moca.server.session.SessionData;

/**
 * A simple class creating a MadGauge that tracks the current number of
 * connected Users in MOCA.
 * 
 * Copyright (c) 2012 Sam Corporation All Rights Reserved
 * 
 * @author eknapp
 */
@ProbeGroup(MonitoringUtils.MOCA_GROUP_NAME)
@ProbeType("Users-Summary")
public class MocaUserProbes extends InitializedProbe {

    @Override
    public void initialize() {
        getFactory().newGauge(getMadName("unique-connected-users"), new MadGauge<Integer>() {
            @Override
            public Integer getValue() {
                Set<InternalCacheEntry> sessions = UserReporter.getSessionCacheEntries();
                return UserReporter.getUniqueUsersCount(sessions);
            }
        });
        
        getFactory().newGauge(getMadName("connected-users"), new MadGauge<List<String>>() {
            @Override
            public List<String> getValue() {
                Set<InternalCacheEntry> sessions = UserReporter.getSessionCacheEntries();
                List<String> users = new ArrayList<String>(sessions.size());
                for (InternalCacheEntry entry : sessions) {
                    SessionData data = (SessionData) entry.getValue();
                    String ip = data.getEnvironment().get(MocaConstants.WEB_CLIENT_ADDR);
                    users.add(String.format("%s (%s)", data.getUserId(), ip));
                }
                return users;
            }
        });
    }
}
