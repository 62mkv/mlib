/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.pool.impl;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;

import com.redprairie.moca.Builder;
import com.redprairie.moca.pool.Validator;
import com.redprairie.moca.pool.impl.AbstractBlockingPool.IdleBusyCreatedCount;

/**
 * Test for min idle size blocking pool
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_MinIdleSizeBlockingPool extends TU_AbstractBlockingPool<MinIdleBlockingPool<Object>, Object> {

    // @see com.redprairie.moca.pool.impl.TU_AbstractBlockingPool#getBlockingPool(int, java.lang.String, com.redprairie.moca.pool.Validator, com.redprairie.moca.Builder, java.util.concurrent.BlockingQueue, java.util.concurrent.Executor)
    @Override
    protected MinIdleBlockingPool<Object> getBlockingPool(int maxSize,
                                                          String name,
                                                          Validator<? super Object> validator,
                                                          Builder<? extends Object> builder,
                                                          BlockingQueue<Object> queue,
                                                          Executor executor) {
        return new MinIdleBlockingPool<Object>(DEFAULT_MIN, maxSize, name, 
                validator, builder, queue, executor);
    } 
    
    @Test
    public void testMaxAsyncAndReleaseInvalid() throws InterruptedException, 
    BrokenBarrierException, TimeoutException {
        callTestUntilMaxThenRelease(50, false, DEFAULT_MIN);
    }
    
    @Test
    public void testSimpleGrabAndRelease() throws InterruptedException {
        callTestSimpleGrabAndRelease(20, 30, 20 + DEFAULT_MIN);
    }
    
    /**
     * Tests that the pools counters remain in sync after a runtime exception occurs
     * during a build() call on the given Builder for the pool.
     * @throws InterruptedException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testPoolBuilderRuntimeException() throws InterruptedException {
        Validator<Object> validator = Mockito.mock(Validator.class);
        Builder<Object> mockBuilder = Mockito.mock(Builder.class);
        BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(DEFAULT_MAX_SIZE);
        
        // We need to use an executor on a different thread instead of DirectExecutor
        // to test the behavior correctly.
        ExecutorService executor = Executors.newFixedThreadPool(1);
        try {
            Mockito.when(mockBuilder.build()).thenReturn(new Object());
            MinIdleBlockingPool<Object> pool = getAndInitializePool(DEFAULT_MAX_SIZE, "TEST", 
                validator, mockBuilder, queue, executor);
            
            // Set the builder to fail with a runtime exception once
            // then it should recover the second time and return an object
            Mockito.when(mockBuilder.build()).thenThrow(new RuntimeException("build runtime exception"))
                   .thenReturn(new Object());
            pool.get();
            
            // Executing with a thread pool so wait for the build() calls to be executed
            Mockito.verify(mockBuilder, Mockito.timeout(10000).times(DEFAULT_MIN + 2)).build();

            int expectedBusy = 1;
            IdleBusyCreatedCount counters = pool._counts.get();
            assertEquals(expectedBusy, counters.getBusy());
            assertEquals(DEFAULT_MIN, counters.getIdleCount());
            assertEquals(DEFAULT_MIN + expectedBusy, counters.getCreated());
            assertEquals(DEFAULT_MIN, pool._pool.size());
        }
        finally {
            executor.shutdown();
        }
    }
    
    // @see com.redprairie.moca.pool.impl.TU_AbstractBlockingPool#validatePool(com.redprairie.moca.pool.BlockingPool, int, int)
    @Override
    protected void validateAfterGetResetException(MinIdleBlockingPool<Object> pool, int definedMaxSize,
                                int numSuccessfulClientRequests) {
        IdleBusyCreatedCount counters = pool._counts.get();
        assertEquals(numSuccessfulClientRequests, counters.getBusy());
        assertEquals(numSuccessfulClientRequests + DEFAULT_MIN, counters.getCreated());
        assertEquals(DEFAULT_MIN, counters.getIdleCount());
        assertEquals(DEFAULT_MIN, pool._pool.size());
    }
    
    // @see com.redprairie.moca.pool.impl.TU_AbstractBlockingPool#validateAfterReleaseException(com.redprairie.moca.pool.BlockingPool, int, int)
    @Override
    protected void validateAfterReleaseException(MinIdleBlockingPool<Object> pool,
                                                 int definedMaxSize,
                                                 int numSuccessfulClientRequests) {
        IdleBusyCreatedCount counters = pool._counts.get();
        int newBusy = numSuccessfulClientRequests - 1;
        assertEquals(newBusy, counters.getBusy());
        assertEquals(newBusy + DEFAULT_MIN, counters.getCreated());
        assertEquals(DEFAULT_MIN, counters.getIdleCount());
        assertEquals(DEFAULT_MIN, pool._pool.size());
    }  

    private static final int DEFAULT_MIN = 1;   
}
