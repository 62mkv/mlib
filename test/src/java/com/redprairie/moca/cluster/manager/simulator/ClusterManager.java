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

package com.redprairie.moca.cluster.manager.simulator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.cluster.manager.AbstractClusterManagerTest;

import static org.junit.Assert.*;

/**
 * 
 * The ClusterManager is a test class used for spinning up
 * nodes for cluster integration testing.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class ClusterManager {
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        
        private Builder() {}
        
        /**
         * Adds a node for the cluster with the given configuration
         * @param config The node configuration
         * @return
         */
        public Builder addNode(NodeConfiguration config) {
            _nodeConfigurations.add(config);
            return this;
        }
        
        /**
         * Adds "count" amount of nodes to the cluster configuration
         * using the given base configuration
         * @param count    The number of nodes to add
         * @param baseConfig The base configuration
         * @return
         */
        public Builder addNodes(int count, NodeConfiguration baseConfig) {
            String clusterName = UUID.randomUUID().toString();
            for (int i = 0; i < count; i++) {
                addNode(NodeConfiguration
                    .builderFrom(
                        AbstractClusterManagerTest.generateUniquePort(),
                        AbstractClusterManagerTest.generateUniquePort(),
                        AbstractClusterManagerTest.generateUniquePort(),
                        clusterName,
                        baseConfig,
                        false).build());
            }
            return this;
        }
        
        /**
         * The amount of time to stagger between node startups
         * @param time  The time
         * @param unit  The time unit
         * @return
         */
        public Builder staggerTime(long time, TimeUnit unit) {
            _staggerTimeMs = unit.toMillis(time);
            return this;
        }
        
        /**
         * Copies the builder configuration but without the node configurations
         * @return
         */
        public Builder copyWithoutNodes() {
            Builder builder = new Builder();
            builder.staggerTime(this._staggerTimeMs, TimeUnit.MILLISECONDS);
            return builder;
        }
        
        /**
         * Gets the defined node configurations for the builder
         * @return
         */
        public List<NodeConfiguration> getDefinedNodes() {
            return Collections.unmodifiableList(_nodeConfigurations);
        }
        
        public ClusterManager build() {
            return new ClusterManager(this);
        }
        
        private final List<NodeConfiguration> _nodeConfigurations = new ArrayList<NodeConfiguration>();
        private long _staggerTimeMs;
    }

    
    private ClusterManager(Builder builder) {
        assertNotNull(System.getenv("MOCADIR"));
        assertNotNull(System.getenv("LESDIR"));
        _nodes = getNodes(builder.getDefinedNodes());
        _staggerTimeMs = builder._staggerTimeMs;
    }
    
    /**
     * Starts the cluster waiting for the given time that all the nodes actually started
     * @param establishTimeout The time to wait
     * @param timeoutUnit      The time unit
     * @throws IOException
     */
    public synchronized void start(long establishTimeout, TimeUnit timeoutUnit) throws IOException {
        assertFalse(_started);
        try {
            startAllNodes(establishTimeout, timeoutUnit);
        }
        catch (IOException ex) {
            stopAllNodes();
            throw ex;
        }
        _started = true;
    }
    
    /**
     * Stops all nodes in the cluster
     * @throws IOException
     */
    public synchronized void stop() throws IOException {
        assertTrue(_started);
        stopAllNodes();
        _started = false;
    }
    
    public synchronized void stopAndRemoveNode(ClusterNode node) {
        node.stop();
        _nodes.remove(node.getConfiguration().getMocaPort());
    }
    
    private void startAllNodes(long establishTimeout, TimeUnit timeoutUnit) throws IOException {
        // Start all the nodes which will be asynchronous as the process spins up
        for (ClusterNode node : _nodes.values()) {
            node.asyncStart();
            if (node != _nodes.lastEntry().getValue()) {
                try {
                    Thread.sleep(_staggerTimeMs);
                }
                catch (InterruptedException e) {
                    throw new MocaInterruptedException(e);
                }
            }
           
        }
        
        // Wait for all the nodes to actually be established
        for (ClusterNode node : _nodes.values()) {
           node.getConnection(establishTimeout, timeoutUnit);
        }
    }
    
    private void stopAllNodes() throws IOException {
        for (ClusterNode node : _nodes.values()) {
            node.stop();
        }
    }
    
    /**
     * Gets the nodes in the cluster
     * @return
     */
    public Collection<ClusterNode> getNodes() {
        return _nodes.values();
    }
    
    public ClusterNode getNode(NodeConfiguration nodeConfig) {
        return _nodes.get(nodeConfig.getMocaPort());
    }
    
    /**
     * Adds and starts a new node in the cluster given the configuration. Waits for the given
     * time to establish a connection to the new node before giving up.
     * @param nodeConfig
     * @param establishTimeout
     * @param unit
     * @throws IOException
     */
    public synchronized void startNewNode(NodeConfiguration nodeConfig, long establishTimeout, TimeUnit unit) throws IOException {
        assertNotNull(nodeConfig);
        ClusterNode node = new ClusterNode(nodeConfig);
        node.asyncStart();
        node.getConnection(establishTimeout, unit);
        _nodes.put(node.getConfiguration().getMocaPort(), node);
        
    }
    
    private static ConcurrentSkipListMap<Integer, ClusterNode> getNodes(List<NodeConfiguration> nodeConfigurations) {
        ConcurrentSkipListMap<Integer, ClusterNode> nodes = 
                new ConcurrentSkipListMap<Integer, ClusterNode>();
        for (NodeConfiguration nodeConfig : nodeConfigurations) {
            ClusterNode node = new ClusterNode(nodeConfig);
            nodes.put(node.getConfiguration().getMocaPort(), node);
        }
        
        return nodes;
    }

    // This map tracks node MOCA port to cluster node
    private final ConcurrentSkipListMap<Integer, ClusterNode> _nodes;
    private final long _staggerTimeMs;
    private boolean _started;
}
