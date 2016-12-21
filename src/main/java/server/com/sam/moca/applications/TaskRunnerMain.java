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

package com.sam.moca.applications;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.logging.log4j.LogManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.sam.moca.MocaRegistry;
import com.sam.moca.server.InstanceUrl;
import com.sam.moca.server.ServerContextFactory;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.SpringTools;
import com.sam.moca.server.SystemConfigurationException;
import com.sam.moca.server.exec.ServerContext;
import com.sam.moca.server.exec.SystemContext;
import com.sam.moca.server.log.LoggingConfigurator;
import com.sam.moca.task.TaskConfig;
import com.sam.moca.task.TaskDefinition;
import com.sam.moca.task.TaskManager;
import com.sam.moca.task.dao.TaskDefinitionDAO;
import com.sam.moca.util.DaemonThreadFactory;
import com.sam.moca.util.Options;
import com.sam.moca.util.OptionsException;
import com.sam.util.AbstractInvocationHandler;
import com.sam.util.ClassUtils;
import com.sam.util.StringReplacer.ReplacementStrategy;
import com.sam.util.VarStringReplacer;

/**
 * This mainline is used to run a task individually.  This will work with any
 * thread based task.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
@Configuration
@Import(TaskConfig.class)
public class TaskRunnerMain {
    
    private static void showUsage() {
        System.out.println("Usage: runtask [ -h ] TaskId [task arguments]\n\n" +
                        "\tThis will only run thread-based tasks.\n\n" +
                        "\tAttempting to run a process-based or daemon-based task will display\n" +
                        "\ta command line that can be used to run the task manually.\n\n" +
                        "\tTasks arguments provided above are appended to the arguments\n" +
                        "\talready defined for the task.\n");		
    }
    
    /**
     * The only options available are for -h for help message.  You are required
     * to pass a task id as well that is valid in the system.  Anything after
     * the first argument will be passed to the task.
     * @param args
     * @throws SystemConfigurationException 
     */
    public static void main(String[] args) throws SystemConfigurationException {
        Options opts = null;
        try {
            opts = Options.parse("h", args);
        }
        catch (OptionsException e) {
            System.err.println("Invalid option: " + e.getMessage());
            showUsage();
            System.exit(1);
        }
        
        if (opts.isSet('h')) {
            showUsage();
            System.exit(0);
        }
        
        String[] remainingArgs = opts.getRemainingArgs();
        
        if (remainingArgs.length == 0) {
            System.err.println("There was no task id provided");
            showUsage();
            System.exit(1);
        }
        
        // Setup Logging
        LoggingConfigurator.configure();
        
        SystemContext systemCtx = ServerUtils.globalContext();
        ServerContextFactory factory = null;
        try {
            factory = ServerUtils.setupServletContext(systemCtx);
        }
        catch (SystemConfigurationException e) {
            System.err.println("There was a problem setting up the instance " + 
                    e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        
        // Setup the InstanceUrl...
        String hostName = null;
        try {
            hostName = InetAddress.getLocalHost().getCanonicalHostName();
        }
        catch (UnknownHostException e) {
            LogManager.getLogger(LogManager.ROOT_LOGGER_NAME).error(
                "There was a problem acquiring fully " + "qualified host name!", e);
        }

        int port = Integer.parseInt(systemCtx.getConfigurationElement(
            MocaRegistry.REGKEY_SERVER_PORT, MocaRegistry.REGKEY_SERVER_PORT_DEFAULT));

        InstanceUrl url = new InstanceUrl(false, hostName, port);
        systemCtx.putAttribute(InstanceUrl.class.getName(), url);
        
        ServerContext context = factory.newContext(null, null);
        
        ServerUtils.setCurrentContext(context);
        
        // Now we can grab the first one since we know there is at least one.
        String taskId = remainingArgs[0];
        
        AnnotationConfigApplicationContext appCtx = new AnnotationConfigApplicationContext(
            TaskConfig.class);
        
        TaskDefinitionDAO dao = appCtx.getBean(TaskDefinitionDAO.class);
        
        TaskDefinition task = dao.read(taskId);
        
        if (task == null) {
            System.err.println("Task [" + taskId + "] was not found in the system!");
            System.exit(1);
        }
        
        String cmdLine = task.getCmdLine();
        
        // If we were given other arguments that means we append the task
        // arguments below us
        if (remainingArgs.length > 1) {
            // First put the real command line
            StringBuilder builder = new StringBuilder(cmdLine);
           
            // Then we append all the arguments with a space in between them
            // We also have to surround them in quotes in case if the user
            // passed in something in quotes since we stripped them
            for (int i = 1; i < remainingArgs.length; ++i) {
                char quote = '"';
                
                String arg = remainingArgs[i];
                // If our string contains a double quote then use single quotes
                if (arg.indexOf('"') != -1) {
                    quote = '\'';
                }
                
                builder.append(' ');
                builder.append(quote);
                builder.append(remainingArgs[i]);
                builder.append(quote);
            }
            
            cmdLine = builder.toString();
        }
        
        // We have to use cglib since TaskDefinition doesn't have an 
        // interface, since we want to override the enable and cmd line possibly
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(TaskDefinition.class);
        enhancer.setCallback(new TaskWrapper(task, cmdLine));
        
        task = (TaskDefinition)enhancer.create();
        
        // We proxy the dao so it only will return the 1 task.
        _proxyDao = (TaskDefinitionDAO)Proxy.newProxyInstance(
                ClassUtils.getClassLoader(), 
                new Class[] {TaskDefinitionDAO.class}, 
                new SingleTaskWrapper(task));
        
        Map<String, Object> singletons = new HashMap<String, Object>();
        singletons.put("systemContext", systemCtx);
        singletons.put("serverContextFactory", factory);
        ApplicationContext parent = SpringTools
            .getContextForPreinstantiatedSingletons(singletons);
        
        AnnotationConfigApplicationContext cookedAppCtx = 
            SpringTools.getContextWithParent(parent, TaskRunnerMain.class);
        
        final TaskManager manager = cookedAppCtx.getBean(TaskManager.class);
        
        // We have to do this check after initializing all the stuff above
        // to get a reference to the TaskManager to the environment
        if (!task.getType().equals(TaskDefinition.THREAD_TASK)) {
            System.out.println("RUNTASK will only run thread-based tasks.\n");
            System.out.println("Execute the following command to run the task manually:\n");
            final Map<String, String> env = manager.getEnvironment();
            env.putAll(task.getEnvironment());
            VarStringReplacer envLookup = new VarStringReplacer(new ReplacementStrategy() {
                @Override
                public String lookup(String key) {
                    return env.get(key);
                }
            });
            System.out.println(envLookup.translate(cmdLine));
            System.exit(0);
        }
        
        // Now we setup a runtime hook to make sure the manager is properly
        // shutdown if we are killed
        Runtime.getRuntime().addShutdownHook(new Thread() {

            // @see java.lang.Thread#run()
            @Override
            public void run() {
                try {
                    manager.stop();
                }
                catch (IllegalStateException e) {
                    // Ignore the error, it is thrown if it was stopped, so we
                    // don't care about it.
                }
            }
            
        });
        
        // Then we start the manager and let it go
        manager.start(false);
        
        // We wait forever for the task to stop if it does.
        try {
            /// We should be running by at least 1 second.
            if (_startLatch.await(1, TimeUnit.SECONDS)) {
                System.out.println("This process may not die if task is set to " +
                		"auto restart or it never finishes.");
                _doneLatch.await();
                System.out.println("Task finished processing.");
            }
            else {
                System.out.println("There was no task submitted to be ran, the " +
                		"task must have been disabled");
            }
        }
        catch (InterruptedException e) {
            // If we were interrupted then just stop the manager and die
            System.err.println("We were interrupted while waiting for task to finish " +
            		"-- aborting.");
        }
        finally {
            manager.stop();
        }
        
        // Need to do a system exit just in case if clustering is enabled
        // since they have non daemon threads
        System.exit(0);
    }
    
    private static class SingleThreadNotifierExecutor extends ThreadPoolExecutor {

        /**
         * We force this to be a single thread executor, since we will only
         * ever have 1 task.
         * @param corePoolSize
         * @param maximumPoolSize
         * @param keepAliveTime
         * @param unit
         * @param workQueue
         */
        public SingleThreadNotifierExecutor() {
            super(1, 1, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
            // We set the thread as daemon, since someone could interrupt our
            // task and we want to make sure to shutdown.
            super.setThreadFactory(new DaemonThreadFactory("Task Runner", false));
        }
        
        // @see java.util.concurrent.ThreadPoolExecutor#beforeExecute(java.lang.Thread, java.lang.Runnable)
        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            super.beforeExecute(t, r);
            // We notify the main that we indeed started
            _startLatch.countDown();
        }

        // @see java.util.concurrent.ThreadPoolExecutor#afterExecute(java.lang.Runnable, java.lang.Throwable)
        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            // Now we notify that we were done
            _doneLatch.countDown();
        }
        
    }
    
    private static class TaskWrapper implements MethodInterceptor {
        public TaskWrapper(TaskDefinition task, String cmdLine) {
            _task = task;
            _cmdLine = cmdLine;
        }
        
        // @see net.sf.cglib.proxy.MethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], net.sf.cglib.proxy.MethodProxy)
        @Override
        public Object intercept(Object obj, Method method, Object[] args,
                MethodProxy proxy) throws Throwable {
            String methodName = method.getName();
            
            // We want to enable the task no matter what
            if (methodName.equals("isAutoStart")) {
                return true;
            }
            else if (methodName.equals("getCmdLine")) {
                return _cmdLine;
            }
            
            try {
                Object returnValue = method.invoke(_task, args);
                return returnValue;
            }
            catch(InvocationTargetException e) {
                throw e.getCause();
            }
        }
        
        private final TaskDefinition _task;
        private final String _cmdLine;
    }
    
    private static class SingleTaskWrapper extends AbstractInvocationHandler {
        public SingleTaskWrapper(TaskDefinition task) {
            _task = task;
        }
        // @see com.sam.util.AbstractInvocationHandler#proxyInvoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
        @Override
        protected Object proxyInvoke(Object proxy, Method m, Object[] args)
                throws Throwable {
            String methodName = m.getName();
            
            if (methodName.equals("readAllTasksForAllAndRoles") || 
                    methodName.equals("readAll")) {
                return Arrays.asList(_task);
            }
            else {
                throw new RuntimeException("Unexpected method dispatched: " + 
                        m + ".  This is a bug.");
            }
        }
        
        private final TaskDefinition _task;
    }
    
    private static final CountDownLatch _startLatch = new CountDownLatch(1);
    private static final CountDownLatch _doneLatch = new CountDownLatch(1);
    
    @Bean
    public int defaultTaskSyncTime() {
        return 0;
    }
    
    @Bean
    public ExecutorService defaultExecutorService() {
        return new SingleThreadNotifierExecutor();
    }
    
    @Bean
    public TaskDefinitionDAO taskDefinitionDAO() {
        return _proxyDao;
    }
    
    private static TaskDefinitionDAO _proxyDao;
}
