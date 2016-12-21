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

package com.sam.moca.servlet;

import com.sam.moca.server.exec.SessionContext;

/**
 * This exception is thrown when trying to retrieve a session that is already
 * running.  A session should only be invoked from one thread at a time.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class SessionRunningException extends Exception {
    private static final long serialVersionUID = 5507152685232372131L;
    
    public SessionRunningException(SessionContext sessionCtx) {
        super("Session " + sessionCtx.getSessionId() + " was already running!");
        _session = sessionCtx;
    }
    
    public SessionContext getSessionContext() {
        return _session;
    }
    
    private final SessionContext _session;

}
