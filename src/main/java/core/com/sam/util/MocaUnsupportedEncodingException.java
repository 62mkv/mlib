/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.sam.util;


/**
 * An exception to change the UnsupportedEncodingException to a 
 * runtime exception.  This is thrown in cases where we don't ever
 * expect to get this exception.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
public class MocaUnsupportedEncodingException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 6275311514248299917L;

    /**
     * 
     */
    public MocaUnsupportedEncodingException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public MocaUnsupportedEncodingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public MocaUnsupportedEncodingException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public MocaUnsupportedEncodingException(Throwable cause) {
        super(cause);
    }


}
