/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.sam.moca.server.socket;

import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;

import com.sam.moca.MocaConstants;
import com.sam.moca.advice.ServerContextConfig;
import com.sam.moca.advice.SessionAdministrationManager;
import com.sam.moca.advice.SessionAdministrationManagerBean;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.dispatch.CommandDispatcher;
import com.sam.moca.server.dispatch.DispatchResult;
import com.sam.moca.server.dispatch.RequestDispatcher;
import com.sam.moca.server.exec.LocalSessionContext;
import com.sam.moca.server.exec.RequestContext;
import com.sam.moca.server.exec.ServerContext;
import com.sam.moca.server.exec.SessionContext;
import com.sam.moca.server.exec.SessionType;
import com.sam.moca.server.session.MocaSessionManager;
import com.sam.moca.server.session.MocaSessionUtils;
import com.sam.moca.util.MocaUtils;

/**
 * An upstream handler that executes a MOCA command request.  This actually
 * does the work of executing the request on the CommandDispatcher.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
@ChannelHandler.Sharable
public class MocaDispatchHandler extends IdleStateAwareChannelUpstreamHandler {
    
    public MocaDispatchHandler(CommandDispatcher dispatcher, MocaSessionManager sessionManager) {
        _dispatcher = dispatcher;
        String sessionId = "S" + Long.toHexString(_RAND.nextLong());
        _session = new LocalSessionContext(sessionId, SessionType.CLIENT_LEGACY);
        _sessionManager = sessionManager;
    }
    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        MocaRequest request = (MocaRequest) e.getMessage();
        _runningThread = Thread.currentThread();
        DispatchResult result;
        try {
            Map<String, String> env = request.getEnv();
            env.put(MocaConstants.WEB_CLIENT_ADDR, request.getHost() + ":" + request.getPort());
            MocaSessionUtils.validateSession(_session, env, _sessionManager);
            RequestContext requestCtx = new RequestContext(env);
            result = _dispatcher.executeCommand(request.getCommand(),
                request.getContext(), request.getArgs(),
                requestCtx, _session, request.isAutoCommit(), request.isRemote());
        }
        finally {
            SessionAdministrationManager manager = 
                (SessionAdministrationManager) ServerUtils.globalContext().getAttribute(
                    SessionAdministrationManagerBean.class.getName());
            manager.unregisterSessionThread(_session.getSessionId(), 
                _runningThread.getId());
            _runningThread = null;
        }
        
        // This can run for a while, so the socket may be closed.
        if (e.getChannel().isOpen()) {
            e.getChannel().write(new MocaResponse(request.getEncryptionType(), result));
        }
        // If the channel is no longer open, make sure we close out the session
        // incase if they had keepalive or auto commit disabled
        else {
            log.debug(MocaUtils.concat("Cleaning up session ",
                _session.getSessionId(), " due to client socket disconnected."));
            RequestDispatcher.cleanupSession(_session);
        }
    }
    
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx,
                                    ChannelStateEvent e) throws Exception {
        if (_runningThread != null) {
            log.debug(MocaUtils.concat(
                "Interrupted Thread ", _runningThread.getId(),
                " due to socket closed"));
            _runningThread.interrupt();
        }
        
        // Close the session if it's not currently running.
        ServerContext sctx = _session.takeServerContext();
        if (sctx != null) {
            log.debug("Closing server context due to session closed.");
            sctx.close();
        }
        
        ServerContextConfig.unregisterSession(_session.getSessionId());

        // We want to shut down tracing just in case if it was still on.
        _session.getTraceState().closeLogging();

        super.channelDisconnected(ctx, e);
    }
    
    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e)
            throws Exception {
        // Being idle while a connection is ongoing is normal.   
        if (_runningThread == null) {
            log.debug(MocaUtils.concat("Idle Timeout occured for session",
                _session.getSessionId()));
            ctx.getChannel().close();
        }
    }

    private final CommandDispatcher _dispatcher;
    private final SessionContext _session;
    private Thread _runningThread;
    private static Random _RAND = new Random();
    private final static Logger log = LogManager.getLogger(MocaDispatchHandler.class);
    private final MocaSessionManager _sessionManager;
}