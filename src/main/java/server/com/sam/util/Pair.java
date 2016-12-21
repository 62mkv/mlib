/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.sam.util;

import java.io.Serializable;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class Pair<T, V> implements Serializable {
    
    public Pair(T first, V second) {
        _first = first;
        _second = second;
    }
    
    /**
     * @return Returns the first value.
     */
    public T getFirst() {
        return _first;
    }
    /**
     * @return Returns the second value.
     */
    public V getSecond() {
        return _second;
    }

    private static final long serialVersionUID = 5901015356228914862L;
    
    private final T _first;
    private final V _second;
}
