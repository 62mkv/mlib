/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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
import java.io.Reader;
import java.io.StringReader;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

import com.google.common.base.Joiner;
import com.redprairie.moca.MocaRuntimeException;
import com.redprairie.moca.cluster.jgroups.JGroupsChannelFactory;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.registry.RegistryReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test cluster node for testing tcp-hosts settings
 * 
 * Copyright (c) 2016 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author j1014071
 */
public class ClusterNodeTcpHosts extends ReceiverAdapter implements Runnable{

    private final static Logger logger = LogManager.getLogger();

    public final int expectedSize;
    public final int bindPort;
    public final String hostname; 
    public final String clusterName;

    public AtomicBoolean isClusterFormed = new AtomicBoolean(false);
    public String clusterView;
    public String tcpHosts;

    private JChannel channel = null;

    public ClusterNodeTcpHosts(String clusterName, int expectedSize, String hostname){
        this.expectedSize = expectedSize;
        this.bindPort = generateUniquePort();
        this.hostname = hostname;
        this.clusterName = clusterName;
    }

    public String getTcpHost(){
        return hostname + "[" + bindPort + "]";
    }

    /**
     * Set tcp-hosts setting excluding the current node
     * @param nodes
     */
    public void setTcpHostsExclusive(ClusterNodeTcpHosts [] nodes){

        for(ClusterNodeTcpHosts node: nodes){
            if(node != this){
                if(tcpHosts == null){
                    tcpHosts = node.getTcpHost();
                } else {
                    tcpHosts += "," + node.getTcpHost();
                }
            }
        }
    }

    /**
     * Set tcp-hosts setting including the current node
     * @param nodes
     */
    public void setTcpHostsInclusive(ClusterNodeTcpHosts [] nodes){
        for(ClusterNodeTcpHosts node: nodes){
            if(tcpHosts == null){
                tcpHosts = node.getTcpHost();
            } else {
                tcpHosts += "," + node.getTcpHost();
            }
        }
    }


    public void viewAccepted(View new_view) {
        logger.info(getTcpHost() + " - " + new_view);

        if(isClusterFormed.get() == false && new_view.size() == expectedSize){
            // Executed once
            isClusterFormed.set(true);
            clusterView = new_view.toString();
        } else {
            assertTrue("ERROR: view size " + new_view.size() + " exceeds expected size " + expectedSize, 
                new_view.size() <= expectedSize);
        }
    }

    public Channel createChannelFromRegistry(String [] registry) throws SystemConfigurationException{

        Reader reader = new StringReader(Joiner.on("\n").join(registry));
        SystemContext context = new RegistryReader(reader);
        channel = JGroupsChannelFactory.getChannelForConfig(context);
        channel.setReceiver(this);
        return channel;
    }

    @Override
    public void run() {
        try {
            channel.connect(clusterName);
            logger.info(getTcpHost() + ": connected channel to cluster " + clusterName);
        } catch (Exception e) {
            logger.info(getTcpHost() + ": ERROR connecting to cluster " + clusterName);
            e.printStackTrace();
        }
    }

    public void cleanup(){
        if(channel != null && !channel.isClosed()){
            channel.close();
            logger.info(getTcpHost() + ": closed channel");
        }
    }

    /*
        public void receive(Message msg) {
            logger.info(msg.getSrc() + ": " + msg.getObject());
        } 
     */   


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

    /**
     * Wait for the cluster to form for the specified timeout and check interval
     * @param nodes
     * @param checkIntervalMillis
     * @param timeoutMillis
     * @return
     * @throws InterruptedException
     */
    public static boolean waitForClusterToForm(ClusterNodeTcpHosts [] nodes, int checkIntervalMillis, int timeoutMillis) throws InterruptedException{
        boolean clusterFormed = true;
        for(int k = 0; k < timeoutMillis/checkIntervalMillis; k++){

            clusterFormed = true;
            for(int i = 0; i < nodes.length; i++){
                if(!nodes[i].isClusterFormed.get()){
                    clusterFormed = false;
                }
            }

            if(clusterFormed){
                logger.info("Complete cluster formed after " + k*checkIntervalMillis + " milliseconds");
                break;
            }

            Thread.sleep(checkIntervalMillis);
        }

        return clusterFormed;
    }


    /**
     * Verify cluster nodes have the same view
     * @param nodes
     */
    public static void verifyNodesHaveSameView(ClusterNodeTcpHosts [] nodes){
        String clusterView = nodes[0].clusterView; 
        for(int i = 0; i < nodes.length; i++){
            assertEquals(clusterView, nodes[i].clusterView);
        }
    }

    // @see java.lang.Runnable#run()

}
