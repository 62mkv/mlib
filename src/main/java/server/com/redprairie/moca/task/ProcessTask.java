/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.task;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.redprairie.mad.server.MadServerStart;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaRuntimeException;
import com.redprairie.moca.server.log.TraceState;
import com.redprairie.moca.task.dao.TaskExecutionDAO;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.util.CommandLineParser;
import com.redprairie.util.ProcessWatcher;
import com.redprairie.util.StringReplacer.ReplacementStrategy;
import com.redprairie.util.VarStringReplacer;

/**
 * Subclass of <code>RunningTask</code> that handles process-oriented tasks.
 * The <code>runtTask</code> method of this class will create a process, watch
 * it, and respawn if necessary.
 * 
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class ProcessTask extends RunningTask {

    public ProcessTask(TaskDefinition task, TaskExecution taskExec, 
                       TaskExecutionDAO taskExecDao, Map<String, String> env, 
                       int startDelay) {
        super(task, taskExec, taskExecDao, env, startDelay);
    }

    @Override
    protected void runTask() {
        // Create a process builder for this task. 
        ProcessBuilder builder = new ProcessBuilder();

        final Map<String, String> env = builder.environment();

        final String taskId = _task.getTaskId();

        // Remove commonly used path variables with different case.
        env.remove("Path");
        env.remove("path");

        // Put the PATH variable back, this time in upper case
        env.put("PATH", System.getenv("PATH"));

        // If the path variable is reset in the system-configured
        // environment, it'll be in upper case.
        env.putAll(_environment);

        // We tell the process it's id through an environment variable
        env.put("MOCA_TASK_ID", taskId);

        // Put the MAD_PATH variable in the path if it's running 
        int madPort = MadServerStart.getPort();
        if (madPort != 0) {
            env.put("MAD_PORT", Integer.toString(madPort));
        } 

        String traceLevel = _task.getTraceLevel();

        TraceState traceState = new TraceState(taskId);

        if (traceLevel != null && !traceLevel.trim().isEmpty()) {
            env.put("MOCA_TRACE_LEVEL", traceLevel);
            traceState.setLevel(traceLevel);
            }

        // Do variable replacement on the various values in the task definition
        VarStringReplacer envLookup = new VarStringReplacer(new ReplacementStrategy() {
            @Override
            public String lookup(String key) {
                return env.get(key);
            }
        });

        String runDirectory = envLookup.translate(_task.getRunDirectory());
        if (runDirectory != null) {
            builder.directory(new File(runDirectory));
        }

        String cmdLine = envLookup.translate(_task.getCmdLine());

        List<String> splitCmdLine = CommandLineParser.split(cmdLine); 

        builder.command(splitCmdLine);
        builder.redirectErrorStream(true);

        // Handle redirected output (optional).
        boolean runTask = true;
            if (_task.getLogFile() != null) {
            String logFileTranslated = envLookup.translate(
                _task.getLogFile());
            traceState.configureLogFileName(logFileTranslated);
            // TODO: figure out how layout will work for this
            // TODO: do we need the console env variable still?
                env.put("MOCA_LOG_CONSOLE", "true");
            }

        traceState.applyTraceStateToThread();
        try {
        // If a task start delay is configured, sleep for the allotted time
        int startDelay = getStartDelay();
        if (startDelay != 0) {
            try {
                _logger.info(MocaUtils.concat("Quarantining task ", taskId, " for ", startDelay, " seconds...")); 
                Thread.sleep(startDelay * 1000L);
            }
            catch (InterruptedException e) {
                _logger.info("Interrupted during initial delay -- aborting");
                _logger.info("Task " + taskId + " startup aborted");
                runTask = false;
            }
        }

        // Set the start date now
        boolean executeBeforeSleep = true;
        _taskExec.setStartDate(new Date());
        try {
            _taskExecDao.save(_taskExec);
            MocaUtils.currentContext().commit();
        }
        catch (MocaRuntimeException e) {
            boolean initialSaveSqlException = false;
            Throwable npe = e;
            while ((npe = npe.getCause()) != null) {
                if (npe instanceof SQLException) {
                    _logger.warn("Couldn't update start date", e);
                    initialSaveSqlException = true;
                    executeBeforeSleep = false; // go straight into sleep section
                    break;
                }
            }
            if (!initialSaveSqlException) throw e;
        }
        catch (MocaException e1) {
            executeBeforeSleep = false; // go straight into sleep/restart section
            _logger.warn("There was a problem committing task status update", e1);
        }

        // We have to handle process restart
        runSleepLoop:
        while (runTask) {
            if (executeBeforeSleep){
                // If the hook wasn't setup let us link it here and set it up to
                // run upon JVM close
                if (_hook == null) {
                    _hook = new ProcessShutdownHook(taskId);
                    Runtime.getRuntime().addShutdownHook(_hook);
                }

                Process p = null;
                Thread taskThread = null;
                try {
                    p = builder.start();
                    // Now we update the hook with the newly created process
                    _hook.setProcess(p);

                    TaskWatcher watcher = new TaskWatcher(p);

                    taskThread = new Thread(watcher, "Task Watcher: " + taskId);
                    taskThread.setDaemon(true);
                    // We set an uncaught exception handler
                    taskThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                        @Override
                        public void uncaughtException(Thread t, Throwable e) {
                            if (e instanceof MocaInterruptedException) {
                                _logger.warn("Process Thread for " + taskId + 
                                        " was interrupted.");
                            }
                            else {
                                _logger.warn("Process Thread for " + taskId + 
                                    " encountered an exception : " + e.getMessage(), e);
                            }
                        }

                    });

                    // We run the watcher in another thread so we can respond to
                    // interrupt.
                    taskThread.start();

                    // If clean shutdown was set then we have to use stdin to
                    // cleanly shut it down.
                    String cleanShutdown = _environment.get(CLEAN_SHUTDOWN_ENV);
                    if (cleanShutdown != null
                            && (cleanShutdown.equalsIgnoreCase("yes") || 
                                    cleanShutdown.equalsIgnoreCase("true") || 
                                    cleanShutdown.equals("1"))) {
                        _taskOutput.put(taskId, p.getOutputStream());
                    }

                    int returnStatus = p.waitFor();

                    _logger.info("Task " + taskId + " exited with exit status " + 
                            returnStatus);
                    _taskExec.setStatus("Process Task finished with " + 
                            returnStatus + " return status");
                }
                catch (InterruptedException e) {
                    // Set interrupt status so outer if can see it.
                    Thread.currentThread().interrupt();
                    runTask = false;
                }
                catch (IOException e) {
                    _logger.error("Unable to start process " + cmdLine + ": " + e);
                    _logger.error("Task " + taskId + " startup aborted");
                    runTask = false;
                    String status = "Task exited with exception: " + e.getMessage();
                    if (status.length() > 2000) {
                        status = status.substring(0, 2000);
                    }
                    _taskExec.setStatus(status);
                }
                finally {
                    // Interrupt the task watcher
                    if (taskThread != null) {
                        taskThread.interrupt();
                    }
                    // We kill the process as well since the task watcher may
                    // not respond to interrupt.
                    stopProcess(p, taskId);
                }

                if (Thread.interrupted()) {
                    _logger.info("Task " + taskId + " thread interrupted -- aborting");
                    runTask = false;

                    _taskExec.setStatus("Process Task was stopped by interrupt");
                }

                _taskExec.setEndDate(new Date());
                try {
                    _taskExecDao.save(_taskExec);
                    MocaUtils.currentContext().commit();
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
                    _logger.info("Restarting task " + taskId);
                    incrementRestartCounter();
                }

                try {
                    final int restartDelay = 5;
                    _logger.info(MocaUtils.concat("Quarantining task ", taskId, " for ", restartDelay, " seconds...")); 
                    Thread.sleep(restartDelay * 1000L);

                    // Have to clear some stuff out and repersist
                    _taskExec.setStartDate(new Date());
                    _taskExec.setEndDate(null);
                    _taskExec.setStatus(null);
                    _taskExec.setStartCause("RESTART");
                    try {
                        _taskExecDao.save(_taskExec);
                        MocaUtils.currentContext().commit();
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
                    catch (MocaException e) {
                        _logger.warn("There was a problem committing task status update", e);
                    }
                }
                catch (InterruptedException e) {
                    _logger.info("Task restart interrupted while waiting on " +
                            "restart duration -- aborting");
                    runTask = false;
                }
            }
            // we execute next iteration
            executeBeforeSleep = true;
        }
        }
        finally {
            traceState.closeLogging();
        }

        _logger.info("Task " + taskId + " stopped");

    }

    //
    // Implementation
    //
    private class TaskWatcher extends ProcessWatcher {

        public TaskWatcher(Process p) {
            super(p);
        }

        @Override
        protected void handleOutput(String line) {
                _logger.debug(MocaUtils.concat("Task ", _task.getTaskId(), ": ", line));
            }
        }

    private static class ProcessShutdownHook extends Thread {

        ProcessShutdownHook(String taskId) {
            _taskId = taskId;
        }

        public void setProcess(Process process) {
            _process = process;
        }

        // @see java.lang.Thread#run()
        @Override
        public void run() {
            stopProcess(_process, _taskId);
        }

        Process _process;
        private final String _taskId;
    }

    private static void stopProcess(Process process, String taskId) {
        OutputStream stream = _taskOutput.remove(taskId);

        boolean force = false;
        try {
            if (stream != null) {
                // Closing output should shut it down.
                stream.close();
            }
            else {
                force = true;
            }
        }
        catch (IOException e) {
            force = true;
        }

        if (force && process != null) {
            process.destroy();
        }
    }

    private static final ConcurrentMap<String, OutputStream> _taskOutput = 
            new ConcurrentHashMap<String, OutputStream>();

    private ProcessShutdownHook _hook;

    public static final String CLEAN_SHUTDOWN_ENV = "MOCA_CLEAN_SHUTDOWN";
}
