/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.sam.moca.components.base;

import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;

/**
 * This exception is thrown if an error occurs while attempting to load a
 * data file.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class LoadDataException extends MocaException {
    
    private static final long serialVersionUID = 6943156106545441804L;
    
    public static final int ERROR = 813;
    public static final String MESSAGE = 
        "An error was encountered while loading data";

    /**
     * @param errorCode
     * @param message
     */
    public LoadDataException(MocaResults res) {
        super(ERROR, MESSAGE);
        super.setResults(res);
    }

}
