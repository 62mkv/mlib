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

package com.redprairie.moca.mad.async;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.jgroups.blocks.executor.ExecutionService.DistributedFuture;

import com.redprairie.moca.AsynchronousExecutor;
import com.redprairie.moca.async.MocaAsynchronousExecutor;
import com.redprairie.moca.async.MocaExecutionRunnerController;
import com.redprairie.moca.async.JGroupsAsynchronousExecutor.CallableWrapper;
import com.redprairie.moca.async.MocaAsynchronousExecutor.MocaCallableWrapper;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SystemContext;

/**
 * Gets and retrieves the status of AsynchronousExecutors
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author klucas
 */
public class AsynchronousExecutorStatus {
    /**
     * Sets the status of the AsynchronousExecutor that is running
     * the caller.
     * 
     * @param status The status
     */
    public static void set(String status) {
        SystemContext sysCxt = ServerUtils.globalContext();
        ServerContext srvCxt = ServerUtils.getCurrentContext();
        
        set(sysCxt, srvCxt, status);
    }
    
    /**
     * Sets the status of the AsynchronousExecutor that is running
     * the caller.
     * 
     * @param systemContext
     * @param serverContext
     * @param status The status
     */
    public static void set(SystemContext systemContext, ServerContext serverContext, String status) {
        String sessionId = getSessionId(serverContext);
        
        if (isClustered(sessionId)) {
            setClustered(systemContext, sessionId, status);
        } else {
            setNonclustered(systemContext, sessionId, status);
        }
    }
    
    /**
     * Sets the status of the AsynchronousExecutor with the specified
     * sessionId.
     * 
     * @param status The status
     * @param sessionId The sessionId
     */
    public static void set(String status, String sessionId) {
        SystemContext sysCxt = ServerUtils.globalContext();
        
        set(sysCxt, status, sessionId);
    }
    
    /**
     * Sets the status of the AsynchronousExecutor with the specified
     * sessionId.
     * 
     * @param systemContext
     * @param status The status
     * @param sessionId The sessionId
     */
    public static void set(SystemContext systemContext, String status, String sessionId) {
        if (isClustered(sessionId)) {
            setClustered(systemContext, sessionId, status);
        } else {
            setNonclustered(systemContext, sessionId, status);
        }
    }
    
    /**
     * Gets the status of the AsynchronousExecutor that the caller
     * is in
     * 
     * @return The status
     */
    public static Status get() {
        SystemContext sysCxt = ServerUtils.globalContext();
        ServerContext srvCxt = ServerUtils.getCurrentContext();
        
        return get(sysCxt, srvCxt);
    }
        
    /**
     * Gets the status of the AsynchronousExecutor that the caller
     * is in
     * 
     * @return The status
     */
    public static Status get(SystemContext systemContext, ServerContext serverContext) {
        final String sessionId = getSessionId(serverContext);
        
        if (isClustered(sessionId)) {
            return getClustered(systemContext, sessionId);
        } else {
            return getNonclustered(systemContext, sessionId);
        }
    }
    
    /**
     * Finds each AsynchronousExecutor which is executing a called of 
     * type clazz and returns a list of the status of those callables.
     * @param clazz The type of Callable to find
     * @return List of statuses
     */
    public static List<CallableStatus> getStatuses(Class<?> clazz) {
        SystemContext context = ServerUtils.globalContext();
        
        return getStatuses(context, clazz);
    }
        
