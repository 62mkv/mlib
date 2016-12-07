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

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.redprairie.moca.cache.infinispan.InfinispanCacheProvider;

/**
 * Simple in-memory implementation of CacheProvider.
 * 
 * Copyright (c) 2009 RedPrairie Corporation All Rights Reserved
 * 
 * @author dinksett
 */
public class DefaultCacheProvider implements MocaCacheProvider {
    // @see com.redprairie.moca.cache.CacheProvider#createCache(java.util.Map, com.redprairie.moca.cache.CacheController)
    @Override
    public <K, V> ConcurrentMap<K, V> createCache(String name,
                                                  Map<String, String> params,
                                                  CacheController<K, V> controller) {
        return new InfinispanCacheProvider().createCache(name, params, controller);
    }
}
