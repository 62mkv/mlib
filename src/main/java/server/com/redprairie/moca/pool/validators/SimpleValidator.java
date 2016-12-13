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

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Simple validator that only overrides {@link #isValid(Object)} method so
 * it will return whether or not the object was marked as being invalid.  The
 * objects being validated should have appropriate {@link #hashCode()} and 
 * {@link #equals(Object)} methods for proper functioning.  All values
 * are stored weakly so if the object is no longer referenced by other classes
 * will be available for garbage collection.  If an object is found to be 
 * invalid any subsequent checks will say it is valid unless it is set as
 * invalid again.
 * <p>
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class SimpleValidator<T> extends BaseValidator<T> {
    public boolean isValid(T t) {
        return _validity.remove(t) == null;
    }
    
    public void setInvalid(T t) {
        _validity.put(t, VALUE);
    }
    
    private static final Object VALUE = new Object();
    private final Map<T, Object> _validity = Collections.synchronizedMap(
        new WeakHashMap<T, Object>());
}
