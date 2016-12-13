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

package com.redprairie.moca.crud;

import com.redprairie.moca.MocaException;

/**
 * An exception class that provides the CRUD commands
 * with an exception that indicates the argument is not
 * on the stack. This is used to deal with the fact that
 * the original CRUD methods have a different code for the
 * same type of error.     
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dpiessen
 * @version $Revision$
 */
public class MissingStackArgumentException extends MocaException {
    private static final long serialVersionUID = -8024067089893503027L;

    /**
     * Creates a new instance of the MissingStackArgumentException class
     * @param errorCode The error code
     * @param paramName The parameter name of the argument.
     * @param colName The column name of the argument that is missing.
     */
    public MissingStackArgumentException(int errorCode, String paramName, String colName) {
        super(errorCode);
        addArg(paramName, colName);
    }

}
