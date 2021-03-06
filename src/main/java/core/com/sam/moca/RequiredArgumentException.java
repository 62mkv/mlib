/*
 *  $RCSfile$
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

package com.sam.moca;


/**
 * An exception raised when a required MOCA command argument is missing.
 *
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 *
 * @author dpiessen
 * @version $Revision$
 */
public class RequiredArgumentException extends MocaException {

    public static final int CODE = 507;
    
    public RequiredArgumentException(String verb, String noun, String name) {
        super(CODE, "Command ^verb^ ^noun^: Argument ^argname^ is required");
        addArg("verb", verb);
        addArg("noun", noun);
        addArg("argname", name);
    }

    public RequiredArgumentException(String command, String name) {
        this(command, "", name);
    }
    
    public RequiredArgumentException(String name) {
        this("", "", name);
    }

    private static final long serialVersionUID = 4049360723645050935L;
}
