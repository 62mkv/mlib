/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.redprairie.moca.server.exec;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.exceptions.RemoteSessionClosedException;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.db.DBAdapter;
import com.redprairie.moca.server.db.MocaTransaction;

import static org.junit.Assert.fail;

/**
 * This class tests various areas of the transaction manager.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_MocaTransactionManager {
    
    @BeforeClass
    public static void beforeClass() throws SystemConfigurationException {
        ServerUtils.setupDaemonContext(TU_MocaTransactionManager.class.getName(), 
                true);
    }
    
    @Before
    public void beforeTest() {
        _argumentSource = Mockito.mock(ArgumentSource.class);
        _dbAdapter = Mockito.mock(DBAdapter.class);
        _dbTransaction = Mockito.mock(MocaTransaction.class);
        Mockito.when(_dbAdapter.newTransaction()).thenReturn(_dbTransaction);
        _transactions = new ArrayDeque<MocaTransaction>();
        _transactions.add(_dbTransaction);
    }
    
    @Test
    public void testRemoteSuccess() throws MocaException, SQLException {
        MocaTransactionManager manager = new MocaTransactionManager(
            _argumentSource, _dbAdapter, _transactions);
        
        // Local transaction is open so we have to rollback in this case
        Mockito.when(_dbTransaction.isOpen()).thenReturn(true);
        
        RemoteTransaction conn = Mockito.mock(RemoteTransaction.class);
        
        Map<String, RemoteTransaction> map = new HashMap<String, RemoteTransaction>();
        map.put("foo", conn);
        
        Mockito.when(_dbTransaction.getAttribute(
            MocaTransactionManager.REMOTE_TX_ATTR)).thenReturn(map);
        
        manager.commit();
        
        Mockito.verify(_dbTransaction).commit();
        
        InOrder inOrder = Mockito.inOrder(conn);
        
        inOrder.verify(conn).prepare();
        inOrder.verify(conn).commit();
    }
    
    @Test
    public void testRemotePrepareFailure() throws MocaException, SQLException {
        MocaTransactionManager manager = new MocaTransactionManager(
            _argumentSource, _dbAdapter, _transactions);
        
        // Local transaction is open so we have to rollback in this case
        Mockito.when(_dbTransaction.isOpen()).thenReturn(true);
        
        RemoteTransaction conn = Mockito.mock(RemoteTransaction.class);
        
        Mockito.doThrow(new RemoteSessionClosedException()).when(conn).prepare();
        
        Map<String, RemoteTransaction> map = new HashMap<String, RemoteTransaction>();
        map.put("foo", conn);
        
        Mockito.when(_dbTransaction.getAttribute(
            MocaTransactionManager.REMOTE_TX_ATTR)).thenReturn(map);
        
        try {
            manager.commit();
            fail("Should have thrown a Session Closed Exception");
        }
        catch (RemoteSessionClosedException e) {
            
        }
        
        Mockito.verify(_dbTransaction).rollback();
    }
    
    private ArgumentSource _argumentSource;
    private DBAdapter _dbAdapter;
    private MocaTransaction _dbTransaction;
    private Deque<MocaTransaction> _transactions;
}
