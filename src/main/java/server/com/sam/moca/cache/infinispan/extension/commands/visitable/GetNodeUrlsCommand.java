/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.sam.moca.cache.infinispan.extension.commands.visitable;

import org.infinispan.context.InvocationContext;
import org.infinispan.lifecycle.ComponentStatus;

import com.sam.moca.cache.infinispan.extension.MocaVisitor;

/**
 * Cluster command to get MOCA instance URLs.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author mdobrinin
 */
public class GetNodeUrlsCommand extends ClusterAdminCommand implements SingleResponseNodeCommand {
    public final static byte COMMAND_ID = -100;
    
    public GetNodeUrlsCommand() {
        super(null);
    }

    // @see org.infinispan.commands.ReplicableCommand#getCommandId()
    @Override
    public byte getCommandId() {
        return COMMAND_ID;
    }
    
    // @see org.infinispan.commands.ReplicableCommand#isReturnValueExpected()
    @Override
    public boolean isReturnValueExpected() {
        return true;
    }

    // @see com.sam.moca.cache.infinispan.extension.commands.visitable.MultipleNodeResponseCommand#perform(org.infinispan.context.InvocationContext)
    @Override
    public NodeResponse perform(InvocationContext ctx) {
        return NodeResponse.sucessfulResponse(getAdmin().getLocalNodeUrl());
    }
    
    // @see com.sam.moca.cache.infinispan.extension.commands.visitable.ClusterAdminCommand#onMocaVisitor(com.sam.moca.cache.infinispan.extension.MocaVisitor, org.infinispan.context.InvocationContext)
    @Override
    protected Object onMocaVisitor(MocaVisitor visitor, InvocationContext ctx)
            throws Throwable {
        return visitor.visitGetNodeUrlsCommand(ctx, this);
    }

    // @see org.infinispan.commands.VisitableCommand#ignoreCommandOnStatus(org.infinispan.lifecycle.ComponentStatus)
    @Override
    public boolean ignoreCommandOnStatus(ComponentStatus arg0) {
        return false;
    }

    // @see org.infinispan.commands.VisitableCommand#shouldInvoke(org.infinispan.context.InvocationContext)
    @Override
    public boolean shouldInvoke(InvocationContext arg0) {
        return true;
    }
    
    // @see org.infinispan.commands.ReplicableCommand#getParameters()
    @Override
    public Object[] getParameters() {
        return new Object[0];
    }

    // @see org.infinispan.commands.ReplicableCommand#setParameters(int, java.lang.Object[])
    @Override
    public void setParameters(int arg0, Object[] arg1) {
    }
}
