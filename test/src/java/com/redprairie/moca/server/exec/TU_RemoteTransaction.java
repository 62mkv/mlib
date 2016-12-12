/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.server.exec;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.client.MocaConnection;
import com.redprairie.moca.exceptions.RemoteSessionClosedException;
import com.redprairie.moca.exceptions.SessionClosedException;

import static org.junit.Assert.fail;

/**
 * This tests the behavior of remote transactions
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_RemoteTransaction {
    
    @Before
    public void beforeMethod() {
        _conn = Mockito.mock(MocaConnection.class);
    }
    
    @Test
    public void testPrepareSessionClosedException() throws MocaException {
        Mockito.when(_conn.executeCommand("prepare")).thenThrow(
            new SessionClosedException());
        RemoteTransaction remote = new RemoteTransaction(_conn, true);
        
        try {
            remote.prepare();
            fail("Should have thrown a RemoteSessionClosedException");
        }
        catch (RemoteSessionClosedException e) {
            
        }
    }
    
    @Test
    public void testPrepareExceptionOutOfTransaction() throws MocaException {
        Mockito.when(_conn.executeCommand("prepare")).thenThrow(
            new SessionClosedException());
        RemoteTransaction remote = new RemoteTransaction(_conn, false);
        
        // This shouldn't error
        remote.prepare();
    }
    
    @Test
    public void testCommitSessionClosedException() throws MocaException {
        Mockito.when(_conn.executeCommand("commit")).thenThrow(
            new SessionClosedException());
        RemoteTransaction remote = new RemoteTransaction(_conn, true);
        
        try {
            remote.commit();
            fail("Should have thrown a RemoteSessionClosedException");
        }
        catch (RemoteSessionClosedException e) {
            
        }
    }
    
    @Test
    public void testCommitExceptionOutOfTransaction() throws MocaException {
        Mockito.when(_conn.executeCommand("commit")).thenThrow(
            new SessionClosedException());
        RemoteTransaction remote = new RemoteTransaction(_conn, false);
        
        // This shouldn't error
        remote.commit();
    }
    
    @Test
    public void testRollbackSessionClosedException() throws MocaException {
        Mockito.when(_conn.executeCommand("rollback")).thenThrow(
            new SessionClosedException());
        RemoteTransaction remote = new RemoteTransaction(_conn, true);
        
        // We don't error with in tran since the remote connection is rolled
        // back anyways
        remote.rollback();
    }
    
    @Test
    public void testRollbackExceptionOutOfTransaction() throws MocaException {
        Mockito.when(_conn.executeCommand("rollback")).thenThrow(
            new SessionClosedException());
        RemoteTransaction remote = new RemoteTransaction(_conn, false);
        
        // This shouldn't error
        remote.rollback();
    }
    
    private MocaConnection _conn;
}
