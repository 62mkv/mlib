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

package com.sam.moca.pool.validators;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

import com.sam.moca.pool.Pool;
import com.sam.moca.pool.PoolException;
import com.sam.util.ArgCheck;

/**
 * Dynamic proxy invocation handler for a pooled object.  This will handle
 * any object that has a close method, by returning the object to the pool and
 * marking the current proxy as invalid.  Also this proxy supports being passed
 * a set of exceptions, if a subtype of one of the exceptions is thrown the
 * pool object will be marked invalid on the provided SimpleValidator, which
 * should force it to be removed from the pool when released.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class BasePoolHandler<T> implements InvocationHandler {
    
    /**
     * @param pool
     * @param target
     * @param validator
     * @param exceptions
     */
    public BasePoolHandler(Pool<T> pool, T target, SimpleValidator<T> validator,
        Set<Class<? extends Exception>> exceptions) {
        ArgCheck.notNull(pool);
        ArgCheck.notNull(target);
        ArgCheck.notNull(validator);
        _pool = pool;
        _target = target;
        _validator = validator;
        _exceptions = exceptions;
    }
    
    // @see java.lang.Object#hashCode()
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_validator == null) ? 0 : _validator.hashCode());
        result = prime * result + ((_pool == null) ? 0 : _pool.hashCode());
        result = prime * result + ((_target == null) ? 0 : _target.hashCode());
        return result;
    }

    // @see java.lang.Object#equals(java.lang.Object)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        
        // We have to deproxy it if possible
        BasePoolHandler<?> otherProxy = null;
        if (obj instanceof BasePoolHandler) {
            otherProxy = (BasePoolHandler<?>) obj;
        }
        else if (Proxy.isProxyClass(obj.getClass())) {
            InvocationHandler ih = Proxy.getInvocationHandler(obj);
            if (!(ih instanceof BasePoolHandler)) {
                return false;
            }
            otherProxy = (BasePoolHandler<?>) ih;
        }
        else {
            // Not a valid comparison...
            return false;
        }
        
        if (_validator == null) {
            if (otherProxy._validator != null) return false;
        }
        else if (!_validator.equals(otherProxy._validator)) return false;
        if (_pool == null) {
            if (otherProxy._pool != null) return false;
        }
        else if (!_pool.equals(otherProxy._pool)) return false;
        if (_target == null) {
            if (otherProxy._target != null) return false;
        }
        else if (!_target.equals(otherProxy._target)) return false;
        return true;
    }

    // @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        // Check for specific methods.
        if (method.getName().equals("close") && (args == null || args.length == 0)) {
            handleClose();
            return null;
        }
        else if (method.getName().equals("equals") && 
                (args != null && args.length == 1)) {
            return equals(args[0]);
        }
        else if (method.getName().equals("hashCode") && 
                (args == null || args.length == 0)) {
            return hashCode();
        }
        else if (method.getName().equals("toString")) {
            return "BasePoolHandler proxying [" + _target + "]";
        }
        else {
            
            boolean error = false;
            checkTarget();
            try {
                return method.invoke(_target, args);
            }
            catch (InvocationTargetException e) {
                Throwable t = e.getCause();
                for (Class<? extends Exception> eClass : _exceptions) {
                    // If the exception is one ours set it to be invalid
                    if (eClass.isAssignableFrom(t.getClass())) {
                        error = true;
                        break;
                    }
                }
                throw t;
            }
            finally {
                if (error) {
                    setError();
                }
            }
        }
    }
    
    /*
     * Check that the target is not null, if so throw an exception.
     * By default this throws a PoolException, however this may not be the
     * declared exception that can be thrown as such people who extend this class
     * should convert the exception to something declared in the method throws
     * to be nice to callers
     */
    protected void checkTarget() throws Exception {
        if (isClosed()) {
            throw new PoolException(12345, "Pooled object has been closed");
        }
        return;
    }
    
    /**
     * Can be overridden to handle whether this object has been closed
     * @return
     */
    protected boolean isClosed() {
        return _target == null;
    }
    
    /**
     * Handle the case where this connection is not closed correctly.  This
     * should handle the situation where the client does not release things
     * cleanly.
     */
    protected void finalize() throws Throwable {
        // Allow the GC to do most of the work.  Our only job is to make sure
        // the pool counters get updated, and that we "drop" the target
        // from the pool.
        try {
            handleClose();
        }
        finally {
            super.finalize();
        }
    }
    
    public T getRealObject() {
        return _target;
    }
    
    /**
     * Return the object back to the pool, the pool decides whether it is
     * valid or not.
     *
     * @throws PoolException this can be thrown from a subclass if needed.
     */
    protected void handleClose() throws PoolException {
        // Return to Pool
        if (_target != null) {
            _pool.release(_target);
            _target = null;
        }
    }
    
    //
    // Implementation
    //
    /**
     * This method should be called whenever a Object has become invalid
     */
    public void setError() {
        _validator.setInvalid(_target);
    }

    protected final Pool<T> _pool;
    protected final SimpleValidator<T> _validator;
    protected T _target;
    protected final Set<Class<? extends Exception>> _exceptions;
}
