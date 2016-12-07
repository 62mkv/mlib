/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.cluster.jgroups;

import java.util.concurrent.locks.Lock;

import org.jgroups.blocks.locking.LockService;

import com.redprairie.moca.cluster.ClusterLockManager;
import com.redprairie.moca.cluster.RoleDefinition;

/**
 * This class can be used with a channel in both standard state transfer
 * and streaming state transfer.  State chunks is not supported.
 * 
 * Copyright (c) 2010 RedPrairie Corporation All Rights Reserved
 * 
 * @author wburns
 */
public class JGroupsLockManager implements ClusterLockManager {

    /**
     * @param channel
     * @param preferredRole
     * @param clusterRoleAwareObjects
     */
    public JGroupsLockManager(LockService service) {
        _lockService = service;
    }
    

    // @see com.redprairie.moca.cluster.ClusterLockManager#getLock(com.redprairie.moca.cluster.NodeDefinition)
    @Override
    public Lock getLock(RoleDefinition node) {
        return _lockService.getLock("ClusterRole-" + node.getRoleId());
    }
    
	public Lock getClusterMergeLock() {
		return _lockService.getLock("mergeLock");
	}
    
    private final LockService _lockService;
}