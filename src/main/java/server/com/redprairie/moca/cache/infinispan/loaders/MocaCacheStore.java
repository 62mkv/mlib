/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.moca.cache.infinispan.loaders;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.container.entries.ImmortalCacheEntry;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.loaders.CacheLoaderConfig;
import org.infinispan.loaders.CacheLoaderException;
import org.infinispan.loaders.CacheLoaderMetadata;
import org.infinispan.marshall.StreamingMarshaller;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.redprairie.moca.cache.CacheController;
import com.redprairie.util.ClassUtils;

/**
 * Moca's CacheController to MocaCacheLoader class.
 * 
 * Copyright (c) 2013 RedPrairie Corporation All Rights Reserved
 * 
 * @author klehrke
 */
@CacheLoaderMetadata(configurationClass = MocaCacheStoreConfig.class)
public class MocaCacheStore<K, V> extends AbstractMocaCacheStore {

    private CacheController<K, V> _controller;
    private String _controllerClass;
    private static Logger _log = Logger.getLogger(MocaCacheStoreConfig.class);
    
    //TODO: Do I still need this after init?
    private MocaCacheStoreConfig _config;

    public MocaCacheStore() {
    }

    

    // @see org.infinispan.loaders.AbstractCacheLoader#init(org.infinispan.loaders.CacheLoaderConfig, org.infinispan.Cache, org.infinispan.marshall.StreamingMarshaller)
    
    @Override
    public void init(CacheLoaderConfig config, Cache<?, ?> cache,
                     StreamingMarshaller m) throws CacheLoaderException {
        super.init(config, cache, m);
        
        //I don't like checking instanceof of a implementation class,
        // but findbugs was complaining about my unchecked cast.
        if (config instanceof MocaCacheStoreConfig) {
            _config = (MocaCacheStoreConfig) config;
        }
        else {
            throw new CacheLoaderException(
                "Incorrect CacheLoaderConfig type.");
        }
        _controllerClass = _config.getLoaderClass();
    }

    
    // @see org.infinispan.loaders.CacheLoader#load(java.lang.Object)
    @SuppressWarnings("unchecked")
    @Override
    public InternalCacheEntry load(Object key) throws CacheLoaderException {
        V value = _controller.loadEntry((K) key);
        if (value == null) {
            _log.debug("Recieved null from provider: " + _controllerClass
                    + " with key: " + key);
            return null;
        }
        ImmortalCacheEntry ice = new ImmortalCacheEntry(key,
            value);
        _log.debug("Recieved value: " + value + " from provider: "
                + _controllerClass + " with key: " + key);
        return ice;
    }

    // @see org.infinispan.loaders.CacheLoader#loadAll()

    @Override
    public Set<InternalCacheEntry> loadAll() throws CacheLoaderException {
        ConcurrentMap<K, V> myMap = new ConcurrentHashMap<K, V>();
        _controller.loadAll(myMap);
        Set<InternalCacheEntry> ices = new HashSet<InternalCacheEntry>();
        for (Entry<K, V> entry : myMap.entrySet()) {
            ices.add(new ImmortalCacheEntry(entry.getKey(),
                entry.getValue()));
        }
        return ices;
    }

    // @see org.infinispan.loaders.CacheLoader#load(int)

    @Override
    public Set<InternalCacheEntry> load(int numEntries)
            throws CacheLoaderException {
        Set<InternalCacheEntry> ices = loadAll();
        Set<InternalCacheEntry> returnIces = ImmutableSet.copyOf(Iterables
            .limit(ices, numEntries));
        return returnIces;
    }

    // @see org.infinispan.loaders.CacheLoader#loadAllKeys(java.util.Set)

    @Override
    public Set<Object> loadAllKeys(Set<Object> keysToExclude)
            throws CacheLoaderException {
        return Collections.emptySet();
    }

    // @see org.infinispan.loaders.CacheLoader#stop()

    @Override
    public void stop() throws CacheLoaderException {
        super.stop();
    }

    // @see org.infinispan.loaders.CacheLoader#getConfigurationClass()

    @Override
    public Class<? extends CacheLoaderConfig> getConfigurationClass() {
       return MocaCacheStoreConfig.class;
    }



    // @see org.infinispan.loaders.CacheStore#store(org.infinispan.container.entries.InternalCacheEntry)
    
    @Override
    public void store(InternalCacheEntry entry) throws CacheLoaderException {
        
    }



    // @see org.infinispan.loaders.CacheStore#fromStream(java.io.ObjectInput)
    
    @Override
    public void fromStream(ObjectInput inputStream) throws CacheLoaderException {
        
    }



    // @see org.infinispan.loaders.CacheStore#toStream(java.io.ObjectOutput)
    
    @Override
    public void toStream(ObjectOutput outputStream) throws CacheLoaderException {
        
    }



    // @see org.infinispan.loaders.CacheStore#clear()
    
    @Override
    public void clear() throws CacheLoaderException {
        
    }



    // @see org.infinispan.loaders.CacheStore#remove(java.lang.Object)
    
    @Override
    public boolean remove(Object key) throws CacheLoaderException {
        return false;
    }



    // @see org.infinispan.loaders.AbstractCacheStore#purgeInternal()
    
    @Override
    protected void purgeInternal() throws CacheLoaderException {
        
    }



    // @see com.redprairie.moca.cache.infinispan.loaders.AbstractMocaCacheStore#onStart()
    
    @SuppressWarnings("unchecked")
    @Override
    protected void onStart() {
        _controller = ClassUtils.instantiateClass(_controllerClass,
            CacheController.class);  
    }

}
