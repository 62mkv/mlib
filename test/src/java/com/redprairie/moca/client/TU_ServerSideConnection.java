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

package com.redprairie.moca.client;

import java.util.Map;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.server.ServerUtils;

/**
 * Unit tests for DirectConnection.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_ServerSideConnection extends TU_AbstractConnection {
    
    // @see junit.framework.TestCase#setUp()
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ServerUtils.setupDaemonContext("ServerTest", false);
    }
    
    @Override
    public void testTimeout() throws MocaException {
       // Server side connections don't support timeout.
    }
    
    // @see com.redprairie.moca.client.TU_AbstractConnection#_getConnection(java.util.Map)
    @Override
    protected MocaConnection _getConnection(String host, Map<String, String> env) throws MocaException {
        return _getConnection(env);
    }

    @Override
    protected MocaConnection _getConnection(Map<String, String> env) throws MocaException {
        MocaConnection conn = new ServerSideConnection();
        conn.setEnvironment(env);
        return conn;
     }
}
