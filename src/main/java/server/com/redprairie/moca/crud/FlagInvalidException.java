/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 */

package com.redprairie.moca.crud;

import com.redprairie.moca.MocaException;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class FlagInvalidException extends MocaException {
    private static final long serialVersionUID = 7145282435119960845L;
    private static final int CODE = 2965;
    private static final String MESSAGE = 
        "Invalid flag value (^flagval^) for ^flagnam^";
    
    /**
     * @param errorCode
     * @param message
     * @param t
     */
    public FlagInvalidException(String name, Object value, Throwable t) {
        super(CODE, MESSAGE, t);
        super.addArg("flagnam", name);
        super.addArg("flagval", value);
    }
    
    /**
     * @param errorCode
     * @param message
     * @param t
     */
    public FlagInvalidException(String name, Object value) {
        super(CODE, MESSAGE);
        super.addArg("flagnam", name);
        super.addArg("flagval", value);
    }

}
