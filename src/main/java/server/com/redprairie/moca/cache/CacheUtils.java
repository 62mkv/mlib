/*
 *  $URL$
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

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.server.ServerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.CacheException;
import org.infinispan.remoting.RemoteException;
import org.infinispan.remoting.transport.jgroups.SuspectException;
import org.infinispan.util.concurrent.TimeoutException;

/**
 * Cache and Map write methods (such as put and remove) can  produce various
 * Exceptions in Infinispan after network disruption or when a node leaves the
 * cluster or when taking concurrent actions on the same cache/map on the same key
 * which could potentially produce a deadlock due to a local lock occurring on each node
 * then a timeout occurring during the RPC replication due to each node trying to replicate
 * to the others but each holds the local lock already (deadlock).
 * <p>
 * This class is a utility class that allows for retries using the MOCA registry
 * key cache.remote-retry-limit with a default limit of 5 retries. We have
 * observed that only 1 retry is needed per cache/map per write operation.
 * <p>
 * Note, when do not succeed after the retrying, we log log a fatal error, given
 * caches are likely not in sync across all nodes as well as throwing the
 * exception encountered.
 * 
 * Copyright (c) 2016 Sam Corporation All Rights Reserved
 * 
 * @author j1014540
 */
public class CacheUtils {
    
    private final static Logger _logger = LogManager.getLogger(CacheUtils.class);
    
    // The amount of times to retry the operation before the failure exception is rethrown
    private final static int RETRY_LIMIT;
    
    // 100 ms delay factor, delay = delay factory * (node_id + 1), used when retrying an operation to sleep the thread
    // in an effort to stagger operations between nodes.
    private final static int DELAY_FACTOR = 100; 

    static {
        // initialize retry limit
        int lim = Integer.parseInt(MocaRegistry.REGKEY_CLUSTER_REMOTE_RETRY_LIMIT_DEFAULT);
        String configuredLimit = ServerUtils.globalContext().getConfigurationElement(
            MocaRegistry.REGKEY_CLUSTER_REMOTE_RETRY_LIMIT);
        if (configuredLimit != null) {
            try {
                int tmp = Integer.parseInt(configuredLimit);
                if (tmp >= 0) {
                    lim = tmp;
                }
                else {
                    _logger.error("Configuration provided for cluster.remote-retry-limit was less than 0, provided value was: {}", tmp);
                }
            }
            catch (NumberFormatException nfe) {
                _logger.error("Configuration provided for cluster.remote-retry-limit was not a numeric number, provided value was: {}", configuredLimit);
            }
        }
        
        RETRY_LIMIT = lim;
    }

    /**
     * ConcurrentMap.replace() with retry given cache.remote-retry-limit
     * 
     * @param map The map
     * @param key The key to replace
     * @param oldValue The old value
     * @param newValue The new value
     * @param name The name of the map for diagnostics purposes
     * @param nodeId The node ID used to handle staggering of retries
     * @return Whether the value was replaced or not matching the old value
     */
    public static <K, V> boolean mapReplace(ConcurrentMap<K, V> map, K key,
                                            V oldValue, V newValue,
                                            String name, int nodeId) {
        boolean ret = false;
        int attempts = 0;
        do { 
            try {
                ret = map.replace(key, oldValue, newValue);
                break;
            }
            catch (TimeoutException e) {
                handleFailureForMap(name, "replace", key, attempts, nodeId, e);
            }
            catch (RemoteException e) {
                handleFailureForMap(name, "replace", key, attempts, nodeId, e);
            }
            catch (SuspectException e) {
                handleFailureForMap(name, "replace", key, attempts, nodeId, e);
            }
            attempts++;
        } while (attempts <= RETRY_LIMIT);
        
        return ret;
    }

    /**
     * Map.remove() with retry given cache.remote-retry-limit
     * 
     * @param map The map
     * @param key The key to remove
     * @param name The name of the map for diagnostics purposes
     * @param nodeId The node ID used for staggering of retries
     * @return The removed value
     */
    public static <K, V> V mapRemove(Map<K, V> map, K key, String name,
                                     int nodeId) {
        V ret = null;
        int attempts = 0;
        do { 
            try {
                ret = map.remove(key);
                break;
            }
            catch (TimeoutException e) {
                handleFailureForMap(name, "remove", key, attempts, nodeId, e);
            }
            catch (RemoteException e) {
                handleFailureForMap(name, "remove", key, attempts, nodeId, e);
            }
            catch (SuspectException e) {
                handleFailureForMap(name, "remove", key, attempts, nodeId, e);
            }
            attempts++;
        } while (attempts <= RETRY_LIMIT);
        
        return ret;
    }

