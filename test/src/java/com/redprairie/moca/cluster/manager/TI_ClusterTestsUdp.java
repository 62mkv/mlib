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

package com.redprairie.moca.cluster.manager;

import com.redprairie.moca.cluster.manager.simulator.NodeConfiguration;

/**
 * 
 * Runs the cluster tests defined in {@link AbstractClusterTests}
 * using UDP (udp + multicast discovery) as the JGroups protocol
 */
public class TI_ClusterTestsUdp extends AbstractClusterTests {

    // @see com.redprairie.moca.cluster.manager.TI_ClusterTests#getBaseNodeConfiguration()
    @Override
    protected NodeConfiguration getBaseNodeConfiguration() {
        return newNodeConfiguration(RoleManagerType.PREFERRED, clusterName, "", false);
    }

}
