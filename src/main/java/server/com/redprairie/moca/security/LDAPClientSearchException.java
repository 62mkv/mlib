/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20167
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

package com.redprairie.moca.security;

import com.redprairie.moca.MocaException;

/**
 * Represents an unexpected LDAP client exception.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 20167 Sam Corporation
 * All rights reserved.
 * </pre>
 * 
 * </b>
 * 
 * @author mlange
 * @version $Revision$
 */
public class LDAPClientSearchException extends MocaException {

    private static final long serialVersionUID = -8938639930823032552L;
    public static final int CODE = 753;

    /**
     * Creates an LDAP client exception with the given default error message.
     * 
     * @param message the default error message to use.
     */
    public LDAPClientSearchException(String message, Throwable cause) {
        super(CODE, message, cause);
    }
}
