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

package com.sam.moca.probes.asyncexec;

import com.redprairie.mad.annotations.ProbeGroup;
import com.redprairie.mad.annotations.ProbeType;
import com.redprairie.mad.client.MadGauge;
import com.redprairie.mad.client.MadUtil;
import com.redprairie.mad.probes.InitializedProbe;
import com.sam.moca.AsynchronousExecutor;
import com.sam.moca.async.MocaAsynchronousExecutor;
import com.sam.moca.mad.MonitoringUtils;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.exec.SystemContext;

/**
 * Gets information about the AsynchronousExecutors
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author klucas
 */
@ProbeGroup(MonitoringUtils.MOCA_GROUP_NAME)
@ProbeType(AsynchronousExecutorProbe.TYPE)
public class AsynchronousExecutorProbe extends InitializedProbe {

    // @see com.redprairie.mad.probes.InitializedProbe#initialize()
    @Override
    public void initialize() {
        MadUtil.registerMBean(getMadName(EXECUTORS_NAME), new AsynchronousExecutors(getSystemContext()));
        
        getFactory().newGauge(getMadName(ACTIVE_NAME), new MadGauge<Integer>() {
            @Override
            public Integer getValue() {
                SystemContext context = getSystemContext();
                MocaAsynchronousExecutor executor = (MocaAsynchronousExecutor)context.getAttribute(
                    AsynchronousExecutor.class.getName());            
                
                return executor.getActiveThreadCount();
            }
        });
        
        getFactory().newGauge(getMadName(MAXIMUM_NAME), new MadGauge<Integer>() {
            @Override
            public Integer getValue() {
                SystemContext context = getSystemContext();
                MocaAsynchronousExecutor executor = (MocaAsynchronousExecutor)context.getAttribute(
                    AsynchronousExecutor.class.getName());            
                
                return executor.getMaxThreadCount();
            }
        });
        
        getFactory().newGauge(getMadName(QUEUED_NAME), new MadGauge<Integer>() {
            @Override
            public Integer getValue() {
                SystemContext context = getSystemContext();
                MocaAsynchronousExecutor executor = (MocaAsynchronousExecutor)context.getAttribute(
                    AsynchronousExecutor.class.getName());            
                
                return executor.getQueue().size();
            }
        });
    }
    
    /**
     * @return SystemContext
     */
    protected SystemContext getSystemContext() {
        return ServerUtils.globalContext();
    }
    
    protected static final String TYPE = "Asynchronous-Executors";
    protected static final String EXECUTORS_NAME = "executors";
    protected static final String ACTIVE_NAME = "active-executors";
    protected static final String MAXIMUM_NAME = "maximum-executors";
    protected static final String QUEUED_NAME = "queue-callables";
}
