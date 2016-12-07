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

package com.redprairie.moca.job;

/**
 * An interface that can be implemented to listen
 * to functions of the Job Manager.
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public interface JobManagerListener {
    
    /**
     * Occurs after the Job Manager has been started
     * @param manager The Job Manager
     */
    public void onStart(JobManager manager);
    
    /**
     * Occurs after the Job Manager has been stopped
     * @param manager The Job Manager
     */
    public void onStop(JobManager manager);
    
    /**
     * Occurs after the Job Manager has been restarted
     * @param manager The Job Manager
     */
    public void onRestart(JobManager manager);
    
    /**
     * Occurs after a job has been added to the Job Manager
     * @param manager The Job Manager
     * @param job The Job Definition
     */
    public void onJobAdded(JobManager manager, JobDefinition job);
    
    /**
     * Occurs after a job has been removed from the Job Manager
     * @param manager The Job Manager
     * @param job The Job Definition
     */
    public void onJobRemoved(JobManager manager, JobDefinition job);
    
    /**
     * Occurs after a job has been changed on the Job Manager
     * @param manager The Job Manager
     * @param oldJob The old Job Definition
     * @param newJob The new Job Definition
     */
    public void onJobChanged(JobManager manager, JobDefinition oldJob, JobDefinition newJob);
    
    /**
     * Occurs after a job has been scheduled by the Job Manager
     * @param manager The Job Manager
     * @param job The Job Definition
     */
    public void onJobScheduled(JobManager manager, JobDefinition job);
    
    /**
     * Occurs after a job has been unscheduled by the Job Manager
     * @param manager The Job Manager
     * @param job The Job Definition
     */
    public void onJobUnscheduled(JobManager manager, JobDefinition job);

}
