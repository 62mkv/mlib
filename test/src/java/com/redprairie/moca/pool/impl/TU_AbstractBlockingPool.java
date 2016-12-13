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

package com.redprairie.moca.pool.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jgroups.util.DirectExecutor;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.Builder;
import com.redprairie.moca.pool.BlockingPool;
import com.redprairie.moca.pool.PoolException;
import com.redprairie.moca.pool.Validator;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public abstract class TU_AbstractBlockingPool<P extends AbstractBlockingPool<T>, T> {
    
    protected abstract P getBlockingPool(int maxSize, 
        String name, Validator<? super T> validator, 
        Builder<? extends T> builder, BlockingQueue<T> queue, Executor executor);
    
    protected P getAndInitializePool(int maxSize,
        String name, Validator<? super T> validator,
        Builder<? extends T> builder, BlockingQueue<T> queue, Executor executor) {
        P pool = getBlockingPool(maxSize, "TEST", 
            validator, builder, queue, executor);
        pool.initializePool();
        return pool;
    }
    
    @BeforeClass
    public static void beforeClass() throws SystemConfigurationException {
        ServerUtils.setupDaemonContext(TU_AbstractBlockingPool.class.getName(), 
            true);
    }
    
    @SuppressWarnings("unchecked")
    @Before
    public void beforeEachTest() {
        DEFAULT_BUILDER = Mockito.mock(Builder.class, Mockito.RETURNS_MOCKS);
    }
    
    @SuppressWarnings("unchecked")
    protected void callTestUntilMaxThenRelease(int maxSize, boolean releaseValid,
        int resultSize) throws InterruptedException, BrokenBarrierException, 
        TimeoutException {
        CyclicBarrier barrier = new CyclicBarrier(maxSize + 1);
        
        Validator<T> validator = Mockito.mock(Validator.class);
        Mockito.when(validator.isValid(Mockito.<T>any())).thenReturn(releaseValid);
        
        BlockingQueue<T> queue = new ArrayBlockingQueue<T>(maxSize);
        ExecutorService service = Executors.newCachedThreadPool();
        
        final P pool = getAndInitializePool(maxSize, "TEST", 
            validator, DEFAULT_BUILDER, queue, service);
        
        Callable<T> callable = new CatchAndRelease(pool, barrier);
        CompletionService<T> completion = 
                new ExecutorCompletionService<T>(service);
        try {
            for (int i = 0; i < maxSize; ++i) {
                completion.submit(callable);
            }
            
            barrier.await(5, TimeUnit.MINUTES);
            
            for (int i = 0; i < maxSize; ++i) {
                Future<T> future = completion.poll(5, TimeUnit.SECONDS);
                if (future == null) {
                    ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
                    ThreadInfo[] threadInfos = threadBean.dumpAllThreads(true, true);
                    System.out.println(Arrays.toString(threadInfos));
                    
                    fail("Threads didn't complete properly!");
                }
            }
        }
        finally {
            service.shutdown();
        }
        
        boolean shutdown = service.awaitTermination(10, TimeUnit.SECONDS);
        
        assertTrue("Service didn't shutdown properly", shutdown);
        
        assertEquals(resultSize, queue.size());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void callTestLotsOfContention()
            throws InterruptedException, BrokenBarrierException, 
            TimeoutException, ExecutionException {
        int threadCount = 30;
        int maxSize = 2;
        final CyclicBarrier barrier = new CyclicBarrier(threadCount + 1);
        
        Validator<T> validator = Mockito.mock(Validator.class);
        Mockito.when(validator.isValid(Mockito.<T>any())).thenReturn(true);
        
        BlockingQueue<T> queue = new ArrayBlockingQueue<T>(maxSize);
        ExecutorService service = Executors.newCachedThreadPool();
        
        final P pool = getAndInitializePool(maxSize, "TEST", 
            validator, DEFAULT_BUILDER, queue, Executors.newCachedThreadPool());
        
        final Callable<T> callable = new CatchAndRelease(pool, null);
        CompletionService<T> completion = 
                new ExecutorCompletionService<T>(service);
        try {
            for (int i = 0; i < threadCount; ++i) {
                completion.submit(new Callable<T>() {
                    @Override
                    public T call() throws Exception {
                        barrier.await();
                        return callable.call();
                    }
                    
                });
            }
            
            // Wait until all threads are started
            barrier.await(5, TimeUnit.SECONDS);
            
            for (int i = 0; i < threadCount; ++i) {
                Future<T> future = completion.poll(5, TimeUnit.SECONDS);
                if (future == null) {
                    ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
                    ThreadInfo[] threadInfos = threadBean.dumpAllThreads(true, true);
                    System.out.println(Arrays.toString(threadInfos));
                    
                    fail("Threads didn't complete properly!");
                }
                future.get();
            }
        }
        catch (TimeoutException e) {
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            ThreadInfo[] threadInfos = threadBean.dumpAllThreads(true, true);
            System.out.println(Arrays.toString(threadInfos));
            
            throw e;
        }
        finally {
            service.shutdown();
        }
        
        // Wait for all to complete
        boolean shutdown = service.awaitTermination(10, TimeUnit.SECONDS);
        
        assertTrue("Service didn't shutdown properly", shutdown);
        
        List<T> objects = new ArrayList<T>(maxSize);
        // Now we try to reacquire all of them again.  This should test
        // to make sure idle is correct
        for (int i = 0; i < maxSize; ++i) {
            T obj = pool.get(10, TimeUnit.MILLISECONDS);
            assertNotNull("Pool must have issue with idle counts", obj);
            objects.add(obj);
        }
        
        for (int i = 0; i < maxSize; ++i) {
            pool.release(objects.get(i));
        }
        
        assertEquals(maxSize, queue.size());
    }
    
    /**
     * @param grabCounts
     * @param maxSize
     * @param expect
     * @throws InterruptedException 
     */
    @SuppressWarnings("unchecked")
    protected void callTestSimpleGrabAndRelease(int grabCounts, int maxSize, 
        int expect) throws InterruptedException {
        Validator<T> validator = Mockito.mock(Validator.class);
        Mockito.when(validator.isValid(Mockito.<T>any())).thenReturn(true);
        
        BlockingQueue<T> queue = new ArrayBlockingQueue<T>(maxSize);
        
        final BlockingPool<T> pool = getAndInitializePool(maxSize, "TEST", 
            validator, DEFAULT_BUILDER, queue, new DirectExecutor());
        
        List<T> objects = new ArrayList<T>(grabCounts);
        for (int i = 0; i < grabCounts; ++i) {
            objects.add(pool.get(2, TimeUnit.MINUTES));
        }
        
        for (int i = 0; i < grabCounts; ++i) {
            pool.release(objects.get(i));
        }
        
        assertEquals(expect, queue.size());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testManualRetrieval() throws InterruptedException, BrokenBarrierException, 
        TimeoutException {
        int retrieval = 20;
        int maxSize = 30;
        
        Validator<T> validator = Mockito.mock(Validator.class);
        Mockito.when(validator.isValid(Mockito.<T>any())).thenReturn(true);
        
        BlockingQueue<T> queue = new ArrayBlockingQueue<T>(maxSize);
        ExecutorService service = Executors.newCachedThreadPool();
        
        List<T> createdObjects = new ArrayList<T>(retrieval);
        
        final BlockingPool<T> pool = getAndInitializePool(maxSize, "TEST", 
            validator, DEFAULT_BUILDER, queue, service);
        
        // Get and release
        for (int i = 0; i < retrieval; ++i) {
            createdObjects.add(pool.get(2, TimeUnit.MINUTES));
        }
        
        for (int i = 0; i < retrieval; ++i) {
            pool.release(createdObjects.get(i));
        }
        
        for (int i = 0; i < retrieval; ++i) {
            T obj = createdObjects.get(i);
            assertTrue("Object could not be removed manually [" + obj + "]", 
                pool.removePooledObject(obj));
        }
        
        assertTrue("Queue " + queue.size() + " can only be as big as the max " +
                "less our retrieval count " + (maxSize - retrieval), 
            (maxSize - retrieval) >= queue.size());
    }
    
    /**
     * This test is to make sure that if all objects are allocated then
     * released that the pool is full afterwards
     * @throws InterruptedException
     * @throws BrokenBarrierException
     * @throws TimeoutException
     */
    @Test
    public void testMaxAsyncAndRelease() throws InterruptedException, BrokenBarrierException, TimeoutException {
        callTestUntilMaxThenRelease(50, true, 50);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testMaxGetTimeoutThenRetrieval() throws InterruptedException, ExecutionException, TimeoutException {
        int maxSize = 20;
        Validator<T> validator = Mockito.mock(Validator.class);
        Mockito.when(validator.isValid(Mockito.<T>any())).thenReturn(true);
        
        BlockingQueue<T> queue = new ArrayBlockingQueue<T>(maxSize);
        
        final P pool = getAndInitializePool(maxSize, "TEST", 
            validator, DEFAULT_BUILDER, queue, new DirectExecutor());
        
        List<T> objects = new ArrayList<T>(maxSize);
        for (int i = 0; i < maxSize; ++i) {
            objects.add(pool.get(2, TimeUnit.MINUTES));
        }
        
        // After we grab them all we should get a timeout
        T obj = pool.get(75, TimeUnit.MILLISECONDS);
        assertNull("We shouldn't have gotten anything back", obj);
        
        // Just to make sure poll is the same
        obj = pool.poll();
        assertNull("We shouldn't have gotten anything back from poll either", obj);
        
        ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            Future<T> future = service.submit(new Callable<T>() {
                @Override
                public T call() throws Exception {
                    return pool.get();
                }
            });
            
            // By releasing this the other thread should have gotten it!
            pool.release(objects.get(0));
            
            obj = future.get(2, TimeUnit.MINUTES);
            assertSame(objects.get(0), obj);
        }
        finally {
            service.shutdownNow();
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testGetWhenShutdown() throws InterruptedException {
        int maxSize = 20;
        Validator<T> validator = Mockito.mock(Validator.class);
        
        BlockingQueue<T> queue = new ArrayBlockingQueue<T>(maxSize);
        
        final P pool = getAndInitializePool(maxSize, "TEST", 
            validator, DEFAULT_BUILDER, queue, new DirectExecutor());
        
        pool.shutdown();
        
        try {
            pool.get();
            fail("Should have thrown an IllegalStateException");
        }
        catch (IllegalStateException e) {
            // Expected
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testPollWhenShutdown() throws InterruptedException {
        int maxSize = 20;
        Validator<T> validator = Mockito.mock(Validator.class);
        
        BlockingQueue<T> queue = new ArrayBlockingQueue<T>(maxSize);
        
        final P pool = getAndInitializePool(maxSize, "TEST", 
            validator, DEFAULT_BUILDER, queue, new DirectExecutor());
        
        pool.shutdown();
        
        try {
            pool.poll();
            fail("Should have thrown an IllegalStateException");
        }
        catch (IllegalStateException e) {
            // Expected
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testTimedGetWhenShutdown() throws InterruptedException {
        int maxSize = 20;
        Validator<T> validator = Mockito.mock(Validator.class);
        
        BlockingQueue<T> queue = new ArrayBlockingQueue<T>(maxSize);
        
        final P pool = getAndInitializePool(maxSize, "TEST", 
            validator, DEFAULT_BUILDER, queue, new DirectExecutor());
        
        pool.shutdown();
        
        try {
            pool.get(2, TimeUnit.MINUTES);
            fail("Should have thrown an IllegalStateException");
        }
        catch (IllegalStateException e) {
            // Expected
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testPoolSaysItIsShutdown() {
        Validator<T> validator = Mockito.mock(Validator.class);
        
        BlockingQueue<T> queue = new ArrayBlockingQueue<T>(DEFAULT_MAX_SIZE);
        
        final P pool = getAndInitializePool(DEFAULT_MAX_SIZE, "TEST", 
            validator, DEFAULT_BUILDER, queue, new DirectExecutor());
        
        pool.shutdown();
        
        assertTrue(pool.isShutdown());
    }
    
    /**
     * Implement to validate the state of the pool after the validator has thrown
     * an unexpected runtime exception.
     * @param pool The pool implementation
     * @param definedMaxSize The defined max size of the pool (should be 20)
     * @param numSuccessfulClientRequests The number of successful requests before the exception occurs
     */
    protected abstract void validateAfterGetResetException(P pool, int definedMaxSize,
                                         int numSuccessfulClientRequests);

    /**
     * Runs the following test for all the getters (get(), get(long, TimeUnit), poll()))
     * 1) Instantiates a new blocking pool of the underlying implementation, max size set to 20
     * 2) Requests 10 objects from the pool
     * 3) Has a runtime exception occur on the 11th getter request, then validates the state
     * Subclasses should implement validateAfterGetResetException to validate the end state
     * @throws InterruptedException
     * @throws PoolException
     */
    @Test
    public void testValidatorResetRuntimeException()
            throws InterruptedException, PoolException {
        P pool = setupResetValidatorRuntimeException();
        try {
            pool.get();
            fail("The RuntimeException was swallowed by the BlockingPool implementation" +
                 "on get() when it should bubble up instead");
        }
        catch (RuntimeException expected) {
            validateAfterGetResetException(pool, DEFAULT_MAX_SIZE, SUCCESSFUL_REQUESTS);
        }
        
        pool = setupResetValidatorRuntimeException();
        try {
            pool.get(2, TimeUnit.MINUTES);
            fail("The RuntimeException was swallowed by the BlockingPool implementation" +
                 "on get(long, TimeUnit) when it should bubble up instead");
        }
        catch (RuntimeException expected) {
            validateAfterGetResetException(pool, DEFAULT_MAX_SIZE, SUCCESSFUL_REQUESTS);
        }
        
        pool = setupResetValidatorRuntimeException();
        try {
            pool.poll();
            fail("The RuntimeException was swallowed by the BlockingPool implementation" +
                 "on poll() when it should bubble up instead");
        }
        catch (RuntimeException expected) {
            validateAfterGetResetException(pool, DEFAULT_MAX_SIZE, SUCCESSFUL_REQUESTS);
        }
    }
    
    /**
     * Used to validate the state of the implementing pool after the testValidatorIsValidRuntimeException test.
     * @param pool The BlockingPool implementation
     * @param definedMaxSize The defined max size of the pool
     * @param numSuccessfulClientRequests The number of successful client requests before the exception
     */
    protected abstract void validateAfterReleaseException(P pool, int definedMaxSize,
                                                          int numSuccessfulClientRequests);
    
    /**
     * This test is used to verify the state of the blocking pool implementation after a runtime exception
     * occurs when trying to add an entry back to the queue. The test works as follows:
     * 1) Instantiates a new blocking pool of the underlying implementation, max size set to 20
     * 2) Makes a single request for an object from the blocking pool
     * 3) Releases the object back to the pool where a RuntimeException occurs when validating the state
     * validateAfterReleaseException should be implemented by the test subclass to validate the state
     * after the exception has occurred.
     * @throws InterruptedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testValidatorIsValidRuntimeException() throws InterruptedException {
        Validator<T> validator = Mockito.mock(Validator.class);
        
        BlockingQueue<T> queue = new ArrayBlockingQueue<T>(DEFAULT_MAX_SIZE);
        
        final P pool = getAndInitializePool(DEFAULT_MAX_SIZE, "TEST", 
            validator, DEFAULT_BUILDER, queue, new DirectExecutor());
        
        Mockito.doThrow(new RuntimeException("on isValid()")).when(validator).isValid(Mockito.<T>any());
        T obj = pool.get();
        try {
            pool.release(obj);
            fail("The RuntimeException was swallowed by the BlockingPool implementation" +
                    "on release(T) when it should bubble up instead");
        }
        catch (RuntimeException expected) {
            validateAfterReleaseException(pool, DEFAULT_MAX_SIZE, 1);
        }
    }
    
    @SuppressWarnings("unchecked")
    private P setupResetValidatorRuntimeException() 
            throws InterruptedException, PoolException {
        Validator<T> validator = Mockito.mock(Validator.class);
        BlockingQueue<T> queue = new ArrayBlockingQueue<T>(DEFAULT_MAX_SIZE);
        final P pool = getAndInitializePool(DEFAULT_MAX_SIZE, "TEST", 
            validator, DEFAULT_BUILDER, queue, new DirectExecutor());
        
        for (int i = 0; i < SUCCESSFUL_REQUESTS; i++) {
            pool.get(2, TimeUnit.MINUTES);
        }
        
        Mockito.doThrow(new RuntimeException("validator reset runtime exception")).when(validator).reset(Mockito.<T>any());
        return pool;
    }
    
    
    private class CatchAndRelease implements Callable<T> {
        
        public CatchAndRelease(P pool, CyclicBarrier barrier) {
            _pool = pool;
            _barrier = barrier;
        }

        // @see java.util.concurrent.Callable#call()
        @Override
        public T call() throws Exception {
            T obj = _pool.get();
            if (_barrier != null) {
                _barrier.await(2, TimeUnit.MINUTES);
            }
            _pool.release(obj);
            return obj;
        }
        
        private final CyclicBarrier _barrier;
        private final P _pool;
    }
    
    protected static final int SUCCESSFUL_REQUESTS = 10;
    protected static final int DEFAULT_MAX_SIZE = 20;
    private Builder<T> DEFAULT_BUILDER;
}
