/*
 *  $RCSfile$
 *  $Revision$
 *  $Author$
 *  $Date$
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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

package com.redprairie.moca;


/**
 * An exception raised when an error occurs with a MOCA argument.
 *
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 *
 * @author dpiessen
 * @version $Revision$
 */
public class MocaArgumentException extends MocaException {

    public static final int CODE = 3;
    
    public MocaArgumentException(String message) {
        super(CODE, "Invalid argument: ^message^");
        addArg("message", message);
    }

    private static final long serialVersionUID = 4049360723645050935L;
}
