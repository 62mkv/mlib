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

package com.redprairie.moca.mad.async;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a status with how long the status has been set
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author klucas
 */
public class Status {
    /**
     * Create a status with an initial status.
     * 
     * @param status The status
     */
    public Status(String status) {
        _status.set(status);
        _setTime.set(System.nanoTime());
    }

    /**
     * Sets a status and resets the set time
     * 
     * @param status The status
     */
    public void setStatus(String status) {
        _status.set(status);
        _setTime.set(System.nanoTime());
    }
    
    /**
     * Gets the current status
     * @return The status
     */
    public String getStatus() {
        return _status.get();
    }
    
    /**
     * Gets the amount of time the current status has been set in unit units
     * @param unit The time unit to return
     * @return The amount of time the currrent status has been set in unit units
     */
    public long getAge(TimeUnit unit) {
        long ns = System.nanoTime() - _setTime.get();
        
        return unit.convert(ns, TimeUnit.NANOSECONDS);
    }
    
    private final AtomicReference<String> _status = new AtomicReference<String>();
    private final AtomicLong _setTime = new AtomicLong();
}
