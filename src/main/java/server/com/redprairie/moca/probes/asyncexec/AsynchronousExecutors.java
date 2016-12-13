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

package com.redprairie.moca.probes.asyncexec;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.redprairie.moca.AsynchronousExecutor;
import com.redprairie.moca.async.MocaAsynchronousExecutor;
import com.redprairie.moca.async.MocaAsynchronousExecutor.MocaCallableWrapper;
import com.redprairie.moca.mad.async.Status;
import com.redprairie.moca.server.exec.SystemContext;

/**
 * Information about the non-clustered AsynchronousExecutors
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author klucas
 */
public class AsynchronousExecutors extends AbstractExecutors {
    /**
     * @param context
     */
    public AsynchronousExecutors(SystemContext context) {
        _context = context;
    }
    
    // @see com.redprairie.moca.probes.asyncexec.ExecutorsMXBean#getExecutors()
    @Override
    public List<AsynchronousExecution> getExecutors() {
        MocaAsynchronousExecutor executor = (MocaAsynchronousExecutor)_context.getAttribute(
            AsynchronousExecutor.class.getName());
        Map<Long, Callable<?>> runningTasks = executor.getRunningTasks();
        
        List<AsynchronousExecution> executors = new LinkedList<AsynchronousExecution>();

        for (Entry<Long, Callable<?>> entry : runningTasks.entrySet()) {
            if (entry.getValue() instanceof MocaCallableWrapper) {
                MocaCallableWrapper<?> wrapper = (MocaCallableWrapper<?>) entry
                    .getValue();

                Status status = wrapper.getStatus();

                executors.add(new AsynchronousExecution(entry.getKey(), String
                    .valueOf(wrapper.getCallable()), status.getStatus(), status
                    .getAge(TimeUnit.MILLISECONDS)));
            }
            else {
                executors.add(new AsynchronousExecution(entry.getKey(), "",
                    "Idle", 0));
            }
        }
        
        return executors;
    }
    
    private final SystemContext _context;
}
