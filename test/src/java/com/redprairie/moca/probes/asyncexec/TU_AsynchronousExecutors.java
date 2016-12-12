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
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.async.MocaAsynchronousExecutor;
import com.redprairie.moca.async.MocaAsynchronousExecutor.MocaCallableWrapper;
import com.redprairie.moca.mad.async.Status;
import com.redprairie.moca.server.exec.SystemContext;

/**
 * Tests AsynchronousExecutors
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author klucas
 */
public class TU_AsynchronousExecutors {
    @Test
    public void testEmpty() {
        SystemContext context = Mockito.mock(SystemContext.class);
        
        MocaAsynchronousExecutor executor = Mockito.mock(MocaAsynchronousExecutor.class);
        Mockito.when(executor.getRunningTasks()).thenReturn(new HashMap<Long, Callable<?>>());
        
        Mockito.when(context.getAttribute(Mockito.anyString())).thenReturn(executor);
        
        AsynchronousExecutors execs = new AsynchronousExecutors(context);
        
        Assert.assertEquals(0, execs.getExecutors().size());
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testOneItem() {
        SystemContext context = Mockito.mock(SystemContext.class);
        
        MocaAsynchronousExecutor executor = Mockito.mock(MocaAsynchronousExecutor.class);
        
        Callable callable = Mockito.mock(Callable.class);
        Mockito.when(callable.toString()).thenReturn("callable");
        
        Status status = Mockito.mock(Status.class);
        Mockito.when(status.getStatus()).thenReturn("status");
        Mockito.when(status.getAge(TimeUnit.MILLISECONDS)).thenReturn(1L);
       
        MocaCallableWrapper<?> wrapper = Mockito.mock(MocaCallableWrapper.class);
        Mockito.when(wrapper.getCallable()).thenReturn(callable);
        Mockito.when(wrapper.getStatus()).thenReturn(status);
        
        Map<Long, Callable<?>> tasks = new HashMap<Long, Callable<?>>();
        tasks.put(1L, wrapper);
        
        Mockito.when(executor.getRunningTasks()).thenReturn(tasks);
        
        Mockito.when(context.getAttribute(Mockito.anyString())).thenReturn(executor);
        
        AsynchronousExecutors execs = new AsynchronousExecutors(context);
        
        Assert.assertEquals(1, execs.getExecutors().size());
        Assert.assertEquals("callable", execs.getExecutors().get(0).getCallable());
        Assert.assertEquals("status", execs.getExecutors().get(0).getStatus());
    }
}
