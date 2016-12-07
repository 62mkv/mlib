/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.server.exec;

import com.redprairie.moca.MocaException;

/**
 * Thrown when a remote server fails to respond to a connection attempt.
 *  
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class RemoteConnectionFailedException extends MocaException {

    public RemoteConnectionFailedException(String connection, Throwable t) {
        super(518, "Unable to connect to remote system ^hostname^", t);
        addArg("hostname", connection);
    }

    private static final long serialVersionUID = 1L;
}
