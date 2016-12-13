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

package com.redprairie.moca.web.console;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.jgroups.JGroupsAddress;
import org.jgroups.Address;
import org.jgroups.protocols.Executing.Owner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.redprairie.mad.reporters.SystemPropertyInformation;
import com.redprairie.moca.AsynchronousExecutor;
import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.advice.ServerContextAdministrationBean;
import com.redprairie.moca.advice.SessionAdministrationBean;
import com.redprairie.moca.advice.SessionAdministrationManagerBean;
import com.redprairie.moca.async.JGroupsAsynchronousExecutor;
import com.redprairie.moca.async.MocaAsynchronousExecutor;
import com.redprairie.moca.async.MocaExecutionRunnerController;
import com.redprairie.moca.cache.infinispan.InfinispanCacheProvider;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.NoActionClusterRequestException;
import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.cluster.infinispan.InfinispanNode;
import com.redprairie.moca.cluster.manager.ClusterRoleManager;
import com.redprairie.moca.server.InstanceUrl;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.db.DBAdapter;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.legacy.NativeProcessPool;
import com.redprairie.moca.server.session.SessionData;
import com.redprairie.moca.servlet.MocaServerAdministration;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.moca.web.AbstractModel;
import com.redprairie.moca.web.WebResults;

/**
 * Model handlers for the Session specific model requests.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author mlange
 */
public class ConsoleModel extends AbstractModel {
    public ConsoleModel(MocaClusterAdministration clusterAdmin) {
        _registerHandler("getClusterHosts", "getClusterHosts");
        _registerHandler("getClusterRoles", "getClusterRoles");
        _registerHandler("getSessions", "getSessions");
        _registerHandler("restartServer", "restartServer");
        _registerHandler("restartCluster", "restartCluster");
        _registerHandler("interruptSession", "interruptSession");
        _registerHandler("getServerThreadInformation", "getServerThreadInformation");
        _registerHandler("getNativeProcesses", "getNativeProcesses");
        _registerHandler("stopNativeProcess", "stopNativeProcess");
        _registerHandler("getDatabaseConnections", "getDatabaseConnections");
        _registerHandler("getJobs", "getJobs");
        _registerHandler("getUserRole", "getUserRole");
        _registerHandler("removeJob", "removeJob");
        _registerHandler("saveJob", "saveJob");
        _registerHandler("startSchedulingJob", "startSchedulingJob");
        _registerHandler("stopSchedulingJob", "stopSchedulingJob");
        _registerHandler("getTasks", "getTasks");
        _registerHandler("saveTask", "saveTask");
        _registerHandler("removeTask", "removeTask");
        _registerHandler("startTask", "startTask");
        _registerHandler("stopTask", "stopTask");
        _registerHandler("restartTask", "restartTask");
        _registerHandler("getTaskEnvironment", "getTaskEnvironment");
        _registerHandler("saveTaskEnvironment", "saveTaskEnvironment");
        _registerHandler("removeTaskEnvironment", "removeTaskEnvironment");
        _registerHandler("getRegistry", "getRegistry");
        _registerHandler("getComponentLibraries", "getComponentLibraries"); 
        _registerHandler("getEnvironmentVariables", "getEnvironmentVariables");
        _registerHandler("getSystemProperties", "getSystemProperties");
        _registerHandler("getResourceUsage", "getResourceUsage");
        _registerHandler("getCommandProfile", "getCommandProfile");
        _registerHandler("clearCommandProfile", "clearCommandProfile");
        _registerHandler("getLogFiles", "getLogFiles");
        _registerHandler("startTrace", "startTrace");
        _registerHandler("getAsyncQueueInfo", "getAsyncQueueInfo");
        _registerHandler("getAsyncExecutionInfo", "getAsyncExecutionInfo");
        _registerHandler("getClusterAsyncQueueInfo", "getClusterAsyncQueueInfo");
        _registerHandler("getClusterAsyncExecutionInfo", "getClusterAsyncExecutionInfo");
        _registerHandler("handleClusterAsyncRunnerRequest", "handleClusterAsyncRunnerRequest");
        _registerHandler("getSessionKeys", "getSessionKeys");
        _registerHandler("revokeSessionKey", "revokeSessionKey");
        _registerHandler("getJobEnvironment", "getJobEnvironment");
        _registerHandler("saveJobEnvironment", "saveJobEnvironment");
        _registerHandler("removeJobEnvironment", "removeJobEnvironment");
        _registerHandler("getJobHistory", "getJobHistory");
        _registerHandler("getTaskHistory", "getTaskHistory");
        _clusterAdmin = clusterAdmin;
    }
    
