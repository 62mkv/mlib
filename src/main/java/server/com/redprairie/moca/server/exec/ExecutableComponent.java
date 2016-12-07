/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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

package com.redprairie.moca.server.exec;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;

/**
 * A unit of execution that takes a ServerContext and returns a result set.
 * Various parts of the command execution tree implement this interface.  In
 * particular, this is a useful abstraction for keeping track of our location
 * in the execution stack.
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author cjolly
 * @version $Revision$
 */
public interface ExecutableComponent {
    /**
     * Execute this component or component grouping. This method will cause
     * this component to execute, using the passed-in server context.
     * @param ctx the ServerContext to use to perform the execution
     * @return
     * @throws MocaException
     */
    public MocaResults execute(ServerContext ctx) throws MocaException;
}