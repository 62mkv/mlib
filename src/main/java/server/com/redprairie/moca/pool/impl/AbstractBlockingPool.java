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

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.mad.client.MadGauge;
import com.redprairie.mad.client.MadMeter;
import com.redprairie.mad.client.MadMetrics;
import com.redprairie.mad.client.MadTimer;
import com.redprairie.mad.client.MadTimerContext;
import com.redprairie.moca.Builder;
import com.redprairie.moca.mad.MonitoringUtils;
import com.redprairie.moca.pool.BlockingPool;
import com.redprairie.moca.pool.PoolException;
import com.redprairie.moca.pool.Validator;
import com.redprairie.moca.util.DaemonThreadFactory;

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
 * Any implementation <b>must</b> increment the creation count before they submit
 * a pool object creation!
 * <p>
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * <p>
 * @author wburns
 */
public abstract class AbstractBlockingPool<T> implements BlockingPool<T> {
    protected AbstractBlockingPool(int size, String name,
            Validator<? super T> validator, Builder<? extends T> builder) {
        this(size, name, validator, builder, Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() + 1, 
            new DaemonThreadFactory(name + "-Pool-" + 
        _poolCount.getAndIncrement())));
    }
    
    /**
     * @param size
     * @param validator
     * @param builder
     * @param executor The executor must be some kind of asynchronous executor
     */
    protected AbstractBlockingPool(int size, String name,
            Validator<? super T> validator, Builder<? extends T> builder,
            Executor executor) {
        this(size, name, validator, builder, new ArrayBlockingQueue<T>(size), 
            executor);
    }
    
    /**
     * @param size
     * @param validator
     * @param builder
     * @param queue
     * @param executor The executor must be some kind of asynchronous executor
     */
    protected AbstractBlockingPool(int size, String name,
            Validator<? super T> validator, Builder<? extends T> builder,
            BlockingQueue<T> queue, Executor executor) {
        _maximumSize = size;
        _builder = builder;
        _validator = validator;
        
        _pool = queue;
        
        _executor = executor;
        _name = name;
        
        // Register probes
        MadFactory mFact = MadMetrics.getFactory();
        // Busy count
        mFact.newGauge(MonitoringUtils.MOCA_GROUP_NAME, PROBE_TYPE, "busy-count", _name, new MadGauge<Integer>() {

            @Override
            public Integer getValue() {
                return _counts.get().getBusy();
            }
        });
        
        // Idle count
        mFact.newGauge(MonitoringUtils.MOCA_GROUP_NAME, PROBE_TYPE, "idle-count", _name, new MadGauge<Integer>() {

            @Override
            public Integer getValue() {
                return _counts.get().getIdleCount();
            }
        });
        
        // Created count
        mFact.newGauge(MonitoringUtils.MOCA_GROUP_NAME, PROBE_TYPE, "created-count", _name, new MadGauge<Integer>() {

            @Override
            public Integer getValue() {
                return _counts.get().getCreated();
            }
        });
        
        // Queue size
        mFact.newGauge(MonitoringUtils.MOCA_GROUP_NAME, PROBE_TYPE, "queue-size", _name, new MadGauge<Integer>() {

            @Override
            public Integer getValue() {
                return _pool.size();
            }
        });
        
        // Implementation type
        final String implementation = this.getClass().getSimpleName();
        mFact.newGauge(MonitoringUtils.MOCA_GROUP_NAME, PROBE_TYPE, "implementation-type", _name, new MadGauge<String>() {

            @Override
            public String getValue() {
                return implementation;
            }
        });
        
        _buildTimer = mFact.newTimer(MonitoringUtils.MOCA_GROUP_NAME, PROBE_TYPE, "build-time", _name,
            TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
        
        _getTimer = mFact.newTimer(MonitoringUtils.MOCA_GROUP_NAME, PROBE_TYPE, "request-time", _name,
            TimeUnit.MILLISECONDS, TimeUnit.MINUTES);
        
        _timeoutMeter = mFact.newMeter(MonitoringUtils.MOCA_GROUP_NAME, PROBE_TYPE,
            "request-timeouts", _name, TimeUnit.MINUTES);
    }
    
    // @see com.redprairie.moca.pool.Pool#poll()
    @Override
    public T poll() {
        verifyRunning();
        
        handlePoolObjectRequested();
        
        boolean foundValidOrNone = false;
        T t = null;
        while (!foundValidOrNone) {
            t = _pool.poll();
            if (t != null) {
                try {
                    _validator.reset(t);
                    foundValidOrNone = true;
                }
                catch (PoolException e) {
                    handlePoolObjectResetException(t, e);
                    t = null;
                }
                // Handle unexpected exceptions that get rethrown
                catch (RuntimeException e) {
                    handlePoolObjectResetException(t, e);
                    throw e;
                }
                catch (Error e) {
                    handlePoolObjectResetException(t, e);
                    throw e;
                }
            }
            else {
                foundValidOrNone = true;
            }
        }
        
        if (t != null) {
            handlePoolObjectRetrieved(t);
        }
        
        return t;
    }
    
    // @see com.redprairie.moca.pool.BlockingPool#get(long, java.util.concurrent.TimeUnit)
    @Override
    public final T get(long timeOut, TimeUnit unit) throws InterruptedException {
        MadTimerContext tCtx = _getTimer.time();
        T t = getInternal(timeOut, unit);
        tCtx.stop();
        
        // Null indicates a timeout occurred waiting for an entry so update our meter
        if (t == null) {
            _timeoutMeter.mark();
        }
        
        return t;
    }

    // @see com.redprairie.moca.pool.BlockingPool#get()
    @Override
    public final T get() throws InterruptedException {
        MadTimerContext tCtx = _getTimer.time();
        T t = getInternal();
        tCtx.stop();
        
        return t;
    }
    
    // @see com.redprairie.moca.pool.Pool#release(java.lang.Object)
    @Override
    public void release(T t) {
        if (t == null) {
            throw new NullPointerException("Pooled object cannot be null");
        }
        
        if (_shutdown.get()) {
            _executor.execute(new InvalidatePoolObject(t));
        }
        else {
            boolean isValid = false;
            try {
                isValid = _validator.isValid(t);
            }
            finally {
                // Need this in the finally block incase
                // we encounter an unexpected exception in the validator
                if (isValid) {
                    handlePoolObjectReturned(t);
                    _pool.add(t);
                }
                else {
                    handlePoolObjectInvalidation(t);
                }
            }
        }
    }
    
    // @see com.redprairie.moca.pool.Pool#shutdown()
    @Override
    public void shutdown() {
        if(_shutdown.getAndSet(true)){
            // Was already shutdown
            return;
        }
        
        _logger.debug("Pool" + (_name != null
                ? " [" + _name + "]" : "") + " has been shutdown.");
        
        for (T t : _pool) {
            _executor.execute(new InvalidatePoolObject(t));
        }
        
        // If the executor is an executor service also shut it down
        if (_executor instanceof ExecutorService) {
            ((ExecutorService)_executor).shutdown();
        }
        
        Iterator<Thread> iter = _waiters.keySet().iterator();
        while (iter.hasNext()) {
            Thread waiter = iter.next();
            waiter.interrupt();
            iter.remove();
        }
    }
    
    // @see com.redprairie.moca.pool.Pool#isShutdown()
    @Override
    public boolean isShutdown() {
        return _shutdown.get();
    }
    
    // @see com.redprairie.moca.pool.Pool#getMaximumSize()
    @Override
    public Integer getMaximumSize() {
        return _maximumSize;
    }
    
    // @see com.redprairie.moca.pool.Pool#removePooledObject(java.lang.Object)
    @Override
    public boolean removePooledObject(T t) {
        verifyRunning();
        boolean removed = _pool.remove(t);
        if (removed) {
            IdleBusyCreatedCount counts = changeCounters(-1, 1, 0);
            if (_logger.isDebugEnabled()) {
                _logger.debug("Object manually removed from pool" + (_name != null
                        ? " [" + _name + "]" : "") + ": busy: " + 
                        counts.getBusy() + ", idle: " + counts.getIdleCount() + 
                        ", started " + counts.getCreated() + " / " + _maximumSize);
            }
        }
        return removed;
    }
    
    /**
     * This method should be invoked on the pool before any other operation.
     * Failure to do so will lead to the pool being in an invalid state and
     * any method call behavior is undefined.
     * @return Returns this pool so it is easier to initialize the pool inline
     */
    public final BlockingPool<T> initializePool() {
        if (!_initialized.getAndSet(true)) {
            onInitialize();
        }
        return this;
    }
    
    /**
     * Retrieves and removes an entry from the pool, waiting up to the
     * specified wait time if necessary for an element to become available.
     * Intended to be overrideable by implementing subclasses
     * @param timeout how long to wait before giving up, in units of
     *        <tt>unit</tt>
     * @param unit a <tt>TimeUnit</tt> determining how to interpret the
     *        <tt>timeout</tt> parameter
     * @return an entry from the pool, or <tt>null</tt> if the
     *         specified waiting time elapses before an element is available
     * @throws InterruptedException if interrupted while waiting
     */
    protected T getInternal(long timeOut, TimeUnit unit) throws InterruptedException {
        verifyRunning();
        
        Thread currentThread = Thread.currentThread();
        _waiters.put(currentThread, HASHVALUE);
        
        handlePoolObjectRequested();
        
        // We find what is the last time we will wait until
        long targetEnd = System.nanoTime() + unit.toNanos(timeOut);
        
        boolean foundValidOrNone = false;
        T t = null;
        try {
            while (!foundValidOrNone) {
                long target = targetEnd - System.nanoTime();
                t = _pool.poll(target, TimeUnit.NANOSECONDS);
                if (t == null) {
                    // This means we timed out, so just return null
                    foundValidOrNone = true;
                }
                else {
                    try {
                        _validator.reset(t);
                        foundValidOrNone = true;
                    }
                    catch (PoolException e) {
                        handlePoolObjectResetException(t, e);
                        t = null;
                    }
                    // Handle unexpected exceptions that get rethrown
                    catch (RuntimeException e) {
                        handlePoolObjectResetException(t, e);
                        throw e;
                    }
                    catch (Error e) {
                        handlePoolObjectResetException(t, e);
                        throw e;
                    }
                }
            }
        }
        finally {
            _waiters.remove(currentThread);
        }
        
        if (t != null) {
            handlePoolObjectRetrieved(t);
        }
        
        return t;
    }
    
    /**
     * Returns an instance from the pool, blocking until one is able to be
     * returned.  This method will only unblock from an interrupt request and
     * will then throw an InterruptedException.  This method can never return
     * null. This can be overrided by implementing subclasses
     * @return An instance from the pool, will always be non null
     * @throws InterruptedException if interrupted while waiting
     */
    protected T getInternal() throws InterruptedException {
        verifyRunning();
        
        Thread currentThread = Thread.currentThread();
        _waiters.put(currentThread, HASHVALUE);
        
        handlePoolObjectRequested();
        
        T t = null;
        try {
            while (t == null) {
                t = _pool.take();
                
                try {
                    _validator.reset(t);
                }
                catch (PoolException e) {
                    handlePoolObjectResetException(t, e);
                    t = null;
                }
                // Handle unexpected exceptions that get rethrown
                catch (RuntimeException e) {
                    handlePoolObjectResetException(t, e);
                    throw e;
                }
                catch (Error e) {
                    handlePoolObjectResetException(t, e);
                    throw e;
                }
            }
        }
        finally {
            _waiters.remove(currentThread);
        }
        
        handlePoolObjectRetrieved(t);
        
        return t;
    }
    
    /**
     * This method should be overridden to provide for initialization time
     * operations.  This method will only be invoked during initialization and
     * users who extends this class should never directly call this method
     */
    protected void onInitialize() {
        // We don't do anything by default
    }
    
    protected void finalize() throws Throwable {
        shutdown();
    }
    
    // Internal overridable methods below
    
    /**
     * Method called by pool when an object is found to throw an exception
     * when it was tried to be reset.  Default implementation logs a warning
     * message, decrements idle/created counts and invalidates the pooled object
     * @param object The pooled object that could not be reset 
     * @param t The throwable that was thrown when reset was attempted
     */
    protected void handlePoolObjectResetException(T object, Throwable t) {

        IdleBusyCreatedCount counts = changeCounters(-1, 0, -1);
        if (_logger.isDebugEnabled()) {
            _logger.debug("Pooled object cannot be reused must discard", t);
            _logger.debug("Requesting object from to pool" + (_name != null
                    ? " [" + _name + "]" : "") + ": busy: " + 
                    counts.getBusy() + ", idle: " + counts.getIdleCount() + 
                    ", started " + counts.getCreated() + " / " + _maximumSize);
        }
        else {
            _logger.warn("Pooled object cannot be reused must discard: " + t.toString());
        }
        _executor.execute(new InvalidatePoolObject(object));
    }
    
    /**
     * This is called when a pool object fails initialization.  This object is
     * not placed in the queue and is available to be garbage collected.  The default
     * implementation is to first print a message and then invalidate the object.
     * The method will then sleep for 1 second to prevent tight loop spawning.
     * Finally it will update the counts to decrement the creation count.
     * @param object The object that threw an exception during initialization
     * @param e The exception encountered when initializing
     */
    protected void handlePoolObjectInitializeException(T object, 
        Throwable e) {
        if (_logger.isDebugEnabled()) {
            _logger.error("Pool object could not be added due to exception", e);
        }
        else {
            _logger.error("Pool object could not be added due to exception: " + 
                    e.toString());
        }
        
        if (object != null) {
            // First we invalidate it if actually got created
            _executor.execute(new InvalidatePoolObject(object));
        }
        
        boolean interrupted = false;
        try {
            // We do a sleep in case if we end up forcing another process to
            // be created.  We don't want to create a busy loop if it 
            // continually tries to create objects that fail initialization
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        }
        catch (InterruptedException e1) {
            interrupted = true;
        }
        finally {
            changeCounters(0, 0, -1);
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * This method is invoked when the pool object is found to be invalid.  The
     * default behavior is to invalidate the object using the 
     * {@link Validator#invalidate(Object)} method of the provider validator on
     * a separate thread.  The counts will also be udpated so busy and created 
     * counts are decremented (a pool object should only be released if 
     * retrieved first)
     * @param object The now invalid pool object
     */
    protected void handlePoolObjectInvalidation(T object) {
        IdleBusyCreatedCount counts = changeCounters(0, -1, -1);
        _executor.execute(new InvalidatePoolObject(object));
        
        if (_logger.isDebugEnabled()) {
            _logger.debug("Object not returned to pool due to Invalidation" + 
                    (_name != null ? " [" + _name + "]" : "") + ": busy: " + 
                    counts.getBusy() + ", idle: " + counts.getIdleCount() + 
                    ", started " + counts.getCreated() + " / " + _maximumSize);
        }
    }
    
    /**
     * This method is invoked when a pool object is requested from the pool.
     * This method is called on the same method that is requesting the object.
     * Default implementation prints a debug message of pool sizing using the
     * counts.  No modifications to counts are done.
     */
    protected void handlePoolObjectRequested() {
        if (_logger.isDebugEnabled()) {
            IdleBusyCreatedCount counts = _counts.get();
            _logger.debug("Requesting object from pool" + (_name != null
                    ? " [" + _name + "]" : "") + ": busy: " + 
                    counts.getBusy() + ", idle: " + counts.getIdleCount() + 
                    ", started " + counts.getCreated() + " / " + _maximumSize);
        }
    }
    
    /**
     * This method is invoked when a pool object is taken out and provided to
     * a caller.  The default implementation will decrement the idle count 
     * and increment the busy count.
     * @param object The pooled object that was returned
     */
    protected void handlePoolObjectRetrieved(T object) {
        IdleBusyCreatedCount counts = changeCounters(-1, 1, 0);
        if (_logger.isDebugEnabled()) {
            _logger.debug("Object retrieved from pool" + (_name != null
                    ? " [" + _name + "]" : "") + ": busy: " + 
                    counts.getBusy() + ", idle: " + counts.getIdleCount() + 
                    ", started " + counts.getCreated() + " / " + _maximumSize);
        }
    }
    
    /**
     * This method is invoked when a pool object is returned to the pool.  The 
     * default method decrements the idle count and increment the busy count.
     * @param object The pooled object that was returned
     */
    protected void handlePoolObjectReturned(T object) {
        IdleBusyCreatedCount counts = changeCounters(1, -1, 0);
        if (_logger.isDebugEnabled()) {
            _logger.debug("Object returned to pool" + (_name != null
                    ? " [" + _name + "]" : "") + ": busy: " + 
                    counts.getBusy() + ", idle: " + counts.getIdleCount() + 
                    ", started " + counts.getCreated() + " / " + _maximumSize);
        }
    }
    
    /**
     * This method is invoked right after a pool object has been created and
     * initialized before putting it in the pool.  The default implementation
     * is to increment the idle count for the pool now that it is available.
     * @param object The object that was just created
     */
    protected void handlePoolObjectCreated(T object) {
     // We add the object to idle now that it is up, the caller
        // should have incremented creation
        changeCounters(1, 0, 0);
    }
    
    /**
     * This method can be called to verify the pool is running.  If the pool
     * is running this will throw an {@link IllegalStateException}.  If you
     * wish to tell if the pool is running or not you can check the 
     * {@link #_shutdown} variable.
     * @throws IllegalStateException Thrown if the pool is shutdown
     */
    protected void verifyRunning() throws IllegalStateException {
        if (_shutdown.get()) {
            throw new IllegalStateException("Pool has been shutdown");
        }
    }
    
    /**
     * Method to increase/decrease the idle, busy and creation counters atomically.
     * All values will be updated at the same time atomically with the 
     * appropriate offsets provided.  The resultant values are returned
     * @param idle How much more to increase the idle count (can be negative)
     * @param busy How much more to increase the busy count (can be negative)
     * @param creation How much more to increase the creation count (can be 
     *        negative)
     * @return the resultant counts
     */
    protected IdleBusyCreatedCount changeCounters(int idle, int busy, 
        int creation) {
        IdleBusyCreatedCount returnCount = null;
        while (returnCount == null) {
            IdleBusyCreatedCount counts = _counts.get();
            returnCount = attemptChangeCounters(counts, idle, busy, creation);
        }
        
        return returnCount;
    }
    
    /**
     * Method to attempt to increase/decrease the idle, busy and creation 
     * counters atomically.  All values will be updated at the same time 
     * atomically with the appropriate offsets provided if possible.  If the
     * values couldn't be updated this time this method will return null.  If
     * the values were updated the resultant values are returned
     * @param idle How much more to increase the idle count (can be negative)
     * @param busy How much more to increase the busy count (can be negative)
     * @param creation How much more to increase the creation count (can be 
     *        negative)
     * @return The resultant counts if they were updated or null if not
     */
    protected IdleBusyCreatedCount attemptChangeCounters(
        IdleBusyCreatedCount previousCounts, int idle, int busy, int creation) {
        int newIdle = previousCounts.getIdleCount() + idle;
        int newBusy = previousCounts.getBusy() + busy;
        int newCreated = previousCounts.getCreated() + creation;
        
        if (newIdle < 0 || newIdle > newCreated || 
                newBusy < 0 || newBusy > newCreated ||
                newCreated < 0 || newCreated > _maximumSize) {
            throw new IllegalStateException("Pool "
                    + (_name != null ? "[" + _name + "] " : "")
                    + "in invalid state! Idle: " + newIdle + ", Busy: "
                    + newBusy + ", Creation: " + newCreated + ", Max: "
                    + _maximumSize);
        }
        IdleBusyCreatedCount newCounts = new IdleBusyCreatedCount(
            previousCounts.getIdleCount() + idle,
            previousCounts.getBusy() + busy, 
            previousCounts.getCreated() + creation);
        if (_counts.compareAndSet(previousCounts, newCounts)) {
            return newCounts;
        }
        return null;
    }
    
    protected static class IdleBusyCreatedCount {
        /**
         * @param idleCount
         * @param busyCount
         * @param createdCount
         */
        public IdleBusyCreatedCount(int idleCount, int busyCount, int createdCount) {
            _idleCount = idleCount;
            _busyCount = busyCount;
            _createdCount = createdCount;
        }
        
        /**
         * @return Returns the idleCount.
         */
        public int getIdleCount() {
            return _idleCount;
        }
        
        /**
         * @return Returns the busyCount.
         */
        public int getBusy() {
            return _busyCount;
        }
        /**
         * @return Returns the createdCount.
         */
        public int getCreated() {
            return _createdCount;
        }
        
        private final int _idleCount;
        private final int _busyCount;
        private final int _createdCount;
    }
    
    /**
     * This class can be used invalide the pool object.  This runnable will
     * just invoke the {@link Validator#invalidate(Object)} method on the 
     * pool's validator.
     * 
     * Copyright (c) 2012 RedPrairie Corporation
     * All Rights Reserved
     * 
     * @author wburns
     */
    protected class InvalidatePoolObject implements Runnable {
        private final T _object;
        
        public InvalidatePoolObject(T object) {
            _object = object;
        }
        
        // @see java.lang.Runnable#run()
        @Override
        public void run() {
            _validator.invalidate(_object);
        }
    }
    
    /**
     * This class can be used create a pool object.  This runnable will
     * invoke the {@link Builder#build()} method of the pool's builder.  It
     * will then {@link Validator#initialize(Object)} the pooled object.  If
     * the object is initialized it will then be added to the pool.  If the pool
     * object fails to initialize the {@link AbstractBlockingPool#handlePoolObjectInitializeException(Object, PoolException)}
     * method is then invoked.
     * 
     * Copyright (c) 2012 RedPrairie Corporation
     * All Rights Reserved
     * 
     * @author wburns
     */
    protected final Runnable _createPoolObject = new Runnable() {
        // @see java.lang.Runnable#run()
        @Override
        public void run() {
            _logger.debug("Pool" + (_name != null
                    ? " [" + _name + "]" : "") + " creating new object");
            T object = null;
            try {
                MadTimerContext tCtx = _buildTimer.time();
                object = _builder.build();
                tCtx.stop();
                
                _validator.initialize(object);
                
                handlePoolObjectCreated(object);
                // If it could never be offered there was a problem!
                _pool.add(object);
            }
            catch (PoolException e) {
                handlePoolObjectInitializeException(object, e);
            }
            // We need to account for a runtime exception/error occurring
            // Using catch instead of finally so we have visibility on the exception
            catch (RuntimeException e) {
                e.printStackTrace();
                handlePoolObjectInitializeException(object, e);
                throw e;
            }
            catch (Error e) {
                handlePoolObjectInitializeException(object, e);
                throw e;
            }
        }
    };
    
    /**
     * This value is only used as a value place holder for the concurrent map
     * since it is used as a concurrent hash set.
     */
    static final Object HASHVALUE = new Object();
    /**
     * Static atomic integer that retains how many thread pools were created
     * on behalf of this class, so they can be easier to distinguish
     */
    static final AtomicInteger _poolCount = new AtomicInteger();
    
    protected final Logger _logger = LogManager.getLogger(getClass());
    
    protected final String _name;
    protected final BlockingQueue<T> _pool;
    protected final int _maximumSize;
    /**
     * This is a map that retains essentially a concurrent hash set of waiters.
     * This map is retained in case if the pool is shutdown while someone is
     * waiting so we can interrupt their thread.
     */
    protected final ConcurrentMap<Thread, Object> _waiters = 
            new ConcurrentHashMap<Thread, Object>();

    protected final Validator<? super T> _validator;
    protected final Builder<? extends T> _builder;

    protected final AtomicBoolean _initialized = new AtomicBoolean(false);
    protected final AtomicBoolean _shutdown = new AtomicBoolean(false);
    
    protected final Executor _executor;
    
    protected final AtomicReference<IdleBusyCreatedCount> _counts = 
            new AtomicReference<IdleBusyCreatedCount>(new IdleBusyCreatedCount(
                0, 0, 0));
    
    protected static final String PROBE_TYPE = "Pool-Queues";
    
    /**
     * Times the Pools associated builders build() time
     */
    private final MadTimer _buildTimer;
    
    /**
     * Times the amount of time waiting for an entry to be 
     * available in the pool queue
     */
    private final MadTimer _getTimer;
    
    /**
     * Tracks the rate that requests for entries from the pool
     * are timing out due to no entries in the pool for X amount of time
     */
    private final MadMeter _timeoutMeter;
}
