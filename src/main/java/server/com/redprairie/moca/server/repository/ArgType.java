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

package com.redprairie.moca.server.repository;

import com.redprairie.moca.MocaType;


/**
 * An enum class to represent the expected types of Arguments expected for a 
 * Command or trigger. 
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author cjolly
 * @version $Revision$
 */
public enum ArgType {
    INTEGER(MocaType.INTEGER),
    STRING(MocaType.STRING),
    FLOAT(MocaType.DOUBLE),
    FLAG(MocaType.BOOLEAN),
    BINARY(MocaType.BINARY),
    POINTER(MocaType.GENERIC),
    RESULTS(MocaType.RESULTS),
    OBJECT(MocaType.OBJECT),
    UNKNOWN(MocaType.UNKNOWN);
    
    public MocaType getMocaType() {
        return _mocaType;
    }
    
    private ArgType(MocaType mocaType) {
        _mocaType = mocaType;
    }
    
    private final MocaType _mocaType;
}
