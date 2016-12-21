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

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.sam.moca.MocaException;
import com.sam.moca.cluster.manager.simulator.ClusterManager;
import com.sam.moca.cluster.manager.simulator.ClusterNode;
import com.sam.moca.cluster.manager.simulator.NodeConfiguration;

import org.junit.Test;


/**
 * Simple integration tests for clustering, these don't test roles at all. This
 * should be subclassed to use different implementation types e.g. UDP vs TCP.
 */
public abstract class AbstractClusterTests extends AbstractClusterManagerTest {
    
    protected String clusterName = UUID.randomUUID().toString();
    /**
     * Tests spinning up 5 nodes and removing them one by one, validating
     * that the size of the cluster is correct reported by both Infinispan
     * and also the cluster URLs cache.
     * @throws IOException
     * @throws InterruptedException
     * @throws MocaException
     */
    @Test
    public void testKillNodesOneByOne() throws IOException, InterruptedException, MocaException {
        
        String clazzName = this.getClass().getSimpleName();
        startNewClusterManagerWithLogging(ClusterManager.builder().addNodes(5, getBaseNodeConfiguration()),
                clazzName + "_testKillNodesOneByOne");
        
        for (ClusterNode node : getManager().getNodes()) {
            getTestUtils().writeLineWithDate("Killing node {" + node + "}");
            getManager().stopAndRemoveNode(node);
            getTestUtils().waitForClusterState("Cluster state was incorrect after killing one node",
                1, TimeUnit.MINUTES);
        }
    }
    
    /**
     * Gets the base {@link NodeConfiguration} to use for all nodes in the test
     * {@link #testKillNodesOneByOne()}.
     * @return The base node configuration
     */
    protected abstract NodeConfiguration getBaseNodeConfiguration();
}
