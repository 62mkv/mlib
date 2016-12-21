/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.sam.moca.cluster.manager;

import java.util.concurrent.TimeUnit;

import org.infinispan.manager.CacheContainer;


import com.sam.moca.cluster.ClusterRoleAware;
import com.sam.moca.cluster.Node;
import com.sam.moca.cluster.RoleDefinition;
import com.sam.moca.cluster.dao.RoleDefinitionDAO;
import com.sam.moca.cluster.jgroups.JGroupsLockManager;

/**
 * This class can be used with a channel in both standard state transfer
 * and streaming state transfer.  State chunks is not supported.
 * 
 * Copyright (c) 2010 Sam Corporation All Rights Reserved
 * 
 * @author wburns
 */
public class DynamicClusterRoleManager extends AbstractManualBasedClusterRoleManager {
    
    public DynamicClusterRoleManager(JGroupsLockManager lockManager, long timer, 
        TimeUnit timeUnit, Iterable<RoleDefinition> manualRoles, 
        Iterable<RoleDefinition> excludeRoles, RoleDefinitionDAO dao, 
        CacheContainer container, ClusterRoleAware aware, 
        ClusterRoleAware... clusterRoleAwareObjects) {
        super(timer, timeUnit, manualRoles, excludeRoles, dao, lockManager, 
            container, aware, clusterRoleAwareObjects);
    }
    

    /**
     * The channel that was provided <b>must</b> be started before calling this.
     */
    public synchronized void start(Node myNode) {
        _ourNode = myNode;
        
        if (!_manualRoles.isEmpty()) {
            obtainManualRoles();
        }
        
        // We always start the dynamic allocation
        super.start(myNode);
    }
}