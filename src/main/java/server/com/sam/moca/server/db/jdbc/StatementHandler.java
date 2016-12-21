/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

import org.apache.logging.log4j.LogManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.sam.util.AbstractInvocationHandler;

/**
 * Proxy that is used to wrap a jdbc Statement object and update statistics
 * when methods are invoked. 
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class StatementHandler extends AbstractInvocationHandler {
    
    @Override
    protected Object proxyInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        
        if (methodName.equals("execute") || methodName.equals("executeUpdate") || methodName.equals("executeQuery")) {
            if (_sql != null) {
                _stats.setLastSQL(_sql);
            }
            else if (args.length > 0) {
                _stats.setLastSQL(String.valueOf(args[0]));
            }
        }
        
        try {
            return method.invoke(_delegate, args);
        }
        catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
    
    // Package interface
    
    public StatementHandler(String sql, ConnectionStatistics stats, Object delegate) {
        _sql = sql;
        _stats = stats;
        _delegate = delegate;
    }
    
    // Implementation

    private final String _sql;
    private final ConnectionStatistics _stats;
    private final Object _delegate;
}
