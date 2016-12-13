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

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An adapter class for the Java logging framework
 * that redirects messages to Log4j.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dpiessen
 * @version $Revision$
 */
 class AdapterHandler extends Handler {

    // @see java.util.logging.Handler#close()
    
    @Override
    public void close() throws SecurityException {
    }

    // @see java.util.logging.Handler#flush()
    
    @Override
    public void flush() {  
    }

    // @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
    
    @Override
    public void publish(LogRecord record) {
        
        Logger log4jLogger = LogManager.getLogger(record.getLoggerName());
        java.util.logging.Level logLevel = record.getLevel();
        
        if (log4jLogger == null || logLevel == java.util.logging.Level.OFF) {
            //Exit if we can't get a logger or logging is disabled
            return;
        }
        
        //Try to match the level and log to log4j
        if (logLevel == java.util.logging.Level.SEVERE) {
            log4jLogger.error(record.getMessage());
        }
        else if (logLevel == java.util.logging.Level.WARNING) {
            log4jLogger.warn(record.getMessage());
        }
        else if (logLevel == java.util.logging.Level.INFO) {
            log4jLogger.info(record.getMessage());
        }
        else if (logLevel == java.util.logging.Level.FINE) {
            log4jLogger.debug(record.getMessage());
        }
        else if (logLevel == java.util.logging.Level.FINER ||
                 logLevel == java.util.logging.Level.FINEST) {
            log4jLogger.trace(record.getMessage());
        }
    }
    
}
