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

import org.apache.logging.log4j.LogManager;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.common.collect.Sets;
import com.redprairie.moca.pool.Pool;
import com.redprairie.moca.pool.PoolException;
import com.redprairie.moca.pool.validators.BasePoolHandler;
import com.redprairie.moca.pool.validators.SimpleValidator;
import com.redprairie.util.ArgCheck;
import com.redprairie.util.ClassUtils;

/**
 * Dynamic proxy invocation handler for a pooled connection.
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class ConnectionPooledHandler extends BasePoolHandler<Connection> {
    
    /**
     * @param pool the pool that this connection is a part of.  This
     * argument cannot be null.
     * @param target the target connection defer calls to.  This argument
     * cannot be null.
     * @param stats The stats that should be updated when this connection is
     *        utilized
     * @param validator The validator that should be notified when this
     *        connection becomes invalid
     */
    @SuppressWarnings("unchecked")
    ConnectionPooledHandler(Pool<Connection> pool, Connection target, 
        ConnectionStatistics stats, SimpleValidator<Connection> validator) {
        super(pool, target, validator, 
            Sets.<Class<? extends Exception>>newHashSet(SQLException.class));
        ArgCheck.notNull(stats);
        _stats = stats;
    }

    // Javadoc inherited from interface
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        // Check for specific methods.
        if (method.getName().equals("isClosed")) {
            return Boolean.valueOf(_isClosed());
        }
        else {
            Object result = super.invoke(proxy, method, args);
                
            // For statement results, (Statement, PreparedStatement, PreparedCall), create a proxy
            if (result instanceof Statement) {
                Class<? extends Object> resultClass = result.getClass();
                String sql = null;
                if (result instanceof PreparedStatement && args.length > 0
                        && args[0] instanceof String) {
                    sql = (String) args[0];
                }
                
                result = Proxy.newProxyInstance(resultClass.getClassLoader(),
                                                ClassUtils.getInterfaces(resultClass),
                                                new StatementHandler(sql, _stats, result));
            }
            
            return result;
        }
    }
    
    //
    // Package interface
    //
    
    /*
     * Handle the _isClosed method.
     */
    protected boolean _isClosed() throws SQLException {
        if (_target == null) {
            return true;
        }
        else {
            try {
                return _target.isClosed();
            }
            catch (SQLException e) {
                setError();
                throw e;
            }
        }
    }
    
    /**
     * Close the connection and determine whether the connection is
     * working properly. If so, return it to the pool, otherwise remove
     * it from the pool. Additionally, this method will call
     * <code>commit</code> on the wrapped connection if its AutoCommit is
     * set to false.
     *
     * @throws SQLException if there was a database error while closing any
     *      contained <code>Statement</code> objects or resetting the
     *      AutoCommit flag to true.
     */
    protected void handleClose() throws PoolException {
        // Return to Pool
        if (_target != null) {
            try {
                if (!_target.getAutoCommit()) {
                    _target.commit();
                }
            }
            catch (SQLException e) {
                setError();
                throw new SQLPoolException(e);
            }
            finally {
                super.handleClose();
            }
        }
    }
    
    // @see com.redprairie.moca.pool.validators.BasePoolHandler#checkTarget()
    @Override
    protected void checkTarget() throws SQLException {
        try {
            super.checkTarget();
        }
        catch (Exception e) {
            if (e instanceof SQLException) {
                throw (SQLException)e;
            }
            throw new SQLException(e);
        }
    }
    
    // @see java.lang.Object#hashCode()
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((_stats == null) ? 0 : _stats.hashCode());
        return result;
    }

    // @see java.lang.Object#equals(java.lang.Object)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        ConnectionPooledHandler other = (ConnectionPooledHandler) obj;
        if (_stats == null) {
            if (other._stats != null) return false;
        }
        else if (!_stats.equals(other._stats)) return false;
        return true;
    }



    private final ConnectionStatistics _stats;
}
