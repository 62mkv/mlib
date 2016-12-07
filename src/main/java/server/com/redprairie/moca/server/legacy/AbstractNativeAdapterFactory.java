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

import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.Builder;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.repository.CommandRepository;
import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.util.MocaUtils;

/**
 * This is the abstract native adapter factory that provides common 
 * functionality between the adapters.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public abstract class AbstractNativeAdapterFactory 
        implements NativeAdapterFactory {
    
    public AbstractNativeAdapterFactory(NativeProcessPoolBuilder poolBuilder, 
            CommandRepository repos) {
        _poolBuilder = poolBuilder.builder(new LibraryInitializationBuilder(
            builder()));
        _repos = repos;
    }

    // @see com.redprairie.moca.server.legacy.NativeAdapterFactory#getNativeAdapter(com.redprairie.moca.server.exec.ServerContext)
    @Override
    public NativeLibraryAdapter getNativeAdapter(ServerContext ctx)
            throws MocaException {
        if (!_initialized.get()) {
            throw new IllegalStateException("The native factory needs to be " +
                        "initialized first before retrieving a native adapter!");
        }
        NativeProcess process = _pool.getNativeProcess();
        nativeProcessAssociated(process, ctx);
        
        NativeLibraryAdapter adapter = new PooledNativeLibraryAdapter(process, 
                getServerAdapter(ctx));
        
        return adapter;
    }

    // @see com.redprairie.moca.server.legacy.NativeAdapterFactory#initialize()
    @Override
    public void initialize() throws MocaException {
        // We only want to initialize once
        boolean initialized = _initialized.getAndSet(true);
        
        if (!initialized) {
            _pool = _poolBuilder.build();
            setup();
            
            final WeakReference<NativeProcessPool> poolRef = 
                    new WeakReference<NativeProcessPool>(_pool);
                
            Runtime.getRuntime().addShutdownHook(new Thread() {
                // @see java.lang.Thread#run()
                @Override
                public void run() {
                    NativeProcessPool pool = poolRef.get();
                    if (pool != null) {
                        // We want to clean up the pool in an attempt to show memory
                        // leaks in the native proceses.
                        pool.shutdown();
                    }
                }
            });
        }
    }
    
    // @see com.redprairie.moca.server.legacy.NativeAdapterFactory#restart()
    @Override
    public void restart(boolean clean, CommandRepository repos) {
        synchronized(this) {
            _repos = repos;
        }
        
        if (clean) {
            _pool.restartPool();
        }
        else {
            _pool.forciblyRestartPool();
        }
    }
    
    /**
     * @return
     */
    public String getNextID() {
        return "moca-process-" + _processCount.getAndIncrement();
    }
    
    // @see com.redprairie.moca.server.legacy.NativeAdapterFactory#getNativeProcessPool()
    @Override
    public NativeProcessPool getNativeProcessPool() {
        return _pool;
    }
    
    NativeProcess initializeLibraries(NativeProcess process) 
            throws RemoteException {
        String processID = process.getId();
        _logger.debug(MocaUtils.concat("Registering Process ID ", processID));
        
        // Initialize native library
        _logger.debug(MocaUtils.concat("Process ID ", processID, 
                " registered"));
        List<ComponentLevel> levels;
        synchronized(AbstractNativeAdapterFactory.this) {
            levels = _repos.getLevels();
        }
        
        for (ComponentLevel level : levels) {
            String lib = level.getLibrary();

            if (lib != null) {
                String levelName = level.getName();
                try {
                    _logger.debug(MocaUtils.concat(processID, ": Loading library ",
                            lib));
                    process.loadLibrary(levelName, lib);
                }
                catch (MocaException e) {
                    _logger.warn("Error initializing native library "
                            + lib + ": " + e);
                }
                catch (RemoteException e) {
                    _logger.warn("RMI error initializing native library "
                                    + lib + ": " + e, e);
                    throw e;
                }
                finally {
                    _logger.debug(MocaUtils.concat(processID,
                            ": Finished loading library ", lib));
                }
            }
        }

        return process;
    }
    
    protected void nativeProcessAssociated(NativeProcess process, ServerContext context) {
    }
    
    private class LibraryInitializationBuilder implements Builder<NativeProcess> {
        public LibraryInitializationBuilder(Builder<? extends NativeProcess> builder) {
            _realBuilder = builder;
        }
        @Override
        public NativeProcess build() {
            NativeProcess process = _realBuilder.build();
            if (process != null) {
                try {
                    initializeLibraries(process);
                }
                // If we got an exception while initializing don't let it out
                catch (RemoteException e) {
                    process = null;
                }
            }
            return process;
        }
        
        private final Builder<? extends NativeProcess> _realBuilder;
    };
    
    protected abstract Builder<? extends NativeProcess> builder();
    
    protected abstract MocaServerAdapter getServerAdapter(ServerContext ctx) throws MocaException;
    
    protected abstract void setup() throws MocaException;
    
    
    final AtomicBoolean _initialized = new AtomicBoolean(false);
    final AtomicInteger _processCount = new AtomicInteger();
    protected final NativeProcessPoolBuilder _poolBuilder;
    protected NativeProcessPool _pool;
    protected CommandRepository _repos;
    protected static final Logger _logger = LogManager.getLogger(NativeAdapterFactory.class);
}
