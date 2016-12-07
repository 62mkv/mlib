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

package com.redprairie.moca.job.cluster;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.infinispan.manager.EmbeddedCacheManager;
import org.quartz.spi.JobFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.cluster.infinispan.InfinispanClusterInformation;
import com.redprairie.moca.job.JobConfig;
import com.redprairie.moca.job.JobDefinition;
import com.redprairie.moca.job.JobManager;

/**
 * Cluster based job configuration
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
@Configuration
public class ClusterJobConfig extends JobConfig implements ApplicationContextAware {
    // @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        _applicationContext = applicationContext;
    }
    
    @Bean
    @Scope(value="prototype")
    public JobManager jobManager() {
        boolean startScheduling;
        String inhibit = _systemContext.getConfigurationElement(
            MocaRegistry.REGKEY_SERVER_INHIBIT_JOBS, "false");
        if (inhibit.equals("true")) {
            startScheduling = false;
        }
        else {
            startScheduling = true;
        }
        
        // Retrieve the Cache Manager and the Local Node from the parent application context.
        EmbeddedCacheManager cacheManager = _applicationContext.getBean(EmbeddedCacheManager.class);
        Node localNode = _applicationContext.getBean(Node.class);
        ConcurrentMap<Node,Set<JobDefinition>> clusterCache = cacheManager.getCache("moca-job-cache");
        
        JobManager manager = new JobManager(jobDefinitionDAO(), jobScheduler(), 
            new InfinispanClusterInformation(cacheManager),
            _applicationContext.getBean("defaultJobSyncTime", Integer.class), 
            startScheduling, clusterCache, localNode, MOCA_JOB_CACHE);
        manager.addListener(jobManagerProbeListener());
        return manager;
    }
    
    @Bean
    public JobFactory jobFactory() {
        return new ClusterMocaJobFactory(_factory, jobExecutionDAO(), _madFactory);
    }
    
    public static final String MOCA_JOB_CACHE = "moca-job-cache";
    
    private ApplicationContext _applicationContext;
}
