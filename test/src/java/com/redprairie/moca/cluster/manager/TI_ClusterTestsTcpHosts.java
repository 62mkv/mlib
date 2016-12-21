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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.sam.moca.MocaRegistry;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.SystemConfigurationException;

import static org.junit.Assert.assertTrue;

/**
 * Test default cluster tcp-hosts settings
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author j1014071
 */
public class TI_ClusterTestsTcpHosts {

    private final static Logger logger = LogManager.getLogger();

    private static final int CLUSTER_SIZE = 3;
    private static String hostname;
    private static String hostaddress;

    private ClusterNodeTcpHosts [] nodes = new ClusterNodeTcpHosts[CLUSTER_SIZE];

    private static final String TCP_PING_TIMEOUT_STD = MocaRegistry.REGKEY_CLUSTER_JGROUPS_TCP_PING_TIMEOUT_DEFAULT;
    private static final String GMS_JOIN_TIMEOUT_STD = MocaRegistry.REGKEY_CLUSTER_JGROUPS_JOIN_TIMEOUT_DEFAULT;  
    private static final String GMS_MERGE_TIMEOUT_STD = MocaRegistry.REGKEY_CLUSTER_JGROUPS_GMS_MERGE_TIMEOUT_DEFAULT;

    // x3 of the default value
    private static final String TCP_PING_TIMEOUT_HIGH = "15000";
    private static final String GMS_JOIN_TIMEOUT_HIGH = "15000";  
    private static final String GMS_MERGE_TIMEOUT_HIGH = "45000";
    
    
    @Rule
    public TestWatcher watcher = new TestWatcher() {
        protected void starting(Description description) {
            try {
                // Initialize Moca's logging system
                ServerUtils.setupDaemonContext(getClass().getName(), true, true);
                ThreadContext.put("logFilename", "cluster-integration-testing/" 
                        + TI_ClusterTestsTcpHosts.class.getSimpleName() 
                        + "/" + description.getMethodName());
                logger.info("Starting test: " + description.getMethodName());
            }
            catch (SystemConfigurationException e) {
                e.printStackTrace();
            }
        }
        protected void finished(Description description){
            ThreadContext.remove("logFilename");
        }
    };    

    @BeforeClass
    public static void beforeClass() throws UnknownHostException{
        InetAddress localhost = InetAddress.getLocalHost();
        hostname = localhost.getHostName();
        hostaddress = localhost.getHostAddress();
    }

