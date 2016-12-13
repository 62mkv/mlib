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

package com.redprairie.moca.server.repository.file;

/**
 * Thrown when the MOCA Command reader gets an error reading a command (or other) definition.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author cjolly
 * @version $Revision$
 */
public class RepositoryReadException extends Exception {

    private static final long serialVersionUID = -5560351400493204156L;

    /**
     * @param message
     */
    public RepositoryReadException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public RepositoryReadException(String message, Throwable cause) {
        super(message, cause);
    }
}
