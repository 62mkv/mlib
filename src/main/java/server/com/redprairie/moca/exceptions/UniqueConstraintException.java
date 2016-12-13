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

package com.redprairie.moca.exceptions;

import java.sql.SQLException;


/**
 * Represents an exception condition related to a unique constraint (usually
 * a primary key) violation.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author Derek Inksetter
 * @version $Revision$
 */
public class UniqueConstraintException extends MocaDBException {
    /**
     * The MOCA error code returned when this exception is thrown. 
     */
    public static final int CODE = -1;

    /**
     * Creates a default UniqueConstraintException
     */
    public UniqueConstraintException(String msg) {
        super(CODE, msg);
    }
    
    public UniqueConstraintException(SQLException cause) {
        super(CODE, cause.getMessage(), cause);
    }
    
    private static final long serialVersionUID = 3258132448989821238L;
}
