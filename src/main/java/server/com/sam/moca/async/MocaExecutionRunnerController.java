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

package com.sam.moca.async;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.sam.moca.mad.async.Status;
import com.sam.moca.util.DaemonThreadFactory;

/**
 * Moca Execution Runner Controller that allows for adding and removing
 * runners dynamically.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MocaExecutionRunnerController {
    public MocaExecutionRunnerController(MocaExecutionRunner runner) {
        _runner = runner;
    }
    
    public void addRunner() {
        _futures.add(_service.submit(_runner));
    }
    
    public void removeRunner() {
        _futures.remove().cancel(true);
    }
    
    public Map<Thread, Runnable> getRunningThreads() {
        return _runner.getCurrentRunningTasks();
    }
    
    public Status getStatus(String sessionId) {
        return _runner.getStatus(sessionId);
    }
    
    public Status getStatus(Thread thread) {
        return _runner.getStatus(thread);
    }
    
    public void setStatus(String sessionId, String status) {
        _runner.setStatus(sessionId, status);
    }
    
    public int getMaximumThreadCount() {
        return _futures.size();
    }
    
    public int getActiveThreadCount() {
        return _runner.getActiveThreadCount();
    }
    
    private final Queue<Future<?>> _futures = new ConcurrentLinkedQueue<Future<?>>();
    
    private final MocaExecutionRunner _runner;
    
    private final ExecutorService _service = Executors.newCachedThreadPool(
        new DaemonThreadFactory("ExecutionRunnerPool"));
}
