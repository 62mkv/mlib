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

package com.sam.moca.job;

import java.util.Map;

import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.redprairie.mad.client.MadFactory;
import com.sam.moca.MocaRegistry;
import com.sam.moca.cluster.NoClusterInformation;
import com.sam.moca.job.dao.JobDefinitionDAO;
import com.sam.moca.job.dao.JobExecutionDAO;
import com.sam.moca.job.dao.hibernate.JobDefinitionHibernateDAO;
import com.sam.moca.job.dao.hibernate.JobExecutionHibernateDAO;
import com.sam.moca.probes.jobs.JobManagerProbeListener;
import com.sam.moca.server.ServerContextFactory;
import com.sam.moca.server.exec.SystemContext;

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
public class JobConfig {
    @Bean
    public JobDefinitionDAO jobDefinitionDAO() {
        return new JobDefinitionHibernateDAO();
    }
    
    @Bean
    public JobExecutionDAO jobExecutionDAO() {
        return new JobExecutionHibernateDAO();
    }
    
    @Bean
    public JobManagerProbeListener jobManagerProbeListener() {
        return new JobManagerProbeListener();
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
        
        JobManager manager = new JobManager(jobDefinitionDAO(), jobScheduler(), 
            new NoClusterInformation(), defaultJobSyncTime(), startScheduling);
        manager.addListener(jobManagerProbeListener());
        return manager;
    }
    
    @Bean
    public JobScheduler jobScheduler() {
        Map<String, String> environment = _systemContext
                .getConfigurationSection(MocaRegistry.REGSEC_ENVIRONMENT, false);
        return new JobScheduler(jobFactory(), environment);
    }
    
    @Bean
    public JobFactory jobFactory() {
        return new MocaJobFactory(_factory, jobExecutionDAO(), _madFactory);
    }
    
    @Bean
    public int defaultJobSyncTime() {
        return 60;
    }
    
    @Autowired
    protected SystemContext _systemContext;
    
    @Autowired
    protected ServerContextFactory _factory;
    
    @Autowired
    protected MadFactory _madFactory;
}
