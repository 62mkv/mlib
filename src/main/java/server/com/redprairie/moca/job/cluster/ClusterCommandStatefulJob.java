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

package com.redprairie.moca.job.cluster;

import org.quartz.DisallowConcurrentExecution;

import com.redprairie.mad.client.MadSettableGauge;
import com.redprairie.moca.AsynchronousExecutor;
import com.redprairie.moca.job.dao.JobExecutionDAO;

/**
 * A clustered job execution that doesn't allow for jobs to be ran more than 
 * once at a time.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
@DisallowConcurrentExecution
public class ClusterCommandStatefulJob extends ClusterCommandJob {

    /**
     * @param asynchronousExecutor
     * @param jobExecDAO
     * @param executionStatusProbe A probe to record the execution status with
     */
    public ClusterCommandStatefulJob(AsynchronousExecutor asynchronousExecutor,
        JobExecutionDAO jobExecDAO,
        MadSettableGauge<Integer> executionStatusProbe) {
        super(asynchronousExecutor, jobExecDAO, executionStatusProbe);
    }
}
