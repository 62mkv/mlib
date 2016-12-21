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

package com.sam.moca.server.legacy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class LoggingMocaServerAdapter implements InvocationHandler {
    /**
     * @param stats TODO
     * 
     */
    public LoggingMocaServerAdapter(MocaServerAdapter delegate, NativeCallStatistics stats) {
        _delegate = delegate;
        _stats = stats;
    }
    
    // @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        long start = System.nanoTime();
        try {
            return method.invoke(_delegate, args);
        }
        catch (InvocationTargetException e) {
            throw e.getCause();
        }
        finally {
            long end = System.nanoTime();
            String detail = null;
            if (args != null && args[0] instanceof String) {
                detail = (String)args[0];
            }
            _stats.logCall(method.getName(), detail, start, end);
        }
    }
    

    private final MocaServerAdapter _delegate;
    private final NativeCallStatistics _stats;
}
