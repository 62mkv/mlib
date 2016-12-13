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

import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import com.redprairie.moca.MocaInterruptedException;

/**
 * 
 * An aspectJ aspect used for cluster testing
 * to inject failures e.g. block UDP traffic
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
@Aspect
public class ClusterTestingAspect {
    
    /**
     * The below pointcuts are meant to intercept the standard
     * Java UDP classes so we can artificially block UDP traffic
     */
    
    @Pointcut("call(* java.net.DatagramSocket.send(..))")
    public void udpSend() {};
    
    @Pointcut("call(* java.net.DatagramSocket.receive(..))")
    public void udpReceive() {};
    
    @Pointcut("call(* java.net.MulticastSocket.send(..))")
    public void udpMulticastSend() {};
    
    @Pointcut("call(* java.net.MulticastSocket.receive(..))")
    public void udpMulticastReceive() {};
    
    @Before("udpSend()") 
    public void blockUdpSend() throws IOException {
        handleBlocking();
    }
    
    @Before("udpReceive()")
    public void blockUdpReceive() throws IOException {
        handleBlocking();
    }
    
    @Before("udpMulticastSend()") 
    public void blockMulticastUdp() throws IOException {
        handleBlocking();
    }
    
    @Before("udpMulticastReceive()")
    public void blockMulticastUdpReceive() throws IOException {
        handleBlocking();
    }
    
    /**
     * Gets the groovy script to start blocking UDP traffic
     * @return
     */
    public static String getBlockUdpScript() {
        return String.format("[[ %s.blockUdp() ]]", ClusterTestingAspect.class.getName());
    }
    
    /**
     * Gets the groovy script to unblock UDP traffic
     * @return
     */
    public static String getUnblockUdpScript() {
        return String.format("[[ %s.unblockUdp() ]]", ClusterTestingAspect.class.getName());
    }
    
    public synchronized static void blockUdp() {
        _logger.info("Blocking UDP!");
        BLOCK_CALLS = true;
    }
    
    public synchronized static void unblockUdp() {
        _logger.info("Unblocking UDP!");
        BLOCK_CALLS = false;
    }
    
    private void handleBlocking() throws IOException {
        if (BLOCK_CALLS) {
            // Try to reduce the spam by sleeping
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException e) {
                throw new MocaInterruptedException(e);
            }
            throw new IOException("Blocking calls");
        }
    }

    private static volatile boolean BLOCK_CALLS = false;
    private static final Logger _logger = Logger.getLogger(ClusterTestingAspect.class);
}
