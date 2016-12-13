/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.cluster.manager;

import java.util.Set;

import com.google.common.collect.Multimap;
import com.redprairie.moca.cluster.MocaClusterMembershipListener;
import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.cluster.RoleDefinition;

/**
 * This is the common interface describing lifetime methods for the various
 * role managers.  Also a common method to retrieve all the known roles from
 * the manager is avaliable.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public interface ClusterRoleManager extends MocaClusterMembershipListener {
    public void start(Node node);
    
    public void stop();
    
    /**
     * Returns a multimap that is a copy of what the cluster roles at the
     * time invoked. 
     * @return
     */
    public Multimap<Node, RoleDefinition> getClusterRoles();
    
    /**
     * Returns a set of nodes that can run the provided role
     * @return
     */
    public Set<Node> getClusterNodes(RoleDefinition role);
}
