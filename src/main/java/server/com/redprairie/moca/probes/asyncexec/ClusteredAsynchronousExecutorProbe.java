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

import com.redprairie.mad.annotations.ProbeGroup;
import com.redprairie.mad.annotations.ProbeType;
import com.redprairie.mad.client.MadGauge;
import com.redprairie.mad.client.MadUtil;
import com.redprairie.mad.probes.InitializedProbe;
import com.redprairie.moca.AsynchronousExecutor;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.async.JGroupsAsynchronousExecutor;
import com.redprairie.moca.async.MocaExecutionRunnerController;
import com.redprairie.moca.mad.MonitoringUtils;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.SystemContext;

/**
 * Gets information about the Clustered AsynchronousExecutors
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author klucas
 */
@ProbeGroup(MonitoringUtils.MOCA_GROUP_NAME)
@ProbeType(ClusteredAsynchronousExecutorProbe.TYPE)
public class ClusteredAsynchronousExecutorProbe extends InitializedProbe {

    // @see com.redprairie.mad.probes.InitializedProbe#initialize()
    @Override
    public void initialize() {
        SystemContext context = getSystemContext();
        
        if (null != context.getConfigurationElement(MocaRegistry.REGKEY_CLUSTER_NAME)) {
            MadUtil.registerMBean(getMadName(EXECUTORS_NAME), new ClusterAsynchronousExecutors(context));
            
            getFactory().newGauge(getMadName(ACTIVE_NAME), new MadGauge<Integer>() {
                @Override
                public Integer getValue() {
                    SystemContext context = getSystemContext();
                    MocaExecutionRunnerController controller = (MocaExecutionRunnerController)context.getAttribute(
                        MocaExecutionRunnerController.class.getName());
                    
                    return controller.getActiveThreadCount();
                }
            });
            
            getFactory().newGauge(getMadName(MAXIMUM_NAME), new MadGauge<Integer>() {
                @Override
                public Integer getValue() {
                    SystemContext context = getSystemContext();
                    MocaExecutionRunnerController controller = (MocaExecutionRunnerController)context.getAttribute(
                        MocaExecutionRunnerController.class.getName());
                    
                    return controller.getMaximumThreadCount();
                }
            });
            
            getFactory().newGauge(getMadName(QUEUED_NAME), new MadGauge<Integer>() {
                @Override
                public Integer getValue() {
                    SystemContext context = getSystemContext();
                    JGroupsAsynchronousExecutor executor = (JGroupsAsynchronousExecutor)context.getAttribute(
                        "cluster-" + AsynchronousExecutor.class.getName()); 
                    
                    return executor.getQueue().size();
                }
            });
        }
    }
    
    /**
     * @return SystemContext
     */
    protected SystemContext getSystemContext() {
        return ServerUtils.globalContext();
    }
    
    protected static final String TYPE = "Clustered-Asynchronous-Executors";
    protected static final String EXECUTORS_NAME = "executors";
    protected static final String ACTIVE_NAME = "active-executors";
    protected static final String MAXIMUM_NAME = "maximum-executors";
    protected static final String QUEUED_NAME = "queue-callables";
  }
