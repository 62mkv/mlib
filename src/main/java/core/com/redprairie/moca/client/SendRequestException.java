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

package com.redprairie.moca.client;

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
public class SendRequestException extends MocaException {
    
    public static final int CODE = 203;

    /**
     * @param message
     */
    public SendRequestException() {
        this(null);
    }

    /**
     * @param message
     * @param t
     */
    public SendRequestException(Throwable t) {
        super(CODE, "MOCA socket communication failure", t);
    }

    private static final long serialVersionUID = 8339227661467098025L;
}
