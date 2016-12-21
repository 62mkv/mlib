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

package com.sam.moca.alerts;

import com.sam.moca.MocaException;

/**
 * Grouping of exceptions that are related to EMS.  This 
 * exception is not generally raised by itself, but rather one of
 * it subclasses are raised.
 *  
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author Brandon Grady
 * @version $Revision$
 */
public class EMSException extends MocaException {

    /**
     * Not generally raised 
     */
    public EMSException(int code, String msg) {
        super(code, msg);
    }

    // ---------------------------------------
    // Implementation:
    // ---------------------------------------
    private static final long serialVersionUID = -5404446062864128303L;
}
