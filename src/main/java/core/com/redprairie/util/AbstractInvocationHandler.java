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

package com.redprairie.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Proxy Invocation Handler that deals with methods defined by Object correctly.  This class is useful
 * for implementing proxies such as remote services that need to have local behavior for certain Object
 * methods, such as equals and hashCode.
 * 
 * This class defines the three protected methods proxyToString, proxyHashCode, and proxyEquals.  These methods
 * allow subclasses to change the behavior of the corresponding methods on Object.  If not extended, the default
 * behavior is the same as that of Object -- equals and hashCode operate on object identity of the proxy instance,
 * and toString returns the hex representation of the identity hashcode.
 * 
 * In addition this class defines the abstract protected method proxyInvoke, which has the same semantics as
 * InvocationHandler.invoke.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author derek
 * @version $Revision$
 */
public abstract class AbstractInvocationHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        Class<?> declaringClass = m.getDeclaringClass();

        if (declaringClass == Object.class) {
            if (m.equals(hashCodeMethod)) {
                return proxyHashCode(proxy);
            }
            else if (m.equals(equalsMethod)) {
                return proxyEquals(proxy, args[0]);
            }
            else if (m.equals(toStringMethod)) {
                return proxyToString(proxy);
            }
            else {
                throw new RuntimeException("unexpected Object method dispatched: " + m);
            }
        }
        else {
            return proxyInvoke(proxy, m, args);
        }
    }


    /**
     * invoked by the {@link #invoke(Object, Method, Object[])} method after checking for the predefined methods handled
     * using the default behavior of Object.
     * @param proxy the instance of the proxy for which this handler is being invoked.
     * @param m the method being called.
     * @param args the arguments being passed
     * @return the result of the method call.
     * @throws Throwable 
     */
    protected abstract Object proxyInvoke(Object proxy, Method m, Object[] args) throws Throwable;

    /**
     * called when the {@link Object#hashCode()} method of this proxy is called.
     * @param proxy
     * @return
     */
    protected int proxyHashCode(Object proxy) {
        return System.identityHashCode(proxy);
    }

    /**
     * called when the {@link Object#equals(Object)} method of this proxy is called. 
     * @param proxy
     * @param other
     * @return
     */
    protected boolean proxyEquals(Object proxy, Object other) {
        return (proxy == other);
    }

    /**
     * called when the {@link Object#toString()} method of this proxy is called. 
     * @param proxy
     * @return
     */
    protected String proxyToString(Object proxy) {
        return proxy.getClass().getName() + '@' + Integer.toHexString(proxy.hashCode());
    }

    // preloaded Method objects for the methods in java.lang.Object
    private static final Method hashCodeMethod;
    private static final Method equalsMethod;
    private static final Method toStringMethod;
    static {
        try {
            hashCodeMethod = Object.class.getMethod("hashCode", (Class[]) null);
            equalsMethod = Object.class.getMethod("equals", new Class[] { Object.class });
            toStringMethod = Object.class.getMethod("toString", (Class[]) null);
        }
        catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }
}
