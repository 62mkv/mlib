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

package com.sam.moca.server.expression.function;

import com.sam.moca.MocaException;

/**
 * Thrown to indicate a problem with a function argument. Since function
 * arguments are not validated against known type metadata, it is up to the
 * function implementation to perform any argument type and precondition
 * testing, including whether the right number of arguments were passed.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author derek
 * @version $Revision$
 */
public class FunctionArgumentException extends MocaException {
    private static final long serialVersionUID = 1296871515638306412L;
    public static final int CODE = 3;
    
    public FunctionArgumentException(String message) {
        this(message, null);
    }
    
    public FunctionArgumentException(String message, Throwable e) {
        super(CODE, "Invalid function argument: ^message^", e);
        addArg("message", message);
    }
}
