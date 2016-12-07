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

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.redprairie.moca.web.console.Authentication.Role;

/**
 * Information tracked by the session manager for each user session.
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class SessionData implements Serializable {
    
    /**
     * 
     */
    public SessionData(String userId, Role role, String sessionId, Date createdDate, Map<String, String> env) {
        _userId = userId;
        _sessionId = sessionId;
        _createdDate = new Date(createdDate.getTime());

        // default is no console access
        _role = role != null ? role : Role.NO_CONSOLE_ACCESS;
        
        Map<String, String> sessionEnv = new LinkedHashMap<>();
        if (env != null) {
            sessionEnv.putAll(env);
        }
        _env = Collections.unmodifiableMap(sessionEnv);
    }
    
    /**
     * @return Returns the createdDate.
     */
    public Date getCreatedDate() {
        return new Date(_createdDate.getTime());
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
     * Returns the session environment.
     * @return
     */
    public Map<String, String> getEnvironment() {
        return _env;
    }
    
    /**
     * Returns the console role privileges.
     */
    public Role getRole() {
        return _role;
    }
    
    
    private final String _userId;
    private final String _sessionId;
    private final Date _createdDate;
    private final Map<String, String> _env;
    private final Role _role;
  
    /**
     * 
     */
    private static final long serialVersionUID = -536960986747887393L;    
}
