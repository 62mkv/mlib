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

package com.sam.moca.job.cluster;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.TriggerFiredBundle;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.mad.client.MadSettableGauge;
import com.sam.moca.AsynchronousExecutor;
import com.sam.moca.job.JobDefinition;
import com.sam.moca.job.LocalCommandJob;
import com.sam.moca.job.LocalCommandStatefulJob;
import com.sam.moca.job.MocaJobFactory;
import com.sam.moca.job.dao.JobExecutionDAO;
import com.sam.moca.server.ServerContextFactory;
import com.sam.moca.util.MocaUtils;

/**
 * Custom job factory for use with moca when running in a cluster.  This factory
 * will check if the job can be ran on any node and if so will submit the
 * job to the clustered execution engine.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class ClusterMocaJobFactory extends MocaJobFactory {
    /**
     * @param dispatcher
     */
    public ClusterMocaJobFactory(ServerContextFactory factory, 
        JobExecutionDAO jobExecDAO,
        MadFactory madFactory) {
        super(factory, jobExecDAO, madFactory);
    }

    // @see com.sam.moca.job.MocaJobFactory#newJob(org.quartz.spi.TriggerFiredBundle, org.quartz.Scheduler)
    @Override
    public Job newJob(TriggerFiredBundle fired, Scheduler scheduler)
            throws SchedulerException {
        JobDetail detail = fired.getJobDetail();
        JobDataMap map = detail.getJobDataMap();
        JobDefinition job = (JobDefinition) map.get("job");
        if (job != null && job.getRole() == null) {
            synchronized (this) {
                // This has to be lazily initialized because the cluster async
                // is not setup during construction of the factory
                if (_asyncExecutor == null) {
                    _asyncExecutor = MocaUtils.clusterAsyncExecutor();
                }
            }
            
            if (_asyncExecutor == null) {
                throw new UnsupportedOperationException("Cluster Job Factory " +
                        "cannot be used without the Cluster Async Executor!");
            }
            Class<? extends Job> jobClass = detail.getJobClass();
            MadSettableGauge<Integer> executionStatusProbe = getJobExecutionStatusProbe(job);
            if (jobClass == LocalCommandJob.class) {
                return new ClusterCommandJob(_asyncExecutor, _jobExecDAO, executionStatusProbe);
            }
            else if (jobClass == LocalCommandStatefulJob.class) {
                return new ClusterCommandStatefulJob(_asyncExecutor, _jobExecDAO, executionStatusProbe);
            }
        }
        return super.newJob(fired, scheduler);
    }
    
    AsynchronousExecutor _asyncExecutor;
}
