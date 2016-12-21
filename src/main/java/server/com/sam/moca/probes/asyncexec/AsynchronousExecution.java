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

package com.sam.moca.probes.asyncexec;

import java.beans.ConstructorProperties;

/**
 * Represents one execution of a Callable in an AsynchronousExecutor
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author klucas
 */
public class AsynchronousExecution {
    /**
     * Creates a new AsynchronousExecution
     * 
     * @param threadId
     * @param callable
     * @param status
     * @param age
     */
    @ConstructorProperties({"threadId", "callable", "status", "status-age-ms"})
    public AsynchronousExecution(long threadId, String callable, String status, long age) {
        _threadId = threadId;
        _callable = callable;
        _status = status;
        _age = age;
    }

    /**
     * @return threadId
     */
    public long getThreadId() {
        return _threadId;
    }
    
    /**
     * @return A string for the callable
     */
    public String getCallable() {
        return _callable;
    }

    
    /**
     * @return The status of the execution
     */
    public String getStatus() {
        return _status;
    }
    
    /**
     * @return The length of time the current status has been test in milliseconds
     */
    public long getStatusAgeMs() {
        return _age;
    }

    private final long _threadId;
    private final String _callable;
    private final String _status;
    private final long _age;
}
