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

package com.redprairie.moca.server.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

/**
 * This class is meant to provide some utility methods for logging while
 * the instance is in a running state.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MocaLogging implements MocaLoggingMXBean {
    public MocaLogging(Configuration config) {
        _config = config;
    }

    // @see java.util.logging.LoggingMXBean#getLoggerLevel(java.lang.String)
    @Override
    public String getLoggerLevel(String loggerName) {
        if (loggerName == null) {
            throw new NullPointerException("loggerName is null");
        }

        
        Map<String, LoggerConfig> loggers = _config.getLoggers();
        LoggerConfig config = loggers.get(loggerName);
        if (config == null) {
            throw new IllegalArgumentException("Logger " + loggerName +
                " does not exist");
        }
        
        Level level = config.getLevel();
        
        if (level == null) {
            return EMPTY_STRING;
        }
        
        return level.toString();
    }

    // @see java.util.logging.LoggingMXBean#getLoggerNames()
    @Override
    public List<String> getLoggerNames() {
        Map<String, LoggerConfig> loggers = _config.getLoggers();
        ArrayList<String> array = new ArrayList<String>();

        for (String name : loggers.keySet()) {
            array.add(name);
        }
        return array;
    }

    // @see java.util.logging.LoggingMXBean#getParentLoggerName(java.lang.String)
    @Override
    public String getParentLoggerName(String loggerName) {
        if (loggerName == null) {
            throw new NullPointerException("loggerName is null");
        }

        Map<String, LoggerConfig> loggers = _config.getLoggers();
        LoggerConfig config = loggers.get(loggerName);
        
        if (config == null) {
            throw new IllegalArgumentException("Logger " + loggerName +
                " does not exist");
        }
        
        LoggerConfig p = config.getParent();
        if (p == null) {
            // root logger
            return EMPTY_STRING;
        }
        else if (p.getName().equals(LogManager.ROOT_LOGGER_NAME)) {
            // We want to display "" so people can query it's Level.  It saying
            // root is misleading, because LogManager.getLogger("root") is not the
            // same as Logger.getRootLogger, but LogManager.getLogger("") is.
            return ROOT_LOGGER_NAME;
        }
        else {
            return p.getName();
        }
    }

    // @see java.util.logging.LoggingMXBean#setLoggerLevel(java.lang.String, java.lang.String)
    @Override
    public void setLoggerLevel(String loggerName, String levelName) {
        if (loggerName == null) {
            throw new NullPointerException("loggerName is null");
        }

        Map<String, LoggerConfig> loggers = _config.getLoggers();
        LoggerConfig config = loggers.get(loggerName);

        Level level = null;
        if (levelName != null) {
            // parse will throw IAE if logLevel is invalid
            level = Level.toLevel(levelName, null);
            if (level == null) {
                throw new IllegalArgumentException("Log level " + levelName + 
                        " is invalid.");
            }
        }

        config.setLevel(level);
    }
    
    // @see com.redprairie.moca.server.log.MocaLoggingMXBean#getLoggerEffectiveLevel(java.lang.String)
    @Override
    public String getLoggerEffectiveLevel(String loggerName) {
        if (loggerName == null) {
            throw new NullPointerException("loggerName is null");
        }

        Map<String, LoggerConfig> loggers = _config.getLoggers();
        LoggerConfig config = loggers.get(loggerName);
        
        if (config == null) {
            throw new IllegalArgumentException("Logger " + loggerName +
                " does not exist");
        }
        // TODO: this seems wrong
        Level level = config.getLevel();
        
        return level.toString();
    }

    private final Configuration _config;

    private static final String ROOT_LOGGER_NAME = "/";
    private static final String EMPTY_STRING = "";
}
