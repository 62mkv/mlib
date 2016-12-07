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

import java.util.Map;

import com.redprairie.moca.server.SecurityLevel;
import com.redprairie.moca.web.console.Authentication.Role;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public interface MocaSessionManager {
    /**
     * Creates and encodes a new session. The encoded token can be used as an
     * argument to the validate method of this class, and produce a valid
     * SessionToken object. The encoded token will be specific to this
     * particular session manager. Any clustered servers must utilize the same
     * session manager (or one that uses the same session store) to ensure that
     * client-initiated tracked sessions remain valid.
     * 
     * @param userId the user ID to use to create the tracked security token
     * @param role 
     * @param env the environment to track, along with the session.  This may
     * or may not be used in the creation of the session token.
     * @param security what security level to create the key at.
     * 
     * @return
     */
    public String createTracked(String userId, Role role, Map<String, String> env, SecurityLevel security);
    
    public String createRemote(String userId);

    /**
     * Decodes a session token string into a SessionToken object. If the session
     * token string does not correspond to a valid session, then
     * <code>null</code> is returned.
     * 
     * @param tokenString
     * @return a SessionToken corresponding to the given token string.
     */
    public SessionToken validate(String tokenString);
    
    
    /***
     * Returns the <code>SessionData</code> from the specified session key.  If the
     * session doesn't exist, it returns <code>null</code>.
     * 
     * @param sessionKey
     * @return <code>SessionData</code> corresponding to the given session key, otherwise 
     *   <code>null</code>.
     */
    public SessionData getSessionData(String sessionKey);
    
    public void close(SessionToken token);
}
