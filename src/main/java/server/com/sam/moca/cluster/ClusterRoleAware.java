/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.sam.moca.cluster;

import java.util.List;

/**
 * This interface is to be implemented by classes that are affected by what
 * roles are currently associated with this server.
 * 
 * Copyright (c) 2010 Sam Corporation All Rights Reserved
 * 
 * @author wburns
 */
public interface ClusterRoleAware {
	public void activateRole(RoleDefinition role);

	public void deactivateRole(RoleDefinition role);

	/**
	 * This method should be invoked on the object to symbolize that there is no
	 * cluster at all and that all roles should be available for use. Once this
	 * is invoked further calls to activate or deactivate roles are simply
	 * ignored.
	 */
	public void noCluster();

	/**
	 * When a merge event is detected (either on startup or when recovering from
	 * split brain), this method will be called on classes implementing this
	 * interface. Typically, you want your implementing class to clear all
	 * cluster caches or have all nodes put all so that caches are mended.
	 * 
	 * Currently, only the leader should take action, the idiom for determine if
	 * a node is the leader is local.equals(members.get(0))
	 * 
	 * Also, locking (condition.await on non-leaders and condition.signalAll on
	 * the leader) is used so that the leader will try it's best to go first.
	 * Since we only need 1 lock per node and not for each cluster role aware
	 * object, the implementing class does not need to handle this.
	 * 
	 * @param local
	 * @param members
	 */
	public void handleMerge(List<Node> members, Node local);

}
