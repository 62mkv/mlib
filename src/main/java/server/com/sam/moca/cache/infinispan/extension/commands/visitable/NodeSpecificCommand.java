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

import java.util.List;

import org.infinispan.commands.VisitableCommand;
import org.infinispan.remoting.transport.Address;

/**
 * Command interface to indicate that a command should only be
 * executed on a subset of Addresses.
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public interface NodeSpecificCommand extends VisitableCommand {
    
    /**
     * Indicates the command has been requested to execute
     * on the given address.
     * @return
     */
    boolean isRequestedForAddress(Address address);
    
    /**
     * Gets an unmodifiable list of addresses of nodes which the command request should go to.
     * If this is null then the request is for all available nodes.
     * @return An unmodifiable list of addresses
     */
    List<Address> getRequestAddresses();

}
