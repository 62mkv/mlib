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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.redprairie.moca.cluster.manager.simulator.ClusterManager;
import com.redprairie.moca.cluster.manager.simulator.ClusterNode;
import com.redprairie.moca.cluster.manager.simulator.ClusterState;
import com.redprairie.moca.cluster.manager.simulator.ClusterTestUtils;
import com.redprairie.moca.cluster.manager.simulator.NodeConfiguration;

import static org.junit.Assert.*;

/**
 * 
 * Tests for a specific role manager type, this tests certain scenarios and allows
 * subclasses to implement how they should behave for validation.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public abstract class AbstractClusterSpecificRoleManagerTest extends AbstractClusterRolesTest {
    
    /**
     * Scenario that tests the following:
     * Three roles/jobs (node0, node1, and node2)
     * 
     * Two nodes:
     * node0 = role node0
     * node1 = role node1
     * 
     * See the validation method {@link #getClusterStateWithTwoNodesThreeRoles()}
     * @throws Exception
     */
    @Test
    public void testTwoNodesThreeRoles() throws Exception {
        RoleManagerType type = getManagerTypeWithVerify();
        String clusterName = UUID.randomUUID().toString();
        ClusterManager.Builder builder = ClusterManager.builder();
        NodeConfiguration nodeConfig0 = newNodeConfiguration(type, clusterName, "node0", false);
        NodeConfiguration nodeConfig1 = newNodeConfiguration(type, clusterName, "node1", false);
        
        builder.addNode(nodeConfig0);
        builder.addNode(nodeConfig1);
        startNewClusterManagerWithLogging(builder, getTestName("testTwoNodesThreeRoles"));
        ClusterState expectedState = getClusterStateWithTwoNodesThreeRoles(nodeConfig0, nodeConfig1);
        assertNotNull("getClusterStateWithTwoNodesThreeRoles() cannot return null", expectedState);
        getTestUtils().validateCluster("Cluster state was incorrect after startup with two nodes and three roles",
            expectedState, true);
    }
    
    /**
     * In this scenario three nodes are started up (node0, node1, and node2) with
     * their respective roles and then node1 (second node) is taken offline. This test will then
     * validate that node0 and node2 react to this appropriately using the state returned
     * by the method {@link #getClusterStateAfterSecondNodeLeft()}.
     * After that, the node will be started again so the test is back to three nodes started.
     * Then the test takes the first node (node0) offline using the method 
     * {@link #getClusterStateForSecondSubcluster()}.as the behavior should be the same for the splitbrain
     * scenario in {@link #testThreeNodesWithSplitBrain()}.
     * After that is validated the first node is started again and all three nodes are once again validated
     * with {@link #getClusterStateAfterThreeNodesStarted()}.
     * @throws Exception
     */
    @Test
    public void testThreeNodesWithNodesLeavingAndJoining() throws Exception {
        RoleManagerType type = getManagerTypeWithVerify();
        String clusterName = UUID.randomUUID().toString();
        
        ClusterManager.Builder builder = ClusterManager.builder();
        
        NodeConfiguration nodeConfig0 = newNodeConfiguration(type, clusterName, "node0", false);
        NodeConfiguration nodeConfig1 = newNodeConfiguration(type, clusterName, "node1", false);
        NodeConfiguration nodeConfig2 = newNodeConfiguration(type, clusterName, "node2", false);
        
        builder.addNode(nodeConfig0);
        builder.addNode(nodeConfig1);
        builder.addNode(nodeConfig2);
        
   
        startNewClusterManagerWithLogging(builder, getTestName("testThreeNodesWithNodesLeavingAndJoining"));
        final ClusterTestUtils testUtils = getTestUtils();
        
        // All nodes should be up, verify all roles are allocated as expected
        ClusterState allStartedState = getClusterStateAfterThreeNodesStarted(nodeConfig0, nodeConfig1, nodeConfig2);
        assertNotNull("getClusterStateAfterThreeNodesStarted() cannot return null", allStartedState);
        testUtils.waitForClusterState("Failed validating startup state", allStartedState, 1, TimeUnit.MINUTES);
        

        testUtils.writeLineWithDate("Starting and stopping the second node in the cluster...");
        // Test stopping/starting the second node
        testUtils.stopAndStartNodeWithValidation(nodeConfig1,
            getClusterStateAfterSecondNodeLeft(nodeConfig0, nodeConfig2), allStartedState);
        
        // Test stopping/starting the first node (should probably be the leader)
        // The state after the node is stopped should be the same as getClusterStateForSecondSubcluster()
        testUtils.writeLineWithDate("Starting and stopping the first node in the cluster...");
        testUtils.stopAndStartNodeWithValidation(nodeConfig0,
            getClusterStateForSecondSubcluster(nodeConfig1, nodeConfig2), allStartedState);
    }
    
    /**
     * Scenario that tests the following:
     * Three roles/jobs (node0, node1, and node2)
     * 
     * Three nodes:
     * node0 = role node0
     * node1 = role node1
     * node2 = role node2
     * 
     * Additionally, this scenario tests a split brain occurrence where
     * traffic to/from node1 is blocked temporarily. See the following 
     * validation methods that subclasses should implement:
     * {@link #getClusterStateAfterThreeNodesStarted()}
     * {@link #getClusterStateForFirstSubcluster()}
     * {@link #getClusterStateForSecondSubcluster()}
     * @throws Exception
     */
    @Test
    public void testThreeNodesWithSplitBrain() throws Exception {
        RoleManagerType type = getManagerTypeWithVerify();
        String clusterName = UUID.randomUUID().toString();
        ClusterManager.Builder builder = ClusterManager.builder();
        
        NodeConfiguration nodeConfig0 = newNodeConfiguration(type, clusterName, "node0", true);
        NodeConfiguration nodeConfig1 = newNodeConfiguration(type, clusterName, "node1", true);
        NodeConfiguration nodeConfig2 = newNodeConfiguration(type, clusterName, "node2", true);
        
        builder.addNode(nodeConfig0);
        builder.addNode(nodeConfig1);
        builder.addNode(nodeConfig2);
        
        startNewClusterManagerWithLogging(builder, getTestName("testThreeNodesWithSplitBrain"));
        final ClusterTestUtils testUtils = getTestUtils();
        // All nodes should be up, verify all roles are allocated as expected
        ClusterState afterStartedState = getClusterStateAfterThreeNodesStarted(nodeConfig0, nodeConfig1, nodeConfig2);
        assertNotNull("getClusterStateAfterThreeNodesStarted() cannot return null", afterStartedState);
        testUtils.waitForClusterState("Failed validating startup state", afterStartedState, 1, TimeUnit.MINUTES);
        
        // Force the split brain to occur and validate the state of the "sub clusters"
        testUtils.blockUdpOnNode(getManager().getNode(nodeConfig0));
        
        // Validate the first sub cluster
        ClusterState firstSubCluster = getClusterStateForFirstSubcluster(nodeConfig0);
        assertNotNull("getClusterStateForFirstSubcluster() cannot return null", firstSubCluster);
        testUtils.waitForNodesState("Failed validating split brain on first sub cluster",
            firstSubCluster, 1, TimeUnit.MINUTES, getManager().getNode(nodeConfig0));
        
        // Validate the second sub cluster
        ClusterState secondSubCluster = getClusterStateForSecondSubcluster(nodeConfig1, nodeConfig2);
        assertNotNull("getClusterStateForSecondSubcluster() cannot return null", secondSubCluster);
        testUtils.waitForNodesState("Failed validating split brain on second sub cluster",
            secondSubCluster, 15, TimeUnit.SECONDS, getManager().getNode(nodeConfig1), getManager().getNode(nodeConfig2));

        // Unblock the blocked node and validate that the cluster reforms correctly after split brain
        testUtils.unblockUdpOnNode(getManager().getNode(nodeConfig0));
        
        testUtils.waitForClusterState("Failed validating state after recovering from split brain",
            afterStartedState, 1, TimeUnit.MINUTES);
    }
    
    private RoleManagerType getManagerTypeWithVerify() {
        RoleManagerType type = getRoleManagerType();
        assertNotNull("The method getManagerType() cannot return null", type);
        return type;
    }
    
    private String getTestName(String testName) {
        return getRoleManagerType().toString() + "_" + testName;
    }
    
    /**
     * Gets the role manager type for the test suite
     * @return
     */
    protected abstract RoleManagerType getRoleManagerType();
    
    /**
     * Gets the expected cluster state after startup for the following scenario:
     * Three roles/jobs (node0, node1, and node2)
     * 
     * Two nodes started:
     * node0 = role node0
     * node1 = role node1
     * @return
     */
    protected abstract ClusterState getClusterStateWithTwoNodesThreeRoles(NodeConfiguration nodeConfig0, NodeConfiguration nodeConfig1);
    
    /**
     * Gets the expected cluster state after startup for the following scenario:
     * Three roles/jobs (node0, node1, and node2)
     * 
     * Three nodes started:
     * node0 = role node0
     * node1 = role node1
     * node2 = role node2
     * 
     * @return
     */
    protected abstract ClusterState getClusterStateAfterThreeNodesStarted(NodeConfiguration nodeconfig0, NodeConfiguration config1, NodeConfiguration config2);
    
    /**
     * Gets the expected cluster state for the first sub cluster after a split brain occurs by blocking
     * the first nodes UDP communication following the scenario described in {@link #getClusterStateAfterThreeNodesStarted()}
     * @return
     */
    protected abstract ClusterState getClusterStateForFirstSubcluster(NodeConfiguration nodeConfig0);
    
    /**
     * Gets the expected cluster state for the second sub cluster after a split brain occurs by blocking
     * the first nodes UDP communication following the scenario described in {@link #getClusterStateAfterThreeNodesStarted()}
     * @return
     */
    protected abstract ClusterState getClusterStateForSecondSubcluster(NodeConfiguration nodeConfig1, NodeConfiguration nodeConfig2);
    
    /**
     * Gets the expected cluster state when the second node is removed from a three node cluster as
     * described in the scenario {@link #testThreeNodesWithNodesLeavingAndJoining()}
     * @return
     */
    protected abstract ClusterState getClusterStateAfterSecondNodeLeft(NodeConfiguration nodeConfig0, NodeConfiguration nodeConfig1);

}
