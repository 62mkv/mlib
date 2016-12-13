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

package com.redprairie.moca.server.db;

import java.sql.Connection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.util.MocaUtils;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TI_MultiThreadConnectionAccess {
    
    private static ServerContext previousContext = null;
    
    @BeforeClass public static void beforeTests() throws SystemConfigurationException {
        try {
            previousContext = ServerUtils.getCurrentContext();
        }
        catch (IllegalStateException ignore) {
            // We don't care if it wasn't initialized yet.  We only do this
            // so we can restore the context afterwards
            // This only happens if it is the first test to initialize the 
            // context
        }
        // First we null out the current context
        ServerUtils.setCurrentContext(null);
        ServerUtils.setupDaemonContext(
                TI_MultiThreadConnectionAccess.class.getName(), false);
    }
    
    @AfterClass public static void afterTests() {
        // Now restore the context back to what it was
        ServerUtils.setCurrentContext(previousContext);
    }
    
    @Before
    public void reset() {
        _connection1 = null;
        _connection2 = null;
    }
    
    @After
    public void afterEachTest() throws IllegalStateException, MocaException {
        ServerUtils.getCurrentContext().rollback();
    }
    
    /**
     * This test is to make sure that a connection received from each
     * context at 2 different points are equal
     */
    @Test
    public void testMainThreadConnectionsEqual() {
        MocaContext context1 = MocaUtils.currentContext();
        
        _connection1 = context1.getDb().getConnection();
        
        MocaContext context2 = MocaUtils.currentContext();
        
        _connection2 = context2.getDb().getConnection();
        
        Assert.assertEquals("The connections should be equal", _connection1, 
                _connection2);
    }
    
    /**
     * This test is to make sure that a connection received from each
     * context at 2 different points are equal even when in a separate thread
     * @throws InterruptedException
     */
    @Test
    public void testSpawnedThreadConnectionEquals() throws InterruptedException {
        Thread connectionThread = new Thread() {

            // @see java.lang.Thread#run()
            @Override
            public void run() {
                MocaContext context1 = MocaUtils.currentContext();
                
                _connection1 = context1.getDb().getConnection();
                
                MocaContext context2 = MocaUtils.currentContext();
                
                _connection2 = context2.getDb().getConnection();
            }
            
        };
        
        connectionThread.start();
        
        connectionThread.join();
        
        Assert.assertEquals("The connections should be equal", _connection1, 
                _connection2);
    }
    
    Connection _connection1;
    Connection _connection2;
}
