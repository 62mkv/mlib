/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.sam.moca.server.exec;

import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;

/**
 * A unit of execution that takes a ServerContext and returns a result set.
 * Various parts of the command execution tree implement this interface.  In
 * particular, this is a useful abstraction for keeping track of our location
 * in the execution stack.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
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