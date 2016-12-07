/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.task;

import com.redprairie.moca.MocaException;

/**
 * This exception is used when attempting to start or stop a task and it was
 * already started or stopped.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TaskInvalidStateException extends MocaException {
    private static final long serialVersionUID = -6701672540806112993L;
    
    public static final int CODE = 812;

    /**
     * Creates a new TaskInvalidState exception
     * @param taskId The task that was in invalid state.
     */
    public TaskInvalidStateException(String taskId) {
        super(CODE, "Cannot stop/start task ^task_id^, as it was already " +
        		"stopped or started.");
        addArg("task_id", taskId);
    }
}
