/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.moca.cache.infinispan.extension.commands.visitable;

import java.util.List;
import java.util.Map;

import org.infinispan.context.InvocationContext;

/**
 * A command that takes multiple request IDs and returns multiple
 * NodeResponses indicating if each request was successful or not.
 * This is useful for multiple actions being taken e.g., 
 * starting multiple jobs on a node.
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public interface MultipleResponseNodeCommand extends NodeSpecificCommand {

    
    /**
     * The command's perform action. This should return a map of request ID to
     * NodeResponse indicating if the command was successful or not for that
     * particular request ID.
     */
    @Override
    public Map<String, NodeResponse> perform(InvocationContext ctx);
    
    /**
     * An unmodifiable list of request IDs
     * @return The unmodifiable list of request IDs
     */
    List<String> getRequestIds();
}
