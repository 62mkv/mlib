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

package com.redprairie.moca.cluster.jgroups;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jgroups.conf.ClassConfigurator;
import org.jgroups.protocols.CENTRAL_EXECUTOR;

/**
 * Moca based central executor that exposes some internal state
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MocaCentralExecutor extends CENTRAL_EXECUTOR {
    static {
        ClassConfigurator.addProtocol((short)1234, MocaCentralExecutor.class);
    }
    public Map<Owner, Runnable> getAwaitingReturnCopy() {
        synchronized (_awaitingReturn) {
            return new HashMap<Owner, Runnable>(_awaitingReturn);
        }
    }
    
    public Collection<Runnable> getAwaitingConsumer() {
        return Collections.unmodifiableCollection(_awaitingConsumer);
    }
}
