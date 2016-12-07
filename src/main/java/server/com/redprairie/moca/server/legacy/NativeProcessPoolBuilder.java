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

import java.util.concurrent.TimeUnit;

import com.redprairie.moca.Builder;
import com.redprairie.util.ArgCheck;

/**
 * This is a builder for a NativeProcessPool.
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class NativeProcessPoolBuilder implements Builder<NativeProcessPool> {
    // @see com.redprairie.moca.Builder#build()
    @Override
    public NativeProcessPool build() {
        ArgCheck.notNull(_processBuilder);
        ArgCheck.isTrue(_maxSize > 0, "Pool max size must be greater than 0");
        if (_maxUsage != null) {
            ArgCheck.isTrue(_maxUsage > 0, "If usage count is provided it must be greater than 0");
        }
        // Currently we only support MocaNativeProcessPool
        return new MocaNativeProcessPool(_timeout, _unit, _minIdleSize, _maxSize, 
            _maxUsage, _processBuilder);
    }
    
    public NativeProcessPoolBuilder builder(Builder<? extends NativeProcess> processBuilder) {
        ArgCheck.notNull(processBuilder);
        _processBuilder = processBuilder;
        return this;
    }
    
    public NativeProcessPoolBuilder pooltimeout(long timeout, TimeUnit unit) {
        ArgCheck.notNull(unit);
        _timeout = timeout;
        _unit = unit;
        return this;
    }
    
    public NativeProcessPoolBuilder fixedSize(int size) {
        size(size, size);
        return this;
    }
    
    public NativeProcessPoolBuilder size(int minIdle, int max) {
        ArgCheck.isTrue(minIdle >= 0, "Minimum size must be greater than or equal to 0");
        ArgCheck.isTrue(max >= minIdle && max > 0, "Maximum size (" + max + ") must be greater " +
                "than or equal to minimum size (" + minIdle + ") and greater than 0");
        _minIdleSize = minIdle;
        _maxSize = max;
        return this;
    }
    
    /**
     * Sets the pools max amount of times a pooled object can be taken out
     * before forcibly being invalidated.  A value of null means it will be able
     * to be taken out as many times as wanted unless found invalid another way.
     * The default value is unlimited.
     * @param usage
     * @return
     */
    public NativeProcessPoolBuilder maxUsage(Integer usage) {
        if (usage != null) {
            ArgCheck.isTrue(usage > 0, "If usage count is provided it must be greater than 0");
        }
        _maxUsage = usage;
        return this;
    }
    
    private Integer _maxUsage;
    private int _minIdleSize;
    private int _maxSize;
    private long _timeout = 1;
    private TimeUnit _unit = TimeUnit.SECONDS;
    private Builder<? extends NativeProcess> _processBuilder;
}
