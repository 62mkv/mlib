/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.sam.moca.client;

import java.util.Map;

import com.sam.moca.MocaArgument;
import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;

/**
 * Client interface to MOCA.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public interface MocaConnection {

    /**
     * Execute a command, returning results from that command in a MocaResults
     * object.
     * 
     * @param command the text of the MOCA command sequence to execute.
     *            Execution will occur on the target system that this connection
     *            is associated with.
     * @return a MocaResults object representing the results of the command
     *         execution.
     * @throws MocaException if an error occurs, either in communication with
     *             the server, or upon execution of the command.
     */
    public MocaResults executeCommand(String command) throws MocaException;

    /**
     * Execute a command, optionally taking a list of arguments to the command.
     * The arguments sent are given to the request and make up the top-level
     * stack when the command is executed.
     * 
     * @param command the text of the MOCA command sequence to execute.
     *            Execution will occur on the target system that this connection
     *            is associated with.
     * @param args a list of arguments to be passed to the request. If this
     *            parameter is <code>null</code>, then no arguments are passed.
     * @return a MocaResults object representing the results of the command
     *         execution.
     * @throws MocaException if an error occurs, either in communication with
     *             the server, or upon execution of the command.
     */
    public MocaResults executeCommandWithArgs(String command, MocaArgument... args) throws MocaException;

    /**
     * Execute a command, optionally taking a list of arguments to the command.
     * The arguments sent are given to the request and make up the top-level
     * stack when the command is executed.
     * @param command the text of the MOCA command sequence to execute.
     *            Execution will occur on the target system that this connection
     *            is associated with.
     * @param args a list of arguments to be passed to the request. If this
     *            parameter is <code>null</code>, then no arguments are passed.
     * @param commandArgs an array of arguments that are treated as actual 
     *        command arguments as if they were passed in the where clause
     *        of the command. 
     * @return a MocaResults object representing the results of the command
     *         execution.
     * @throws MocaException if an error occurs, either in communication with
     *             the server, or upon execution of the command.
     */
    public MocaResults executeCommandWithContext(String command, MocaArgument[] args, MocaArgument[] commandArgs)
            throws MocaException;

    /**
     * Set the autoCommit flag. Normally, each command execution comprises a
     * single transaction. If the autocommit flag is set to false, multiple
     * commands will be executed within the same transactional context. Warning:
     * disabling autoCommit can affect the health of the server framework, and
     * should only be used for very short periods of time.
     * 
     * @param autoCommit the value of the autoCommit flag to be used for
     *            subsequent commands.
     */
    public void setAutoCommit(boolean autoCommit);

    /**
     * Returns the value of the autoCommit flag for this connection.
     * 
     * @return the value of the autoCommit flag.
     */
    public boolean isAutoCommit();

    /**
     * Sets this connection into "remote semantics" mode.  Error codes are not
     * translated into client-friendly errors in remote mode, but are passed
     * back to the caller as they occurred.  In most implementations, remote
     * mode defaults to <code>false</code> if this method is not called.
     * @param remote the new setting of remote mode.
     */
    public void setRemote(boolean remote);
    
    /**
     * Returns the current setting of remote mode for this connection.
     * @return the current remote settting.
     */
    public boolean isRemote();
    
    /**
     * Closes the connection. Any attempt to execute further commands on a
     * connection that has been closed will fail.
     */
    public void close();

    /**
     * Gets the environment for this connection. The environment String is used
     * to pass client-specific information to server-side components. Certain
     * environment entries are reserved for MOCA, and certain one are reserved
     * for well-known application use.
     * 
     * @return A Map object, mapping environment entry names to their String
     *         values.
     */
    public Map<String, String> getEnvironment();

    /**
     * Sets up the environment for this connection. The environment String is
     * used to pass client-specific information to server-side components.
     * Certain environment entries are reserved for MOCA, and certain one are
     * reserved for well-known application use.
     * 
     * @param env a Map object, mapping environment entry names to their String
     *            values.
     */
    public void setEnvironment(Map<String, String> env);

    /**
     * Sets the response timeout from this connection. Calls to execute methods that
     * take longer than the given number of milliseconds will throw an exception to
     * the caller.  Note that timeouts are approximate.  
     * @param ms The time this connection will wait for a response from the server.
     */
    public void setTimeout(long ms);
    
    /**
     * Returns the server key associated with this connection.  Each server (or
     * server cluster has an associated server key.  This key identifies the
     * server (somewhat) uniquely.  All connections to the same server should
     * have the same server key.
     * 
     * @return the server key 
     */
    public String getServerKey();
}
