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

package com.sam.moca.server.db.translate;

import com.sam.moca.MocaException;

/**
 * A base exception that indicates a translation error occurred.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TranslationException extends MocaException {
    
    public static final int CODE = 103;
    
    /**
     * @param message
     */
    public TranslationException(String message) {
        super(CODE, message);
    }

    /**
     * @param message
     * @param cause
     */
    public TranslationException(String message, Throwable cause) {
        super(CODE, message, cause);
    }
    
    private static final long serialVersionUID = -7255923259128654597L;
}
