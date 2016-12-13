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

package com.redprairie.moca.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.server.exec.SystemContext;

import static org.junit.Assert.assertTrue;

/**
 * This class tests various aspects of the DefaultServerContextFactory 
 * implementation.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_DefaultServerContextFactory {
    
    @BeforeClass
    public static void beforeClass() throws SystemConfigurationException {
        ServerUtils.setupDaemonContext(TU_DefaultServerContextFactory.class.getName(), true);
        
        _system = ServerUtils.globalContext();
    }
    
    @Test
    public void testThreadSafetyWithServerUtils() throws InterruptedException {
        ServerContextFactory factory = (ServerContextFactory)_system.getAttribute(
            ServerContextFactory.class.getName());
        
        ExecutorService executor = Executors.newCachedThreadPool();
        
        for (int i = 0; i < 5; ++i) {
            executor.submit(new FactoryUtilsTester(factory, 5));
        }
        
        executor.shutdown();
        
        // If a timeout occurs
        assertTrue("A deadlock most likely occurred!", 
            executor.awaitTermination(30, TimeUnit.SECONDS));
    }
    
    private static class FactoryUtilsTester implements Runnable {
        
        private FactoryUtilsTester(ServerContextFactory factory, int times) {
            _factory = factory;
            _times = times;
        }

        // @see java.lang.Runnable#run()
        @Override
        public void run() {
            for (int i = 0; i < _times; ++i) {
                // We try to create a new factory which will try to get
                // the current context
                try {
                    _factory.restart(false);
                }
                catch (MocaException e) {
                    e.printStackTrace();
                }
                
                ServerUtils.removeCurrentContext();
                
                // We will try to get the current context that will have to
                // call the factory since it was removed
                ServerUtils.getCurrentContext();
                System.out.println("Done with - " + i);
            }
        }
        
        private final ServerContextFactory _factory;
        private final int _times;
    }
    
    private static SystemContext _system;
}
