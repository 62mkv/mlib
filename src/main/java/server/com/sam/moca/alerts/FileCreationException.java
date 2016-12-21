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
public class FileCreationException extends EMSException {

    public static final int CODE = 858;

    /**
     * Error creating file.
     */
    public FileCreationException() {
        super(CODE, msg);
    }
    
    /**
     * Error creating the file.
     * @param filename
     */
    public FileCreationException(String filename) {
        super(CODE, msg2);
        addArg("filename", filename);
    }
    
    // ------------------------------
    // Implementation:
    // ------------------------------
    private static final long serialVersionUID = 6752423074726964397L;
    private static final String msg = "File creation has failed.";
    private static final String msg2 = "File ^filename^ has failed to create.";
}
