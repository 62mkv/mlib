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

package com.redprairie.moca.util;

import java.io.IOException;

import com.redprairie.moca.MocaException;

/**
 * This exception is to hold io exceptions as they occur
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class MocaIOException extends MocaException {
    public static final int CODE = 809;
    
    /**
     * Constructs a MocaIOException with the detail message.
     * Note that most times that a MocaIOException gets thrown is
     * due to an IOException occurring which we're wrapping into a
     * MocaIOException. In that case you should always use {@link #MocaIOException(String, IOException)}
     * so that the full exception stack gets included.
     * @param detail The detail message
     */
    public MocaIOException(String detail) {
        super(CODE, "IO operation failure (^detail^)");
        addArg("detail", detail);
    }

    /**
     * Wraps an IOException into a MocaIOException.
     * @param detail The detail message
     * @param cause The IOException cuase
     */
    public MocaIOException(String detail, IOException cause) {
        super(CODE, "IO operation failure (^detail^)", cause);
        addArg("detail", detail);
    }
    
    private static final long serialVersionUID = -4703498644793748358L;
}
