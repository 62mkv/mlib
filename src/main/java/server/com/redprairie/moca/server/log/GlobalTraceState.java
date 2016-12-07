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

package com.redprairie.moca.server.log;

import org.apache.logging.log4j.ThreadContext;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class GlobalTraceState {

    /**
     * This method should only ever be called once in the main thread of an
     * application before it spawns any threads.  If this is called anywhere else
     * it is very very very very very bad.
     * @param level
     */
    public static void setGlobalTraceLevel(String level) {
        _traceLevel = level;
        if (_traceLevel != null && !_traceLevel.isEmpty()) {
            ThreadContext.put(TraceState.MOCA_CONTEXT_TRACE_LEVEL_NAME,
                _traceLevel);
        }
    }
    
    public static String getGlobalTraceLevel() {
        return _traceLevel;
    }
    
    /**
     * This method should only ever be called once in the main thread of an
     * application before it spawns any threads.  If this is called anywhere else
     * it is very very very very very bad.
     * @param level
     */
    public static void setGlobalTraceFile(String file) {
        _traceFile = file;
        if (_traceFile != null && !_traceFile.isEmpty()) {
            ThreadContext.put(TraceState.MOCA_CONTEXT_TRACE_FILE_NAME,
                _traceFile);
        }
    }

    public static String getGlobalTraceFile() {
        return _traceFile;
    }

    private static volatile String _traceLevel;
    private static volatile String _traceFile;
}
