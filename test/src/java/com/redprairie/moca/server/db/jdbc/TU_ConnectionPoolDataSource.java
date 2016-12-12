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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.Builder;
import com.redprairie.moca.pool.Validator;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.db.NamedConnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Test class for database pool source.
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_ConnectionPoolDataSource {
    
    @BeforeClass
    public static void setupClass() throws SystemConfigurationException {
        ServerUtils.setupDaemonContext(TU_ConnectionPoolDataSource.class.getName(), 
                true);
        _logger = LogManager.getLogger(TU_ConnectionPoolDataSource.class);
    }
    
    /**
     * Test that different connections can be obtained from a data source.
     */
    @Test
    public void testPoolSize() throws Exception {
        ConnectionPoolDataSource pool = _setupProperties(1, 3);
        
        Connection conn1 = pool.getConnection();
        conn1.close();
        
        conn1 = pool.getConnection();
        Connection conn2 = pool.getConnection();
        Connection conn3 = pool.getConnection();
        _verifyPoolSize(pool, 0, 3);
        conn1.close();
        conn3.close();
        _verifyPoolSize(pool, 2, 1);
        conn2.close();
        _verifyPoolSize(pool, 3, 0);
    }
    
    /**
     * Test that a <code>SQLException</code> is thrown if the connection time
     * out time specified is exceeded.
     * @throws Throwable 
     */
    @Test
    public void testConnectionTimeOut() throws Throwable {
        int maxSize = 3;
        int timeout = 700;
        Connection[] connections = new Connection[maxSize];
        
        ConnectionPoolDataSource pool = _setupProperties(maxSize, maxSize, timeout, 
            TimeUnit.MILLISECONDS);
        
        // get all the connections available in the connection pool
        for (int i = 0; i < maxSize; i++) {
            connections[i] = pool.getConnection();
        }
        
        // now there should be no more connections available in the pool, so
        // introduce a waiter
        ConnectionRetriever waiter = new ConnectionRetriever(pool);
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        try {
            Future<Connection> future = executor.submit(waiter);
            
            // Now we try to wait for that thread for the same amount of time the 
            // timeout is plus another 100 milliseconds 
            future.get(timeout + 100, TimeUnit.MILLISECONDS);
            fail("We should have received a SQLException for timeout");
        }
        catch (ExecutionException e) {
            Throwable realException = e.getCause();
            
            if (!(realException instanceof SQLException)) {
                // We should have got a SQLException, but if not then throw it
                // up
                throw realException;
            }
        }
        finally {
            // Try to shutdown the threads as well
            executor.shutdownNow();
        }
    }
    
    @Test
    public void testGetConnectionsConcurrentlyAlot() throws InterruptedException, 
            SQLException, ExecutionException {
        int minIdle = 10;
        int max = 100;
        int totalGrabs = max * 10;
        int steals = max / 5;
        ConnectionPoolDataSource pool = _setupProperties(minIdle, max, 1, 
            TimeUnit.SECONDS);
        
        // Make sure we spawn them all
        Connection[] connections = new Connection[max];
        for (int i = 0; i < max; ++i) {
            connections[i] = pool.getConnection();
        }
        
        for (int i = 0; i < max; ++i) {
            connections[i].close();
        }
        
        ExecutorService service = Executors.newFixedThreadPool(max);
        try {
            Callable<Void> callable = new ConnectionRetrieverAndReturner(pool);
            CompletionService<Void> completionService = 
                    new ExecutorCompletionService<Void>(service);
            
            Set<Future<Connection>> futures = new HashSet<Future<Connection>>();
            Callable<Connection> callable2 = new ConnectionRetriever(pool);
            CompletionService<Connection> completionService2 = 
                    new ExecutorCompletionService<Connection>(service);
            
            for (int i = 0; i < totalGrabs; ++i) {
                if (i % (totalGrabs / steals) == 0) {
                    completionService2.submit(callable2);
                }
                completionService.submit(callable);
            }
            
            // Wait for all get and return to finish first
            for (int i = 0; i < totalGrabs; ++i) {
                completionService.poll(2, TimeUnit.SECONDS);
            }
            
            // Then make sure all the steals are completed
            for (int i = 0; i < steals; ++i) {
                futures.add(completionService2.poll(2, TimeUnit.SECONDS));
            }
            
            assertEquals(futures.size(), pool.getBusyConnections().size());
            assertEquals(max - futures.size(), pool.getIdleConnections().size());
            
            for (Future<Connection> future : futures) {
                future.get().close();
            }
            
            assertEquals(max, pool.getIdleConnections().size());
            assertEquals(0, pool.getBusyConnections().size());
        }
        finally {
            pool.closeAll();
            service.shutdownNow();
        }
    }

    /**
     * Make sure that after connections are returned to the pool, any waiters
     * are handed the newly available connection.
     * @throws Throwable 
     */
    @Test
    public void testWaitingForReturnedConnections() throws Throwable {
        int maxSize = 3;
        int timeout = 500;
        Connection[] connections = new Connection[maxSize];
        
        ConnectionPoolDataSource pool = _setupProperties(2, maxSize, timeout, 
            TimeUnit.MILLISECONDS);
        
        // get all the connections available in the connection pool
        for (int i = 0; i < maxSize; i++) {
            connections[i] = pool.getConnection();
        }
        
        // now there should be no more connections available in the pool, so
        // introduce a waiter
        ConnectionRetriever waiter = new ConnectionRetriever(pool);
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        try {
            try {
                Future<Connection> future = executor.submit(waiter);
        
                // Now we try to wait for that thread for the same amount of time the 
                // timeout is plus another 100 milliseconds 
                future.get(timeout + 100, TimeUnit.MILLISECONDS);
                fail("We should have received a SQLException for timeout");
            }
            catch (ExecutionException e) {
                Throwable realException = e.getCause();
                
                if (!(realException instanceof SQLException)) {
                    // We should have got a SQLException, but if not then throw it
                    // up
                    throw realException;
                }
            }
            
            Future<Connection> future = executor.submit(waiter);
            
            // Now release a connection which should let it be retrieved
            connections[0].close();
            
            Connection conn = future.get(100, TimeUnit.SECONDS);
            
            assertNotNull(conn);
        }
        finally {
            // Try to shutdown the threads as well
            executor.shutdownNow();
        }
    }
    
    /**
     * Tests that the connections are put in correct state when closeAll is
     * invoked
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testCloseAll() throws Exception {
        int maxSize = 10;
        Connection[] connections = new Connection[maxSize];
        
        Validator<Connection> validator = Mockito.mock(Validator.class);
        Mockito.when(validator.isValid(Mockito.any(Connection.class))).thenReturn(true);
        
        ConnectionPoolDataSource pool = _setupProperties(2, maxSize, validator);
        
        // Creations happen asynchronously
        Mockito.verify(validator, Mockito.timeout(1000).times(2)).initialize(
            Mockito.any(Connection.class));
        
        assertEquals(2, pool.getIdleConnections().size());
        assertEquals(0, pool.getBusyConnections().size());
        
        // get all the connections available in the connection pool
        for (int i = 0; i < maxSize; i++) {
            connections[i] = pool.getConnection();
        }
        
        assertEquals(0, pool.getIdleConnections().size());
        assertEquals(maxSize, pool.getBusyConnections().size());
        
        // Then close them, returning the to the pool
        for (int i = 0; i < maxSize; i++) {
            connections[i].close();
        }
        
        assertEquals(maxSize, pool.getIdleConnections().size());
        assertEquals(0, pool.getBusyConnections().size());
        
        // Close the whole pool.
        pool.closeAll();
        
        // Invalidations occur asynchronously
        Mockito.verify(validator, Mockito.timeout(1000).times(maxSize)).invalidate(
            Mockito.any(Connection.class));
        
        assertEquals(0, pool.getIdleConnections().size());
        assertEquals(0, pool.getBusyConnections().size());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testResetCalledTrue() throws Exception {
        int maxSize = 3;
        
        Validator<Connection> validator = Mockito.mock(Validator.class);
        Mockito.when(validator.isValid(Mockito.any(Connection.class))).thenReturn(true);
        
        ConnectionPoolDataSource pool = _setupProperties(1, maxSize, validator);
        
        Connection conn = pool.getConnection();
        conn.close();
        
        Mockito.verify(validator).reset(Mockito.any(Connection.class));
    }
    
    @Test
    public void testConnectionInvalidRemovedFromPool() throws Exception {
        
    }
    
    /**
     * An connection retriever
     */
    private static class ConnectionRetriever implements Callable<Connection> {
        public ConnectionRetriever(ConnectionPoolDataSource pool) {
            _pool = pool;
        }

        @Override
        public Connection call() throws Exception {
            _logger.debug("Stealing connection");
            Connection conn = _pool.getConnection();
            _logger.debug("Stole connection");
            return conn;
        }
        
        private final ConnectionPoolDataSource _pool;
    }
    
    private static class ConnectionRetrieverAndReturner implements Callable<Void> {
        public ConnectionRetrieverAndReturner(ConnectionPoolDataSource pool) {
            _pool = pool;
        }
        // @see java.util.concurrent.Callable#call()
        @Override
        public Void call() throws Exception {
            _logger.debug("Yanking connection");
            Connection conn = _pool.getConnection();
            _logger.debug("Releasing connection");
            conn.close();
            return null;
        }
        
        private final ConnectionPoolDataSource _pool;
    }
    
    private void _verifyPoolSize(ConnectionPoolDataSource pool, int idle,
                                 int busy) {
        assertEquals(idle, pool.getIdleConnections().size());
        assertEquals(busy, pool.getBusyConnections().size());
    }

    private ConnectionPoolDataSource _setupProperties(int minIdleSize, 
        int maxSize, int connTimeout, TimeUnit unit, Validator<Connection> validator) {
        Builder<NamedConnection> builder = new Builder<NamedConnection>() {
            @Override
            public NamedConnection build() {
                _logger.debug("Creating new connection");
                Connection conn = Mockito.mock(Connection.class);
                return new NamedConnection(conn, "TEST");
            }
        };
        return new ConnectionPoolDataSource(connTimeout, unit, minIdleSize, 
            maxSize, builder, validator);
    }

    private ConnectionPoolDataSource _setupProperties(int minIdleSize, 
        int maxSize, Validator<Connection> validator) {
        return _setupProperties(minIdleSize, maxSize, 30, TimeUnit.SECONDS, validator);
    }

    private ConnectionPoolDataSource _setupProperties(int minIdleSize, int maxSize) {
        return _setupProperties(minIdleSize, maxSize, 30, TimeUnit.SECONDS);
    }
    
    @SuppressWarnings("unchecked")
    private ConnectionPoolDataSource _setupProperties(int minIdleSize, 
        int maxSize, int timeout, TimeUnit unit) {
        Validator<Connection> validator = Mockito.mock(Validator.class);
        Mockito.when(validator.isValid(Mockito.any(Connection.class))).thenReturn(true);
        return _setupProperties(minIdleSize, maxSize, timeout, unit, validator);
    }
    
    private static Logger _logger;
}
