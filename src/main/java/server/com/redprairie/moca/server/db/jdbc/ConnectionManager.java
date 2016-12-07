/*
 *  $URL$
 *  $Author$
 *  $Date$
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

package com.redprairie.moca.server.db.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import com.google.common.base.Preconditions;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.util.AbstractInvocationHandler;
import com.redprairie.util.ClassUtils;

/**
 * This class handles registering various data sources to a named named string.
 * You can then ask for {@link Connection} instances given a named string.
 * This way all your datasources can be registered early on and code can 
 * retrieve connections by just knowing what name to use. 
 * <p>
 * All connections retrieved via the ConnectionManager are proxied so that
 * they will register themselves with the transaction manager if a transaction
 * is present in the current thread.
 * <p>
 * Additionally, a threadsafe {@link ConnectionValidator} must be provided when registering
 * a datasource, this will be used when connections are validated via {@link Connection#isValid(int)}
 * bypassing the standard JDBC implementation as not all drivers implement this functionality.
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class ConnectionManager {
    
    private ConnectionManager() {}
    
    public static void registerDataSource(String name, CommonDataSource source,
        PropertyProvider provider, ConnectionValidator connectionValidator) {
        DataSourcePropertyEditor editor = provider.getPropertyEditor();
        
        registerDataSource(name, source, editor, connectionValidator);
    }
    
    static void registerDataSource(String name, CommonDataSource source, 
            DataSourcePropertyEditor editor, ConnectionValidator connectionValidator) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(source);
        Preconditions.checkNotNull(editor);
        Preconditions.checkNotNull(connectionValidator);

        ConnectionDataSourceEntry entry = new ConnectionDataSourceEntry(source, editor, connectionValidator);
        
        // If there was something else then error
        if (_connectionEntries.putIfAbsent(name, entry) != null) {
            throw new IllegalArgumentException("Data source named " + name + " already registered!");
        }
        else {
            _logger.debug(MocaUtils.concat("Registered datasource - (", source, 
                ") for named url ", name));
        }
    }
    
    public static Connection getConnection(String name,
            Properties properties) throws SQLException {
        ConnectionDataSourceEntry entry = _connectionEntries.get(name);
        if (entry == null) {
            throw new IllegalArgumentException("The connection named " + name + 
                " was not configured!");
        }
        
        // We have to synchronize on the entry so the data source can have it's
        // property set and then guarantee the connection is retrieved with
        // those properties
        MocaDBConnection mocaConn = null;
        synchronized (entry) {
            entry._editor.applyProperties(entry._dataSource, properties);
            
            if (entry._dataSource instanceof XADataSource) {
                XADataSource source = (XADataSource)entry._dataSource;
                
                XAConnection connection = source.getXAConnection();
                
                mocaConn = new XAMocaDBConnection(connection, entry._connectionValidator);
            }
            else if (entry._dataSource instanceof DataSource) {
                DataSource source = (DataSource)entry._dataSource;
                
                Connection connection = source.getConnection();
                
                mocaConn = new NonXAMocaDBConnection(connection, entry._connectionValidator);
            }
            else {
                throw new IllegalStateException("Unsupported data source found!");
            }
        }

        return proxyMocaConnection(mocaConn);
    }
    
    private static class ConnectionDataSourceEntry {
        
        public ConnectionDataSourceEntry(CommonDataSource dataSource, DataSourcePropertyEditor editor,
                                         ConnectionValidator connectionValidator) {
            _dataSource = dataSource;
            _editor = editor;
            _connectionValidator = connectionValidator;
        }
        
        private final DataSourcePropertyEditor _editor;
        private final CommonDataSource _dataSource;
        private final ConnectionValidator _connectionValidator;
    }
    
    private static Connection proxyMocaConnection(MocaDBConnection mocaConnection) throws SQLException {
        Connection realConnection = mocaConnection.getConnection();
        
        return (Connection)Proxy.newProxyInstance(
            ClassUtils.getClassLoader(), 
            org.springframework.util.ClassUtils.getAllInterfaces(realConnection), 
            new XAResourceAwareConnection(mocaConnection));
    }
    
    /**
     * Used to wrap a {@link MocaDBConnection} as a {@link Connection} proxy
     * intercepting specific methods e.g. {@link Connection#isValid(int)} and
     * {@link Connection#close()}. All other methods are just delegated to the
     * real {@link Connection}.
     */
    private static class XAResourceAwareConnection extends AbstractInvocationHandler {
        
        public XAResourceAwareConnection(MocaDBConnection mocaConn) {
            _mocaConn = mocaConn;
        }
        
        // @see com.redprairie.util.AbstractInvocationHandler#proxyInvoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
        @Override
        protected Object proxyInvoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            
            switch (method.getName()) {
                case "close"   : _doClose();
                                 return null;
                
                // Bypass the standard JDBC isValid and use our own (not all drivers implement this)
                // Note: We don't want this check to register the transaction
                // as this is typically used to just check if the connection is still alive
                // e.g. in a database pool (such actions shouldn't start a transaction)
                case "isValid" : return _mocaConn.isValid();
                
                // Otherwise, call the real Connections method making sure
                // that it's been registered with the transaction
                default        : _mocaConn.registerTransactionInThread();
                                 return invokeOnRealConnection(method, args);
            }
        }
        
        private Object invokeOnRealConnection(Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(_mocaConn.getConnection(), args);
            }
            catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
        
        private void _doClose() throws SQLException {
            _mocaConn.close();
        }
        
        // @see java.lang.Object#finalize()
        @Override
        protected void finalize() throws Throwable {
            _doClose();
        }
        
        private final MocaDBConnection _mocaConn;
    }
    
    private final static Logger _logger = LogManager.getLogger(ConnectionManager.class);
    private final static ConcurrentMap<String, ConnectionDataSourceEntry> _connectionEntries = 
        new ConcurrentHashMap<String, ConnectionManager.ConnectionDataSourceEntry>();
    
    static {
        // This is done so the class will register itself with the DriverManager
        _logger.debug("Loading up class " + MocaDriver.class);
    }
}
