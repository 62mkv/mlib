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

package com.redprairie.moca.client;

import java.util.Map;

import org.junit.Test;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.SystemContext;

/**
 * Unit tests for HttpConnection.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_HttpConnection extends TU_AbstractConnection {
    
    // @see com.redprairie.moca.client.TU_AbstractConnection#_getConnection(java.util.Map)
    @Override
    protected MocaConnection _getConnection(String host, Map<String, String> env) throws MocaException {
        SystemContext ctx = ServerUtils.globalContext();
        String url = ctx.getConfigurationElement(MocaRegistry.REGKEY_SERVER_URL);
        return new HttpConnection(url, env);
    }
    
    /**
     * This test is to test to make sure that if a connection is closed while
     * an open tx is open that it doesn't error. MOCA-4280
     * @throws MocaException
     */
    @Test
    public void testOpenTxCloseSession() throws MocaException {
        SystemContext ctx = ServerUtils.globalContext();
        String url = ctx.getConfigurationElement(MocaRegistry.REGKEY_SERVER_URL);
        HttpConnection conn =  new HttpConnection(url, null);
        ConnectionUtils.login(conn, LOGIN_USER, LOGIN_PASS, "test");
        
        conn.setAutoCommit(false);
        
        // We have to hit the db to force it to create a tx
        conn.executeCommand("[select * from dual]");
        
        conn.disconnect();
    }
}
