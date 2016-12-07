/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.server.log;

import java.util.logging.LoggingMXBean;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to define the methods for accessing loggers remotely
 * and changing them at runtime.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public interface MocaLoggingMXBean extends LoggingMXBean {

    /**
     * Gets the name of the log level associated with the specified logger.
     * If the specified logger does not exist, <tt>null</tt>
     * is returned.
     * This method first finds the logger of the given name and
     * then returns the name of the log level by calling:
     * <blockquote>
     *   {@link Logger#getEffectiveLevel Logger.getEffectiveLevel()}.{@link Level#getName getName()};
     * </blockquote>
     *
     * <p>
     * The effective level of the specificied logger can never be <tt>null</tt>
     *
     * @param loggerName The name of the <tt>Logger</tt> to be retrieved.
     *
     * @return The name of the log level of the specified logger; or
     *         an empty string if the log level of the specified logger
     *         is <tt>null</tt>.  If the specified logger does not
     *         exist, a {@link IllegalArgumentException} is thrown.
     *
     * @see Logger#getEffectiveLevel
     */
    public String getLoggerEffectiveLevel(String loggerName);
}
