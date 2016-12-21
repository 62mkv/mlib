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


/**
 * A session manager that doesn't bother tracking sessions, and all correctly-
 * formed sessions are good.  This is similar to the behavior of classic MOCA
 * session management, where a key lasts forever and cannot be revoked.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class ClassicMocaSessionManager extends BaseMocaSessionManager {
    
    public ClassicMocaSessionManager(String myDomain, String[] trustedDomains) {
        super(myDomain, trustedDomains, 0, true);
    }
    
    @Override
    protected boolean checkSession(String sessionId) {
        // All sessions are good.
        return true;
    }
    
    @Override
    protected void removeSession(String sessionId) {
        // Never remove sessions, as they're all good.
    }
    
    @Override
    protected void saveSession(String sessionId, SessionData data) {
        // don't bother to save sessions, as they're all good.
    }

    // @see com.sam.moca.server.session.BaseMocaSessionManager#getSessionData(java.lang.String)
    
    @Override
    public SessionData getSessionData(String sessionId) {
        //return
        return null;
    }
}
