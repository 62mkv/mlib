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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.redprairie.moca.Builder;
import com.redprairie.moca.pool.Validator;
import com.redprairie.moca.pool.impl.AbstractBlockingPool.IdleBusyCreatedCount;

import static org.junit.Assert.assertEquals;

/**
 * Test for demand blocking pool
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_DemandBasedBlockingPool extends TU_AbstractBlockingPool<DemandBasedBlockingPool<Object>, Object> {

    //  @see com.redprairie.moca.pool.impl.TU_AbstractBlockingPool#getBlockingPool(int, java.lang.String, com.redprairie.moca.pool.Validator, com.redprairie.moca.Builder, java.util.concurrent.BlockingQueue, java.util.concurrent.Executor)
    @Override
    protected DemandBasedBlockingPool<Object> getBlockingPool(int maxSize,
                                                              String name,
                                                              Validator<? super Object> validator,
                                                              Builder<? extends Object> builder,
                                                              BlockingQueue<Object> queue,
                                                              Executor executor) {
        return new DemandBasedBlockingPool<Object>(maxSize, name, 
                validator, builder, queue, executor);
    }
    
    
    @Test
    public void testMaxAsyncAndReleaseInvalid() throws InterruptedException, 
    BrokenBarrierException, TimeoutException {
        callTestUntilMaxThenRelease(30, false, 0);
    }
    
    @Test
    public void testSimpleGrabAndRelease() throws InterruptedException {
        callTestSimpleGrabAndRelease(20, 30, 20);
    }

    // @see com.redprairie.moca.pool.impl.TU_AbstractBlockingPool#validatePool(com.redprairie.moca.pool.BlockingPool, int, int)
    @Override
    protected void validateAfterGetResetException(DemandBasedBlockingPool<Object> pool, int definedMaxSize,
                                int numSuccessfulClientRequests) {
        IdleBusyCreatedCount counters = pool._counts.get();
        assertEquals(numSuccessfulClientRequests, counters.getBusy());
        assertEquals(numSuccessfulClientRequests, counters.getCreated());
        assertEquals(0, counters.getIdleCount());
        assertEquals(0, pool._pool.size());
    }

    // @see com.redprairie.moca.pool.impl.TU_AbstractBlockingPool#validateAfterReleaseException(com.redprairie.moca.pool.BlockingPool, int, int)
    @Override
    protected void validateAfterReleaseException(DemandBasedBlockingPool<Object> pool,
                                                 int definedMaxSize,
                                                 int numSuccessfulClientRequests) {
        IdleBusyCreatedCount counters = pool._counts.get();
        assertEquals(numSuccessfulClientRequests - 1, counters.getBusy());
        assertEquals(numSuccessfulClientRequests - 1, counters.getCreated());
        assertEquals(0, counters.getIdleCount());
        assertEquals(0, pool._pool.size());
    }
}
