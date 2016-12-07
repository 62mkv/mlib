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

package com.redprairie.moca.cluster;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.cache.CacheUtils;
import com.redprairie.moca.cluster.infinispan.GlobalListener;

/**
 * This abstract type denotes that the extending type uses a cluster cache. Each
 * extending type will have a ConcurrentMap Cache which is keyed with a Node in
 * the cluster. The Values of the ConcurrentMap are Collections of the tenplated
 * type. This type takes care of properly adding and removing entries to the
 * Cluster Cache. Note that these caches are keyed on Node so they must be
 * notified when the cluster topology changes (e.g. a node is removed). To
 * facilitate this this class implements {@link MocaClusterMembershipListener}
 * and must be registered with the {@link GlobalListener} to receive callbacks
 * on topology changes.
 * 
 * Copyright (c) 2013 RedPrairie Corporation All Rights Reserved
 * 
 * @author eknapp
 */
public abstract class ClusterCachable<K> implements
        MocaClusterMembershipListener {

    public ClusterCachable(ConcurrentMap<Node, Set<K>> clusterCache,
            Node localNode, String name) {
        _clusterCache = clusterCache != null ? clusterCache
                : new ConcurrentHashMap<Node, Set<K>>();
        _localNode = localNode != null ? localNode : new BasicNode();
        _name = name;
    }

    @Override
    public void notifyMembership(Node local, List<Node> members,
                                 List<Node> joiners, List<Node> leavers,
                                 Boolean isMergeView) {
        // The leader should remove the nodes that left from the cache
        _nodeId = members.indexOf(local);
        if (_nodeId == 0) {
            _logger
                .debug("Leader of the cluster handling removal of removed nodes from the cache");
            for (Node leaver : leavers) {
                _logger.debug("Removing node [{}] from the cluster cache",
                    leaver);
                synchronized (_lock) {
                    CacheUtils.mapRemove(_clusterCache, leaver, _name, _nodeId);
                }
            }
        }
    }

    /**
     * This method is used to add a value of type K to the Cluster Cache. The
     * Cache and the Node used are defaulted to the local Cache for the CLuster
     * and the Local Server Node.
     * 
     * @param value
     */
    protected void addToClusterCache(K value) {
        addToClusterCache(_localNode, value, _clusterCache, _name, _nodeId);
    }

    /**
     * This is the main method used to add a value of type K to the Cache Map
     * for a given Node. This method is setup to handle various nodes
     * concurrently trying to change the Cache. That is the reason the method
     * tries to add the vale to Cache until it is a confirmed success.
     * 
     * @param node
     * @param value
     * @param map
     * @param name
     * @param nodeId
     */
    private void addToClusterCache(Node node, K value,
                                   ConcurrentMap<Node, Set<K>> map,
                                   String name, int nodeId) {
        synchronized (_lock) {
            boolean success = false;

            while (!success) {
                Set<K> prevCollection = map.get(node);
                Set<K> collection;
                // We do a copy on write operation so that the sets are thread
                // safe
                if (prevCollection == null) {
                    collection = new HashSet<K>();
                    collection.add(value);
                    // merge may have to be handled here in the future
                    success = CacheUtils.mapPutIfAbsent(map, node, collection,
                        name, nodeId) == null;
                }
                else if (!prevCollection.contains(value)) {
                    collection = new HashSet<K>(prevCollection);
                    collection.add(value);
                    // merge may have to be handled here in the future
                    success = CacheUtils.mapReplace(map, node, prevCollection,
                        collection, name, nodeId);
                }
                else {
                    // If it was already in the collection we don't need to
                    // update anything
                    success = true;
                }
            }
        }
    }

    protected boolean removeFromClusterCache(K value) {
        return removeFromClusterCache(_localNode, value, _clusterCache, _name,
            _nodeId);
    }

    /**
     * This method is used to remove entries from the Clustered Cache. Given a
     * Node, a value, and a Cache this method will remove the value for the
     * given node in the Cache.
     * 
     * @param node
     * @param value
     * @param map
     * @param name
     * @param nodeId
     * @return
     */
    private boolean removeFromClusterCache(Node node, K value,
                                           ConcurrentMap<Node, Set<K>> map,
                                           String name, int nodeId) {
        boolean removed = false;
        synchronized (_lock) {
            boolean success = false;

            while (!success) {
                Set<K> oldValues = map.get(node);
                // We do a copy on write operation so that the sets are thread
                // safe
                if (oldValues != null && oldValues.contains(value)) {
                    Set<K> newValues = new HashSet<K>(oldValues);
                    // This should be true since the contains check above
                    removed = newValues.remove(value);
                    // merge may have to be handled here in the future
                    success = CacheUtils.mapReplace(map, node, oldValues,
                        newValues, name, nodeId);
                    if (success) {
                        removed = true;
                    }
                }
                // If the set no longer contains it we can't remove it anymore
                else {
                    success = true;
                }
            }
        }
        return removed;
    }

    protected ConcurrentMap<Node, Set<K>> getClusterCache() {
        return _clusterCache;
    }

    /**
     * Important, replicate is a get and put but exists to force replication
     * over cluster when recovering from split brain.
     */
    protected void replicate() {
        synchronized (_lock) {
            /*
             * Important, we would like to use replace here to perform this
             * operation atomically, however; in split brain, the distribution
             * of the caches is checked for replace to work correctly.
             * 
             * Meaning it's likely that _clusterCache.containsKey(_localNode)
             * fails on some other node. Since there is no technique to mend
             * caches, instead we have to use a common lock for all write
             * operations.
             */
            if (_localNode != null && _clusterCache != null) {
                Set<K> set = _clusterCache.get(_localNode);
                if (set != null) {
                    CacheUtils.mapPut(_clusterCache, _localNode, set, _name,
                        _nodeId);
                }
            }
        }
    }

    /*
     * This is the Cluster's Cache and the local Node in the cluster.
     */
    private final ConcurrentMap<Node, Set<K>> _clusterCache;
    protected final Node _localNode;
    private final String _name;
    private int _nodeId;
    private final Logger _logger = LogManager.getLogger(this.getClass());
    // used to sync replicate with other write operations on _clusterCache
    private final Object _lock = new Object();
}