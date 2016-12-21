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

package com.sam.moca;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.sam.moca.util.MocaUtils;

/**
 * This interface describes the methods that can be used in a MOCA system to
 * execute the given {@link Callable} in another thread as a background task.
 * This task will have its own dedicated session.  If this task spawns a thread
 * these spawned threads will share this session however.
 * <p>
 * If the callable exits by returning a value, then the transaction will be
 * committed afterwards.  If any exception is thrown then the transaction
 * will be rolled back.
 * <p>
 * This class will provide 3 different ways of retrieving results of these
 * background tasks.  There is a push or pull type of mechanism.  
 * <ul>
 * <li>
 * The push mechanism requires the calling code to implement the 
 * {@linkplain AsynchronousExecutor.AsynchronousExecutorCallback AsynchronousExecutorCallback} 
 * interface and pass an instance to
 * the methods that take them.  This callback would then be called when the
 * task is done passing the <code>Callable</code> that was provided as well as the
 * {@link Future} object that will immediately return any values that were
 * queried.  The <code>Future</code> object will not be able to be cancelled and
 * all calls to {@link Future#isDone()} will return true and any attempts at
 * {@linkplain Future#get() retrieving a value} will return immediately.
 * <b>NOTE</b>: the callback will take place in a separate transaction, with
 * the same <code>MocaContext</code>, native process and thread that it was
 * executed on. Similar to the task if the callback returns normally then
 * the transaction will be committed.  However if an exception is thrown it
 * will roll the transaction back.
 * <li>
 * The pull mechanism requires calling a method that immediately returns a 
 * <code>Future</code> object.  This object allows for requesting if the task
 * has finished and provides concurrency affects to wait for a duration or
 * completion.  Since the <code>Future</code> object is returned before completion
 * the caller can attempt to cancel the computation if desired through the
 * use of the {@link Future#cancel(boolean)} method.
 * <li>
 * The latter is a pull mechanism provided that provides a layer of abstraction
 * between submission and consumption of the results by using the 
 * {@link CompletionService}.  This allows for as many tasks to be submitted at 
 * once and then any number of consumers can just poll until a task
 * is finished.  The order of which they are returned are the order in which
 * they are completed not submitted.
 * </ul>
 * <p>
 * There are also methods provided to optionally run this tasks after a set
 * duration.  These tasks will not use available threads until after the duration
 * has expired.
 * <p>
 * More information pertaining to how values or exceptions are returned should
 * be read on the {@link Future} page.
 * <p>
 * A <code>Callable</code> can be obtained from a {@link Runnable} instance as well
 * by using methods such as {@link Executors#callable(Runnable, Object)} method
 * to convert.  Please see the {@link Executors} class for other methods 
 * available as well.
 * <p>
 * A <code>Callable</code> can be obtained for a given MOCA command and it's
 * arguments by using the 
 * {@link MocaUtils#mocaCommandCallable(String, MocaArgument...)} method.  This
 * callable will allow for it to be passed directly to this method and will
 * invoke the method and the Future object will be typed to return the 
 * <code>MocaResults</code> that was returned from the execution.
 * <p>
 * Copyright (c) 2010 Sam Corporation
 * <p>
 * All Rights Reserved
 * 
 * @author wburns
 */
public interface AsynchronousExecutor {

    /**
     * Submits the given callable to be executed asynchronously
     * when possible.  This will use the pull method of retrieving results and
     * is thus possible to cancel.
     * @param <V> The return type
     * @param callable The callable object to execute in another thread.
     * @return The future object associated with this callable that will in
     *         the future contain the result or exception encountered.
     */
    public <V> Future<V> executeAsynchronously(Callable<V> callable);

    /**
     * Submits the given callable to be executed asynchronously
     * when possible.  This will use the push method of retrieving results and
     * thus the calling code doesn't need to do anything else special after
     * calling this method.  The callback will be invoked upon completion of
     * the task.
     * @param <V> The return type
     * @param callable The callable object to execute in another thread.
     * @param callback The callback to call when the execution is completed.
     */
    public <V> void executeAsynchronously(Callable<V> callable,
            AsynchronousExecutorCallback<V> callback);
    
    /**
     * Submits the given callables to be executed asynchronously
     * when possible.  These task's completions can then be pulled from the
     * <code>CompletionService</code> as they are completed.  This way you
     * can submit as many tasks as desired at once and then a consumer(s) can then
     * poll or block until a task is finished.  Tasks <i>can</i> be completed in
     * a different order than provided.
     * <p>
     * <b>NOTE<b>: The warning related to varag generics can be safely ignored
     * @param <V> The return type
     * @param callables The callables to execute in another thread
     * @return A completion service that can be polled to find when a task
     *         has completed.  The order of future objects returned can be in
     *         any order, determined by their asynchronous completion.
     */
    public <V> CompletionService<V> executeGroupAsynchronously(
            Callable<V>... callables);
    
    /**
     * This interface is provided as a callback to support push type of 
     * completion notifications for a background task.
     * 
     * Copyright (c) 2010 Sam Corporation
     * All Rights Reserved
     * 
     * @param <V> The return type
     * @author wburns
     */
    public interface AsynchronousExecutorCallback<V> {
        /**
         * This method will be invoked on the implementor when the given
         * task for the callable is finished.  This could be due to an exception
         * or success.  If this method returns normally any transactional
         * work will be committed.
         * @param callable The callable that was submitted as a task that has
         *        now finished.  This is provided to help callers tell which
         *        task this is related to if needed.
         * @param future The future object containing the status of the task
         *        completion.
         * @throws Exception If a problem occurs in the callback, forcing any
         *         transactional work to be rolled back.
         */
        public void done(Callable<V> callable, Future<V> future) throws Exception;
    }
}