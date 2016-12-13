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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class TestCacheProvider implements MocaCacheProvider {
    
    public static class TestCache<K,V> implements ConcurrentMap<K,V> {

        // @see java.util.Map#size()
        
        @Override
        public int size() {
            return 0;
        }

        // @see java.util.Map#isEmpty()
        
        @Override
        public boolean isEmpty() {
            return false;
        }

        // @see java.util.Map#containsKey(java.lang.Object)
        
        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        // @see java.util.Map#containsValue(java.lang.Object)
        
        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        // @see java.util.Map#get(java.lang.Object)
        
        @Override
        public V get(Object key) {
            return null;
        }

        // @see java.util.Map#put(java.lang.Object, java.lang.Object)
        
        @Override
        public V put(K key, V value) {
            return null;
        }

        // @see java.util.Map#remove(java.lang.Object)
        
        @Override
        public V remove(Object key) {
            return null;
        }

        // @see java.util.Map#putAll(java.util.Map)
        
        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            
        }

        // @see java.util.Map#clear()
        
        @Override
        public void clear() {
            
        }

        // @see java.util.Map#keySet()
        
        @Override
        public Set<K> keySet() {
            return null;
        }

        // @see java.util.Map#values()
        
        @Override
        public Collection<V> values() {
            return null;
        }

        // @see java.util.Map#entrySet()
        
        @Override
        public Set<Entry<K, V>> entrySet() {
            return null;
        }

        // @see java.util.concurrent.ConcurrentMap#putIfAbsent(java.lang.Object, java.lang.Object)
        
        @Override
        public V putIfAbsent(K key, V value) {
            return null;
        }

        // @see java.util.concurrent.ConcurrentMap#remove(java.lang.Object, java.lang.Object)
        
        @Override
        public boolean remove(Object key, Object value) {
            return false;
        }

        // @see java.util.concurrent.ConcurrentMap#replace(java.lang.Object, java.lang.Object, java.lang.Object)
        
        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            return false;
        }

        // @see java.util.concurrent.ConcurrentMap#replace(java.lang.Object, java.lang.Object)
        
        @Override
        public V replace(K key, V value) {
            return null;
        }
        
    }

    // @see com.redprairie.moca.cache.CacheProvider#createCache(java.lang.String, java.util.Map, com.redprairie.moca.cache.CacheController)
    @Override
    public <K, V> ConcurrentMap<K, V> createCache(String name,
                                                  Map<String, String> params,
                                                  CacheController<K, V> controller) {
        return new TestCache<K, V>();
    }

}
