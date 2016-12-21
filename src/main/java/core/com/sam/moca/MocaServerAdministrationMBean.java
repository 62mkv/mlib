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

package com.sam.moca;

import javax.management.MXBean;


/**
 * This interface defines the methods that are available when administering the
 * moca server.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
@MXBean
public interface MocaServerAdministrationMBean {

    /**
     * This method will call all the registered call back functions in an 
     * attempt to restart vital parts of the moca server without physically 
     * shutting it down.
     */
    public void restart(boolean clean);

    
    /**
     * This will shutdown the moca server.
     */
    public void stop();
    
    /**
     * This will attempt to start a non running task.
     * @param taskId The task to start
     * @throws IllegalArgumentException This occurs if the task is not found.
     * @throws IllegalStateException This is thrown if the task manager
     *         is not running
     */
    public void startTask(String taskId) throws IllegalArgumentException, 
            IllegalStateException;
    
    /**
     * This will attempt to stop a running task.
     * @param taskId The task to stop.
     * @throws IllegalArgumentException This occurs if the task is not found.
     * @throws IllegalStateException This is thrown if the task manager
     *         is not running
     */
    public void stopTask(String taskId) throws IllegalArgumentException, 
            IllegalStateException;
    
    /**
     * This will attempt to schedule a job based on job id.
     * 
     * @param jobId
     * @throws IllegalStateException
     */
    public void startSchedulingJob(String jobId) throws IllegalStateException;
    
    /**
     * This will attempt to deschedule a job based on job id.
     * 
     * @param jobId
     * @throws IllegalStateException
     */
    public void stopSchedulingJob(String jobId) throws IllegalStateException;

}