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

package com.redprairie.moca.probes.asyncexec;

import java.util.Queue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.mad.client.MadGauge;
import com.redprairie.mad.client.MadMetrics;
import com.redprairie.mad.client.MadName;
import com.redprairie.mad.client.MadNameImpl;
import com.redprairie.mad.client.MadUtil;
import com.redprairie.moca.async.MocaAsynchronousExecutor;
import com.redprairie.moca.mad.MonitoringUtils;
import com.redprairie.moca.server.exec.SystemContext;

import static org.junit.Assert.assertEquals;

/**
 * Tests AsynchronousExecutorProbe
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author klucas
 */
public class TU_AsynchronousExecutorProbe {
    @BeforeClass
    public static void setupClass() {
        _factToRestore = MadMetrics.getFactory();
        
        MadMetrics.setFactory(_mockFactory);
    }
    
    @AfterClass
    public static void afterClass() {
        MadMetrics.setFactory(_factToRestore);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testProbes() {
        AsynchronousExecutorProbe probe = new AsynchronousExecutorProbe() {
            @Override
            protected SystemContext getSystemContext() {
                SystemContext context = Mockito.mock(SystemContext.class);
                
                MocaAsynchronousExecutor executor = Mockito.mock(MocaAsynchronousExecutor.class);
                
                Queue queue = Mockito.mock(Queue.class);
                Mockito.when(queue.size()).thenReturn(5);
                
                Mockito.when(executor.getActiveThreadCount()).thenReturn(10);
                Mockito.when(executor.getMaxThreadCount()).thenReturn(20);
                Mockito.when(executor.getQueue()).thenReturn(queue);
                
                Mockito.when(context.getAttribute(Mockito.anyString())).thenReturn(executor);
                
                return context;
            }
        };
        
        // Mock out MadName generation, should have a better way to do this
        MadName execName = mockNameGeneration(AsynchronousExecutorProbe.EXECUTORS_NAME);
        MadName maxName = mockNameGeneration(AsynchronousExecutorProbe.MAXIMUM_NAME);
        MadName actName = mockNameGeneration(AsynchronousExecutorProbe.ACTIVE_NAME);
        MadName queueName = mockNameGeneration(AsynchronousExecutorProbe.QUEUED_NAME);
        
        probe.initialize();
        
        ArgumentCaptor<MadGauge> maxGauge = ArgumentCaptor.forClass(MadGauge.class);
        Mockito.verify(_mockFactory).newGauge(Mockito.eq(maxName), maxGauge.capture());
        assertEquals(Integer.valueOf(20), (Integer)maxGauge.getValue().getValue());
        
        ArgumentCaptor<MadGauge> actGauge = ArgumentCaptor.forClass(MadGauge.class);
        Mockito.verify(_mockFactory).newGauge(Mockito.eq(actName), actGauge.capture());
        assertEquals(Integer.valueOf(10), (Integer)actGauge.getValue().getValue());
        
        ArgumentCaptor<MadGauge> queueGauge = ArgumentCaptor.forClass(MadGauge.class);
        Mockito.verify(_mockFactory).newGauge(Mockito.eq(queueName), queueGauge.capture());
        assertEquals(Integer.valueOf(5), (Integer)queueGauge.getValue().getValue()); 
        
        MadUtil.unregisterMBean(execName);
    }
    
    private MadName mockNameGeneration(String name) {
        MadName mName = new MadNameImpl(MonitoringUtils.MOCA_GROUP_NAME,
            AsynchronousExecutorProbe.TYPE, name);
        Mockito.when(_mockFactory.newMadName(Mockito.anyString(),
            Mockito.anyString(), Mockito.eq(name)))
                .thenReturn(mName);
        return mName;
    }
    
    private static MadFactory _mockFactory = Mockito.mock(MadFactory.class);
    private static MadFactory _factToRestore;
}
