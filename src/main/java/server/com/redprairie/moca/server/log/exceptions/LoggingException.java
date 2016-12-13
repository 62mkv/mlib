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

package com.redprairie.moca.server.log.exceptions;

import com.redprairie.moca.MocaException;

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
public class LoggingException extends MocaException {

    private static final long serialVersionUID = -3258317795258009553L;
    private static final int _errorCode = 1;

    /**
     * Creates a new {@link LoggingException}.
     * @param message The exception message
     */
    public LoggingException(String message) {
        super(_errorCode, message);
    }
    
    /**
     * Creates a new {@link LoggingException}.
     * @param message The exception message.
     * @param t The inner exception class.
     */
    public LoggingException(String message, Throwable t) {
        super(_errorCode, message, t);
    }
}
