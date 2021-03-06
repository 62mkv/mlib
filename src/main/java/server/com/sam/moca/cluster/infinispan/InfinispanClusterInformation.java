/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.sam.moca.cluster.infinispan;

import java.util.List;
import java.util.Map;

import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;

import com.sam.moca.cluster.ClusterInformation;
import com.sam.moca.cluster.Node;
import com.sam.moca.server.InstanceUrl;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.web.console.MocaClusterAdministration;

/**
 * A cluster information implementation that uses infinispan cache manager
 * as a back end to tell information.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class InfinispanClusterInformation implements ClusterInformation {
    public InfinispanClusterInformation(EmbeddedCacheManager cacheManager) {
        _cacheManager = cacheManager;
    }

    // @see com.sam.moca.cluster.ClusterInformation#isLeader()
    @Override
    public boolean isLeader() {
        MocaClusterAdministration admin = ServerUtils.globalAttribute(
            MocaClusterAdministration.class);
        Map<Node, InstanceUrl> nodes = admin.getKnownNodes();
        List<Address> addresses = _cacheManager.getMembers();
        
        Address leader = null;
        // Find the first member that has a url as this will be the leader
        for (Address address : addresses) {
            if (nodes.containsKey(new InfinispanNode(address))) {
                leader = address;
                break;
            }
        }
        
        return _cacheManager.getAddress().equals(leader);
    }

    private final EmbeddedCacheManager _cacheManager;
}