    /**
     * Cache.put() with retry given cache.remote-retry-limit
     * 
     * @param cache The cache to put into
     * @param key The key to put
     * @param value The value to put
     * @param nodeId The node ID for handling staggering of retries
     * @return The previous value for the key or null if not present
     */
    public static <K, V> V cachePut(Cache<K, V> cache, K key, V value,
                                    int nodeId) {
        V ret = null;
        int attempts = 0;
        do { 
            try {
                ret = cache.put(key, value);
                break;
            }
            catch (TimeoutException e) {
                handleFailureForCache(cache, "put", key, attempts, nodeId, e);
            }
            catch (RemoteException e) {
                handleFailureForCache(cache, "put", key, attempts, nodeId, e);
            }
            catch (SuspectException e) {
                handleFailureForCache(cache, "put", key, attempts, nodeId, e);
            }
            attempts++;
        } while (attempts <= RETRY_LIMIT);
        
        return ret;
    }
    
    /**
     * Cache.putAll() with retry given cache.remote-retry-limit
     * 
     * @param cache The cache to put into
     * @param nodeId The node ID for handling staggering of retries
     * @return The previous value for the key or null if not present
     */
    public static <K, V> void cachePutAll(Cache<K, V> cache, Map<K, V> collection,
                                    int nodeId) {
        int attempts = 0;
        do { 
            try {
                cache.putAll(collection);
                break;
            }
            catch (TimeoutException e) {
                handleFailureForCacheGeneric(cache, "putAll", attempts, nodeId, e);
            }
            catch (RemoteException e) {
                handleFailureForCacheGeneric(cache, "putAll", attempts, nodeId, e);
            }
            catch (SuspectException e) {
                handleFailureForCacheGeneric(cache, "putAll", attempts, nodeId, e);
            }
            attempts++;
        } while (attempts <= RETRY_LIMIT);
    }

    /**
     * ConcurrentMap.putIfAbsent() with retry given cache.remote-retry-limit
     * 
     * @param map The map to put into
     * @param key The key to put
     * @param value The value to put
     * @param name The name of the map for diagnostics purposes
     * @param nodeId The node ID for handling staggering of retries
     * @return The previous value associated with key, or null if there was no mapping for key
     */
    public static <K, V> V mapPutIfAbsent(ConcurrentMap<K, V> map, K key,
                                          V value, String name, int nodeId) {
        V ret = null;
        int attempts = 0;
        do { 
            try {
                ret = map.putIfAbsent(key, value);
                break;
            }
            catch (TimeoutException e) {
                handleFailureForMap(name, "putIfAbsent", key, attempts, nodeId, e);
            }
            catch (RemoteException e) {
                handleFailureForMap(name, "putIfAbsent", key, attempts, nodeId, e);
            }
            catch (SuspectException e) {
                handleFailureForMap(name, "putIfAbsent", key, attempts, nodeId, e);
            }
            attempts++;
        } while (attempts <= RETRY_LIMIT);
        
        return ret;
    }

    /**
     * Map.put() with retry given cache.remote-retry-limit
     * 
     * @param map The map to put into
     * @param key The key to put
     * @param value The value to put
     * @param name The name of the map for diagnostics purposes
     * @param nodeId The node ID for handling staggering of retries
     * @return The previous value associated with key, or null if there was no mapping for key
     */
    public static <K, V> V mapPut(Map<K, V> map, K key, V value, String name,
                                  int nodeId) {
        V ret = null;
        int attempts = 0;
        do { 
            try {
                ret = map.put(key, value);
                break;
            }
            catch (TimeoutException e) {
                handleFailureForMap(name, "put", key, attempts, nodeId, e);
            }
            catch (RemoteException e) {
                handleFailureForMap(name, "put", key, attempts, nodeId, e);
            }
            catch (SuspectException e) {
                handleFailureForMap(name, "put", key, attempts, nodeId, e);
            }
            attempts++;
        } while (attempts <= RETRY_LIMIT);
        
        return ret;
    }

    /**
     * Cache.remove() with retry given cache.remote-retry-limit
     * 
     * @param cache The cache to remove from
     * @param key The key to remove
     * @param nodeId The node ID for handling staggering of retries
     * @return The removed value
     */
    public static <K, V> V cacheRemove(Cache<K, V> cache, K key, int nodeId) {
        V ret = null;
        int attempts = 0;
        do { 
            try {
                ret = cache.remove(key);
                break;
            }
            catch (TimeoutException e) {
                handleFailureForCache(cache, "remove", key, attempts, nodeId, e);
            }
            catch (RemoteException e) {
                handleFailureForCache(cache, "remove", key, attempts, nodeId, e);
            }
            catch (SuspectException e) {
                handleFailureForCache(cache, "remove", key, attempts, nodeId, e);
            }
            attempts++;
        } while (attempts <= RETRY_LIMIT);

        return ret;
    }

