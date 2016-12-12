/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.redprairie.moca.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.redprairie.moca.MocaResults;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.ServerContext;

import static org.junit.Assert.*;

/**
 * This class contains various utility methods for use when running unit tests
 * that rely on MOCA.
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MocaTestUtils {
    /**
     * This method will allow for the current thread to override any command
     * execution that would occur for the given named command.  Instead of
     * invoking the command as it would normally, the provided 
     * {@link CommandInterceptor} will be called instead, passing the current
     * MocaContext for the current thread.
     * <p>
     * Care should be taken to properly clear the overridden commands after the
     * test is completed.  Failure to do so may cause other tests to improperly
     * override that command and cause unknown problems.
     * @param namedCommand The command to override
     * @param interceptor The interceptor to call instead of the real command
     */
    public static void overrideCommand(String namedCommand, CommandInterceptor interceptor) {
        ServerContext context = ServerUtils.getCurrentContext();
        context.overrideCommand(namedCommand, interceptor);
    }
    
    /**
     * This command will clear all the overridden commands for the current
     * thread associated context.  This should be called after a test is 
     * completed so that the overridden commands will not leak into another
     * test.
     */
    public static void removeCommandOverrides() {
        ServerContext context = ServerUtils.getCurrentContext();
        context.clearOverriddenCommands();
    }
    
    /**
     * Makes the given count of concurrent database requests.
     * This is done by spawning "count" number of threads and
     * executing a select statement on each thread. This method also
     * uses {@link CyclicBarrier}s to try to increase the effects of concurrency to pick up
     * on concurrency issues with executing SQL statements.
     * 
     * Also supports specifying the number of iterations to run.
     * @param count
     * @param iterations
     * @throws InterruptedException
     * @throws BrokenBarrierException
     * @throws ExecutionException
     */
    public static void makeConcurrentDbRequests(int count, int iterations) throws InterruptedException, BrokenBarrierException, ExecutionException {
        ExecutorService service = Executors.newFixedThreadPool(count);
        try {
            for (int i = 0; i < iterations; i++) {
                final CyclicBarrier startBarrier = new CyclicBarrier(count);
                List<Future<MocaResults>> futures = new ArrayList<Future<MocaResults>>(count);
                for (int submitted = 0; submitted < count; submitted++) {
                    futures.add(service.submit(new Callable<MocaResults>() {
    
    
                        @Override
                        public MocaResults call() throws Exception {
                            boolean completed = false;
                            try {
                                startBarrier.await();
                                MocaResults res =
                                        MocaUtils.currentContext().executeSQL("select * from dual");
                                completed = true;
                                return res;
                            }
                            finally {
                                if (completed) {
                                    MocaUtils.currentContext().commit();
                                }
                                else {
                                    MocaUtils.currentContext().rollback();    
                                }
                            }
                        }
                    }));
                }
                
                for (Future<MocaResults> future : futures) {
                    MocaResults res = future.get();
                    assertEquals("Should have had a single column", 1, res.getColumnCount());
                    assertEquals("Query results were incorrect", 1, res.getRowCount());
                }
            }
        }
        finally {
            service.shutdownNow();
        }
    }
}