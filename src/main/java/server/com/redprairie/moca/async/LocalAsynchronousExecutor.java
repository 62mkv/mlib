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

package com.redprairie.moca.async;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.util.concurrent.SettableFuture;
import com.redprairie.moca.AsynchronousExecutor;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.ServerContext;

/**
 * This is a local in thread asynchronous executor implementation.
 * This asynchronous executor only uses the current context from which it is
 * invoked.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class LocalAsynchronousExecutor implements AsynchronousExecutor {
    // @see com.redprairie.moca.AsynchronousExecutor#executeAsynchronously(java.util.concurrent.Callable)
    @Override
    public <V> Future<V> executeAsynchronously(Callable<V> callable) {
        SettableFuture<V> future = SettableFuture.create();
        ServerContext contextNullable = ServerUtils.getCurrentContextNullable();
        try {
            V value = callable.call();
            future.set(value);
            if (contextNullable != null) {
                contextNullable.commit();
            }
        }
        catch (Throwable e) {
            future.setException(e);
            if (contextNullable != null) {
                try {
                    contextNullable.rollback();
                }
                catch (MocaException e1) {
                    _logger.warn("There was a problem rolling back transaction " +
                            "for executing " + callable + " when it failed!");
                }
            }
        }
        return future;
    }

    // @see com.redprairie.moca.AsynchronousExecutor#executeAsynchronously(java.util.concurrent.Callable, com.redprairie.moca.AsynchronousExecutor.AsynchronousExecutorCallback)
    @Override
    public <V> void executeAsynchronously(Callable<V> callable,
        AsynchronousExecutorCallback<V> callback) {
        Future<V> future = executeAsynchronously(callable);
        ServerContext contextNullable = ServerUtils.getCurrentContextNullable();
        try {
            callback.done(callable, future);
            if (contextNullable != null) {
                contextNullable.commit();
            }
        }
        catch (Exception e) {
            if (contextNullable != null) {
                try {
                    contextNullable.rollback();
                }
                catch (MocaException e1) {
                    _logger.warn("There was a problem rolling back transaction " +
                            "for executing " + callable + " when it failed!");
                }
            }
        }

    }

    // @see com.redprairie.moca.AsynchronousExecutor#executeGroupAsynchronously(java.util.concurrent.Callable<V>[])
    @Override
    public <V> CompletionService<V> executeGroupAsynchronously(
        Callable<V>... callables) {
        BlockingQueue<Future<V>> queue = new LinkedBlockingQueue<Future<V>>();
        for (Callable<V> callable : callables) {
            Future<V> future = executeAsynchronously(callable);
            queue.add(future);
        }
        return new ExecutorCompletionService<V>(new Executor() {
            
            @Override
            public void execute(Runnable command) {
                executeAsynchronously(Executors.callable(command));
            }
        }, queue);
    }
    
    private static final Logger _logger = LogManager.getLogger(
        LocalAsynchronousExecutor.class);
}
