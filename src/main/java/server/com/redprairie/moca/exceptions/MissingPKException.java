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

package com.redprairie.moca.exceptions;

import com.redprairie.moca.MocaException;

/**
 * An exception class that represents error code 2967
 * Missing PK Fields for a table.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dpiessen
 * @version $Revision$
 */
public class MissingPKException extends MocaException {
    public static final int CODE = 2967;
    public static final String MSG = "Missing a PK field for table: ^tblnam^";
    
    /** Creates a new MissingPKException
     * @param tableName The name of the table that is missing values.
     */
    public MissingPKException(String tableName) {
        super(CODE, MSG);
        addArg("tblnam", tableName);
    } 
    
    private static final long serialVersionUID = -61114830754875596L;
}