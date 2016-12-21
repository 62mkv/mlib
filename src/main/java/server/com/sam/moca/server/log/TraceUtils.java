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

package com.sam.moca.server.log;

import com.sam.moca.MocaTrace;
import com.sam.moca.server.log.eventfactory.MocaLogEventFactory;
import com.sam.moca.server.log.exceptions.LoggingException;


/**
 * A tracing utility class that allows internal MOCA commands to manipulate
 * tracing options.
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
 * @author dpiessen
 * @version $Revision$
 */
public class TraceUtils {
    
    /**
     * Disables tracing for a given session and closes the writer.
     * 
     * @throws LoggingException Thrown if logging fails to be initialized.
     */
    public static void closeSessionTracing() throws LoggingException {
        TraceState traceState = getTraceState();

        if (traceState != null) {
            traceState.closeLogging();
        }
    }

    /**
     * Disables tracing for a given session. This method sets the logging level
     * to Off to disable tracing but does not close the writer.
     * 
     * @throws LoggingException Thrown if logging fails to be initialized.
     */
    public static void disableSessionTracing() throws LoggingException {
        TraceState traceState = getTraceState();

        if (traceState != null) {
            traceState.closeLogging();
        }
    }

    /**
     * Sets up tracing for an individual session including a file name to trace
     * to and wither that file should be appended or overwritten.
     * 
     * @param filename The file name to write to.
     * @param append Indicates if the current file should be appended or
     *                overwritten if it already exists.
     * @throws LoggingException Thrown if logging fails to be initialized.
     */
    public static void enableSessionTracing(String filename, boolean append)
            throws LoggingException {
        enableSessionTracing(filename, append, MocaTrace.getAllLevels());
    }
    
    /**
     * Sets up tracing for an individual session including a file name to trace
     * to and wither that file should be appended or overwritten.  
     * 
     * With the new tracing changes, any non null, non empty string
     * will result in a <code>*</code> trace level.
     * 
     * @param filename The file name to write to.
     * @param append Indicates if the current file should be appended or
     *                overwritten if it already exists.
     * @param traceLevel The MOCA tracing level to activate specified by {@link TraceLevel}.
     * @throws LoggingException Thrown if logging fails to be initialized.
     */
    public static void enableSessionTracing(String filename, boolean append, 
            String traceLevel)
            throws LoggingException {
        TraceState traceState = getTraceState();
        
        if(traceLevel != null && !traceLevel.isEmpty()) {
            traceLevel = "*";
        }
        
        if (traceState != null) {
            if (filename != null) {
                traceState.configureLogFileName(filename, append);
            }
            traceState.setLevel(traceLevel);
            traceState.applyTraceStateToThread();
        }
    }
    
    /**
     * Sets up tracing for an individual session including a file name to trace
     * to and wither that file should be appended or overwritten.
     * 
     * With the new tracing changes, any non null, non empty string
     * will result in a <code>*</code> trace level.
     * 
     * @param filename The file name to write to.
     * @param append Indicates if the current file should be appended or
     *                overwritten if it already exists.
     * @param traceLevel The MOCA tracing level to activate specified by {@link TraceLevel}.
     * @throws LoggingException Thrown if logging fails to be initialized.
     */
    public static void enableSessionTracing(String filename, boolean append, 
            int traceLevel) throws LoggingException {
        TraceUtils.enableSessionTracing(filename, append, traceLevel > 0 ? "*" : null);
        }

    /**
     * Set the logging and tracing level for a given session.
     *
     * With the new tracing changes, any non null, non empty string
     * will result in a <code>*</code> trace level.
     * 
     * @param traceLevel The tracing level. This level defines which areas of
     *                tracing are enabled and are determined by the defined
     *                levels specified by {@link TraceLevel}. If the value is
     *                less than 0 it the levels are not modified.
     * @throws LoggingException Thrown if the trace state cannot be accessed or
     *                 an error occurs.
     */
    public static void setSessionTraceLevel(int traceLevel) 
            throws LoggingException {
        TraceState traceState = getTraceState();

        if (traceState != null) {
            TraceUtils.enableSessionTracing(null, false, traceLevel > 0 ? "*": null);
        }
    }
    
