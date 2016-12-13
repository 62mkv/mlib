/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.redprairie.moca.components.mocatest;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.cluster.manager.ClusterRoleManager;
import com.redprairie.moca.cluster.manager.TI_DynamicRoleManager;
import com.redprairie.moca.server.InstanceUrl;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.db.JDBCAdapter;
import com.redprairie.moca.web.console.MocaClusterAdministration;

/**
 * 
 * Test component used by the cluster integration tests e.g.
 * {@link TI_DynamicRoleManager}
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class ClusterTestComponent {
	
	private static final RoleDefinition EMPTY_ROLE;
	
	static {
		EMPTY_ROLE = new RoleDefinition();
		EMPTY_ROLE.setRoleId("");
	}
    
    /**
     * Gets a result set of node|roles which is each
     * node URL to a comma separated list of roles
     * @return
     */
    public static MocaResults getNodesToRoles() {
        SimpleResults res = new SimpleResults();
        res.addColumn("node", MocaType.STRING);
        res.addColumn("roles", MocaType.STRING);
        
        ClusterRoleManager manager = ServerUtils.globalAttribute(
            ClusterRoleManager.class);
        MocaClusterAdministration admin = (MocaClusterAdministration) ServerUtils.globalContext().getAttribute(MocaClusterAdministration.class.getName());
        if (manager != null && admin != null) {
            Multimap<Node, RoleDefinition> multiMap = manager.getClusterRoles();
            Map<Node, InstanceUrl> urls = admin.getKnownNodes();
            _logger.info("Cluster URLs: " + urls.entrySet());
            _logger.info("Node to Roles: " + multiMap.asMap().entrySet());
            
            Multimap<InstanceUrl, RoleDefinition> urlRoleMap = HashMultimap.create();
            
            for (Entry<Node, Collection<RoleDefinition>> entry : multiMap.asMap().entrySet()) {
                InstanceUrl url = urls.get(entry.getKey());
                // TODO: trying to handle that cluster URLs is wrong and doesn't contain the entry
                if (url == null) {
                    url = new InstanceUrl(false, entry.getKey().toString(), 0);
                }
                urlRoleMap.putAll(url, entry.getValue());
            }

            for (Entry<Node, InstanceUrl> url : urls.entrySet()) {
                if (!urlRoleMap.containsKey(url.getValue())) {
                    urlRoleMap.put(url.getValue(),
                        EMPTY_ROLE);
                }
            }
            
            // Sort by the node url
            Map<InstanceUrl, Collection<RoleDefinition>> sortedMap = 
                    new TreeMap<InstanceUrl, Collection<RoleDefinition>>(new Comparator<InstanceUrl>() {

                @Override
                public int compare(InstanceUrl o1, InstanceUrl o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });

            sortedMap.putAll(urlRoleMap.asMap());
            for (Map.Entry<InstanceUrl, Collection<RoleDefinition>> entry : sortedMap.entrySet()) {
                res.addRow();
                res.setStringValue("node", entry.getKey().toString());
                res.setStringValue("roles", Joiner.on(',').join(entry.getValue()));
            }
        }
        else {
            _logger.info("Cluster manager and/or admin not set");
        }
        
        return res;
    }
    
    public static MocaResults getClusterUrls() {
        MocaClusterAdministration admin = (MocaClusterAdministration) ServerUtils.globalContext().getAttribute(MocaClusterAdministration.class.getName());
        return admin.getDisplayNamesAndUrls();
    }

    
    private static final Logger _logger = Logger.getLogger(ClusterTestComponent.class);
}
