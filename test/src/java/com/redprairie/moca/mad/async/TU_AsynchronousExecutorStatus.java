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

package com.redprairie.moca.mad.async;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.jgroups.blocks.executor.ExecutionService.DistributedFuture;
import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.AsynchronousExecutor;
import com.redprairie.moca.async.JGroupsAsynchronousExecutor.CallableWrapper;
import com.redprairie.moca.async.MocaAsynchronousExecutor;
import com.redprairie.moca.async.MocaAsynchronousExecutor.MocaCallableWrapper;
import com.redprairie.moca.async.MocaExecutionRunnerController;
import com.redprairie.moca.server.exec.SystemContext;

/**
 * Tests AsynchronousExecutorStatus
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author klucas
 */
public class TU_AsynchronousExecutorStatus {
    
    @Test
    public void testSetStatusNonclustered() {
        SystemContext context = Mockito.mock(SystemContext.class);
        Map<Long, Callable<?>> asyncList = new HashMap<Long, Callable<?>>();
        
        MocaCallableWrapper<?> wrapper = Mockito.mock(MocaCallableWrapper.class);
        MocaCallableWrapper<?> unusedWrapper = Mockito.mock(MocaCallableWrapper.class);
        
        Mockito.when(wrapper.getSessionId()).thenReturn("sessionId");
        
        asyncList.put(1L, wrapper);
        asyncList.put(2L, unusedWrapper);
        
        MocaAsynchronousExecutor executor = Mockito.mock(MocaAsynchronousExecutor.class);
        MocaExecutionRunnerController controller = Mockito.mock(MocaExecutionRunnerController.class);
        
        Mockito.when(executor.getRunningTasks()).thenReturn(asyncList);
        
        Mockito.when(context.getAttribute(AsynchronousExecutor.class.getName())).thenReturn(executor);
        Mockito.when(context.getAttribute(MocaExecutionRunnerController.class.getName())).thenReturn(controller);
        
        AsynchronousExecutorStatus.set(context, "status", "sessionId");
        
        Mockito.verify(wrapper).setStatus("status");
        Mockito.verify(unusedWrapper, Mockito.never()).setStatus("status");
        
        Mockito.verifyZeroInteractions(controller);
    }
    
    @Test
    public void testSetStatusClustered() {
        SystemContext context = Mockito.mock(SystemContext.class);
        
        MocaAsynchronousExecutor executor = Mockito.mock(MocaAsynchronousExecutor.class);
        MocaExecutionRunnerController controller = Mockito.mock(MocaExecutionRunnerController.class);
        
        Mockito.when(context.getAttribute(AsynchronousExecutor.class.getName())).thenReturn(executor);
        Mockito.when(context.getAttribute(MocaExecutionRunnerController.class.getName())).thenReturn(controller);
        
        AsynchronousExecutorStatus.set(context, "status", "dist-sessionId");
        
        Mockito.verify(controller).setStatus("dist-sessionId", "status");
        
        Mockito.verifyZeroInteractions(executor);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testGetStatuses() {
        Callable c1 = Mockito.mock(Callable.class);
        Callable c2 = Mockito.mock(Callable.class);
        
        Mockito.when(c1.toString()).thenReturn("c1");
        Mockito.when(c2.toString()).thenReturn("c2");
        
        Status s1 = Mockito.mock(Status.class);
        Status s2 = Mockito.mock(Status.class);
        
        Thread t1 = Mockito.mock(Thread.class);
        Thread t2 = Mockito.mock(Thread.class);
        
        Mockito.when(t1.getId()).thenReturn(11L);
        Mockito.when(t2.getId()).thenReturn(12L);
        
        
        MocaAsynchronousExecutor executor = Mockito.mock(MocaAsynchronousExecutor.class);
        
        Map<Long, Callable<?>> asyncList = new HashMap<Long, Callable<?>>();
        
        MocaCallableWrapper<?> wrapper = Mockito.mock(MocaCallableWrapper.class);
        
        Mockito.when(wrapper.getCallable()).thenReturn(c1);
        Mockito.when(wrapper.getStatus()).thenReturn(s1);
        
        asyncList.put(t1.getId(), wrapper);
        
        Mockito.when(executor.getRunningTasks()).thenReturn(asyncList);

        MocaExecutionRunnerController controller = Mockito.mock(MocaExecutionRunnerController.class);
        
        Map<Thread, Runnable> clustList = new HashMap<Thread, Runnable>();
        
        CallableWrapper<?> distWrapper = Mockito.mock(CallableWrapper.class);
        
        Mockito.when(distWrapper.getCallable()).thenReturn(c2);
        
        DistributedFuture<?> distFuture = Mockito.mock(DistributedFuture.class);
        
        Mockito.when(distFuture.getCallable()).thenReturn((Callable) distWrapper);
        
        clustList.put(t2, distFuture);
        
        Mockito.when(controller.getRunningThreads()).thenReturn(clustList);
        Mockito.when(controller.getStatus(t2)).thenReturn(s2);
        
        SystemContext context = Mockito.mock(SystemContext.class);
        
        Mockito.when(context.getAttribute(AsynchronousExecutor.class.getName())).thenReturn(executor);
        Mockito.when(context.getAttribute(MocaExecutionRunnerController.class.getName())).thenReturn(controller);
        
        List<CallableStatus> statuses = AsynchronousExecutorStatus.getStatuses(context, Callable.class);
        
        Assert.assertEquals(2, statuses.size());
        
        CallableStatus first = statuses.get(0);
        
        Assert.assertEquals(c1.toString(), first.getCallableString());
        Assert.assertEquals(t1.getId(), first.getThreadId());
        Assert.assertEquals(s1, first.getStatus());
        
        CallableStatus second = statuses.get(1);
        
        Assert.assertEquals(c2.toString(), second.getCallableString());
        Assert.assertEquals(t2.getId(), second.getThreadId());
        Assert.assertEquals(s2, second.getStatus());
    }
}
