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

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class ProfileUtils {
    public static void calculateChildTimes(Iterable<CommandUsageStatistics> stats) {
        // This is a deep copy of the profiling statistics, so we can manipulate
        // this however we want.
        Map<String, Long> childTimes = new HashMap<String, Long>();
        
        for (CommandUsageStatistics entry : stats) {
            String path = entry.getPath();
            String parentPath;
            int tailPosition = path.lastIndexOf("->");
            if (tailPosition > 0) {
                parentPath = path.substring(0, tailPosition); 
            }
            else {
                parentPath = "";
            }
           
            Long time = childTimes.get(parentPath);
            if (time == null) {
                time = entry.nanos();
            }
            else {
                time += entry.nanos();
            }
            childTimes.put(parentPath, time);
        }

        for (CommandUsageStatistics entry : stats) {
            Long childTime = childTimes.get(entry.getPath());
            if (childTime != null) {
                entry.setChildTime(childTime);
            }
            else {
                entry.setChildTime(0L);
            }
        }
    }

    public static void writeUsage(Collection<CommandUsageStatistics> values,
                                  Writer out) throws IOException {
        out.write("component_level,command,type,command_path,execution_count,total_ms,min_ms,max_ms,self_ms");
        out.write(System.getProperty("line.separator"));
        for (CommandUsageStatistics stats : values) {
            out.write(stats.getLevelName());
            out.write(',');
            out.write(stats.getCommandName());
            out.write(',');
            out.write(stats.getCommandType());
            out.write(',');
            out.write(stats.getPath());
            out.write(',');
            out.write(String.valueOf(stats.getCount()));
            out.write(',');
            out.write(String.valueOf(stats.getTotalTime()));
            out.write(',');
            out.write(String.valueOf(stats.getMinTime()));
            out.write(',');
            out.write(String.valueOf(stats.getMaxTime()));
            out.write(',');
            out.write(String.valueOf(stats.getSelfTime()));
            out.write(System.getProperty("line.separator"));
        }
    }
}
