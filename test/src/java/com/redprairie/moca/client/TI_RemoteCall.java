/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.sam.moca.client;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sam.moca.MocaArgument;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaRegistry;
import com.sam.moca.MocaResults;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.SystemConfigurationException;
import com.sam.moca.server.exec.SystemContext;
import com.sam.moca.util.MocaUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A test class to test remote call functionality.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
public class TI_RemoteCall {
    @BeforeClass
    public static void beforeClass() throws SystemConfigurationException {
        ServerUtils.setupDaemonContext("TU_RemoteCall", true);
    }
    
    @Test
    public void testSimpleRemoteCall() throws MocaException {
        SystemContext ctx = ServerUtils.globalContext();
        MocaContext moca = MocaUtils.currentContext();
        String port = ctx
            .getConfigurationElement(MocaRegistry.REGKEY_SERVER_CLASSIC_PORT);
        MocaResults results = moca.executeCommand(
            "remote(@url) publish data where foo=@bar", new MocaArgument("url",
                "localhost:" + port), new MocaArgument("bar", "bar"));
        assertTrue(results.next());
        assertEquals("bar", results.getString("foo"));
    }
    
    @Test
    public void testParallelCall() throws MocaException {
        SystemContext ctx = ServerUtils.globalContext();
        String url = ctx.getConfigurationElement(MocaRegistry.REGKEY_SERVER_URL);
        
        MocaContext moca = MocaUtils.currentContext();
        MocaResults results = moca.executeCommand(
            "parallel(@url || ',' || @url) publish data where foo=@bar", 
                new MocaArgument("url", url), 
                new MocaArgument("bar", "bar"));
        
        for (int i = 0; i < 2; ++i) {
            assertTrue(results.next());
            assertEquals(url, results.getString("system"));
            assertEquals(0, results.getInt("status"));
            
            MocaResults childRes = results.getResults("resultset");
            assertTrue(childRes.next());
            assertEquals("bar", childRes.getString("foo"));
        }
    }
    
    @Test
    public void testParallelThrowsMocaException() throws MocaException {
        SystemContext ctx = ServerUtils.globalContext();
        String url = ctx.getConfigurationElement(MocaRegistry.REGKEY_SERVER_URL);
        
        MocaContext moca = MocaUtils.currentContext();
        MocaResults results = moca.executeCommand(
            "parallel(@url || ',' || @url) [[ throw new OutOfMemoryError();]]", 
                new MocaArgument("url", url), 
                new MocaArgument("bar", "bar"));
        
        for (int i = 0; i < 2; ++i) {
            assertTrue(results.next());
            assertEquals(url, results.getString("system"));
            assertEquals(502, results.getInt("status"));
            
            MocaResults childRes = results.getResults("resultset");
            assertFalse(childRes.next());
            assertEquals(0, childRes.getColumnCount());
        }
    }
}
