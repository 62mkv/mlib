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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * Represents the expected state of the cluster
 * used with validation via {@link ClusterTestUtils}
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class ClusterState {
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        
        private Builder() {}
        
        /**
         * Adds an expected NodeState to the cluster state
         * @param nodeState The node state
         * @return
         */
        public Builder addNode(NodeState nodeState) {
            _nodeStates.add(nodeState);
            return this;
        }
        
        /**
         * Indicates floating roles for the cluster state. A floating
         * role is one that should exist in ONE of the nodes in the cluster
         * but not more than one.
         * @param roles
         * @return
         */
        public Builder withFloatingRoles(String... roles) {
            _floatingRoles = new HashSet<String>(Arrays.asList(roles));
            return this;
        }
        
        public ClusterState build() {
            return new ClusterState(this);
        }
        
        private final Set<NodeState> _nodeStates = new HashSet<NodeState>();
        private Set<String> _floatingRoles = Collections.emptySet();
    }
    
    
    public ClusterState(Builder builder) {
        _nodeStates = Collections.unmodifiableSet(new HashSet<NodeState>(builder._nodeStates));
        _floatingRoles = Collections.unmodifiableSet(new HashSet<String>(builder._floatingRoles));
    }
    
    public Set<String> getFloatingRoles() {
        return _floatingRoles;
    }
    
    public Set<NodeState> getNodeStates() {
        return _nodeStates;
    }
    
    public int getExpectedSize() {
        return _nodeStates.size();
    }
    
    
    private final Set<NodeState> _nodeStates;
    private final Set<String> _floatingRoles;

}
