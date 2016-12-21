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
 * File creation exception occurs when the EMS file cannot be created,
 * thus the alert will not be raised.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author Brandon Grady
 * @version $Revision$
 */
public class FileReadException extends EMSException {

    public static final int CODE = 862;

    /**
     * Error reading the file.
     * @param message
     */
    public FileReadException(String message) {
        super(CODE, msg + " - " + message);
    }
    
    /**
     * Error reading the file.
     */
    public FileReadException() {
        super(CODE, msg);
    }

    // -------------------------------
    // Implementation:
    // -------------------------------
    private static final long serialVersionUID = -3055777201469212088L;
    private static final String msg = "File read error: ";
}
