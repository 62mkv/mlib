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

package com.redprairie.moca.server.socket;

import com.redprairie.moca.MocaResults;
import com.redprairie.moca.server.dispatch.DispatchResult;

/**
 * A class that holds the result of a command execution initiated by the client.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class MocaResponse {
    public MocaResponse(String encryptionType, DispatchResult result) {
        _encryptionType = encryptionType;
        _result = result;
    }
    
    public MocaResults getResults() {
        return _result.getResults();
    }
    
    public int getStatus() {
        return _result.getStatus();
    }
    
    public String getMessage() {
        return _result.getMessage();
    }
    
    public String getEncryptionType() {
        return _encryptionType;
    }
    
    private DispatchResult _result;
    private String _encryptionType;
}
