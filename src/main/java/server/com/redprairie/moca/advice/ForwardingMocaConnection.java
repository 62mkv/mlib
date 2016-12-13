/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.advice;

import java.util.Map;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.client.MocaConnection;

/**
 * A forwarding MocaConnection class that can be 
 * extended to provide custom override logic.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
public class ForwardingMocaConnection implements MocaConnection {


    private final MocaConnection _mocaConn;

    public ForwardingMocaConnection(MocaConnection conn) {
        _mocaConn = conn;
    }

    // @see com.redprairie.moca.client.MocaConnection#executeCommand(java.lang.String)

    @Override
    public MocaResults executeCommand(String command) throws MocaException {
        return delegate().executeCommand(command);
    }

    // @see com.redprairie.moca.client.MocaConnection#executeCommandWithArgs(java.lang.String, com.redprairie.moca.MocaArgument[])

    @Override
    public MocaResults executeCommandWithArgs(String command,
                                              MocaArgument... args)
            throws MocaException {
        return delegate().executeCommandWithArgs(command, args);
    }

    // @see com.redprairie.moca.client.MocaConnection#executeCommandWithContext(java.lang.String, com.redprairie.moca.MocaArgument[], com.redprairie.moca.MocaArgument[])

    @Override
    public MocaResults executeCommandWithContext(String command,
                                                 MocaArgument[] args,
                                                 MocaArgument[] commandArgs)
            throws MocaException {
        return delegate().executeCommandWithContext(command, args, commandArgs);
    }

    // @see com.redprairie.moca.client.MocaConnection#setAutoCommit(boolean)

    @Override
    public void setAutoCommit(boolean autoCommit) {
        delegate().setAutoCommit(autoCommit);
    }

    // @see com.redprairie.moca.client.MocaConnection#isAutoCommit()

    @Override
    public boolean isAutoCommit() {
        return delegate().isAutoCommit();
    }

    // @see com.redprairie.moca.client.MocaConnection#setRemote(boolean)

    @Override
    public void setRemote(boolean remote) {
        delegate().setRemote(remote);
    }

    // @see com.redprairie.moca.client.MocaConnection#isRemote()

    @Override
    public boolean isRemote() {
        return delegate().isRemote();
    }

    // @see com.redprairie.moca.client.MocaConnection#close()

    @Override
    public void close() {
        delegate().close();
    }

    // @see com.redprairie.moca.client.MocaConnection#getEnvironment()

    @Override
    public Map<String, String> getEnvironment() {
        return delegate().getEnvironment();
    }

    // @see com.redprairie.moca.client.MocaConnection#setEnvironment(java.util.Map)

    @Override
    public void setEnvironment(Map<String, String> env) {
        delegate().setEnvironment(env);
    }

    // @see com.redprairie.moca.client.MocaConnection#setTimeout(long)

    @Override
    public void setTimeout(long ms) {
        delegate().setTimeout(ms);
    }

    // @see com.redprairie.moca.client.MocaConnection#getServerKey()

    @Override
    public String getServerKey() {
        return delegate().getServerKey();
    }
        
    protected MocaConnection delegate() {
        return _mocaConn;
    }

}
