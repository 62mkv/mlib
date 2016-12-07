/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2006
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
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2006 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class ReadResponseException extends MocaException {
    
    public static final int CODE = 204;

    /**
     * @param message
     */
    public ReadResponseException() {
        this(null);
    }

    /**
     * @param message
     * @param t
     */
    public ReadResponseException(Throwable t) {
        super(CODE, "MOCA socket communication failure", t);
    }

    private static final long serialVersionUID = 3192851540590421940L;
}
