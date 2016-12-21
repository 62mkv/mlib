/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.sam.moca.applications.traceanalyzer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a TraceLine from a log file.
 */
public class TraceLine {
    
    private static final List<String> ERROR_LEVELS = new ArrayList<String>();
    
    static {
        ERROR_LEVELS.add("W");
        ERROR_LEVELS.add("E");
        ERROR_LEVELS.add("F");
    }
    
    TraceLine(String fullLine, int threadId, String sessionId, String logLevel, Date logDate,
                     String logDateAsString, String loggerName, int stackLevel, String logMessage) {
        _threadId = threadId;
        _sessionId = sessionId;
        _logLevel = logLevel;
        _logDate = new Date(logDate.getTime());
        _logDateAsString = logDateAsString;
        _loggerName = loggerName;
        _stackLevel = stackLevel;
        _logMessage = logMessage;
        _fullLine = fullLine;
    }

    public int getThreadId() {
        return _threadId;
    }
    
    public String getSessionId() {
        return _sessionId;
    }
    
    public String getLogLevel() {
        return _logLevel;
    }
    
    public boolean isErrorLevel() {
        return ERROR_LEVELS.contains(getLogLevel());
    }
    
    public Date getDate() {
        return new Date(_logDate.getTime());
    }
    
    public String getDateAsString() {
        return _logDateAsString;
    }
    
    public String getLogger() {
        return _loggerName;
    }
    
    public int getStackLevel() {
        return _stackLevel;
    }
    
    public String getMessage() {
        return _logMessage;
    }
    
    public String getFullLine() {
        return _fullLine;
    }
    
    @Override
    public String toString() {
        return _fullLine;
    }
    
    private final String _fullLine;
    private final int _threadId;
    private final String _sessionId;
    private final String _logLevel;
    private final Date _logDate;
    private final String _logDateAsString;
    private final String _loggerName;
    private final int _stackLevel;
    private final String _logMessage;
}
