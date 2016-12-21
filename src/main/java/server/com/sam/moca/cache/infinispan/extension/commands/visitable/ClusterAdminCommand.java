/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.sam.moca.cache.infinispan.extension.commands.visitable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.infinispan.commands.Visitor;
import org.infinispan.context.InvocationContext;
import org.infinispan.remoting.transport.Address;

import com.sam.moca.cache.infinispan.extension.MocaVisitor;
import com.sam.moca.web.console.MocaClusterAdministration;

/**
 * Abstract class that commands can extend when doing cluster
 * administration. Allows commands to be executed against specific nodes.
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public abstract class ClusterAdminCommand implements NodeSpecificCommand {
    
    /**
     * Specifies the request addresses for the command, null can be
     * passed to indicate all nodes.
     * @param requestAddresses The list of request addresses
     */
    public ClusterAdminCommand(List<Address> requestAddresses) {
        if (requestAddresses == null) {
            _addresses = null;
        }
        else {
            _addresses = new ArrayList<Address>(requestAddresses);
        }
    }
    
    // @see org.infinispan.commands.VisitableCommand#acceptVisitor(org.infinispan.context.InvocationContext, org.infinispan.commands.Visitor)
    @Override
    public Object acceptVisitor(InvocationContext ctx, Visitor visitor)
            throws Throwable {
        if (visitor instanceof MocaVisitor) {
            return onMocaVisitor((MocaVisitor) visitor, ctx);
        }
        else {
            return visitor.visitUnknownCommand(ctx, this);
        }
    }
    
    @Override
    public List<Address> getRequestAddresses() {
        if (_addresses == null) {
            return null;
        }
        else {
            return Collections.unmodifiableList(_addresses);
        }
    }
    
    @Override
    public boolean isRequestedForAddress(Address address) {
        // No specific addresses means all nodes so return true
        if (_addresses == null) return true;
        
        return _addresses.contains(address);
    }
    
    synchronized
    public void injectComponents(MocaClusterAdministration admin) {
        _admin = admin;
    }      
    
    /**
     * Implement to visit your command.
     * @param visitor The MocaVisitor
     * @param ctx The InovactionContext
     * @return
     * @throws Throwable
     */
    protected abstract Object onMocaVisitor(MocaVisitor visitor, InvocationContext ctx) throws Throwable;

    synchronized
    protected MocaClusterAdministration getAdmin() {
        return _admin;
    }    

    private transient List<Address> _addresses;
    private transient MocaClusterAdministration _admin;
}
