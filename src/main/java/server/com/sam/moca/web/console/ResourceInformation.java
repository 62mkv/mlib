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

package com.sam.moca.web.console;

import com.sam.moca.AsynchronousExecutor;
import com.sam.moca.EditableResults;
import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.SimpleResults;
import com.sam.moca.async.MocaAsynchronousExecutor;
import com.sam.moca.server.ServerContextFactory;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.db.DBAdapter;
import com.sam.moca.server.db.jdbc.ConnectionPoolStatistics;
import com.sam.moca.server.exec.SystemContext;
import com.sam.moca.server.legacy.NativeProcessPool;
import com.sam.moca.servlet.WebSessionManager;

public class ResourceInformation {

    private ResourceInformation() {
    }

    public static MocaResults getResourceInformation() throws MocaException {
        
        EditableResults res = new SimpleResults();
        
        res.addColumn("current_heap_used", MocaType.DOUBLE);
        res.addColumn("current_heap_size", MocaType.DOUBLE);
        res.addColumn("max_heap_size", MocaType.DOUBLE);
        res.addColumn("delta_current_heap_size", MocaType.DOUBLE);
        res.addColumn("delta_max_heap_size", MocaType.DOUBLE);
        
        res.addColumn("current_sessions", MocaType.INTEGER);
        res.addColumn("peak_sessions", MocaType.INTEGER);
        res.addColumn("max_sessions", MocaType.INTEGER);
        res.addColumn("delta_peak_sessions", MocaType.INTEGER);
        res.addColumn("delta_max_sessions", MocaType.INTEGER);
        
        res.addColumn("current_native_processes", MocaType.INTEGER);
        res.addColumn("peak_native_processes", MocaType.INTEGER);
        res.addColumn("max_native_processes", MocaType.INTEGER);
        res.addColumn("delta_peak_processes", MocaType.INTEGER);
        res.addColumn("delta_max_processes", MocaType.INTEGER);

        res.addColumn("current_db_connections", MocaType.INTEGER);
        res.addColumn("peak_db_connections", MocaType.INTEGER);
        res.addColumn("max_db_connections", MocaType.INTEGER);
        res.addColumn("delta_peak_db_connections", MocaType.INTEGER);
        res.addColumn("delta_max_db_connections", MocaType.INTEGER);
        
        res.addColumn("current_async_threads_used", MocaType.INTEGER);
        res.addColumn("current_async_threads_size", MocaType.INTEGER);
        res.addColumn("max_async_threads_size", MocaType.INTEGER);
        res.addColumn("delta_current_async_threads_size", MocaType.INTEGER);
        res.addColumn("delta_max_async_threads_size", MocaType.INTEGER);
            
        res.addRow();
         
        //
        // Memory        
        //
        
        Runtime vm = Runtime.getRuntime();

        double vmFreeMemory = truncate(vm.freeMemory() / (double) 1024 / 1024);
        double vmTotalMemory = truncate(vm.totalMemory() / (double) 1024 / 1024);
        double vmMaxMemory = truncate(vm.maxMemory() / (double) 1024 / 1024);
        double vmUsedMemory = truncate(vmTotalMemory - vmFreeMemory);

        res.setDoubleValue("current_heap_used", vmUsedMemory);
        res.setDoubleValue("current_heap_size", vmTotalMemory);
        res.setDoubleValue("max_heap_size", vmMaxMemory);
        res.setDoubleValue("delta_current_heap_size", vmTotalMemory - vmUsedMemory);
        res.setDoubleValue("delta_max_heap_size", vmMaxMemory - vmTotalMemory);
        
        //
        // Sessions
        //
        
        WebSessionManager sessionManager = (WebSessionManager) ServerUtils.globalContext().getAttribute(WebSessionManager.class.getName());

        if (sessionManager == null) {
            throw new IllegalStateException(
                "WebSessionManager is null. Cannot generate ResourceInformation."
                        + " This generally happens because the WebSessionManager"
                        + " isn't configured in a process based server task.");
        }
        
        int sessions = sessionManager.getSessionCount();
        int peakSessions = sessionManager.getPeakSessionCount();
        int maxSessions = sessionManager.getMaxSessions();
        
        res.setIntValue("current_sessions", sessions);   
        res.setIntValue("peak_sessions", peakSessions);
        res.setIntValue("max_sessions", maxSessions);
        res.setIntValue("delta_peak_sessions", peakSessions - sessions);
        res.setIntValue("delta_max_sessions", maxSessions - peakSessions);
        
        SystemContext context = ServerUtils.globalContext();
        ServerContextFactory factory = (ServerContextFactory)context.getAttribute(
                ServerContextFactory.class.getName());         
        
        //
        // Native Process Pool
        //
        
        NativeProcessPool nativeProcessPool = factory.getNativePool();
        int processes = nativeProcessPool.getSize();
        int peakProcesses = nativeProcessPool.getPeakSize();
        int maxProcesses = nativeProcessPool.getMaximumSize();
        
        res.setIntValue("current_native_processes", processes);   
        res.setIntValue("peak_native_processes", peakProcesses);
        res.setIntValue("max_native_processes", maxProcesses);
        res.setIntValue("delta_peak_processes", peakProcesses - processes);
        res.setIntValue("delta_max_processes", maxProcesses - peakProcesses);
        
        //
        // Database Connections
        //
          
        DBAdapter dbAdapter = factory.getDBAdapter();
        ConnectionPoolStatistics dbPoolStats = dbAdapter.getConnectionPoolStatistics();
        int connections = dbPoolStats.getCurrentConnections();
        int peakConnections = dbPoolStats.getPeakConnections();
        Integer nullableMaxConnections = dbPoolStats.getMaxConnections();
        int maxConnections = nullableMaxConnections == null ? Integer.MAX_VALUE : nullableMaxConnections;
        
        res.setIntValue("current_db_connections", connections);
        res.setIntValue("peak_db_connections", peakConnections);
        res.setIntValue("max_db_connections", maxConnections);
        res.setIntValue("delta_peak_db_connections", peakConnections - connections);
        res.setIntValue("delta_max_db_connections", maxConnections - peakConnections);
        
        // Asynchronous Executor
        
        MocaAsynchronousExecutor executor = (MocaAsynchronousExecutor)context.getAttribute(
            AsynchronousExecutor.class.getName());
        
        int currentThreadCount = executor.getCurrentThreadCount();
        int activeThreadCount = executor.getActiveThreadCount();
        int maxThreadCount = executor.getMaxThreadCount();
        
        res.setIntValue("current_async_threads_used", activeThreadCount);
        res.setIntValue("current_async_threads_size", currentThreadCount);
        res.setIntValue("max_async_threads_size", maxThreadCount);
        res.setIntValue("delta_current_async_threads_size", currentThreadCount - activeThreadCount);
        res.setIntValue("delta_max_async_threads_size", maxThreadCount - currentThreadCount);
        
        return res;
    }

    private static double truncate(double value) {
        long asLong = (long) value * 100;      
        return (double) asLong / 100;
    }
    
}