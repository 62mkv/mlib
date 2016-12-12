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

package com.redprairie.moca.cluster.manager.simulator;

import java.io.File;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.redprairie.util.ArgCheck;

import static org.junit.Assert.*;

/**
 * 
 * Indicates the configuration for a {@link ClusterNode}
 * 
 * Copyright (c) 2014 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class NodeConfiguration {
    
    public static Builder builder(int mocaPort, int rmiPort, int classicPort, String clusterName, boolean isAspectJ) {
        return new Builder(mocaPort, rmiPort, classicPort, clusterName, isAspectJ);
    }
    
    /**
     * Creates a NodeConfiguration builder from the base configuration.
     * @param baseConfig the base config to copy from
     * @param isAspectJ Indicates if we should use aspectj javaagent arg
     * @param the MOCA port to use
     * @return
     */
    public static Builder builderFrom(int mocaPort, int rmiPort,
                                      int classicPort, String clusterName,
                                      NodeConfiguration baseConfig, boolean isAspectJ) {
        ArgCheck.notNull(baseConfig);
        Builder builder = new Builder(mocaPort, rmiPort, classicPort,
            clusterName, isAspectJ);
        builder
            .registryPath(baseConfig.getRegistryPath())
            .memoryFilePath(baseConfig.getMemoryFilePath())
            .environmentVariables(
                baseConfig.getAdditionalEnvironmentVariables())
            .outputStream(baseConfig.getOutputStream());
        return builder;
    }
    
    public static class Builder {

        private Builder(int mocaPort, int rmiPort, int classicPort,
                String clusterName, boolean isAspectJ) {
            _mocaPort = mocaPort;
            _rmiPort = rmiPort;
            _classicPort = classicPort;
            _clusterName = clusterName;
            _isAspectJ = isAspectJ;
        }
        
        /**
         * Specifies the registry path for the node. Default is whatever
         * is defined by the MOCA_REGISTRY environment variable.
         * @param registryPath
         * @return
         */
        public Builder registryPath(String registryPath) {
            _registryPath = registryPath;
            return this;
        }
        
        /**
         * Optionally specifies the memory file path the node should use
         * @param memoryFilePath
         * @return
         */
        public Builder memoryFilePath(String memoryFilePath) {
            _memoryFilePath = memoryFilePath;
            return this;
        }
        
        /**
         * Specifies additional environment variables to bootstrap the node with
         * @param additionalEnvVars
         * @return
         */
        public Builder environmentVariables(Map<String, String> additionalEnvVars) {
            _additionalEnvVars = new HashMap<String, String>(additionalEnvVars);
            return this;
        }
        
        /**
         * Specifies the stream to write the nodes output to, default is System.out
         * @param stream
         * @return
         */
        public Builder outputStream(OutputStream stream) {
            _outputStream = stream;
            return this;
        }
        
        public NodeConfiguration build() {
            return new NodeConfiguration(this);
        }
     
        private final int _mocaPort;
        private final int _classicPort;
        private final int _rmiPort;
        private final String _clusterName;
        private final boolean _isAspectJ;
        private String _registryPath = System.getenv("MOCA_REGISTRY");
        private Map<String, String> _additionalEnvVars = Collections.emptyMap();
        private String _memoryFilePath;
        private OutputStream _outputStream = System.out;
    }
    
    private NodeConfiguration(Builder builder) {
        String baseLesDir = System.getenv("LESDIR");
        assertNotNull("LESDIR environment variable must be defined", baseLesDir);
        _mocaPort = builder._mocaPort;
        _rmiPort = builder._rmiPort;
        _classicPort = builder._classicPort;
        _mocaRegistryPath = builder._registryPath;
        _outputStream = builder._outputStream;
        _clusterName = builder._clusterName;
        _isAspectJ = builder._isAspectJ;
        _envVars = new HashMap<String, String>(builder._additionalEnvVars);
        // Each node needs a unique LESDIR so take the parent directory
        // of LESDIR and create a new folder under each for each node.
        File lesDirParent = new File(baseLesDir).getParentFile();
        _lesDir = new File(lesDirParent, "les-cluster-testing" + File.separatorChar + _mocaPort);
        _envVars.put("LESDIR", _lesDir.getAbsolutePath());
        _envVars.put("MOCA_NODE_NAME", "NODE" + builder._mocaPort);
        _memoryFilePath = builder._memoryFilePath;
    }
    
    
    // @see java.lang.Object#toString()
    
    @Override
    public String toString() {
        return "NodeConfiguration [_mocaPort=" + _mocaPort + ", _rmiPort="
                + _rmiPort + ", _classicPort=" + _classicPort
                + ", _clusterName=" + _clusterName + "]";
    }

    public Map<String, String> getAdditionalEnvironmentVariables() {
        return Collections.unmodifiableMap(_envVars);
    }
    
    public int getMocaPort() {
        return _mocaPort;
    }
    
    public int getRmiPort() {
        return _rmiPort;
    }

    public int getClassicPort() {
        return _classicPort;
    }

    public String getRegistryPath() {
        return _mocaRegistryPath;
    }
    
    /**
     * @return Returns the _clusterName.
     */
    public String getClusterName() {
        return _clusterName;
    }

    public OutputStream getOutputStream() {
        return _outputStream;
    }

    /**
     * Gets the memory file path, this may return NULL
     * @return The memory file path or null if not specified
     */
    public String getMemoryFilePath() {
        return _memoryFilePath;
    }

    public File getLesDir() {
        return _lesDir;
    }
    
    public boolean isAspectJ() {
        return _isAspectJ;
    }
    
    
    private final int _mocaPort;
    private final int _rmiPort;
    private final int _classicPort;
    private final String _clusterName;
    private final String _mocaRegistryPath;
    private final boolean _isAspectJ;
    private final OutputStream _outputStream;
    private final Map<String, String> _envVars;
    private final String _memoryFilePath;
    private final File _lesDir;

}
