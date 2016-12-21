/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * A simple fixed-size LRU cache.  This class extends a Java collections
 * class to implement its behavior. 
 * 
 * <p><strong>Note</strong> this implementation is not synchronized, so any
 * callers that allow access to an instance of this class from multiple
 * threads should take care to wrap it with a synchronized wrapper:
 * <blockquote><code>
 *     Map cache = Collections.synchronizedMap(new FixedSizeCache(100));
 * </code></blockquote>
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class FixedSizeCache<K,V> extends LinkedHashMap<K,V> {

    /**
     * Creates an instance of this class with a fixed cache size.
     * 
     * @param maxSize the maximum size of this cache.  After the
     * (<code>maxSize + 1</code>)th element is added to the cache, the least
     * recently accessed element will be removed.
     */
    public FixedSizeCache(int maxSize) {
        super(16, 0.75f, true);
        _maxSize = maxSize;
    }
    
    //
    // Subclass interface
    //
    protected boolean removeEldestEntry(Entry<K, V> eldest) {
        return size() > _maxSize;
    }
    
    //
    // Implementation
    //
    private static final long serialVersionUID = -240237160404870212L;
    private final int _maxSize;
}
