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

package com.redprairie.moca.cache.infinispan.loaders;

import org.infinispan.loaders.LockSupportCacheStoreConfig;

/**
 * This is the MocaCacheStore config for infinispan. This
 * defines the properties we take in for the config. We require
 * a legacy MOCA CacheController class to be passed in for proper
 * use.
 * 
 * Copyright (c) 2013 Sam Corporation All Rights Reserved
 * 
 * @author klehrke
 * @see com.redprairie.moca.cache.CacheController
 */
public class MocaCacheStoreConfig extends LockSupportCacheStoreConfig {

    private static final long serialVersionUID = -1179255809656114685L;
    private String _loaderClass;

    public MocaCacheStoreConfig() {
        setCacheLoaderClassName(MocaCacheStore.class.getName());
        _loaderClass = null;
    }

    public String getLoaderClass() {
        return _loaderClass;
    }

    public void setLoaderClass(String loaderClass) {
        _loaderClass = loaderClass;
    }
    
   
}
