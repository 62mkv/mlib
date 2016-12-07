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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class InMemoryCommandUsage implements CommandUsage {
    
    @Override
    public void logCommandExecution(CommandPath path, long nanos) {
        CommandPathElement cmd = path.getTop();
        CommandUsageStatistics stats = _pathLog.get(path);
        if (stats == null) {
            stats = new CommandUsageStatistics(cmd, path);
            _pathLog.put(path, stats);
        }

        stats.log(nanos);
    }
    
    @Override
    public Collection<CommandUsageStatistics> getStats() {
        List<CommandUsageStatistics> result = new ArrayList<CommandUsageStatistics>();
        for (CommandUsageStatistics stats : _pathLog.values()) {
            CommandUsageStatistics copy = (CommandUsageStatistics) stats.clone();
            result.add(copy);
        }
        
        // Now that we have a copy of the values, it's safe to do some
        // calculations on them.
        ProfileUtils.calculateChildTimes(result);
        
        return result;
    }
    
    // @see com.redprairie.moca.server.profile.CommandUsage#reset()
    
    @Override
    public void reset() {
        _pathLog.clear();
    }
    
    private ConcurrentMap<CommandPath, CommandUsageStatistics> _pathLog = new ConcurrentHashMap<CommandPath, CommandUsageStatistics>();
}
