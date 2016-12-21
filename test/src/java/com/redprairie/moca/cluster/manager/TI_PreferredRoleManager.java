/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.sam.moca.cluster.manager;

import com.sam.moca.cluster.manager.simulator.ClusterState;
import com.sam.moca.cluster.manager.simulator.NodeConfiguration;
import com.sam.moca.cluster.manager.simulator.NodeState;

/**
 * 
 * Provides integration validations for the preferred role manager
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TI_PreferredRoleManager extends AbstractClusterSpecificRoleManagerTest {
    
    @Override
    protected RoleManagerType getRoleManagerType() {
        return RoleManagerType.PREFERRED;
    }

    /**
     * For two nodes the third role should be floating (either on node0 or node1)
     */
    @Override
    protected ClusterState getClusterStateWithTwoNodesThreeRoles(NodeConfiguration nodeConfig0, NodeConfiguration nodeConfig1) {
        return ClusterState.builder()
                            .addNode(NodeState.builder(nodeConfig0.getMocaPort())
                                              .includedRoles("node0")
                                              .excludedRoles("node1")
                                              .build())
                            .addNode(NodeState.builder(nodeConfig1.getMocaPort())
                                              .includedRoles("node1")
                                              .excludedRoles("node0")
                                              .build())
                            .withFloatingRoles("node2")
                            .build();
    }

    /**
     * For three nodes each node should have its corresponding role
     */
    @Override
    protected ClusterState getClusterStateAfterThreeNodesStarted(NodeConfiguration nodeConfig0, NodeConfiguration nodeConfig1, NodeConfiguration nodeConfig2) {
        return ClusterState.builder()
                            .addNode(NodeState.builder(nodeConfig0.getMocaPort())
                                              .includedRoles("node0")
                                              .excludedRoles("node1", "node2")
                                              .build())
                            .addNode(NodeState.builder(nodeConfig1.getMocaPort())
                                              .includedRoles("node1")
                                              .excludedRoles("node0", "node2")
                                              .build())
                            .addNode(NodeState.builder(nodeConfig2.getMocaPort())
                                              .includedRoles("node2")
                                              .excludedRoles("node0", "node1")
                                              .build())
                            .build();
    }

    /**
     * During split brain the first node can only see itself so it should
     * acquire all roles
     */
    @Override
    protected ClusterState getClusterStateForFirstSubcluster(NodeConfiguration nodeConfig0) {
        return ClusterState.builder()
                            .addNode(NodeState.builder(nodeConfig0.getMocaPort())
                                              .includedRoles("node0", "node1", "node2")
                                              .build())
                            .build();
    }

    /**
     * During split brain the second and third nodes still see each other so
     * one of them should acquire node0 role.
     */
    @Override
    protected ClusterState getClusterStateForSecondSubcluster(NodeConfiguration nodeConfig1, NodeConfiguration nodeConfig2) {
        return ClusterState.builder()
                            .addNode(NodeState.builder(nodeConfig1.getMocaPort())
                                              .includedRoles("node1")
                                              .excludedRoles("node2")
                                              .build())
                            .addNode(NodeState.builder(nodeConfig2.getMocaPort())
                                              .includedRoles("node2")
                                              .excludedRoles("node1")
                                              .build())
                            .withFloatingRoles("node0")
                            .build();
    }
    
    /**
     * When three nodes are up and the second node is killed one of them
     * should inherit the role "node1".
     */
    @Override
    protected ClusterState getClusterStateAfterSecondNodeLeft(NodeConfiguration nodeConfig0, NodeConfiguration nodeConfig2) {
        return ClusterState.builder()
                            .addNode(NodeState.builder(nodeConfig0.getMocaPort())
                                              .includedRoles("node0")
                                              .excludedRoles("node2")
                                              .build())
                            .addNode(NodeState.builder(nodeConfig2.getMocaPort())
                                              .includedRoles("node2")
                                              .excludedRoles("node0")
                                              .build())
                            .withFloatingRoles("node1")
                            .build();
    }
}
