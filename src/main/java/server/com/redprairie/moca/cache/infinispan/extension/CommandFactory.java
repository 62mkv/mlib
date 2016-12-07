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

package com.redprairie.moca.cache.infinispan.extension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.infinispan.commands.ReplicableCommand;
import org.infinispan.commands.module.ExtendedModuleCommandFactory;
import org.infinispan.commands.remote.CacheRpcCommand;

import com.redprairie.moca.cache.infinispan.extension.commands.visitable.GetNodeUrlsCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.RestartServerCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.RestartTasksCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.ScheduleJobsCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.StartTasksCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.StopTasksCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.UnscheduleJobsCommand;

/**
 * This factory sets up additional commands that we can run
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class CommandFactory implements ExtendedModuleCommandFactory {

    @Override
    public Map<Byte, Class<? extends ReplicableCommand>> getModuleCommands() {
       Map<Byte, Class<? extends ReplicableCommand>> map = new HashMap<Byte, Class<? extends ReplicableCommand>>(6);
       map.put(RestartServerCommand.COMMAND_ID, RestartServerCommand.class);
       map.put(UnscheduleJobsCommand.COMMAND_ID, UnscheduleJobsCommand.class);
       map.put(ScheduleJobsCommand.COMMAND_ID, ScheduleJobsCommand.class);
       map.put(StartTasksCommand.COMMAND_ID, StartTasksCommand.class);
       map.put(StopTasksCommand.COMMAND_ID, StopTasksCommand.class);
       map.put(RestartTasksCommand.COMMAND_ID, RestartTasksCommand.class);
       map.put(GetNodeUrlsCommand.COMMAND_ID, GetNodeUrlsCommand.class);
       return map;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ReplicableCommand fromStream(byte commandId, Object[] args) {
        ReplicableCommand c;
        switch (commandId) {
           case RestartServerCommand.COMMAND_ID:
              c = new RestartServerCommand();
              break;
           case UnscheduleJobsCommand.COMMAND_ID:
              c = new UnscheduleJobsCommand((List<String>) args[0]);
              break;
           case ScheduleJobsCommand.COMMAND_ID:
              c = new ScheduleJobsCommand((List<String>) args[0]);
              break;
           case GetNodeUrlsCommand.COMMAND_ID:
               c = new GetNodeUrlsCommand();
               break;
           case StartTasksCommand.COMMAND_ID:
              c = new StartTasksCommand((List<String>) args[0]);
              break;
           case StopTasksCommand.COMMAND_ID:
               c = new StopTasksCommand((List<String>) args[0]);
               break;
           case RestartTasksCommand.COMMAND_ID:
               c = new RestartTasksCommand((List<String>) args[0]);
               break;
           default:
              throw new IllegalArgumentException("Not registered to handle command id " + commandId);
        }
        c.setParameters(commandId, args);
        return c;
    }

    // @see org.infinispan.commands.module.ExtendedModuleCommandFactory#fromStream(byte, java.lang.Object[], java.lang.String)
    @Override
    public CacheRpcCommand fromStream(byte commandId, Object[] args,
        String cacheName) {
        return null;
    }
 }