    /**
     * Set the logging and tracing level for a given session.
     * 
     * With the new tracing changes, any non null, non empty string
     * will result in a <code>*</code> trace level.
     * 
     * @param logLevel The log level as specific to log4j.  This will allow the
     *                 caller to affect the log level that is enabled by the
     *                 log4j system for the session.  Null will leave this as is. 
     * @param traceLevel The tracing level. This level defines which areas of
     *                tracing are enabled and are determined by the defined
     *                levels specified by {@link TraceLevel}.  If null is
     *                provided the trace levels will be untouched.
     * @throws LoggingException Thrown if the trace state cannot be accessed or
     *                 an error occurs.
     */
    public static void setSessionTraceLevel(String traceLevel)
            throws LoggingException {
        TraceState traceState = getTraceState();

        if(traceLevel != null && !traceLevel.isEmpty()) {
            traceLevel = "*";
        }
        
        if (traceState != null) {
            TraceUtils.enableSessionTracing(null, false, traceLevel);
        }
    }

    /**
     * Sets the file to which trace messages for all server sessions and the
     * server are sent.
     * 
     * @param filename The file name to which trace messages are sent. This must
     *                be a full existing path on the server. No variable
     *                replacements are being performed.
     */
    public static void setGlobalTraceFile(String filename) {
        if (filename != null) {
            // TODO: look into this as well
            GlobalTraceState.setGlobalTraceFile(filename);
        }
    }

    /**
     * Sets global tracing to a given trace level based on the defined levels
     * specified by {@link TraceLevel}. Setting the value to 0 filters all
     * messages but does not close the trace file.
     * 
     * @param traceLevel The new tracing levels.
     */
    public static void setGlobalTraceLevel(int traceLevel) {
        // TODO: do this somehow
        GlobalTraceState.setGlobalTraceLevel(traceLevel > 0 ? "*": null);
    }
    
    /**
     * This method will verify if the current session trace is enabled for the
     * desired level as specified by {@link TraceLevel}.
     * @param level The level to check
     * @return Whether or not the level is enabled
     * @throws LoggingException If there was a problem obtaining the state of
     *         the trace level.
     *         
     */
    public static boolean isSessionTraceLevelEnabled(int level) 
            throws LoggingException {
        TraceState traceState = getTraceState();
        
        // If the level anded with the trace state is the same, that means
        // that all of those levels are enabled.
        return traceState.getTraceLevel() != null && 
                !traceState.getTraceLevel().isEmpty();
    }
    
    /**
     * This method will verify if the current global trace is enabled for the
     * desired level as specified by {@link TraceLevel}.
     * @param level The level to check
     * @return Whether or not the level is enabled
     */
    public static boolean isGlobalTraceLevelEnabled(int level) {
        String traceLevel = GlobalTraceState.getGlobalTraceLevel();
        boolean globalTraceOn = traceLevel != null && !traceLevel.isEmpty(); 
        
        //Global tracing is on and the level is > 0.
        // return that it is enabled.
        if (globalTraceOn && level > 0) {
            return true;
        } else if(!globalTraceOn && level == 0) {  
            // Is this correct? 
            // See how it was done before:
            // return (global level & level) == level 
            // Therefore (0 & 0) == 0 which would return true.
            return true;
        }
        
        //Anything else, return FALSE
        return false;
    }
    
    /**
     * This will return the current trace level for the global trace
     * With the log4j2 changes, this will return 1 for ON and 0 for OFF
     * @return the integer representing the trace levels enabled.
     */
    public static int getGlobalTraceLevel() {
        String traceLevel = GlobalTraceState.getGlobalTraceLevel();
        if(traceLevel == null || traceLevel.isEmpty()) {
            return 0;
        }
        
        return 1;
    }
    
    /**
     * This will return the current trace level for the session
     * @return the integer representing the trace levels enabled.
     */
    public static int getSessionTraceLevel() {
        int level = 0;
        TraceState traceState;
        try {
            traceState = getTraceState();
        }
        catch (LoggingException e1) {
            // If there was an exception, treat it as if session tracing is not
            // enabled
            traceState = null;
        }
        
        if (traceState != null && traceState.getTraceLevel() != null && 
                !traceState.getTraceLevel().isEmpty()) {
            level = MocaTrace.getAllLevels();
        }
        
        return level;
    }

    /**
     * Gets the session TraceState object from the MDC context
     * 
     * @return A session trace context.  This will always be not null.
     * @throws LoggingException Thrown if logging fails to be initialized.
     */
    public static TraceState getTraceState() throws LoggingException {
        TraceState state = MocaLogEventFactory._localTraceState.get();

        if (state != null) {
            return state;
        }
        
        throw new LoggingException(
            "Trace state could not be found in the local thread context.");
    }
}
