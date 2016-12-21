/*
 *  $URL$
 *  $Author$
 *  $Date$
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

package com.sam.moca.cluster;

import java.util.List;

/**
 * This class is an interface describing a callback that is called whenever
 * there is a change in the membership of the cluster.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public interface MocaClusterMembershipListener {
    /**
     * This method is a callback that is invoked displaying all the current
     * known members at the time of invocation.  This method is invoked when
     * either a member has been added or removed.
     * <p>
     * This method should complete as fast as possible.  Normally just updating
     * some internal state that another thread can read and act upon.
     * @param local
     * @param members
     * @param joiners
     * @param leavers
     * @param isMergeView
     */
    public void notifyMembership(Node local, List<Node> members,
                                 List<Node> joiners, List<Node> leavers,
                                 Boolean isMergeView);
}