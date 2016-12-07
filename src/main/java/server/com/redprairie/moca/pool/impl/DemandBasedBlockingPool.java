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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.redprairie.moca.Builder;
import com.redprairie.moca.pool.Validator;

/**
 * This is a demand based pool implementation.  There are no guarantees to
 * having idle objects in the pool.  Objects are only created as they are 
 * requested.  The poll method therefore can and will in many cases return null
 * consistently.
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class DemandBasedBlockingPool<T> extends AbstractBlockingPool<T> {
    /**
     * @param size
     * @param validator
     * @param builder
     */
    public DemandBasedBlockingPool(int size, String name,
            Validator<? super T> validator, Builder<? extends T> builder) {
        super(size, name, validator, builder);
    }
    
    DemandBasedBlockingPool(int size, String name,
            Validator<? super T> validator, Builder<? extends T> builder,
            BlockingQueue<T> queue, Executor executor) {
        super(size, name, validator, builder, queue, executor);
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Since this pool is demand based there is a high likelihood that this
     * method will return null.  The only times it won't is if there is an idle
     * object or the object was spawned fast enough.  In either case this method
     * can cause an object to be created through demand, but more than likely
     * it will not see it soon enough to grab it before returning.
     */
    @Override
    public T poll() {
        preRetrieval();
        T object = null;
        try {
            object = super.poll();
            return object;
        }
        finally {
            postRetrieval(object);
        }
    }
    
    // @see com.redprairie.moca.pool.impl.AbstractBlockingPool#get()
    @Override
    public T getInternal() throws InterruptedException {
        preRetrieval();
        T object = null;
        try {
            object = super.getInternal();
            return object;
        }
        finally {
            postRetrieval(object);
        }
    }
    
    // @see com.redprairie.moca.pool.impl.AbstractBlockingPool#get(long, java.util.concurrent.TimeUnit)
    @Override
    public T getInternal(long timeOut, TimeUnit unit) throws InterruptedException {
        preRetrieval();
        T object = null;
        try {
            object = super.getInternal(timeOut, unit);
            return object;
        }
        finally {
            postRetrieval(object);
        }
    }
    
    private void preRetrieval() {
        IdleBusyCreatedCount counts;
        int requestCount;
        IdleBusyCreatedCount newCounts = null;
        _requestWriteLock.lock();
        try {
            // Post decrement, cause we don't want to count ourselves
            requestCount = _requestCount++;
            counts = _counts.get();
            // If our request put it above idle, we need to try to spawn
            // another and increment idle
            if (_requestCount > counts.getIdleCount() && 
                    counts.getCreated() < _maximumSize) {
                newCounts = changeCounters(1, 0, 1);
                _executor.execute(_createPoolObject);
            }
        }
        finally {
            _requestWriteLock.unlock();
        }
        
        // Outside of lock we print the info
        printPoolSizing(counts, requestCount);
        if (newCounts != null) {
            printOnSpawnObject(newCounts);
        }
    }
    
    private void postRetrieval(T object) {
        IdleBusyCreatedCount counts = null;
        int requestCount;
        _requestWriteLock.lock();
        try {
            // This is pre decrement so we don't count ourselves any more
            requestCount = --_requestCount;
            if (object != null) {
                counts = changeCounters(-1, 1, 0);
            }
        }
        finally {
            _requestWriteLock.unlock();
        }
        if (counts != null) {
            printPoolAfterGrab(counts, requestCount);
        }
    }
    
    // @see com.redprairie.moca.pool.impl.AbstractBlockingPool#changeCounters(int, int, int)
    @Override
    protected IdleBusyCreatedCount changeCounters(int idle, int busy, 
        int creation) {
        _requestReadLock.lock();
        try {
            return super.changeCounters(idle, busy, creation);
        }
        finally {
            _requestReadLock.unlock();
        }
    }
    
    // @see com.redprairie.moca.pool.impl.AbstractBlockingPool#attemptChangeCounters(com.redprairie.moca.pool.impl.AbstractBlockingPool.IdleBusyCreatedCount, int, int, int)
    @Override
    protected IdleBusyCreatedCount attemptChangeCounters(
        IdleBusyCreatedCount previousCounts, int idle, int busy, int creation) {
        _requestReadLock.lock();
        try {
            return super.attemptChangeCounters(previousCounts, idle, busy, 
                creation);
        }
        finally {
            _requestReadLock.unlock();
        }
    }
    
    /**
     * We don't decrement the idle and increase busy since this is done in
     * our get/poll methods.  We don't even print the messages.  This is
     * all handled in the get/poll methods directly.
     */
    @Override
    protected void handlePoolObjectRetrieved(T object) {
        // Do Nothing
    }
    
    /**
     * We don't print messages here
     */
    @Override
    protected void handlePoolObjectRequested() {
        // Do Nothing
    }
    
    /**
     * We do not want to increment idle on creation since we handle that logic
     * ourselves
     */
    @Override
    protected void handlePoolObjectCreated(T object) {
        // Do Nothing
    }
    
    /**
     * This method should only ever be invoked when holding onto the read or
     * write lock.
     * @param previousCounts
     */
    private void printPoolSizing(IdleBusyCreatedCount counts, int waiters) {
        if (_logger.isDebugEnabled()) {
            _logger.debug("Object requested from pool" + (_name != null
                    ? " [" + _name + "] " : "") + ": busy: " + 
                    counts.getBusy() + ", idle: " + 
                    counts.getIdleCount() + ", started " + 
                    counts.getCreated() + " / " + _maximumSize + 
                    " with " + waiters + " waiting.");
        }
    }
    
    private void printOnSpawnObject(IdleBusyCreatedCount counts) {
        if (_logger.isDebugEnabled()) {
            _logger.debug("Object being created for pool" + (_name != null
                    ? " [" + _name + "] " : "") + ": busy: " + 
                    counts.getBusy() + ", idle: " + 
                    counts.getIdleCount() + ", started " + 
                    counts.getCreated() + " / " + _maximumSize +
                    " with " + _requestCount + " waiting.");
        }
    }
    
    /**
     *      * This method should only ever be invoked when holding onto the read or
     * write lock.
     * @param counts
     */
    private void printPoolAfterGrab(IdleBusyCreatedCount counts, int waiters) {
        if (_logger.isDebugEnabled()) {
            _logger.debug("Object retrieved from pool" + (_name != null
                    ? " [" + _name + "] " : "") + ": busy: " + 
                    counts.getBusy() + ", idle: " + 
                    counts.getIdleCount() + ", started " + 
                    counts.getCreated() + " / " + _maximumSize +
                    " with " + waiters + " waiting.");
        }
    }
    
    /**
     * This variable must be protected by the write lock whenever updated
     */
    protected int _requestCount = 0;
    
    protected final ReadWriteLock _requestLock = new ReentrantReadWriteLock();
    protected final Lock _requestReadLock = _requestLock.readLock();
    protected final Lock _requestWriteLock = _requestLock.writeLock();
    
}
