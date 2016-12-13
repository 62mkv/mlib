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

package com.redprairie.moca.server.exec;

import java.util.concurrent.Callable;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.client.MocaConnection;

class RemoteCall implements Callable<MocaResults> {
    public RemoteCall(MocaConnection conn, String command, MocaArgument[] context, MocaArgument[] commandArgs) {
        _conn = conn;
        _command = command;
        _context = context;
        _commandArgs = commandArgs;
    }
    
    // @see java.util.concurrent.Callable#call()
    @Override
    public MocaResults call() throws MocaException {
        return _conn.executeCommandWithContext(_command, _context, _commandArgs);
    }
    
    private final String _command;
    private final MocaConnection _conn;
    private final MocaArgument[] _context;
    private final MocaArgument[] _commandArgs;
}