/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.sam.moca.async;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.jgroups.blocks.executor.ExecutionCompletionService;
import org.jgroups.protocols.Executing.Owner;
import org.jgroups.util.FutureListener;
import org.jgroups.util.NotifyingFuture;
import org.jgroups.util.Streamable;
import org.jgroups.util.Util;

import com.google.common.util.concurrent.ForwardingBlockingQueue;
import com.google.common.util.concurrent.SettableFuture;
import com.sam.moca.AsynchronousExecutor;
import com.sam.moca.MocaInterruptedException;
import com.sam.moca.cluster.jgroups.MocaExecutionService;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.exec.RequestContext;
import com.sam.moca.server.exec.ServerContext;
import com.sam.moca.server.exec.SessionContext;

/**
 * JGroups implementation of asynchronous executor.  This will submit tasks
 * to the cluster for completion
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class JGroupsAsynchronousExecutor implements AsynchronousExecutor {
    
    public JGroupsAsynchronousExecutor(MocaExecutionService service, Integer 
        maxRequestSize) {
        _executionService = service;
        if (maxRequestSize != null) {
            _callableQueue = new ArrayBlockingQueue<Callable<?>>(
                    maxRequestSize);
        }
        else {
            _callableQueue = new LinkedBlockingQueue<Callable<?>>();
        }
    }
    
    private <V>Callable<V> wrapCallable(Callable<V> callable, 
        AsynchronousExecutorCallback<V> callback) {
        ServerContext context = ServerUtils.getCurrentContextNullable();
        
        Map<String, String> env = new HashMap<String, String>();
        if (context != null) {
            SessionContext session = context.getSession();
            env.putAll(session.getAllVariables());
            
            RequestContext request = context.getRequest();
            env.putAll(request.getAllVariables());
        }
        return new CallableWrapper<V>(callable, callback, env);
    }
    
    public static class CallableWrapper<V> implements Callable<V>, Streamable {
        /**
         * This constructor should <b>never</b> be called except when 
         * deserializing
         */
        public CallableWrapper() {
            
        }
        
        public CallableWrapper(Callable<V> callable, 
            AsynchronousExecutorCallback<V> callback, Map<String, String> env) {
            if (!(callable instanceof Serializable || callable instanceof Streamable)) {
                IllegalArgumentException iae = new IllegalArgumentException(
                    "Command was not Serializable or Streamable - " + callable);
                
                String message = "Could not submit task: " + iae;
                if (_logger.isDebugEnabled()) {
                    _logger.debug(message, iae);
                }
                else {
                    _logger.error(message);
                }
                throw iae;
            }
            _callable = callable;
            _callback = callback;
            _env = env;
        }
        @Override
        public V call() throws Exception {
            ServerContext context = ServerUtils.getCurrentContext();
            RequestContext request = context.getRequest();
            for (Entry<String, String> entry : _env.entrySet()) {
                request.putVariable(entry.getKey(), entry.getValue());
            }
            V value = null;
            Throwable t = null;
            try {
                value = _callable.call();
                context.commit();
            }
            catch (Throwable e) {
                context.rollback();
                t = e;
            }
            
            final V returnedValue = value;
            final Throwable returnedException = t;
            
            if (_callback != null) {
                SettableFuture<V> future = SettableFuture.create();
                if (returnedException != null) {
                    future.setException(returnedException);
                }
                else {
                    future.set(returnedValue);
                }
                
                try {
                    _callback.done(_callable, future);
                    context.commit();
                }
                catch (Exception e) {
                    context.rollback();
                    _logger.warn("Callback had failure (ignored)!", e);
                }
            }

            if (t != null){
                if (t instanceof Exception) {
                    throw (Exception)t;
                }
                else if (t instanceof Error) {
                    throw (Error)t;
                }
                else {
                    throw new RuntimeException(t);
                }
            }
            else {
                return value;
            }
        }
        
        // @see org.jgroups.util.Streamable#readFrom(java.io.DataInput)
        @SuppressWarnings("unchecked")
        @Override
        public void readFrom(DataInput dis) throws IOException,
                IllegalAccessException, InstantiationException {
            boolean same = dis.readBoolean();
            try {
                _callable = (Callable<V>)Util.readObject(dis);
                if (!same) {
                    _callback = (AsynchronousExecutorCallback<V>)Util.readObject(dis);
                }
                else {
                    _callback = (AsynchronousExecutorCallback<V>)_callable;
                }
                
                int count = dis.readInt();
                _env = new HashMap<String, String>(count);
                
                for (int i = 0; i < count; ++i) {
                    _env.put(dis.readUTF(), dis.readUTF());
                }
            }
            catch (Exception e) {
                throw new IOException(e);
            }
        }
        // @see org.jgroups.util.Streamable#writeTo(java.io.DataOutput)
        @Override
        public void writeTo(DataOutput dos) throws IOException {
            try {
                boolean same = _callable == _callback;
                dos.writeBoolean(same);
                Util.writeObject(_callable, dos);
                if (!same) {
                    Util.writeObject(_callback, dos);
                }
                
                dos.writeInt(_env.size());
                
                for (Entry<String, String> entry : _env.entrySet()) {
                    dos.writeUTF(entry.getKey());
                    dos.writeUTF(entry.getValue());
                }
            }
            catch (Exception e) {
                throw new IOException(e);
            }
        }
        
        public Callable<V> getCallable() {
            return _callable;
        }
        
        // @see java.lang.Object#toString()
        @Override
        public String toString() {
            return "Cluster Async Callable [" + _callable + "]" + (_callback != null ? 
                    " with Callback [" + _callback + "]" : "");
        }
        
        private Callable<V> _callable;
        private AsynchronousExecutorCallback<V> _callback;
        private Map<String, String> _env;
    }

    // @see com.sam.moca.AsynchronousExecutor#executeAsynchronously(java.util.concurrent.Callable)
    @Override
    public <V> Future<V> executeAsynchronously(Callable<V> callable) {
        if (callable == null) {
            throw new NullPointerException();
        }
        try {
            _callableQueue.put(callable);
            NotifyingFuture<V> future = _executionService.submit(wrapCallable(
                callable, null));
            future.setListener(new RemovalFutureListener<V>(callable));
            return future;
        }
        catch (InterruptedException e) {
            throw new MocaInterruptedException(e);
        }
    }
    
    // @see com.sam.moca.AsynchronousExecutor#executeAsynchronously(java.util.concurrent.Callable, com.sam.moca.AsynchronousExecutor.AsynchronousExecutorCallback)
    @Override
    public <V> void executeAsynchronously(final Callable<V> callable,
        final AsynchronousExecutorCallback<V> callback) {
        if (callable == null || callback == null) {
            throw new NullPointerException();
        }
        try {
            _callableQueue.put(callable);
            NotifyingFuture<V> future = _executionService.submit(wrapCallable(
                callable, callback));
            future.setListener(new RemovalFutureListener<V>(callable));
        }
        catch (InterruptedException e) {
            throw new MocaInterruptedException(e);
        }
        
    }

    // @see com.sam.moca.AsynchronousExecutor#executeGroupAsynchronously(java.util.concurrent.Callable<V>[])
    @Override
    public <V> CompletionService<V> executeGroupAsynchronously(
        Callable<V>... callables) {
        final Map<Future<V>, Callable<V>> futureCallables = 
                new ConcurrentHashMap<Future<V>, Callable<V>>();
        
        BlockingQueue<NotifyingFuture<V>> queue = new RemovalBlockingQueue<NotifyingFuture<V>, V>(
            new LinkedBlockingQueue<NotifyingFuture<V>>(), futureCallables);
        
        ExecutionCompletionService<V> completionService = 
            new ExecutionCompletionService<V>(_executionService, queue) {
            // @see java.util.concurrent.ExecutorCompletionService#submit(java.util.concurrent.Callable)
            @Override
            public Future<V> submit(Callable<V> task) {
                try {
                    _callableQueue.put(task);
                }
                catch (InterruptedException e) {
                    throw new MocaInterruptedException(e);
                }
                Future<V> future = super.submit(wrapCallable(task, null));
                futureCallables.put(future, task);
                return future;
            }

            // @see java.util.concurrent.ExecutorCompletionService#submit(java.lang.Runnable, java.lang.Object)
            @Override
            public Future<V> submit(Runnable task, V result) {
                Callable<V> callable = Executors.callable(task, result);
                return submit(callable);
            }
        };
        for (Callable<V> callable : callables) {
            completionService.submit(wrapCallable(callable, null));
        }
        return completionService;
    }
    
    private class RemovalFutureListener<V> implements FutureListener<V> {
        
        private RemovalFutureListener(Callable<V> callable) {
            _callable = callable;
        }

        // @see org.jgroups.util.FutureListener#futureDone(java.util.concurrent.Future)
        @Override
        public void futureDone(Future<V> future) {
            // Now that are callable is done remove it from the pendingQueue
            // so another can be submitted and unblocked if they are
            _callableQueue.remove(_callable);
        }
        
        private final Callable<V> _callable;
    }
    
    private class RemovalBlockingQueue<V, T> extends ForwardingBlockingQueue<V> {
        
        public RemovalBlockingQueue(BlockingQueue<V> queue, 
            Map<Future<T>, Callable<T>> futureCallables) {
            _blockingQueue = queue;
            _futureCallables = futureCallables;
        }

        // @see com.google.common.util.concurrent.ForwardingBlockingQueue#delegate()
        @Override
        protected BlockingQueue<V> delegate() {
            return _blockingQueue;
        }
        
        public boolean add(V e) {
            _callableQueue.remove(_futureCallables.remove(e));
            return super.add(e);
        }
        
        private final BlockingQueue<V> _blockingQueue;
        private final Map<Future<T>, Callable<T>> _futureCallables;
    }
    
    /**
     * Returns a copy of the tasks that are currently being serviced
     * @return
     */
    public Map<Owner, Runnable> getRunningRequests() {
        return _executionService.getAwaitingReturnCopy();
    }
    
    /**
     * Returns an immutable collection of all tasks submitted
     * @return
     */
    public Collection<Runnable> getQueue() {
        return _executionService.getAwaitingConsumer();
    }

    private final MocaExecutionService _executionService;
    private final BlockingQueue<Callable<?>> _callableQueue;
    
    private final static Logger _logger = LogManager.getLogger(JGroupsAsynchronousExecutor.class);
}
