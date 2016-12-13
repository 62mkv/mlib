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

package com.redprairie.moca.components.crud;

import com.redprairie.moca.MocaException;

/**
 * A MOCA exception (2966) that indicates the primary key for this record already exists
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dpiessen
 * @version $Revision$
 */
public class PrimaryKeyExistsException extends MocaException {
    public static final int CODE = 2966;
    public static final String MSG = "^key^ already exists^";
    
    /** Creates a new PrimaryKeyExistsException class
     * @param keyValue The primary key string
     */
    public PrimaryKeyExistsException(String keyValue) {
        super(CODE, MSG);
        addArg("key", keyValue);
    }

    private static final long serialVersionUID = 6760638625291059108L;
}
