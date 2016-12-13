/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
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

package com.redprairie.moca.server.expression;

import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.dispatch.DispatchResult;
import com.redprairie.moca.server.dispatch.MessageResolver;
import com.redprairie.moca.server.dispatch.RequestDispatcher;
import com.redprairie.moca.server.exec.RequestContext;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SessionContext;
import com.redprairie.moca.server.session.SessionToken;

import static org.junit.Assert.assertEquals;

/**
 * This class is to test to make sure that error message expressions
 * work correctly.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_ErrorMessageExpression {
    @Test
    public void testMessageReplacementCorrectly() throws SystemConfigurationException, MocaException {
        String bareMessage = "The problem was caused by ^cause^";
        String replacement = "failure to wash hands";
        int errorCode = 12;

        // We have to mock out the arguments as well, since we can't create
        // them directly
        MocaException.Args mockArgs = Mockito.mock(MocaException.Args.class);
        Mockito.when(mockArgs.getName()).thenReturn("cause");
        Mockito.when(mockArgs.getValue()).thenReturn(replacement);
        
        // We then mock out the exception to have it return our mocked arguments
        // and set the error code to what we expect
        MocaException mockException = Mockito.mock(MocaException.class);
        Mockito.when(mockException.getArgList()).thenReturn(
                new MocaException.Args[] { mockArgs });
        Mockito.when(mockException.getErrorCode()).thenReturn(errorCode);

        // This is the message resolver lookup, acts as a get mls id call.
        MessageResolver mockResolver = Mockito.mock(MessageResolver.class);
        Mockito.when(mockResolver.getMessage("err" + errorCode)).thenReturn(bareMessage);
        
        // The server context has to return our mesage resolver and any time
        // a command is executed it will throw our Exception
        ServerContext mockContext = Mockito.mock(ServerContext.class);
        Mockito.when(mockContext.getMessageResolver()).thenReturn(mockResolver);
        Mockito.when(mockContext.executeCommandWithRemoteContext(
                Mockito.anyString(), Mockito.anyCollectionOf(MocaArgument.class), 
                Mockito.anyCollectionOf(MocaArgument.class))).thenThrow(mockException);
        
        // Finally we create the factory to be used passing in our fake server
        // context
        ServerContextFactory mockFactory = Mockito.mock(
                ServerContextFactory.class);
        Mockito.when(mockFactory.newContext(Mockito.isA(RequestContext.class), 
                Mockito.isA(SessionContext.class))).thenReturn(mockContext);
        
        RequestDispatcher dispatcher = new RequestDispatcher(mockFactory);
        
        SessionContext mockSesContext = Mockito.mock(SessionContext.class);
        Mockito.when(mockSesContext.getSessionToken())
                .thenReturn(new SessionToken("TEST"));
        
        DispatchResult result = dispatcher.executeCommand(
                "set return status " +
                "  where status = " + errorCode + 
                "    and cause = '" + replacement + "'", null, null, 
                new RequestContext(), 
                mockSesContext, false, false);
        
        String message = result.getMessage();
        
        // Now we finally make sure the message that came back was the error
        // we wanted
        assertEquals(bareMessage.replaceAll("\\^cause\\^", replacement), message);
    }
}
