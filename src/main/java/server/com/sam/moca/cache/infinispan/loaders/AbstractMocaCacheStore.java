/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.sam.moca.cache.infinispan.loaders;

import org.infinispan.loaders.AbstractCacheStore;
import org.infinispan.loaders.CacheLoaderException;

import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.exec.ServerContext;

/**
 * A version of a AbstractCacheStore for products to implement where MOCA
 * handles the closing of the MocaContext after the startup logic.  This is 
 * because some products initialize a datastore after utilizing the MocaContext
 * inside of start().  Therefore to clean up, we need to clean up the context.
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
public abstract class AbstractMocaCacheStore  extends AbstractCacheStore{
    
    /***
     * The onStart method is where we want to handle any
     * logic we need to accomplish during the starting of the
     * CacheStore.  MOCA will clean up the MocaContext after this
     * method is called.
     * 
     */
    protected abstract void onStart();
    
    /***
     * The start method that infinispan calls during 
     * the start of the cache/cacheloader. We run
     * onStart and then try to clean up any open MocaContexts.
     */
    @Override
    public final void start() throws CacheLoaderException {
        super.start();
        onStart();
        ServerContext sc = ServerUtils.getCurrentContextNullable();
        if(sc != null){
            sc.close();
            ServerUtils.setCurrentContext(null);
        }
    }
}
