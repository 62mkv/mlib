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

package com.sam.moca.cluster.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.sam.moca.cluster.Node;
import com.sam.moca.cluster.RoleDefinition;

/**
 * This is a basic implementation of the Cluster Role Manager meant to be used with
 * non-clustered deployments.  It behaves as a empty implementation.
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author eknapp
 */
public class NonClusteredRoleManager implements ClusterRoleManager {

    // @see com.sam.moca.cluster.MocaClusterMembershipListener#notifyMembership(com.sam.moca.cluster.Node, java.util.List, java.util.List)
    @Override
    public void notifyMembership(Node local, List<Node> members, List<Node> joiners, List<Node> leavers, Boolean isMerge) {
    }

    // @see com.sam.moca.cluster.manager.ClusterRoleManager#start(com.sam.moca.cluster.Node)
    @Override
    public void start(Node node) {
    }

    // @see com.sam.moca.cluster.manager.ClusterRoleManager#stop()
    @Override
    public void stop() {
    }

    // @see com.sam.moca.cluster.manager.ClusterRoleManager#getClusterRoles()
    @Override
    public Multimap<Node, RoleDefinition> getClusterRoles() {
        return HashMultimap.create();
    }

    // @see com.sam.moca.cluster.manager.ClusterRoleManager#getClusterNodes(com.sam.moca.cluster.RoleDefinition)
    @Override
    public Set<Node> getClusterNodes(RoleDefinition role) {
        return new HashSet<Node>();
    }

}
