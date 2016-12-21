/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.sam.moca.server.session;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.sam.moca.server.SecurityLevel;

/**
 * A session token, representing all the information that's in the session key
 * value returned from the login components, and passed along with the server
 * environment.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class SessionToken implements Serializable {
    
    public SessionToken(String domain, String userId, String sessionId,
                        Date createdDate, Map<String, String> attributes,
                        SecurityLevel security) {
       _domain = domain;
       _userId = userId;
       _sessionId = sessionId;
       _createdDate = createdDate == null ? null : createdDate.getTime();
       _attributes = new HashMap<String, String>(attributes);
       _securityLevel = security;
    }
    
    /**
     * Creates a session using only a user ID.  This type of session is
     * common for server-side sessions.
     * @param userId
     */
    public SessionToken(String userId) {
        _domain = "";
        _userId = userId;
        _sessionId = null;
        _createdDate = System.currentTimeMillis();
        _attributes = new HashMap<String, String>();
        _securityLevel = SecurityLevel.ALL;
    }
    
    /**
     * @return Returns the createdDate.
     */
    public Date getCreatedDate() {
        return (_createdDate == null) ? null : new Date(_createdDate);
    }
    
    /**
     * @return Returns the sessionId.
     */
    public String getSessionId() {
        return _sessionId;
    }
    
    /**
     * @return Returns the userId.
     */
    public String getUserId() {
        return _userId;
    }
    
    /**
     * @return Returns the domain.
     */
    public String getDomain() {
        return _domain;
    }
    
    public String getAttribute(String key) {
        return _attributes.get(key);
    }
    
    public SecurityLevel getSecurityLevel() {
        return _securityLevel;
    }
    
    private static final long serialVersionUID = 8282714642292888027L;

    private final String _userId;
    private final String _domain;
    private final Long _createdDate;
    private final String _sessionId;
    private final SecurityLevel _securityLevel;
    private final Map<String, String> _attributes;
}
