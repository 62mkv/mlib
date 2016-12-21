/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.sam.moca.advice;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.sam.moca.MocaContext;
import com.sam.moca.MocaResults;
import com.sam.moca.client.XMLResultsEncoder;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.exec.DefaultServerContext;
import com.sam.moca.server.exec.ServerContext;
import com.sam.moca.server.exec.ServerContextStatus;
import com.sam.moca.server.profile.CommandPath;

public class ServerContextAdministration extends SessionAdministration implements ServerContextAdministrationBean {
    
    public ServerContextAdministration(ServerContext obj, 
            SessionAdministration sessionAdmin) {
        super(obj.getSession());
        _weakObject = new WeakReference<ServerContext>(obj, 
                new ReferenceQueue<ServerContext>());
        _sessionAdmin = sessionAdmin;
    }
    
    /**
     * @param newObj The new server context
     */
    public void setObject(ServerContext newObj) {
        _weakObject = new WeakReference<ServerContext>(newObj, 
                new ReferenceQueue<ServerContext>());
    }
    
    /**
     * @param clientIpAddress The _clientIpAddress to set.
     */
    public void setClientIpAddress(String clientIpAddress) {
        _clientIpAddress = clientIpAddress;
        _sessionAdmin._clientIpAddress = this._clientIpAddress;
    }
    
    /**
     * @param command The command being executed
     */
    public void setLastCommand(String lastCommand) {
        _lastCommandTime = new DateTime();
        _lastCommand = lastCommand;
       
        synchronized (_sessionAdmin) {
            _sessionAdmin._lastCommandTime = this._lastCommandTime;
            _sessionAdmin._lastCommand = this._lastCommand;
        }
    }
    
    /**
     * @param sqlStatement The _lastSqlStatement to set.
     */
    public void setLastSqlStatement(String sqlStatement) {
        _lastSqlStatementTime = new DateTime();
        _lastSqlStatement = sqlStatement;
        
        synchronized (_sessionAdmin) {
            _sessionAdmin._lastSqlStatementTime = this._lastSqlStatementTime;
            _sessionAdmin._lastSqlStatement = this._lastSqlStatement;
        }
    }

    /**
     * @param script The _lastScript to set.
     */
    public void setLastScript(String script) {
        _lastScriptTime = new DateTime();
        _lastScript = script;
        
        synchronized (_sessionAdmin) {
            _sessionAdmin._lastScriptTime = this._lastScriptTime;
            _sessionAdmin._lastScript = this._lastScript;
        }
    }

    // @see com.sam.moca.advice.ServerContextAdministrationMXBean#getStatus()
    @Override
    public ServerContextStatus getStatus() {
        ServerContext context = _weakObject.get();
        
        if (_weakObject.isEnqueued()) {
            ServerContextConfig._logger.debug("Admin Console : Context proxy was GC'd!");
        }
        
        if (context == null) {
            return ServerContextStatus.INACTIVE;
        }
        
        return context.getCurrentStatus();
    }

    // @see com.sam.moca.advice.ServerContextAdministrationMXBean#interrupt()
    @Override
    public void interrupt() {
        
        // First get all the threads and interrupt the last one
        Thread[] threads = getThreads();

        if (threads.length > 0) {
            threads[threads.length - 1].interrupt();
        }
    }

    // @see com.sam.moca.advice.ServerContextAdministrationMXBean#queryDataStack()
    @Override
    public String queryDataStack() {
        ServerContext obj = _weakObject.get();
        
        if (obj != null) {
            MocaContext moca = obj.getComponentContext();
            
            MocaResults res = DefaultServerContext.dumpStack(moca, false);
            
            StringBuilder builder = new StringBuilder();
            
            try {
                XMLResultsEncoder.writeResults(res, builder);
            }
            catch (IOException e) {
                // This should never happen since we are using a
                // String Builder
                e.printStackTrace();
            }
            
            return builder.toString();
        }
        
        return "Context Inactive";
    }
    
    // @see com.sam.moca.advice.ServerContextAdministrationBean#getCurrentCommandPath()
    @Override
    public String getCurrentCommandPath() {
        ServerContext obj = _weakObject.get();
        if (obj != null) {
            CommandPath path = obj.currentCommandPath();
            if (path != null) {
                return path.toString();
            }
        }
        return null;
    }

    // @see com.sam.moca.advice.ServerContextAdministrationMXBean#getSessionThreads()
    @Override
    public ThreadInfo[] getSessionThreads() {
        ThreadMXBean threadInfo = ManagementFactory
                .getThreadMXBean();
        
        Thread[] threads = getThreads();

        if (threads.length > 0) {
            
            long[] threadIds = new long[threads.length];
            for (int i = 0; i < threads.length; ++i) {
                Thread thread = (Thread) threads[i];
                if (thread != null && thread.isAlive()) {
                    threadIds[i] = thread.getId();

                }
            }
            
            // If we have active threads then return the info
            return threadInfo.getThreadInfo(threadIds,
                    Integer.MAX_VALUE);
        }
        
        return new ThreadInfo[0];
    }
    
    /**
     * This will retrieve the threads associated with our server context
     * @return The threads for the context
     */
    private Thread[] getThreads() {
        ServerContext obj = _weakObject.get();
        
        if (obj != null) {
            List<Thread> threads = ServerUtils.getContextThreads(obj);
            return threads.toArray(new Thread[threads.size()]);
        }
        
        return new Thread[0];
    }
    
    // @see com.sam.moca.advice.ServerContextAdministrationBean#getRequestEnvironment()
    @Override
    public Map<String, String> getRequestEnvironment() {
        ServerContext obj = _weakObject.get();
        
        if (obj != null) {
            Map<String, String> map = obj.getRequest().getAllVariables();
            synchronized (map) {
                return new HashMap<String, String>(map);
            }
        }
        
        return Collections.emptyMap();
    }

    // @see com.sam.moca.advice.SessionAdministration#hashCode()
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    // @see com.sam.moca.advice.SessionAdministration#equals(java.lang.Object)
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    private WeakReference<ServerContext> _weakObject;
    private final SessionAdministration _sessionAdmin;
}