    /**
     * Generic operation, presumably on a cache, with retry given cache.remote-retry-limit.
     * Will retry if a CacheException is encountered. All other exceptions are rethrown without retrying.
     * @param name description of entity on which operation was performed
     * @param operation description of what the callable is doing
     * @param callable implemented operation
     * @return callable return value
     * @throws throws runtime exception if encountered and it is not a CacheException
     */
    public static <T> T retryGenericOperation(String name, String operation, CacheCallable<T> callable)
            throws RuntimeException {
        T ret = null;
        int attempts = 0;
        do {
            try {
                ret = callable.call();
                break;
            }
            catch (CacheException e) {
                handleFailureGeneric(name, operation, attempts, 0, e);
            }
            catch (RuntimeException r) {
                // all other runtime exceptions we need to explode
                _logger.error("Unexpected exception while retrying generic cache operation [" + operation +
                    "] on entity [" + name + "] on attempt " + (attempts + 1), r);
                throw r;
            }
            attempts++;
        } while (attempts <= RETRY_LIMIT);

        return ret;
    }
    
    public static int getRetryLimit() {
        return RETRY_LIMIT;
    }
    
    public static int getDelayFactor() {
        return DELAY_FACTOR;
    }
    
    /**
     * Gets the stagger/delay to use for the node when sleeping between attempts (delay_factor * (nodeId + 1))
     * @param nodeId The nodeId for this current node to factor in the stagger logic
     * @return The delay to sleep
     */
    static long getDelay(long nodeId) {
        return DELAY_FACTOR * (nodeId + 1);
    }
    
    private static <K, V> void handleFailureForCache(Cache<K, V> cache, String operation, K key, int attempts, int nodeId, CacheException ex) {
        handleFailure(cache.getName() + " (cache)", operation, key, attempts, nodeId, ex);
    }
    
    private static <K, V> void handleFailureForMap(String mapName, String operation, K key, int attempts, int nodeId, CacheException ex) {
        handleFailure(mapName + " (map)", operation, key, attempts, nodeId, ex);
    }
    
    private static <K, V> void handleFailureForCacheGeneric(Cache<K, V> cache, String operation, int attempts, int nodeId, CacheException ex) {
        handleFailureGeneric(cache.getName() + "(cache)", operation, attempts, nodeId, ex);
    }
    
    // Handles logging of failed cache/map operations and will sleep inside this method to stagger the retry
    private static <K, V> void handleFailure(String entityName, String operation, K key, int attempts, int nodeId, CacheException ex) {
        // If the number of retries has hit the limit then log it and rethrow the exception
        if (attempts == RETRY_LIMIT) {
            _logger.fatal("After {} retries the operation [{}] failed on entity [{}] with key [{}], shutdown and restart is recommended",
                    attempts, operation, entityName, key, ex);
            throw ex;
        }
        else {
            // Retry stagger logic is here, stagger the sleep based on node ID and hand back
            // to the caller where they can call the method again
            try {
                long retryTime = getDelay(nodeId);
                _logger.info("Cache exception occurred on the operation [{}] on entity [{}] with key [{}], retrying in {} ms, {} tries remaining. Exception message: {}",
                        operation, entityName, key, retryTime, RETRY_LIMIT - attempts, ex.getMessage());
                Thread.sleep(retryTime);
            }
            catch (InterruptedException ie) {
                _logger.warn("Interrupted while waiting to retry operation [{}] on entity [{}] with key [{}]",
                        operation, entityName, key);
                Thread.currentThread().interrupt();
                throw new MocaInterruptedException(ie);
            }
        }
    }
    
    // don't have a key to report on
    private static <K, V> void handleFailureGeneric(String entityName, String operation, int attempts, int nodeId, CacheException ex) {
        // If the number of retries has hit the limit then log it and rethrow the exception
        if (attempts >= RETRY_LIMIT) {
            _logger.fatal("After {} retries the operation [{}] failed on entity [{}], shutdown and restart is recommended",
                    attempts, operation, entityName, ex);
            throw ex;
        }
        else {
            // Retry stagger logic is here, stagger the sleep based on node ID and hand back
            // to the caller where they can call the method again
            try {
                long retryTime = getDelay(nodeId);
                _logger.info("Cache exception occurred on the operation [{}] on entity [{}], retrying in {} ms, {} tries remaining. Exception message: {}",
                        operation, entityName, retryTime, RETRY_LIMIT - attempts, ex.getMessage());
                Thread.sleep(retryTime);
            }
            catch (InterruptedException ie) {
                _logger.warn("Interrupted while waiting to retry operation [{}] on entity [{}]",
                        operation, entityName);
                Thread.currentThread().interrupt();
                throw new MocaInterruptedException(ie);
            }
        }
    }  
    
    private CacheUtils() {}

    // weewooweewoo hack alert until java 8
    /**
     * Interface that returns a value without any declared exceptions.
     * This callable can still throw (and probably will) runtime exceptions.
     * @param <Z> return type
     */
    public interface CacheCallable<Z> {
        Z call();
    }

}