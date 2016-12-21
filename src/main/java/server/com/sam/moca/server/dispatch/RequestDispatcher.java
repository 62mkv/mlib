/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.sam.moca.server.dispatch;

import java.util.Collection;

import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import com.sam.moca.MocaArgument;
import com.sam.moca.MocaConstants;
import com.sam.moca.MocaException;
import com.sam.moca.MocaInterruptedException;
import com.sam.moca.MocaResults;
import com.sam.moca.NotFoundException;
import com.sam.moca.exceptions.UnexpectedException;
import com.sam.moca.exceptions.UniqueConstraintException;
import com.sam.moca.server.ServerContextFactory;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.TransactionManagerUtils;
import com.sam.moca.server.exec.LocalSessionContext;
import com.sam.moca.server.exec.RequestContext;
import com.sam.moca.server.exec.ServerContext;
import com.sam.moca.server.exec.SessionContext;
import com.sam.moca.server.exec.SessionType;
import com.sam.moca.server.log.ActivityLogMessage;
import com.sam.moca.server.session.SessionToken;
import com.sam.moca.util.MocaUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The request dispatcher handles the setup of any necessary context objects and
 * begins command execution.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class RequestDispatcher implements CommandDispatcher {
    
        // Retrieve transaction from session
    public static void cleanupSession(SessionContext session) {
        // We need to rollback the transaction first, else there could be a
        // race condition where someone retrieves the connection from the pool
        // before we actually kill the transaction
        Transaction transaction = (Transaction)session.removeAttribute(
            MocaConstants.SUSPENDED_TX);
        if (transaction != null) {
            _logger.debug("Rolling back suspended transaction due to session closed.");
            try {
                transaction.rollback();
            }
            catch (IllegalStateException e) {
                // Ignore this -- we don't care if it was already rolled back
            }
            catch (SystemException e) {
                _logger.debug(MocaUtils.concat(
                    "Error while rolling back transaction (should be already closed however): ", 
                    e));
            }
        }
        
        ServerContext ctx = session.takeServerContext();
        if (ctx != null) {
            _logger.debug("Closing server context due to session closed.");
            ctx.close();
        }
    }
    
    /**
     * Constructor for the request dispatcher. The passed-in
     * {@link ServerContextFactory} represents the current running MOCA system.
     * 
     * @param mocaFactory the ServerContextFactory representing this MOCA
     *            system.
     */
    public RequestDispatcher(ServerContextFactory mocaFactory) {
        _mocaFactory = mocaFactory;
        _activityHook = mocaFactory.getHook(DispatchActivityHook.class);
    }
    
    // @see com.sam.moca.server.dispatch.CommandDispatcher#resolveException(com.sam.moca.MocaException)
    @Override
    public DispatchResult resolveException(MocaException e) {
        _logger.debug("Creating new ServerContext ");

        // set up request context and session
        // so that message resolvers can use commands and the database
        final RequestContext request = new RequestContext();
        final String sessionToken = "messageResolver-" + Integer.toHexString(System.identityHashCode(request));

        // session type is server so that we don't check auth on every command
        final SessionContext session = new LocalSessionContext(sessionToken, SessionType.SERVER);
        session.setSessionToken(new SessionToken(sessionToken));

        final ServerContext ctx = _mocaFactory.newContext(request, session);
        final ServerContext previous = ServerUtils.getCurrentContextNullable();

        try {
            ServerUtils.setCurrentContext(ctx);
            return resolveException(e, ctx, e.getErrorCode());
        }
        finally {
            // we have to make sure to close the context to clean up the session
            // and to disassociate it
            ctx.close();
            ServerUtils.setCurrentContext(previous);
        }
    }
    
    private DispatchResult resolveException(MocaException e, ServerContext ctx, 
        int errorCode) {
        MocaResults errorResults = e.getResults();
        String message;
        if (e.isMessageResolved()) {
            message = e.getMessage();
        }
        else {
            message = new ErrorMessageBuilder(errorCode, e.getMessage(), 
                    e.getArgList(), ctx.getMessageResolver()).getMessage();
        }
        _logger.debug(message);
        
        DispatchResult result = new DispatchResult(errorResults, errorCode, message);
        return result;
    }
    
    // @see com.sam.moca.server.dispatch.CommandDispatcher#executeCommand(java.lang.String, java.util.Collection, java.util.Collection, com.sam.moca.server.exec.RequestContext, com.sam.moca.server.exec.SessionContext, boolean)
    public DispatchResult executeCommand(String command, Collection<MocaArgument> context,
                                         Collection<MocaArgument> args, RequestContext request,
                                         SessionContext session, boolean autoCommit,
                                         boolean remoteMode) {

        // Retrieve transaction from session
        ServerContext ctx = session.takeServerContext();
        
        // Or make a new one.
        if (ctx == null) {
            _logger.debug("Creating new ServerContext ");
            ctx = _mocaFactory.newContext(request, session);
        }
        else {
            _logger.debug("Pulling ServerContext from session");
            _mocaFactory.associateContext(request, session);
        }
        
        ServerUtils.setCurrentContext(ctx);
        
        Transaction transaction = (Transaction)session.removeAttribute(
            MocaConstants.SUSPENDED_TX);
        if (transaction != null) {
            TransactionManager transactionManager = TransactionManagerUtils.getManager();
            try {
                _logger.debug(MocaUtils.concat("Resuming transaction ", 
                    transaction, " for session"));
                transactionManager.resume(transaction);
                
            }
            catch (SystemException e) {
                // TODO Do we do something different?
                _logger.warn("Problem encountered resuming transaction!", e);
            }
            catch (InvalidTransactionException e) {
                // TODO Do we do something different?
                _logger.warn("Problem encountered resuming invalid transaction!", e);
            }
        }
        
        _logger.debug("Dispatching command...");
        
        long beginTime = System.currentTimeMillis();
        
        DispatchResult result = null;

        boolean hasPreHookTransaction = false;
        
        try {
            try {
                _logger.debug(MocaUtils.concat("Server got: ", command));
                MocaResults results;
                try {
                    results = ctx.executeCommandWithRemoteContext(command, context, args);
                }
                finally {
                    hasPreHookTransaction = ctx.hasTransaction();
                }
                
                if (autoCommit) {
                    ctx.commit();
                }
                _logger.debug(MocaUtils.concat("Returning ", results.getRowCount(), " row(s)"));
                
                result = new DispatchResult(results);
                return result;
            }
            catch (MocaException e) {
                if (e instanceof NotFoundException) {
                    _logger.debug("Exception raised from command: " + e);
                }
                else {
                    _logger.debug("Exception raised from command: " + e, e);
                }
                if (autoCommit) {
                    ctx.rollback();
                }
                throw e;
            }
            catch (MocaInterruptedException e) {
                _logger.debug("Command Execution Interrupted " + e, e);
                if (autoCommit) {
                    ctx.rollback();
                }
                // Then turn it into a checked exception so we can are caught
                // by the immediately surrounding catch block so we can be
                // parsed out correctly.
                throw new MocaException(e);
            }
            catch (RuntimeException e) {
                _logger.debug("Exception raised from command: " + e, e);
                if (autoCommit) {
                    ctx.rollback();
                }
                throw new UnexpectedException(e);
            }
            catch (Error e) {
                _logger.fatal("Fatal error encountered handling client request.", e);
                if (autoCommit) {
                    ctx.rollback();
                }
                throw new UnexpectedException(e);
            }
        }
        catch (MocaException e) {
            int errorCode = e.getErrorCode();
            
            if (!remoteMode) {
                // We mangle some error codes before returning them to the client.
                if (errorCode == NotFoundException.DB_CODE)
                    errorCode = ERROR_DB_NOT_FOUND;
                else if (errorCode == UniqueConstraintException.CODE)
                    errorCode = ERROR_DB_UNIQUE_CONSTRAINT;
                else if (errorCode < 0)
                    errorCode = ERROR_DB_GENERIC;            
            }
            
            _logger.debug(MocaUtils.concat("Returning error ", errorCode, " to client"));
            
            result = resolveException(e, ctx, errorCode);
            return result;
        }
        finally {
            if (result != null) {
                // Deal with activity and/or audit logs.
                long duration = System.currentTimeMillis() - beginTime;
                int errorCode = result.getStatus();
                MocaResults data = result.getResults();
                int rowCount = data == null ? 0 : data.getRowCount();
    
                if (_activityLogger.isDebugEnabled()) {
                    ActivityLogMessage message = new ActivityLogMessage(
                        duration, command, request.getAllVariables(),
                        errorCode, rowCount);
                    _activityLogger.debug(
                        message.asMapMessage());
                }
    
                if (_activityHook != null) {
                    try {
                        _activityHook.activity(duration, command,
                            request.getAllVariables(), errorCode, rowCount);
                    }
                    catch (Exception e) {
                        _logger.warn("DispatchActivityHook failed with exception: "
                                    + e);
                    }
                }
            }
            
            // Now we deal with the possibility that one or more of our hooks has fiddled with the transaction.
            boolean hasPostHookTransaction = ctx.hasTransaction();
            boolean hasKeepAlive = ctx.hasKeepalive();

            // There are two options.
            // 1. We save the server context, and attach it to the session. 
            // 2. We close the server context and allow the session to be closed (by the caller).
            boolean keepTransaction = (!autoCommit && hasPreHookTransaction);
            
            // If autocommit is disabled or keepalive is set, save the context into the session.
            if (keepTransaction || hasKeepAlive) {
                if (keepTransaction) {
                    // We only save off the tx if we had a connection still after
                    // the command
                    TransactionManager transactionManager = TransactionManagerUtils.getManager();
                    try {
                        _logger.debug("Suspending transaction due to auto commit false");
                        transaction = transactionManager.suspend();
                        _logger.debug(MocaUtils.concat("Suspended transaction ", 
                            transaction));
                        session.putAttribute(MocaConstants.SUSPENDED_TX, transaction);
                    }
                    catch (SystemException e) {
                        // TODO Do we do something different?
                        _logger.error("Problem encountered suspending transaction!", e);
                    }
                }
                else if (hasPostHookTransaction) {
                    _logger.debug("rolling back transaction after hooks");
                    try {
                        ctx.rollback();
                    }
                    catch (MocaException e) {
                        _logger.warn("Problem with rolling back transaction for hooks", e);
                    }
                }
                _logger.debug(MocaUtils.concat(
                    "Holding on to session context. Keepalive: ", hasKeepAlive,
                    ", autoCommit: " + autoCommit));
                session.saveServerContext(ctx);
            }
            else {
                // Edge case -- autocommit is disabled, no transaction was present, but now there is.
                // In this case, we explicitly roll back.
                if (!autoCommit && hasPostHookTransaction) {
                    _logger.debug("rolling back transaction after hooks");
                }
                _logger.debug("Releasing Session context. ");
                ctx.close();
            }
            
            _logger.debug("Dispatched command");
            ServerUtils.setCurrentContext(null);
        }
    }
    
    private static int ERROR_DB_NOT_FOUND = NotFoundException.SERVER_CODE;
    private static int ERROR_DB_UNIQUE_CONSTRAINT = 512;
    private static int ERROR_DB_GENERIC = 511;
    private static final Logger _logger = LogManager.getLogger(CommandDispatcher.class);
    private static final Logger _activityLogger = LogManager.getLogger(ActivityLogMessage.LOGGER_NAME);
    
    private final ServerContextFactory _mocaFactory;
    private final DispatchActivityHook _activityHook;
}
