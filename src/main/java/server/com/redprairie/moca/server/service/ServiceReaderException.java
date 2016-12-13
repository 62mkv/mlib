/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.server.service;

import com.redprairie.moca.MocaException;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author mlange
 */
public class ServiceReaderException extends MocaException {
    
    public static final int CODE = 816;

    public ServiceReaderException() {
        super(CODE);
    }
    
    public ServiceReaderException(String msg) {
        super(CODE, msg);
    }
    
    public ServiceReaderException(String msg, Throwable t) {
        super(CODE, msg, t);
    }

    private static final long serialVersionUID = -5785795423717869076L;
}