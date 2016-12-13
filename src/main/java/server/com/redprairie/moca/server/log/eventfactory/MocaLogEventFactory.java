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

package com.redprairie.moca.server.log.eventfactory;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.impl.DefaultLogEventFactory;
import org.apache.logging.log4j.message.Message;

import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.StackLevel;
import com.redprairie.moca.server.log.TraceState;

/**
 * MocaLogEventFactory that handles creating the event to be logged
 * by log4j2.  This will retrieve the current local trace state and
 * place the information into the trace. Information consists of 
 * MOCA stack level and the session id.
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
public class MocaLogEventFactory extends DefaultLogEventFactory {
    
    public static final ThreadLocal<TraceState> _localTraceState = 
            new InheritableThreadLocal<TraceState>();

    // @see org.apache.logging.log4j.core.impl.DefaultLogEventFactory#createEvent(java.lang.String, org.apache.logging.log4j.Marker, java.lang.String, org.apache.logging.log4j.Level, org.apache.logging.log4j.message.Message, java.util.List, java.lang.Throwable)
    
    @Override
    public LogEvent createEvent(String loggerName, Marker marker, String fqcn,
                                Level level, Message data,
                                List<Property> properties, Throwable t) {
        // We want to pass along our objects with the values required for the
        // event to take place.
        TraceState state = _localTraceState.get();
        String stackLevel;
        String sessionId;
        
        // We only append the stack level and session if we have a trace state object
        if (state != null) {
            ServerContext context = ServerUtils.getCurrentContextNullable();
            
            StackLevel mocaStackLevel;
            if (context != null && (mocaStackLevel = context.getStackLevel()) != null) {
                stackLevel = mocaStackLevel.toString();
            }
            else {
                stackLevel = "";
            }
            sessionId = state.getSessionId();
        }
        else {
            stackLevel = "";
            sessionId = "";
        }
        
        try {
            ThreadContext.put("moca-session", sessionId);
            ThreadContext.put("moca-stack-level", stackLevel);
            return super.createEvent(loggerName, marker, fqcn, level, data, properties, t);
        }
        finally {
            ThreadContext.remove("moca-session");
            ThreadContext.remove("moca-stack-level");
        }
    }
}
