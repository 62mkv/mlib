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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sam.moca.pool.PoolException;
import com.sam.moca.pool.Validator;

/**
 * This validator allows for multiple validators to be used in the place of
 * one.  The is {@link #isValid(Object)} method will iterate over the validators 
 * and if one returns false it will return false without running the rest of the 
 * validators.  If a validator throws an exception the rest of the validators 
 * are still ran but only the first exception is propagated.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MultiValidator<T> implements Validator<T> {
    public MultiValidator(Iterable<? extends Validator<? super T>> validators) {
        List<Validator<? super T>> list = new ArrayList<Validator<? super T>>();
        
        for (Validator<? super T> validator : validators) {
            list.add(validator);
        }
        
        _validators = Collections.unmodifiableList(list);
    }
    
    // @see com.sam.moca.pool.Validator#initialize(java.lang.Object)
    @Override
    public void initialize(T t) throws PoolException {
        if (t == null) {
            throw new PoolException(-2, "Object creation problem encountered, " +
                    "check trace output!");
        }
        PoolException firstException = null;
        for (Validator<? super T> validator : _validators) {
            try {
                validator.initialize(t);
            }
            catch (PoolException e) {
                if (firstException == null) {
                    firstException = e;
                }
                else {
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("PoolException ignored as was not first", e);
                    }
                }
            }
        }
        
        if (firstException != null) {
            throw firstException;
        }
    }
    
    // @see com.sam.moca.pool.Validator#reset(java.lang.Object)
    @Override
    public void reset(T t) throws PoolException {
        PoolException firstException = null;
        for (Validator<? super T> validator : _validators) {
            try {
                validator.reset(t);
            }
            catch (PoolException e) {
                if (firstException == null) {
                    firstException = e;
                }
                else {
                    if (_logger.isDebugEnabled()) {
                        _logger.debug("PoolException ignored as was not first", e);
                    }
                }
            }
        }
        
        if (firstException != null) {
            throw firstException;
        }
    }
    
    // @see com.sam.moca.pool.Validator#isValid(java.lang.Object)
    @Override
    public boolean isValid(T t) {
        boolean valid = true;
        for (Validator<? super T> validator : _validators) {
            if (!(valid = validator.isValid(t))) {
                break;
            }
        }
        return valid;
    }
    
    // @see com.sam.moca.pool.Validator#invalidate(java.lang.Object)
    @Override
    public void invalidate(T t) {
        for (Validator<? super T> validator : _validators) {
            validator.invalidate(t);
        }
    }
    
    private final Iterable<Validator<? super T>> _validators;
    
    private static final Logger _logger = LogManager.getLogger(MultiValidator.class); 
}
