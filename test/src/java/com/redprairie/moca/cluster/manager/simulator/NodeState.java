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
 * Represents the state of a node used for
 * validating it.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class NodeState {
    
    /**
     * Constructs a NodeState Builder with the given port.
     * @param port The MOCA port for the Node State being built
     * @return
     */
    public static Builder builder(int port) {
        return new Builder(port);
    }

    public static class Builder {
        
        private Builder(int nodePort) {
            _port = nodePort;
           
        }
        
        /**
         * The expected number of roles on the node, if not
         * specified then this will not be validated.
         * @param size
         * @return
         */
        public Builder expectedRolesSize(int size) {
            _expectedRolesSize = size;
            return this;
        }
        
        /**
         * The roles that are expected to be included on the node
         * @param roles
         * @return
         */
        public Builder includedRoles(String... roles) {
            _includedRoles = new HashSet<String>(Arrays.asList(roles));
            return this;
        }
        
        /**
         * The roles that are expected to be excluded on the node
         * @param roles
         * @return
         */
        public Builder excludedRoles(String... roles) {
            _excludedRoles = new HashSet<String>(Arrays.asList(roles));
            return this;
        }
        
        public NodeState build() {
            return new NodeState(this);
        }
        
        private Set<String> _includedRoles = Collections.emptySet();
        private Set<String> _excludedRoles = Collections.emptySet();
        private final int _port;
    
        private Integer _expectedRolesSize;
    }
    
    
    private NodeState(Builder builder) {
        _includedRoles = Collections.unmodifiableSet(new HashSet<String>(builder._includedRoles));
        _excludedRoles = Collections.unmodifiableSet(new HashSet<String>(builder._excludedRoles));
        _nodePort = builder._port;
        _expectedRolesSize = builder._expectedRolesSize;
        
    }
    
    public int getNodePort() {
        return _nodePort;
    }
    
    public Set<String> getIncludedRoles() {
        return _includedRoles;
    }
    
    public Set<String> getExcludedRoles() {
        return _excludedRoles;
    }
    
    public Integer getExpectedRolesSize() {
        return _expectedRolesSize;
    }
    
    
    private final Set<String> _includedRoles;
    private final Set<String> _excludedRoles;
    private final int _nodePort;
    private final Integer _expectedRolesSize;
}
