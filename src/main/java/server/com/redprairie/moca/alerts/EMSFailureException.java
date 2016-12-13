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

package com.redprairie.moca.alerts;

/**
 * Grouping of EMS exceptions that denote failures of the EMS server.
 * An exception of this type results in the message being moved
 * to the BAD directory.
 *  
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author Brandon Grady
 * @version $Revision$
 */
public class EMSFailureException extends EMSException {

    /**
     * Not generally raised by itself.  Rather its subclasses are raised.
     */
    public EMSFailureException(int code, String msg) {
        super(code, msg);
    }

    // ---------------------------------------
    // Implementation:
    // ---------------------------------------
    private static final long serialVersionUID = 3829221043087603579L;
}
