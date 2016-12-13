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
 * Represents that a query results exceeded the internal row limit set by MOCA.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */
public class TooManyRowsException extends MocaException {
    
    public final static int CODE = 540;
    
    /**
     * Creates a TooManyRowsException to indicate that the query exceeded a system-defined limit.
     */
    public TooManyRowsException(long limit) {
        super (CODE, "Row limit of " + limit + " exceeded in query.");
        _queryLimit = limit;
    }
    
    /**
     * Gets the query row limit
     * @return The query row limit
     */
    public long getQueryLimit() {
        return _queryLimit;
    }

    private final long _queryLimit;
    
    private static final long serialVersionUID = 3256999951996301881L;
}
