/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.sam.moca.server.log.exceptions;

import com.sam.moca.MocaRuntimeException;

/**
 * An exception that is thrown when logging internals fail
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dpiessen
 * @version $Revision$
 */
public class LoggingRuntimeException extends MocaRuntimeException {

    private static final long serialVersionUID = -3258317795258009553L;
    private static final int _errorCode = 1;

    /**
     * Creates a new {@link LoggingRuntimeException}.
     * @param message The exception message
     */
    public LoggingRuntimeException(String message) {
        super(_errorCode, message);
    }
    
    /**
     * Creates a new {@link LoggingRuntimeException}.
     * @param message The exception message.
     * @param t The inner exception class.
     */
    public LoggingRuntimeException(String message, Throwable t) {
        super(_errorCode, message, t);
    }
}
