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

package com.redprairie.moca.server.legacy;

import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.Builder;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.pool.BlockingPool;
import com.redprairie.moca.pool.BlockingPoolBuilder;
import com.redprairie.moca.pool.PoolException;
import com.redprairie.moca.pool.Validator;
import com.redprairie.moca.pool.validators.BaseValidator;
import com.redprairie.moca.pool.validators.PoolUsageValidator;
import com.redprairie.moca.pool.validators.SimpleValidator;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.util.ClassUtils;

/**
 * This is the native process pool that is to be accessed remotely from tasks
 * or internally from the server.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class MocaNativeProcessPool implements NativeProcessPool {
    public MocaNativeProcessPool(long poolTimeout, TimeUnit poolTimeoutUnit, 
        int poolMinIdleSize, int poolMaxSize, Integer maxPoolUsage, 
        Builder<? extends NativeProcess> builder) {
        
        this("Native-Process", poolTimeout, poolTimeoutUnit, poolMinIdleSize, 
            poolMaxSize, maxPoolUsage, builder);
    }
    
    MocaNativeProcessPool(String poolName, long poolTimeout, TimeUnit poolTimeoutUnit, 
        int poolMinIdleSize, int poolMaxSize, Integer maxPoolUsage, 
        Builder<? extends NativeProcess> builder) {
        
        _poolTimeout = poolTimeout;
        _poolTimeoutUnit = poolTimeoutUnit;
        _poolMaxSize = poolMaxSize;
        
        _simpleValidator = new SimpleValidator<NativeProcess>();
        
        List<Validator<NativeProcess>> validators = new ArrayList<Validator<NativeProcess>>();
        validators.add(_simpleValidator);
        // If user provides a max pool usage, then setup another validator to
        // control that usage
        if (maxPoolUsage != null) {
            _usageValidator = new PoolUsageValidator<NativeProcess>(maxPoolUsage);
            validators.add(_usageValidator);
        }
        else {
            _usageValidator = null;
        }
        
        // Make sure our validator is last, since it relies on if our method
        // returns true that we were last
        validators.add(new OurValidator());
        
        BlockingPoolBuilder<NativeProcess> poolBuilder = 
                new BlockingPoolBuilder<NativeProcess>(builder);
        _pool = poolBuilder
            .name(poolName)
            .validators(validators)
            .minMaxSize(poolMinIdleSize, _poolMaxSize).build();
    }
    
    // @see com.redprairie.moca.server.legacy.NativeProcessPool2#getNativeProcess()
    @Override
    public NativeProcess getNativeProcess() throws NativeProcessTimeoutException {
        NativeProcess process;
        try {
            process = _pool.get(_poolTimeout, _poolTimeoutUnit);
            if (process == null) {
                throw new NativeProcessTimeoutException("Timeout waiting for process to become available!");
            }
        }
        catch (InterruptedException e) {
            _logger.debug("Interrupted while waiting for Native Process!");
            throw new MocaInterruptedException(e);
        }
        
        _logger.debug(MocaUtils.concat("Retrieved process ",
            process.getId(), " from the pool."));
        
        NativeProcess pooledProcess = (NativeProcess)Proxy.newProxyInstance(
            ClassUtils.getClassLoader(),
            new Class[] {NativeProcess.class},
            new NativeProcessHandler(_pool, process, _simpleValidator));
        return pooledProcess;
    }
    
    // @see com.redprairie.moca.server.legacy.NativeProcessPool2#restartPool()
    @Override
    public void restartPool() {
        _shutdownWriteLock.lock();
        try {
            _logger.info("Restarting native process pool");
            Iterator<NativeProcess> busyProcesses = _busyProcesses.keySet().iterator();
            while (busyProcesses.hasNext()) {
                // The temp process still counts against the pool count, but
                // will be automatically invalidated when released back.  This 
                // is required as the pool has the invalidator.
                NativeProcess busyProcess = busyProcesses.next();
                busyProcesses.remove();
                _logger.info("Making process temp: " + busyProcess.getId());
                _prevProcesses.put(busyProcess, HASHVALUE);
            }
            
            // Now we drain all of the idle processes and release them back
            // which will kill them all
            NativeProcess process;
            while ((process = _pool.poll()) != null) {
                _busyProcesses.remove(process);
                _pool.release(process);
            }
        }
        finally {
            _shutdownWriteLock.unlock();
        }
    }
    
    // @see com.redprairie.moca.server.legacy.NativeProcessPool2#forciblyRestartPool()
    @Override
    public void forciblyRestartPool() {
        _logger.info("Forcibly restarting process pool");
        forciblyCloseProcesss(true);
    }
    
    private void forciblyCloseProcesss(boolean takeCareOfIdle) {
        _shutdownWriteLock.lock();
        try {
            Iterator<NativeProcess> prevProcesses = _prevProcesses.keySet().iterator();
            while (prevProcesses.hasNext()) {
                NativeProcess prevProcess = prevProcesses.next();
                prevProcesses.remove();
                _pool.release(prevProcess);
            }
            
            Iterator<NativeProcess> busyProcesses = _busyProcesses.keySet().iterator();
            while (busyProcesses.hasNext()) {
                NativeProcess busyProcess = busyProcesses.next();
                busyProcesses.remove();
                _pool.release(busyProcess);
            }
            
            if (takeCareOfIdle) {
                // Now we drain all of the idle processes and release them back
                // which will kill them all
                NativeProcess process;
                while ((process = _pool.poll()) != null) {
                    _busyProcesses.remove(process);
                    _pool.release(process);
                }
            }
        }
        finally {
            _shutdownWriteLock.unlock();
        }
    }
    
    // @see com.redprairie.moca.server.legacy.NativeProcessPool2#getAllProcesses()
    @Override
    public Collection<NativeProcess> getAllProcesses() {
        Set<NativeProcess> allProcesses = new HashSet<NativeProcess>();
        
        allProcesses.addAll(_busyProcesses.keySet());
        allProcesses.addAll(_idleProcesses.keySet());
        
        return allProcesses;
    }

    // @see com.redprairie.moca.server.legacy.NativeProcessPool2#getActiveProcesses()
    @Override
    public Collection<NativeProcess> getActiveProcesses() {
        return new ArrayList<NativeProcess>(_busyProcesses.keySet());
    }

    // @see com.redprairie.moca.server.legacy.NativeProcessPool2#getTemporaryProcesses()
    @Override
    public Collection<NativeProcess> getTemporaryProcesses() {
        return new ArrayList<NativeProcess>(_prevProcesses.keySet());
    }

    // @see com.redprairie.moca.server.legacy.NativeProcessPool2#getSize()
    @Override
    public int getSize() {
        return _currentSize.get();
    }

    // @see com.redprairie.moca.server.legacy.NativeProcessPool2#getPeakSize()
    @Override
    public int getPeakSize() {
        return _peakSize.get();
    }

    // @see com.redprairie.moca.server.legacy.NativeProcessPool2#getMaximumSize()
    @Override
    public int getMaximumSize() {
        return _poolMaxSize;
    }

    // @see com.redprairie.moca.server.legacy.NativeProcessPool2#shutdownProcess(com.redprairie.moca.server.legacy.NativeProcess)
    @Override
    public void shutdownProcess(NativeProcess process) {
        if (Proxy.isProxyClass(process.getClass())) {
            Object invocHandler = Proxy.getInvocationHandler(process);
            if (invocHandler instanceof NativeProcessHandler) {
                NativeProcessHandler handler = (NativeProcessHandler)invocHandler;
                process = handler.getRealObject();
                // We tell the handler it is closed as well
                handler._closed.set(true);
                // If the process is already gone
                if (process == null) {
                    return;
                }
            }
        }
        _shutdownWriteLock.lock();
        try {
            // This will set it to be invalid first off
            boolean wasBusy = _busyProcesses.remove(process) != null;
            if (wasBusy) {
                _logger.warn("Active Native Process " + process.getId() + 
                    " manually shutdown!");
                _pool.release(process);
            }
            // By removing the process from idle it will be shutdown when
            // it is retrieved from the pool.
            _idleProcesses.remove(process);
            
            // If we are able to remove the pooled process we just release it
            // back now that we have forced it to be invalid by removing it
            // from the various process queues
            if (_pool.removePooledObject(process)) {
                _pool.release(process);
            }
        }
        finally {
            _shutdownWriteLock.unlock();
        }
        
    }

    // @see com.redprairie.moca.server.legacy.NativeProcessPool2#timesTaken(com.redprairie.moca.server.legacy.NativeProcess)
    @Override
    public Integer timesTaken(NativeProcess process) {
        if (_usageValidator == null) {
            return null;
        }
        else {
            return _usageValidator.getCount(process);
        }
    }
    
    // @see com.redprairie.moca.server.legacy.NativeProcessPool#shutdown()
    @Override
    public void shutdown() {
        // The shutdown closes down idle guys already
        forciblyCloseProcesss(false);
        _pool.shutdown();
    }
    
    private class OurValidator extends BaseValidator<NativeProcess> {
        // @see com.redprairie.moca.pool.validators.BaseValidator#initialize(java.lang.Object)
        @Override
        public void initialize(NativeProcess t) {
            _idleProcesses.put(t, HASHVALUE);
            // We have to update peak and current at same time
            synchronized (_currentSize) {
                int current = _currentSize.incrementAndGet();
                _peakSize.compareAndSet(current - 1, current);
            }
        }
        
        // @see com.redprairie.moca.pool.validators.BaseValidator#reset(java.lang.Object)
        @Override
        public void reset(NativeProcess t) throws PoolException {
            _shutdownReadLock.lock();
            try {
                if (_idleProcesses.remove(t) == null) {
                    throw new PoolException(12345, "Process was no longer available, " +
                            "most likely due from restart or shutdown");
                }
                _busyProcesses.put(t, HASHVALUE);
            }
            finally {
                _shutdownReadLock.unlock();
            }
        }
        
        // @see com.redprairie.moca.pool.validators.BaseValidator#isValid(java.lang.Object)
        @Override
        public boolean isValid(NativeProcess t) {
            _shutdownReadLock.lock();
            try {
                boolean removed = _busyProcesses.remove(t) != null;
                // It is only valid if the process is in our busy map
                if (removed) {
                    _logger.debug(MocaUtils.concat("Returning process ",
                        t.getId(), " to the pool."));
                    _idleProcesses.put(t, HASHVALUE);
                }
                
                // If it wasn't removed that means it is invalid
                return removed;
            }
            finally {
                _shutdownReadLock.unlock();
            }
        }
        
        // @see com.redprairie.moca.pool.validators.BaseValidator#invalidate(java.lang.Object)
        @Override
        public void invalidate(NativeProcess t) {
            _logger.debug("Invalidating process: " + t.getId());
            _currentSize.decrementAndGet();
            
            _prevProcesses.remove(t);
            _idleProcesses.remove(t);
            _busyProcesses.remove(t);
            try {
                t.close();
            }
            catch (RemoteException e) {
                _logger.debug("Problem encountered shutting down native process", e);
            }
        }
    }
    
    private final ReadWriteLock _shutdownLock = new ReentrantReadWriteLock();
    private final Lock _shutdownReadLock = _shutdownLock.readLock();
    private final Lock _shutdownWriteLock = _shutdownLock.writeLock();
    
    /**
     * This value is only used as a value place holder for the concurrent map
     * since it is used as a concurrent hash set.
     */
    private static final Object HASHVALUE = new Object();
    
    private final ConcurrentMap<NativeProcess, Object> _prevProcesses = 
            new ConcurrentHashMap<NativeProcess, Object>();
    private final ConcurrentMap<NativeProcess, Object> _busyProcesses = 
            new ConcurrentHashMap<NativeProcess, Object>();
    private final ConcurrentMap<NativeProcess, Object> _idleProcesses = 
            new ConcurrentHashMap<NativeProcess, Object>();
    
    // The peak size of this pool
    private final AtomicInteger _peakSize = new AtomicInteger();
    
    private final AtomicInteger _currentSize = new AtomicInteger();
    
    private final long _poolTimeout;
    private final TimeUnit _poolTimeoutUnit;
    private final int _poolMaxSize;
    
    private final BlockingPool<NativeProcess> _pool;
    private final SimpleValidator<NativeProcess> _simpleValidator;
    private final PoolUsageValidator<NativeProcess> _usageValidator;
    
    static final transient Logger _logger = LogManager.getLogger(MocaNativeProcessPool.class);
}
