/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

import com.sam.moca.MocaException;

/**
 * This exception is used when attempting to lookup a specific task and it
 * is not found.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TaskNotFoundException extends MocaException {
    private static final long serialVersionUID = 8190095923604868280L;
    
    public static final int CODE = 811;
    /**
     * Creates a new TaskNotFoundException for the given task Id.
     * @param taskId The task id to use.
     */
    public TaskNotFoundException(String taskId) {
        super(CODE, "Task ^task_id^ was not found.");
        addArg("task_id", taskId);
    }
}
