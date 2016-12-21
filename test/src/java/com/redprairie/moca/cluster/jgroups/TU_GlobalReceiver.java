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

package com.sam.moca.cluster.jgroups;

import java.util.ArrayList;
import java.util.List;

import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.infinispan.remoting.transport.Address;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.sam.moca.cluster.MocaClusterMembershipListener;
import com.sam.moca.cluster.Node;
import com.sam.moca.cluster.infinispan.GlobalListener;
import com.sam.moca.cluster.infinispan.InfinispanNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the implementation of GlobalReceiver
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_GlobalReceiver {
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testMembershipListener() {
        GlobalListener receiver = new GlobalListener();
        
        MocaClusterMembershipListener listener = Mockito.mock(MocaClusterMembershipListener.class);
        
        receiver.addMembershipListener(listener);
        
        List<Address> members = new ArrayList<Address>();
        Address address1 = Mockito.mock(Address.class);
        Address address2 = Mockito.mock(Address.class);
        members.add(address1);
        members.add(address2);
        ViewChangedEvent event = Mockito.mock(ViewChangedEvent.class);
        Mockito.when(event.getNewMembers()).thenReturn(members);
        receiver.viewAccepted(event);
        
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(listener)
            .notifyMembership(Mockito.any(Node.class), captor.capture(),
                Mockito.any(List.class), Mockito.any(List.class),
                Mockito.any(Boolean.class));
        
        List<Node> nodes = (List<Node>)captor.getValue();
        
        for (Node node : nodes) {
            assertTrue(node instanceof InfinispanNode);
            assertTrue("We received a node we didn't expect!", 
                members.remove(((InfinispanNode)node).getAddress()));
        }
        
        assertEquals("We didn't get some nodes passed correctly", 0, 
            members.size());
    }
}