    /**
     * Finds each AsynchronousExecutor which is executing a called of 
     * type clazz and returns a list of the status of those callables.
     * @param context SystemContext 
     * @param clazz The type of Callable to find
     * @return List of statuses
     */
    public static List<CallableStatus> getStatuses(SystemContext context, Class<?> clazz) {
        List<CallableStatus> statuses = new LinkedList<CallableStatus>();
        
        // Do the normal AsynchronousExecutor
        MocaAsynchronousExecutor executor = (MocaAsynchronousExecutor)context.getAttribute(
            AsynchronousExecutor.class.getName());
        if (executor != null) {
            Map<Long, Callable<?>> runningTasks = executor.getRunningTasks();
            
            for (Entry<Long, Callable<?>> entry : runningTasks.entrySet()) {
                if (entry.getValue() instanceof MocaCallableWrapper) {
                    MocaCallableWrapper<?> wrapper = (MocaCallableWrapper<?>) entry
                        .getValue();

                    Status status = wrapper.getStatus();
                    Callable<?> callable = wrapper.getCallable();

                    if (clazz.isAssignableFrom(callable.getClass())) {
                        statuses.add(new CallableStatus(String
                            .valueOf(callable), entry.getKey(), status));
                    }
                }
            }
        }
        
        // Do the clustered AsynchronousExecutor
        MocaExecutionRunnerController controller = (MocaExecutionRunnerController)context.getAttribute(
            MocaExecutionRunnerController.class.getName());
        
        if (controller != null) {
            Map<Thread, Runnable> runningThreads = controller.getRunningThreads();
            
            for (Entry<Thread, Runnable> entry : runningThreads.entrySet()) {
                Thread thread = entry.getKey();
                Runnable value = entry.getValue();
                
                
                CallableWrapper<?> wrapper = getWrapper(value);
                Status status = controller.getStatus(thread);
                
                if (wrapper != null) {
                    Callable<?> callable = wrapper.getCallable();
                    
                    if (clazz.isAssignableFrom(callable.getClass())) {
                        statuses.add(new CallableStatus(String.valueOf(callable), thread.getId(), status));
                    }
                }
            }
        }
        
        return statuses;
    }
    
    private static String getSessionId(ServerContext context) {
        return context.getSession().getSessionId();
    }
    
    private static boolean isClustered(String sessionId) {
        return sessionId.startsWith("dist");
    }
    
    private static void setClustered(SystemContext context, final String sessionId, final String status) {
        MocaExecutionRunnerController controller = (MocaExecutionRunnerController)context.getAttribute(
            MocaExecutionRunnerController.class.getName());
        
        controller.setStatus(sessionId, status);
    }
    
    private static void setNonclustered(SystemContext context, final String sessionId, final String status) {
        MocaAsynchronousExecutor executor = (MocaAsynchronousExecutor)context.getAttribute(
                AsynchronousExecutor.class.getName());
        
        forEachCallable(executor, new MocaCallableWrapperVisitor<Object> () {
            @Override
            public Object visit(MocaCallableWrapper<?> wrapper)  {
                if (sessionId.equals(wrapper.getSessionId())) {
                    wrapper.setStatus(status);
                    
                    return new Object();
                } else {
                    return null;
                }
            }
        });
    }
    
    private static Status getClustered(SystemContext context, final String sessionId) {
        MocaExecutionRunnerController controller = (MocaExecutionRunnerController)context.getAttribute(
            MocaExecutionRunnerController.class.getName());
        
        return controller.getStatus(sessionId);
    }
    
    private static Status getNonclustered(SystemContext context, final String sessionId) {
        MocaAsynchronousExecutor executor = (MocaAsynchronousExecutor)context.getAttribute(
                AsynchronousExecutor.class.getName());
        
        return forEachCallable(executor, new MocaCallableWrapperVisitor<Status> () {
            @Override
            public Status visit(MocaCallableWrapper<?> wrapper)  {
                if (sessionId.equals(wrapper.getSessionId())) {
                    return wrapper.getStatus();
                } else {
                    return null;
                }
            }
        });
    }
    
    private static <T> T forEachCallable(MocaAsynchronousExecutor executor, MocaCallableWrapperVisitor<T> visitor) {
        Map<Long, Callable<?>> tasks = executor.getRunningTasks();
        
        T value = null;
        
        for(Entry<Long, Callable<?>> entry : tasks.entrySet()) {
            Callable<?> callable = entry.getValue();
            
            if (callable instanceof MocaCallableWrapper) {
                MocaCallableWrapper<?> wrapper = (MocaCallableWrapper<?>)callable;
                
                value = visitor.visit(wrapper);
                
                if (value != null) {
                    break;
                }
            }
        }
        
        return value;
    }
    
    private interface MocaCallableWrapperVisitor<T> {
        public T visit(MocaCallableWrapper<?> callable);
    }
    
    private static CallableWrapper<?> getWrapper(Runnable runnable) {
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
}
