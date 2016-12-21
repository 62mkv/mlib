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

package com.sam.moca.cluster.manager.simulator;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

import com.sam.moca.MocaException;
import com.sam.moca.MocaInterruptedException;
import com.sam.moca.MocaRegistry;
import com.sam.moca.MocaRuntimeException;
import com.sam.moca.client.ConnectionUtils;
import com.sam.moca.client.LoginFailedException;
import com.sam.moca.client.MocaConnection;
import com.sam.moca.cluster.jgroups.JGroupsChannelFactory;
import com.sam.moca.server.ServerUtils;
import com.sam.util.ArgCheck;
import com.sam.util.ProcessWatcher;

/**
 * 
 * Represents a Node in the cluster, used in conjunction
 * with the {@link ClusterManager}.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public final class ClusterNode {
    
    public static final String TEST_LOGIN_USER = "SUPER";
    public static final String TEST_LOGIN_PASSWORD = "SUPER";
    
    public static final String ASPECTJ_JAR_NAME = "aspectjweaver-1.8.0.jar";
    private static final String ASPECTJ_WEAVER_PATH = "test/3rdparty/aspectj/" + ASPECTJ_JAR_NAME;
    
    ClusterNode(NodeConfiguration config) {
        ArgCheck.notNull(config);
        _config = config;
        assertTrue(new File(config.getRegistryPath()).exists());
    }
    
    /**
     * Gets the configuration used for the node
     * @return
     */
    public NodeConfiguration getConfiguration() {
        return _config;
    }
    
    synchronized void asyncStart() throws IOException {
        System.out.println("Starting node " + _config);
        String mocaDir = System.getenv("MOCADIR");
        assertNotNull("MOCADIR environment variable must be set!", mocaDir);
        List<String> command = new ArrayList<String>();
        command.add("java");
        command.add("-Xmx256m");
        if (_config.isAspectJ()) {
        // AspectJ agent for load time weaving
        command.add(String.format("-javaagent:%s/%s", mocaDir, ASPECTJ_WEAVER_PATH));
        }

        String bindAddr = System.getProperty(JGroupsChannelFactory.JGROUPS_BIND_ADDR);
        if (bindAddr == null) {
            bindAddr = ServerUtils.globalContext()
                .getConfigurationElement(MocaRegistry.REGKEY_CLUSTER_JGROUPS_BIND_ADDR, "LOOPBACK");
        }

        command.add("-Djava.net.preferIPv4Stack=true"); // IPv4 for clustering
        command.add("-D" + JGroupsChannelFactory.JGROUPS_BIND_ADDR + "=" + bindAddr);
        command.add("-cp");
        command.add(System.getenv("MCSDIR") + "/lib/*" + File.pathSeparatorChar
            + System.getenv("MOCADIR") + "/lib/*" + File.pathSeparatorChar
            + System.getenv("MOCADIR") + "/javalib/*");
        // startup class which is a wrapper for MocaServerMain but
        // handles cleanup of the child process if this parent process
        // exits abruptly without shutdown hooks running
        command.add(TestMocaServerMain.class.getName());
        command.add("-p"); // MOCA Port
        command.add(Integer.toString(_config.getMocaPort()));
        command.add("-r"); // RMI Port
        command.add(Integer.toString(_config.getRmiPort()));
        command.add("-P"); // Classic Port
        command.add(Integer.toString(_config.getClassicPort()));
        command.add("-t*"); // Enable tracing
        
        if (_config.getMemoryFilePath() != null) {
            command.add("-m");
            command.add(_config.getMemoryFilePath());
        }
        
        ProcessBuilder b = new ProcessBuilder(command); 
        
        // Add additional environment variables
        b.environment().putAll(System.getenv());
        b.environment().putAll(_config.getAdditionalEnvironmentVariables());
        
        String lesDir = b.environment().get("LESDIR");
        assertFalse("LESDIR must be different than MOCADIR", 
            mocaDir.equals(lesDir));
        File lesDirFile = new File(lesDir);
        // Bootstrap a new empty lesdir for logs/data during the test
        if (!lesDirFile.exists()) {
            assertTrue(new File(lesDirFile, "data").mkdirs());
            assertTrue(new File(lesDirFile, "log").mkdir());
        }
        
        // Bootstrap the new registry leaving a reference to the old one (the one that this test process is running under)
        if (!_config.getRegistryPath().equals(System.getenv("MOCA_REGISTRY"))) {
            b.environment().put("MOCA_REGISTRY_ORIGINAL", System.getenv("MOCA_REGISTRY"));
            b.environment().put("MOCA_REGISTRY", _config.getRegistryPath());
        }
        b.environment().put("MOCA_CLUSTER_NAME", _config.getClusterName());
        b.redirectErrorStream(true);
        _process = b.start();
        final OutputStream outputStream = _config.getOutputStream();
        ProcessWatcher watcher = new NodeProcessWatcher(this) {
                
                @Override
                protected void handleOutput(String line) {
                    try {
                        outputStream.write(line.getBytes(UTF8_CHARSET));
                        outputStream.write(NEW_LINE);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
        _watcherThread = new Thread(watcher);
        _watcherThread.setDaemon(true);
        _watcherThread.start();
    }
    
    /**
     * Gets a reference to the connection for the node, used to execute commands
     * @return
     */
    public synchronized MocaConnection getConnection() {
        return getConnection(0, TimeUnit.NANOSECONDS);
    }
    
    /**
     * Gets a reference to the connection for the node waiting the specified
     * amount of time to establish the connection if needed
     * @param timeout
     * @param unit
     * @return
     */
    public synchronized MocaConnection getConnection(long timeout, TimeUnit unit) {
        if (_mocaConn == null) {
            try {
                buildMocaConnection(unit.toMillis(timeout));
            }
            catch (MocaException e) {
                throw new MocaRuntimeException(e);
            }
        }
        
        return _mocaConn;
    }
    
    synchronized void stop() {
        if (_mocaConn != null) {
            _mocaConn.close();
            _mocaConn = null;
        }
        
        if (_process != null) {
            System.out.println("Killing process: " + _process);
            _process.destroy();
            _process = null; 
        }
        
        if (_watcherThread != null) {
            _watcherThread.interrupt();
        }
        
    }
    
    private synchronized void buildMocaConnection(long timeoutMs) throws MocaException {
        if (_mocaConn != null) return;
        
        System.out.println("    Building MOCA connection");
        long endtime = System.currentTimeMillis() + timeoutMs;
        do {
            try {
                _mocaConn =
                        ConnectionUtils.createConnection(
                            String.format("http://localhost:%d/service", _config.getMocaPort()), null);
                System.out.println("    Connection successfully established");
                break;
            }
            catch (MocaException ex) {
                try {
                    System.out.println("    Failed to connect, trying again in 5 seconds: " + ex);
                    Thread.sleep(5000);
                }
                catch (InterruptedException e) {
                    throw new MocaInterruptedException(e);
                }
            }
        } while (System.currentTimeMillis() <= endtime);
        
        if (_mocaConn == null) {
            throw new RuntimeException("Timed out waiting to establish a " +
               "connection to the following node (check logs for details): " + this);
        }
        
        try {
            ConnectionUtils.login(_mocaConn, TEST_LOGIN_USER, TEST_LOGIN_PASSWORD);
            System.out.println("    Logged in successfully");
        }
        catch (LoginFailedException e) {
            throw new RuntimeException("Failed login after connecting to the following node (check logs for details): " + this, e);
        }
    }

    @Override
    public String toString() {
        return String.format("MOCA Port[%d], RMI Port[%d], Classic Port[%d], Cluster Name[%s]",
            _config.getMocaPort(), _config.getRmiPort(), _config.getClassicPort(), _config.getClusterName());
    }

    private static class NodeProcessWatcher extends ProcessWatcher {

        public NodeProcessWatcher(ClusterNode node) {
            super(node._process);
            _node = node;
        }

        @Override
        protected void processExit(int exitValue) {
            System.out.println(String.format("The process for node [%s] ended with exit value [%d]",
                _node, exitValue));
        }
        
        @Override
        public void run() {
            try {
                super.run();
            }
            catch (Exception ex) {
                System.out.println("Watcher ended for node: " + _node);
            }
        }
        
        protected final ClusterNode _node;
    }
    
    private final NodeConfiguration _config;
    private Process _process;
    private MocaConnection _mocaConn;
    private Thread _watcherThread;
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    private static final byte[] NEW_LINE = System.getProperty("line.separator").getBytes(UTF8_CHARSET); 
}
