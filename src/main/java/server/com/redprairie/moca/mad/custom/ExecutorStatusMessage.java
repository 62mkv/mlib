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

package com.redprairie.moca.mad.custom;

import com.redprairie.mad.protocol.MadMessageCustom;

/**
 * Custom message for AsynchronousExecutor status
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author klucas
 */
public class ExecutorStatusMessage extends MadMessageCustom {
    /**
     * Creates a new message for AsynchronousExecutor status
     * 
     * @param customType
     * @param sessionId
     * @param status
     */
    public ExecutorStatusMessage(int customType, String sessionId, String status) {
        super(customType);
        
        _sessionId = sessionId;
        _status = status;
    }
    
    /**
     * @return The sessionId
     */
    public String getSessionId() {
        return _sessionId;
    }
    
    /**
     * @return The status
     */
    public String getStatus() {
        return _status;
    }
    
    private final String _sessionId;
    private final String _status;
}
