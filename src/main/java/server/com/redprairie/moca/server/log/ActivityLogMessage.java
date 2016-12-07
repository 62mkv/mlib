/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

import java.util.Map;

import org.apache.logging.log4j.message.MapMessage;

/**
 * A log message that captures MOCA server activity, in the form of a request and environment variables, as well as
 * result code and row count.
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class ActivityLogMessage {
    public static final String LOGGER_NAME = "com.redprairie.moca.Activity";
    
    /**
     * 
     */
    public ActivityLogMessage(long duration, String command, Map<String, String> env, int errorCode, int rowCount) {
        _duration = duration;
        _command = command;
        _env = env;
        _errorCode = errorCode;
        _rowCount = rowCount;
    }

    public MapMessage asMapMessage(){
        MapMessage mapMessage = new MapMessage();
        mapMessage.put("duration", String.valueOf(_duration));
        mapMessage.put("command", String.valueOf(_command).replaceAll("\\s+", " "));
        mapMessage.put("env", String.valueOf(_env).replaceAll("\\s+", " "));
        mapMessage.put("error", String.valueOf(_errorCode));
        mapMessage.put("rowCount", String.valueOf(_rowCount));
        return mapMessage;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(_duration).append(SEPARATOR);
        buf.append(String.valueOf(_env).replaceAll("\\s+", " ")).append(SEPARATOR);
        buf.append(_errorCode).append(SEPARATOR);
        buf.append(_rowCount).append(SEPARATOR);
        buf.append(String.valueOf(_command).replaceAll("\\s+", " "));
        return buf.toString();
    }
    
    private final Map<String, String> _env;
    private final String _command;
    private final long _duration;
    private final int _errorCode;
    private final int _rowCount;
    private static final String SEPARATOR = "\t";
}
