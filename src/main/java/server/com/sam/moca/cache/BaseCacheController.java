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

package com.sam.moca.cache;

import java.util.concurrent.ConcurrentMap;

/**
 * Base implementation of a cache controller.  This allows subclasses to either
 * implement all-at-once cache building, or entry-at-a-time cache building.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class BaseCacheController<K, V> implements CacheController<K, V> {

    // @see com.sam.moca.cache.CacheController#loadAll(com.sam.moca.cache.MocaCache)
    @Override
    public void loadAll(ConcurrentMap<K, V> cache) {
        return;
    }

    // @see com.sam.moca.cache.CacheController#loadEntry(java.lang.Object)
    @Override
    public V loadEntry(K key) {
        return null;
    }

}
