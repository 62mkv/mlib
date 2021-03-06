package com.sam.moca.servlet;

import java.util.HashSet;
import java.util.Set;

import com.sam.moca.MocaServerAdministrationMBean;
import com.sam.moca.MocaServerHook;
import com.sam.moca.job.JobException;
import com.sam.moca.job.JobManager;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.exec.ServerContext;
import com.sam.moca.task.TaskManager;

/**
 * This administration object is used to perform various operations.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class MocaServerAdministration implements MocaServerAdministrationMBean {
    
    // @see com.sam.moca.servlet.MocaServerAdministrationMBean#restart()
    public void restart(boolean clean) {
        synchronized (_restartCallbacks) {
            // We hold onto the current context so we can close it afterwards.
            ServerContext context = ServerUtils.getCurrentContext();
            try {
                for (MocaServerHook callback : _restartCallbacks) {
                    callback.onRestart(clean);
                }
            }
            finally {
                context.close();
                ServerUtils.removeCurrentContext();
            }
        }
    }
    
    // @see com.sam.moca.MocaServerAdministrationMBean#stop()
    @Override
    public void stop() {
        System.exit(0);
    }
    
    public void addRestartCallback(MocaServerHook callback) {
        synchronized (_restartCallbacks) {
            _restartCallbacks.add(callback);
        }
    }
    
    public void stopTask(String taskId) throws IllegalArgumentException, 
            IllegalStateException {
        if (_taskManager != null) {
            _taskManager.stopTask(taskId);
        }
        else {
            throw new UnsupportedOperationException(
                    "The task manager was not started.");
        }
    }
    
    public void startTask(String taskId) throws IllegalArgumentException, 
            IllegalStateException {
        if (_taskManager != null) {
            _taskManager.startTask(taskId);
        }
        else {
            throw new UnsupportedOperationException(
                    "The task manager was not started.");
        }
    }
    
    public void setTaskManager(TaskManager taskManager) {
        _taskManager = taskManager;
    }
    
    public void setJobManager(JobManager jobManager) {
        _jobManager = jobManager;
    }
    
    public void startSchedulingJob(String jobId) throws IllegalStateException{
        if (_jobManager != null) {
                try {
                    if(!_jobManager.isScheduled(jobId)) {
                        _jobManager.scheduleJob(jobId);
                    }
                    else {
                        throw new UnsupportedOperationException(
                        "The job is already scheduled.");
                    }
                }
                catch (JobException e) {
                    throw new IllegalStateException(e);
                }
        }
        else {
            throw new UnsupportedOperationException(
            "The job manager was not started.");
        }
    }
    
    public void stopSchedulingJob(String jobId) throws IllegalStateException{
        if (_jobManager != null){
                try {
                    if(_jobManager.isScheduled(jobId)) {
                        _jobManager.unscheduleJob(jobId);
                    }
                    else {
                        throw new UnsupportedOperationException(
                            "The job is not scheduled.");
                    }
                }
                catch (JobException e) {
                    throw new IllegalStateException(e);
                }
        }
        else {
            throw new UnsupportedOperationException(
            "The job manager was not started.");
        }
    }
 
    private TaskManager _taskManager;
    private JobManager _jobManager;
    private final Set<MocaServerHook> _restartCallbacks = 
        new HashSet<MocaServerHook>();
    
        
    public static final String ATTRIBUTE_NAME = "MocaServerAdministration";
}