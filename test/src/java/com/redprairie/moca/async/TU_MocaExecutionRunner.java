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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.jgroups.Event;
import org.jgroups.JChannel;
import org.jgroups.blocks.executor.ExecutorEvent;
import org.jgroups.protocols.Executing;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;

import static org.junit.Assert.assertEquals;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_MocaExecutionRunner {
    @BeforeClass
    public static void beforeClass() throws SystemConfigurationException {
        ServerUtils.setupDaemonContext(TU_MocaExecutionRunner.class.getName(), true);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testNormalSubmission() throws Throwable {
        final AtomicReference<Thread> outerThread = new AtomicReference<Thread>();
        final Runnable runnable = Mockito.mock(Runnable.class);
        
        Mockito.doAnswer(new Answer<Void> () {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                // Our runnable is just a noop
                // NOTE: Original logic was interrupting the outer thread here to exit the test.
                // This caused occasional test failures with number of executions exceeding the 
                // the expected value (864 != 2).  This was happening because interrupted outer thread 
                // is not guaranteed to immediately wake up from join() and start executing, since 
                // CPU might be under heavy load and outer thread will need to wait for its turn to run.
                return null;
            }
        }).when(runnable).run();
        
        Answer<Runnable> runnableAnswer = new Answer<Runnable>() {
            private int count = 0;
            @Override
            public Runnable answer(InvocationOnMock invocation)
                    throws Throwable {
                // The 1st time we are returning valid runnable
                // The 2nd time we are returning null which will force MocaExecutionRunner 
                // internal thread to exit the processing loop
                return count++ == 0 ? runnable : null;
            }
        };
        Answer<Void> taskCompleteAnswer = Mockito.mock(Answer.class);
        
        JChannel channel = mockJChannel(runnableAnswer, taskCompleteAnswer);
        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class, 
            Mockito.RETURNS_MOCKS);
        MocaExecutionRunner runner = new MocaExecutionRunner(channel, factory);
        
        ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                outerThread.set(t);
                return t;
            }
        });
        
        try {
            Future<?> future = executor.submit(runner);
            
            future.get(2, TimeUnit.SECONDS);
            
            // Hack need to wait for down to be called
            Mockito.verify(channel, Mockito.timeout(250).times(3)).down(Mockito.any(Event.class));
            Mockito.verify(taskCompleteAnswer).answer(Mockito.any(InvocationOnMock.class));
            Mockito.verify(runnable).run();
        }
        finally {
            executor.shutdownNow();
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testInterruptWhileWaitingForTask() throws Throwable {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Thread> outerThread = new AtomicReference<Thread>();
        Answer<Runnable> runnableAnswer = new Answer<Runnable>() {
            @Override
            public Runnable answer(InvocationOnMock invocation)
                    throws Throwable {
                latch.countDown();
                // We basically sleep forever, but it is interruptible
                Thread.sleep(Long.MAX_VALUE);
                return null;
            }
        };
        Answer<Void> taskCompleteAnswer = Mockito.mock(Answer.class);
        
        JChannel channel = mockJChannel(runnableAnswer, taskCompleteAnswer);
        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class, 
            Mockito.RETURNS_MOCKS);
        MocaExecutionRunner runner = new MocaExecutionRunner(channel, factory);
        
        ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                outerThread.set(t);
                return t;
            }
        });
        
        try {
            Future<?> future = executor.submit(runner);
            
            // Wait until we know it is running
            latch.await();
            
            outerThread.get().interrupt();
            
            // Now this should end because we interrupted
            future.get(2, TimeUnit.SECONDS);
            
            // The only interaction should be the wait
            Mockito.verify(channel).down(Mockito.any(Event.class));
            Mockito.verifyNoMoreInteractions(taskCompleteAnswer);
        }
        catch (TimeoutException e) {
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            ThreadInfo[] threadInfos = threadBean.dumpAllThreads(true, true);
            System.out.println(Arrays.toString(threadInfos));
            
            throw e;
        }
        finally {
            executor.shutdownNow();
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testInterruptWhileTaskRunning() throws Throwable {
        final CyclicBarrier barrier = new CyclicBarrier(2);
        final AtomicReference<Thread> outerThread = new AtomicReference<Thread>();
        final AtomicReference<Thread> innerThread = new AtomicReference<Thread>();
        final Runnable runnable = Mockito.mock(Runnable.class);
        
        Mockito.doAnswer(new Answer<Void> () {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                innerThread.set(Thread.currentThread());
                // Let outer know that we are running
                barrier.await();
                // Wait for outer to release us now
                barrier.await();
                return null;
            }
        }).when(runnable).run();
        
        Answer<Runnable> runnableAnswer = new Answer<Runnable>() {
            @Override
            public Runnable answer(InvocationOnMock invocation)
                    throws Throwable {
                return runnable;
            }
        };
        Answer<Void> taskCompleteAnswer = Mockito.mock(Answer.class);
        
        JChannel channel = mockJChannel(runnableAnswer, taskCompleteAnswer);
        ServerContextFactory factory = Mockito.mock(ServerContextFactory.class, 
            Mockito.RETURNS_MOCKS);
        MocaExecutionRunner runner = new MocaExecutionRunner(channel, factory);
        
        ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                outerThread.set(t);
                return t;
            }
        });
        
        try {
            Future<?> future = executor.submit(runner);
            
            // First wait for task to start
            barrier.await(1, TimeUnit.SECONDS);
            
            // Since the task has started one thread should be active
            assertEquals(1, runner.getActiveThreadCount());
            
            // Now we interrupt them
            outerThread.get().interrupt();
            
            // It should now be stopped - however the task is still running
            // until we release it
            future.get(2, TimeUnit.SECONDS);
            
            // Now release the task running, which should now stop it.  If
            // task isn't running we will get a timeout exception
            barrier.await(100, TimeUnit.MILLISECONDS);
            
            innerThread.get().join(TimeUnit.SECONDS.toMillis(2));
            
            Mockito.verify(channel, Mockito.times(2)).down(Mockito.any(Event.class));
            Mockito.verify(taskCompleteAnswer).answer(Mockito.any(InvocationOnMock.class));
            Mockito.verify(runnable).run();
        }
        finally {
            executor.shutdownNow();
        }
    }
    
    private JChannel mockJChannel(final Answer<Runnable> consumerReadyAnswer, 
        final Answer<Void> taskCompleteAnswer) {
        JChannel channel = Mockito.mock(JChannel.class, Mockito.RETURNS_DEEP_STUBS);
        
        Executing exec = Mockito.mock(Executing.class);
        Mockito.when(channel.getProtocolStack().findProtocol(
            Mockito.eq(Executing.class))).thenReturn(exec);
        
        Mockito.when(channel.down(Mockito.any(ExecutorEvent.class))).thenAnswer(
            new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocation)
                        throws Throwable {
                    ExecutorEvent event = (ExecutorEvent)invocation.getArguments()[0];
                    switch (event.getType()) {
                    case ExecutorEvent.CONSUMER_READY:
                        return consumerReadyAnswer.answer(invocation);
                    case ExecutorEvent.TASK_COMPLETE:
                        return taskCompleteAnswer.answer(invocation);
                    default:
                        throw new IllegalArgumentException("Event type " + 
                                event.getType() + " not expected!");
                    }
                }
            });
        return channel;
    }
}
