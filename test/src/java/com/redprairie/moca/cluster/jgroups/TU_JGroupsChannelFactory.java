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

package com.redprairie.moca.cluster.jgroups;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

import com.google.common.base.Joiner;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.registry.RegistryReader;

import static org.junit.Assert.assertTrue;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author j1014071
 */
public class TU_JGroupsChannelFactory {

    
    @Test
    public void tcpConfigurationXmlDefaults() throws SystemConfigurationException{
        
        Reader reader = new StringReader(Joiner.on("\n").join(
            "[CLUSTER]",
            "name=moca-test",
            "cookie-domain=.JDA.COM",
            "jgroups-protocol=tcp-hosts",
            "jgroups-bind-port=7800",
            "jgroups-tcp-hosts=JGWBT32[7800],JGWBT32[7820],JGWBT32[7830]",
            "jgroups-bind-addr=10.47.4.99"
            ));
        
        SystemContext registry = new RegistryReader(reader);
        String contents = JGroupsChannelFactory.tcpConfigurationXml(registry, JGroupsChannelFactory.Protocol.TCP_HOSTS);

        validateSettings(contents, 
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_JOIN_TIMEOUT_DEFAULT, 
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_TCP_PORT_RANGE_DEFAULT, 
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_TCPPING_PORT_RANGE_DEFAULT, 
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_TIMER_MAX_THREADS_DEFAULT);
    }

    @Test
    public void tcpConfigurationXmlCustom() throws SystemConfigurationException{
        
        // Custom values
        final String JOIN_TIMEOUT = "6000";
        final String TCP_PORT_RANGE="15";
        final String TCPPING_PORT_RANGE="3";
        final String TIMER_MAX_THREADS="15";
        
        Reader reader = new StringReader(Joiner.on("\n").join(
            "[CLUSTER]",
            "name=moca-test",
            "cookie-domain=.JDA.COM",
            "jgroups-protocol=tcp-hosts",
            "jgroups-bind-port=7800",
            "jgroups-tcp-hosts=JGWBT32[7800],JGWBT32[7820],JGWBT32[7830]",
            "jgroups-bind-addr=10.47.4.99",
            // Custom settings
            "jgroups-join-timeout=" + JOIN_TIMEOUT,
            "jgroups-tcp-port-range=" + TCP_PORT_RANGE,
            "jgroups-tcpping-port-range=" + TCPPING_PORT_RANGE,
            "jgroups-timer-max-threads=" + TIMER_MAX_THREADS
            ));
        
        SystemContext registry = new RegistryReader(reader);
        String contents = JGroupsChannelFactory.tcpConfigurationXml(registry, JGroupsChannelFactory.Protocol.TCP_HOSTS);
        
        validateSettings(contents, JOIN_TIMEOUT, TCP_PORT_RANGE, TCPPING_PORT_RANGE, TIMER_MAX_THREADS);
    }

    public String parseProtocol(String protocol, String contents){
        
        int beginIndex = contents.indexOf("<" + protocol + " ");
        if(beginIndex >= 0){
            int endIndex = contents.indexOf("/>", beginIndex);
            if(endIndex > beginIndex){
                return contents.substring(beginIndex, endIndex + "/>".length());
            }
        }

        // Return null on any issue
        return null;
    }
    
    private void validateSettings(String contents, String joinTimeout, String tcpPortRange, String tcpPingPortRange, String timerMaxThreads){
        String tcpSettings = parseProtocol("TCP", contents);
        String tcppingSettings = parseProtocol("TCPPING", contents);
        String gmsSettings = parseProtocol("pbcast.GMS", contents);
        
        assertTrue(tcpSettings.contains("bind_port=\"7800\""));
        assertTrue(tcpSettings.contains("port_range=\"" + tcpPortRange + "\""));
        assertTrue(tcpSettings.contains("timer_type=\"old\""));
        assertTrue(tcpSettings.contains("timer.min_threads=\"4\""));
        assertTrue(tcpSettings.contains("timer.max_threads=\"" + timerMaxThreads + "\""));
        
        //assertTrue(tcppingSettings.contains("async_discovery=\"true\""));
        assertTrue(tcppingSettings.contains("port_range=\"" + tcpPingPortRange + "\""));
        
        assertTrue(gmsSettings.contains("join_timeout=\"" + joinTimeout + "\""));
        
    }
    
}
