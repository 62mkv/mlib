/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.server.session;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class InMemoryMocaSessionManager extends BaseMocaSessionManager {
    
    public InMemoryMocaSessionManager(String myDomain, String[] trustedDomains,
                                      int sessionTimeoutSecs, int remoteWindowSecs,
                                      boolean allowLegacySessions) {
        super(myDomain, trustedDomains, remoteWindowSecs, allowLegacySessions);
        _cache = CacheBuilder.newBuilder()
                .expireAfterAccess(sessionTimeoutSecs, TimeUnit.SECONDS)
                .<String,Boolean>build(); 
    }
    
    @Override
    protected boolean checkSession(String sessionId) {
       return _cache.getIfPresent(sessionId) != null;
    }
    
    @Override
    protected void saveSession(String sessionId, SessionData data) {
        _cache.put(sessionId, Boolean.TRUE);
    }
    
    @Override
    protected void removeSession(String sessionId) {
        _cache.invalidate(sessionId);
    }
    
    @Override
    public SessionData getSessionData(String sessionId) {
        return null;
    }
    
    private Cache<String,Boolean> _cache;
}
