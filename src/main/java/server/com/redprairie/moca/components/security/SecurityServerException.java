/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005-2007
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

package com.redprairie.moca.components.security;

import com.redprairie.moca.MocaException;

/**
 * Thrown by the <code>SecurityServerComponents</code> if there is an exception.
 * 
 * <b><pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class SecurityServerException extends MocaException {
    public static final int CODE = 830;
    private static final String MESSAGE = "Missing security server registry entry (^name^)";
    
    public SecurityServerException(String name) {
        super(CODE, MESSAGE);
        addArg("name", name);
    }

    private static final long serialVersionUID = -4386279527426329355L;
}