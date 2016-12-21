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

package com.sam.moca.components.base;

import com.sam.moca.MocaException;

/**
 * Thrown by the <code>HTTP</code> components if there is
 * a communication failure.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 */
public class HttpServiceException extends MocaException {
    public static final int CODE = 352;
    
    public HttpServiceException(String detail, Throwable cause) {
        super(CODE, "HTTP communication failure (^detail^)", cause);
        addArg("detail", detail);
    }
    private static final long serialVersionUID = -6865414324357629696L;
}