    /**
     * Get the list of cluster hosts and URLs.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> getAsyncQueueInfo(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();

        try {
            SystemContext context = ServerUtils.globalContext();
            MocaAsynchronousExecutor executor = (MocaAsynchronousExecutor)context.getAttribute(
                AsynchronousExecutor.class.getName());
            Collection<Runnable> queue = executor.getQueue();
            
            EditableResults res = new SimpleResults();
            
            res.addColumn("queued", MocaType.STRING);
            res.addColumn("order", MocaType.INTEGER);
            
            int i = 0;
            Iterator<Runnable> iter = queue.iterator();
            while (iter.hasNext()) {
                Runnable runnable = iter.next();
                res.addRow();
                res.setStringValue("queued", runnable.toString());
                res.setIntValue("order", ++i);
            }
            
            results.add(res);
        }
        catch (Exception e) {
            e.printStackTrace();
            results.handleException(e);
        }

        return results;
    }
    
    /**
     * Get the list of cluster hosts and URLs.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> getAsyncExecutionInfo(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();

        try {
            SystemContext context = ServerUtils.globalContext();
            MocaAsynchronousExecutor executor = (MocaAsynchronousExecutor)context.getAttribute(
                AsynchronousExecutor.class.getName());
            Map<Long, Callable<?>> tasks = executor.getRunningTasks();
            
            EditableResults res = new SimpleResults();
            
            res.addColumn("task_thread", MocaType.STRING);
            res.addColumn("task_name", MocaType.STRING);
            
            Iterator<Entry<Long, Callable<?>>> iter = tasks.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Long, Callable<?>> entry = iter.next();
                res.addRow();
                res.setStringValue("task_thread", String.valueOf(entry.getKey()));
                res.setStringValue("task_name", String.valueOf(entry.getValue()));
            }
            
            results.add(res);
        }
        catch (Exception e) {
            e.printStackTrace();
            results.handleException(e);
        }

        return results;
    }
    
    /**
     * Get the cluster async executor submissions for this node and where they
     * are running
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> getClusterAsyncQueueInfo(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();

        try {
            SystemContext context = ServerUtils.globalContext();
            JGroupsAsynchronousExecutor executor = (JGroupsAsynchronousExecutor)context.getAttribute(
                "cluster-" + AsynchronousExecutor.class.getName());
            
            EditableResults res = new SimpleResults();
            
            res.addColumn("queued", MocaType.STRING);
            res.addColumn("noderunning", MocaType.STRING);
            
            Map<Node, InstanceUrl> urls = _clusterAdmin.getKnownNodes();
            
            if (executor != null) {
                Map<Owner, Runnable> queue = executor.getRunningRequests();
                Collection<Runnable> allRunnables = executor.getQueue();
                Set<Runnable> runnablesUsed = new HashSet<Runnable>();
                for (Entry<Owner, Runnable> entry : queue.entrySet()) {
                    res.addRow();
                    res.setStringValue("queued", String.valueOf(entry.getValue()));
                    Owner owner = entry.getKey();
                    
                    Address jgroupsAddress = owner.getAddress();
                    
                    // Our map holds the address as an infinispan wrapped node
                    InstanceUrl url = urls.get(new InfinispanNode(
                        new JGroupsAddress(jgroupsAddress)));
                    
                    // The request id is our request id is our generated one and
                    // not the thread id on the server side unfortunately
                    res.setStringValue("noderunning", url != null ? url.toString() : 
                        jgroupsAddress.toString());
                    
                    runnablesUsed.add(entry.getValue());
                }
                
                for (Runnable runnable : allRunnables) {
                    if (!runnablesUsed.contains(runnable)) {
                        res.addRow();
                        res.setStringValue("queued", String.valueOf(runnable));
                    }
                }
                
                results.add(res);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            results.handleException(e);
        }

        return results;
    }
    
    /**
     * Get the list of runners we are running and their tasks
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> getClusterAsyncExecutionInfo(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();

        try {
            SystemContext context = ServerUtils.globalContext();
            MocaExecutionRunnerController controller = (MocaExecutionRunnerController)context.getAttribute(
                MocaExecutionRunnerController.class.getName());
            Map<Thread, Runnable> runningThreads = controller.getRunningThreads();
            
            EditableResults res = new SimpleResults();
            
            res.addColumn("task_thread", MocaType.STRING);
            res.addColumn("task_name", MocaType.STRING);
            
            for (Entry<Thread, Runnable> entry : runningThreads.entrySet()) {
                res.addRow();
                res.setStringValue("task_thread", String.valueOf(entry.getKey().getId()));
                Runnable value = entry.getValue();
                if (value != null) {
                    res.setStringValue("task_name", String.valueOf(value));
                }
                else {
                    res.setStringValue("task_name", "Idle");
                }
            }
            
            results.add(res);
        }
        catch (Exception e) {
            e.printStackTrace();
            results.handleException(e);
        }

        return results;
    }
    
    /**
     * Controls adding a removing runners
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> handleClusterAsyncRunnerRequest(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();

        boolean shouldAdd = Boolean.valueOf(parameters.get("add")[0]);
        try {
            SystemContext context = ServerUtils.globalContext();
            MocaExecutionRunnerController controller = (MocaExecutionRunnerController)context.getAttribute(
                MocaExecutionRunnerController.class.getName());
            
            if (shouldAdd) {
                controller.addRunner();
            }
            else {
                controller.removeRunner();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            results.handleException(e);
        }

        return results;
    }
    
    /**
     * Get the list of cluster hosts and URLs.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> getClusterHosts(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();      
        
        if (_clusterAdmin != null) {
            results.add(_clusterAdmin.getDisplayNamesAndUrls());
        }
        return results;
    }
    
    /**
     * Get the list of cluster hosts and URLs.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> getClusterRoles(Map<String, String[]> parameters) {
        WebResults<Multimap<InstanceUrl, RoleDefinition>> results = 
            new WebResults<Multimap<InstanceUrl, RoleDefinition>>();
            
        ClusterRoleManager manager = ServerUtils.globalAttribute(
            ClusterRoleManager.class);
        
        Multimap<Node, RoleDefinition> multiMap = manager.getClusterRoles();
        Map<Node, InstanceUrl> urls = _clusterAdmin.getKnownNodes();
        
        Multimap<InstanceUrl, RoleDefinition> urlRoleMap = HashMultimap.create();
        
        for (Entry<Node, Collection<RoleDefinition>> entry : multiMap.asMap().entrySet()) {
            urlRoleMap.putAll(urls.get(entry.getKey()), entry.getValue());
        }
        
        results.add(urlRoleMap);
        return results;
    }
    
    /**
     * Method for processing the getSessions mode in the MOCA Web Console.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return String A JSONifyed object containing the list of sessions.
     */
    public WebResults<?> getSessions(Map<String, String[]> parameters) {
        WebResults<SessionInformation> results = new WebResults<SessionInformation>();
        List<SessionInformation> sessionInfos = SessionAdministration.getSessionInformation();
        for (SessionInformation sessionInfo : sessionInfos) {
            results.add(sessionInfo);
        }
        return results;
    }

