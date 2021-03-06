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

package com.sam.moca.cache.infinispan;

import java.util.concurrent.ConcurrentMap;

import com.sam.moca.cache.CacheController;

/**
 * A test controller that always returns null.
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
public class MocaNullTestController implements CacheController<String,String>{

    // @see com.sam.moca.cache.CacheController#loadEntry(java.lang.Object)
    
    @Override
    public String loadEntry(String key) {
        return null;
    }

    // @see com.sam.moca.cache.CacheController#loadAll(java.util.concurrent.ConcurrentMap)
    
    @Override
    public void loadAll(ConcurrentMap<String, String> cache) {
    }
    
}