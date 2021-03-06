/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

/**
 * Bad XML Exception is raised whenever an alert is raised with invalid XML.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author Brandon Grady
 * @version $Revision$
 */
public class BadXMLException extends EMSFailureException {

    public static final int CODE = 856;

    /**
     * XML message passed to EMS was malformed.
     * @param message
     */
    public BadXMLException(String message) {
        super(CODE, msg);
        addArg("message", message);
    }
    
    // --------------------------------
    // Implementation:
    // --------------------------------
    private static final long serialVersionUID = -3567771217498938620L;
    private static final String msg = "Invalid XML passed to EMS: ^message^";

}
