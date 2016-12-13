/*
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

package com.redprairie.moca.server.db.jdbc;

import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.Builder;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.pool.BlockingPool;
import com.redprairie.moca.pool.BlockingPoolBuilder;
import com.redprairie.moca.pool.PoolException;
import com.redprairie.moca.pool.Validator;
import com.redprairie.moca.pool.validators.SimpleValidator;
import com.redprairie.moca.server.db.NamedConnection;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.util.ClassUtils;

/**
 * A data source that can be used to retrieve connection objects.  This source
 * will draw from the given pool when new objects are requested.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class ConnectionPoolDataSource implements MocaDataSource {
    
    /**
     * @param connectionNames
     * @param timeout
     * @param builder
     */
    public ConnectionPoolDataSource(int timeout, TimeUnit timeUnit, 
        int minIdleConnections, int maxConnections, 
        Builder<? extends NamedConnection> builder, 
        Validator<? super Connection> validator) {
        _connTimeout = timeout;
        _connUnit = timeUnit;
        
        _busyConns = new ConcurrentHashMap<Connection, ConnectionStatistics>();
        _idleConns = new ConcurrentHashMap<Connection, ConnectionStatistics>();
        
        _connectionNames = new ConcurrentHashMap<Connection, String>();
        
        // We wrap their validator with ours so we can capture some info
        _validator = new ValidatorInterceptor(validator);
        _pool = new BlockingPoolBuilder<Connection>(
                new BuilderConvertor(builder, _connectionNames))
                .name("Connection")
                .validator(_validator)
                .minMaxSize(minIdleConnections, maxConnections)
                .build();
    }

    // @see javax.sql.CommonDataSource#getLogWriter()
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return _logWriter;
    }

    // @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        _logWriter = out;
    }

    // @see javax.sql.CommonDataSource#setLoginTimeout(int)
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        _connTimeout = seconds;
        _connUnit = TimeUnit.SECONDS;
    }

    // @see javax.sql.CommonDataSource#getLoginTimeout()
    @Override
    public int getLoginTimeout() throws SQLException {
        return (int)_connUnit.toSeconds(_connTimeout);
    }

    // @see java.sql.Wrapper#unwrap(java.lang.Class)
    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        boolean isInstance = iface.isInstance(this);
        
        if (isInstance) {
            return iface.cast(this);
        }
        
        return null;
    }

    // @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }

    // @see com.redprairie.moca.server.db.jdbc.MocaDataSource#closeAll()
    @Override
    public void closeAll() {
        _pool.shutdown();
    }

    // @see com.redprairie.moca.server.db.jdbc.MocaDataSource#getIdleConnections()
    @Override
    public Map<Connection, ConnectionStatistics> getIdleConnections() {
        return new HashMap<Connection, ConnectionStatistics>(_idleConns);
    }

    // @see com.redprairie.moca.server.db.jdbc.MocaDataSource#getBusyConnections()
    @Override
    public Map<Connection, ConnectionStatistics> getBusyConnections() {
        return new HashMap<Connection, ConnectionStatistics>(_busyConns);
    }

    // @see com.redprairie.moca.server.db.jdbc.MocaDataSource#getConnectionPoolStatistics()
    @Override
    public ConnectionPoolStatistics getConnectionPoolStatistics() {
        int current = _idleConns.size() + _busyConns.size();
        return new ConnectionPoolStatistics(current, _peakSize.get(), 
            _pool.getMaximumSize());
    }

    // @see com.redprairie.moca.server.db.jdbc.MocaDataSource#getSize()
    @Override
    public int getSize() {
        return _idleConns.size() + _busyConns.size();
    }

    // @see com.redprairie.moca.server.db.jdbc.MocaDataSource#getMaxSize()
    @Override
    public Integer getMaxSize() {
        return _pool.getMaximumSize();
    }

    // @see javax.sql.DataSource#getConnection()
    @Override
    public Connection getConnection() throws SQLException {
        Connection conn;
        try {
            conn = _pool.get(_connTimeout, _connUnit);
            if (conn == null) {
                _logger.warn("A timeout occurred attempting to get a database " +
                        "connection from the pool");
                throw new SQLException("exceeded db connection timeout "
                        + "value: " + _connTimeout + " " + _connUnit);
            }
        }
        catch (InterruptedException e) {
            _logger.debug("Interrupted while waiting for Database connection!");
            throw new MocaInterruptedException(e);
        }
        
        ConnectionStatistics stats = _idleConns.remove(conn);
        
        stats.setThread(Thread.currentThread());
        
        _busyConns.put(conn, stats);
        
        _logger.debug(MocaUtils.concat("Retrieving ",
            stats.getConnectionIdentifier(), " from db pool."));
        
        Connection pooledConn = (Connection)Proxy.newProxyInstance(
                ClassUtils.getClassLoader(),
                new Class[] {Connection.class},
                new ConnectionPooledHandler(_pool, conn, stats, _validator));
        return pooledConn;
    }

    // @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
    @Override
    public Connection getConnection(String username, String password)
            throws SQLException {
        throw new SQLException("cannot create a connection with a unique"
                + " username and password");
    }
    
    // @see javax.sql.CommonDataSource#getParentLogger()
    public java.util.logging.Logger getParentLogger()
            throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
    
    class ValidatorInterceptor extends SimpleValidator<Connection> {
        public ValidatorInterceptor(Validator<? super Connection> validator) {
            _theirValidator = validator;
        }
        
        // @see com.redprairie.moca.pool.Validator#initialize(java.lang.Object)
        @Override
        public void initialize(Connection t) throws PoolException {
            // It is possible this is null if an exception was caught or login
            // timeout was reached.
            if (t != null) {
                String connectionName = _connectionNames.remove(t);
                // If we weren't notified about the name we should put something
                // at least
                _idleConns.put(t, new ConnectionStatistics(connectionName));
                // We have to update peak and current at same time
                synchronized (_currentSize) {
                    int current = _currentSize.incrementAndGet();
                    _peakSize.compareAndSet(current - 1, current);
                }
            }
            _theirValidator.initialize(t);
        }

        // @see com.redprairie.moca.pool.Validator#reset(java.lang.Object)
        @Override
        public void reset(Connection t) throws PoolException {
            _theirValidator.reset(t);
        }

        // @see com.redprairie.moca.pool.Validator#isValid(java.lang.Object)
        @Override
        public boolean isValid(Connection t) {
            // The super class is used to tell whether we should test validity or not
            boolean noError = super.isValid(t);
            boolean isValid = true;
            // If this connection had an error we have to check the validity
            if (!noError) {
                isValid = _theirValidator.isValid(t);
            }
            ConnectionStatistics stats = _busyConns.remove(t);
            // If the connection is still valid move the stats to idle state
            if (isValid) {
                _logger.debug(MocaUtils.concat("Returning ",
                    stats.getConnectionIdentifier(), " to the db pool."));
                
                stats.setThread(null);
                _idleConns.put(t, stats);
            }
            else {
                if (stats != null) {
                    _logger.debug(MocaUtils.concat("Invalid db connection not returned to pool: ",
                        stats.getConnectionIdentifier()));
                }
            }
            return isValid;
        }

        // @see com.redprairie.moca.pool.Validator#invalidate(java.lang.Object)
        @Override
        public void invalidate(Connection t) {
            _currentSize.decrementAndGet();
            _theirValidator.invalidate(t);
            
            ConnectionStatistics stats = _busyConns.remove(t);
            if (stats != null) {
                _logger.debug(MocaUtils.concat("Invalidating busy db connection: ",
                    stats.getConnectionIdentifier()));
            }
            stats = _idleConns.remove(t);
            if (stats != null) {
                _logger.debug(MocaUtils.concat("Invalidating idle db connection: ",
                    stats.getConnectionIdentifier()));
            }
        }
        
        private final Validator<? super Connection> _theirValidator;
        private final AtomicInteger _currentSize = new AtomicInteger();
    }
    
    static class BuilderConvertor implements Builder<Connection> {
        /**
         * 
         */
        public BuilderConvertor(Builder<? extends NamedConnection> builder,
            ConcurrentMap<Connection, String> connectionNames) {
            _builder = builder;
            _connectionNames = connectionNames;
        }
        
        // @see com.redprairie.moca.Builder#build()
        @Override
        public Connection build() {
            NamedConnection namedConn = _builder.build();
            Connection conn = null;
            if (namedConn != null) {
                conn = namedConn.getConnection();
                _connectionNames.put(conn, namedConn.getName());
            }
            return conn;
        }
        
        private final Builder<? extends NamedConnection> _builder;
        private final ConcurrentMap<Connection, String> _connectionNames;
    }
    
    // Our pool exists in the following collections:
    // A map that describes the busy objects
    // A map that describes the idle objects
    // These are not private since it is referenced by inner class
    final ConcurrentMap<Connection, ConnectionStatistics> _busyConns;
    final ConcurrentMap<Connection, ConnectionStatistics> _idleConns;
    
    private final ConcurrentMap<Connection, String> _connectionNames;
    
    private final BlockingPool<Connection> _pool;
    private final ValidatorInterceptor _validator;

    // The peak size of this pool, this is not private since it is referenced
    // by inner class
    final AtomicInteger _peakSize = new AtomicInteger();
    
    // Needed to provide logging capability
    private PrintWriter _logWriter = null;
    
    // The timeout to use for logging in - default 30 secs
    private int _connTimeout = 30;
    private TimeUnit _connUnit = TimeUnit.SECONDS; 
    
    private static final Logger _logger = LogManager.getLogger(ConnectionPoolDataSource.class);
}