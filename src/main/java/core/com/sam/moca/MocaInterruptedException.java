/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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
 * This is the MOCA related interrupt exception.  It is thrown as a Runtime to
 * try to prevent users from continuing forward in case of a interrupt, since
 * so many places could be catching MocaExeption and just continuing forward.
 * There is still the issue of people catching RuntimeException or Exception as
 * well, but hopefully there will be few.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MocaInterruptedException extends MocaRuntimeException {
    private static final long serialVersionUID = -9155870042085375866L;
    public static final int CODE = 539;
    public static final String MESSAGE = "Execution encountered an interrupt request.";

    /**
     * This is just the default exception that should be thrown when checking
     * if your thread was interrupted (ie. Thread.interrupted()).
     */
    public MocaInterruptedException() {
        super(CODE, MESSAGE);
    }

    /**
     * This is a MocaInterruptedException that was caused by another throwable.
     * This is usually because of an Interrupted or InterruptedIO Exception.
     * @param t The exception that shows the interrupt.
     */
    public MocaInterruptedException(Throwable t) {
        super(CODE, MESSAGE, t);
    }
}
