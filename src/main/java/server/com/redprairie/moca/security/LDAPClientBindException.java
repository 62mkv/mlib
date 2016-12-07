/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2007
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

package com.redprairie.moca.security;

import com.redprairie.moca.MocaException;

/**
 * Represents an unexpected LDAP client exception.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All rights reserved.
 * </pre>
 * 
 * </b>
 * 
 * @author mlange
 * @version $Revision$
 */
public class LDAPClientBindException extends MocaException {

    private static final long serialVersionUID = -5926247130386272566L;
    public static final int CODE = 751;
    public static final String MESSAGE = "LDAP client bind failure (^detail^)";

    /**
     * Creates an LDAP client bind exception with the given default error
     * message.
     * 
     * @param detail the default error message to use.
     */
    public LDAPClientBindException(String detail, Throwable cause) {
        super(CODE, MESSAGE, cause);
        addArg("detail", detail);
    }
}
