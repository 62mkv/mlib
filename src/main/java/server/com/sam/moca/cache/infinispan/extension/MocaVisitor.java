/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.sam.moca.cache.infinispan.extension;

import org.infinispan.commands.Visitor;
import org.infinispan.context.InvocationContext;

import com.sam.moca.cache.infinispan.extension.commands.visitable.GetNodeUrlsCommand;
import com.sam.moca.cache.infinispan.extension.commands.visitable.RestartServerCommand;
import com.sam.moca.cache.infinispan.extension.commands.visitable.RestartTasksCommand;
import com.sam.moca.cache.infinispan.extension.commands.visitable.ScheduleJobsCommand;
import com.sam.moca.cache.infinispan.extension.commands.visitable.StartTasksCommand;
import com.sam.moca.cache.infinispan.extension.commands.visitable.StopTasksCommand;
import com.sam.moca.cache.infinispan.extension.commands.visitable.UnscheduleJobsCommand;

/**
 * This is the vistor that should be used with MOCA to visit all the command
 * types.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public interface MocaVisitor extends Visitor {
    Object visitRestartServerCommand(InvocationContext ctx, RestartServerCommand command) throws Throwable;
    
    Object visitScheduleJobsCommand(InvocationContext ctx, ScheduleJobsCommand command) throws Throwable;
    
    Object visitUnscheduleJobsCommand(InvocationContext ctx, UnscheduleJobsCommand command) throws Throwable;
    
    Object visitStartTasksCommand(InvocationContext ctx, StartTasksCommand command) throws Throwable;
    
    Object visitStopTasksCommand(InvocationContext ctx, StopTasksCommand command) throws Throwable;
    
    Object visitRestartTasksCommand(InvocationContext ctx, RestartTasksCommand command) throws Throwable;
    
    Object visitGetNodeUrlsCommand(InvocationContext ctx, GetNodeUrlsCommand command) throws Throwable;
}
