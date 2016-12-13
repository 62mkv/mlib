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

package com.redprairie.moca.server.db;

import com.redprairie.moca.exceptions.MocaDBException;

/**
 * Exception that is thrown when a bind variable is used in a SQL
 * statement, but is not passed in as an argument.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MissingVariableException extends MocaDBException {
    private static final long serialVersionUID = 210856868285382694L;
    public static final int CODE = -3;
    
    public MissingVariableException(String var) {
        super(CODE, "Missing bind variable ^var^ in SQL statement");
        addArg("var", var);
    }
}
