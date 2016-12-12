/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.cluster.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.infinispan.notifications.cachelistener.event.Event.Type;
import org.infinispan.transaction.xa.GlobalTransaction;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.cluster.jgroups.JGroupsLockManager;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;

/**
 * Abstract class to share some common requirements for ClusterRoleManagers
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public abstract class TU_AbstractClusterRoleManager {

    protected AbstractClusterRoleManager _roleManager;

    protected CacheContainer _cacheContainer;
    
    protected Cache<Node, Set<RoleDefinition>> _roleMap;
    protected Cache<Node, Set<RoleDefinition>> _forcedRoleMap;
    protected Cache<RoleDefinition, Object> _singleForcedRoles;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUpCaches() {
        _cacheContainer = Mockito.mock(CacheContainer.class);
        _roleMap = Mockito.mock(Cache.class, Mockito.withSettings().name("roleMap"));
        Mockito.doReturn(_roleMap).when(_cacheContainer).getCache("moca-current-roles");
        _forcedRoleMap = Mockito.mock(Cache.class, Mockito.withSettings().name("forcedRoleMap"));
        Mockito.doReturn(_forcedRoleMap).when(_cacheContainer).getCache("moca-forced-roles");
        _singleForcedRoles = Mockito.mock(Cache.class, Mockito.withSettings().name("singleForcedRoles"));
        Mockito.doReturn(_singleForcedRoles).when(_cacheContainer).getCache("moca-single-forced-roles");
    }
    
    protected void configureCachesForManager(
        AbstractClusterRoleManager manager) {
        // Unfortunately due to generics we had to use doReturn instead of when
        configureMockitoCache(_roleMap, manager);
        configureMockitoCache(_forcedRoleMap, manager);
        configureMockitoCache(_singleForcedRoles, manager);
    }
    
    private static class OurCacheEntryEvent<K, V> implements CacheEntryModifiedEvent<K, V>,
            CacheEntryRemovedEvent<K, V> {
        public OurCacheEntryEvent(K key, V value, boolean pre, 
            boolean local, Cache<K, V> mock, Type type) {
            _key = key;
            _value = value;
            _pre = pre;
            _local = local;
            _mock = mock;
            _type = type;
        }
        
        // @see java.lang.Object#toString()
        @Override
        public String toString() {
            return "OurCacheEntryEvent{" +
                    "pre=" + _pre +
                    ", key=" + _key +
                    ", originLocal=" + _local +
                    ", type=" + _type +
                    ", cache=" + _mock +
                    ", value=" + _value +
                    '}';
        }
        
        private final K _key;
        private final V _value;
        private final boolean _pre;
        private final boolean _local;
        private final Cache<K, V> _mock;
        private final Type _type;
        
        // @see org.infinispan.notifications.cachelistener.event.CacheEntryEvent#getKey()
        @Override
        public K getKey() {
            return _key;
        }
        // @see org.infinispan.notifications.cachelistener.event.TransactionalEvent#getGlobalTransaction()
        @Override
        public GlobalTransaction getGlobalTransaction() {
            return null;
        }
        // @see org.infinispan.notifications.cachelistener.event.TransactionalEvent#isOriginLocal()
        @Override
        public boolean isOriginLocal() {
            return _local;
        }
        // @see org.infinispan.notifications.cachelistener.event.Event#getType()
        @Override
        public Type getType() {
            return _type;
        }
        // @see org.infinispan.notifications.cachelistener.event.Event#isPre()
        @Override
        public boolean isPre() {
            return _pre;
        }
        // @see org.infinispan.notifications.cachelistener.event.Event#getCache()
        @Override
        public Cache<K, V> getCache() {
            return _mock;
        }
        // @see org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent#getValue()
        @Override
        public V getValue() {
            return _value;
        }
    }
    
    protected final AtomicBoolean _localModification = new AtomicBoolean(true);
    
    @SuppressWarnings("unchecked")
    private <K, V>void configureMockitoCache(final Cache<K, V> cache,
        final AbstractClusterRoleManager manager) {
        final ConcurrentMap<K, V> concurrentMap = new ConcurrentHashMap<K, V>();
        Mockito.when(cache.put((K)Mockito.any(), (V)Mockito.any())).thenAnswer(new Answer<V>() {
            @Override
            public V answer(final InvocationOnMock invocation) throws Throwable {
                boolean local = _localModification.get();
                manager.onModification(new OurCacheEntryEvent<K, V>(
                    (K)invocation.getArguments()[0], concurrentMap.get(
                        invocation.getArguments()[0]),
                    true, local, cache, Type.CACHE_ENTRY_MODIFIED));
                try {
                    return concurrentMap.put((K)invocation.getArguments()[0], 
                        (V)invocation.getArguments()[1]);
                }
                finally {
                    manager.onModification(new OurCacheEntryEvent<K, V>(
                            (K)invocation.getArguments()[0], (V)invocation.getArguments()[1],
                            false, local, cache, Type.CACHE_ENTRY_MODIFIED));
                }
            }
        });
        Mockito.when(cache.replace((K)Mockito.any(), (V)Mockito.any(), 
            (V)Mockito.any())).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(final InvocationOnMock invocation) throws Throwable {
                K key = (K)invocation.getArguments()[0];
                V newValue = (V)invocation.getArguments()[2];
                V old = concurrentMap.get(key);
                boolean replaced = concurrentMap.replace(key, 
                    (V)invocation.getArguments()[1], newValue);
                if (replaced) {
                    boolean local = _localModification.get();
                    // TODO: need to check if this is true or not or if it always
                    // sends the pre even if it fails..
                    manager.onModification(new OurCacheEntryEvent<K, V>(
                        key, old,
                        true, local, cache, Type.CACHE_ENTRY_MODIFIED));
                    manager.onModification(new OurCacheEntryEvent<K, V>(
                            key, newValue,
                            false, local, cache, Type.CACHE_ENTRY_MODIFIED));
                }
                return replaced;
            }
            
        });
        Mockito.when(cache.containsKey(Mockito.any())).thenAnswer(new Answer<Boolean>() {
            // @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return concurrentMap.containsKey(invocation.getArguments()[0]);
            }
        });
        Mockito.when(cache.remove(Mockito.any())).thenAnswer(new Answer<V>() {
           // @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
            @Override
            public V answer(InvocationOnMock invocation) throws Throwable {
                // We treat all removes as non local, since it doesn't really 
                // matter which it is
                manager.onRemoval(new OurCacheEntryEvent<K, V>(
                        (K)invocation.getArguments()[0], concurrentMap.get(
                            invocation.getArguments()[0]),
                        true, false, cache, Type.CACHE_ENTRY_REMOVED));
                try {
                    return concurrentMap.remove(invocation.getArguments()[0]);
                }
                finally {
                    manager.onRemoval(new OurCacheEntryEvent<K, V>(
                            (K)invocation.getArguments()[0], null,
                            false, false, cache, Type.CACHE_ENTRY_REMOVED));
                }
            }
        });
        Mockito.when(cache.values()).thenAnswer(new Answer<Collection<V>>() {
            // @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
            @Override
            public Collection<V> answer(InvocationOnMock invocation)
                    throws Throwable {
                return concurrentMap.values();
            }
        });
        Mockito.when(cache.get(Mockito.any())).thenAnswer(new Answer<V>() {
            // @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
            @Override
            public V answer(InvocationOnMock invocation) throws Throwable {
                return concurrentMap.get(invocation.getArguments()[0]);
            }
        });
        Mockito.when(cache.size()).thenAnswer(new Answer<Integer>() {
            // @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return concurrentMap.size();
            }
        });
        Mockito.when(cache.keySet()).thenAnswer(new Answer<Set<K>>() {
            // @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
            @Override
            public Set<K> answer(InvocationOnMock invocation) throws Throwable {
                return concurrentMap.keySet();
            }
        });
        Mockito.when(cache.entrySet()).thenAnswer(new Answer<Set<Entry<K, V>>>() {
            // @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
            @Override
            public Set<Entry<K, V>> answer(InvocationOnMock invocation) throws Throwable {
                return concurrentMap.entrySet();
            }
        });
        
        Mockito.when(cache.putIfAbsent((K)Mockito.any(), (V)Mockito.any())).thenAnswer(new Answer<V>() {
            // @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
            @Override
            public V answer(InvocationOnMock invocation) throws Throwable {
                K key = (K)invocation.getArguments()[0];
                V newValue = (V)invocation.getArguments()[1];
                V oldValue = concurrentMap.putIfAbsent(key, newValue);
                boolean local = _localModification.get();
                    // TODO: need to check if this is true or not or if it always
                    // sends the pre even if it fails..
                manager.onModification(new OurCacheEntryEvent<K, V>(key, 
                        oldValue, true, local, cache, 
                        Type.CACHE_ENTRY_MODIFIED));
                manager.onModification(new OurCacheEntryEvent<K, V>(key, 
                        newValue, false, local, cache, 
                        Type.CACHE_ENTRY_MODIFIED));
                
                return oldValue;
            }
        });
        Mockito.doAnswer(new Answer<Void>() {
            // @see
            // org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                for (Entry<K, V> entry : concurrentMap.entrySet()) {
                    boolean local = _localModification.get();
                    manager.onRemoval(new OurCacheEntryEvent<K, V>(entry
                        .getKey(), entry.getValue(), true, local, cache,
                        Type.CACHE_ENTRY_REMOVED));
                    manager.onRemoval(new OurCacheEntryEvent<K, V>(entry
                        .getKey(), null, false, local, cache,
                        Type.CACHE_ENTRY_REMOVED));
                }
                concurrentMap.clear();
                return null;
            }
        }).when(cache).clear();
    }
    
    @After
    public void stopRunner() {
        if (_roleManager != null) {
            _roleManager.stop();
            _roleManager = null;
        }
    }
    
    protected static final RoleDefinition definition1 = Mockito.mock(RoleDefinition.class, 
        Mockito.withSettings().serializable().name("role1"));
    protected static final RoleDefinition definition2 = Mockito.mock(RoleDefinition.class, 
        Mockito.withSettings().serializable().name("role2"));
    protected static final RoleDefinition definition3 = Mockito.mock(RoleDefinition.class, 
        Mockito.withSettings().serializable().name("role3"));
    protected static final RoleDefinition definition4 = Mockito.mock(RoleDefinition.class, 
        Mockito.withSettings().serializable().name("role4"));
    
    protected JGroupsLockManager getLockManager() {
        return _lockManager;
    }
    
    @BeforeClass
    public static void setupTest() throws SystemConfigurationException {
        ServerUtils.setupDaemonContext(TU_AbstractClusterRoleManager.class.getName(), true);
    }
    
    @AfterClass
    public static void cleanLockManager() {
        synchronized (_map) {
            _map.clear();
        }
    }
    
    private static final JGroupsLockManager _lockManager;
    private static final Map<RoleDefinition, Lock> _map = 
        new HashMap<RoleDefinition, Lock>();
    
    static {
        _lockManager = Mockito.mock(JGroupsLockManager.class);
        Mockito.when(_lockManager.getClusterMergeLock()).thenReturn(
            new ReentrantLock());
        Mockito.when(_lockManager.getLock(Mockito.any(
            RoleDefinition.class))).thenAnswer(new Answer<Lock>() {

                @Override
                public Lock answer(InvocationOnMock invocation)
                        throws Throwable {
                    RoleDefinition arg = (RoleDefinition)invocation.getArguments()[0];
                    synchronized (_map) {
                        Lock value = _map.get(arg);
                        
                        if (value == null) {
                            value = new ReentrantLock();
                            _map.put(arg, value);
                        }
                        
                        return value;
                    }
                }
            });
    }
}
