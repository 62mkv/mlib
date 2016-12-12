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

package com.redprairie.moca.probes.asyncexec;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import junit.framework.Assert;

import org.jgroups.blocks.executor.ExecutionService.DistributedFuture;
import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.async.MocaExecutionRunnerController;
import com.redprairie.moca.async.JGroupsAsynchronousExecutor.CallableWrapper;
import com.redprairie.moca.mad.async.Status;
import com.redprairie.moca.server.exec.SystemContext;

/**
 * Tests ClusterAsynchronousExecutor
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author klucas
 */
public class TU_ClusterAsynchronousExecutors {
    @Test
    public void testEmpty() {
        SystemContext context = Mockito.mock(SystemContext.class);
        
        MocaExecutionRunnerController controller = Mockito.mock(MocaExecutionRunnerController.class);
        
        Mockito.when(controller.getRunningThreads()).thenReturn(new HashMap<Thread, Runnable>());
        
        Mockito.when(context.getAttribute(Mockito.anyString())).thenReturn(controller);
        
        ClusterAsynchronousExecutors execs = new ClusterAsynchronousExecutors(context);
        
        Assert.assertEquals(0, execs.getExecutors().size());
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testOneItem() {
        Callable callable = Mockito.mock(Callable.class);
        
        Mockito.when(callable.toString()).thenReturn("callable");
        
        Status status = Mockito.mock(Status.class);
        Mockito.when(status.getStatus()).thenReturn("status");
        
        Thread thread = Mockito.mock(Thread.class);
        
        Mockito.when(thread.getId()).thenReturn(11L);

        MocaExecutionRunnerController controller = Mockito.mock(MocaExecutionRunnerController.class);
        
        Map<Thread, Runnable> clustList = new HashMap<Thread, Runnable>();
        
        CallableWrapper<?> distWrapper = Mockito.mock(CallableWrapper.class);
        
        Mockito.when(distWrapper.getCallable()).thenReturn(callable);
        
        DistributedFuture<?> distFuture = Mockito.mock(DistributedFuture.class);
        
        Mockito.when(distFuture.getCallable()).thenReturn((Callable) distWrapper);
        
        clustList.put(thread, distFuture);
        
        Mockito.when(controller.getRunningThreads()).thenReturn(clustList);
        Mockito.when(controller.getStatus(thread)).thenReturn(status);
        
        SystemContext context = Mockito.mock(SystemContext.class);
        
        Mockito.when(context.getAttribute(MocaExecutionRunnerController.class.getName())).thenReturn(controller);
        
        ClusterAsynchronousExecutors execs = new ClusterAsynchronousExecutors(context);
        
        Assert.assertEquals(1, execs.getExecutors().size());
        Assert.assertEquals("callable", execs.getExecutors().get(0).getCallable());
        Assert.assertEquals("status", execs.getExecutors().get(0).getStatus());
    }
}
