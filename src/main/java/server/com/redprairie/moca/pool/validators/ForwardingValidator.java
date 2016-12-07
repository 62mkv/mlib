/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2014
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

package com.redprairie.moca.pool.validators;

import com.google.common.collect.ForwardingObject;
import com.redprairie.moca.pool.PoolException;
import com.redprairie.moca.pool.Validator;

/**
 * Forwarding object for {@link Validator} so that subclasses
 * can decorate and augment the behavior on the particular
 * methods they're interested in.
 * 
 * Copyright (c) 2014 RedPrairie Corporation
 * All Rights Reserved
 */
public class ForwardingValidator<T> extends ForwardingObject implements Validator<T> {
    
    public ForwardingValidator(Validator<T> delegate) {
        _delegate = delegate;
    }
    
    // @see com.redprairie.moca.pool.Validator#initialize(java.lang.Object)
    @Override
    public void initialize(T t) throws PoolException {
        delegate().initialize(t);
    }

    // @see com.redprairie.moca.pool.Validator#reset(java.lang.Object)
    @Override
    public void reset(T t) throws PoolException {
        delegate().reset(t);
    }

    // @see com.redprairie.moca.pool.Validator#isValid(java.lang.Object)
    @Override
    public boolean isValid(T t) {
        return delegate().isValid(t);
    }

    // @see com.redprairie.moca.pool.Validator#invalidate(java.lang.Object)
    @Override
    public void invalidate(T t) {
        delegate().invalidate(t);
    }
    
    // @see com.google.common.collect.ForwardingObject#delegate()
    @Override
    protected Validator<T> delegate() {
        return _delegate;
    }

    private final Validator<T> _delegate;
}
