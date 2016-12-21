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

package com.sam.moca.server.legacy;

import com.sam.moca.MocaException;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MocaNativeException extends MocaException {
    
    private static final long serialVersionUID = 5270094879034675771L;
    public static final int CODE = 516;
    
    /**
     * 
     */
    public MocaNativeException(String library, String function, String message) {
        super(CODE, "Invalid native library/function: (^library^/^function^) - ^message^");
        addArg("library", library);
        addArg("function", function);
        addArg("message", message);
    }
}
