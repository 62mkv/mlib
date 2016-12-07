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

import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.cache.infinispan.InfinispanCacheProvider;
import com.redprairie.moca.util.MocaUtils;

/**
 * MOCA Session manager using infinispan's clustered cache technology.
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class InfinispanMocaSessionManager extends BaseMocaSessionManager {
    
    public InfinispanMocaSessionManager(String myDomain, String[] trustedDomains,
                                      String cacheName, int remoteWindowSecs,
                                      boolean allowLegacySessions) {
        super(myDomain, trustedDomains, remoteWindowSecs, allowLegacySessions);
        
        _sessionMap = new InfinispanCacheProvider().getCache(cacheName);
        _logger.debug(MocaUtils.concat("Using distributed cache: ", _sessionMap));
    }
    
    @Override
    protected boolean checkSession(String sessionId) {
     
        _logger.debug(MocaUtils.concat("Validating session ID ", sessionId));
        SessionData data = _sessionMap.get(sessionId);
        if (data == null) {
            _logger.debug(MocaUtils.concat("Invalid session ID: ", sessionId));
            return false;
        }
        else {
            return true;
        }
    }
    
    @Override
    protected void saveSession(String sessionId, SessionData data) {
        _sessionMap.put(sessionId, data);
    }
    
    @Override
    public SessionData getSessionData(String sessionId) {
        return _sessionMap.get(sessionId);
    }
    
    @Override
    protected void removeSession(String sessionId) {
        _sessionMap.remove(sessionId);
    }
    
    ConcurrentMap<String, SessionData> _sessionMap;
    
    static final Logger _logger = LogManager.getLogger(InfinispanMocaSessionManager.class);
}
