/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.sam.moca.server.repository.docs;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class CommandDocumentationException extends Exception {

    public CommandDocumentationException(String msg) {
        super(msg);
    }

    public CommandDocumentationException(Throwable e) {
        super("Error writing documentation: " + e, e);
    }
        
    public CommandDocumentationException(String msg, Throwable e) {
        super(msg, e);
    }
        
    private static final long serialVersionUID = 5820518008564074775L;
}
