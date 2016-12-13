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

package com.redprairie.moca;


/**
 * Represents that a query did not return any data, or that an updat
 * did not affect any rows.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */
public class NotFoundException extends MocaException {
    
    public final static int DB_CODE = -1403;
    public final static int SERVER_CODE = 510;
    
    /**
     * Creates a default NoDataException
     */
    public NotFoundException() {
        this(DB_CODE);
    }
    
    /**
     * Creates a NoDataException with the given results.  The results
     * are used to provide column information to the caller.
     * @param results a results object to be used to provide
     * column information to the caller.
     */
    public NotFoundException(MocaResults results) {
        this(DB_CODE, results);
    }
    
    /**
     * Creates a NoDataException with the given code.
     * @param code what MOCA code value to use for this exception.  This
     * argument must be either DB_CODE or SERVER_CODE.
     */
    public NotFoundException(int code) {
        this(code, null);
    }
    
    /**
     * Creates a NoDataException with the given code and results.  The results
     * are used to provide column information to the caller.
     * @param code what MOCA code value to use for this exception.  This
     * argument must be either DB_CODE or SERVER_CODE.
     * @param results a results object to be used to provide
     * column information to the caller.
     */
    public NotFoundException(int code, MocaResults results) {
        super (code, "no rows affected");
        setResults(results);
    }
    
    private static final long serialVersionUID = 3256999951996301881L;
}
