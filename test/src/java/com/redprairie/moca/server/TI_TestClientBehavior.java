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

package com.redprairie.moca.server;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.client.ConnectionUtils;
import com.redprairie.moca.client.HttpConnection;
import com.redprairie.moca.client.MocaConnection;
import com.redprairie.moca.server.exec.SystemContext;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Unit tests for MOCA Command Processing behavior. These tests require that some test libraries
 * ($MOCADIR/test/src/libsrc/mocatest) be built and deployed.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TI_TestClientBehavior {
    
    public static final String LOGIN_USER="SUPER";
    public static final String LOGIN_PASS="SUPER";
    
    @Test
    public void testPassedEnvironment() throws MocaException {
        Map<String, String> env = new HashMap<String, String>();
        env.put("MYVAR", "Blah");
        MocaConnection conn = _getConnection(env);
        
        try {
            MocaResults res = conn.executeCommand("publish data where x = @@MYVAR");
            assertTrue(res.next());
            assertEquals("Blah", res.getString("x"));
        }
        finally {
            conn.close();
        }
    }
    
    @Test
    public void testPassedEnvironmentFromC() throws MocaException {
        Map<String, String> env = new HashMap<String, String>();
        env.put("MYVAR", "Blah");
        MocaConnection conn = _getConnection(env);
        
        try {
            MocaResults res = conn.executeCommand("test get variable where name='MYVAR'");
            assertTrue(res.next());
            assertEquals("Blah", res.getString("value"));
        }
        finally {
            conn.close();
        }
    }
    
    @Test
    public void testEnvironmentOverrideFromC() throws MocaException {
        Map<String, String> env = new HashMap<String, String>();
        env.put("MOCADIR", "Blah");
        MocaConnection conn = _getConnection(env);

        try {
            MocaResults res = conn.executeCommand("test get variable where name='MOCADIR'");
            assertTrue(res.next());
            assertEquals("Blah", res.getString("value"));
        }
        finally {
            conn.close();
        }
    }
    
    protected MocaConnection _getConnection(Map<String, String> env) throws MocaException {
        SystemContext ctx = ServerUtils.globalContext();
        String url = ctx.getConfigurationElement(MocaRegistry.REGKEY_SERVER_URL);
        MocaConnection conn = new HttpConnection(url, env);
        ConnectionUtils.login(conn, LOGIN_USER, LOGIN_PASS, "test");
        return conn;
    }

}
