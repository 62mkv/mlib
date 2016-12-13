/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.server.dispatch;

import java.util.Collection;

import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.exec.RequestContext;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SessionContext;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 Sam Corporation All Rights Reserved
 * 
 * @author klehrke
 */
public class TU_RequestDispatcher {

    /***
     * This junit tests the most general case in which auto commit is on, has a
     * transaction, no keepalive.
     * 
     * This case should just close with no other rollbacks or context saves.
     * 
     * 
     * @throws MocaException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRegularConditionCloseContext() throws MocaException {

        ServerContext mockServerContext = Mockito.mock(ServerContext.class);
        SessionContext mockSessionContext = Mockito.mock(SessionContext.class);

        Mockito.when(mockServerContext.getSession()).thenReturn(
            mockSessionContext);
        Mockito.when(mockServerContext.hasTransaction()).thenReturn(true);

        MocaResults results = Mockito.mock(MocaResults.class);
        Mockito.when(results.getRowCount()).thenReturn(1);

        Mockito.when(
            mockServerContext.executeCommandWithRemoteContext(
                Mockito.anyString(), Mockito.any(Collection.class),
                Mockito.any(Collection.class))).thenReturn(results);

        Mockito.when(mockSessionContext.getSessionId()).thenReturn("1");
        Mockito.when(mockSessionContext.getServerContext()).thenReturn(
            mockServerContext);
        Mockito.when(mockServerContext.getSession()).thenReturn(
            mockSessionContext);

        ServerContextFactory mockServerContextFactory = Mockito
            .mock(ServerContextFactory.class);

        Mockito.when(
            mockServerContextFactory.newContext((RequestContext) Mockito.any(),
                (SessionContext) Mockito.any())).thenReturn(mockServerContext);

        RequestDispatcher rd = new RequestDispatcher(mockServerContextFactory);

        // Has transaction and autocommit is true.

        rd.executeCommand("list library versions", null, null, null,
            mockSessionContext, true, false);

        Mockito.verify(mockServerContext).close();
        Mockito.verify(mockServerContext, Mockito.never()).rollback();
        Mockito.verify(mockSessionContext, Mockito.never()).saveServerContext(
            mockServerContext);

    }

    /***
     * This junit tests the case in which auto commit is on, has no transaction,
     * but has keepalive set.
     * 
     * This case should save the context, don't close or rollback.
     * 
     * 
     * @throws MocaException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRegularConditionSaveContext() throws MocaException {

        ServerContext mockServerContext = Mockito.mock(ServerContext.class);
        SessionContext mockSessionContext = Mockito.mock(SessionContext.class);

        Mockito.when(mockServerContext.getSession()).thenReturn(
            mockSessionContext);
        Mockito.when(mockServerContext.hasTransaction()).thenReturn(false);

        MocaResults results = Mockito.mock(MocaResults.class);
        Mockito.when(results.getRowCount()).thenReturn(1);

        Mockito.when(
            mockServerContext.executeCommandWithRemoteContext(
                (String) Mockito.any(),
                (Collection<MocaArgument>) Mockito.any(),
                (Collection<MocaArgument>) Mockito.any())).thenReturn(results);

        Mockito.when(mockSessionContext.getSessionId()).thenReturn("1");
        Mockito.when(mockServerContext.getSession()).thenReturn(
            mockSessionContext);

        ServerContextFactory mockServerContextFactory = Mockito
            .mock(ServerContextFactory.class);

        Mockito.when(
            mockServerContextFactory.newContext((RequestContext) Mockito.any(),
                (SessionContext) Mockito.any())).thenReturn(mockServerContext);

        RequestDispatcher rd = new RequestDispatcher(mockServerContextFactory);

        // Has transaction and autocommit is true, but more importantly,
        // keepalive is true
        Mockito.when(mockServerContext.hasKeepalive()).thenReturn(true);

        rd.executeCommand("list library versions", null, null, null,
            mockSessionContext, true, false);

        // Save the context but don't rollback
        Mockito.verify(mockSessionContext).saveServerContext(mockServerContext);
        Mockito.verify(mockServerContext, Mockito.never()).rollback();
        Mockito.verify(mockServerContext, Mockito.never()).close();
    }

    /***
     * This junit tests the case in which auto commit is on, has a transaction,
     * and has keepalive set. The execution will throw a MocaException.
     * 
     * This case should have 2 rollbacks, don't close and save the context.
     * 
     * 
     * @throws MocaException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRegularConditionErrorCondition() throws MocaException {

        ServerContext mockServerContext = Mockito.mock(ServerContext.class);
        SessionContext mockSessionContext = Mockito.mock(SessionContext.class);

        Mockito.when(mockServerContext.getSession()).thenReturn(
            mockSessionContext);

        MessageResolver mockedMessageResolver = Mockito
            .mock(MessageResolver.class);
        Mockito.when(mockedMessageResolver.getMessage("Error")).thenReturn(
            "Error");
        Mockito.when(mockServerContext.getMessageResolver()).thenReturn(
            mockedMessageResolver);

        Mockito.when(mockServerContext.hasTransaction()).thenReturn(true);

        MocaResults results = Mockito.mock(MocaResults.class);
        Mockito.when(results.getRowCount()).thenReturn(1);

        Mockito.when(
            mockServerContext.executeCommandWithRemoteContext(
                Mockito.anyString(), Mockito.any(Collection.class),
                Mockito.any(Collection.class))).thenThrow(
            new MocaException(502));

        Mockito.when(mockSessionContext.getSessionId()).thenReturn("1");
        Mockito.when(mockSessionContext.getServerContext()).thenReturn(
            mockServerContext);
        Mockito.when(mockServerContext.getSession()).thenReturn(
            mockSessionContext);

        ServerContextFactory mockServerContextFactory = Mockito
            .mock(ServerContextFactory.class);

        Mockito.when(
            mockServerContextFactory.newContext(
                Mockito.any(RequestContext.class),
                Mockito.any(SessionContext.class))).thenReturn(
            mockServerContext);

        RequestDispatcher rd = new RequestDispatcher(mockServerContextFactory);

        // Has transaction and autocommit is true, but more importantly,
        // keepalive is true
        Mockito.when(mockServerContext.hasKeepalive()).thenReturn(true);

        rd.executeCommand("list library versions", null, null, null,
            mockSessionContext, true, false);

        // With auto-commit enabled and keepalive on, we should roll back after failure, but not close the context.
        Mockito.verify(mockServerContext, Mockito.times(2)).rollback();
        Mockito.verify(mockServerContext, Mockito.never()).close();
        Mockito.verify(mockSessionContext).saveServerContext(mockServerContext);

    }

    /***
     * This junit tests the case in which auto commit is off, has no transaction
     * until after the hooks have run, and has keepalive set. This includes a
     * MocaException from the execute.
     * 
     * This case should have a rollback, no context close and ensure that we
     * don't save the context.
     * 
     * 
     * @throws MocaException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRegularConditionErrorConditionWithNewOpenTransaction()
            throws MocaException {

        ServerContext mockServerContext = Mockito.mock(ServerContext.class);
        SessionContext mockSessionContext = Mockito.mock(SessionContext.class);

        Mockito.when(mockServerContext.getSession()).thenReturn(
            mockSessionContext);

        MessageResolver mockedMessageResolver = Mockito
            .mock(MessageResolver.class);
        Mockito.when(mockedMessageResolver.getMessage("Error")).thenReturn(
            "Error");
        Mockito.when(mockServerContext.getMessageResolver()).thenReturn(
            mockedMessageResolver);

        Mockito.when(mockServerContext.hasTransaction())
            .thenReturn(false, true);

        MocaResults results = Mockito.mock(MocaResults.class);
        Mockito.when(results.getRowCount()).thenReturn(1);

        Mockito.when(
            mockServerContext.executeCommandWithRemoteContext(
                Mockito.anyString(), Mockito.any(Collection.class),
                Mockito.any(Collection.class))).thenThrow(
            new MocaException(502));

        Mockito.when(mockSessionContext.getSessionId()).thenReturn("1");
        Mockito.when(mockSessionContext.getServerContext()).thenReturn(
            mockServerContext);
        Mockito.when(mockServerContext.getSession()).thenReturn(
            mockSessionContext);

        ServerContextFactory mockServerContextFactory = Mockito
            .mock(ServerContextFactory.class);

        Mockito.when(
            mockServerContextFactory.newContext(
                Mockito.any(RequestContext.class),
                Mockito.any(SessionContext.class))).thenReturn(
            mockServerContext);

        RequestDispatcher rd = new RequestDispatcher(mockServerContextFactory);

        // Has transaction and autocommit is false, but more importantly,
        // keepalive is true
        Mockito.when(mockServerContext.hasKeepalive()).thenReturn(true);

        rd.executeCommand("list library versions", null, null, null,
            mockSessionContext, false, false);

        // Because autocommit is false, we retain the transaction, neither committing nor rolling it back.
        Mockito.verify(mockServerContext).rollback();
        Mockito.verify(mockServerContext, Mockito.never()).commit();
        Mockito.verify(mockServerContext, Mockito.never()).close();
        Mockito.verify(mockSessionContext).saveServerContext(mockServerContext);
        Mockito.verify(mockSessionContext).takeServerContext();
    }
    
    /**
     * This test is to make sure the correct behavior occurs when auto commit
     * is enabled, keepalive is set and there is a successful execution.
     * We want the command itself to be committed, and then the context to be
     * left open but no rollback/commit afterwards
     * @throws MocaException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAutoCommitAndKeepaliveOnSuccess() throws MocaException {
        ServerContext mockServerContext = Mockito.mock(ServerContext.class);
        SessionContext mockSessionContext = Mockito.mock(SessionContext.class);

        Mockito.when(mockServerContext.getSession()).thenReturn(
            mockSessionContext);
        // We set it to have a tx after execution but not after hooks
        Mockito.when(mockServerContext.hasTransaction())
            .thenReturn(true, false);

        MocaResults results = Mockito.mock(MocaResults.class);
        Mockito.when(results.getRowCount()).thenReturn(1);

        Mockito.when(
            mockServerContext.executeCommandWithRemoteContext(
                Mockito.anyString(), Mockito.any(Collection.class),
                Mockito.any(Collection.class))).thenReturn(results);

        Mockito.when(mockSessionContext.getSessionId()).thenReturn("1");
        Mockito.when(mockSessionContext.getServerContext()).thenReturn(
            mockServerContext);
        Mockito.when(mockServerContext.getSession()).thenReturn(
            mockSessionContext);

        ServerContextFactory mockServerContextFactory = Mockito
            .mock(ServerContextFactory.class);

        Mockito.when(
            mockServerContextFactory.newContext(
                Mockito.any(RequestContext.class),
                Mockito.any(SessionContext.class))).thenReturn(
            mockServerContext);

        RequestDispatcher rd = new RequestDispatcher(mockServerContextFactory);

        // Has transaction and autocommit is false, but more importantly,
        // keepalive is true
        Mockito.when(mockServerContext.hasKeepalive()).thenReturn(true);

        rd.executeCommand("list library versions", null, null, null,
            mockSessionContext, true, false);

        // Because autocommit is false, we retain the transaction, neither committing nor rolling it back.
        Mockito.verify(mockServerContext).commit();
        Mockito.verify(mockServerContext, Mockito.never()).rollback();
        Mockito.verify(mockServerContext, Mockito.never()).close();
        Mockito.verify(mockSessionContext).saveServerContext(mockServerContext);
        Mockito.verify(mockSessionContext).takeServerContext();
    }
}
