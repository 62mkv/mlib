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

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.redprairie.moca.cache.infinispan.extension.commands.visitable.ClusterAdminCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.GetNodeUrlsCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.RestartServerCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.RestartTasksCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.ScheduleJobsCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.StartTasksCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.StopTasksCommand;
import com.redprairie.moca.cache.infinispan.extension.commands.visitable.UnscheduleJobsCommand;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.web.console.MocaClusterAdministration;
import org.infinispan.commands.ReplicableCommand;
import org.infinispan.commands.module.ModuleCommandInitializer;
import org.infinispan.remoting.transport.Address;

/**
 * This allows for various customized commands to be initialized
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class CommandInitializer implements ModuleCommandInitializer {

    public RestartServerCommand buildPerformRestartServerCommand() {
       RestartServerCommand command = new RestartServerCommand();
       initializeReplicableCommand(command, false);
       return command;
    }
    
    public ScheduleJobsCommand buildScheduleJobsCommand(List<String> jobIds,
            List<Address> requestAddresses) {
        ScheduleJobsCommand command = new ScheduleJobsCommand(jobIds, requestAddresses);
        initializeReplicableCommand(command, false);
        return command;
    }
    
    public UnscheduleJobsCommand buildUnscheduleJobsCommand(List<String> jobIds,
            List<Address> requestAddresses) {
        UnscheduleJobsCommand command = new UnscheduleJobsCommand(jobIds, requestAddresses);
        initializeReplicableCommand(command, false);
        return command;
    }
    
    public StartTasksCommand buildStartTasksCommand(List<String> taskIds,
            List<Address> requestAddresses) {
        StartTasksCommand command = new StartTasksCommand(taskIds, requestAddresses);
        initializeReplicableCommand(command, false);
        return command;
    }
    
    public StopTasksCommand buildStopTasksCommand(List<String> taskIds,
            List<Address> requestAddresses) {
        StopTasksCommand command = new StopTasksCommand(taskIds, requestAddresses);
        initializeReplicableCommand(command, false);
        return command;
    }
    
    public RestartTasksCommand buildRestartTasksCommand(List<String> taskIds,
            List<Address> requestAddresses) {
        RestartTasksCommand command = new RestartTasksCommand(taskIds, requestAddresses);
        initializeReplicableCommand(command, false);
        return command;
    }
    
    public GetNodeUrlsCommand buildGetNodeUrlsCommand() {
        GetNodeUrlsCommand command = new GetNodeUrlsCommand();
        initializeReplicableCommand(command, false);
        return command;
    }

    // @see org.infinispan.commands.module.ModuleCommandInitializer#initializeReplicableCommand(org.infinispan.commands.ReplicableCommand, boolean)
    public void initializeReplicableCommand(ReplicableCommand c, boolean isRemote) {
        if (c instanceof ClusterAdminCommand) {
            try {
                // we wait for MCA to be inserted into the global context,
                // otherwise a very quick RPC from the other node could beat us to this point!
                if (!RPC_LATCH.await(15, TimeUnit.SECONDS)) {
                    throw new RuntimeException("Timeout waiting for RPC latch");
                }
                MocaClusterAdministration admin = ServerUtils.globalAttribute(MocaClusterAdministration.class);
                ((ClusterAdminCommand) c).injectComponents(admin);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Allow remote (or local) RPC commands to be executed against this node.
     * Any currently waiting commands are unblocked and executed.
     */
    public static void allowRPC() {
        RPC_LATCH.countDown();
    }

    /**
     * Indicates that MocaClusterAdministration is available in the global context to be used
     * (after this latch is opened).
     */
    private static final CountDownLatch RPC_LATCH = new CountDownLatch(1);
}
