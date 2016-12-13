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

package com.redprairie.moca.xml;

import com.redprairie.moca.MocaException;

/**
 * Represents an unexpected XMLAdapter exception.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author mlange
 * @version $Revision$
 */
public class XMLAdapterException extends MocaException {

    private static final long serialVersionUID = 5408778087713147806L;
    public static final int CODE = 600;

    /**
     * Creates an XMLAdapter exception with the given default error message.
     * 
     * @param detail the default error message to use.
     */
    public XMLAdapterException(String detail) {
        super(CODE, detail);
    }

    /**
     * Creates an XMLAdapter exception with the given default error message.
     * 
     * @param detail the default error message to use.
     */
    public XMLAdapterException(String detail, Throwable cause) {
        super(CODE, detail, cause);
    }

    /**
     * Returns the error code associated with this error.
     * 
     * @return the error code for this error
     */
    public int getErrorCode() {
        return super.getErrorCode();
    }

    /**
     * Returns the error message associated with this error.
     * 
     * @return the error message for this error
     */
    public String getErrorMessage() {
        return super.toString();
    }
}