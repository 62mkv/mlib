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

package com.sam.moca.components.base;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sam.moca.EditableResults;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.RequiredArgumentException;
import com.sam.moca.server.log.MocaTraceMessaging;
import com.sam.moca.server.log.TraceLevelConverter;
import com.sam.moca.server.log.TraceUtils;
import com.sam.moca.server.log.exceptions.LoggingException;
import com.sam.moca.util.MocaUtils;
import com.sam.util.ArgCheck;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TraceService {
    /**
     * This implements the set trace component in moca base component.  This 
     * will enable a newly created trace file as well as the specified level.
     * @param moca The moca context
     * @param filename The file name to be traced as
     * @param directory The directory the file will be located
     * @param level The trace level of the system
     * @param appendMode Whether or not we append.  By default this is true
     * @param activate If tracing should be enabled, 1 = true
     * @return The results telling the file name and if tracing is enabled.
     * @throws LoggingException This is thrown if any problem is encountered 
     *         while enabling tracing.
     * @throws RequiredArgumentException 
     */
    public MocaResults setTrace(MocaContext moca, String filename, 
            String directory, String level, String mode, 
            Integer activate) throws LoggingException, RequiredArgumentException {
        if (activate == null) {
            throw new RequiredArgumentException("activate");
        }
        
        EditableResults retRes = moca.newResults();
        
        retRes.addColumn("filename", MocaType.STRING);
        retRes.addColumn("tracing", MocaType.BOOLEAN);
        
        retRes.addRow();
        
        // If we are turning on tracing
        if (activate == 1) {
            if (filename == null || filename.trim().length() == 0) {
                throw new RequiredArgumentException("filename");
            }            
            
            // With the new log4j2 changes, if you're enabling 
            // client tracing, you're getting everything. 
            // YOU HAVE NO CHOICE.
            level = "*";
            
            // If the directory isn't specified default to $LESDIR/log
            if (directory == null || directory.isEmpty()) {
                directory = MocaUtils.expandEnvironmentVariables(moca, 
                        "$LESDIR" + File.separator + "log");
            }
            else {
                directory = MocaUtils.expandEnvironmentVariables(moca, 
                        directory);
            }
            
            _logger.debug("Setting trace file to " + directory + 
                    File.separator + filename);
            File traceFile = new File(directory, filename);
            
            TraceUtils.enableSessionTracing(traceFile.getAbsolutePath(), 
                    mode == null || mode.contains("a"), level);
            
            MocaTraceMessaging.logLibraryVersions(moca);
            
            retRes.setStringValue("filename", traceFile.getAbsolutePath());
            retRes.setBooleanValue("tracing", true);
            
        }
        else {
            // It was set to not activate so we have to disable tracing
            TraceUtils.disableSessionTracing();
            
            retRes.setBooleanValue("tracing", false);
        }
        
        return retRes;
    }
    
    
    
    /**
     * This will trace a message with the given level.
     * @param moca The moca context
     * @param message The message to trace
     * @param level The level to use
     */
    public void writeTraceMessage(MocaContext moca, String message, 
            String level) {
        ArgCheck.notNull(message);
        ArgCheck.notNull(level);
        // The level has to be 1 length
        ArgCheck.isTrue(level.length() == 1, "Invalid Level argument " + level);
        int intLevel = TraceLevelConverter.getTraceLevelFromCharacter(
                level.charAt(0));
        // The level has to resolve
        ArgCheck.isFalse(intLevel == -1, "Invalid level argument " + level);
        moca.trace(intLevel, message);
    }
    
    
    /**
     * This will log a message with the given level.
     * @param moca The moca context
     * @param message The message to trace
     * @param level The level to use
     */
    public void writeLogMessage(MocaContext moca, String message, 
            String level) {
        ArgCheck.notNull(message);
        ArgCheck.notNull(level);
        // The level has to be 1 length
        ArgCheck.isTrue(level.length() == 1, "Invalid Level argument " + level);
        
        switch (level.charAt(0)) {
        case 'E':
            moca.logError(message);
            break;
        case 'W':
            moca.logWarning(message);
            break;
        case 'I':
            moca.logInfo(message);
            break;
        default:
            throw new IllegalArgumentException("Invalid Level argument "
                    + level);
        }
    }
    
    static Logger _logger = LogManager.getLogger(TraceService.class);
}
