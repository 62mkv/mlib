/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.sam.moca.pool.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

import com.sam.moca.Builder;
import com.sam.moca.pool.Validator;

/**
 * A BlockingPool implementation that will maintain a defined number of idle pool
 * objects.  If an object is taken from the pool and makes the pool size drop
 * below the minimum a new object will be created to take it's place.  The only
 * exception is if the pool has already met the maximum amount of objects.
 * <p>
 * This implementation retains a set of threads that are blocking waiting for
 * an object from the pool.  If shutdown is invoked these threads will be sent
 * an interrupt request.
 * <p>
 * The {@link #initializePool()} method should be invoked before doing anything
 * on the pool.  Failure to do so may lead to the pool never returning anything
 * <p>
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * <p>
 * @author wburns
 */
public class MinIdleBlockingPool<T> extends AbstractBlockingPool<T> {
    /**
     * Instantiates an instance with the provided min idle size, max size, 
     * validator and builder
     * @param minIdle This is how many idle pool objects the pool will always
     *        try to maintain.  This will be guaranteed until the pool gets
     *        to becoming full at which point will not cause additional objects
     *        to be created to satisfy the min idle setting
     * @param maxSize The most pool objects this pool will allow to be around
     *        at the same time
     * @param name The name of this pool, useful for trace messages
     * @param validator The validator that will be used to validate pool
     *        objects
     * @param builder The builder that will be called to create a new pooled
     *        instance
     */
    public MinIdleBlockingPool(int minIdle, int maxSize, String name,
        Validator<? super T> validator, Builder<? extends T> builder) {
        super(maxSize, name, validator, builder);
        _minIdle = minIdle;
        _counts.set(new IdleBusyCreatedCount(0, 0, _minIdle));
    }
    
    MinIdleBlockingPool(int maxIdle, int maxSize, String name,
            Validator<? super T> validator, Builder<? extends T> builder,
            BlockingQueue<T> queue, Executor executor) {
        super(maxSize, name, validator, builder, queue, executor);
        _minIdle = maxIdle;
        _counts.set(new IdleBusyCreatedCount(0, 0, _minIdle));
    }
    
    /**
     * We override the default changeCounters so when creation or idle
     * counts are updated we can know to spawn another process
     * @param busy How much more to increase the busy count (can be negative)
     * @param creation How much more to increase the creation count (can be 
     *        negative)
     */
    @Override
    protected IdleBusyCreatedCount changeCounters(int idle, int busy, int creation) {
        IdleBusyCreatedCount returnCounts = null;
        boolean updated = false;
        
        while (!updated) {
            IdleBusyCreatedCount counts = _counts.get();
            int newBusy = counts.getBusy() + busy;
            int newCreated = counts.getCreated() + creation;
            
            boolean tryCreate = false;
            if (newBusy + _minIdle > newCreated && newCreated < _maximumSize) {
                newCreated += 1;
                tryCreate = true;
            }
            
            returnCounts = new IdleBusyCreatedCount(counts.getIdleCount() + idle, 
                newBusy, newCreated);
            updated = _counts.compareAndSet(counts, returnCounts);
            if (tryCreate && updated) {
                _executor.execute(_createPoolObject);
            }
        }
        
        return returnCounts;
    }
    
    // @see com.sam.moca.pool.impl.AbstractBlockingPool#onInitialize()
    @Override
    protected void onInitialize() {
        // The constructor already set the max idle value for the number of
        // created processes so we just have to create that many
        for (int i = 0; i < _minIdle; ++i) {
            _executor.execute(_createPoolObject);
        }
    }
    
    protected final int _minIdle;
}
