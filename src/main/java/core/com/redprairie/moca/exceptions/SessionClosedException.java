/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.exceptions;

import com.redprairie.moca.MocaException;

/**
 * This exception is thrown to a client when a request comes in that wants
 * a specific session but that session is no longer available.  This could
 * include a database transaciton that has timed out as well.
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class SessionClosedException extends MocaException {
    public static final int CODE = 542;

    public SessionClosedException() {
        super(CODE, "Session was closed due to inactivity.  Previous " +
                "transaction was rolled back if applicable.  Next request " +
                "will start a new session.");
    }
    
    private static final long serialVersionUID = -286402764291340846L;
}
