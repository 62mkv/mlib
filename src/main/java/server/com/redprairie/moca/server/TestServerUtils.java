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

package com.redprairie.moca.server;

import java.io.PrintStream;
import java.util.List;

import com.redprairie.moca.server.ServerUtils.CurrentValues;
import com.redprairie.moca.server.db.DBAdapter;
import com.redprairie.moca.server.db.jdbc.MocaDataSource;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.legacy.NativeProcessPool;
import com.redprairie.moca.server.log.TraceState;
import com.redprairie.moca.util.AbstractMocaMultiThreadTestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Utility class to enable certain test configurations.
 * This test should <b>NOT</b> be used except possibly in testing.  Even then
 * it's usage can affect many things.
 * The reason this test is not in the test jar instead is due to the fact that
 * {@linkplain AbstractMocaMultiThreadTestCase} cannot be moved to the test
 * package without products refactoring their code.

import org.apache.logging.log4j.LogManager; * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class TestServerUtils {

    /**
     * This method is used to override the current system context.  Great care
     * should be taken when using this method.
     * @param reg
     */
    public static void overrideGlobalContext(SystemContext reg) {
        ServerUtils.setGlobalContext(reg);
    }
    
    public static SystemContext getGlobalContext() {
        return ServerUtils.getGlobalContext();
    }
    
    /**
     * This method will unassociate the various ServerUtils stored values with
     * the current thread and return them.
     * Great care should be taken when using this method.
     * @return
     */
    public static CurrentValues takeCurrentValues() {
        return ServerUtils.takeCurrentValues();
    }
    
    /**
     * This method will set the various ServerUtils values with for the current
     * thread overriding whatever is currently associated if anything.
     * Great care should be taken when using this method.
     * @return
     */
    public static void restoreValues(CurrentValues objects) {
        ServerUtils.restoreValues(objects);
    }

    /**
     * Cleanup at the end of the test after setupDaemonContext(..., singleThreaded = false)
     */
    public static void cleanupDaemonContext(){
        CurrentValues tempValues = takeCurrentValues();
        tempValues.getServerContext().close();
        tempValues.getTraceState().closeLogging();

        ServerContextFactory serverContextFactory = tempValues.getServerContextFactory();
        if(serverContextFactory != null){
            DBAdapter dbAdapter = serverContextFactory.getDBAdapter();
            if(dbAdapter != null){
                MocaDataSource mds = (MocaDataSource) dbAdapter.getDataSource();
                if(mds != null){
                    mds.closeAll();
                }
            }

            NativeProcessPool nativePool = serverContextFactory.getNativePool();
            if(nativePool != null){
                nativePool.shutdown();
            }
            serverContextFactory.close();
        }
    }
    
    public static void verifyActiveContextPointers() {
        Thread currentThread = Thread.currentThread();
        ServerContext context = ServerUtils.getCurrentContext();
        ServerContext hardContextReference = ServerUtils
            .getHardContextReferenceForThread(currentThread);
        assertEquals(context, hardContextReference);
        
        List<Thread> threads = ServerUtils.getContextThreads(context);
        assertTrue("Our current context didn't have our current thread " + 
                "assigned to it had " + threads, threads.contains(currentThread));
    }
    
    public static void dumpThreads(PrintStream ps){
        int active = Thread.activeCount();
        Thread threads[] = new Thread[active];
        Thread.enumerate(threads);

        for(int i = 0; i < active; i++){
            ps.println(i + ": " + threads[i]);
        }
    }
}
