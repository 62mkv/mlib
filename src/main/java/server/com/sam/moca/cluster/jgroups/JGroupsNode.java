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

import org.jgroups.Address;

import com.sam.moca.cluster.Node;

/**
 * This is a node implementation that internally holds onto an JGroups 
 * address object.  The class itself is immutable, however it is unknown if
 * JGroups Address object is immutable.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class JGroupsNode implements Node {
    
    private static final long serialVersionUID = 3585260519798712372L;

    public JGroupsNode(Address address) {
        _address = address;
    }
    
    public Address getAddress() {
        return _address;
    }
    
    // @see java.lang.Object#hashCode()
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((_address == null) ? 0 : _address.hashCode());
        return result;
    }

    // @see java.lang.Object#equals(java.lang.Object)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        JGroupsNode other = (JGroupsNode) obj;
        if (_address == null) {
            if (other._address != null) return false;
        }
        else if (!_address.equals(other._address)) return false;
        return true;
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return "JGroupsNode [address=" + _address + "]";
    }

    private final Address _address;
}
