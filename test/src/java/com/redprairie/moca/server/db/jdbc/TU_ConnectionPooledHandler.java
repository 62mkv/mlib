/*
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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

package com.redprairie.moca.server.db.jdbc;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.pool.Pool;
import com.redprairie.moca.pool.validators.SimpleValidator;
import com.redprairie.util.ClassUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit test to verify operations of a ConnectionPooledHandler proxy
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_ConnectionPooledHandler {
    
    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        _pool = Mockito.mock(Pool.class);
        _conn = Mockito.mock(Connection.class, Mockito.RETURNS_MOCKS);
        _stats = Mockito.mock(ConnectionStatistics.class);
        _validator = Mockito.mock(SimpleValidator.class);
        
        _connProxy = (Connection)Proxy.newProxyInstance(
            ClassUtils.getClassLoader(),
            new Class[] {Connection.class},
            new ConnectionPooledHandler(_pool, _conn, _stats, _validator));
    }
    
    @Test
    public void testCloseCommitWhenNoAutoCommit() throws SQLException {
        Mockito.when(_conn.getAutoCommit()).thenReturn(false);
        
        _connProxy.close();
        
        Mockito.verify(_pool).release(_conn);
        Mockito.verify(_conn).commit();
        Mockito.verifyZeroInteractions(_stats, _validator);
    }
    
    @Test
    public void testCloseNoCommitWhenAutoCommit() throws SQLException {
        Mockito.when(_conn.getAutoCommit()).thenReturn(true);
        
        _connProxy.close();
        
        Mockito.verify(_pool).release(_conn);
        Mockito.verify(_conn, Mockito.never()).commit();
        Mockito.verifyZeroInteractions(_stats, _validator);
    }
    
    @Test
    public void testCloseInvalidateOnException() throws SQLException {
        Mockito.when(_conn.prepareStatement(Mockito.anyString())).thenThrow(
            new SQLException());
        
        try {
            _connProxy.prepareStatement("throw the exception");
            fail("Should have thrown a SQLException");
        }
        catch (SQLException e) {
            // Should have gone here
        }
        
        // Now when we close the connection will be checked for validity
        _connProxy.close();
        
        Mockito.verify(_pool).release(_conn);
        Mockito.verify(_validator).setInvalid(_conn);
        Mockito.verifyZeroInteractions(_stats);
    }
    
    @Test
    public void testIsClosedBasedOnProxyClose() throws SQLException {
        assertFalse(_connProxy.isClosed());
        
        _connProxy.close();
        
        assertTrue(_connProxy.isClosed());
    }
    
    @Test
    public void testExceptionMetWhenClosed() throws SQLException {
        _connProxy.prepareStatement("Shouldn't throw exception");
        
        _connProxy.close();
        
        try {
            _connProxy.prepareStatement("Should throw exception");
            fail("Should have thrown a SQLException");
        }
        catch (SQLException e) {
            // Should go here
        }
    }
    
    @Test
    public void testStatementExecuteUpdate() throws SQLException {
        String statement = "Run something1";
        Statement stmt = _connProxy.createStatement();
        stmt.execute(statement);
        
        Mockito.verify(_stats).setLastSQL(statement);
    }
    
    @Test
    public void testPrepareStatementExecuteUpdate() throws SQLException {
        String statement = "Run something2";
        PreparedStatement stmt = _connProxy.prepareStatement(statement);
        stmt.execute();
        
        Mockito.verify(_stats).setLastSQL(statement);
    }
    
    @Test
    public void testPrepareCallExecuteUpdate() throws SQLException {
        String statement = "Run something3";
        PreparedStatement stmt = _connProxy.prepareCall(statement);
        stmt.execute();
        
        Mockito.verify(_stats).setLastSQL(statement);
    }
    
    Pool<Connection> _pool;
    Connection _conn;
    ConnectionStatistics _stats;
    SimpleValidator<Connection> _validator;
    
    Connection _connProxy;
}
