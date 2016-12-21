/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.sam.moca.cluster.jgroups;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.jgroups.JChannel;
import org.jgroups.blocks.executor.ExecutionService;
import org.jgroups.protocols.Executing.Owner;

/**
 * This is a simple class that extends jgroups ExecutionService to expose
 * some information about queued elements.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MocaExecutionService extends ExecutionService {
    
    public MocaExecutionService() {
        super();
    }
    
    public MocaExecutionService(JChannel channel) {
        super(channel);
    }
    
    public Map<Owner, Runnable> getAwaitingReturnCopy() {
        if (_execProt instanceof MocaCentralExecutor) {
            return ((MocaCentralExecutor)_execProt).getAwaitingReturnCopy();
        }
        return Collections.emptyMap();
    }
    
    public Collection<Runnable> getAwaitingConsumer() {
        if (_execProt instanceof MocaCentralExecutor) {
            return ((MocaCentralExecutor)_execProt).getAwaitingConsumer();
        }
        return Collections.emptyList();
    }
}
