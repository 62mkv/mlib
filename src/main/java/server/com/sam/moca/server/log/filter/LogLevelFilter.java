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

package com.sam.moca.server.log.filter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

/**
 * This filter returns the onMatch result if the level in the LogEvent is the same or more specific
 * than the configured level and the onMismatch value otherwise. For example, if the ThresholdFilter
 * is configured with Level ERROR and the LogEvent contains Level DEBUG then the onMismatch value will
 * be returned since ERROR events are more specific than DEBUG.
 *
 * The default Level is ERROR.
 */
@Plugin(name = "LogLevelFilter", elementType = "filter", printObject = true, category = "Core")
public final class LogLevelFilter extends AbstractFilter {

    private LogLevelFilter(final Result onMatch, final Result onMismatch) {
        super(onMatch, onMismatch);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final String msg,
                         final Object... params) {
        return filter(logger.getLevel(), level);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final Object msg,
                         final Throwable t) {
        return filter(logger.getLevel(), level);
    }

    @Override
    public Result filter(final Logger logger, final Level level, final Marker marker, final Message msg,
                         final Throwable t) {
        return filter(logger.getLevel(), level);
    }

    @Override
    public Result filter(final LogEvent event) {
        String loggerName = event.getLoggerName();
        if (loggerName != null) {
            Logger logger = (Logger)LogManager.getLogger(loggerName);
            return filter(logger.getLevel(), event.getLevel());
        }
        else {
            LOGGER.warn("Event provided didn't contain a loggerName");
            return onMismatch;
        }
    }

    private Result filter(final Level loggerLevel, final Level level) {
        return level.isAtLeastAsSpecificAs(loggerLevel) ? onMatch : onMismatch;
    }

    /**
     * Create a LogLevelFilter.
     * @param match The action to take on a match.
     * @param mismatch The action to take on a mismatch.
     * @return The created ThresholdFilter.
     */
    @PluginFactory
    public static LogLevelFilter createFilter(@PluginAttribute("onMatch") final String match,
                                              @PluginAttribute("onMismatch") final String mismatch) {
        final Result onMatch = Result.toResult(match, Result.NEUTRAL);
        final Result onMismatch = Result.toResult(mismatch, Result.DENY);
        return new LogLevelFilter(onMatch, onMismatch);
    }

}
