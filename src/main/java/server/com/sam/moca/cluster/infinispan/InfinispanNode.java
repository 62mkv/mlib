/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

import org.infinispan.remoting.transport.Address;

import com.sam.moca.cluster.Node;

/**
 * This is a node implementation that internally holds onto an infinispan 
 * address object.  The class itself is immutable, however it is unknown if
 * Infinispan Address object is immutable.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class InfinispanNode implements Node {

    private static final long serialVersionUID = -8886222036784169957L;

    public InfinispanNode(Address address) {
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
        InfinispanNode other = (InfinispanNode) obj;
        if (_address == null) {
            if (other._address != null) return false;
        }
        else if (!_address.equals(other._address)) return false;
        return true;
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return "InfinispanNode [address=" + _address + "]";
    }

    private final Address _address;
}
