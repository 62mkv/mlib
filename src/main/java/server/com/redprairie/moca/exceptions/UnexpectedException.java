/*
 *  $URL$
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

package com.redprairie.moca.exceptions;

import com.redprairie.moca.MocaException;

/**
 * An exception that indicates that an unexpected error condition has arisen
 * in MOCA or the MOCA Java executive.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author Derek Inksetter
 * @version $Revision$
 */
public class UnexpectedException extends MocaException {

    /**
     * The MOCA error code returned when this exception is thrown. 
     */
    public static final int CODE = 502;
    
    /**
     * @param t an instance of <code>Throwable</code> that caused this
     * exception.
     */
    public UnexpectedException(Throwable t) {
        super(CODE, "Caught exception " + t, t);
    }

    private static final long serialVersionUID = 3256442512435853365L;
}
