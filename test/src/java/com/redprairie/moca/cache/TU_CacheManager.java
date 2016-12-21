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

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentMap;

import org.junit.Test;
import org.mockito.Mockito;

import com.sam.moca.MocaException;
import com.sam.moca.server.SystemConfigurationException;
import com.sam.moca.server.TestServerUtils;
import com.sam.moca.server.exec.SystemContext;
import com.sam.moca.server.registry.RegistryReader;
import com.sam.moca.util.AbstractMocaTestCase;

/**
 * Unit test for Cache Manager
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_CacheManager extends AbstractMocaTestCase{
    
    @Test
    public void testRegistryConfiguration() throws MocaException,
            UnsupportedEncodingException, SystemConfigurationException {
        SystemContext ctx = new RegistryReader(new InputStreamReader(
            TU_CacheManager.class.getResourceAsStream("registry"), "UTF-8"));
        SystemContext toRestore = TestServerUtils.getGlobalContext();
        TestServerUtils.overrideGlobalContext(ctx);
        try {
            // Cache manager uses the global context.
            ConcurrentMap<String, String> cache = CacheManager.getCache("foo", null);

            assertTrue(cache instanceof TestCacheProvider.TestCache<?, ?>);
        }
        finally {
            TestServerUtils.overrideGlobalContext(toRestore);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testProviderCalledCorrectly() {
        MocaCacheProvider provider = Mockito.mock(MocaCacheProvider.class);
        ConcurrentMap<?, ?> cache = Mockito.mock(ConcurrentMap.class);
        
        Mockito.when(
            provider.createCache(
                Mockito.anyString(), Mockito.anyMap(),
                (CacheController<String, String>) Mockito.any()))
            .thenReturn(cache);

        ConcurrentMap<?, ?> retrievedCache = CacheManager.getCache("provider", 
                provider, null, null);
        
        assertEquals("We should have gotten back the same object", cache, 
                retrievedCache);
    }
    
//    @SuppressWarnings("unchecked")
//    @Test
//    public void testControllerCalledCorrectly() {
//        CacheController<Object, Object> controller = Mockito.mock(CacheController.class);
//        // This will have the method do nothing the first time and then throw
//        // an assertion error any subsequent times when a loadAll is called.
//        Mockito.doNothing().when(controller).loadAll(Mockito.isA(ConcurrentMap.class));
//        
//        // Now we actually call the method we are testing
//        ConcurrentMap<Object, Object> cache = CacheManager.getCache("controller", null,
//                controller, null);
//        // We verify we were called only once with the cache we got back.
//        Mockito.verify(controller).loadAll(Mockito.eq(cache));
//    }
//    

//    private static class TestNullCacheController extends BaseCacheController<String,String> {
//        // @see com.sam.moca.cache.BaseCacheController#loadEntry(java.lang.Object)
//        
//        @Override
//        public String loadEntry(String key) {
//            loadCount++;
//            if (key.equals("foo")) {
//                return "bar";
//            }
//            else {
//                return null;
//            }
//        }
//        
//        private int loadCount = 0;
//    }

    
//    @Test
//    public void testCacheGetReturnsNull() {
//        TestNullCacheController controller = new TestNullCacheController();
//        
//        ConcurrentMap<String, String> cache = CacheManager.getCache("testCacheGetReturnsNull", controller);
//        assertEquals("bar", cache.get("foo"));
//        assertNull(cache.get("bar"));
//        assertEquals("bar", cache.get("foo"));
//        assertNull(cache.get("bar"));
//        // Since we get a null on cache.get("bar"), we don't put the 
//        // null into the map, since nulls aren't supported. 
//        // Therefore, when we check for bar again, it's not there, 
//        // so we check the controller again.
//        assertEquals(3, controller.loadCount);
//    }
}