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
 * Used to indicate a failure in the MOCA protocol.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class ProtocolException extends MocaException {
    
    public static final int CODE = 207;

    /**
     * @param message
     */
    public ProtocolException(String message) {
        super(CODE, message);
    }

    /**
     * @param message
     * @param t
     */
    public ProtocolException(String message, Throwable t) {
        super(CODE, message, t);
    }

    private static final long serialVersionUID = -3373206715190277058L;
}
