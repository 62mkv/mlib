/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.sam.moca.cache.infinispan;

import java.util.Properties;

import org.infinispan.remoting.transport.jgroups.JGroupsChannelLookup;
import org.jgroups.Channel;

import com.sam.moca.cluster.jgroups.JGroupsChannelFactory;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.SystemConfigurationException;

/**
 * This is a lookup to be used by infinispan to find a jgroups channel.  This
 * is used so that we can provide a way to inject the xml configuration file
 * when using infinispan to reuse the same channel properties as all the rest
 * of the system.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MocaJGroupsChannelLookup implements JGroupsChannelLookup {
    
    // @see org.infinispan.remoting.transport.jgroups.JGroupsChannelLookup#getJGroupsChannel(java.util.Properties)
    @Override
    public Channel getJGroupsChannel(Properties p) {
        try {
            return JGroupsChannelFactory.getChannelForConfig(
                ServerUtils.globalContext());
        }
        catch (SystemConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    // @see org.infinispan.remoting.transport.jgroups.JGroupsChannelLookup#shouldStartAndConnect()
    @Override
    public boolean shouldStartAndConnect() {
        return true;
    }

    // @see org.infinispan.remoting.transport.jgroups.JGroupsChannelLookup#shouldStopAndDisconnect()
    @Override
    public boolean shouldStopAndDisconnect() {
        return true;
    }
}
