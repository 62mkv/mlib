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

package com.redprairie.moca.socket;

import java.io.IOException;

/**
 * A processor for socket activity.  Generally, processors should be distinct, and not shared between threads,
 * although that is not a requirement.  Upon detecting socket activity, the socket server will create an instance
 * of SocketProcessor (via <code>SocketProcessorFactory</code>) and call the <code>process</code> method.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public interface SocketProcessor {
    /**
     * Process an incoming socket request.
     * 
     * @param endpoint
     * @throws IOException
     * @throws SocketProcessorException
     */
    public void process(SocketEndpoint endpoint) throws IOException, SocketProcessorException;
}
