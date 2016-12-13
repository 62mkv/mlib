/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.server.log.config;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.LogEventFactory;
import org.apache.logging.log4j.status.StatusLogger;

import com.redprairie.moca.server.log.eventfactory.MocaLogEventFactory;

/**
 * This is a Moca Logger Config that basically is only here to allow for
 * a special moca logging event to get logged
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
@Plugin(name = "logger", printObject = true, category = "Core")
public class MocaLoggerConfig extends LoggerConfig {
    /**
     * @param name
     * @param appenders
     * @param filter
     * @param level
     * @param additive
     * @param properties
     * @param config
     */
    public MocaLoggerConfig(String name, List<AppenderRef> appenders, 
        Filter filter, Level level, boolean additive, Property[] properties, 
        Configuration config, boolean someBoolean) {
        super(name, appenders, filter, level, additive, properties, config, someBoolean);
        setLogEventFactory(MOCA_LOG_FACTORY);
    }

    @PluginFactory
    public static LoggerConfig createLogger(
        @PluginAttribute("additivity") String additivity,
        @PluginAttribute("level") String levelName,
        @PluginAttribute("name") String loggerName,
        @PluginAttribute("includeLocation") final String includeLocation,
        @PluginElement("appender-ref") AppenderRef[] refs,
        @PluginElement("properties") Property[] properties,
        @PluginConfiguration Configuration config,
        @PluginElement("filters") Filter filter) {
        if (loggerName == null) {
            STATUS_LOGGER.error("Loggers cannot be configured without a name");
            return null;
        }

        List<AppenderRef> appenderRefs = Arrays.asList(refs);
        Level level;
        try {
            level = Level.toLevel(levelName, Level.ERROR);
        } catch (Exception ex) {
            STATUS_LOGGER.error("Invalid Log level specified: {}. Defaulting to Error", levelName);
            level = Level.ERROR;
        }
        String name = loggerName.equals("root") ? "" : loggerName;
        boolean additive = additivity == null ? true : 
            Boolean.parseBoolean(additivity);

        return new MocaLoggerConfig(name, appenderRefs, filter, level, additive, 
            properties, config, includeLocation(includeLocation));
    }
    
    private static final Logger STATUS_LOGGER = StatusLogger.getLogger();
    public static final LogEventFactory MOCA_LOG_FACTORY = new MocaLogEventFactory();
}
