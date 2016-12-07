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

package com.redprairie.moca.probes.asyncexec;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.jgroups.blocks.executor.ExecutionService.DistributedFuture;

import com.redprairie.moca.async.MocaExecutionRunnerController;
import com.redprairie.moca.async.JGroupsAsynchronousExecutor.CallableWrapper;
import com.redprairie.moca.mad.async.Status;
import com.redprairie.moca.server.exec.SystemContext;

/**
 * Gets informations about the Clustered AsynchronousExecutors which are running
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author klucas
 */
public class ClusterAsynchronousExecutors extends AbstractExecutors {
    /**
     * @param context
     */
    public ClusterAsynchronousExecutors(SystemContext context) {
        _context = context;
    }
    
    private CallableWrapper<?> getWrapper(Runnable runnable) {
        CallableWrapper<?> wrapper = null;
        
        if (runnable != null) {
            if (runnable instanceof DistributedFuture) {
                Callable<?> callable = ((DistributedFuture<?>)runnable).getCallable();
                    
                if (callable instanceof CallableWrapper) {
                    wrapper = ((CallableWrapper<?>)callable);
                }
            }
        }
        
        return wrapper;
    }
    
    // @see com.redprairie.moca.probes.asyncexec.AsynchronousExecutorsMXBean#getExecutors()
    @Override
    public List<AsynchronousExecution> getExecutors() {
        MocaExecutionRunnerController controller = (MocaExecutionRunnerController)_context.getAttribute(
            MocaExecutionRunnerController.class.getName());
        Map<Thread, Runnable> runningThreads = controller.getRunningThreads();
        
        List<AsynchronousExecution> executors = new LinkedList<AsynchronousExecution>();
        
        for (Entry<Thread, Runnable> entry : runningThreads.entrySet()) {
            Thread thread = entry.getKey();
            Runnable value = entry.getValue();
            
            
            CallableWrapper<?> wrapper = getWrapper(value);
            Status status = controller.getStatus(thread);
            
            if (wrapper != null) {
                executors.add(new AsynchronousExecution(thread.getId(),
                    String.valueOf(wrapper.getCallable()), 
                    status.getStatus(), 
                    status.getAge(TimeUnit.MILLISECONDS)));
            } else {
                executors.add(new AsynchronousExecution(thread.getId(), "", "Idle", 0));
            }
            
        }
        
        return executors;
    }
    
    private final SystemContext _context;
}
