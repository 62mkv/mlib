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

package com.sam.moca.server.db.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

/**
 * This is a data source implementation that when requested of a connection
 * will calls to the driver to get the connection.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class DriverDataSource implements DataSource {
    
    public DriverDataSource(String url, Driver driver) {
        _url = url;
        _driver = driver;
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return "DriverDataSource - driver=[" + _driver + "] for url [" + _url + 
        "]";
    }
    
    // @see javax.sql.CommonDataSource#getLogWriter()
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    // @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
    }

    // @see javax.sql.CommonDataSource#setLoginTimeout(int)
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
    }

    // @see javax.sql.CommonDataSource#getLoginTimeout()
    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
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

    // @see javax.sql.DataSource#getConnection()
    @Override
    synchronized
    public Connection getConnection() throws SQLException {
        if (_nextCallProps == null) {
            _nextCallProps = new Properties(_defaultProps.get());
        }
        return _driver.connect(_url, _nextCallProps);
    }

    // @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
    @Override
    synchronized
    public Connection getConnection(String username, String password)
            throws SQLException {
        if (_nextCallProps == null) {
            _nextCallProps = new Properties(_defaultProps.get());
        }
        try {
            _nextCallProps.put("user", username);
            _nextCallProps.put("password", password);
            return _driver.connect(_url, _nextCallProps);
        }
        finally {
            _nextCallProps = null;
        }
    }
    
    // @see javax.sql.CommonDataSource#getParentLogger()
    public java.util.logging.Logger getParentLogger()
            throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }
    
    synchronized
    public void setProperties(Properties props) {
        _nextCallProps = new Properties();
        _nextCallProps.putAll(_defaultProps.get());
        _nextCallProps.putAll(props);
    }
    
    synchronized
    public void initializeDefaultProperties(Properties props) {
        Properties defaultProps = new Properties();
        defaultProps.putAll(props);
        if (!_defaultProps.compareAndSet(null, defaultProps)) {
            throw new IllegalStateException("Default properties were initialized twice!");
        }
    }
    
    private Properties _nextCallProps = null;
    private final Driver _driver;
    private final String _url;
    private final AtomicReference<Properties> _defaultProps = 
        new AtomicReference<Properties>();
}
