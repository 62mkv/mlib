/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2014
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

package com.redprairie.moca.cluster.manager;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.cluster.manager.simulator.ClusterManager;
import com.redprairie.moca.cluster.manager.simulator.ClusterState;
import com.redprairie.moca.cluster.manager.simulator.NodeConfiguration;
import com.redprairie.moca.cluster.manager.simulator.NodeState;

/**
 * Integration tests with multiple nodes using mixed role managers
 * 
 * Copyright (c) 2014 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TI_MixedRoleManagers extends AbstractClusterRolesTest{
    
    /**
     * The goal of this test is to verify that the fixed role manager
     * can steal role node1 from the already running preferred manager.
     * This is done by bringing up the preferred node first and letting it
     * take all 3 roles, then bringing up the fixed node and verifying it
     * steals role node1 from the first node.
     * @throws IOException
     * @throws MocaException
     */
    @Test
    public void testMixedPreferredAndFixed() throws IOException, MocaException {
        testStartupOneThenStealRole("TI_MixedRoleManagers_testMixedPreferredAndFixed", RoleManagerType.FIXED);
    }
    
    /**
     * This test should behave the same way as {@link #testMixedPreferredAndFixed()}
     * except it uses a Preferred node + Dynamic node (which steals back the role)
     * @throws IOException
     * @throws MocaException
     */
    @Test
    public void testMixedPreferredAndDynamic() throws IOException, MocaException {
        testStartupOneThenStealRole("TI_MixedRoleManagers_testMixedPreferredAndDynamic", RoleManagerType.DYNAMIC);
    }
    
    private void testStartupOneThenStealRole(String testName, RoleManagerType secondRoleManager) throws IOException, MocaException {
        //generating a unique cluster name
        String clusterName = UUID.randomUUID().toString();
        
        // Bring up the first node which is a preferred role manager, it should acquire all roles
        NodeConfiguration nodeConfig0 = newNodeConfiguration(RoleManagerType.PREFERRED, clusterName, "node1", false);
        startNewClusterManagerWithLogging(
            ClusterManager.builder().addNode(nodeConfig0), testName);
        getTestUtils().waitForClusterState(
            "Node0 should have acquired all 3 roles",
            ClusterState.builder()
                        .addNode(NodeState.builder(nodeConfig0.getMocaPort())
                                          .includedRoles("node0", "node1", "node2")
                                          .build())
                        .build(),
                        1, TimeUnit.MINUTES);
        
        // Bring up the second node (which should be dynamic or fixed) with the given role node1

        // It should then steal back these two roles (node1 and node2) from the preferred node.
        NodeConfiguration nodeConfig1 = newNodeConfiguration(secondRoleManager, clusterName, "node1,node2", false);
        getManager().startNewNode(
            nodeConfig1,
            2, TimeUnit.MINUTES);

        getTestUtils().waitForClusterState(
            "Node1 should have stole role Node1 from Node0",
            ClusterState
                .builder()

                .addNode(
                    NodeState.builder(nodeConfig0.getMocaPort())
                        .excludedRoles("node1", "node2").build())
                .addNode(
                    NodeState.builder(nodeConfig1.getMocaPort())
                        .includedRoles("node1", "node2").build()).build(), 1,
            TimeUnit.MINUTES);
    }

}
