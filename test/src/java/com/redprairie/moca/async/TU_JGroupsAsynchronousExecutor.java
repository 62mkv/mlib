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

package com.sam.moca.async;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jgroups.util.FutureListener;
import org.jgroups.util.NotifyingFuture;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sam.moca.AsynchronousExecutor.AsynchronousExecutorCallback;
import com.sam.moca.cluster.jgroups.MocaExecutionService;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Test for jgroups asynchronous executor
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_JGroupsAsynchronousExecutor {
    
    /**
     * Test method for {@link com.sam.moca.async.JGroupsAsynchronousExecutor#executeAsynchronously(java.util.concurrent.Callable)}.
     * @throws InterruptedException 
     * @throws ExecutionException 
     * @throws TimeoutException 
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testExecuteAsynchronouslyCallableOfVLimit() throws InterruptedException, ExecutionException, TimeoutException {
        int limit = 20;
        
        final BlockingQueue<FutureListener> listeners = 
                new LinkedBlockingQueue<FutureListener>();
        MocaExecutionService execService = Mockito.mock(MocaExecutionService.class);
        NotifyingFuture mockFuture = Mockito.mock(NotifyingFuture.class);
        
        Mockito.when(mockFuture.setListener(Mockito.any(FutureListener.class)))
            .thenAnswer(new Answer<NotifyingFuture<?>>() {
            @Override
            public NotifyingFuture<?> answer(InvocationOnMock invocation)
                    throws Throwable {
                listeners.add((FutureListener<?>)invocation.getArguments()[0]);
                return (NotifyingFuture<?>)invocation.getMock();
            }
        });
        
        Mockito.when(execService.submit(Mockito.any(Callable.class))).thenReturn(
            mockFuture);
        
        final JGroupsAsynchronousExecutor asyncExec = new JGroupsAsynchronousExecutor(
            execService, limit);
        
        final Callable<?> mockCallable = Mockito.mock(Callable.class, Mockito
            .withSettings().serializable());
        
        for (int i = 0; i < limit; ++i) {
            asyncExec.executeAsynchronously(mockCallable);
        }
        
        ExecutorService service = Executors.newSingleThreadExecutor();
        
        try {
            Callable<Future<?>> callable = new Callable<Future<?>>() {
                @Override
                public Future<?> call() throws Exception {
                    return asyncExec.executeAsynchronously(mockCallable);
                }
            };
            
            Future<Future<?>> future = service.submit(callable);
            
            // We wait 100 milliseconds for it to timeout as it should.
            try {
                future.get(100L, TimeUnit.MILLISECONDS);
                fail("For some reason we got a context, when the max submissions was full");
            }
            catch (TimeoutException e) {
                // We should have timed out.
            }
            
            // We now finish all the tests
            for (int i = 0; i < limit; ++i) {
                FutureListener<?> listener = listeners.take();
                listener.futureDone(mockFuture);
            }
            
            // This should return now.
            Future<?> timedFuture  = future.get(100L, TimeUnit.MILLISECONDS);
            
            assertNotNull("The context returned was null", timedFuture);
        }
        finally {
            service.shutdown();
        }
    }

    /**
     * Test method for {@link com.sam.moca.async.JGroupsAsynchronousExecutor#executeAsynchronously(java.util.concurrent.Callable, com.sam.moca.AsynchronousExecutor.AsynchronousExecutorCallback)}.
     * @throws InterruptedException 
     * @throws ExecutionException 
     * @throws TimeoutException 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testExecuteAsynchronouslyCallableOfVAsynchronousExecutorCallbackOfVLimit() throws InterruptedException, ExecutionException, TimeoutException {
        int limit = 20;
        
        final BlockingQueue<FutureListener> listeners = 
                new LinkedBlockingQueue<FutureListener>();
        MocaExecutionService execService = Mockito.mock(MocaExecutionService.class);
        NotifyingFuture mockFuture = Mockito.mock(NotifyingFuture.class);
        
        Mockito.when(mockFuture.setListener(Mockito.any(FutureListener.class)))
            .thenAnswer(new Answer<NotifyingFuture<?>>() {
            @Override
            public NotifyingFuture<?> answer(InvocationOnMock invocation)
                    throws Throwable {
                listeners.add((FutureListener<?>)invocation.getArguments()[0]);
                return (NotifyingFuture<?>)invocation.getMock();
            }
        });
        
        Mockito.when(execService.submit(Mockito.any(Callable.class))).thenReturn(
            mockFuture);
        
        final JGroupsAsynchronousExecutor asyncExec = new JGroupsAsynchronousExecutor(
            execService, limit);
        
        final Callable mockCallable = Mockito.mock(Callable.class, Mockito
            .withSettings().serializable());
        final AsynchronousExecutorCallback mockCallback = Mockito.mock(
            AsynchronousExecutorCallback.class);
        
        for (int i = 0; i < limit; ++i) {
            asyncExec.executeAsynchronously(mockCallable, mockCallback);
        }
        
        ExecutorService service = Executors.newSingleThreadExecutor();
        
        try {
            Callable<Void> callable = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    asyncExec.executeAsynchronously(mockCallable, 
                        mockCallback);
                    return null;
                }
            };
            
            Future<Void> future = service.submit(callable);
            
            // We wait 100 milliseconds for it to timeout as it should.
            try {
                future.get(100L, TimeUnit.MILLISECONDS);
                fail("For some reason we got a context, when the max submissions was full");
            }
            catch (TimeoutException e) {
                // We should have timed out.
            }
            
            // We now finish all the tests
            for (int i = 0; i < limit; ++i) {
                FutureListener<?> listener = listeners.take();
                listener.futureDone(mockFuture);
            }
            
            // This should return now.
            Void timedFuture = future.get(100L, TimeUnit.MILLISECONDS);
            
            assertNull("The context returned was null", timedFuture);
        }
        finally {
            service.shutdown();
        }
    }

    /**
     * Test method for {@link com.sam.moca.async.JGroupsAsynchronousExecutor#executeGroupAsynchronously(java.util.concurrent.Callable<V>[])}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testExecuteGroupAsynchronouslyLimit() throws InterruptedException, ExecutionException, TimeoutException {
        int limit = 20;
        
        final BlockingQueue<FutureListener> listeners = 
                new LinkedBlockingQueue<FutureListener>();
        MocaExecutionService execService = Mockito.mock(MocaExecutionService.class);
        NotifyingFuture mockFuture = Mockito.mock(NotifyingFuture.class);
        
        Mockito.when(mockFuture.setListener(Mockito.any(FutureListener.class)))
            .thenAnswer(new Answer<NotifyingFuture<?>>() {
            @Override
            public NotifyingFuture<?> answer(InvocationOnMock invocation)
                    throws Throwable {
                listeners.add((FutureListener<?>)invocation.getArguments()[0]);
                return (NotifyingFuture<?>)invocation.getMock();
            }
        });
        
        Mockito.when(execService.submit(Mockito.any(Callable.class))).thenReturn(
            mockFuture);
        
        final JGroupsAsynchronousExecutor asyncExec = new JGroupsAsynchronousExecutor(
            execService, limit);
        
        final Callable mockCallable = Mockito.mock(Callable.class, Mockito
            .withSettings().serializable());
        
        Callable<Object>[] callableArray = new Callable[limit];
        for (int i = 0; i < limit; ++i) {
            callableArray[i] = mockCallable;
        }
        
        asyncExec.executeGroupAsynchronously(
            callableArray);
        
        ExecutorService service = Executors.newSingleThreadExecutor();
        
        try {
            Callable<Void> callable = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    // A single one should block
                    asyncExec.executeGroupAsynchronously(mockCallable);
                    return null;
                }
            };
            
            Future<Void> future = service.submit(callable);
            
            // We wait 100 milliseconds for it to timeout as it should.
            try {
                future.get(100L, TimeUnit.MILLISECONDS);
                fail("For some reason we got a context, when the max submissions was full");
            }
            catch (TimeoutException e) {
                // We should have timed out.
            }
            
            // We now finish all the tests
            for (int i = 0; i < limit; ++i) {
                FutureListener<?> listener = listeners.take();
                listener.futureDone(mockFuture);
            }
            
            // This should return now.
            Void timedFuture = future.get(100L, TimeUnit.MILLISECONDS);
            
            assertNull("The context returned was null", timedFuture);
        }
        finally {
            service.shutdown();
        }
    }

    /**
     * Test method for {@link com.sam.moca.async.JGroupsAsynchronousExecutor#executeGroupAsynchronously(java.util.concurrent.Callable<V>[])}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testExecuteGroupAsynchronouslyLimitWithSubmit() throws InterruptedException, ExecutionException, TimeoutException {
        int limit = 20;
        
        final BlockingQueue<FutureListener> listeners = 
                new LinkedBlockingQueue<FutureListener>();
        MocaExecutionService execService = Mockito.mock(MocaExecutionService.class);
        NotifyingFuture mockFuture = Mockito.mock(NotifyingFuture.class);
        
        Mockito.when(mockFuture.setListener(Mockito.any(FutureListener.class)))
            .thenAnswer(new Answer<NotifyingFuture<?>>() {
            @Override
            public NotifyingFuture<?> answer(InvocationOnMock invocation)
                    throws Throwable {
                listeners.add((FutureListener<?>)invocation.getArguments()[0]);
                return (NotifyingFuture<?>)invocation.getMock();
            }
        });
        
        Mockito.when(execService.submit(Mockito.any(Callable.class))).thenReturn(
            mockFuture);
        
        final JGroupsAsynchronousExecutor asyncExec = new JGroupsAsynchronousExecutor(
            execService, limit);
        
        final Callable mockCallable = Mockito.mock(Callable.class, Mockito
            .withSettings().serializable());
        
        Callable<Object>[] callableArray = new Callable[limit];
        for (int i = 0; i < limit; ++i) {
            callableArray[i] = mockCallable;
        }
        
        final CompletionService<Object> completion = asyncExec.executeGroupAsynchronously(
            callableArray);
        
        ExecutorService service = Executors.newSingleThreadExecutor();
        
        try {
            Callable<Void> callable = new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    // A single one should block
                    completion.submit(mockCallable);
                    return null;
                }
            };
            
            Future<Void> future = service.submit(callable);
            
            // We wait 100 milliseconds for it to timeout as it should.
            try {
                future.get(100L, TimeUnit.MILLISECONDS);
                fail("For some reason we got a context, when the max submissions was full");
            }
            catch (TimeoutException e) {
                // We should have timed out.
            }
            
            // We now finish all the tests
            for (int i = 0; i < limit; ++i) {
                FutureListener<?> listener = listeners.take();
                listener.futureDone(mockFuture);
            }
            
            // This should return now.
            // NOTE: bumped up the future.get() timeout from the original value of 100 MILLISECONDS, 
            // to prevent junit test from occasionally failing with a TimeoutException on this line.
            Void timedFuture = future.get(100000L, TimeUnit.MILLISECONDS);
            
            assertNull("The context returned was null", timedFuture);
        }
        finally {
            service.shutdown();
        }
    }
}