    /**
     * Retrieve server thread information.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> getServerThreadInformation(Map<String, String[]> parameters) {
        WebResults<ThreadInfo> results = new WebResults<ThreadInfo>();

        try {
            String sessionName = parameters.get("name")[0];
            String threadId = parameters.get("threadId")[0];
            
            // If we don't have a thread id means it is a session
            if (threadId == null || threadId.isEmpty()) {
                return results;
            }

            ThreadInfo threadInfo = ServerThreadInformation.getThreadInfo(
                sessionName, threadId);
            if (threadInfo != null) {
                results.add(threadInfo);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            results.handleException(e);
        }

        return results;
    }
    
    public WebResults<?> getSessionKeys(Map<String, String[]> parameters) {
        WebResults<?> results;
        
        try {
            EmbeddedCacheManager manager = InfinispanCacheProvider
                .getInfinispanCacheManager(ServerUtils.globalContext());
            
            Cache<?, SessionData> cache = manager.getCache("moca-sessions");
            SessionKeyInformation info = new SessionKeyInformation(cache);
            results = info.getSessionKeyInfo();
        }
        catch (Exception e) {
            results = new WebResults<Object>();
            e.printStackTrace();
            results.handleException(e);
        }
        
        return results;
    }
    
    public WebResults<?> revokeSessionKey(Map<String, String[]> parameters) {
        // Build a result set to return.
        WebResults<MocaResults> results = new WebResults<MocaResults>();
        String sessionId = parameters.get("sessionId")[0];
        
        try {
            EmbeddedCacheManager manager = InfinispanCacheProvider
                .getInfinispanCacheManager(ServerUtils.globalContext());
            
            Cache<String, SessionData> cache = manager.getCache("moca-sessions");
            
            cache.remove(sessionId);
            results.setStatus(0);
        }
        catch (Exception e) {
            e.printStackTrace();
            results.handleException(e);
        }
        
        return results;
    }

    /**
     * Restart the server.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the request.
     * @return
     */
    public WebResults<?> restartServer(Map<String, String[]> parameters) {

        // Get the administrator from the system context. 
        SystemContext sys = ServerUtils.globalContext();
        MocaServerAdministration admin = (MocaServerAdministration) sys.getAttribute(MocaServerAdministration.ATTRIBUTE_NAME);
        
        // Actually restart the server.
        admin.restart(true);
        
        // Build a result set to return.
        WebResults<MocaResults> results = new WebResults<MocaResults>();
        results.setStatus(0); 

        return results;
    } 
    
    /**
     * Restart the cluster.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the request.
     * @return
     */
    public WebResults<?> restartCluster(Map<String, String[]> parameters) {
        
        // Actually restart the cluster.
        _clusterAdmin.restartCluster();
        
        // Build a result set to return.
        WebResults<MocaResults> results = new WebResults<MocaResults>();
        results.setStatus(0); 

        return results;
    } 
    
    /**
     * The following method will invoke the interrupt of a session thread.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return String the result of the interrupt operation.
     */
    public WebResults<?> interruptSession(Map<String, String[]> parameters) {
        WebResults<Object> results = new WebResults<Object>();

        String sessionName = parameters.get("name")[0];
        String threadId = parameters.get("threadId")[0];
        
        SessionAdministrationManagerBean manager = ServerUtils.globalAttribute(
            SessionAdministrationManagerBean.class);
        
        SessionAdministrationBean session = manager.getSession(sessionName);
        
        if (session != null) {
            Map<Long, ServerContextAdministrationBean> sessionThreads = 
                manager.getSessionBeans(session);
            
            ServerContextAdministrationBean adminBean = sessionThreads.get(Long.valueOf(threadId));
    
            if (adminBean != null) {
                adminBean.interrupt();
            }
            else {
                results.setStatus(-1);
                results.setMessage("Session thread is no longer present");
            }
        }
        else {
            results.setStatus(-1);
            results.setMessage("Session is no longer present");
        }

        return results;
    }
    
