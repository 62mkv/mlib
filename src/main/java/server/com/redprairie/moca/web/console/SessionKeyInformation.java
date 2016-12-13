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

package com.redprairie.moca.web.console;

import java.util.Map;

import org.infinispan.Cache;
import org.infinispan.container.DataContainer;
import org.infinispan.container.entries.InternalCacheEntry;
import org.joda.time.DateTime;

import com.redprairie.moca.server.session.SessionData;
import com.redprairie.moca.web.WebResults;

/**
 * Session Key Information provider
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class SessionKeyInformation {
    public SessionKeyInformation(Cache<?, SessionData> cache) {
        _cache = cache;
    }
    
    public WebResults<SessionDataBean> getSessionKeyInfo() {
        WebResults<SessionDataBean> results = new WebResults<SessionDataBean>();
        
        DataContainer container = _cache.getAdvancedCache().getDataContainer();
        for (InternalCacheEntry entry : container.entrySet()) {
            SessionDataBean bean = new SessionDataBean(
                (SessionData)entry.getValue(), new DateTime(entry.getLastUsed()));
            results.add(bean);
        }
        
        return results;
    }
    
    protected static class SessionDataBean {
        
        public SessionDataBean(SessionData sessionData, DateTime lastAccess) {
            _createdDate = new DateTime(sessionData.getCreatedDate().getTime());
            _environment = sessionData.getEnvironment();
            _sessionId = sessionData.getSessionId();
            _userId = sessionData.getUserId();
            _lastAccess = lastAccess;
            _role = sessionData.getRole().toString();
        }
        
        /**
         * @return Returns the userId.
         */
        public String getUserId() {
            return _userId;
        }
        /**
         * @return Returns the sessionId.
         */
        public String getSessionId() {
            return _sessionId;
        }
        /**
         * @return Returns the environment.
         */
        public Map<?, ?> getEnvironment() {
            return _environment;
        }
        /**
         * @return Returns the lastAccess.
         */
        public DateTime getLastAccess() {
            return _lastAccess;
        }
        /**
         * @return Returns the createdDate.
         */
        public DateTime getCreatedDate() {
            return _createdDate;
        }
        
        public String getRole() {
            return _role;
        }
        
        private final String _userId;
        private final String _sessionId;
        private final Map<?, ?> _environment;
        private final DateTime _lastAccess;
        private final DateTime _createdDate;
        private final String _role;
    }
    
    private final Cache<?, SessionData> _cache;
}
