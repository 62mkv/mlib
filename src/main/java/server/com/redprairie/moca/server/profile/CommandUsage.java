/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.server.profile;

import java.util.Collection;

/**
 * Command Usage Logger.  This class tracks how often a command has been used.
 * 
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public interface CommandUsage {
    /**
     * Logs a single command path execution. The command is assumed to be the
     * "top" of the command path.
     * @param path the command path that executed.
     * @param nanos The time (in nanoseconds) spent executing the command path.
     */
    public void logCommandExecution(CommandPath path, long nanos);
    
    /**
     * Returns a collection of Command usage statistics.  The collection
     * returned from this method is a deep copy, so the caller can manipulate
     * the collection and the statistics objects within it as needed.
     * @return
     */
    public Collection<CommandUsageStatistics> getStats();
    
    /**
     * Clears the current statistics.
     */
    public void reset();
}
