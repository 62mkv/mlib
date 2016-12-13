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

package com.redprairie.moca.components.base;

import java.util.Map;
import java.util.Map.Entry;

import com.redprairie.moca.MocaException;

/**     
 * A generic exception class that allows the population of arguments into the
 * error message.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dpiessen
 * @version $Revision$
 */
public class GenericException extends MocaException {
    
    /**
     * Creates a default generic exception.  This is equivalent to the error
     * code eERROR in C code.
     * @param message The message to put into the exception
     */
    public GenericException(String message) {
        super(_eERROR, message);
    }

    /**
     * Creates a new DynamicException object with the needed arguments
     * 
     * @param errorCode The exception error code
     * @param message The exception error message
     */
    public GenericException(int errorCode, String message,
            Map<String, Object> args, Map<String, String> lookupArgs) {
        super(errorCode, message);
        
        addArguments(args);
        addLookupArguments(lookupArgs);
    }
    
    // Implementation
    
    /**
     * Add all the regular arguments to the exception
     * @param args The argument map
     */
    private void addArguments(Map<String, Object> args) {
        if (args != null) {
            for (Entry<String, Object>entry : args.entrySet()) {
                addArg(entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     * Adds the translated arguments to the argument map
     * @param args The lookup argument map
     */
    private void addLookupArguments(Map<String, String> args) {
        if (args != null) {
            for (Entry<String, String>entry : args.entrySet()) {
                addLookupArg(entry.getKey(), entry.getValue());
            }
        }
    }

    private static final int _eERROR = 2;
    private static final long serialVersionUID = -2120033491988723346L;
}
