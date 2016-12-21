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

package com.sam.moca.server.legacy;

import java.util.Map;

import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.client.ConnectionUtils;
import com.sam.moca.client.LoginFailedException;
import com.sam.moca.client.LogoutFailedException;
import com.sam.moca.client.MocaConnection;

/**
 * Adapter class between the legacy mcclib  and the Java  layer.
 * 
 * This interface essentially comprises the API for C code.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author mlange
 * @version $Revision$
 */
public class MocaClientAdapter {

    /**
     * Create a client connection to the MOCA service.
     *  
     * @param url the base URL to use to communicate with MOCA.
     * @param env a colon-separated list of environment name/value pairs.
     * @throws MocaException if an error occurs connecting to the MOCA service.
     */ 
    public MocaClientAdapter(String url, String env) throws MocaException {    
        // Create a connection to this URL.
        _conn = ConnectionUtils.createConnection(url, ConnectionUtils.parseEnvironmentString(env));
    }
    
    /**
     * Close this client connection to MOCA.
     */  
    public void close() {
        _conn.close();
    }
    
    /**
     * Execute a MOCA command.
     * 
     * @param command the command to execute.
     * @return a <code>MocaResults</code> object containing the results of the
     *         command.
     * @throws MocaException if the command threw an exception, or if the MOCA
     *             system was unable to process the command.
     */  
    public MocaResults executeCommand(String command) throws MocaException {        
        try {
            return castResults(_conn.executeCommand(command));
        }
        catch (MocaException e) {
            e.setResults(castResults(e.getResults()));
            throw e;
        }
    }

    private WrappedResults castResults(MocaResults res) {
        if (res == null || res instanceof WrappedResults) {
            return (WrappedResults) res;
        }
        else {
            WrappedResults out = new WrappedResults(res, false);
            res.close();
            return out;
        }
    }
    
    /**
     * Login a user.
     * 
     * @param userid the userid of the user to login.
     * @throws LoginFailedException if the user could not be logged in.
     */
    public void login(String userid, String password, String clientKey) throws LoginFailedException {
        ConnectionUtils.login(_conn, userid, password, clientKey);
    }
    
    /**
     * Logout the current user.
     * 
     * @throws LogoutFailedException if the user could not be logged out.
     */
    public void logout() throws LogoutFailedException {
        ConnectionUtils.logout(_conn);
    }
   
    /**
     * Set the auto-commit behavior of the MOCA service.  When auto-commit is enabled
     * MOCA will automatically perform a commit after the successful completion of a
     * command executed via <code>executeCommand()</code> or automatically perform a
     * rollback if an exception was raised from <code>executeCommand()</code>.
     * 
     * @param enable a boolean value stating if auto-commit should be enabled.
     */
    public void setAutoCommit(boolean enable) {
        _conn.setAutoCommit(enable);
    }
 
    /**
     * Set the application id for this client connection.
     * 
     * @param applicationId an application id.
     */
    public void setApplicationId(String appId) {
        Map<String,String> env = _conn.getEnvironment();     
        env.put("MOCA_APPL_ID", appId);       
        _conn.setEnvironment(env);
    }
    
    /**
     * Set the environment for this client connection. The environment string is
     * used to pass client-specific information to server-side components.
     * Certain environment entries are reserved for MOCA, and certain one are
     * reserved for well-known application use.
     * 
     * @param env a colon-separated list of environment name/value pairs.
     */
    public void setEnvironment(String envString) {
        Map<String,String> env = _conn.getEnvironment();      
        Map<String,String> envToAdd = ConnectionUtils.parseEnvironmentString(envString);   
        env.putAll(envToAdd);
        _conn.setEnvironment(env);
    }
    
    private final MocaConnection _conn;
}