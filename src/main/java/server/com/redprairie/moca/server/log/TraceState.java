/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.redprairie.moca.server.log;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;

import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.log.appender.MocaAppender;

/**
 * A class that contains all tracing information for a specific session
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TraceState {

    /**
     * Creates a new {@link TraceState} class.
     * 
     * @param sessionId The session ID
     */
    public TraceState(String sessionId) {
        this._sessionId = sessionId;
    }

    /**
     * Indicates if file tracing is currently enabled
     * 
     * @return Returns a value indicating if file tracing is enabled.
     */
    public boolean isEnabled() {
        return _logFileName != null && !_logFileName.isEmpty();
    }

    /**
     * Gets the file name used to create a log
     * 
     * @return Returns the log Filename.
     */
    public String getLogFileName() {
        return _logFileName;
    }

    /**
     * Sets the log file name. This configures logging
     * to append by default, see {@link #configureLogFileName(String, boolean)}
     * to set that behavior.
     * 
     * @param logFileName The logFileName to set.
     */
    public void configureLogFileName(String logFileName) {
        configureLogFileName(logFileName, true);
    }
    
    /**
     * Sets the log file name and whether logging should
     * append to the file or not.
     * @param logFileName The log file name
     * @param append Whether to append to the log file or not
     *
     */
    public void configureLogFileName(String logFileName, boolean append) {
        if (logFileName != null && !logFileName.isEmpty()) {
            // Only have to update if the names don't match
            if (!logFileName.equals(_logFileName)) {
                // If we had a previous log file then decrement the ref count
                if (_logFileName != null && !_logFileName.isEmpty()) {
                    decrementFileCounter(_logFileName);
                }
                incrementFileCounter(logFileName);
            }
            
            // Delete the file if it already exists and we're not appending
            if (!append) {
                File file = new File(logFileName);
                if (file.exists() && !file.delete()) {
                    _logger.warn(
                        "Unable to delete the existing log file when configuring the log for file: {}",
                        file);
                }
            }
        }
        else if (_logFileName != null && !_logFileName.isEmpty()) {
            decrementFileCounter(_logFileName);
        }
        this._logFileName = logFileName;

    }

    /**
     * Gets the session Id for the MDC context.
     * 
     * @return Returns the sessionId for this trace state.
     */
    public String getSessionId() {
        return _sessionId;
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return _sessionId;
    }

    /**
     * Closes down the active log writer and resets to the default settings
     */
    public void closeLogging() {
        String traceName = ThreadContext.get(MOCA_CONTEXT_TRACE_FILE_NAME);
        if (traceName != null && !traceName.isEmpty()
                && !traceName.equals(GlobalTraceState.getGlobalTraceFile())) {
            decrementFileCounter(traceName);
        }
        _logFileName = null;
        _traceLevel = null;
        clearTraceStateFromThread();
    }

    // @see java.lang.Object#finalize()
    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        // We want to make sure logging is cleaned up
        closeLogging();
    }

    /**
     * @param level
     */
    public void setLevel(String level) {
        _traceLevel = level;
    }

    /**
     * This level is only used between requests and should not be relied upon
     * to tell if tracing is enabled for the current thread.
     * The log4j ThreadContext holds this in the moca-trace-level variable.
     * @see ThreadContext#get(String)
     */
    public String getTraceLevel() {
        return _traceLevel;
    }

    public void applyTraceStateToThread() {
        String globalTrace;
        // If the trace state has it's own level, put it in, otherwise
        // default to the global trace level.
        if (_traceLevel != null && !_traceLevel.isEmpty()) {
            ThreadContext.put(MOCA_CONTEXT_TRACE_LEVEL_NAME, _traceLevel);
        }
        else if ((globalTrace = GlobalTraceState.getGlobalTraceLevel()) != null) {
            ThreadContext.put(MOCA_CONTEXT_TRACE_LEVEL_NAME, globalTrace);
        }

        String globalFile = GlobalTraceState.getGlobalTraceFile();
        // If the trace state has it's own log filename, put it in, otherwise
        // default to the global trace filename.
        if (_logFileName != null) {
            ThreadContext.put(MOCA_CONTEXT_TRACE_FILE_NAME, _logFileName);
        }
        else if (globalFile != null) {
            ThreadContext.put(MOCA_CONTEXT_TRACE_FILE_NAME, globalFile);
        }
    }

    
    // Keep track of the open files for client tracing.
    private static void incrementFileCounter(String filename) {
        AtomicInteger counter = new AtomicInteger();
        AtomicInteger prevCounter = _filesOpen.putIfAbsent(filename,
            counter);
        if (prevCounter != null) {
            counter = prevCounter;
        }
        synchronized (_filesOpen) {
            counter.incrementAndGet();
        }
    }
    
    // Close the file we just used for client tracing.
    private static int decrementFileCounter(String filename) {

        AtomicInteger counter = _filesOpen.get(filename);
        if (counter == null) {
            throw new IllegalStateException(
                "Trying to decrement a file counter that doesn't exist!");
        }

        synchronized (_filesOpen) {
            int value = counter.decrementAndGet();
            if (value == 0) {
                LoggerContext context = ServerUtils
                    .globalAttribute(LoggerContext.class);
                Map<String, Appender> map = context.getConfiguration()
                    .getAppenders();
                Appender appender = map
                    .get(LoggingConfigurator._routingAppenderName);
                if (appender instanceof MocaAppender) {
                    MocaAppender mAppender = (MocaAppender) appender;
                    mAppender.closeAppender(filename);
                }
                else {
                    _logger.warn("Unable to close file pointer for file :"
                            + filename + " due to no MocaAppender named: "
                            + LoggingConfigurator._routingAppenderName
                            + " being configured");
                }
            }
            return value;
        }
    }    
    
    //Remove the tracing from the thread.
    public static void clearTraceStateFromThread() {

        // Get the global trace level, if it's not null, replace it
        // with whatever is in the thread context. Otherwise, we'll just
        // remove the level all together.
        String globalTraceStateLevel = GlobalTraceState.getGlobalTraceLevel();
        if (globalTraceStateLevel != null) {
            ThreadContext.put(MOCA_CONTEXT_TRACE_LEVEL_NAME,
                globalTraceStateLevel);
        }
        else {
            ThreadContext.remove(MOCA_CONTEXT_TRACE_LEVEL_NAME);
        }

        String globalTraceFile = GlobalTraceState.getGlobalTraceFile();
        // Now reset back to global trace file if necessary.
        if (globalTraceFile != null) {
            ThreadContext.put(MOCA_CONTEXT_TRACE_FILE_NAME, globalTraceFile);
        }
        else {
            ThreadContext.remove(MOCA_CONTEXT_TRACE_FILE_NAME);
        }
    }
    
    public final static String MOCA_CONTEXT_TRACE_LEVEL_NAME = "moca-trace-level";
    public final static String MOCA_CONTEXT_TRACE_FILE_NAME = "moca-trace-file";
        
    private final static ConcurrentMap<String, AtomicInteger> _filesOpen = 
            new ConcurrentHashMap<String, AtomicInteger>();

    private final static Logger _logger = LogManager.getLogger(TraceState.class);
    
    private String _logFileName = null;
    private String _traceLevel;
    private final String _sessionId;
}
