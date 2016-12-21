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

package com.sam.moca.cluster.infinispan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachemanagerlistener.annotation.Merged;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.infinispan.remoting.transport.Address;

import com.sam.moca.cluster.MocaClusterMembershipListener;
import com.sam.moca.cluster.Node;

/**
 * This class is designed to be the global receiver that listeners can register 
 * with to allow them to get notifications of membership changes or state 
 * requests or retrievals.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
@Listener(sync=false)
public class GlobalListener {
    /**
     * Adds another listener that will be called whenever a member change occurs.
     * @param listener The listener to receive callbacks.
     */
    public void addMembershipListener(MocaClusterMembershipListener listener) {
        _membershipListeners.add(listener);
    }

    @Merged
    @ViewChanged
    public void viewAccepted(ViewChangedEvent event) {
        _logger.info("View Changed Event: " + event);
        List<Address> current = event.getNewMembers();
        List<Address> old = event.getOldMembers();
        Address local = event.getLocalAddress();
        
        List<Node> nodes = new ArrayList<Node>(current.size());
        
        for (Address member : current) {
            nodes.add(new InfinispanNode(member));
        }
        
        // Make it unmodifiable so that listeners can't mess up later listeners
        List<Node> unmodifiableMembers = Collections.unmodifiableList(nodes);
        
        List<Address> oldCopy = new ArrayList<Address>(old);
        // This will leave old only having members that have now left.
        oldCopy.removeAll(current);
        List<Node> leavers = new ArrayList<Node>(oldCopy.size());
        
        for (Address member : oldCopy) {
            leavers.add(new InfinispanNode(member));
        }
        
        // Make it unmodifiable so that listeners can't mess up later listeners
        List<Node> unmodifiableLeavers = Collections.unmodifiableList(leavers);
        
        List<Address> curCp = new ArrayList<Address>(current);
        // This will leave only the new members that have been recently added
        curCp.removeAll(old);
        List<Node> joiners = new ArrayList<Node>(curCp.size());
        
        for (Address member : curCp) {
            joiners.add(new InfinispanNode(member));
        }
        
        // Make it unmodifiable so that listeners can't mess up later listeners
        List<Node> unmodifiableJoiners = Collections.unmodifiableList(joiners);
        
        for (MocaClusterMembershipListener listener : _membershipListeners) {
            // merging expects that MocaClusterAdministration is first and 
            // and Role Manager is 2nd
            try {
                listener.notifyMembership(new InfinispanNode(local),
                    unmodifiableMembers, unmodifiableJoiners,
                    unmodifiableLeavers, event.isMergeView());
            }
            catch (Exception ex) {
                // Safeguard ourselves from bad listeners, we should still execute the others
                _logger.error(String.format("Execution of cluster membership listener %s ended in an exception",
                        listener), ex);
            }
        }
    }
    
    private final List<MocaClusterMembershipListener> _membershipListeners = 
        new CopyOnWriteArrayList<MocaClusterMembershipListener>();
    
    private static final Logger _logger = LogManager.getLogger(GlobalListener.class);
}
