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

package com.sam.moca.socket;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sam.moca.MocaArgument;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaType;
import com.sam.moca.NotFoundException;
import com.sam.moca.server.ServerContextFactory;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.exec.LocalSessionContext;
import com.sam.moca.server.exec.RequestContext;
import com.sam.moca.server.exec.ServerContext;
import com.sam.moca.server.exec.SessionContext;
import com.sam.moca.server.exec.SessionType;
import com.sam.moca.server.log.TraceState;
import com.sam.moca.server.session.SessionToken;
import com.sam.moca.util.MocaUtils;
import com.sam.util.ArgCheck;

/**
 * 
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class CommandContextSocketProcessor implements SocketProcessorFactory {
    
    public CommandContextSocketProcessor(String command) {
        this(command, false, null);
    }
    
    /**
     * Configures the instance to generate unique session IDs when
     * executing commands using the given prefix
     * @param command The command to execute
     * @param useUniqueSessions Whether to generate unique sessions for each request
     * @param sessionPrefix The string prefix for the sessions, required
     *                      if useUniqueSessions is true
     */
    CommandContextSocketProcessor(String command, boolean useUniqueSessions, String sessionPrefix) {
        _command = command;
        _useUniqueSessions = useUniqueSessions;
        if (_useUniqueSessions) {
            ArgCheck.notNull(sessionPrefix,
                "A session prefix must be provided when generating unique sessions");
        }
        _sessionPrefix = sessionPrefix;
    }
    
    @Override
    public SocketProcessor getSocketProcessor() throws SocketProcessorException {
        return new Processor(_useUniqueSessions, _sessionPrefix, 
            ServerUtils.getCurrentContext().getSession().getTraceState());
    }
    
    private class Processor implements SocketProcessor {
        
        private Processor(boolean useUniqueSessions, String sessionPrefix, TraceState parentTraceState) {
            _processWithUniqueSession = useUniqueSessions;
            _processSessionPrefix = sessionPrefix;
            _parentTraceState = parentTraceState;
        }
        
        @Override
        public void process(SocketEndpoint endpoint) throws IOException, SocketProcessorException {
            MocaContext moca = null;
            ServerContext serverContext = null;

            // If we're using a unique session for each request
            // then we'll generate it here
            if (_processWithUniqueSession) {
                serverContext = setupSessionContext();
                moca = serverContext.getComponentContext();
            }
            else {
                moca = MocaUtils.currentContext();
            }
            
            _log.debug("Processing incoming socket request");
            
            boolean successful = false;
            try {
                _log.debug("Executing command: [" + _command +"]");

                // We don't use the results of the command for anything
                moca.executeCommand(_command, new MocaArgument("socket", MocaType.OBJECT, endpoint));
                successful = true;
            }
            catch (MocaException e) {
                if (e instanceof NotFoundException) {
                    _log.debug("Command executed with error status: " + e);
                }
                else {
                    throw new SocketProcessorException("Command executed with error status", e);
                }
            }
            finally {
                try {
                    endTransaction(moca, successful);
                }
                finally {
                    _log.debug("Finished processing incoming socket request");
                    
                    // Close out the session/servercontext if we're using
                    // a unique session for the request
                    if (_processWithUniqueSession) {
                        serverContext.close();
                        ServerUtils.setCurrentContext(null);
                    }
                }
            }
        }
        
        // Sets up the server context/unique session for the processor
        private ServerContext setupSessionContext() {
            ServerContextFactory factory = ServerUtils
                    .globalAttribute(ServerContextFactory.class);
            
            // Suffix the current thread ID so it's unique
            String token = _processSessionPrefix + "-" + Thread.currentThread().getId();
            RequestContext request = new RequestContext();
            SessionContext session = new LocalSessionContext(token, 
                SessionType.TASK);

            // Authenticate our session
            session.setSessionToken(new SessionToken(token));

            ServerContext serverContext = factory.newContext(request, session);
            ServerUtils.setCurrentContext(serverContext);
            
            // Inherit the parent trace state if we have it
            TraceState traceState = _parentTraceState == null ? session.getTraceState() : _parentTraceState;
            traceState.applyTraceStateToThread();
            
            return serverContext;
        }
        
        private void endTransaction(MocaContext moca, boolean successful) throws SocketProcessorException {
            try {
                if (successful) {
                    _log.debug("Command executed successfully, committing...");
                    moca.commit();
                }
                else {
                    _log.debug("rolling back... ");
                    moca.rollback();
                }
            }
            catch (MocaException ex) {
                String action = successful ? "commit" : "rollback";
                throw new SocketProcessorException("Unable to " + action + " transaction", ex);
            }
        }
        
        private final boolean _processWithUniqueSession;
        private final String _processSessionPrefix;
        private final TraceState _parentTraceState;
    }
    private final String _command;
    private final boolean _useUniqueSessions;
    private final String _sessionPrefix;
    private final static Logger _log = LogManager.getLogger(CommandContextSocketProcessor.class);
}
