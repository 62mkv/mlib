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

package com.redprairie.moca.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Abstract factory that produces cache objects.
 * 
 * Copyright (c) 2016 Sam Corporation All Rights Reserved
 * 
 * @author dinksett
 */
public interface MocaCacheProvider {
    /**
     * Create a cache object with the given parameters and using the named cache
     * controller for lazy loading.
     * 
     * @param <K>
     * @param <V>
     * @param name The name of the cache.
     * @param params Cache parameters, specific to this cache, from the MOCA
     *            registry. This parameter may not be null, but the map may be
     *            empty
     * @param controller a controller instance to be used with this cache. This
     *            parameter may be null, indicating no controller.
     * @return a cache object
     */
    public <K, V> ConcurrentMap<K, V> createCache(String name,
                                              Map<String, String> params, CacheController<K, V> controller);
}
