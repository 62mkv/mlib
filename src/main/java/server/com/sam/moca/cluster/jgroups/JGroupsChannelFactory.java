/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.sam.moca.cluster.jgroups;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.JChannel;

import com.sam.moca.MocaRegistry;
import com.sam.moca.server.SystemConfigurationException;
import com.sam.moca.server.exec.SystemContext;
import com.sam.moca.util.MocaUtils;
import com.sam.util.SimpleFilenameFilter;

/**
 * This is a static factory for JChannel objects based on the SystemContext
 * provided to it
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class JGroupsChannelFactory {
    
    private synchronized static JChannel getChannelForConfigUsingXmlFile(
        SystemContext context, String xml) throws SystemConfigurationException {
        File xmlFile = context.getDataFile(new SimpleFilenameFilter(xml));
        if (xmlFile == null) {
            throw new SystemConfigurationException("JGroups XML file: " + xml
                    + " not found in data directory!");
        }
        xml = xmlFile.getAbsolutePath();
        _logger.debug(MocaUtils.concat("Found xml ", xml,
            " file for use with jgroups."));

        JChannel channel;
        try {
            channel = new JChannel(xml);
        }
        catch (Exception e) {
            throw new SystemConfigurationException("Problem encountered " +
                    "starting up jgroups using xml file", e);
        }

        return channel;
    }
    
    /**
     * @param context
     * @return
     * @throws SystemConfigurationException This is thrown when a configuration
     *         issue arises either due to invalid xml, registry or jgroups
     *         error
     */
    public synchronized static JChannel getChannelForConfig(SystemContext context)
            throws SystemConfigurationException {
        JChannel channel = _channels.get(context);

        if (channel == null) {
            String xml = context
                .getConfigurationElement(MocaRegistry.REGKEY_CLUSTER_JGROUPS_XML);
            if (xml != null) {
                channel = getChannelForConfigUsingXmlFile(context, xml);
            }
            else {
                channel = getChannelForConfigUsingRegistry(context);
            }
            
            _channels.put(context, channel);
        }
        
        return channel;
    }
    
    private synchronized static StringBuilder configureLockAndExecutor() {

        StringBuilder xml = new StringBuilder(" <CENTRAL_LOCK");
        xml.append(" num_backups=\"2\"").append(" />")
            .append(" <com.sam.moca.cluster.jgroups.MocaCentralExecutor")
            .append("    num_backups=\"2\"")
            .append(" />");

        return xml;
    }
    
    private synchronized static StringBuilder configureBindInterface(
        String bindInterface) {
        StringBuilder xml = new StringBuilder();
        if (bindInterface != null) {
            // FYI, when this registry key is not set, jgroups still sets this
            // attribute as empty on startup

            // FYI, in jgroups 3.4.3, the following protocols support this
            // attribute
            //
            // - Transport (TCP/UDP)
            // - FD_SOCK
            // - VERIFY_SUSPECT
            // - STATE_SOCK
            //
            // If other protocols are added to the auto generated xml,
            // make sure to cross check the current requirements on this
            // attribute. Also, jgroups appears to not check system
            // properties for this value, so if we want to set it,
            // it has to go in the generated xml.
            xml.append(" bind_interface_str=\"" + bindInterface + "\"");
        }
        return xml;
    }
    
    private synchronized static StringBuilder configureCommonTransferProtocolProperties(
            String bindInterface, SystemContext context, Protocol protocol) {
        StringBuilder xml = new StringBuilder(configureBindInterface(bindInterface));
        
        String bindPort = context.getConfigurationElement(
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_BIND_PORT);
        if (bindPort != null) {
            xml.append(" bind_port=\"" + bindPort + "\"");
        }
        else if (protocol == Protocol.TCP_HOSTS) {
            xml.append(" bind_port=\"" + 
                    MocaRegistry.REGKEY_CLUSTER_JGROUPS_BIND_PORT_DEFAULT + 
                    "\"");
        }
        
        return xml;
    }
    
    private synchronized static StringBuilder configureCompressionProtocol(SystemContext context) {
        String compression = context.getConfigurationElement(
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_COMPRESSION,
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_COMPRESSION_DEFAULT);

        StringBuilder xml = new StringBuilder();
        if (compression.equalsIgnoreCase("true")) {

            xml.append("<COMPRESS")
                .append("    compression_level=\"1\"")
                .append("    min_size=\"500\"")
                .append("    pool_size=\"2\"")
                .append(" />");
        }
        return xml;

    }
    
    private static String getGMSJoinTimeout(SystemContext context) { 
        String joinTimeout = context
                .getConfigurationElement(
                    MocaRegistry.REGKEY_CLUSTER_JGROUPS_JOIN_TIMEOUT, 
                    MocaRegistry.REGKEY_CLUSTER_JGROUPS_JOIN_TIMEOUT_DEFAULT);
        return joinTimeout;

    }
    
    private static String getTimerMaxThreads(SystemContext context) { 
        String value = context
                .getConfigurationElement(
                    MocaRegistry.REGKEY_CLUSTER_JGROUPS_TIMER_MAX_THREADS, 
                    MocaRegistry.REGKEY_CLUSTER_JGROUPS_TIMER_MAX_THREADS_DEFAULT);
        return value;
    }
    
    private static String getTcpPingPortRange(SystemContext context) {
        String value = context.getConfigurationElement(
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_TCPPING_PORT_RANGE,
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_TCPPING_PORT_RANGE_DEFAULT);
        return value;
    }

    private static String getTcpPortRange(SystemContext context) {
        String value = context.getConfigurationElement(
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_TCP_PORT_RANGE,
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_TCP_PORT_RANGE_DEFAULT);
        return value;
    }
    
    private synchronized static String udpConfigurationXml(SystemContext context, 
            Protocol protocol) {
        String mcastAddr = context.getConfigurationElement(
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_MCAST_ADDR,
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_MCAST_ADDR_DEFAULT); 
                
        String mcastPort = context.getConfigurationElement(
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_MCAST_PORT,
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_MCAST_PORT_DEFAULT);
        
        final String mergeTimeout = context.getConfigurationElement(
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_GMS_MERGE_TIMEOUT, 
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_GMS_MERGE_TIMEOUT_DEFAULT);
        
        String bindInterface = context
            .getConfigurationElement(MocaRegistry.REGKEY_CLUSTER_JGROUPS_BIND_INTERFACE);
        
        StringBuilder xml = new StringBuilder(" <config xmlns=\"urn:org:jgroups\"");
        xml.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")
            .append(" xsi:schemaLocation=\"urn:org:jgroups file:schema/JGroups-3.1.xsd\">")
            .append(" <UDP")
            .append(configureCommonTransferProtocolProperties(bindInterface, context, protocol))
            .append("    mcast_addr=\"" + mcastAddr + "\"")
            .append("    mcast_port=\"" + mcastPort + "\"")
            .append("    tos=\"8\"")
            .append("    ucast_recv_buf_size=\"20m\"")
            .append("    ucast_send_buf_size=\"640k\"")
            .append("    mcast_recv_buf_size=\"25m\"")
            .append("    mcast_send_buf_size=\"640k\"")
            .append("    loopback=\"true\"")
            .append("    max_bundle_size=\"64000\"")
            .append("    max_bundle_timeout=\"30\"")
            .append("    ip_ttl=\"2\"")
            .append("    enable_bundling=\"true\"")
            .append("    enable_diagnostics=\"false\"")
            .append("    bundler_type=\"old\"")
            // TIMER settings
            .append("    timer_type=\"old\"")
            .append("    timer.min_threads=\"4\"")
            .append("    timer.max_threads=\"" + getTimerMaxThreads(context) + "\"")
            .append("    timer.keep_alive_time=\"5000\"")
            .append("    timer.queue_max_size=\"500\"")
            // THREAD POOL settings
            .append("    thread_naming_pattern=\"pl\"")
            .append("    thread_pool.enabled=\"true\"")
            .append("    thread_pool.min_threads=\"2\"")
            .append("    thread_pool.max_threads=\"30\"")
            .append("    thread_pool.keep_alive_time=\"60000\"")
            .append("    thread_pool.queue_enabled=\"true\"")
            .append("    thread_pool.queue_max_size=\"100\"")
            .append("    thread_pool.rejection_policy=\"Discard\"")
            .append("    oob_thread_pool.enabled=\"true\"")
            .append("    oob_thread_pool.min_threads=\"2\"")
            .append("    oob_thread_pool.max_threads=\"30\"")
            .append("    oob_thread_pool.keep_alive_time=\"60000\"")
            .append("    oob_thread_pool.queue_enabled=\"false\"")
            .append("    oob_thread_pool.queue_max_size=\"100\"")
            .append("    oob_thread_pool.rejection_policy=\"Discard\"")
            .append(" />")
            .append(" <PING timeout=\"3000\" num_initial_members=\"3\"/>")
            .append(" <MERGE2 max_interval=\"30000\" min_interval=\"10000\"/>")
            .append(" <FD_SOCK ")
            .append(configureBindInterface(bindInterface))
            .append(" />")
            .append(" <FD_ALL/>")
            .append(" <pbcast.NAKACK  exponential_backoff=\"0\"")
            .append("    use_mcast_xmit=\"true\"")
            .append("    retransmit_timeout=\"300,600,1200\"")
            .append("    discard_delivered_msgs=\"true\"/>")
            .append(" <UNICAST2 ")
            .append("    stable_interval=\"5000\"")
            .append("    max_bytes=\"1m\"/>")
            .append(" <pbcast.STABLE stability_delay=\"500\" desired_avg_gossip=\"5000\" max_bytes=\"1m\"/>")
            .append(" <pbcast.GMS print_local_addr=\"true\" join_timeout=\"" + getGMSJoinTimeout(context) + "\" merge_timeout=\"" + mergeTimeout + "\" view_bundling=\"true\"/>")
            .append(" <UFC max_credits=\"100k\" min_threshold=\"0.20\" max_block_time=\"2500\"/>")
            .append(" <MFC max_credits=\"100k\" min_threshold=\"0.20\" max_block_time=\"2500\"/>")
            .append(configureCompressionProtocol(context))
            .append(" <FRAG2 frag_size=\"8000\"  />")
            .append(" <RSVP timeout=\"60000\" resend_interval=\"500\" ack_on_delivery=\"false\" />")
            .append(configureLockAndExecutor())
            .append(" <pbcast.FLUSH timeout=\"30000\"/>")
            .append(" </config>");
        return xml.toString();
    }
    
    synchronized static String tcpConfigurationXml(SystemContext context, 
            Protocol protocol) throws SystemConfigurationException {
        final String mergeTimeout = context.getConfigurationElement(
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_GMS_MERGE_TIMEOUT, 
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_GMS_MERGE_TIMEOUT_DEFAULT);
        
        String bindInterface = context
            .getConfigurationElement(MocaRegistry.REGKEY_CLUSTER_JGROUPS_BIND_INTERFACE);
        
        StringBuilder xml = new StringBuilder("<config xmlns=\"urn:org:jgroups\"");
        xml.append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")
            .append(" xsi:schemaLocation=\"urn:org:jgroups file:schema/JGroups-3.1.xsd\">")
            .append(" <TCP")
            .append(configureCommonTransferProtocolProperties(bindInterface, context, protocol))
            .append("    loopback=\"true\"")
            .append("    port_range=\"" + getTcpPortRange(context) + "\"")
            .append("    recv_buf_size=\"20m\"")
            .append("    send_buf_size=\"640k\"")
            .append("    max_bundle_size=\"64000\"")
            .append("    max_bundle_timeout=\"30\"")
            .append("    enable_bundling=\"true\"")
            .append("    use_send_queues=\"true\"")
            .append("    enable_diagnostics=\"false\"")
            .append("    bundler_type=\"old\"")
            // TIMER settings
            .append("    timer_type=\"old\"")
            .append("    timer.min_threads=\"4\"")
            .append("    timer.max_threads=\"" + getTimerMaxThreads(context) + "\"")
            .append("    timer.keep_alive_time=\"5000\"")
            .append("    timer.queue_max_size=\"500\"")
            // THREAD POOL settings
            .append("    thread_naming_pattern=\"pl\"")
            .append("    thread_pool.enabled=\"true\"")
            .append("    thread_pool.min_threads=\"2\"")
            .append("    thread_pool.max_threads=\"30\"")
            .append("    thread_pool.keep_alive_time=\"60000\"")
            .append("    thread_pool.queue_enabled=\"true\"")
            .append("    thread_pool.queue_max_size=\"100\"")
            .append("    thread_pool.rejection_policy=\"Discard\"")
            .append("    oob_thread_pool.enabled=\"true\"")
            .append("    oob_thread_pool.min_threads=\"2\"")
            .append("    oob_thread_pool.max_threads=\"30\"")
            .append("    oob_thread_pool.keep_alive_time=\"60000\"")
            .append("    oob_thread_pool.queue_enabled=\"false\"")
            .append("    oob_thread_pool.queue_max_size=\"100\"")
            .append("    oob_thread_pool.rejection_policy=\"Discard\"")
            .append(" />")
            .append(configurePingProtocol(context, protocol))
            .append(" <MERGE2 max_interval=\"30000\" min_interval=\"10000\"/>")
            .append(" <FD_SOCK ")
            .append(configureBindInterface(bindInterface))
            .append(" />")
            .append(" <FD timeout=\"3000\" max_tries=\"3\"/>")
            .append(" <VERIFY_SUSPECT timeout=\"1500\" ")
            .append(configureBindInterface(bindInterface))
            .append(" />")
            .append(" <pbcast.NAKACK")
            .append("    use_mcast_xmit=\"false\"")
            .append("    retransmit_timeout=\"300,600,1200,2400,4800\"")
            .append("    discard_delivered_msgs=\"false\"/>")
            .append(" <UNICAST2 ")
            .append("    stable_interval=\"5000\"")
            .append("    max_bytes=\"1m\"/>")
            .append(" <pbcast.STABLE stability_delay=\"500\" desired_avg_gossip=\"5000\" max_bytes=\"1m\"/>")
            .append(" <pbcast.GMS print_local_addr=\"true\" join_timeout=\"" + getGMSJoinTimeout(context)+ "\" merge_timeout=\"" + mergeTimeout + "\" view_bundling=\"true\"/>")
            .append(" <UFC max_credits=\"600k\" min_threshold=\"0.20\" max_block_time=\"2500\"/>")
            .append(protocol == Protocol.TCP_MCAST ? " <MFC max_credits=\"600k\" min_threshold=\"0.20\" max_block_time=\"2500\"/>" : "")
            .append(configureCompressionProtocol(context))
            .append(" <FRAG2 frag_size=\"40000\"/>")
            .append(" <RSVP timeout=\"60000\" resend_interval=\"500\" ack_on_delivery=\"false\" />")
            .append(configureLockAndExecutor())
            .append(" <pbcast.FLUSH timeout=\"30000\"/>")
            .append(" </config>");
        return xml.toString();
    }
    
    private synchronized static StringBuilder configurePingProtocol(
        SystemContext context, Protocol protocol)
            throws SystemConfigurationException {
        
        StringBuilder xml = new StringBuilder();
        
        switch (protocol) {
            case TCP_HOSTS:
                String initialHosts = context
                    .getConfigurationElement(
                        MocaRegistry.REGKEY_CLUSTER_JGROUPS_TCP_HOSTS);
                
                if (initialHosts == null) {
                    throw new SystemConfigurationException(
                        "jgroups-tcp-hosts registry key is required when using the tcp-hosts protocol.");
                }
                
                String tcpPingTimeout = context
                        .getConfigurationElement(
                            MocaRegistry.REGKEY_CLUSTER_JGROUPS_TCP_PING_TIMEOUT, 
                            MocaRegistry.REGKEY_CLUSTER_JGROUPS_TCP_PING_TIMEOUT_DEFAULT);
                int numInitialHosts = 1;
                final String[] hosts = initialHosts.split(",");
                if(hosts != null) {
                   numInitialHosts = hosts.length + 1; // plus yourself
                }
                
                xml.append(" <TCPPING timeout=\"" + tcpPingTimeout + "\"")
                    .append("    initial_hosts=\"" + initialHosts + "\"")
                    .append("    port_range=\"" + getTcpPingPortRange(context) + "\"")
                    .append("    num_initial_members=\"" + numInitialHosts + "\"")
                    .append("    ergonomics=\"false\"").append(" />");
                break;
            case TCP_MCAST:
                String mcastAddr = context.getConfigurationElement(
                    MocaRegistry.REGKEY_CLUSTER_JGROUPS_MCAST_ADDR,
                    MocaRegistry.REGKEY_CLUSTER_JGROUPS_MCAST_ADDR_DEFAULT); 
                
                String mcastPort = context.getConfigurationElement(
                    MocaRegistry.REGKEY_CLUSTER_JGROUPS_MCAST_PORT,
                    MocaRegistry.REGKEY_CLUSTER_JGROUPS_MCAST_PORT_DEFAULT);
                
                xml.append(" <MPING break_on_coord_rsp=\"true\"")
                    .append("    mcast_addr=\"" + mcastAddr + "\"")
                    .append("    mcast_port=\"" + mcastPort + "\"")
                    .append("    ip_ttl=\"2\"")
                    .append("    num_initial_members=\"3\"/>");
                break;
            default:
                throw new IllegalArgumentException("Invalid protocol provided, " +
                        "only TCP based protocols allowed: " + protocol);
        }
        
        return xml;
    }
    
    private synchronized static JChannel getChannelForConfigUsingRegistry(
        SystemContext context) throws SystemConfigurationException {

        String protocol = context.getConfigurationElement(
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_PROTOCOL,
            MocaRegistry.REGKEY_CLUSTER_JGROUPS_PROTOCOL_DEFAULT);
        
        // setting the system property ensures all protocols are configured
        // with the correct bind address.
        if (System.getProperty(JGROUPS_BIND_ADDR) == null) {
            final String bindAddr = context
                .getConfigurationElement(MocaRegistry.REGKEY_CLUSTER_JGROUPS_BIND_ADDR);
            if (bindAddr != null) {
                System.setProperty(JGROUPS_BIND_ADDR, bindAddr);
            }
        }
                
        String xml = null;
        
        String toCheck = protocol.toUpperCase(Locale.ROOT)
                .replace("-", "_");
        
        Protocol proto = Protocol.valueOf(toCheck);
        
        switch (proto) {
            case UDP:
                xml = udpConfigurationXml(context, proto);
                break;
            case TCP_HOSTS:
            case TCP_MCAST:
                xml = tcpConfigurationXml(context, proto);
                break;
            default:
                throw new SystemConfigurationException(
                    "Unsupported protocol type: " + protocol);
        }
        
        _logger.debug("Generated JGroups xml:\n" + xml.replace(">", ">\n"));
        try {
            return new JChannel(new ByteArrayInputStream(xml.getBytes(Charset
                .forName("UTF-8"))));
        }
        catch (Exception e) {
            throw new SystemConfigurationException("Problem encountered " +
                    "starting up jgroups using registry", e);
        }
    }
    
    private static Map<SystemContext, JChannel> _channels = 
            new HashMap<SystemContext, JChannel>();
    
    public final static String JGROUPS_BIND_ADDR = "jgroups.bind_addr";
    
    enum Protocol { UDP, TCP_HOSTS, TCP_MCAST };
    private static Logger _logger = LogManager.getLogger(JGroupsChannelFactory.class);
}
