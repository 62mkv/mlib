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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.redprairie.moca.AsynchronousExecutor.AsynchronousExecutorCallback;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.advice.ServerContextConfig;
import com.redprairie.moca.advice.SessionAdministration;
import com.redprairie.moca.advice.SessionAdministrationManager;
import com.redprairie.moca.advice.SessionAdministrationManagerBean;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.exec.RequestContext;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SessionContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This class tests the various methods of the MocaAsynchronousExecutor
 * 
 * Any tests that rely on methods being invoked from the done method must have
 * a timeout, since a future object can return the value before a done method
 * is invoked.  This is why the class does not allow for both to be done from
 * the same method.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_MocaAsynchronousExecutor {
    
    @BeforeClass
    public static void beforeClass() throws SystemConfigurationException {
        ServerUtils.setupDaemonContext(
                TU_MocaAsynchronousExecutor.class.getName(), true);
    }
    
    @After
    public void afterEachTest() {
        if (_executor != null) {
            _executor.close(true);
            _executor = null;
        }
        
        // Since we set the context we want to remove it at end of each test
        ServerUtils.removeCurrentContext();
    }

    /**
     * Test method for {@link com.redprairie.moca.async.MocaAsynchronousExecutor#executeAsynchronously(java.util.concurrent.Callable)}.
     * @throws ExecutionException 
     * @throws InterruptedException 
     * @throws TimeoutException 
     * @throws MocaException 
     */
    @Test
    public void testExecuteAsynchronouslyCallableOfV() 
            throws InterruptedException, ExecutionException, TimeoutException, 
            MocaException {
        ServerContext mockContext = Mockito.mock(ServerContext.class);
        
        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class);
        Mockito.when(factory.newContext(Mockito.any(RequestContext.class), 
                Mockito.any(SessionContext.class))).thenReturn(mockContext);
        
        _executor = new MocaAsynchronousExecutor(factory, 1);
        
        Callable<ServerContext> callable = new Callable<ServerContext>() {
            @Override
            public ServerContext call() throws Exception {
                return ServerUtils.getCurrentContext();
            }
        };
        
        Future<ServerContext> future = _executor.executeAsynchronously(callable);
        
        ServerContext ctx = future.get(100, TimeUnit.MILLISECONDS);
        
        assertEquals("The context used isn't as expected.", mockContext, ctx);
        
        Mockito.verify(mockContext).commit();
        Mockito.verify(mockContext, Mockito.timeout(50)).close();
    }
    
    /**
     * Test method for {@link com.redprairie.moca.async.MocaAsynchronousExecutor#executeAsynchronously(java.util.concurrent.Callable)}.
     * @throws ExecutionException 
     * @throws InterruptedException 
     * @throws TimeoutException 
     * @throws MocaException 
     */
    @Test
    public void testExecuteAsynchronouslyCallableOfVThrowsException() 
            throws InterruptedException, ExecutionException, TimeoutException, 
            MocaException {
        ServerContext mockContext = Mockito.mock(ServerContext.class);
        
        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class);
        Mockito.when(factory.newContext(Mockito.any(RequestContext.class), 
                Mockito.any(SessionContext.class))).thenReturn(mockContext);
        
        final Exception mockException = Mockito.mock(Exception.class);
        
        _executor = new MocaAsynchronousExecutor(factory, 1);
        
        Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                throw mockException;
            }
        };
        
        Future<Void> future = _executor.executeAsynchronously(callable);
        
        try {
            future.get(100, TimeUnit.MILLISECONDS);
            fail("We should have thrown an exception from the callable");
        }
        catch (ExecutionException e) {
            assertEquals("The context used isn't as expected.", mockException, 
                    e.getCause());            
        }
        
        Mockito.verify(mockContext).rollback();
        // We should have never called commit
        Mockito.verify(mockContext, Mockito.never()).commit();
        Mockito.verify(mockContext, Mockito.timeout(50)).close();
    }

    /**
     * Test method for {@link com.redprairie.moca.async.MocaAsynchronousExecutor#executeAsynchronously(java.util.concurrent.Callable, com.redprairie.moca.AsynchronousExecutor.AsynchronousExecutorCallback)}.
     * @throws InterruptedException 
     * @throws TimeoutException 
     * @throws MocaException 
     */
    @Test
    public void testExecuteAsynchronouslyCallableOfVAsynchronousExecutorCallbackOfV() 
            throws InterruptedException, TimeoutException, MocaException {
        ServerContext mockContext = Mockito.mock(ServerContext.class);
        
        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class);
        Mockito.when(factory.newContext(Mockito.any(RequestContext.class), 
                Mockito.any(SessionContext.class))).thenReturn(mockContext);
        
        _executor = new MocaAsynchronousExecutor(factory, 1);
        
        Callable<ServerContext> callable = new Callable<ServerContext>() {
            @Override
            public ServerContext call() throws Exception {
                return ServerUtils.getCurrentContext();
            }
        };
        
        final Exchanger<ServerContext> exchanger = new Exchanger<ServerContext>();
        
        AsynchronousExecutorCallback<ServerContext> callback = new AsynchronousExecutorCallback<ServerContext>() {
            @Override
            public void done(Callable<ServerContext> callable,
                    Future<ServerContext> future) {
                try {
                    exchanger.exchange(future.get());
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };
        
        _executor.executeAsynchronously(callable, callback);
        
        ServerContext ctx = exchanger.exchange(null, 100, TimeUnit.MILLISECONDS);
        
        assertEquals("The context used isn't as expected.", mockContext, ctx);
        
        // This should be twice: 1 for the task and 1 for the callback
        Mockito.verify(mockContext, Mockito.timeout(50).times(2)).commit();
        Mockito.verify(mockContext, Mockito.timeout(50)).close();
    }

    /**
     * Test method for {@link com.redprairie.moca.async.MocaAsynchronousExecutor#executeGroupAsynchronously(java.util.concurrent.Callable<V>[])}.
     * @throws InterruptedException 
     * @throws ExecutionException 
     * @throws MocaException 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testExecuteGroupAsynchronously() throws InterruptedException, 
            ExecutionException, MocaException {
        ServerContext mockContext = Mockito.mock(ServerContext.class);
        
        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class);
        Mockito.when(factory.newContext(Mockito.any(RequestContext.class), 
                Mockito.any(SessionContext.class))).thenReturn(mockContext);
        
        _executor = new MocaAsynchronousExecutor(factory, 5);
        
        CompletionService<Long> completion = _executor.executeGroupAsynchronously(
                new ExecuteDelayAndReturn(500L), new ExecuteDelayAndReturn(50L), 
                new ExecuteDelayAndReturn(250L));
        
        // They should come back in order of timeouts
        // The first was a wait of 50 milliseconds
        Future<Long> fut = completion.poll(500L, TimeUnit.MILLISECONDS);
        assertEquals(Long.valueOf(50L), fut.get());
        
        // The second ones timeout was 250 milliseconds
        fut = completion.poll(500L, TimeUnit.MILLISECONDS);
        assertEquals(Long.valueOf(250L), fut.get());
        
        // The last was a wait of 500 milliseconds
        fut = completion.poll(500L, TimeUnit.MILLISECONDS);
        assertEquals(Long.valueOf(500L), fut.get());
        
        Mockito.verify(factory, Mockito.times(3)).newContext(
                Mockito.any(RequestContext.class), Mockito.any(SessionContext.class));
        
        Mockito.verify(mockContext, Mockito.times(3)).commit();
        Mockito.verify(mockContext, Mockito.timeout(50).times(3)).close();
    }
    
    @Test
    public void testSessionUnregisterAtEnd() throws InterruptedException, 
            ExecutionException, TimeoutException, BrokenBarrierException {

        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class);
        Mockito.when(factory.newContext(Mockito.any(RequestContext.class), 
                Mockito.any(SessionContext.class))).thenAnswer(new Answer<ServerContext>() {
                    @Override
                    public ServerContext answer(InvocationOnMock invocation)
                            throws Throwable {
                        Object[] args = invocation.getArguments();
                        return ServerContextConfig.serverContext(null, null, 
                            (SessionContext)args[1], ServerUtils.globalContext(), 
                            (RequestContext)args[0], null, null, null, 
                            null, null, null);
                    }
                    
                });
        
        _executor = new MocaAsynchronousExecutor(factory, 5);
        
        SessionAdministrationManager manager = 
            (SessionAdministrationManager)ServerUtils.globalContext().getAttribute(
                SessionAdministrationManagerBean.class.getName());
        
        SessionAdministration[] sessionsBefore = manager.getSessions();
        
        CyclicBarrier barrier = new CyclicBarrier(2);
        
        Future<Void> future = _executor.executeAsynchronously(
            new ExecuteLatch(barrier));
        
        // Wait for thread to start up
        barrier.await(100L, TimeUnit.MILLISECONDS);
        
        // There should be a new session while running
        List<SessionAdministration> newSessions = getNewSessions(sessionsBefore, manager);
        assertEquals("Unexpected number of new sessions", 1, newSessions.size());

        // Let it finish now
        barrier.await(100L, TimeUnit.MILLISECONDS);
        
        future.get(100, TimeUnit.MILLISECONDS);
        
        // We sleep to let the done callback fire which should close our session
        Thread.sleep(100);
        
        // No new sessions should be left over
        newSessions = getNewSessions(sessionsBefore, manager);
        assertEquals("Failure cleaning up new sessions", 0, newSessions.size());
        
        // Does not always work
        // SessionAdministration[] sessionsAfter = manager.getSessions();
        //assertEquals(sessionsBefore.length, sessionsAfter.length);
    }
    
    private List<SessionAdministration> getNewSessions(SessionAdministration[] originalSessions, 
        SessionAdministrationManager manager){

        List<SessionAdministration> result = new ArrayList<SessionAdministration>();

        SessionAdministration[] sessions = manager.getSessions();

        if(sessions != null && sessions.length > 0){

            // Create a list of current sessions and then remove all original sessions from the original list
            Collections.addAll(result, sessions);

            if(originalSessions != null && originalSessions.length > 0){
                for(SessionAdministration session: originalSessions){
                    result.remove(session);
                }
            }
        } 

        return result;
    }
    
    
    @Test
    public void testRequestVariablesPropagated() throws InterruptedException, 
            ExecutionException, TimeoutException, MocaException {
        final String key = "TEST_USR_ID_";
        final String user = "fooman";
        Map<String, String> variables = Collections.singletonMap(key, user);
        
        RequestContext requestContext = Mockito.mock(RequestContext.class);
        Mockito.when(requestContext.getAllVariables()).thenReturn(variables);
        
        ServerContext mockContext = Mockito.mock(ServerContext.class, Mockito.RETURNS_MOCKS);
        Mockito.when(mockContext.getRequest()).thenReturn(requestContext);
        
        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class);
        Mockito.when(factory.newContext(Mockito.any(RequestContext.class), 
                Mockito.any(SessionContext.class))).thenAnswer(new Answer<ServerContext>() {

                    @Override
                    public ServerContext answer(InvocationOnMock invocation)
                            throws Throwable {
                        final RequestContext request = 
                            (RequestContext)invocation.getArguments()[0];
                        ServerContext serverContext = Mockito.mock(
                            ServerContext.class);
                        Mockito.when(serverContext.getSystemVariable(
                            Mockito.anyString())).thenAnswer(new Answer<String>() {

                                @Override
                                public String answer(InvocationOnMock invocation)
                                        throws Throwable {
                                    return request.getVariable(
                                        (String)invocation.getArguments()[0]);
                                }
                                
                        });
                        return serverContext;
                    }
                });
        
        ServerUtils.setCurrentContext(mockContext);
        
        _executor = new MocaAsynchronousExecutor(factory, 1);
        
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                ServerContext serverContext = ServerUtils.getCurrentContext();
                return serverContext.getSystemVariable(key);
            }
        };
        
        Future<String> future = _executor.executeAsynchronously(callable);
        
        assertEquals(user, future.get(100, TimeUnit.MILLISECONDS));
    }
    
    /**
     * @throws TimeoutException 
     * @throws InterruptedException 
     * @throws ExecutionException 
     * @throws MocaException 
     */
    @Test
    public void testInterruptExecution() throws InterruptedException, TimeoutException, ExecutionException, MocaException {
        ServerContext mockContext = Mockito.mock(ServerContext.class);
        
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                // This behavior emulates commit failing when interrupted
                if (Thread.interrupted()) {
                    throw new MocaInterruptedException();
                }
                return null;
            }
            
        }).when(mockContext).commit();
        
        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class);
        Mockito.when(factory.newContext(Mockito.any(RequestContext.class), 
                Mockito.any(SessionContext.class))).thenReturn(mockContext);
        
        _executor = new MocaAsynchronousExecutor(factory, 1);
        
        final Exchanger<Thread> exchanger = new Exchanger<Thread>();
        
        Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() throws InterruptedException {
                exchanger.exchange(Thread.currentThread());
                try {
                    Thread.sleep(Long.MAX_VALUE);
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return null;
            }
        };
        
        InterruptTestCallback callback = new InterruptTestCallback();
        
        _executor.executeAsynchronously(callable, callback);
        
        Thread runningThread = exchanger.exchange(null, 100L, 
            TimeUnit.MILLISECONDS);
        
        runningThread.interrupt();
        
        Thread.sleep(100L);
        
        assertFalse(callback._interrupted.get());
        Object returned = callback._returned.get();
        if (returned == null) {
            fail("Expected MocaInterruptedException wrapped by ExecutionException");
        }
        
        assertTrue(returned instanceof ExecutionException);
        
        ExecutionException excp = (ExecutionException)returned;
        Throwable cause = excp.getCause();
        assertTrue(cause instanceof MocaInterruptedException);
    }
    
    private static class InterruptTestCallback implements 
        AsynchronousExecutorCallback<Void> { 

        @Override
        public void done(Callable<Void> callable, Future<Void> future) {
            boolean interrupted = Thread.interrupted();
            _interrupted.set(interrupted);
            try {
                _returned.set(future.get());
            }
            catch (Exception e) {
                _returned.set(e);
            }
        }
        
        private final AtomicReference<Object> _returned = new AtomicReference<Object>();
        private final AtomicBoolean _interrupted = new AtomicBoolean(false);
    }
    
    private static class ExecuteDelayAndReturn implements Callable<Long> {
        
        public ExecuteDelayAndReturn(Long integer) {
            _long = integer;
        }
        

        // @see java.util.concurrent.Callable#call()
        @Override
        public Long call() throws Exception {
            Thread.sleep(_long);
            return _long;
        }
        
        private final Long _long;
    }
    
    private static class ExecuteLatch implements Callable<Void> {
        
        public ExecuteLatch(CyclicBarrier barrier) {
            _barrier = barrier;
        }
        

        // @see java.util.concurrent.Callable#call()
        @Override
        public Void call() throws Exception {
            _barrier.await();
            _barrier.await();
            return null;
        }
        
        private final CyclicBarrier _barrier;
    }

    private MocaAsynchronousExecutor _executor;
}
