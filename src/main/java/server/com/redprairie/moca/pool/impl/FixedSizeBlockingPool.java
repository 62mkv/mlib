/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

import com.redprairie.moca.Builder;
import com.redprairie.moca.pool.Validator;

/**
 * A fixed size pool that implements all optional blocking policy methods.  This
 * pool will try to keep always N # of pool objects around.  That is the
 * number of used and available objects should always be the same.  If an
 * object becomes invalid the pool will call to the builder to create another
 * one to replace it.  All creation and invalidation is done on a separate thread
 * that caused the operation to occur.
 * <p>
 * This implementation retains a set of threads that are blocking waiting for
 * an object from the pool.  If shutdown is invoked these threads will be sent
 * an interrupt request.
 * <p>
 * The {@link #initializePool()} method should be invoked before doing anything
 * on the pool.  Failure to do so may lead to the pool never returning anything
 * <p>
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * <p>
 * @author wburns
 */
public class FixedSizeBlockingPool<T> extends AbstractBlockingPool<T> {
    /**
     * Instantiates an instance with the given fixed size, validator and builder
     * @param size The size this pool will always try to maintain
     * @param validator The validator that will be used to validate pool
     *        objects
     * @param builder The builder that will be called to create a new pooled
     *        instance
     */
    public FixedSizeBlockingPool(int size, String name,
            Validator<? super T> validator, Builder<? extends T> builder) {
        super(size, name, validator, builder);
        _counts.set(new IdleBusyCreatedCount(0, 0, size));
    }
    
    FixedSizeBlockingPool(int size, String name,
            Validator<? super T> validator, Builder<? extends T> builder,
            BlockingQueue<T> queue, Executor executor) {
        super(size, name, validator, builder, queue, executor);
        _counts.set(new IdleBusyCreatedCount(0, 0, size));
    }
    
    // @see com.redprairie.moca.pool.impl.AbstractBlockingPool#onInitialize()
    @Override
    protected void onInitialize() {
        for (int i = 0; i < _maximumSize; ++i) {
            _executor.execute(_createPoolObject);
        }
    }
    
    // @see com.redprairie.moca.pool.impl.AbstractBlockingPool#changeCounters(int, int, int)
    @Override
    protected IdleBusyCreatedCount changeCounters(
        int idle, int busy, int creation) {
        int spawnCount = 0;
        // If we are ever taking one down we need to recreate it!
        if (creation < 0) {
            spawnCount = -creation;
            creation = 0;
        }
        
        try {
            return super.changeCounters(idle, busy, creation);
        }
        finally {
            for (int i = 0; i < spawnCount; ++i) {
                _executor.execute(_createPoolObject);
            }
        }
    }
}
