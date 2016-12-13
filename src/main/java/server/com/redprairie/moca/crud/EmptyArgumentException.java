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

package com.redprairie.moca.crud;

import com.redprairie.moca.exceptions.MissingArgumentException;

/** An override class of a {@link MissingArgumentException} (802) that
 *  differentiates between a missing argument and one that is empty.
 *  
 * @author dpiessen
 *
 */
public class EmptyArgumentException extends MissingArgumentException {

    public EmptyArgumentException(String argumentName) {
        super(argumentName);
    }
    
    public EmptyArgumentException(String argumentName, Throwable t) {
        super(argumentName, t);
    }
    
    /**
     * 
     */
    private static final long serialVersionUID = -2552878230828332529L;
}
