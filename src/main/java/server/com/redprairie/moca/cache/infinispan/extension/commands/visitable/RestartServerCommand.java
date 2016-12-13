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

package com.redprairie.moca.cache.infinispan.extension.commands.visitable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.infinispan.commands.VisitableCommand;
import org.infinispan.commands.Visitor;
import org.infinispan.context.InvocationContext;
import org.infinispan.lifecycle.ComponentStatus;

import com.redprairie.moca.cache.infinispan.extension.MocaVisitor;

/**
 * This is a custom infinispan command that will restart the server
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class RestartServerCommand extends ClusterAdminCommand {
    public final static byte COMMAND_ID = -126;
    
    public RestartServerCommand() {
        super(null);
    }

    // @see org.infinispan.commands.ReplicableCommand#perform(org.infinispan.context.InvocationContext)
    @Override
    public Object perform(InvocationContext ctx) throws Throwable {
        _logger.info("Performing restart server from cluster request");
        if (getAdmin() != null) {
            getAdmin().restartNode();
        }

        return null;
    }

    // @see org.infinispan.commands.ReplicableCommand#getCommandId()
    @Override
    public byte getCommandId() {
        return COMMAND_ID;
    }

    // @see org.infinispan.commands.ReplicableCommand#getParameters()
    @Override
    public Object[] getParameters() {
        return new Object[0];
    }

    // @see org.infinispan.commands.ReplicableCommand#setParameters(int, java.lang.Object[])
    @Override
    public void setParameters(int commandId, Object[] parameters) {
    }

    // @see org.infinispan.commands.ReplicableCommand#isReturnValueExpected()
    @Override
    public boolean isReturnValueExpected() {
        return false;
    }

    // @see org.infinispan.commands.VisitableCommand#shouldInvoke(org.infinispan.context.InvocationContext)
    @Override
    public boolean shouldInvoke(InvocationContext ctx) {
        return true;
    }

    // @see org.infinispan.commands.VisitableCommand#ignoreCommandOnStatus(org.infinispan.lifecycle.ComponentStatus)
    @Override
    public boolean ignoreCommandOnStatus(ComponentStatus status) {
        return false;
    }
    
    // @see com.redprairie.moca.cache.infinispan.extension.commands.visitable.ClusterAdminCommand#onMocaVisitor(com.redprairie.moca.cache.infinispan.extension.MocaVisitor)
    @Override
    protected Object onMocaVisitor(MocaVisitor visitor, InvocationContext ctx) throws Throwable {
        return visitor.visitRestartServerCommand(ctx, this);
    }

    private static Logger _logger = LogManager.getLogger(RestartServerCommand.class);
}
