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

package com.redprairie.moca.probes.tasks;

import com.redprairie.mad.client.MadName;
import com.redprairie.mad.client.MadNameImpl;
import com.redprairie.mad.client.MadUtil;
import com.redprairie.moca.mad.MonitoringUtils;
import com.redprairie.moca.task.TaskManager;

/**
 * Used to expose Task Summary information
 * on a MBean Server.
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TaskSummaryProbe implements TaskSummaryMXBean {
    
    public static final MadName NAME = new MadNameImpl(MonitoringUtils.MOCA_GROUP_NAME,
                                                       "Tasks-Summary",
                                                       "summary");
    
    public static void registerTaskSummary(TaskManager manager) {
        TaskSummaryProbe summary = new TaskSummaryProbe(manager);
        MadUtil.registerMBean(NAME, summary);
    }
    
    public static void unregisterTaskSummary() {
        MadUtil.unregisterMBean(NAME);
    }

    public TaskSummaryProbe(TaskManager manager) {
        _manager = manager;
    }
    
    // @see com.redprairie.moca.mad.TaskSummaryMXBean#getSummary()
    @Override
    public TaskSummarySnapshot getSummary() {
        return new TaskSummarySnapshot(_manager);
    }

    private TaskManager _manager;
}
