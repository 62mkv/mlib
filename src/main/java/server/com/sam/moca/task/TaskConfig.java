/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.sam.moca.task;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.sam.moca.MocaRegistry;
import com.sam.moca.probes.tasks.TaskManagerProbeListener;
import com.sam.moca.server.ServerContextFactory;
import com.sam.moca.server.exec.SystemContext;
import com.sam.moca.task.dao.TaskDefinitionDAO;
import com.sam.moca.task.dao.TaskExecutionDAO;
import com.sam.moca.task.dao.hibernate.TaskDefinitionHibernateDAO;
import com.sam.moca.task.dao.hibernate.TaskExecutionHibernateDAO;

/**
 * This class is the spring container for the task and jobs stuff.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
@Configuration
public class TaskConfig {
    @Bean
    public TaskDefinitionDAO taskDefinitionDAO() {
        return new TaskDefinitionHibernateDAO();
    }
    
    @Bean
    public TaskExecutionDAO taskExecutionDAO() {
        return new TaskExecutionHibernateDAO();
    }
    
    @Bean
    public TaskManagerProbeListener taskManagerProbeListener() {
        return new TaskManagerProbeListener();
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
        
        TaskManager manager = new TaskManager(defaultExecutorService(), taskDefinitionDAO(), 
            taskExecutionDAO(), _factory, defaultTaskSyncTime(), environment, 
            startTasks);

        // Add listener for registering/unregistering task probes
        manager.addListener(taskManagerProbeListener());
        return manager;
    }
    
    @Bean
    public int defaultTaskSyncTime() {
        return 60;
    }
    
    @Bean
    public ExecutorService defaultExecutorService() {
        return Executors.newCachedThreadPool();
    }
    
    @Autowired(required=false)
    protected SystemContext _systemContext;
    
    @Autowired(required=false)
    protected ServerContextFactory _factory;
    
    protected static final String DEFAULT = "java";
}
