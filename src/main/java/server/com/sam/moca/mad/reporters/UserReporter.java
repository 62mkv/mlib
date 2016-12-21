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

package com.sam.moca.mad.reporters;

import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.infinispan.Cache;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.manager.EmbeddedCacheManager;

import com.sam.moca.MocaConstants;
import com.sam.moca.cache.infinispan.InfinispanCacheProvider;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.session.SessionData;

/**
 * A Class that formats a text report consisting of summary information about
 * the set of connected users in MOCA and informationa bout each individual user
 * inside of MOCA.
 * 
 * Copyright (c) 2012 Sam Corporation All Rights Reserved
 * 
 * @author eknapp
 */
public class UserReporter {

    public static String generateUserReport() {
        StringBuilder builder = new StringBuilder();
        Formatter formatter = new Formatter(builder);

        // Get the Sessions cache entries...
        Set<InternalCacheEntry> entries = getSessionCacheEntries();

        // Print to the buffer the number of users...
        String format = "%1$-25s%2$-10s%n%n";
        formatter.format(format, "Connected-Users:",
            String.valueOf(getUniqueUsersCount(entries)));

        // For each Session, print out information regarding the user...
        format = "%1$-25s%2$-70s%n";
        for (InternalCacheEntry entry : entries) {
            SessionData data = (SessionData) entry.getValue();
            formatter.format(format, "User-ID:", String.valueOf(data.getUserId()));
            formatter.format(format, "Session-ID:", String.valueOf(data.getSessionId()));
            formatter.format(format, "Created-Date:",
                String.valueOf(data.getCreatedDate()));
            Map<String, String> env = new HashMap<String, String>(data.getEnvironment());
            String ip = env.remove(MocaConstants.WEB_CLIENT_ADDR);
            formatter.format(format, "IP:", ip);
            formatter.format(format, "Environment:",
                String.valueOf(env));
            formatter.format(format, "Last-Accessed-Date:",
                String.valueOf(new Date(entry.getLastUsed())));
            builder.append("\n");
        }

        // Close the formatter and return the report.
        String report = builder.toString();
        formatter.close();
        return report;
    }

    public static int getUniqueUsersCount(Set<InternalCacheEntry> sessions) {
        Set<String> users = new HashSet<String>();
        for (InternalCacheEntry session : sessions) {
            users.add(((SessionData) session.getValue()).getUserId());
        }
        return users.size();
    }

    public static Set<InternalCacheEntry> getSessionCacheEntries() {
        EmbeddedCacheManager manager = InfinispanCacheProvider
            .getInfinispanCacheManager(ServerUtils.globalContext());
        Cache<?, SessionData> cache = manager.getCache("moca-sessions");
        return cache.getAdvancedCache().getDataContainer().entrySet();
    }

}
