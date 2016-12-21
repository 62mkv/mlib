/*
 *  $URL$
 *  $Author$
 *  $Date$
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

package com.sam.moca.pool.validators;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A validator that can be used so that an object will only be taken out of
 * pool so many times before it is automatically marked as invalid.  All values
 * are stored weakly so if the object is no longer referenced by other classes
 * will be available for garbage collection.  If an object is found to be 
 * invalid any subsequent checks will say it is valid unless it is set as
 * invalid again.
 * <p>
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * <p>
 * @author wburns
 */
public class PoolUsageValidator<T> extends BaseValidator<T> {
    public PoolUsageValidator(int maxUsageCount) {
        _commandCountLimit = maxUsageCount;
    }
    
    // @see com.sam.moca.pool.validators.BaseValidator#reset(java.lang.Object)
    @Override
    public void reset(T t) {
        AtomicInteger count = _usageCounts.get(t);
        if (count == null) {
            count = new AtomicInteger();
            _usageCounts.put(t, count);
        }
        
        count.incrementAndGet();
    }
    
    // @see com.sam.moca.pool.validators.BaseValidator#isValid(java.lang.Object)
    @Override
    public boolean isValid(T t) {
        AtomicInteger count = _usageCounts.get(t);
        // As long as the usage count is less than the limit it is valid
        boolean valid = count == null || count.get() < _commandCountLimit;
        if (!valid) {
            _usageCounts.remove(t);
            if (_logger.isDebugEnabled()) {
                _logger.debug("Pool object: " + t + " forced invalid due to max " + 
                        "usage of " + _commandCountLimit + " reached");
            }
        }
        return valid;
    }
    
    /**
     * Returns how often this object has been taken from the pool
     * @param t The pooled object to test
     * @return How often the pooled object has been taken or null if this
     *         object has not been taken
     */
    public Integer getCount(T t) {
        AtomicInteger count = _usageCounts.get(t);
        if (count != null) {
            return count.get();
        }
        return null;
    }
    
    /**
     * We do not concurrently touch the same key, but different keys could
     * all hit the map at the same time so we synchronize access to it
     */
    private final Map<T, AtomicInteger> _usageCounts = Collections.synchronizedMap(
        new WeakHashMap<T, AtomicInteger>());
    
    private final int _commandCountLimit;
    
    private static final Logger _logger = LogManager.getLogger(PoolUsageValidator.class);
}
