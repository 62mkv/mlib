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

package com.sam.moca.client;

import com.sam.moca.MocaException;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class ConnectionTimeoutException extends MocaException {
    
    public static final int CODE = 301;

    /**
     * @param message
     */
    public ConnectionTimeoutException(String message) {
        super(CODE, message);
    }

    /**
     * @param message
     * @param t
     */
    public ConnectionTimeoutException(String message, Throwable t) {
        super(CODE, message, t);
    }

    private static final long serialVersionUID = -6930058484169642271L;
}
