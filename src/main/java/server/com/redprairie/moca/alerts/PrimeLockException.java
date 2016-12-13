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

package com.redprairie.moca.alerts;

/**
 * Prime Lock Exception is raised when a Prime is attempted on an
 * existing event configuration with the prime lock flag set to true.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author Brandon Grady
 * @version $Revision$
 */
public class PrimeLockException extends EMSFailureException {

    public static final int CODE = 857;
    
    /**
     * A locked event configuration was attempted to be re-primed.
     * @param event
     */
    public PrimeLockException(String event) {
        super(CODE, msg);
        
        addArg("event", event);
    }
    
    // -----------------------------
    // Implentation:
    // -----------------------------
    private static final long serialVersionUID = 8155671345128399939L;
    private static final String msg = "Event ^event^ cannot be primed as priming is locked.";
}
