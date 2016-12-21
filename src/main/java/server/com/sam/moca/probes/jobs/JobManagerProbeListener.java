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

package com.sam.moca.probes.jobs;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sam.moca.job.JobDefinition;
import com.sam.moca.job.JobManager;
import com.sam.moca.job.JobManagerListener;

/**
 * A listener that registers probes related
 * to jobs by listening to the Job Managers functions.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class JobManagerProbeListener implements JobManagerListener {

    // @see com.sam.moca.job.JobManagerListener#onStart(com.sam.moca.job.JobManager)
    @Override
    public void onStart(JobManager manager) {
        // Nothing to do here
    }

    // @see com.sam.moca.job.JobManagerListener#onStop(com.sam.moca.job.JobManager)
    @Override
    public void onStop(JobManager manager) {
        _logger.debug("Removing all job related probes");
        for (JobProbe jobProbe : _jobProbes.values()) {
            jobProbe.unregister();
        }
        _jobProbes.clear();
    }

    // @see com.sam.moca.job.JobManagerListener#onRestart(com.sam.moca.job.JobManager)
    @Override
    public void onRestart(JobManager manager) {
        // We need to update the references on the Job Definitions
        // as while the Jobs may be fundamentally equal the underlying
        // objects have changed after a restart.
        List<JobDefinition> currentJobs = manager.getCurrentJobs();
        for (JobDefinition job : currentJobs) {
            JobProbe probe = _jobProbes.get(job.getJobId());
            if (probe != null) {
                probe.setJobDefinition(job);
            }
        }
    }

    // @see com.sam.moca.job.JobManagerListener#onJobAdded(com.sam.moca.job.JobManager, com.sam.moca.job.JobDefinition)
    @Override
    public void onJobAdded(JobManager manager, JobDefinition job) {
        addJob(manager, job);
    }

    // @see com.sam.moca.job.JobManagerListener#onJobRemoved(com.sam.moca.job.JobManager, com.sam.moca.job.JobDefinition)
    @Override
    public void onJobRemoved(JobManager manager, JobDefinition job) {
        removeJob(job);
    }

    // @see com.sam.moca.job.JobManagerListener#onJobChanged(com.sam.moca.job.JobManager, com.sam.moca.job.JobDefinition, com.sam.moca.job.JobDefinition)
    @Override
    public void onJobChanged(JobManager manager, JobDefinition oldJob,
                             JobDefinition newJob) {
        removeJob(oldJob);
        addJob(manager, newJob);
    }
    
    // @see com.sam.moca.job.JobManagerListener#onJobScheduled(com.sam.moca.job.JobManager, com.sam.moca.job.JobDefinition)
    @Override
    public void onJobScheduled(JobManager manager, JobDefinition job) {
        JobProbe probe = _jobProbes.get(job.getJobId());
        probe.notifyJobScheduled(manager);
    }

    // @see com.sam.moca.job.JobManagerListener#onJobUnscheduled(com.sam.moca.job.JobManager, com.sam.moca.job.JobDefinition)
    @Override
    public void onJobUnscheduled(JobManager manager, JobDefinition job) {
        // We won't unregister anything when a job is unscheduled
    }
    
    private void addJob(JobManager manager, JobDefinition job) {
        JobProbe probe = new JobProbe(manager, job);
        _jobProbes.put(job.getJobId(), probe);
    }
    
    private void removeJob(JobDefinition job) {
        JobProbe probe = _jobProbes.get(job.getJobId());
        probe.unregister();
    }

    private final Map<String, JobProbe> _jobProbes = new ConcurrentHashMap<String, JobProbe>();
    private static final Logger _logger = LogManager.getLogger(JobManagerProbeListener.class);
    
}
