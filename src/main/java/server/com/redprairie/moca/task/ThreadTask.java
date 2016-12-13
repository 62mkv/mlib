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

package com.redprairie.moca.task;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.mad.client.MadMetrics;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaRuntimeException;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.LocalSessionContext;
import com.redprairie.moca.server.exec.RequestContext;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SessionContext;
import com.redprairie.moca.server.exec.SessionType;
import com.redprairie.moca.server.log.TraceState;
import com.redprairie.moca.server.session.SessionToken;
import com.redprairie.moca.task.dao.TaskExecutionDAO;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.util.CommandLineParser;
import com.redprairie.util.StringReplacer.ReplacementStrategy;
import com.redprairie.util.VarStringReplacer;

/**
 * Subclass of <code>RunningTask</code> that handles thread-oriented tasks. The
 * <code>runTask</code> method of this class will instantiate a Runnable class
 * (defined in the TaskDefinition), run it in the current thread, and will do it
 * again if the task definition requires it.
 * 
 * Copyright (c) 2016 Sam Corporation All Rights Reserved
 * 
 * @author dinksett
 */
public class ThreadTask extends RunningTask {

    public ThreadTask(TaskDefinition task, TaskExecution taskExec,
                      TaskExecutionDAO taskExecDao, Map<String, String> env,
                      ServerContextFactory contextFactory, int startDelay) {
        super(task, taskExec, taskExecDao, env, startDelay);
        _contextFactory = contextFactory;
    }

