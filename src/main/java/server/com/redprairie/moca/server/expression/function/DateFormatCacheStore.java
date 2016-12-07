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

package com.redprairie.moca.server.expression.function;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Set;

import org.infinispan.container.entries.ImmortalCacheEntry;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.loaders.AbstractCacheStore;
import org.infinispan.loaders.CacheLoaderConfig;
import org.infinispan.loaders.CacheLoaderException;

/**
 * A cache store that holds initial mappings of oracle date formats to
 * the proper java date formats.
 * 
 * Copyright (c) 2013 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
public class DateFormatCacheStore extends AbstractCacheStore{

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

    // @see org.infinispan.loaders.CacheLoader#load(java.lang.Object)
    
    @Override
    public InternalCacheEntry load(Object key) throws CacheLoaderException {
        String sdfFormat = _mapper.apply((String) key);
        if (sdfFormat != null) {
            try {
                return new ImmortalCacheEntry(key, new SimpleDateFormat(sdfFormat));
            }
            catch (IllegalArgumentException e) {
                // Ignore malformed input -- use other means
            }
        }
        
        return null;
    }

    // @see org.infinispan.loaders.CacheLoader#loadAll()
    
    @Override
    public Set<InternalCacheEntry> loadAll() throws CacheLoaderException {
        return Collections.emptySet();
    }

    // @see org.infinispan.loaders.CacheLoader#load(int)
    
    @Override
    public Set<InternalCacheEntry> load(int numEntries)
            throws CacheLoaderException {
        return Collections.emptySet();
    }

    // @see org.infinispan.loaders.CacheLoader#loadAllKeys(java.util.Set)
    
    @Override
    public Set<Object> loadAllKeys(Set<Object> keysToExclude)
            throws CacheLoaderException {
        return Collections.emptySet();
    }

    // @see org.infinispan.loaders.CacheLoader#getConfigurationClass()
    
    @Override
    public Class<? extends CacheLoaderConfig> getConfigurationClass() {
        return null;
    }

    // @see org.infinispan.loaders.AbstractCacheStore#purgeInternal()
    
    @Override
    protected void purgeInternal() throws CacheLoaderException {
        
    }

    private final static FormatMapper _mapper = new OracleDBDateFormatToJava();
}
