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

import javax.management.MXBean;

/**
 * A MXBean used for general monitoring operations.
 * 
 * Copyright (c) 2012 RedPrairie Corporation All Rights Reserved
 * 
 * @author rrupp
 */
@MXBean
public interface MonitoringMBean {
    
    /**
     * Returns a report containing information about the application's
     * prensently active Sessions.
     * 
     * @return A formatted Session Report
     */
    public String dumpSessionInformation();

    /**
     * Returns a snapshot report of the current database connections
     * 
     * @return A formatted Database Connection Report
     */
    public String dumpDatabaseConnectionInformation();

    /**
     * Returns a report containing information about the application's presently
     * connected users.
     * 
     * @return A formatted report of connected users.
     */
    public String dumpConnectedUserInformation();

    /**
     * Returns a report containing information about the currently running
     * Native Processes withing application.
     * 
     * @return a formatted report of native processes.
     */
    public String dumpNativeProcessInformation();
}