    @Override
    protected void runTask() {
        // Create a new request and session object
        RequestContext req = new RequestContext();
        SessionContext session = new LocalSessionContext("task-" + _task.getTaskId(), 
            SessionType.TASK, _environment);

        // Authenticate our session
        session.setSessionToken(new SessionToken("task-" + _task.getTaskId()));

        // Go ahead and create a server context
        final ServerContext ctx = _contextFactory.newContext(req, session);

        // Set up task ID 
        ctx.putSystemVariable("MOCA_TASK_ID", _task.getTaskId());

        // Do variable replacement on the various values in the task definition
        VarStringReplacer envLookup = new VarStringReplacer(new ReplacementStrategy() {
            @Override
            public String lookup(String key) {
                return ctx.getSystemVariable(key);
            }
        });

            // Handle redirected output (optional).
            String logFile = _task.getLogFile();
            TraceState traceState = session.getTraceState();

            if (logFile != null) {
            traceState.configureLogFileName(envLookup.translate(logFile));
            }

            // We always want to set the trace level
            String traceLevel = _task.getTraceLevel();

            // Handle tracing, if configured
            if (traceLevel == null || traceLevel.isEmpty()) {
                traceLevel = _environment.get("MOCA_TRACE_LEVEL");
            }
            if (traceLevel != null && !traceLevel.isEmpty()) {
                traceState.setLevel(traceLevel);
            }
        traceState.applyTraceStateToThread();

        // We have to handle process restart
        try {
        // Set up the new context on this thread.
        ServerUtils.setCurrentContext(ctx);

        boolean runTask = true;

        // If a task start delay is configured, sleep for the allotted time
        int startDelay = getStartDelay();
        if (startDelay != 0) {
            try {
                _logger.info(MocaUtils.concat("Quarantining task ", _task.getTaskId(), " for ", startDelay, " seconds..."));
                Thread.sleep(startDelay * 1000L);
            }
            catch (InterruptedException e) {
                _logger.info("Interrupted during initial delay -- aborting");
                _logger.info("Task " + _task.getTaskId() + " startup aborted");
                runTask = false;
            }
        }

        // Set the start date now
        boolean executeBeforeSleep = true;
        _taskExec.setStartDate(new Date());
        try {
            _taskExecDao.save(_taskExec);
            ctx.commit();
        }
        catch (MocaRuntimeException e) {
            boolean initialSaveSqlException = false;
            Throwable npe = e;
            while ((npe = npe.getCause()) != null) {
                if (npe instanceof SQLException) {
                    _logger.warn("Couldn't update start date", e);
                    initialSaveSqlException = true;
                    executeBeforeSleep = false; // go right into sleep/restart section
                    break;
                }
            }
            if (!initialSaveSqlException) throw e;
        }
        catch (MocaException e1) {
            executeBeforeSleep = false;  // go right into sleep/restart section
            _logger.warn("There was a problem committing task status update", e1);
        }

            runSleepLoop:
            while (runTask) {
                if (executeBeforeSleep) {
                    Runnable actualTask = instantiateRunnableFromCommandLine(ctx,
                        _task.getCmdLine()); 

                    try {
                        // We are our own thread, so we run the process watcher in-line, rather
                        // than spawning another one.
                        actualTask.run();

                        _logger.info("Task " + _task.getTaskId() + " finished executing normally");
                        _taskExec.setStatus("Task finished executing normally");
                    }
                    catch (MocaInterruptedException e) {
                        // If we found an interrupt then just interrupt ourselves
                        // so the outer if statement will be true
                        Thread.currentThread().interrupt();
                    }
                    catch (Exception e) {
                        _logger.info("Task " + _task.getTaskId() + " exited throwing an exception ", e);
                        String status = "Task exited with exception: " + e;
                        if (status.length() > 2000) {
                            status = status.substring(0, 2000);
                        }
                        _taskExec.setStatus(status);
                    }

                    if (Thread.interrupted()) {
                        _logger.info("Task thread interrupted -- aborting");
                        runTask = false;
                        _taskExec.setStatus("Thread Task was stopped by interrupt");
                    }

                    // Make sure their transactions is done, we didn't do anything
                    // before so a rollback is probably best.
                    try {
                        ctx.rollback();
                    }
                    catch (MocaException e) {
                        _logger.warn("There was a problem with transaction rollback", e);
                    }

                    _taskExec.setEndDate(new Date());
                    try {
                        _taskExecDao.save(_taskExec);
                        ctx.commit();
                    }
                    catch (MocaRuntimeException e) {
                        boolean endDateSqlException = false;
                        Throwable npe = e;
                        while ((npe = npe.getCause()) != null) {
                            if (npe instanceof SQLException) {
                                // if we can't write end date go back to sleep 
                                _logger.warn("Couldn't update end date", e);
                                endDateSqlException = true;
                                break;
                            }
                        }
                        if (!endDateSqlException) throw e;
                    }
                    catch (MocaException e1) {
                        _logger.warn("There was a problem committing task status update", e1);
                    }
                }

                // OK, if we haven't run into any reason to not restart this task,
                // consider restarting it.
                if (runTask && !_task.isRestart()) runTask = false;

                if (runTask) {
                    // if task hasn't executed yet and is reaching this section because
                    // it errored and needs to sleep, then we don't want to count it as a restart
                    if (executeBeforeSleep) {
                        _logger.info("Restarting task " + _task.getTaskId());
                        incrementRestartCounter();
                    }

                    try {
                        final int restartDelay = 5;                     
                        _logger.info(MocaUtils.concat("Quarantining task ", _task.getTaskId(), " for ", restartDelay, " seconds..."));
                        Thread.sleep(restartDelay * 1000L);

                        // Have to clear some stuff out and repersist
                        _taskExec.setStartDate(new Date());
                        _taskExec.setStartCause("RESTART");
                        _taskExec.setEndDate(null);
                        _taskExec.setStatus(null);
                        try {
                            _taskExecDao.save(_taskExec);
                            ctx.commit();
                        }
                        catch (MocaRuntimeException e) {
                            boolean startDateSqlException = false;
                            Throwable npe = e;
                            while ((npe = npe.getCause()) != null) {
                                if (npe instanceof SQLException) {
                                    // if we can't write start date go back to sleep
                                    // we continue the loop without committing
                                    _logger.warn("Couldn't update start date", e);
                                    startDateSqlException = true;
                                    executeBeforeSleep = false;
                                    continue runSleepLoop;
                                }
                            }
                            if (!startDateSqlException) throw e;
                        }
                        catch (MocaException e1) {
                            _logger.warn("There was a problem committing task status update", e1);
                        }
                    }
                    catch (InterruptedException e) {
                        _logger.info("Task restart interrupted while waiting on " +
                                "restart duration -- aborting");
                        runTask = false;
                    }
                }
                // we can execute next iteration
                executeBeforeSleep = true;
            }

            _logger.info("Task " + _task.getTaskId() + " stopped");
        }
        catch (Exception e) {
            _logger.error("Unexpected Error Running Task: " + e, e);
        }
        finally {
            _logger.info("Closing context for task " + _task.getTaskId());
            ctx.close();
            ServerUtils.removeCurrentContext();
            traceState.closeLogging();
        }
    }

