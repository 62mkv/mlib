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

package com.redprairie.moca.cache.infinispan;

import java.util.concurrent.ConcurrentMap;

import com.redprairie.moca.cache.CacheController;

/**
 * A test controller that returns bar if given foo, otherwise 'blah'
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
public class MocaTestController implements CacheController<String,String>{

    // @see com.redprairie.moca.cache.CacheController#loadEntry(java.lang.Object)
    public String _key = "foo";
    public String _value = "bar";
    
    
    @Override
    public String loadEntry(String key) {
        return key.equals(_key) ? _value : "blah";
    }

    // @see com.redprairie.moca.cache.CacheController#loadAll(java.util.concurrent.ConcurrentMap)
    
    @Override
    public void loadAll(ConcurrentMap<String, String> cache) {
        cache.put(_key, "bar");
    }
    
}