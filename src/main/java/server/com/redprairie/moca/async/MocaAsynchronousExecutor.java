/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.AsynchronousExecutor;
import com.redprairie.moca.mad.async.Status;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.LocalSessionContext;
import com.redprairie.moca.server.exec.RequestContext;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SessionContext;
import com.redprairie.moca.server.exec.SessionType;
import com.redprairie.moca.server.session.SessionToken;
import com.redprairie.moca.util.DaemonThreadFactory;
import com.redprairie.moca.util.MocaUtils;

/**
 * Implementation of AsynchronousExecutor that is done by wrapping a JRE 
 * {@link ScheduledThreadPoolExecutor} and providing hooks for callbacks
 * and configures each callable to have their own new session.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MocaAsynchronousExecutor implements AsynchronousExecutor {
    
    public MocaAsynchronousExecutor(ServerContextFactory factory, int poolSize) {
        _factory = factory;
        _executor = new AsynchronousExecutorService(poolSize);
    }
    
    // @see com.redprairie.moca.async.AsynchronousExecutor#executeAsynchronously(java.util.concurrent.Callable)
    @Override
    public <V> Future<V> executeAsynchronously(Callable<V> callable) {
        MocaCallableWrapper<V> callableWrapper = createWrapper(
                callable, null);
        return _executor.submit(callableWrapper);
    }
    
    // @see com.redprairie.moca.async.AsynchronousExecutor#executeAsynchronously(java.util.concurrent.Callable, com.redprairie.moca.async.MocaAsynchronousExecutor.AsynchronousExecutorCallback)
    @Override
    public <V> void executeAsynchronously(Callable<V> callable, 
            AsynchronousExecutorCallback<V> callback) {
        MocaCallableWrapper<V> callableWrapper = createWrapper(
                callable, callback);
        _executor.submit(callableWrapper);
    }
    
    // @see com.redprairie.moca.AsynchronousExecutor#executeGroupAsynchronously(java.util.concurrent.Callable<V>[])
    @Override
    public <V> CompletionService<V> executeGroupAsynchronously(
            Callable<V>... callables) {
        CompletionService<V> completion = new ExecutorCompletionService<V>(
                _executor) {
            // @see java.util.concurrent.ExecutorCompletionService#submit(java.util.concurrent.Callable)
            @Override
            public Future<V> submit(Callable<V> task) {
                return super.submit(createWrapper(
                    task, null));
            }

            // @see java.util.concurrent.ExecutorCompletionService#submit(java.lang.Runnable, java.lang.Object)
            @Override
            public Future<V> submit(Runnable task, V result) {
                return super.submit(createWrapper(Executors.callable(task, 
                    result), null));
            }
            
        };
        for (Callable<V> callable : callables) {
            completion.submit(callable);
        }
        return completion;
    }
    
    private <V>MocaCallableWrapper<V> createWrapper(Callable<V> callable, 
        AsynchronousExecutorCallback<V> callback) {
        ServerContext context = ServerUtils.getCurrentContextNullable();
        
        Map<String, String> env = new HashMap<String, String>();
        if (context != null) {
            SessionContext session = context.getSession();
            env.putAll(session.getAllVariables());
            
            RequestContext request = context.getRequest();
            env.putAll(request.getAllVariables());
        }
        
        return new MocaCallableWrapper<V>(callable, callback, env);
    }
    
    /**
     * Close down the executor.  Any further attempts to
     * execute anything will throw a {@link RejectedExecutionException}.
     * Threads associated with this executor will also be closed down as
     * they finish tasks.  If clean is not true these threads will also
     * be interrupted in an attempt to close them earlier.
     * @param clean Whether or not to cleanly close down.  If not clean
     *        then any threads will be interrupted.
     */
    public void close(boolean clean) {
        if (clean) {
            _executor.shutdown();
        }
        else {
            _executor.shutdownNow();
        }
    }
    
    public class MocaCallableWrapper<V> implements Callable<V> {
        
        MocaCallableWrapper(Callable<V> callable, 
            AsynchronousExecutorCallback<V> callback, Map<String, String> env) {
            _callable = callable;
            _callback = callback;
            _env = env;
        }
 
        // @see java.util.concurrent.Callable#call()
        @Override
        public V call() throws Exception {
            // Create a new request and session object
            RequestContext req = new RequestContext(_env);
            String sessionId = "async-" + Integer.toHexString(
                    System.identityHashCode(req));
            SessionContext session = new LocalSessionContext(sessionId, 
                SessionType.ASYNC);
            
            // Authenticate our session
            session.setSessionToken(new SessionToken(sessionId));

            // Go ahead and create a server context
            _ctx = _factory.newContext(req, session);
            
            // Set the session ID in the environment
            _ctx.putSystemVariable("ASYNC_SESSION_ID", sessionId);
            _sessionId = sessionId;
            
            // Set up the new context on this thread.  We have to deassociate
            // this in the done call on the future.
            ServerUtils.setCurrentContext(_ctx);
            
            _tasks.put(Thread.currentThread().getId(), this);
            
            V value;
            
            setStatus("Active");
            
            try {
                value = _callable.call();
                _ctx.commit();
            }
            catch (Exception e) {
                _ctx.rollback();
                throw e;
            }
            finally {
                setStatus("Idle");
            }
            
            
            return value;
        }
        
        public Callable<V> getCallable() {
            return _callable;
        }
        
        public void setStatus(String status) {
            _status.setStatus(status);
        }
        
        public Status getStatus() {
            return _status;
        }
        
        public String getSessionId() {
            return _sessionId;
        }
        
        // @see java.lang.Object#toString()
        @Override
        public String toString() {
            return "Async Callable [" + _callable + "]" + (_callback != null ? 
                    " with Callback [" + _callback + "]" : "");
        }
        
        private ServerContext _ctx;
        
        private final Callable<V> _callable;
        private final AsynchronousExecutorCallback<V> _callback;
        private final Map<String, String> _env;
        
        private final Status _status = new Status("Idle");
        private String _sessionId;
    }

    private class AsynchronousExecutorService extends ThreadPoolExecutor {
        /**
         * @param maxPoolSize
         */
        AsynchronousExecutorService(int maxPoolSize) {
            super(maxPoolSize, maxPoolSize, 60L, TimeUnit.SECONDS, 
                    new LinkedBlockingQueue<Runnable>(), new DaemonThreadFactory(
                    "MocaAsynchronousExecutor", maxPoolSize > 1));
            // We make it so that threads will go away if they are not used
            // within 60 seconds
            //
            // We cannot have a different max, since that is only used if the queue
            // cannot be offered the job.  If we changed it to be a 
            // SynchronousQueue instead it would work, but then the caller 
            // would be blocked until a task was completed, which I doubt we 
            // want.  So we have to let the core threads timeout.
            allowCoreThreadTimeOut(true);
            
            _logger.debug(MocaUtils.concat("Asynchronous Executor started with ", 
                    maxPoolSize, " thread(s)."));
        }

        // @see java.util.concurrent.AbstractExecutorService#newTaskFor(java.util.concurrent.Callable)
        /**
         * This method is required to support the ExecutorCompletionService
         */
        @Override
        protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
            // This cast is alright since we control who calls the executor.
            MocaCallableWrapper<T> wrapper = (MocaCallableWrapper<T>) callable;
            _logger.debug(MocaUtils.concat("Asynchronous Executor submitted ",
                wrapper._callable));
            return new NotifyFutureTask<T>(wrapper);
        }
    }
    
    /**
     * This class is used via extending FutureTask to allow us to intercept
     * when the task is actually done.  We partially extend it's features
     * by implementing the RunnableScheduledFuture class so we can delay
     * execution of the task.
     * 
     * Copyright (c) 2010 Sam Corporation
     * All Rights Reserved
     * 
     * @param <V>
     * @author wburns
     */
    private class NotifyFutureTask<V> extends FutureTask<V> {

        /**
         * @param callable
         */
        NotifyFutureTask(MocaCallableWrapper<V> callable) {
            super(callable);
            _callable = callable;
        }
        
        // @see java.util.concurrent.FutureTask#done()
        @Override
        protected void done() {
            super.done();
            _logger.debug(MocaUtils.concat("Asynchronous Executor ", _callable._callable, 
                    " completed."));
            AsynchronousExecutorCallback<V> callback = _callable._callback;
            
            // After we are done we have to clear out the session with the
            // context
            ServerContext ctx = _callable._ctx;
            
            if (ctx == null) {
                ctx = ServerUtils.getCurrentContext();
            }
            
            try {
                if (callback != null) {
                    _logger.debug(MocaUtils.concat(
                            "Asynchronous Executor calling callback: ", callback));
                    callback.done(_callable._callable, this);
                    ctx.commit();
                    _logger.debug(MocaUtils.concat(
                            "Asynchronous Executor callback: ", callback, 
                            " completed successfully."));
                }
            }
            catch (Exception e) {
                // We don't have to rollback since in the finally it will
                // roll the transaction back when it is closed if it is still
                // open.
                _logger.debug(MocaUtils.concat(
                        "Asynchronous Executor callback: ", callback, 
                        " errored."), e);
            }
            finally {
                _tasks.remove(Thread.currentThread().getId());
                ctx.close();
                ServerUtils.setCurrentContext(null);
            }
        }
        
        // @see java.lang.Object#toString()
        @Override
        public String toString() {
            return _callable.toString();
        }

        private final MocaCallableWrapper<V> _callable;
    }
    
    public long getCompletedCount() {
        return _executor.getCompletedTaskCount();
    }
    
    public int getCurrentThreadCount() {
        return _executor.getPoolSize();
    }
    
    public int getMaxThreadCount() {
        return _executor.getMaximumPoolSize();
    }
    
    public int getActiveThreadCount() {
        return _executor.getActiveCount();
    }
    
    /**
     * Great care should be taken to not directly modify this queue
     * @return
     */
    public Collection<Runnable> getQueue() {
        return _executor.getQueue();
    }
    
    /**
     * This returns an unmodifiable map containing all of the running tasks
     * but will update as the tasks are updated.
     * @return
     */
    public Map<Long, Callable<?>> getRunningTasks() {
        return Collections.unmodifiableMap(_tasks);
    }
    
    private final ServerContextFactory _factory;
    private final AsynchronousExecutorService _executor;
    private final ConcurrentMap<Long, Callable<?>> _tasks = 
        new ConcurrentHashMap<Long, Callable<?>>();
    
    private static final Logger _logger = LogManager.getLogger(MocaAsynchronousExecutor.class);
}
