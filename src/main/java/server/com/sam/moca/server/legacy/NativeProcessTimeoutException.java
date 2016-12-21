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

package com.sam.moca.server.legacy;

import com.sam.moca.MocaException;

/**
 * This class represents an exception when a native process was not able to
 * be retrieved in the allotted time.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class NativeProcessTimeoutException extends MocaException {
    public static final int CODE = 536;
    
    private static final long serialVersionUID = -4806273043696951699L;
    
    /**
     * @param errorCode
     * @param message
     */
    public NativeProcessTimeoutException(String message) {
        super(CODE, message);
    }
}
