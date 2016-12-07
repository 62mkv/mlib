/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.server.log.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

/**
 * Formats the event thread id
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
@Plugin(name = "ThreadPatternConverter", category = "Converter")
// TODO: does this replace thread?
@ConverterKeys({"T"})
public final class ThreadIdPatternConverter extends LogEventPatternConverter {
    /**
     * Singleton.
     */
    private static final ThreadIdPatternConverter INSTANCE =
        new ThreadIdPatternConverter();

    /**
     * Private constructor.
     */
    private ThreadIdPatternConverter() {
        super("ThreadId", "thread");
    }

    /**
     * Obtains an instance of ThreadIdPatternConverter.
     *
     * @param options options, currently ignored, may be null.
     * @return instance of ThreadIdPatternConverter.
     */
    public static ThreadIdPatternConverter newInstance(
        final String[] options) {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final StringBuilder toAppendTo) {
        toAppendTo.append(Thread.currentThread().getId());
    }
}
