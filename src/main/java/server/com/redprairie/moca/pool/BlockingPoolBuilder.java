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

package com.redprairie.moca.pool;

import com.redprairie.moca.Builder;
import com.redprairie.moca.pool.impl.AbstractBlockingPool;
import com.redprairie.moca.pool.impl.DemandBasedBlockingPool;
import com.redprairie.moca.pool.impl.FixedSizeBlockingPool;
import com.redprairie.moca.pool.impl.MinIdleBlockingPool;
import com.redprairie.moca.pool.validators.BaseValidator;
import com.redprairie.moca.pool.validators.MultiValidator;
import com.redprairie.util.ArgCheck;

/**
 * Builder that can be used to create blocking pool instances.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class BlockingPoolBuilder<T> implements Builder<BlockingPool<T>> {
    /**
     * Constructor to be used with the pool.  A builder is always required.
     * @param builder The builder to use for the pool
     */
    public BlockingPoolBuilder(Builder<? extends T> builder) {
        ArgCheck.notNull(builder, "Builder is required for pool to be built");
        _builder = builder;
    }
    /**
     * Sets the validator to be used for this BlockingPool
     * @param validator
     * @return
     */
    public BlockingPoolBuilder<T> validator(Validator<? super T> validator) {
        ArgCheck.notNull(validator);
        _validator = validator;
        return this;
    }
    
    /**
     * Sets more than 1 validators.  This set will not be used until 
     * construction time.  Thus if you are updating the Iterable you should
     * update it after invoking the build method.
     * @param validators Multiple validators which will be used in the order
     *        that the iterator returns them
     * @return this
     */
    public BlockingPoolBuilder<T> validators(Iterable<? extends Validator<? super T>> validators) {
        ArgCheck.notNull(validators);
        _validator = new MultiValidator<T>(validators);
        return this;
    }
    
    /**
     * If a fixed size pool is desired.  The pool will try to make sure
     * there are always this amount of pool objects present.
     * @param size The size you want the pool to always be
     * @return this
     */
    public BlockingPoolBuilder<T> fixedSize(int size) {
        ArgCheck.isTrue(size > 0, "Size must be greater than 0");
        _min = size;
        _max = size;
        return this;
    }
    
    /**
     * A min idle based pool.  This type of pool will try to make sure at
     * least a certain number of idle objects are available.  The only exception
     * to this is if the maximum size of the pool is reached.  If min idle
     * is set to 0 then the pool will only create objects as they are requested.
     * @param min The min idle value to pass.  This number must be 0 or higher
     * @param max The maximum size this pool will get.  This number must be
     *        higher than 0 and min
     * @return this
     */
    public BlockingPoolBuilder<T> minMaxSize(int min, int max) {
        ArgCheck.isTrue(min >= 0, "Minimum size must be greater than or equal to 0");
        ArgCheck.isTrue(max >= min && max > 0, "Maximum size (" + max + ") must be greater " +
                "than or equal to minimum size (" + min + ") and greater than 0");
        _min = min;
        _max = max;
        return this;
    }
    
    /**
     * The name to name the pool, useful for debugging as it will be displayed
     * when showing pool contents
     * @param name The name of the pool
     * @return this
     */
    public BlockingPoolBuilder<T> name(String name) {
        _name = name;
        return this;
    }

    // @see com.redprairie.moca.Builder#build()
    @Override
    public BlockingPool<T> build() {
        Validator<? super T> validator = _validator;
        if (validator == null) {
            validator = new BaseValidator<T>();
        }
        
        ArgCheck.isTrue(_min >= 0, "Minimum size must be greater than 0");
        ArgCheck.isTrue(_max >= _min && _max > 0, 
                "Maximum size (" + _max + ") must be greater than or equal to " +
                "minimum size (" + _min + ") and also greater than 0");
        
        AbstractBlockingPool<T> pool;
        if (_min == 0) {
            pool = new DemandBasedBlockingPool<T>(_max, _name, validator, 
                    _builder);
        }
        else if (_min == _max) {
            pool = new FixedSizeBlockingPool<T>(_min, _name, validator,
                _builder);
        }
        else {
            pool = new MinIdleBlockingPool<T>(_min, _max, _name,
                validator, _builder);
        }
        
        pool.initializePool();
        return pool;
    }
    
    private String _name = null;
    private int _min = 0;
    private int _max = 0;
    private Validator<? super T> _validator;
    private final Builder<? extends T> _builder;
}
