/*
 *  $RCSfile$
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

package com.sam.moca.exceptions;

import com.sam.moca.MocaException;

/**
 * An exception raised when a MOCA command argument is missing.
 *
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 *
 * @author mlange
 * @version $Revision$
 */
public class InvalidArgumentException extends MocaException {

	public static final int CODE = 810;
    
    public InvalidArgumentException(String name) {
        super(CODE, "Invalid argument: ^name^");
        addArg("name", name);
    }
    
    public InvalidArgumentException(String name, String desc) {
        super(CODE, "Invalid argument: ^desc^ (^name^)");
        addArg("desc", desc);
        addArg("name", name);
    }
    
	private static final long serialVersionUID = 9109754553410678391L;
}
