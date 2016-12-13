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

package com.redprairie.moca.components.base;

import com.redprairie.moca.MocaException;

/**
 * Thrown by the <code>SecureFTP</code> components if there is
 * a communcation failure with the SFTP server.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class EmailServiceException extends MocaException {
	public static final int CODE = 803;
    public static final String MSG="SMTP communication failure (^detail^)";

    public EmailServiceException(String detail) {
        super(CODE, MSG);
        addArg("detail", detail);
    }
    
    public EmailServiceException(String detail, Throwable cause) {
    	super(CODE, MSG, cause);
    	addArg("detail", detail);
    }
    
	private static final long serialVersionUID = 2195712567448764863L;
}
