/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.moca.cache.infinispan.extension.commands.visitable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.infinispan.context.InvocationContext;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.remoting.transport.Address;

import com.redprairie.moca.cache.infinispan.extension.MocaVisitor;

/**
 * This custom infinispan command is used to restart tasks.
 * Supports restarting a list of tasks.
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class RestartTasksCommand extends ClusterAdminCommand implements MultipleResponseNodeCommand {

    public final static byte COMMAND_ID = -124;

    public RestartTasksCommand(List<String> taskIds) {
        this(taskIds, null);
    }
    
    public RestartTasksCommand(List<String> taskIds, List<Address> requestAddresses) {
        super(requestAddresses);
        _taskIds = new ArrayList<String>(taskIds);
    }
    
    // @see org.infinispan.commands.ReplicableCommand#getCommandId()
    @Override
    public byte getCommandId() {
        return COMMAND_ID;
    }

    // @see org.infinispan.commands.ReplicableCommand#getParameters()
    @Override
    public Object[] getParameters() {
        return new Object[] { _taskIds };
    }

    // @see org.infinispan.commands.ReplicableCommand#isReturnValueExpected()
    @Override
    public boolean isReturnValueExpected() {
        return true;
    }

    /**
     * Stores a result map of request ID (task ID) to NodeResponse indicating if the
     * task was successfully restarted.
     * @see org.infinispan.commands.ReplicableCommand#perform(org.infinispan.context.InvocationContext)
     */
    @Override
    public Map<String, NodeResponse> perform(InvocationContext ctx) {
        Map<String, NodeResponse> restartedTasks = new HashMap<String, NodeResponse>(_taskIds.size());
        for (String taskId : _taskIds) {
            restartedTasks.put(taskId, getAdmin().restartTaskOnNode(taskId));
        }

        return restartedTasks;
    }

    // @see org.infinispan.commands.ReplicableCommand#setParameters(int, java.lang.Object[])
    @Override
    public void setParameters(int commandId, Object[] args) {
        // Constructor should be used instead
    }

    // @see org.infinispan.commands.VisitableCommand#ignoreCommandOnStatus(org.infinispan.lifecycle.ComponentStatus)    
    @Override
    public boolean ignoreCommandOnStatus(ComponentStatus status) {
        return false;
    }

    // @see org.infinispan.commands.VisitableCommand#shouldInvoke(org.infinispan.context.InvocationContext)
    @Override
    public boolean shouldInvoke(InvocationContext ctx) {
        return true;
    }
    
    public List<String> getRequestIds() {
        return Collections.unmodifiableList(_taskIds);
    }
    
    // @see com.redprairie.moca.cache.infinispan.extension.commands.visitable.ClusterAdminCommand#onMocaVisitor(com.redprairie.moca.cache.infinispan.extension.MocaVisitor, org.infinispan.context.InvocationContext)
    @Override
    protected Object onMocaVisitor(MocaVisitor visitor, InvocationContext ctx)
            throws Throwable {
        return visitor.visitRestartTasksCommand(ctx, this);
    } 
    
    private final List<String> _taskIds;
}
