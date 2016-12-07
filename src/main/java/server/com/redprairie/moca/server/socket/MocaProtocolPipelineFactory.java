/*
 *  $URL$
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

import java.nio.charset.Charset;
import java.util.concurrent.Executor;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import com.redprairie.moca.server.dispatch.CommandDispatcher;
import com.redprairie.moca.server.session.MocaSessionManager;

/**
 * Puts together the Netty "pipeline" of handlers that handle the various IO
 * operations on incoming and outgoing socket channels.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class MocaProtocolPipelineFactory implements ChannelPipelineFactory {
    
    public MocaProtocolPipelineFactory(CommandDispatcher dispatcher,
                                       MocaSessionManager authManager,
                                       Executor threadPool,
                                       Charset encoding,
                                       int idleTimeout) {
        _dispatcher = dispatcher;
        _authManager = authManager;
        _threadPool = threadPool;
        _encoding = encoding;
        _idleTimeout = idleTimeout;
        if (idleTimeout > 0) {
            _idleTimer = new HashedWheelTimer();
        }
        else {
            _idleTimer = null;
        }
    }
    
    public ChannelPipeline getPipeline() {
        ChannelPipeline pipeline = Channels.pipeline();
        if (_idleTimeout > 0) {
            pipeline.addLast("timeout", new IdleStateHandler(_idleTimer, _idleTimeout, 0, 0));
        }
        pipeline.addLast("decode", new MocaRequestDecoder(_encoding));
        pipeline.addLast("encode", new MocaResponseEncoder(_encoding));
        pipeline.addLast("pool", new ExecutionHandler(_threadPool));
        pipeline.addLast("dispatch", new MocaDispatchHandler(_dispatcher, _authManager));
        
        return pipeline;
    }
    
    private final CommandDispatcher _dispatcher;
    private final Executor _threadPool;
    private final MocaSessionManager _authManager;
    private final Charset _encoding;
    private final int _idleTimeout;
    private final Timer _idleTimer;
}