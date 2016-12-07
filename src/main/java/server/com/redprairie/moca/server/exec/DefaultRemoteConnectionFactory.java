/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

import java.util.Map;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.client.ConnectionUtils;
import com.redprairie.moca.client.MocaConnection;

/**
 * Default remote connection factory that just falls back to ConnectionUtils
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class DefaultRemoteConnectionFactory implements RemoteConnectionFactory {
    // @see com.redprairie.moca.server.exec.RemoteConnectionFactory#getConnection(java.lang.String, java.util.Map)
    @Override
    public MocaConnection getConnection(String connectionString,
        Map<String, String> env) throws MocaException {
        return ConnectionUtils.createConnection(connectionString, env);
    }
}
