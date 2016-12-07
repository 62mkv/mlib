/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.advice;

import java.io.IOException;
import java.util.Date;

import com.redprairie.moca.server.exec.SessionType;

/**
 * This is the interface exposed to allow for querying of session specific
 * information.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public interface SessionAdministrationBean {
    
    /**
     * Retrieve the name of this session
     * @return the session name
     */
    public String getSessionName();
    
    /**
     * Retrieve the last executed command
     * @return the command
     */
    public String getLastCommand();
    
    /**
     * Retrieve the date when the last executed command was executed
     * @return the date of execution
     */
    public Date getLastCommandTime();
    
    /**
     * Retrieve the last executed sql statement
     * @return the sql statement
     */
    public String getLastSqlStatement();
    
    /**
     * Retrieve the date when the last executed sql statement was executed
     * @return the date of execution
     */
    public Date getLastSqlStatementTime();
    
    /**
     * Retrieve the last executed script
     * @return the script
     */
    public String getLastScript();
    
    /**
     * Retrieve the date when the last executed script was executed
     * @return the date of execution
     */
    public Date getLastScriptTime();
    
    /**
     * Retrieve the last connected ip address for this session
     * @return the ip address of the client
     */
    public String getConnectedIpAddress();
    
    /**
     * Retrieve the date when this session was first started
     * @return the date of started
     */
    public Date getStartedTime();
    
    /**
     * Returns the name of the trace file.  If it is not a trace file such
     * as a custom appender.  The name will be <code>Custom Appender</code>
     * @return The name of the trace file.
     */
    public String getTraceFile();
    
    /**
     * Starts trace for this session sending to the file specified.
     * @param fileName The file to trace to
     * @param level The trace level to set.  If empty is provided this
     *        will disable the trace
     * @throws IOException if a problem occurs enabling trace
     */
    public void startTrace(String fileName, String level) throws IOException;
    
    /**
     * Tells what type of session this session is running as
     * @return the session type
     */
    public SessionType getSessionType();
}