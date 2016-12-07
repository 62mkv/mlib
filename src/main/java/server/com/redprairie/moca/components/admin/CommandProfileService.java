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

package com.redprairie.moca.components.admin;

import java.util.Collection;

import com.redprairie.moca.BeanResults;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.server.exec.DefaultServerContext;
import com.redprairie.moca.server.profile.CommandUsageStatistics;
import com.redprairie.moca.util.MocaUtils;

/**
 * A component front-end to the command profiler capability in MOCA.
 * 
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class CommandProfileService {
    public MocaResults listCommandUsage(MocaContext moca) throws MocaException {
        Collection<CommandUsageStatistics> stats = DefaultServerContext.listCommandUsage(moca);
        BeanResults<CommandUsageStatistics> res = new BeanResults<CommandUsageStatistics>(CommandUsageStatistics.class);
        res.addRows(stats);
        
        return MocaUtils.filterResults(res, moca);
    }
    
    public void clearCommandUsage(MocaContext moca) throws MocaException {
        DefaultServerContext.clearCommandUsage(moca);
    }
}
