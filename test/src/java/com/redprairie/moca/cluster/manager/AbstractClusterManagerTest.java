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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.MocaRuntimeException;
import com.redprairie.moca.cluster.manager.simulator.ClusterManager;
import com.redprairie.moca.cluster.manager.simulator.ClusterTestUtils;
import com.redprairie.moca.cluster.manager.simulator.NodeConfiguration;
import com.redprairie.moca.util.AbstractMocaJunit4TestCase;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 
 * An abstract test class to assist with cluster integration tests.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class AbstractClusterManagerTest extends AbstractMocaJunit4TestCase {
    
    public static enum RoleManagerType {
        DYNAMIC,
        FIXED,
        PREFERRED
    }
    
    /**
     * The base MOCA port used when creating new nodes
     */

    public static int generateUniquePort() {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
        catch (IOException e) {
            throw new MocaRuntimeException(502, e.getMessage());
        }

    }

    public static final String ROLES_ENV_VAR = "ROLES_ENV_VAR";
    public static final String ROLE_MANAGER_ENV_VAR = "ROLE_MANAGER_ENV_VAR"; 
    
    @Override
    protected void mocaSetUp() throws Exception {
        // Safety check that if this gets shutdown mid run we kill the cluster
        if (!_addedShutdownHook) {
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

                @Override
                public void run() {
                    if (_clusterManager != null) {
                        try {
                            _clusterManager.stop();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                
            }));
            _addedShutdownHook = true;
        }
    }
    
    @Override
    protected void mocaTearDown() throws Exception {
        // After each test print the cluster state for debugging purposes then shut it down
        if (_clusterManager != null) {
            try {
                _testUtils.printClusterState();
            }
            finally {
                _clusterManager.stop();
                _clusterManager = null;
            }
        }
        
    }
    
    /**
     * Creates a new node configuration using the given environment variables.
     * This by default will use the registry under 
     * $MOCADIR/test/src/java/com/redprairie/moca/cluster/manager/registries/testRegistryUdp
     * @param roleManager The role manager to use
     * @param roles       The roles for the node
     * @return
     */
    protected NodeConfiguration newNodeConfiguration(RoleManagerType roleManager,
                                                     String clusterName,
                                                     String roles,
                                                     boolean isAspectJ) {
        return newNodeConfiguration(roleManager, clusterName, roles,
            Collections.<String, String> emptyMap(), isAspectJ);
    }
    
    /**
     * Creates a new node configuration using the given environment variables.
     * This by default will use the registry under 
     * $MOCADIR/test/src/java/com/redprairie/moca/cluster/manager/registries/testRegistryUdp
     * @param roleManager The role manager to use
     * @param roles       The roles for the node
     * @param envVars     Any additional environment variables, use Collections.emptyMap() if none
     * @param isAspectJ   Indicates if we should use aspectj javaagent arg
     * @return
     */
    protected NodeConfiguration newNodeConfiguration(RoleManagerType roleManager,
                                                     String clusterName,
                                                     String roles,
                                                     Map<String, String> envVars,
                                                     boolean isAspectJ) {
        return newNodeConfiguration(
            roleManager,
            clusterName,
            roles,
            envVars,
            System.getenv("MOCADIR")
                    + "/test/src/java/com/redprairie/moca/cluster/manager/registries/testRegistryUdp",
                    isAspectJ);
    }
    
    /**
     * Creates a new node configuration using the given environment variables and
     * registry path.
     * @param roleManager  The role manager to use
     * @param roles        The roles for the node
     * @param envVars      Any additional environment variables, use Collections.emptyMap() if none
     * @param registryPath The absolute path to the registry to use
     * @param isAspectJ    Indicates if we should use aspectj javaagent arg
     * @return
     */
    protected NodeConfiguration newNodeConfiguration(RoleManagerType roleManager, String clusterName, String roles,
                                                     Map<String, String> envVars, String registryPath, boolean isAspectJ) {
        assertNotNull("The role manager must be specified", roleManager);
        assertNotNull("The roles must be specified", roles);
        assertTrue("The specified registry does not exist: " + registryPath,
            new File(registryPath).exists());
        Map<String, String> allEnvVars = new HashMap<String, String>(envVars);
        allEnvVars.put(ROLE_MANAGER_ENV_VAR, roleManager.toString());
        allEnvVars.put(ROLES_ENV_VAR, roles);
        
        String memoryFilePath = _moca.getRegistryValue(MocaRegistry.REGKEY_SERVER_MEMORY_FILE);
        NodeConfiguration config = NodeConfiguration.builder(getNodePort(), getNodePort(), getNodePort(), clusterName, isAspectJ)
                                                    .registryPath(registryPath)
                                                    .environmentVariables(allEnvVars)
                                                    .memoryFilePath(memoryFilePath)
                                                    .build();
       
        return config;
    }
    
    /**
     * Gets the MOCA port for the node ID (zero based).
     * @param nodeId the node ID
     * @return
     */
    protected int getNodePort() {
        return generateUniquePort();
    }
    
    /**
     * Starts a new cluster manager with the given builder with logging for each
     * node being written to $LESDIR/log/cluster-integration-testing/<testName>
     * @param builder    The cluster manager builder
     * @param testName   The test name
     * @throws IOException
     * @throws MocaException
     */
    protected void startNewClusterManagerWithLogging(ClusterManager.Builder builder, String testName) throws IOException, MocaException {
        startNewClusterManagerWithLogging(builder, testName, true);
    }
    
    /**
     * Starts a new cluster manager with the given builder with logging for each
     * node being written to $LESDIR/log/cluster-integration-testing/<testName>
     * @param builder    The cluster manager builder
     * @param testName   The test name
     * @param validateAfterStartup whether to validate the cluster after startup
     * @throws IOException
     * @throws MocaException
     */
    protected void startNewClusterManagerWithLogging(ClusterManager.Builder builder, String testName, boolean validateAfterStartup) throws IOException, MocaException {
        String lesDir = System.getenv("LESDIR");
        assertNotNull(lesDir);
        File logDirectory = new File(lesDir, "log/cluster-integration-testing");
        startNewClusterManagerWithLogging(builder, logDirectory, testName, validateAfterStartup);
    }
    
    /**
     * Starts a new cluster manager with the given builder with logging for each
     * node being written to the given log directory so the following format:
     * <logDirectory>/<testName>
     * @param originalBuilder The original builder used as the base configuration
     * @param baseDirectory    The directory to write log files to
     * @param testName
     * @param validateAfterStartup whether to validate the cluster after startup
     * @throws IOException
     * @throws MocaException
     */
    protected void startNewClusterManagerWithLogging(ClusterManager.Builder originalBuilder, File baseDirectory, String testName, boolean validateAfterStartup) throws IOException, MocaException {
        if (!baseDirectory.exists()) {
            assertTrue(baseDirectory.mkdir());
        }
        
        File testLogFolder = new File(baseDirectory, testName);
        if (!testLogFolder.exists()) {
            assertTrue(testLogFolder.mkdir());
        }
        
        // Copy the builder and recreate the node configurations but this time with logging via PrintStreams
        ClusterManager.Builder newBuilder = originalBuilder.copyWithoutNodes();
        for (NodeConfiguration config : originalBuilder.getDefinedNodes()) {
            NodeConfiguration.Builder nodeBuilder = NodeConfiguration.builderFrom(config.getMocaPort(), config.getRmiPort(), config.getClassicPort(), config.getClusterName(), config, config.isAspectJ());
            newBuilder.addNode(nodeBuilder.outputStream(new PrintStream(
                                                 new File(testLogFolder, "Node-" + config.getMocaPort() + ".log"), "UTF-8"))
                                          .build());
        }
        
        startNewClusterManager(new File(testLogFolder, "testRunner.log"), newBuilder, 2, TimeUnit.MINUTES, validateAfterStartup);
    }
    
    /**
     * Starts a cluster manager using the given builder. Waits for the given timeout
     * for the cluster to fully start before an exception is thrown.
     * @param outputFile The file to log test output to
     * @param builder  The cluster builder
     * @param timeout  The timeout
     * @param unit     The unit for the timeout
     * @param validateAfterStartup whether to validate the cluster after startup
     * @throws IOException
     * @throws MocaException
     */
    private void startNewClusterManager(File outputFile,
                                        ClusterManager.Builder builder, long timeout, TimeUnit unit, boolean validateAfterStartup) throws IOException, MocaException {
        _clusterManager = builder.build();
        _testUtils = new ClusterTestUtils(new PrintStream(outputFile, "UTF-8"), _clusterManager);
        _clusterManager.start(timeout, unit);
        
        if (validateAfterStartup) {
            _testUtils.validateCluster("Cluster state was incorrect after startup");
        }
    }
    
    /**
     * Gets a reference to the cluster test utils which are set once the manager is started.
     * @return
     */
    protected ClusterTestUtils getTestUtils() {
        return _testUtils;
    }
    
    protected ClusterManager getManager() {
        return _clusterManager;
    }
    
    protected void writeLine(String line) {
        _testUtils.writeLine(line);
    }
    
    private ClusterManager _clusterManager;
    private ClusterTestUtils _testUtils;
    private boolean _addedShutdownHook = false;

}