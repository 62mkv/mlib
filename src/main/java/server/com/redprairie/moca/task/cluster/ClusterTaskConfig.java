/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.moca.task.cluster;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.infinispan.manager.EmbeddedCacheManager;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.task.TaskConfig;
import com.redprairie.moca.task.TaskDefinition;
import com.redprairie.moca.task.TaskManager;

/**
 * This is a specialized Task Spring Configuration Class used to configure
 * Tasks for Clustered environments. It is meant to create a TaskManager 
 * instance with the Cluster's Cache and the Server's Local Node.
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author eknapp
 */
@Configuration
public class ClusterTaskConfig extends TaskConfig implements ApplicationContextAware {

    // @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        _applicationContext = applicationContext;   
    }
    
    @Bean
    @Scope(value="prototype")
    public TaskManager taskManager() {
        boolean startTasks;
        String inhibit = _systemContext.getConfigurationElement(
                MocaRegistry.REGKEY_SERVER_INHIBIT_TASKS, "false");
        if (inhibit.equals("true")) {
            startTasks = false;
        }
        else {
            startTasks = true;
        }
        
        Map<String, String> environment = _systemContext.getConfigurationSection(
                MocaRegistry.REGSEC_ENVIRONMENT, false);
        
        // We need to insert into the environment the values for some known
        // task variables.
        String javaVm = _systemContext.getConfigurationElement(
                MocaRegistry.REGKEY_JAVA_VM);

        // If the registry contains vm then use it.
        if (javaVm != null) {
            environment.put(TaskManager.JAVAVM, javaVm);   
        }
        // If the environment doesn't contain the JAVA value then we insert 
        // the default.
        else if (!environment.containsKey(TaskManager.JAVAVM)) {
            environment.put(TaskManager.JAVAVM, DEFAULT);
        }
    
        String javaVm32 = _systemContext.getConfigurationElement(
                MocaRegistry.REGKEY_JAVA_VM32);
        // If the registry contains vm32 then use it.
        if (javaVm32 != null) {
            environment.put(TaskManager.JAVAVM32, javaVm32);   
        }
        // If the environment doesn't contain the JAVA32 value then we insert 
        // the default.
        else if (!environment.containsKey(TaskManager.JAVAVM32)) {
            environment.put(TaskManager.JAVAVM32, DEFAULT);
        }
        
        EmbeddedCacheManager cacheManager = _applicationContext.getBean(EmbeddedCacheManager.class);
        Node localNode = _applicationContext.getBean(Node.class);
        ConcurrentMap<Node,Set<TaskDefinition>> clusterCache = cacheManager.getCache("moca-task-cache");
        
        TaskManager manager = new TaskManager(defaultExecutorService(), taskDefinitionDAO(), 
            taskExecutionDAO(), _factory, defaultTaskSyncTime(), environment, 
            startTasks, clusterCache, localNode, MOCA_TASK_CACHE);

        // Add listener for registering/unregistering task probes
        manager.addListener(taskManagerProbeListener());
        return manager;
    }
    
    public static final String MOCA_TASK_CACHE = "moca-task-cache";
    
    private ApplicationContext _applicationContext;

}
