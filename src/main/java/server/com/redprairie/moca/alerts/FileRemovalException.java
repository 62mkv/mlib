/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 */

package com.redprairie.moca.alerts;

/**
 * File removal exception occurs when the EMS file cannot be removed,
 * making the alert potentially duplicated.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author Brandon Grady
 * @version $Revision$
 */
public class FileRemovalException extends EMSException {

    public static final int CODE = 859;

    /**
     * Error removing file.
     * @param fileName
     */
    public FileRemovalException(String fileName) {
        super(CODE, msg);
        
        addArg("filename", fileName);
    }

    // --------------------------
    // Implementation:
    // --------------------------
    private static final long serialVersionUID = -2074568137109061593L;
    private static final String msg = "File removal has failed on file ^filename^.";
}
