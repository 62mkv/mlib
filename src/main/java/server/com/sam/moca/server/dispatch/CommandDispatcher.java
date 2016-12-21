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

package com.sam.moca.server.dispatch;

import java.util.Collection;

import com.sam.moca.MocaArgument;
import com.sam.moca.MocaException;
import com.sam.moca.server.exec.RequestContext;
import com.sam.moca.server.exec.SessionContext;

/**
 * Dispatches commands in the current MOCA execution environment.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public interface CommandDispatcher {

    /**
     * Executes the given command on behalf of the given session. The autocommit
     * state is used to determine whether to automatically commit the
     * transaction (thereby closing it) at the end of the request, or to hang
     * onto the transaction and allow further requests to use same transaction.
     * 
     * @param command the incoming command.
     * @param context the data context passed in to this command. Used for
     *            remote command execution
     * @param args the argument list. Used for remote command execution
     * @param request the request context for the incoming request.
     * @param session the session for the incoming request.
     * @param autoCommit if false, autocommit is disabled.
     * @param remoteMode if true, remote semantics are in place.  Mostly,
     * error code mangling does not occur.
     * @return a <code>DispatchResult</code> object, containing command results,
     *         or an error code and message.
     */
    public DispatchResult executeCommand(String command,
                                         Collection<MocaArgument> context,
                                         Collection<MocaArgument> args,
                                         RequestContext request,
                                         SessionContext session,
                                         boolean autoCommit,
                                         boolean remoteMode);

    /**
     * This method will resolve an exception just as if it was raised from a
     * command execution
     * @param exception The exception to resolve
     * @return The result of the resolved exception.
     */
    public DispatchResult resolveException(MocaException exception);
}