    @After
    public void after(){
        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i].cleanup();
        }
    }

    @Test
    public void testCluster1() throws Exception{
        logger.info("Using port_range=0, tcp-hosts excludes current node, sequential channel connect");

        final String CLUSTER_NAME = "MOCA_TCPHOSTS_CLUSTER_1";
        final int checkIntervalMillis = 1000; 
        final int timeoutMillis = 60000;

        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i] = new ClusterNodeTcpHosts(CLUSTER_NAME, CLUSTER_SIZE, hostname);
        }

        // Generate the tcp-hosts setting
        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i].setTcpHostsExclusive(nodes);
        }

        // Prepare the channels
        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i].createChannelFromRegistry(new String[]{
                    "[CLUSTER]",
                    "name=" + nodes[i].clusterName,
                    "cookie-domain=.local",
                    "jgroups-protocol=tcp-hosts",
                    "jgroups-bind-port=" + nodes[i].bindPort,
                    "jgroups-tcp-hosts=" + nodes[i].tcpHosts,
                    "jgroups-bind-addr=" + hostaddress,
                    "jgroups-tcpping-port-range=0",
                    "jgroups-tcp-ping-timeout=" + TCP_PING_TIMEOUT_STD,
                    "jgroups-join-timeout=" + GMS_JOIN_TIMEOUT_STD,
                    "jgroups-merge-timeout=" + GMS_MERGE_TIMEOUT_STD
            });
        }

        // Now connect
        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i].run();
        }

        // Check if cluster was formed
        // Minimize test execution time by checking every 1 second with a maximum delay of 60 seconds

        boolean clusterFormed = ClusterNodeTcpHosts.waitForClusterToForm(nodes, checkIntervalMillis, timeoutMillis);

        assertTrue("ERROR: cluster was not formed", clusterFormed);

        ClusterNodeTcpHosts.verifyNodesHaveSameView(nodes);
    }


    @Test
    public void testCluster2() throws Exception{
        logger.info("Using port_range=5, tcp-hosts excludes current node, sequential channel connect");

        final String CLUSTER_NAME = "MOCA_TCPHOSTS_CLUSTER_2";
        final int checkIntervalMillis = 1000; 
        final int timeoutMillis = 60000;

        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i] = new ClusterNodeTcpHosts(CLUSTER_NAME, CLUSTER_SIZE, hostname);
            // Skip 5 ports to offset bind ports
            for(int k = 0; k < 5; k++){
                ClusterNodeTcpHosts.generateUniquePort();
            }
        }

        // Generate the tcp-hosts setting
        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i].setTcpHostsExclusive(nodes);
        }

        // Prepare the channels
        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i].createChannelFromRegistry(new String[]{
                    "[CLUSTER]",
                    "name=" + nodes[i].clusterName,
                    "cookie-domain=.local",
                    "jgroups-protocol=tcp-hosts",
                    "jgroups-bind-port=" + nodes[i].bindPort,
                    "jgroups-tcp-hosts=" + nodes[i].tcpHosts,
                    "jgroups-bind-addr=" + hostaddress,
                    "jgroups-tcpping-port-range=5",
                    "jgroups-tcp-ping-timeout=" + TCP_PING_TIMEOUT_HIGH,
                    "jgroups-join-timeout=" + GMS_JOIN_TIMEOUT_HIGH,  
                    "jgroups-merge-timeout=" + GMS_MERGE_TIMEOUT_HIGH
            });
        }
        
        // Now connect
        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i].run();
        }

        // Check if cluster was formed
        // Minimize test execution time by checking every 1 second with a maximum delay of 60 seconds

        boolean clusterFormed = ClusterNodeTcpHosts.waitForClusterToForm(nodes, checkIntervalMillis, timeoutMillis);

        assertTrue("ERROR: cluster was not formed", clusterFormed);

        ClusterNodeTcpHosts.verifyNodesHaveSameView(nodes);
    }


    @Test
    public void testCluster3() throws Exception{
        logger.info("Using port_range=0, tcp-hosts excludes current node, simultaneous channel connect");

        final String CLUSTER_NAME = "MOCA_TCPHOSTS_CLUSTER_3";
        final int checkIntervalMillis = 1000; 
        final int timeoutMillis = 120000;

        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i] = new ClusterNodeTcpHosts(CLUSTER_NAME, CLUSTER_SIZE, hostname);
        }

        // Generate the tcp-hosts setting
        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i].setTcpHostsExclusive(nodes);
        }

        // Prepare the channels
        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i].createChannelFromRegistry(new String[]{
                    "[CLUSTER]",
                    "name=" + nodes[i].clusterName,
                    "cookie-domain=.local",
                    "jgroups-protocol=tcp-hosts",
                    "jgroups-bind-port=" + nodes[i].bindPort,
                    "jgroups-tcp-hosts=" + nodes[i].tcpHosts,
                    "jgroups-bind-addr=" + hostaddress,
                    "jgroups-tcpping-port-range=0",
                    "jgroups-tcp-ping-timeout=" + TCP_PING_TIMEOUT_STD,
                    "jgroups-join-timeout=" + GMS_JOIN_TIMEOUT_STD,
                    "jgroups-merge-timeout=" + GMS_MERGE_TIMEOUT_STD
            });
        }

        ExecutorService pool = null;
        try{
            pool = Executors.newFixedThreadPool(CLUSTER_SIZE);

            // Now connect
            for(int i = 0; i < CLUSTER_SIZE; i++){
                pool.submit(nodes[i]);
            }

            // Check if cluster was formed
            // Minimize test execution time by checking every 1 second with a maximum delay of 60 seconds

            boolean clusterFormed = ClusterNodeTcpHosts.waitForClusterToForm(nodes, checkIntervalMillis, timeoutMillis);

            assertTrue("ERROR: cluster was not formed", clusterFormed);

            ClusterNodeTcpHosts.verifyNodesHaveSameView(nodes);
        } finally {
            if(pool != null){
                pool.shutdown();
            }
        }
    }


    @Test
    public void testCluster4() throws Exception{
        logger.info("Using port_range=5, tcp-hosts excludes current node, simultaneous channel connect");

        final String CLUSTER_NAME = "MOCA_TCPHOSTS_CLUSTER_4";
        final int checkIntervalMillis = 1000; 
        final int timeoutMillis = 120000;

        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i] = new ClusterNodeTcpHosts(CLUSTER_NAME, CLUSTER_SIZE, hostname);
            // Skip 5 ports to offset bind ports
            for(int k = 0; k < 5; k++){
                ClusterNodeTcpHosts.generateUniquePort();
            }
        }

        // Generate the tcp-hosts setting
        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i].setTcpHostsExclusive(nodes);
        }

        // Prepare the channels
        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i].createChannelFromRegistry(new String[]{
                    "[CLUSTER]",
                    "name=" + nodes[i].clusterName,
                    "cookie-domain=.local",
                    "jgroups-protocol=tcp-hosts",
                    "jgroups-bind-port=" + nodes[i].bindPort,
                    "jgroups-tcp-hosts=" + nodes[i].tcpHosts,
                    "jgroups-bind-addr=" + hostaddress,
                    "jgroups-tcpping-port-range=5",
                    "jgroups-tcp-ping-timeout=" + TCP_PING_TIMEOUT_HIGH,
                    "jgroups-join-timeout=" + GMS_JOIN_TIMEOUT_HIGH,  
                    "jgroups-merge-timeout=" + GMS_MERGE_TIMEOUT_HIGH
            });
        }

        // Now connect

        ExecutorService pool = null;
        try{
            pool = Executors.newFixedThreadPool(CLUSTER_SIZE);

            for(int i = 0; i < CLUSTER_SIZE; i++){
                pool.submit(nodes[i]);
            }

            // Check if cluster was formed
            // Minimize test execution time by checking every 1 second with a maximum delay of 60 seconds

            boolean clusterFormed = ClusterNodeTcpHosts.waitForClusterToForm(nodes, checkIntervalMillis, timeoutMillis);

            assertTrue("ERROR: cluster was not formed", clusterFormed);

            ClusterNodeTcpHosts.verifyNodesHaveSameView(nodes);
        } finally {
            if(pool != null){
                pool.shutdown();
            }
        }
    }


    @Test
    public void testCluster5() throws Exception{
        logger.info("Using port_range=0, tcp-hosts includes current node, simultaneous channel connect");

        final String CLUSTER_NAME = "MOCA_TCPHOSTS_CLUSTER_5";
        final int checkIntervalMillis = 1000; 
        final int timeoutMillis = 120000;

        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i] = new ClusterNodeTcpHosts(CLUSTER_NAME, CLUSTER_SIZE, hostname);
        }

        // Generate the tcp-hosts setting
        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i].setTcpHostsInclusive(nodes);
        }

        // Prepare the channels
        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i].createChannelFromRegistry(new String[]{
                    "[CLUSTER]",
                    "name=" + nodes[i].clusterName,
                    "cookie-domain=.local",
                    "jgroups-protocol=tcp-hosts",
                    "jgroups-bind-port=" + nodes[i].bindPort,
                    "jgroups-tcp-hosts=" + nodes[i].tcpHosts,
                    "jgroups-bind-addr=" + hostaddress,
                    "jgroups-tcpping-port-range=0",
                    "jgroups-tcp-ping-timeout=" + TCP_PING_TIMEOUT_STD,
                    "jgroups-join-timeout=" + GMS_JOIN_TIMEOUT_STD,
                    "jgroups-merge-timeout=" + GMS_MERGE_TIMEOUT_STD
            });
        }

        ExecutorService pool = null;
        try{
            pool = Executors.newFixedThreadPool(CLUSTER_SIZE);

            // Now connect
            for(int i = 0; i < CLUSTER_SIZE; i++){
                pool.submit(nodes[i]);
            }

            // Check if cluster was formed
            // Minimize test execution time by checking every 1 second with a maximum delay of 60 seconds

            boolean clusterFormed = ClusterNodeTcpHosts.waitForClusterToForm(nodes, checkIntervalMillis, timeoutMillis);

            assertTrue("ERROR: cluster was not formed", clusterFormed);

            ClusterNodeTcpHosts.verifyNodesHaveSameView(nodes);
        } finally {
            if(pool != null){
                pool.shutdown();
            }
        }
    }


    @Test
    public void testCluster6() throws Exception{
        logger.info("Using port_range=5, tcp-hosts includes current node, simultaneous channel connect");

        final String CLUSTER_NAME = "MOCA_TCPHOSTS_CLUSTER_6";
        final int checkIntervalMillis = 1000; 
        final int timeoutMillis = 120000;

        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i] = new ClusterNodeTcpHosts(CLUSTER_NAME, CLUSTER_SIZE, hostname);
            // Skip 5 ports to offset bind ports
            for(int k = 0; k < 5; k++){
                ClusterNodeTcpHosts.generateUniquePort();
            }
        }

        // Generate the tcp-hosts setting
        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i].setTcpHostsExclusive(nodes);
        }

        // Prepare the channels
        for(int i = 0; i < CLUSTER_SIZE; i++){
            nodes[i].createChannelFromRegistry(new String[]{
                    "[CLUSTER]",
                    "name=" + nodes[i].clusterName,
                    "cookie-domain=.local",
                    "jgroups-protocol=tcp-hosts",
                    "jgroups-bind-port=" + nodes[i].bindPort,
                    "jgroups-tcp-hosts=" + nodes[i].tcpHosts,
                    "jgroups-bind-addr=" + hostaddress,
                    "jgroups-tcpping-port-range=5",
                    "jgroups-tcp-ping-timeout=" + TCP_PING_TIMEOUT_HIGH,
                    "jgroups-join-timeout=" + GMS_JOIN_TIMEOUT_HIGH,  
                    "jgroups-merge-timeout=" + GMS_MERGE_TIMEOUT_HIGH,
                    "jgroups-timer-max-threads=15"
            });
        }

        // Now connect

        ExecutorService pool = null;
        try{
            pool = Executors.newFixedThreadPool(CLUSTER_SIZE);

            for(int i = 0; i < CLUSTER_SIZE; i++){
                pool.submit(nodes[i]);
            }

            // Check if cluster was formed
            // Minimize test execution time by checking every 1 second with a maximum delay of 60 seconds

            boolean clusterFormed = ClusterNodeTcpHosts.waitForClusterToForm(nodes, checkIntervalMillis, timeoutMillis);

            assertTrue("ERROR: cluster was not formed", clusterFormed);

            ClusterNodeTcpHosts.verifyNodesHaveSameView(nodes);
        } finally {
            if(pool != null){
                pool.shutdown();
            }
        }
    }

}
