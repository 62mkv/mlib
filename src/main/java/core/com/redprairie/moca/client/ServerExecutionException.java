/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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

package com.redprairie.moca.client;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;


/**
 * Package-private exception class that acts as the default MOCA exception in
 * cases where a more specific exception cannot be established.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author Derek Inksetter
 * @version $Revision$
 */
class ServerExecutionException extends MocaException {

    /**
     * @param code the error code returned from the server.
     * @param message the default message returned from the server.
     * @param res the results object returned with the error.
     */
    public ServerExecutionException(int code, String message) {
        super(code, message);
    }
    
    public ServerExecutionException(int code, String message, MocaResults results) {
        super(code, message);
        setResults(results);
    }
    
    public ServerExecutionException(int code, String message, MocaResults results, Throwable t) {
        super(code, message, t);
        setResults(results);
    }
    
    @Override
    public String toString() {
        return super.toString() + " (ERR=" + getErrorCode() +")";
    }
    
    @Override
    public boolean isMessageResolved() {
        return true;
    }

    private static final long serialVersionUID = 3256442512435853365L;
}    