    static Runnable instantiateRunnableFromCommandLine(CharSequence seq) {
        return instantiateRunnableFromCommandLine(null, seq);
    }

    static Runnable instantiateRunnableFromCommandLine(ServerContext context, 
                                                       CharSequence seq) {
        List<String> arguments = CommandLineParser.split(seq);

        if (arguments.size() == 0) {
            throw new IllegalArgumentException("No class name was provided for task.");
        }

        String className = arguments.remove(0);

        Class<? extends Runnable> classObj;
        synchronized(_classMap) {
            classObj = _classMap.get(className);

            if (classObj == null) {
                try {
                    Class<?> theirClass = Class.forName(className);

                    // If this implements Runnable than we can use it
                    if (Runnable.class.isAssignableFrom(theirClass)) {
                        classObj = theirClass.asSubclass(Runnable.class);
                    }
                    else {
                        throw new IllegalArgumentException("class " + className +
                            " does not extend/implement " + Runnable.class.getName());
                    }
                }
                catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException(
                        "Class " + className + " not found", e);
                }

                _classMap.put(className, classObj);
            }
        }

        if (context != null) {
            // First we want to try and find a constructor with parameters for
            // MocaContext, MadFactory, and string array.
            Constructor<? extends Runnable> mocaFactoryStringArrayArgConst = null;
            try {
                mocaFactoryStringArrayArgConst = classObj.getConstructor(MocaContext.class, 
                    MadFactory.class, String[].class);
            }
            catch (SecurityException e) {
                _logger.debug("We could not access the MOCA conext, MAD Factory, and String" +
                        " array constructor on class " + classObj, e);
            }
            catch (NoSuchMethodException e) {
                _logger.debug(MocaUtils.concat("There is no Moca context, Mad Factory, and" + 
                        " String array constructor on class ", classObj));
            }

            if (mocaFactoryStringArrayArgConst != null) {
                _logger.debug(MocaUtils.concat("Using MOCA context, Mad Factory, and String" + "" +
                        " Array constructor for class ", className, " ", arguments));      
                return instantiate(mocaFactoryStringArrayArgConst, className, context.getComponentContext(),
                    MadMetrics.getFactory(), (Object) arguments.toArray(new String[arguments.size()]));
            }

            // Secondly we try the MocaContext & string array constructor
            Constructor<? extends Runnable> mocaStringArrayArgConst = null;
            try {
                mocaStringArrayArgConst = classObj.getConstructor(MocaContext.class, 
                    String[].class);
            }
            catch (SecurityException e) {
                _logger.debug("We could not access the MOCA and String array constructor " +
                        "on class " + classObj, e);
            }
            catch (NoSuchMethodException e) {
                _logger.debug(MocaUtils.concat("There is no Moca and String array constructor on class ", classObj));
            }

            if (mocaStringArrayArgConst != null) {
                _logger.debug(MocaUtils.concat("Using MOCA and String Array constructor for class ", className, " ", arguments));
                return instantiate(mocaStringArrayArgConst, className, context.getComponentContext(),
                    (Object) arguments.toArray(new String[arguments.size()]));
            }
        }

        // Then we try the MadFactory and string array constructor
        Constructor<? extends Runnable> factoryStringArrayArgConst = null;
        try {
            factoryStringArrayArgConst = classObj.getConstructor(MadFactory.class, String[].class);
        }
        catch (SecurityException e) {
            _logger.debug("We could not access the Mad Factory and String" +
                    " array constructor on class " + classObj, e);
        }
        catch (NoSuchMethodException e) {
            _logger.debug(MocaUtils.concat("There is no Mad Factory and String" +
                    " array constructor on class ", classObj));
        }

        if (factoryStringArrayArgConst != null) {
            _logger.debug(MocaUtils.concat("Using Mad Factory and String Array" +
                    " constructor for class ", className, " ", arguments));
            return instantiate(factoryStringArrayArgConst, className,
                MadMetrics.getFactory(),
                (Object) arguments.toArray(new String[arguments.size()]));
        }

        // Then we try the string array constructor
        Constructor<? extends Runnable> stringArrayArgConst = null;
        try {
            stringArrayArgConst = classObj.getConstructor(String[].class);
        }
        catch (SecurityException e) {
            _logger.debug("We could not access the String array constructor " +
                    "on class " + classObj, e);
        }
        catch (NoSuchMethodException e) {
            _logger.debug(MocaUtils.concat("There is no String array constructor on class ", classObj));
        }

