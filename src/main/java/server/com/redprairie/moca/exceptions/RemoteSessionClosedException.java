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
 * This exception is thrown when a remote call fails due to the remote session
 * being closed.  This can occur on any call including commit.
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class RemoteSessionClosedException extends MocaException {
    public static final int CODE = 543;
    
    /**
     * @param errorCode
     * @param message
     */
    public RemoteSessionClosedException() {
        super(CODE, "Remote session was closed due to delay between requests.  " +
            "Local transaction(if any) was rolled back to guarantee consistency.");
    }
    
    private static final long serialVersionUID = -6715591088179463002L;
}
