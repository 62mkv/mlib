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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.sam.moca.MocaRegistry;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.exec.SystemContext;
import com.sam.util.ClassUtils;

/**
 * Class with utility methods to manage application-level caches.  This class
 * maintains a list of current caches, and allows those caches to be cleared
 * from a central location.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class CacheManager {
    /**
     * Returns a cache of the given name. If the cache already exists as a cache
     * object, it is returned. If not, it will be created with default values.
     * Default values come from registry keys associated with the named cache.
     * If no cache configuration exists in the registry, reasonable defaults
     * will be used.
     * 
     * @param <K>
     * @param <V>
     * @param name the name of the cache.
     * @param controller
     * @return a valid cache of the given name.  This method will not return null.
     */
    public static <K,V> ConcurrentMap<K,V> getCache(String name, CacheController<K, V> controller) {
        return getCache(name, null, controller, null);
    }
    
    /**
     * Returns a cache of the given name. If the cache already exists as a cache
     * object, it is returned. If not, it will be created from the provider
     * given as an argument passing in the map parameters and desired controller
     * @param <K> The type associated to the key in the cache
     * @param <V> The type associated to the value in the cache
     * @param name The name of the cache to lookup and use
     * @param provider The provider to control what cache object is spawned from
     *        this invocation.  This can be null.
     * @param controller The controler to dictate how objects are loaded in the cache
     *        initially and on misses.  This can be null.
     * @param params The map parameters that can be used by a controllers.  
     *        This can be null and if so it will load up the values from the
     *        registry.  If an actual map object is provided it will not look
     *        at the registry.
     * @return The cache object either from a previous invocation or newly 
     *         returned.  This method will not return null.
     */
    @SuppressWarnings("unchecked")
    synchronized
    public static <K,V> ConcurrentMap<K,V> getCache(String name, MocaCacheProvider provider, 
        CacheController<K, V> controller, Map<String, String> params) {
        ConcurrentMap<K,V> cache = (ConcurrentMap<K,V>)_caches.get(name);
        
        if (cache == null) {
            SystemContext ctx = ServerUtils.globalContext();
            
            if (params == null) {
                Map<String, String> cacheSection = ctx.getConfigurationSection(MocaRegistry.REGKEY_CACHE, true);
                params = new HashMap<String, String>();
                
                // Go through the list of cache settings, extracting the
                // parameters for this cache entry from the list.
                for (Map.Entry<String, String> configEntry : cacheSection.entrySet()) {
                    if (configEntry.getKey().startsWith(name.toUpperCase() + ".")) {
                        params.put(configEntry.getKey().substring(name.length() + 1).toLowerCase(),
                                   configEntry.getValue());
                    }
                }
            }
            
            if (provider == null) {
                // First we check if the provider is in the map or not
                String cacheProviderName = params.get(MocaRegistry.REGKEY_CACHE_FACTORY_SUFFIX);
                if (cacheProviderName != null) {
                    provider = ClassUtils.instantiateClass(cacheProviderName, MocaCacheProvider.class);
                }
                
                // If the provider is still null then use the default
                if (provider == null) {
                    provider = new DefaultCacheProvider();
                }
            }
            
            if (controller == null) {
                // First check the controller in the map, if it is not there
                // then we don't use a controller
                String controllerName = params.get(MocaRegistry.REGKEY_CACHE_CONTROLLER_SUFFIX);
                if (controllerName != null) {
                    controller = ClassUtils.instantiateClass(controllerName, CacheController.class);
                }
            }
             
            cache = provider.createCache(name, params, controller);

            _caches.put(name, cache);
        }
        
        return cache;
    }
    
    /**
     * Clears all caches currently under management. This calls the
     * <code>clear</code> method on all caches currently being managed by this
     * class.
     */
    synchronized
    public static void clearCaches() {
        for (ConcurrentMap<?, ?> cache : _caches.values()) {
            cache.clear();
        }
    }

    /**
     * Clears the cache of the given name.
     */
    synchronized
    public static void clearCache(String name) {
        ConcurrentMap<?, ?> cache = (ConcurrentMap<?, ?>) _caches.get(name);
        if (cache != null) {
            cache.clear();
        }
    }
    
    private static Map<String, ConcurrentMap<?, ?>> _caches = 
        new HashMap<String, ConcurrentMap<?, ?>>();
}
