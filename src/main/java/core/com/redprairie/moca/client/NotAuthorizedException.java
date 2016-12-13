/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.redprairie.moca.client;

import com.redprairie.moca.MocaException;

/**
 * 
 * Exception for denying access to MOCA services such as the Console and JMX.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author mdobrinin
 */
public class NotAuthorizedException extends MocaException {
    
    public static final int CODE = 760;

    public NotAuthorizedException(String message) {
        super(CODE, message);
    }

    public NotAuthorizedException(String message, Throwable t) {
        super(CODE, message, t);
    }
    
    private static final long serialVersionUID = 1583162928015005635L;
}
