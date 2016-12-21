/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.sam.moca.server.exec;

import java.util.Map;

import com.sam.moca.server.log.TraceState;
import com.sam.moca.server.session.SessionToken;

/**
 * Interface representing a user's session -- shared state between requests.
 * This state can be used to tie together multiple requests into a coherent
 * "session".
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public interface SessionContext {

    public boolean isVariableMapped(String name);

    public String getVariable(String name);

    /**
     * This will put a variable name into the map
     * @param name The name of the variable to set
     * @param value The value of the variable to set
     * @return The old value of the variable or null if there was none
     */
    public String putVariable(String name, String value);

    public void removeVariable(String name);
    
    /**
     * Retrieve a copy of the list of session variables.  This map can be 
     * modified without affecting the stored variables.
     * @return The copy of the variables
     */
    public Map<String, String> getAllVariables();

    public TraceState getTraceState();

    public String getSessionId();

    public Object getAttribute(String name);

    public void putAttribute(String name, Object value);

    public Object removeAttribute(String name);

    public ServerContext takeServerContext();
    
    public ServerContext getServerContext();
    
    public void saveServerContext(ServerContext ctx);

    public SessionToken getSessionToken();
    
    public void setSessionToken(SessionToken token);

    public String getUserId();
    
    public SessionType getSessionType();
}