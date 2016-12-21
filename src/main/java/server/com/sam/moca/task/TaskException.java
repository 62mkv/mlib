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

package com.sam.moca.task;

import com.sam.moca.MocaException;

/**
 * An exception associated with task scheduling or configuration errors.  This
 * exception is typically not thrown in the case of simple task failure.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author mlange
 */
public class TaskException extends MocaException {
    // TODO - What should we use for an error code here?
    public static final int CODE = 535;

    public TaskException(String msg) {
            super(CODE, msg);
    }

    public TaskException(String msg, Throwable e) {
        super(CODE, msg, e);
    }

    private static final long serialVersionUID = 1L;
}