        if (stringArrayArgConst != null) {
            _logger.debug(MocaUtils.concat("Using String Array constructor for class ", className, " ", arguments));
            return instantiate(stringArrayArgConst, className, (Object)
                arguments.toArray(new String[arguments.size()]));
        }

        if (context != null) {
            // Next try the MocaContext and MadFactory arguments constructor
            Constructor<? extends Runnable> mocaFactoryArgConst = null;
            try {
                mocaFactoryArgConst = classObj.getConstructor(MocaContext.class, MadFactory.class);
            }
            catch (SecurityException e) {
                _logger.debug("We could not access the MocaContext and MadFactory arg constructor " +
                        "on class " + classObj, e);
            }
            catch (NoSuchMethodException e) {
                _logger.debug(MocaUtils.concat("There is no MocaContext and MadFactory " +
                        "arg constructor on class ", classObj));
            }

            if (mocaFactoryArgConst != null) {
                _logger.debug(MocaUtils.concat("Using MocaContext and MadFactory " +
                        "arg constructor for class ", className));
                return instantiate(mocaFactoryArgConst, className,
                    context.getComponentContext(), MadMetrics.getFactory());
            }

            // Next We try the MocaContext no arg constructor
            Constructor<? extends Runnable> mocaArgConst = null;
            try {
                mocaArgConst = classObj.getConstructor(MocaContext.class);
            }
            catch (SecurityException e) {
                _logger.debug("We could not access the MocaContext arg constructor " +
                        "on class " + classObj, e);
            }
            catch (NoSuchMethodException e) {
                _logger.debug(MocaUtils.concat("There is no MocaContext arg constructor on class ", classObj));
            }

            if (mocaArgConst != null) {
                _logger.debug(MocaUtils.concat("Using MocaContext arg constructor for class ", className));
                return instantiate(mocaArgConst, className, context.getComponentContext());
            }
        }

        // Try the single MadFactory argument constructor
        Constructor<? extends Runnable> factoryArgConst = null;
        try {
            factoryArgConst = classObj.getConstructor(MadFactory.class);
        }
        catch (SecurityException e) {
            _logger.debug("We could not access the MadFactory constructor " +
                    "on class " + classObj, e);
        }
        catch (NoSuchMethodException e) {
            _logger.debug(MocaUtils.concat("There is no MadFactory constructor on class ", classObj));
        }

        if (factoryArgConst != null) {
            _logger.debug(MocaUtils.concat("Using MadFactory constructor for class ", className, " ", arguments));
            return instantiate(factoryArgConst, className, MadMetrics.getFactory());
        }

        // Lastly we use the no arg constructor
        Constructor<? extends Runnable> noArgConst = null;
        try {
            noArgConst = classObj.getConstructor();
        }
        catch (SecurityException e) {
            _logger.debug("We could not access the empty arg constructor " +
                    "on class " + classObj, e);
        }
        catch (NoSuchMethodException e) {
            _logger.debug(MocaUtils.concat("There is no empty arg constructor on class ", classObj));
        }

        if (noArgConst != null) {
            _logger.debug(MocaUtils.concat("Using No Arg constructor for class ", className));
            return instantiate(noArgConst, className);
        }

        throw new IllegalArgumentException(
            "No valid constructors were found for class " + className);
    }

    private static Runnable instantiate(Constructor<? extends Runnable> constructor, 
                                        String className, Object... arguments) {
        try {
            return constructor.newInstance(arguments);
        }
        catch (IllegalArgumentException e) {
            // This would be a bug in the code
            throw new RuntimeException("This is a bug in the ThreadTask code", e);
        }
        catch (InstantiationException e) {
            throw new IllegalArgumentException(
                "cannot instantiate class " + className, e);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException(
                "cannot access constructor on class " + className, e);
        }
        catch (InvocationTargetException e) {
            throw new IllegalArgumentException("constructor on class " + className +
                " threw exception" + e.getCause().getMessage(), e.getCause());
        }
    }

    private final static Map<String, Class<? extends Runnable>> _classMap = 
            new HashMap<String, Class<? extends Runnable>>();
    private final ServerContextFactory _contextFactory;
}
