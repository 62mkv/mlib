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

package com.redprairie.moca.server.exec;

import com.redprairie.moca.MocaException;

/**
 * Thrown by the JavaMOCA executive when a class that has been indicated as
 * the implementaiton class for a component is not found.  This exception class
 * can also be thrown if a named class is found, but no method is found matching
 * a component definition.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author Derek Inksetter
 * @version $Revision$
 */
public class ComponentNotFoundException extends MocaException {
    /**
     * The MOCA error code returned when this exception is thrown. 
     */
    public static final int CODE = 530;

    /**
     * Indicates that the given class was not found.
     * @param className
     */
    public ComponentNotFoundException(String className) {
        super(CODE, _MESSAGE);
        addArg("class", className);
        addArg("method", "");
    }

    /**
     * @param className
     */
    public ComponentNotFoundException(String className, String methodName) {
        super(CODE, _MESSAGE);
        addArg("class", className);
        addArg("method", "." + methodName);
    }
    
    private static final String _MESSAGE =
            "Component (^class^^method^) not found"; 

    private static final long serialVersionUID = 3617292315970844214L;
}
