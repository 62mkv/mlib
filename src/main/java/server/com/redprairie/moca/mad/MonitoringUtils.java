/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.mad;

import com.redprairie.mad.client.MadMetrics;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.web.console.RegistryInformation;

/**
 * Monitoring Utility methods
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class MonitoringUtils {

    public static final String MOCA_GROUP_NAME = "com.redprairie.moca";

    /**
     * A simple method to check whether or not Monitoring and Diagnostics tools
     * are enabled or disabled.
     * 
     * @return
     */
    public static boolean isMonitoringEnabled() {
        return isMonitoringEnabled(ServerUtils.globalContext());
    }

    /**
     * A simple method to check if Monitoring and Diagnostics are enabled or
     * disabled given a System Context.
     * 
     * @param context
     * @return
     */
    public static boolean isMonitoringEnabled(SystemContext context) {
        String probing = context.getConfigurationElement(
            MocaRegistry.REGKEY_SERVER_MAD_PROBING_ENABLED,
            MocaRegistry.REGKEY_SERVER_MAD_PROBING_ENABLED_DEFAULT);

        if (probing.equalsIgnoreCase("true") || probing.equals("1")
                || probing.equalsIgnoreCase("yes")) {
            return true;
        }

        return false;
    }
    
    public static void pushStartupMetrics() {
        // Push the registry information
        String contents = RegistryInformation.getRegistryContents();
        MadMetrics.getFactory().sendNotification("moca.registry", contents);
    }
    
}
