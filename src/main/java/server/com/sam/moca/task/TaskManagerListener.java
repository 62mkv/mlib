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

package com.sam.moca.task;

/**
 * A listener that listens for the Task Manager
 * adding/changing/removing tasks. Also has
 * hook points for the Task Manager being started,
 * stopped, and restarted.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public interface TaskManagerListener {
    
    /**
     * Occurs right after the Task Manager is started
     * @param manager The Task Manager
     */
    public void onStart(TaskManager manager);
    
    /**
     * Occurs right after the Task Manager is stopped
     * @param manager The Task Manager
     */
    public void onStop(TaskManager manager);
    
    /**
     * Occurs right after the Task Manager has restarted
     * @param manager The Task Manager
     */
    public void onRestart(TaskManager manager);

    /**
     * Occurs when a Task is added to the Task Manager
     * @param manager The Task Manager
     * @param task The Task Definition
     */
    public void onTaskAdded(TaskManager manager, TaskDefinition task);
    
    /**
     * Occurs when a Task is changed on the Task Manager
     * @param manager The Task Manager
     * @param oldTask The old Task Definition
     * @param newTask The new Task Definition
     */
    public void onTaskChanged(TaskManager manager, TaskDefinition oldTask, TaskDefinition newTask);
    
    /**
     * Occurs when a Task is removed on the Task Manager
     * @param manager The Task Manager
     * @param task The Task Definition that is removed
     */
    public void onTaskRemoved(TaskManager manager, TaskDefinition task);
    
    /**
     * Occurs when a Task has been started by the Task Manager
     * @param manager The Task Manager
     * @param task    The Task Definition for the task that started
     */
    public void onTaskStarted(TaskManager manager, TaskDefinition task);
    
    /**
     * Occurs when a Task has been stopped either by the Task Manager or its
     * execution has simply ended either due to an exception or simply is done running.
     * @param manager The Task Manager
     * @param task    The Task Definition for the task that stopped
     */
    public void onTaskStopped(TaskManager manager, TaskDefinition task);

}