    public WebResults<MocaResults> getNativeProcesses(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();
        
        SystemContext context = ServerUtils.globalContext();
        ServerContextFactory factory = (ServerContextFactory)context.getAttribute(
                ServerContextFactory.class.getName());
        
        try {
            NativeProcessPool pool = factory.getNativePool();
            NativeProcessInformation info = new NativeProcessInformation(pool);
            results.add(info.getNativeProcesses());
        }
        catch (ConsoleException e) {
            e.printStackTrace();
            results.handleException(e);
        }
        catch (NullPointerException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
  
        return results;
    }
    
    public WebResults<Object> stopNativeProcess(Map<String, String[]> parameters) {
        WebResults<Object> results = new WebResults<Object>();
           
        SystemContext context = ServerUtils.globalContext();
        ServerContextFactory factory = (ServerContextFactory)context.getAttribute(
                ServerContextFactory.class.getName());   
        
        try {
            NativeProcessPool pool = factory.getNativePool();
            NativeProcessInformation info = new NativeProcessInformation(pool);
            String mocaProcess = parameters.get("moca_prc")[0];
            info.stopNativeProcess(mocaProcess);
        }
        catch (ConsoleException e) {
            e.printStackTrace();
            results.handleException(e);
        }
        catch (NullPointerException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        
        return results;
    }
    
    public WebResults<MocaResults> getDatabaseConnections(Map<String, String[]> parameters) { 
        WebResults<MocaResults> results = new WebResults<MocaResults>();
    
        SystemContext context = ServerUtils.globalContext();
        ServerContextFactory factory = (ServerContextFactory)context.getAttribute(
                ServerContextFactory.class.getName());         
        
        try {
            DBAdapter dbAdapter = factory.getDBAdapter();
            DatabaseConnectionInformation databaseConnectionInformation = new DatabaseConnectionInformation(dbAdapter);
            results.add(databaseConnectionInformation.getDatabaseConnections());
        }
        catch (ConsoleException e) {
            e.printStackTrace();
            results.handleException(e);
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
    
        return results;
    }
      
    /**
     * Retrieve jobs.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the request.
     * @return
     */
    public WebResults<?> getJobs(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();

        try {
            JobInformation jobInformation = new JobInformation();
            results.add(jobInformation.getJobDefinitions());
        }
        catch (Exception e) {
            e.printStackTrace();
            results.handleException(e);
        }

        return results;
    }

    /**
     * Delete a job.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> removeJob(Map<String, String[]> parameters) throws MocaException {
        WebResults<MocaResults> results = new WebResults<MocaResults>();
        
        ServerContext ctx = ServerUtils.getCurrentContext();
        
        try {
            final Map<String,String> args = new HashMap<String, String>(1);
            args.put("job_id",  parameters.get("job_id")[0]);
            
            ctx.executeCommand("remove job where job_id = @job_id", args, false);
            ctx.commit();
            results.setStatus(0); 
        }
        catch (IllegalArgumentException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        catch (IllegalStateException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        catch (MocaException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        finally {
            ctx.close();
            ServerUtils.removeCurrentContext(); 
        }

        return results;
    }
    
    /**
     * Add a job.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> saveJob(Map<String, String[]> parameters) throws MocaException {
        WebResults<MocaResults> results = new WebResults<MocaResults>();
        
        String action = parameters.get("action")[0];
        
        ServerContext ctx = ServerUtils.getCurrentContext();
        
        final Map<String,Object> args = new HashMap<String, Object>(13);    
        args.put("job_id", parameters.get("job_id")[0]);
        args.put("name", parameters.get("name")[0]);
        args.put("grp_nam", parameters.get("grp_nam")[0]);
        args.put("role_id", parameters.get("role_id")[0]);
        args.put("command", parameters.get("command")[0]);
        args.put("enabled", parameters.get("enabled")[0].matches("true"));
        args.put("overlap", parameters.get("overlap")[0].matches("true"));
        args.put("type", parameters.get("type")[0]);
        args.put("start_delay", parameters.get("start_delay")[0]);
        args.put("log_file", parameters.get("log_file")[0]);
        args.put("trace_level", parameters.get("trace_level")[0]);
        args.put("schedule", parameters.get("schedule")[0]);
        args.put("timer", parameters.get("timer")[0]);
        
        String cmd = "";
        
        if (action.equals("add")){
            cmd = "add job where ";
        }
        if (action.equals("edit")) {
            cmd = "change job where ";
        }
        
        cmd += "job_id = @job_id and " + 
                "role_id = @role_id and " + 
                "name = @name and " +
                "type = @type and " +
                "command = @command and " +
                "enabled = @enabled and " +
                "overlap = @overlap and " +
                "log_file = @log_file and " +
                "trace_level = @trace_level and " +
                "start_delay = @start_delay and " +
                "schedule = @schedule and " +
                "timer = @timer and " +
                "grp_nam = @grp_nam";
        
        try {
            ctx.executeCommand(cmd, args, false);
            ctx.commit();
            results.setStatus(0); 
        }
        catch (IllegalArgumentException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        catch (IllegalStateException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        catch (MocaException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        finally {
            ctx.close();
            ServerUtils.removeCurrentContext(); 
        }

        return results;
    }

    /**
     * Enable a job.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the request.
     * @return
     */
    public WebResults<?> startSchedulingJob(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();

        String jobIdList = parameters.get("job_id_list")[0];
        List<String> jobIds = new ArrayList<String>();
        
        try {
            // Enable each job in the list.
            for (String jobId : jobIdList.split(",")) {
                jobIds.add(jobId);
            }
            
            _clusterAdmin.scheduleJobOnCluster(jobIds, 
                    getInstanceUrlsFromParameters(parameters));
            results.setStatus(0); 
        }
        catch (NoActionClusterRequestException e) {
            results.setStatus(-1);
            String str = jobIds.size() > 1 ? "jobs" : "job"; 
            results.setMessage(String.format("Could not start scheduling %s %s.%n" +
                    "Another node may have already started scheduling the specified %s.",
                    str, jobIds, str));
        }
        catch (Exception e) {
            results.setStatus(-1); 
            results.setMessage(e.getMessage());
        }
        
        return results;
    } 

    /**
     * Disable a job.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the request.
     * @return
     */
    public WebResults<?> stopSchedulingJob(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();
     
        String jobIdList = parameters.get("job_id_list")[0];
        List<String> jobIds = new ArrayList<String>();
        
        try {
            // Disable each job in the list.
            for (String jobId : jobIdList.split(",")) {
                jobIds.add(jobId);
            }
            
            _clusterAdmin.unscheduleJobOnCluster(jobIds,
                    getInstanceUrlsFromParameters(parameters));
            results.setStatus(0); 
        }
        catch (NoActionClusterRequestException e) {
            results.setStatus(-1);
            String str = jobIds.size() > 1 ? "jobs" : "job";
            results.setMessage(String.format("Could not stop scheduling %s %s.%n" +
                    "Another node may have already stopped the specified %s from being scheduled.",
                    str, jobIds, str));
        }
        catch (Exception e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }

        return results;
    }
    
    /**
     * Retrieve tasks.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> getTasks(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();
        
        try {
            TaskInformation taskInformation = new TaskInformation();
            results.add(taskInformation.getTaskDefinitions());
        }
        catch (Exception e) {
            e.printStackTrace();
            results.handleException(e);
        }

        return results;
    }
    
    /**
     * Add a task.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> saveTask(Map<String, String[]> parameters) throws MocaException {
        WebResults<MocaResults> results = new WebResults<MocaResults>();
        
        String action = parameters.get("action")[0];
        
        ServerContext ctx = ServerUtils.getCurrentContext();
        
        final Map<String,Object> args = new HashMap<String, Object>(13);
        args.put("task_id", parameters.get("task_id")[0]);
        args.put("role_id", parameters.get("role_id")[0]);
        args.put("name", parameters.get("name")[0]);
        args.put("task_typ", parameters.get("task_typ")[0]);
        args.put("cmd_line", parameters.get("cmd_line")[0]);
        args.put("run_dir", parameters.get("run_dir")[0]);
        args.put("log_file", parameters.get("log_file")[0]);
        args.put("trace_level", parameters.get("trace_level")[0]);
        args.put("role_id", parameters.get("role_id")[0]);
        args.put("restart", parameters.get("restart")[0].matches("true"));
        args.put("auto_start", parameters.get("auto_start")[0].matches("true"));
        args.put("start_delay", parameters.get("start_delay")[0]);
        args.put("grp_nam", parameters.get("grp_nam")[0]);
        
        String cmd = "";
        
        if (action.equals("add")){
            cmd = "add task where ";
        }
        if (action.equals("edit")) {
            cmd = "change task where ";
        }
        
        cmd += "task_id = @task_id and " + 
                "role_id = @role_id and " + 
                "name = @name and " +
                "task_typ = @task_typ and " +
                "cmd_line = @cmd_line and " +
                "run_dir = @run_dir and " +
                "log_file = @log_file and " +
                "trace_level = @trace_level and " +
                "role_id = @role_id and " +
                "restart = @restart and " +
                "auto_start = @auto_start and " +
                "start_delay = @start_delay and " +
                "grp_nam = @grp_nam";
        
        try {
            ctx.executeCommand(cmd, args, false);
            ctx.commit();
            results.setStatus(0); 
        }
        catch (IllegalArgumentException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        catch (IllegalStateException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        catch (MocaException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        finally {
            ctx.close();
            ServerUtils.removeCurrentContext(); 
        }

        return results;
    }
    
    /**
     * Delete a task.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> removeTask(Map<String, String[]> parameters) throws MocaException {
        WebResults<MocaResults> results = new WebResults<MocaResults>();
        
        ServerContext ctx = ServerUtils.getCurrentContext();
        
        try {
            final Map<String, Object> args = new HashMap<String, Object>();
            args.put("task_id", parameters.get("task_id")[0]);
            
            ctx.executeCommand("remove task where task_id = @task_id", args, false);
            ctx.commit();
            results.setStatus(0); 
        }
        catch (IllegalArgumentException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        catch (IllegalStateException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        catch (MocaException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        finally {
            ctx.close();
            ServerUtils.removeCurrentContext();
        }

        return results;
    }
    
    /**
     * Start a task.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the request.
     * @return
     */
    public WebResults<?> startTask(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();
     
        String taskIdList = parameters.get("task_id_list")[0];
        List<String> taskIds = new ArrayList<String>();
        
        try {
            // Start each task in the list
            for (String taskId : taskIdList.split(",")) {
                taskIds.add(taskId);
            }
            
            _clusterAdmin.startTaskOnCluster(taskIds,
                    getInstanceUrlsFromParameters(parameters));
            results.setStatus(0); 
        }
        catch (NoActionClusterRequestException e) {
            results.setStatus(-1);
            String str = taskIds.size() > 1 ? "tasks" : "task";
            results.setMessage(String.format("Could not start %s %s.%n" +
                    "Another node may already started the specified %s.",
                    str, taskIds, str));
        }
        catch (Exception e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }

        return results;
    } 

    /**
     * Stop a task.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the request.
     * @return
     */
    public WebResults<?> stopTask(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();

        String taskIdList = parameters.get("task_id_list")[0];
        List<String> taskIds = new ArrayList<String>();
        
        try {
            // Stop each task in the list
            for (String taskId : taskIdList.split(",")) {
                taskIds.add(taskId);
            }
            
            _clusterAdmin.stopTaskOnCluster(taskIds,
                    getInstanceUrlsFromParameters(parameters));
            results.setStatus(0); 
        }
        catch (NoActionClusterRequestException e) {
            results.setStatus(-1);
            String str = taskIds.size() > 1 ? "tasks" : "task";
            results.setMessage(String.format("Could not stop %s %s.%n" +
                    "Another node may have already stopped the specified %s.",
                    str, taskIds, str));
        }
        catch (Exception e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }

        return results;
    }  
    
    /**
     * Restart a task.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the request.
     * @return
     */
    public WebResults<?> restartTask(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();

        String taskIdList = parameters.get("task_id_list")[0];
        
        try {
            // Restart each task in the list
            List<String> taskIds = new ArrayList<String>();
            for (String taskId : taskIdList.split(",")) {
                taskIds.add(taskId);
            }          
            
            _clusterAdmin.restartTaskOnCluster(taskIds,
                    getInstanceUrlsFromParameters(parameters));
            results.setStatus(0); 
        }
        catch (Exception e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }

        return results;
    }
    
    public WebResults<?> getTaskEnvironment(Map<String, String[]> parameters) throws MocaException {
        WebResults<MocaResults> results = new WebResults<MocaResults>();
        ServerContext context = ServerUtils.getCurrentContext();
        
        String taskID = parameters.get("task_id")[0];   
        if (taskID == null || taskID.isEmpty()) {
            return results;
        }
        
        String cmd = "list task env where task_id = @task_id";
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("task_id", taskID);

        try {
            MocaResults commandResults = context.executeCommand(cmd, args, true);

            if (commandResults.getRowCount() > 0) {
                results.add(commandResults);
            }

            results.setStatus(0);
            context.commit();
        }
        catch (NotFoundException e) {
            //OK for no rows to be returned
            results.setStatus(0);
        }
        catch (MocaException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        finally {
            context.close();
            ServerUtils.removeCurrentContext();
        }

        return results;
    }
    
    public WebResults<?> getJobHistory(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();
        ServerContext context = ServerUtils.getCurrentContext();
        
        final String[] jobIds = parameters.get("job_id");
        final String[] startRow = parameters.get("start");
        final String[] rowLimit = parameters.get("limit");
        final String[] sort = parameters.get("sort");
        Map<String, Object> args = new HashMap<String, Object>();
        
        String cmd = "list job executions where start_row = @start_row and row_limit = @row_limit and calculate_total = @calculate_total";
        args.put("start_row", startRow != null ? startRow[0] : 0);
        args.put("row_limit", rowLimit != null ? rowLimit[0] : 200);
        args.put("calculate_total", true);
        if (jobIds != null) {
            cmd += " and job_id = @job_id";
            args.put("job_id", jobIds[0]);
        }
        
        try {
            // do server side sorting
            // if client passes sort parameter but format is wrong we should fail
            if (sort != null && sort.length > 0) {
                final JSONArray a = new JSONArray(sort[0]);
                final JSONObject o = (JSONObject) a.get(0);
                final String sortCol = o.getString("property");
                final String sortDir = o.getString("direction");
                cmd += " and orderByClause = @orderByClause";
                args.put("orderByClause", sortCol + " " + sortDir);
            }
            
            MocaResults commandResults = context.executeCommand(cmd, args, true);

            if (commandResults.getRowCount() > 0) {
                results.add(commandResults);
            }
            
            int total = MocaUtils.getPagedTotalRowCount(commandResults);
            results.setTotalRows(total);

            results.setStatus(0);
            context.commit();
        }
        catch (NotFoundException e) {
            //OK for no rows to be returned
            results.setStatus(0);
        }
        catch (JSONException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        catch (MocaException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }

        return results;
    }
    
    public WebResults<?> getTaskHistory(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();
        ServerContext context = ServerUtils.getCurrentContext();
        
        final String[] taskIds = parameters.get("task_id");
        final String[] startRow = parameters.get("start");
        final String[] rowLimit = parameters.get("limit");
        final String[] sort = parameters.get("sort");
        Map<String, Object> args = new HashMap<String, Object>();
        
        String cmd = "list task executions where start_row = @start_row and row_limit = @row_limit and calculate_total = @calculate_total";
        args.put("start_row", startRow != null ? startRow[0] : 0);
        args.put("row_limit", rowLimit != null ? rowLimit[0] : 200);
        args.put("calculate_total", true);
        if (taskIds != null) {
            cmd += " and task_id = @task_id";
            args.put("task_id", taskIds[0]);
        }
        
        try {
            // do server side sorting
            // if client passes sort parameter but format is wrong we should fail
            if (sort != null && sort.length > 0) {
                final JSONArray a = new JSONArray(sort[0]);
                final JSONObject o = (JSONObject) a.get(0);
                final String sortCol = o.getString("property");
                final String sortDir = o.getString("direction");
                cmd += " and orderByClause = @orderByClause";
                args.put("orderByClause", sortCol + " " + sortDir);
            }
            
            MocaResults commandResults = context.executeCommand(cmd, args, true);

            if (commandResults.getRowCount() > 0) {
                results.add(commandResults);
            }
            
            int total = MocaUtils.getPagedTotalRowCount(commandResults);
            results.setTotalRows(total);

            results.setStatus(0);
            context.commit();
        }
        catch (NotFoundException e) {
            //OK for no rows to be returned
            results.setStatus(0);
        }
        catch (JSONException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        catch (MocaException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }

        return results;
    }
    
    public WebResults<?> saveTaskEnvironment(Map<String, String[]> parameters) throws MocaException {
        WebResults<MocaResults> results = new WebResults<MocaResults>();
        ServerContext context = ServerUtils.getCurrentContext();
        
        String cmd = "";
        
        String action = parameters.get("action")[0];
        if (action.equals("add")){
            cmd = "add task env where task_id = @task_id and name = @name and value = @value";
        }
        else if (action.equals("edit")) {
            cmd = "change task env where task_id = @task_id and name = @name and value = @value";
        }
        else {
            results.setStatus(-1);
            results.setMessage("Cannot save Task Environment using action: '" + action + "'");
            return results;
        }
        
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("task_id", parameters.get("task_id")[0]);
        args.put("name", parameters.get("name")[0]);
        args.put("value", parameters.get("value")[0]);
        
        try {
            context.executeCommand(cmd, args, true);
            context.commit();
            results.setStatus(0); 
        }
        catch (IllegalArgumentException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        catch (IllegalStateException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        catch (MocaException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        finally {
            context.close();
            ServerUtils.removeCurrentContext();
        }

        return results;
    }
    
    public WebResults<?> removeTaskEnvironment(Map<String, String[]> parameters) throws MocaException {
        WebResults<MocaResults> results = new WebResults<MocaResults>(); 
        ServerContext context = ServerUtils.getCurrentContext();
        
        String cmd = "remove task env where task_id = @task_id and name = @name";
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("task_id", parameters.get("task_id")[0]);
        args.put("name", parameters.get("name")[0]);
        
        try {
            context.executeCommand(cmd, args, true);
            context.commit(); 
            results.setStatus(0); 
        }
        catch (IllegalArgumentException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        catch (IllegalStateException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        catch (MocaException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        finally {
            context.close();
            ServerUtils.removeCurrentContext();
        }

        return results;
    }
    
    /**
     * Retrieve registry file.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> getRegistry(Map<String, String[]> parameters) {
        WebResults<String> results = new WebResults<String>();

        String contents = RegistryInformation.getRegistryContents();
        results.add(contents);

        return results;
    }

    /**
     * Retrieve component libraries.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> getComponentLibraries(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();

        try {
            results.add(ComponentLibraryInformation.getComponentLibraries());
        }
        catch (Exception e) {
            e.printStackTrace();
            results.handleException(e);
        }

        return results;
    }

    /**
     * Retrieve environment variables.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> getEnvironmentVariables(Map<String, String[]> parameters) {
        WebResults<Map<String, String>> results = new WebResults<Map<String, String>>();

        EnvironmentVariableInformation environmentVariables = new EnvironmentVariableInformation();
        results.add(environmentVariables.getEnvironmentVariables());

        return results;
    }

    /**
     * Retrieve system properties.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> getSystemProperties(Map<String, String[]> parameters) {
        WebResults<Map<String, String>> results = new WebResults<Map<String, String>>();

        SystemPropertyInformation props = new SystemPropertyInformation();
        results.add(props.getSystemProperties());

        return results;
    }

    /**
     * Retrieve resource usage.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> getResourceUsage(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();

        try {
            results.add(ResourceInformation.getResourceInformation());
        }
        catch (Exception e) {
            e.printStackTrace();
            results.handleException(e);
        }

        return results;
    }

    /**
     * Retrieve command profile.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> getCommandProfile(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();

        try {
            results.add(CommandProfileInformation.getCommandProfile());
        }
        catch (Exception e) {
            e.printStackTrace();
            results.handleException(e);
        }

        return results;
    }
    
    /**
     * Clears the command profile.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public WebResults<?> clearCommandProfile(Map<String, String[]> parameters) {
        WebResults<Object> results = new WebResults<Object>();

        try {
            CommandProfileInformation.clearCommandProfile();
        }
        catch (Exception e) {
            e.printStackTrace();
            results.handleException(e);
        }

        return results;
    }

    /**
     * Retrieve the list of all files under LESDIR/log.
     *
     * @return
     */
    public WebResults<?> getLogFiles(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();
        
        try {
            results.add(LogFileInformation.getLogFiles());
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            results.handleException(e);
        }
        catch (IOException e) {
            e.printStackTrace();
            results.handleException(e);
        }
        
        return results;
    }
    
    public WebResults<?> startTrace(Map<String, String[]> parameters) {
        WebResults<MocaResults> results = new WebResults<MocaResults>();
        
        String name = parameters.get("name")[0];
        String filename = parameters.get("filename")[0];
        String level = parameters.get("level")[0];
        
        try {
            SessionAdministrationManagerBean manager = 
                ServerUtils.globalAttribute(SessionAdministrationManagerBean.class);
            
            SessionAdministrationBean session = manager.getSession(name);
            if (session != null) {
                
                // Null filename should close out logging.
                if (filename == null || filename.trim().isEmpty()) {
                    session.startTrace(null, null);
                }
                else {
                    String pathname = "$LESDIR" + File.separator + "log" + File.separator + filename;
                    session.startTrace(pathname, level);
                }
            }
            else {
                results.setStatus(-1);
                results.setMessage("Session is no longer present");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            results.handleException(e);
        }
        
        return results;
    }
    
    /**
     * Get the job environment.
     * @param parameters Parameters for call. Parameters must contain job_id.
     * @return WebResults of call.
     * @throws MocaException failure to execute underlying command.
     */
    public WebResults<?> getJobEnvironment(Map<String, String[]> parameters) throws MocaException {
        WebResults<MocaResults> results = new WebResults<MocaResults>();
        ServerContext ctx = ServerUtils.getCurrentContext();
        
        try {
            final Map<String,String> args = new HashMap<String, String>(1);
            args.put("job_id", parameters.get("job_id")[0]);
            
            MocaResults commandResults = ctx.executeCommand("list job env where job_id = @job_id", args, false);

            if (commandResults.getRowCount() > 0) {
                results.add(commandResults);
            }

            results.setStatus(0);
        }
        catch (NotFoundException e) {
            //OK for no rows to be returned
        }
        catch (MocaException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        finally {
            ctx.close();
            ServerUtils.removeCurrentContext(); 
        }

        return results;
    }
    
    public WebResults<?> getUserRole(Map<String, String[]> parameters) throws MocaException {
        WebResults<String> userResult = new WebResults<String>();
        return userResult;
    }
    
    /**
     * Update job environment.
     * @param parameters Parameters for call. Parameters must contain job_id, name, action and value.
     * The "action" parameter value can be one of: "edit", "add".
     * @return WebResults of call.
     * @throws MocaException failure to execute underlying command.
     */
    public WebResults<?> saveJobEnvironment(Map<String, String[]> parameters) throws MocaException {
        WebResults<MocaResults> results = new WebResults<MocaResults>();
        ServerContext ctx = ServerUtils.getCurrentContext();
        
        try {
            String action = parameters.get("action")[0];
            
            String cmd = "";
            if ("add".equals(action)) {
                cmd = "add job env where job_id = @job_id and name = @name and value = @value";
            }
            else if ("edit".equals(action)) {
                cmd = "change job env where job_id = @job_id and name = @name and value = @value";
            }
            else {
                results.setStatus(-1);
                results.setMessage("Cannot save Job Environment using action: '" + action + "'");
                return results;
            }
            
            final Map<String,String> args = new HashMap<String, String>(3);
            args.put("job_id", parameters.get("job_id")[0]);
            args.put("name", parameters.get("name")[0]);
            args.put("value", parameters.get("value")[0]);
                 
            ctx.executeCommand(cmd, args, false);
            ctx.commit();
            results.setStatus(0); 
        }
        catch (MocaException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        finally {
            ctx.close();
            ServerUtils.removeCurrentContext(); 
        }

        return results;
    }
    
    /**
     * Remove job environment value.
     * @param parameters Parameters for call. Parameters must contain job_id and name.
     * @return WebResults of call.
     * @throws MocaException failure to execute underlying command.
     */
    public WebResults<?> removeJobEnvironment(Map<String, String[]> parameters) throws MocaException {
        WebResults<MocaResults> results = new WebResults<MocaResults>();
        ServerContext ctx = ServerUtils.getCurrentContext();
        
        try {
            final Map<String,String> args = new HashMap<String, String>(2);
            args.put("job_id", parameters.get("job_id")[0]);
            args.put("name", parameters.get("name")[0]);
            
            ctx.executeCommand("remove job env where job_id = @job_id and name = @name", args, false);
            ctx.commit();
            results.setStatus(0);
        }
        catch (MocaException e) {
            results.setStatus(-1);
            results.setMessage(e.getMessage());
        }
        finally {
            ctx.close();
            ServerUtils.removeCurrentContext(); 
        }

        return results;
    }
    
    private List<String> getInstanceUrlsFromParameters(Map<String, String[]> parameters) {
        List<String> instanceUrls = new ArrayList<String>();
        if (parameters.get("node_id_list") != null) {
            String instanceUrlList = parameters.get("node_id_list")[0];
            for (String instanceUrl : instanceUrlList.split(",")) {
                instanceUrls.add(instanceUrl);
            }              
        }         

        return instanceUrls;
    }
    
    private final MocaClusterAdministration _clusterAdmin;
    
    final static Logger _logger = LogManager.getLogger(ConsoleModel.class);
}
