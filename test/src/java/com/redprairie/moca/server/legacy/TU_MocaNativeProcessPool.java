/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.server.legacy;

import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.redprairie.moca.Builder;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.web.console.ConsoleModel;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertTrue;

/**
 * This class tests various aspects of the NativeProcessPool.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_MocaNativeProcessPool {
    
    @BeforeClass
    public static void beforeClass() throws SystemConfigurationException {
//        ServerUtils.setupDaemonContext(TU_MocaNativeProcessPool.class.getName(),
//            true);
    }

    /**
     * Test method for {@link com.redprairie.moca.server.legacy.MocaNativeProcessPool#getNativeAdapter(com.redprairie.moca.server.exec.ServerContext)}.
     * @throws MocaException 
     * @throws TimeoutException 
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    @Test
    public void testGetNativeAdapterAtMaximumSize() throws MocaException, 
            InterruptedException, ExecutionException, TimeoutException {
        int max = 5;
        final MocaNativeProcessPool pool = new MocaNativeProcessPool(
            "testGetNativeAdapterAtMaximumSize",1, TimeUnit.SECONDS, 1, max, 
            null, new EmptyNativeProcess());
        final NativeProcess[] processes = new NativeProcess[max];
        
        for (int i = 0; i < max; ++i) {
            
            NativeProcess process = pool.getNativeProcess();
            processes[i] = process;
        }
        
        new Thread() {
            public void run() {
                // We sleep for 10 milliseconds to let them get into the restart
                try {
                    Thread.sleep(10);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                for (NativeProcess process : processes) {
                    try {
                        process.close();
                    }
                    catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        
        ExecutorService execService = Executors.newSingleThreadExecutor();
        
        Future<NativeProcess> futureProcess = execService.submit(new Callable<NativeProcess>() {

            @Override
            public NativeProcess call() throws Exception {
                // We try to get an adapter, it will block at first until
                // another caller releases the adapter
                return pool.getNativeProcess();
            }
            
        });

        
        // Now we want to make sure that the process isn't null and that we don't
        // timeout (aka blocked forever)
        Assert.assertNotNull(futureProcess.get(100, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testProcessShutdownAfterMaxRequest() throws MocaException, RemoteException {
        int maxCommand = 2;
        final MocaNativeProcessPool pool = new MocaNativeProcessPool(
            "testProcessShutdownAfterMaxRequest", 1, TimeUnit.SECONDS, 1, 1, 
            maxCommand, new EmptyNativeProcess());
        NativeProcess process = pool.getNativeProcess();
        String processId = process.getId();
        process.close();
        
        // Now we spawn and check for the reset size
        for (int i = 0; i < maxCommand - 1; ++i) {
            NativeProcess retrieved = pool.getNativeProcess();
            Assert.assertSame("iteration: " + i, processId, retrieved.getId());
            retrieved.close();
        }
        
        // Now that the last process has been retrieved max times we should
        // get a new process
        NativeProcess newProc = pool.getNativeProcess();
        Assert.assertNotSame(processId, newProc.getId());
    }
    
    @Test
    public void testProcessShutdownActive() throws MocaException, RemoteException {
        int maxCommand = 5;
        final MocaNativeProcessPool pool = new MocaNativeProcessPool(
            "testProcessShutdownActive", 1, TimeUnit.SECONDS, 1, 1, maxCommand, 
            new EmptyNativeProcess());
        NativeProcess process = pool.getNativeProcess();
        
        pool.shutdownProcess(process);
        // Now that the last process has been retrieved max times we should
        // get a new process
        NativeProcess newProc = pool.getNativeProcess();
        Assert.assertNotSame(process.getId(), newProc.getId());
    }
    
    @Test
    public void testProcessShutdownInctive() throws MocaException, RemoteException {
        int maxCommand = 5;
        final MocaNativeProcessPool pool = new MocaNativeProcessPool(
            "testProcessShutdownInctive", 1, TimeUnit.SECONDS, 1, 1, maxCommand, 
            new EmptyNativeProcess());
        NativeProcess process = pool.getNativeProcess();
        
        process.close();
        
        pool.shutdownProcess(process);
        // Now that the last process was shutdown we should get a new one
        NativeProcess newProc = pool.getNativeProcess();
        Assert.assertNotSame(process.getId(), newProc.getId());
    }

    /**
     * Simple test that reproduces an NPE that can happen when manually shutting down a process.
     */
    @Test
    public void testManualShutdown() throws NativeProcessTimeoutException, RemoteException {
        final MocaNativeProcessPool pool = new MocaNativeProcessPool(
                "testManualShutdown", 60, TimeUnit.SECONDS, 5, 10, Integer.MAX_VALUE,
                new EmptyNativeProcess());

        // we grab one process from the pool which forces the pool
        // to spawn up to the idle processes
        pool.getNativeProcess();

        // then try to shut down everything
        for (NativeProcess process : pool.getAllProcesses()) {
            pool.shutdownProcess(process);
        }
    }

    /**
     * Simple test that DOESN'T reproduce an NPE because we use all the native processes at least once.
     * This shows that this exception only happens on native processes that have not been
     * requested from the pool yet.
     */
    @Test
    public void testManualShutdownWithUsage() throws NativeProcessTimeoutException, RemoteException {
        final int MAX = 10;
        final MocaNativeProcessPool pool = new MocaNativeProcessPool(
                "testManualShutdownWithUsage", 60, TimeUnit.SECONDS, 5, MAX, Integer.MAX_VALUE,
                new EmptyNativeProcess());

        for (int i = 0; i < MAX; i++) {
            pool.getNativeProcess();
        }

        for (NativeProcess process : pool.getAllProcesses()) {
            pool.shutdownProcess(process);
        }
    }
    
    /**
     * Orphan processes with the new pooling count against the total count
     * unlike older versions
     * @throws MocaException
     * @throws RemoteException
     */
    @Test
    public void testPoolShutdownOrphan() throws MocaException, RemoteException {
        int maxCommand = 5;
        final MocaNativeProcessPool pool = new MocaNativeProcessPool(
            "testPoolShutdownOrphan", 250, TimeUnit.MILLISECONDS, 1, 1, maxCommand, 
            new EmptyNativeProcess());
        NativeProcess process = pool.getNativeProcess();
        
        pool.restartPool();
        
        try {
            // The orphan shouldn't have affected it
            pool.getNativeProcess();
            Assert.fail("This should have thrown a timeout exception!");
        }
        catch (NativeProcessTimeoutException e) {
            // Should go here
        }
        // close the old process and then we should be able to get the process
        process.close();
        
        NativeProcess newProc = pool.getNativeProcess();
        Assert.assertNotSame(process, newProc);
    }

    /**
     * Test that we can request 10 native processes from the pool when the max is 5 - then we only get 5 back
     * and that the other 5 are not able to complete the request.
     */
    @Test
    public void testConcurrenctNativeProcessRequests() throws 
            InterruptedException, BrokenBarrierException, TimeoutException, ExecutionException {
        int max = 5;
        int threadCount = max * 2;
        final MocaNativeProcessPool pool = new MocaNativeProcessPool(
            "testConcurrenctNativeProcessRequests", 1, TimeUnit.SECONDS, 1, max, 
            null, new EmptyNativeProcess());
        
        int unfinishedCount = testExpectedResults(threadCount, pool);
        
        Assert.assertEquals(threadCount - max, unfinishedCount);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testNativeProcessSpawnCrashNotMessingUpMax() throws InterruptedException, 
            BrokenBarrierException, TimeoutException, NativeProcessTimeoutException, RemoteException {
        Builder<NativeProcess> builder = Mockito.mock(Builder.class);
        
        Mockito.when(builder.build()).thenReturn(null).thenAnswer(
            new Answer<NativeProcess>() {
                @Override
                public NativeProcess answer(InvocationOnMock invocation)
                        throws Throwable {
                    return realBuilder.build();
                }
                
                private final Builder<NativeProcess> realBuilder = 
                        new EmptyNativeProcess();
        });
        
        int max = 5;
        final MocaNativeProcessPool pool = new MocaNativeProcessPool(
            "testNativeProcessSpawnCrashNotMessingUpMax", 200, TimeUnit.SECONDS, 
            1, max, null, builder);
        
        
        // This should do a back off if the process failed, but it should be
        // all oblivious to us other than it taking a little longer
        //
        // Have a crashed process, this should basically leave the pool okay
        NativeProcess process = pool.getNativeProcess();
        process.close();
        
        int unfinishedCount = testExpectedResults(max, pool);
        
        Assert.assertEquals(0, unfinishedCount);
    }
    
    @Test
    public void testNativeProcessCrashNotMessingUpMax() throws InterruptedException, 
            BrokenBarrierException, TimeoutException, MocaException, RemoteException {
        int max = 5;
        final MocaNativeProcessPool pool = new MocaNativeProcessPool(
            "testNativeProcessCrashNotMessingUpMax", 1, TimeUnit.SECONDS, 1, 
            max, null, new EmptyNativeProcess());
        
        // Have a crashed process, this should basically leave the pool okay
        NativeProcess process = pool.getNativeProcess();
        
        assertTrue(Proxy.isProxyClass(process.getClass()));
        
        NativeProcessHandler invocHandler = (NativeProcessHandler)Proxy.getInvocationHandler(process);
        
        // The drop is called if the process gave us a bad error so we want
        // to get rid of it
        invocHandler.setError();
        
        process.close();
        
        int unfinishedCount = testExpectedResults(max, pool);
        
        Assert.assertEquals(0, unfinishedCount);
    }
    
    @Test
    public void testNativeProcessReturnedProperlyNotMessingUpMax() throws InterruptedException, 
            BrokenBarrierException, TimeoutException, MocaException, RemoteException {
        int max = 5;
        final MocaNativeProcessPool pool = new MocaNativeProcessPool(
            "testNativeProcessReturnedProperlyNotMessingUpMax", 1, 
            TimeUnit.SECONDS, 1, max, null, new EmptyNativeProcess());
        
        // Have a crashed process, this should basically leave the pool okay
        NativeProcess process = pool.getNativeProcess();
        
        // This is called when process returned correctly
        process.close();
        
        int unfinishedCount = testExpectedResults(5, pool);
        
        Assert.assertEquals(0, unfinishedCount);
    }
    
    /**
     * This will run normal creation for number of threads on the pool.  Then
     * it will wait on each of those for 100 ms in parallel and return how
     * many didn't spawn
     */
    private int testExpectedResults(int threadCount, 
            final MocaNativeProcessPool pool) throws InterruptedException, 
            BrokenBarrierException, TimeoutException {
        ExecutorService execService = Executors.newFixedThreadPool(threadCount);
        // Use the barrier to make sure all the threads are running
        final CyclicBarrier barrier = new CyclicBarrier(threadCount + 1);
     
        List<Future<NativeProcess>> array = new ArrayList<Future<NativeProcess>>(threadCount);
        for (int i = 0; i < threadCount; ++i) {
            Future<NativeProcess> futureProcess = execService.submit(new Callable<NativeProcess>() {
    
                @Override
                public NativeProcess call() throws Exception {
                    barrier.await();
                    // We try to get an adapter, it will block at first until
                    // another caller releases the adapter
                    return pool.getNativeProcess();
                }
            });
            
            array.add(futureProcess);
        }

        execService.shutdown();
        barrier.await(10, TimeUnit.SECONDS);

        int unfinishedCount = 0;
        for (Future<NativeProcess> result : array) {
            try {
                // technically we should already have all the futures
                // and they should already be completed, however the
                // CI can be slow so don't take risks on getting them
                result.get(10, TimeUnit.SECONDS);
            }
            catch (ExecutionException e) {
                // we are specifically looking for timeout exceptions
                // anything else should result in a test failure
                if (!(e.getCause() instanceof NativeProcessTimeoutException)) {
                    throw new RuntimeException(e.getCause());
                }
                unfinishedCount++;
            }
        }
        
        return unfinishedCount;
    }

    public static class EmptyNativeProcess implements Builder<NativeProcess> {

        // @see com.redprairie.moca.Builder#build()
        @Override
        public NativeProcess build() {
            NativeProcess process = Mockito.mock(NativeProcess.class);
            Mockito.when(process.getId()).thenReturn("TEST-" + _counter.getAndIncrement());
            return process;
        }
        
        private final AtomicInteger _counter = new AtomicInteger();
    }
    
    public static class NativeProcessCrash implements Builder<NativeProcess> {

        // @see com.redprairie.moca.Builder#build()
        @Override
        public NativeProcess build() {
            return null;
        }
        
    }
}
