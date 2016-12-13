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

package com.redprairie.moca.components.crud;

import com.redprairie.moca.MocaException;

/**
 * A MOCA exception (2003) that indicates the variable 
 * value is invalid for this column
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dpiessen
 * @version $Revision$
 */
public class InvalidValueException extends MocaException {
    public static final int CODE = 2003;
    public static final String MSG = 
        "Invalid value (^argval^) for ^argdsc^";
    
    /**
     * Create InvalidValueException.
     * @param argValue The argument name/value pair.
     * @param argId The argument id.
     */
    public InvalidValueException(String argValue, String argId) {
        super(CODE, MSG);
        addArg("argval", argValue);
        addArg("argid", argId);
        addLookupArg("argdsc", argId);
    }

    private static final long serialVersionUID = 2645764845574875100L;
}
