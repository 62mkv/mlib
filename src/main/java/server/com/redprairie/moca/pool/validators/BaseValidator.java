/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.pool.validators;

import com.redprairie.moca.pool.PoolException;
import com.redprairie.moca.pool.Validator;

/**
 * A validator that just defines base methods that do nothing for all methods.
 * An object is always valid with this validator.  This class can be used
 * as a base class to extend if it is desired to only implement a single
 * method.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class BaseValidator<T> implements Validator<T> {

    // @see com.redprairie.moca.pool.Validator#initialize(java.lang.Object)
    @Override
    public void initialize(T t) throws PoolException {
    }

    // @see com.redprairie.moca.pool.Validator#reset(java.lang.Object)
    @Override
    public void reset(T t) throws PoolException {
    }

    // @see com.redprairie.moca.pool.Validator#isValid(java.lang.Object)
    @Override
    public boolean isValid(T t) {
        return true;
    }

    // @see com.redprairie.moca.pool.Validator#invalidate(java.lang.Object)
    @Override
    public void invalidate(T t) {
    }
}
