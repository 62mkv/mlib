/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.sam.moca.server.exec;

import java.util.Map;

import com.sam.moca.MocaException;
import com.sam.moca.client.MocaConnection;


/**
 * This interface describes a factory that will return a given MocaConnection
 * for the provided connection string.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public interface RemoteConnectionFactory {
    /**
     * Returns a connection without specifying user and password for the given
     * @param connectionString
     * @param env
     * @return
     * @throws MocaException
     */
    public MocaConnection getConnection(String connectionString, 
        Map<String, String> env) throws MocaException;
}
