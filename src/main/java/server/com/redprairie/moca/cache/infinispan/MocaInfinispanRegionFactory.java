/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

import java.util.Properties;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.infinispan.InfinispanRegionFactory;
import org.hibernate.cfg.Settings;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.transaction.lookup.TransactionManagerLookup;

import com.redprairie.moca.server.ServerUtils;

/**
 * This class is a simple lookup to plug into our infinispan cache provider
 * so that hibernate uses the same cache that we have configured.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MocaInfinispanRegionFactory extends InfinispanRegionFactory {
    private static final long serialVersionUID = -6687377364766314791L;

    // @see org.hibernate.cache.infinispan.InfinispanRegionFactory#createCacheManager(java.util.Properties)
    @Override
    protected EmbeddedCacheManager createCacheManager(Properties properties)
            throws CacheException {
        return InfinispanCacheProvider.getInfinispanCacheManager(
            ServerUtils.globalContext());
    }
    
    // @see com.redprairie.moca.cache.infinispan.InfinispanRegionFactory#createTransactionManagerLookup(org.hibernate.cfg.Settings, java.util.Properties)
    @Override
    protected TransactionManagerLookup createTransactionManagerLookup(
        Settings settings, Properties properties) {
        return new MocaTransactionManagerLookup();
    }
}
