/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.server.socket;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.dispatch.CommandDispatcher;
import com.redprairie.moca.server.dispatch.RequestDispatcher;
import com.redprairie.moca.server.session.MocaSessionManager;
import com.redprairie.moca.util.DaemonThreadFactory;
import com.redprairie.moca.util.NonMocaDaemonThreadFactory;

/**
 * The core classic MOCA server facade.  This class will initiate a server on
 * a given port.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class MocaProtocolServer {
    
    public MocaProtocolServer(int port, int poolSize, String encoding, int idleTimeout) {
        _port = port;
        _poolSize = poolSize;
        _idleTimeout = idleTimeout;
        if (encoding != null) {
            _encoding = Charset.forName(encoding);
        }
        else {
            _encoding = Charset.forName("UTF-8");
        }
    }
    
    public void start(ServerContextFactory serverContextFactory,
                      MocaSessionManager authManager) {
        CommandDispatcher dispatcher = new RequestDispatcher(serverContextFactory);

        ChannelFactory serverFactory = new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(new NonMocaDaemonThreadFactory("classic-boss")),
                    Executors.newCachedThreadPool(new NonMocaDaemonThreadFactory("classic-worker")));
        
        ServerBootstrap bootstrap = new ServerBootstrap(serverFactory);

        Executor dispatchPool;
        if (_poolSize > 0) {
            dispatchPool = Executors.newFixedThreadPool(_poolSize, new DaemonThreadFactory("classic-dispatch"));
        }
        else {
            dispatchPool = Executors.newCachedThreadPool(new DaemonThreadFactory("classic-dispatch"));
        }
        
        bootstrap.setPipelineFactory(new MocaProtocolPipelineFactory(dispatcher, authManager, dispatchPool, _encoding, _idleTimeout));
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.setOption("reuseAddress", true);
        
        _logger.debug("Starting classic server on port " + _port + " (" + _encoding + ")");
        try {
            _listenChannel = bootstrap.bind(new InetSocketAddress(_port));
            _logger.info("Classic server started on port " + _port + " (" + _encoding + ")");
        }
        catch (Exception e) {
            _logger.error("Error starting classic server on port " + _port, e);
        }
    }
    
    public void stop() {
        _listenChannel.close();
    }
    
    private final int _port;
    private final int _poolSize;
    private final int _idleTimeout;
    private final Charset _encoding;
    private static final Logger _logger = LogManager.getLogger(MocaProtocolServer.class);
    private Channel _listenChannel;
}
