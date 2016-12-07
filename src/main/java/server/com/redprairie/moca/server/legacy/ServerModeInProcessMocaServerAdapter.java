/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 */

package com.redprairie.moca.server.legacy;

import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;

/**
 * In Process Moca Server Adapter that is utilized for usage in a server mode
 * C process.
 * 
 * Copyright (c) 2013 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class ServerModeInProcessMocaServerAdapter extends
        InProcessMocaServerAdapter {

    /**
     * @param processName
     * @param singleThreaded
     * @throws SystemConfigurationException
     */
    public ServerModeInProcessMocaServerAdapter(String processName,
            boolean singleThreaded) throws SystemConfigurationException {
        // We need to configure it this way since we don't want to create the 
        // ContextMocaServerAdapter until after we configure logging
        super(new ContextMocaServerAdapter(ServerUtils.setupDaemonContext(
                processName, singleThreaded, true)));
    }

}
