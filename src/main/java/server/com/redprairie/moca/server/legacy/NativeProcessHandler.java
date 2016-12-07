/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.server.legacy;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Sets;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.pool.Pool;
import com.redprairie.moca.pool.PoolException;
import com.redprairie.moca.pool.validators.BasePoolHandler;
import com.redprairie.moca.pool.validators.SimpleValidator;

/**
 * This is a handler for native processes that basically will log a message
 * when a remote exception is occurred stating that a crash happened.
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class NativeProcessHandler extends BasePoolHandler<NativeProcess> {

    /**
     * @param pool
     * @param target
     * @param validator
     * @param exceptions
     */
    @SuppressWarnings("unchecked")
    public NativeProcessHandler(Pool<NativeProcess> pool, NativeProcess target,
            SimpleValidator<NativeProcess> validator) {
        super(pool, target, validator, 
            Sets.newHashSet(RemoteException.class, MocaInterruptedException.class));
    }

    // @see com.redprairie.moca.pool.validators.BasePoolHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        try {
            if (method.getName().equals("getId")) {
                return _target.getId();
            }
            
            return super.invoke(proxy, method, args);
        }
        catch (RemoteException e) {
            MocaNativeProcessPool._logger.error("Native Process Crash at " + method.getName());
            MocaNativeProcessPool._logger.debug("Native Process Crash at " + method.getName(), e);
            throw e;
        }
    }
    
    // @see com.redprairie.moca.pool.validators.BasePoolHandler#isClosed()
    
    @Override
    protected boolean isClosed() {
        return _closed.get();
    }
    
    // @see com.redprairie.moca.pool.validators.BasePoolHandler#handleClose()
    @Override
    protected void handleClose() throws PoolException {
        // We only want to release it the first time it was closed
        if (!_closed.getAndSet(true)) {
            _pool.release(_target);
        }
    }
    
    // @see com.redprairie.moca.pool.validators.BasePoolHandler#checkTarget()
    @Override
    protected void checkTarget() throws RemoteException {
        try {
            super.checkTarget();
        }
        catch (Exception e) {
            if (e instanceof RemoteException) {
                throw (RemoteException)e;
            }
            throw new RemoteException(e.getMessage(), e);
        }
    }
    
    // @see java.lang.Object#hashCode()
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((_closed == null) ? 0 : _closed.hashCode());
        return result;
    }

    // @see java.lang.Object#equals(java.lang.Object)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        NativeProcessHandler other = (NativeProcessHandler) obj;
        if (_closed == null) {
            if (other._closed != null) return false;
        }
        else if (!_closed.equals(other._closed)) return false;
        return true;
    }



    protected final AtomicBoolean _closed = new AtomicBoolean(false);
}
