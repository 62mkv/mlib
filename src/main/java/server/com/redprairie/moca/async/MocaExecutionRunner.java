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

package com.redprairie.moca.async;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.JChannel;
import org.jgroups.blocks.executor.ExecutorEvent;
import org.jgroups.protocols.Executing;

import com.redprairie.moca.cache.Maybe;
import com.redprairie.moca.mad.async.Status;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.LocalSessionContext;
import com.redprairie.moca.server.exec.RequestContext;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SessionContext;
import com.redprairie.moca.server.exec.SessionType;
import com.redprairie.moca.server.session.SessionToken;

/**
 * Moca based ExecutionRunner that sets up context for each request.
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MocaExecutionRunner implements Runnable {
    /**
     * @param channel
     */
    public MocaExecutionRunner(JChannel channel, ServerContextFactory factory) {
        _factory = factory;
        setChannel(channel);
    }
    
    public void setChannel(JChannel ch) {
        this.ch=ch;
        _execProt=(Executing)ch.getProtocolStack().findProtocol(Executing.class);
        if(_execProt == null)
            throw new IllegalStateException("Channel configuration must include a executing protocol " +
                                              "(subclass of " + Executing.class.getName() + ")");
    }

    // @see java.lang.Runnable#run()
    @Override
    public void run() {
        final Lock shutdownLock = new ReentrantLock();
        // The following 2 atomic boolean should only ever be updated while
        // protected by the above lock.  They don't have to be atomic boolean,
        // but it is a nice wrapper to share a reference between threads.
        // Reads can be okay in certain circumstances
        final AtomicBoolean canInterrupt = new AtomicBoolean(true);
        final AtomicBoolean shutdown = new AtomicBoolean();
        // This thread is only spawned so that we can differentiate between
        // an interrupt of a task and an interrupt causing a shutdown of
        // runner itself.
        Thread executionThread = new Thread() {

            // @see java.lang.Thread#run()
            @Override
            public void run() {
                Thread currentThread = Thread.currentThread();
                Runnable runnable = null;
                
                _statuses.put(currentThread, new Status("Idle"));
                
                // This thread should only ever be interrupted by the outer
                // thread
                while (!shutdown.get()) {
                    _runnables.put(currentThread, new Maybe<Runnable>(
                            null));
                    runnable = (Runnable)ch.down(new ExecutorEvent(
                        ExecutorEvent.CONSUMER_READY, null));
                    
                    // This means we were interrupted while waiting
                    if (runnable == null) {
                        break;
                    }
                    
                    // First retrieve the lock to make sure we can tell them
                    // to not interrupt us
                    shutdownLock.lock();
                    try {
                        // Clear interrupt state as we don't want to stop the
                        // task we just received.  If we got a shutdown signal
                        // we will only do it after we loop back around.
                        Thread.interrupted();
                        canInterrupt.set(false);
                    }
                    finally {
                        shutdownLock.unlock();
                    }
                    _runnables.put(currentThread, new Maybe<Runnable>(
                            runnable));
                    _activeThreads.incrementAndGet();
                    
                    // Create a new request and session object
                    RequestContext req = new RequestContext(null);
                    String sessionId = "dist-async-" + Integer.toHexString(
                            System.identityHashCode(req));
                    SessionContext session = new LocalSessionContext(sessionId, 
                        SessionType.ASYNC);
                    
                    // Authenticate our session
                    session.setSessionToken(new SessionToken(sessionId));

                    // Go ahead and create a server context
                    ServerContext ctx = _factory.newContext(req, session);
                    
                    ctx.putSystemVariable("ASYNC_SESSION_ID", sessionId);
                    _sessions.put(sessionId, currentThread);
                    _statuses.get(currentThread).setStatus("Active");
                    
                    // Set up the new context on this thread.  We have to deassociate
                    // this in the done call on the future.
                    ServerUtils.setCurrentContext(ctx);
                    
                    try {
                        Throwable throwable = null;
                        try {
                            runnable.run();
                        }
                        // This can only happen if user is directly doing an execute(Runnable)
                        catch (Throwable t) {
                            _logger.error("Unexpected Runtime Error encountered in Runnable request", t);
                            throwable = t;
                        }
                        
                        ch.down(new ExecutorEvent(ExecutorEvent.TASK_COMPLETE, 
                            throwable != null ? new Object[]{runnable, throwable} : runnable));
                    }
                    finally {
                        _activeThreads.decrementAndGet();
                        _statuses.get(currentThread).setStatus("Idle");
                        _sessions.remove(currentThread);
                        ctx.close();
                        ServerUtils.setCurrentContext(null);
                    }
                    
                    // We have to let the outer thread we can now be interrupted
                    shutdownLock.lock();
                    try {
                        canInterrupt.set(true);
                    }
                    finally {
                        shutdownLock.unlock();
                    }
                }
                
                _statuses.remove(currentThread);
                _runnables.remove(currentThread);
            }
        };

        executionThread.setName(Thread.currentThread().getName() + "- Task Runner");
        executionThread.start();
        
        try {
            executionThread.join();
        }
        catch (InterruptedException e) {
            shutdownLock.lock();
            try {
                if (canInterrupt.get()) {
                    executionThread.interrupt();
                }
                shutdown.set(true);
            }
            finally {
                shutdownLock.unlock();
            }
            
            if (_logger.isTraceEnabled()) {
                _logger.trace("Shutting down Execution Runner");
            }
        }
    }
    
    /**
     * Returns a copy of the runners being used with the runner and what threads.
     * If a thread is not currently running a task it will return with a null
     * value.  This map is a copy and can be modified if necessary without
     * causing issues.
     * @return
     */
    public Map<Thread, Runnable> getCurrentRunningTasks() {
        Map<Thread, Runnable> map = new HashMap<Thread, Runnable>();
        for (Entry<Thread, Maybe<Runnable>> entry : _runnables.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getValue());
        }
        return map;
    }
    
    public Status getStatus(String sessionId) {
        Thread thread = _sessions.get(sessionId);
        Status status = _statuses.get(thread);
        
        return status;
    }
    
    public Status getStatus(Thread thread) {
        return _statuses.get(thread);
    }

    public void setStatus(String sessionId, String statusStr) {
        Thread thread = _sessions.get(sessionId);
        Status status = _statuses.get(thread);
        
        if (status != null) {
            status.setStatus(statusStr);
        }
    }
    
    public int getActiveThreadCount() {
        return _activeThreads.get();
    }
     
    private final ServerContextFactory _factory;
    private final Map<Thread, Maybe<Runnable>> _runnables = 
            new ConcurrentHashMap<Thread, Maybe<Runnable>>();
    
    private final Map<Thread, Status> _statuses = 
            new ConcurrentHashMap<Thread, Status>();
    
    private final Map<String, Thread> _sessions = 
            new ConcurrentHashMap<String, Thread>();
    
    private final AtomicInteger _activeThreads = new AtomicInteger(0);
    
    private JChannel ch;
    private Executing _execProt;
    
    private static final Logger _logger = LogManager.getLogger(
        MocaExecutionRunner.class);
}
