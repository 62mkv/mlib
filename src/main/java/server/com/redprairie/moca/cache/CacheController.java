/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.cache;

import java.util.concurrent.ConcurrentMap;

/**
 * Class that performs on-the-fly or all-at-once loading of the cache.  Instances
 * of this class respond to cache events, such as a cache miss (loadEntry) or
 * cache instantiation (loadAll).
 * 
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public interface CacheController<K,V> {
    /**
     * Called when a cache miss occurs.  If this method returns null, the
     * cache get method returns null on a cache miss.
     * @param key
     * @return
     */
    public V loadEntry(K key);
    
    /**
     * Called when the cache is first loaded.
     * @param cache a reference to the cache that is being loaded.
     */
    public void loadAll(ConcurrentMap<K, V> cache);
}
