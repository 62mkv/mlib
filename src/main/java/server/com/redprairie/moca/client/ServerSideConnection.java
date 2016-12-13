/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20167
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

package com.redprairie.moca.client;

import java.util.Arrays;
import java.util.Map;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.exceptions.UniqueConstraintException;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.util.MocaUtils;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 20167 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class ServerSideConnection implements MocaConnection {
    
    /**
     * Create a new Server Side Connection that will have error conversion
     * enabled.
     */
    public ServerSideConnection() {
        this(true);
    }
    
    /**
     * Create a new Server Side Connection where you can select whether to have
     * error code conversion enabled.
     * @param convertErrorCode Whether to convert error codes for exceptions 
     */
    public ServerSideConnection(boolean convertErrorCode) {
        _convertErrorCode = convertErrorCode;
    }
    

    // @see com.redprairie.moca.client.MocaConnection#close()
    public void close() {
        // NOOP
    }

    // @see com.redprairie.moca.client.MocaConnection#executeCommand(java.lang.String)
    public MocaResults executeCommand(String command) throws MocaException {
        return executeCommandWithContext(command, null, null);
    }

    // @see com.redprairie.moca.client.MocaConnection#executeCommandWithArgs(java.lang.String, com.redprairie.moca.MocaArgument[])
    @Override
    public MocaResults executeCommandWithArgs(String command, MocaArgument... args) throws MocaException {
        return executeCommandWithContext(command, args, null);
    }
    
    // @see com.redprairie.moca.client.MocaConnection#executeCommandWithContext(java.lang.String, com.redprairie.moca.MocaArgument[], com.redprairie.moca.MocaArgument[])
    @Override
    public MocaResults executeCommandWithContext(String command, MocaArgument[] args, MocaArgument[] commandArgs)
            throws MocaException {    
        ServerContext ctx = ServerUtils.getCurrentContext();
        try {
            MocaResults res = ctx.executeCommandWithRemoteContext(command, 
                    args != null ? Arrays.asList(args) : null, 
                    commandArgs != null ? Arrays.asList(commandArgs) : null);
            if (_autoCommit) ctx.commit();
            return res;
        }
        catch (MocaException e) {
            int errorCode = e.getErrorCode();
            MocaResults errorResults = e.getResults();
            String message = e.getMessage();

            if (!_remoteMode) {
                // We mangle some error codes before returning them to the client.
                if (errorCode == NotFoundException.DB_CODE)
                    errorCode = ERROR_DB_NOT_FOUND;
                else if (errorCode == UniqueConstraintException.CODE)
                    errorCode = ERROR_DB_UNIQUE_CONSTRAINT;
                else if (errorCode < 0)
                    errorCode = ERROR_DB_GENERIC;
            }
            
            if (_autoCommit) ctx.rollback();
            
            if (_convertErrorCode) {
                if (errorCode == NotFoundException.DB_CODE || 
                        errorCode == NotFoundException.SERVER_CODE) {
                    throw new NotFoundException(errorCode, errorResults);
                }
                e.printStackTrace();
                throw new ServerExecutionException(errorCode, message, 
                        errorResults, e);
            }
            else {
                throw e;
            }
        }
    }
    
    // @see com.redprairie.moca.client.MocaConnection#isAutoCommit()
    public boolean isAutoCommit() {
        return _autoCommit;
    }

    // @see com.redprairie.moca.client.MocaConnection#setAutoCommit(boolean)
    public void setAutoCommit(boolean autoCommit) {
        _autoCommit = autoCommit;
    }
    
    // @see com.redprairie.moca.client.MocaConnection#setRemote(boolean)
    @Override
    public void setRemote(boolean remote) {
        // If remote, no error code conversion is done.
        _remoteMode = remote;
    }
    
    // @see com.redprairie.moca.client.MocaConnection#isRemote()
    @Override
    public boolean isRemote() {
        return _remoteMode;
    }

    // @see com.redprairie.moca.client.MocaConnection#setEnvironment(java.util.Map)
    public void setEnvironment(Map<String, String> env) {
        MocaContext ctx = MocaUtils.currentContext();
        if (env != null) {
            for (Map.Entry<String, String> entry : env.entrySet()) {
                ctx.putSystemVariable(entry.getKey(), entry.getValue());
            }
        }
    }
    
    // @see com.redprairie.moca.client.MocaConnection#getEnvironment()
    public Map<String, String> getEnvironment() {
        // NOOP
        return null;
    }
    
    @Override
    public void setTimeout(long ms) {
        // NOOP
        return;
    }
    
    @Override
    public String getServerKey() {
        // This method doesn't apply to server-mode connections.
        return "na";
    }
    
    @Override
    public String toString() {
        return "Local (" + MocaUtils.currentContext().getSystemVariable("MOCA_ENVNAME") + ")";
    }
    
    private static int ERROR_DB_NOT_FOUND = NotFoundException.SERVER_CODE;
    private static int ERROR_DB_UNIQUE_CONSTRAINT = 512;
    private static int ERROR_DB_GENERIC = 511;
    
    private boolean _autoCommit = true;
    private boolean _remoteMode = false;
    private final boolean _convertErrorCode;
    
}
