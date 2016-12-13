/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.redprairie.moca.server.profile;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Null implementation of CommandUsage logger.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class NullCommandUsage implements CommandUsage {

    @Override
    public void logCommandExecution(CommandPath path, long nanos) {
        // Do nothing
    }
    
    @Override
    public Collection<CommandUsageStatistics> getStats() {
        return new ArrayList<CommandUsageStatistics>();
    }

    @Override
    public void reset() {
        // Do nothing
        
    }
}
