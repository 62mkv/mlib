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

package com.redprairie.moca.cluster.manager;

import java.util.UUID;

import org.junit.Test;

import com.redprairie.moca.cluster.manager.simulator.ClusterManager;
import com.redprairie.moca.cluster.manager.simulator.ClusterState;
import com.redprairie.moca.cluster.manager.simulator.NodeConfiguration;
import com.redprairie.moca.cluster.manager.simulator.NodeState;

/**
 * Integration tests using Fixed role managers
 */
public class TI_FixedRoleManager extends AbstractClusterSpecificRoleManagerTest {
    
    @Override
    protected RoleManagerType getRoleManagerType() {
        return RoleManagerType.FIXED;
    }

    @Override
    protected ClusterState getClusterStateWithTwoNodesThreeRoles(NodeConfiguration nodeConfig0, NodeConfiguration nodeConfig1) {
        return ClusterState.builder()
                            .addNode(NodeState.builder(nodeConfig0.getMocaPort())
                                              .includedRoles("node0")
                                              .excludedRoles("node1", "node2")
                                              .build())
                            .addNode(NodeState.builder(nodeConfig1.getMocaPort())
                                              .includedRoles("node1")
                                              .excludedRoles("node0", "node2")
                                              .build())
                            .build();
    }

    @Override
    protected ClusterState getClusterStateAfterThreeNodesStarted(NodeConfiguration nodeConfig0, NodeConfiguration nodeConfig1, NodeConfiguration nodeConfig2 ) {
        
        return ClusterState.builder()
                            .addNode(NodeState.builder(nodeConfig0.getMocaPort()) // Node0 with role node0
                                              .includedRoles("node0")
                                              .expectedRolesSize(1)
                                              .build())
                            .addNode(NodeState.builder(nodeConfig1.getMocaPort()) // Node1 with role node1
                                              .includedRoles("node1")
                                              .expectedRolesSize(1)
                                              .build())
                            .addNode(NodeState.builder(nodeConfig2.getMocaPort()) // Node2 with role node2
                                              .includedRoles("node2")
                                              .expectedRolesSize(1)
                                              .build())
                            .build();
        }
       
    

    @Override
    protected ClusterState getClusterStateForFirstSubcluster(NodeConfiguration nodeConfig0) {
        
        
        return ClusterState.builder()
                            .addNode(NodeState.builder(nodeConfig0.getMocaPort()) // Node0 by itself with role node0
                                
                                              .includedRoles("node0")
                                              .expectedRolesSize(1)
                                              .build())
                            .build();
    }

    @Override
    protected ClusterState getClusterStateForSecondSubcluster(NodeConfiguration nodeConfig1, NodeConfiguration nodeConfig2) {
        return ClusterState.builder()
                            .addNode(NodeState.builder(nodeConfig1.getMocaPort()) // Node1 with role node1
                                              .includedRoles("node1")
                                              .expectedRolesSize(1)
                                              .build())
                            .addNode(NodeState.builder(nodeConfig2.getMocaPort()) // Node2 with role node2
                                              .includedRoles("node2")
                                              .expectedRolesSize(1)
                                              .build())
                            .build();
    }
    
    /**
     * When three nodes are started and the second node leaves, the first
     * and third nodes should just hold onto their existing roles (fixed)
     */
    @Override
    protected ClusterState getClusterStateAfterSecondNodeLeft(NodeConfiguration nodeConfig0, NodeConfiguration nodeConfig1) {
        return ClusterState.builder()
                            .addNode(NodeState.builder(nodeConfig0.getMocaPort()) // Node0 with role node0
                                              .includedRoles("node0")
                                              .expectedRolesSize(1)
                                              .build())
                            .addNode(NodeState.builder(nodeConfig1.getMocaPort()) // Node2 with role node2
                                              .includedRoles("node2")
                                              .expectedRolesSize(1)
                                              .build())
                            .build();
    }
    
    /**
     * Integration tests that tests bringing up 3 nodes all using the FIXED
     * role manager. However, in this case node2 has the role "never" which
     * isn't in any of the corresponding jobs therefore this will never be allocated.
     * Verifies that the first two nodes get their corresponding roles (node0 and node1)
     * and then the last node doesn't have any roles.
     * @throws Exception
     */
    @Test
    public void testFixedRoleManagerTwoRolesAllocated() throws Exception {
       
        ClusterManager.Builder builder = ClusterManager.builder();
        String clusterName = UUID.randomUUID().toString();
        NodeConfiguration nodeConfig0 = newNodeConfiguration(getRoleManagerType(), clusterName, "node0", false);
        NodeConfiguration nodeConfig1 = newNodeConfiguration(getRoleManagerType(), clusterName, "node1", false);
        NodeConfiguration nodeConfig2 = newNodeConfiguration(getRoleManagerType(), clusterName, "never", false);
        
        builder.addNode(nodeConfig0);
        builder.addNode(nodeConfig1);
        builder.addNode(nodeConfig2);
        
        startNewClusterManagerWithLogging(builder, "TI_FixedRoleManager_testFixedRoleManagerTwoRolesAllocated");
        getTestUtils().validateCluster(
            "Cluster state was incorrect after startup",
            ClusterState.builder()
                        .addNode(NodeState.builder(nodeConfig0.getMocaPort()) // Node0 has role node0
                                          .includedRoles("node0")
                                          .expectedRolesSize(1)
                                          .build())
                        .addNode(NodeState.builder(nodeConfig1.getMocaPort()) // Node1 has role node1
                                          .includedRoles("node1")
                                          .expectedRolesSize(1)
                                          .build())
                        .addNode(NodeState.builder(nodeConfig2.getMocaPort()) // Node2 has no roles
                                          .expectedRolesSize(0)
                                          .build())
                        .build(), true);
    }
         
}
