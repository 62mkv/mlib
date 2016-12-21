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
 * Invalid Event Name exception is raised when an alert is passed to EMS
 * that has an invalid event name.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author Brandon Grady
 * @version $Revision$
 */
public class InvalidEventNameException extends EMSFailureException {

    public static final int CODE = 855;

    /**
     * An alert whose event has not been primed was passed to EMS. 
     * @param event
     */
    public InvalidEventNameException(String event) {
        super(CODE, msg);
        
        addArg("event", event);
    }

    // --------------------------------
    // Implementation:
    // --------------------------------
    private static final long serialVersionUID = -427874104282108999L;
    private static final String msg = "XML passed with and Invalid Event Name: ^event^.";
}
