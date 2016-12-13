/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.advice;

import com.google.common.collect.ForwardingObject;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.db.DBAdapter;
import com.redprairie.moca.server.exec.RequestContext;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SessionContext;
import com.redprairie.moca.server.legacy.NativeProcessPool;
import com.redprairie.moca.server.profile.CommandUsage;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class ForwardingServerContextFactory extends ForwardingObject 
        implements ServerContextFactory {
    
    public ForwardingServerContextFactory(ServerContextFactory factory) {
        _factory = factory;
    }

    // @see com.redprairie.moca.server.ServerContextFactory#newContext(com.redprairie.moca.server.exec.RequestContext, com.redprairie.moca.server.exec.SessionContext)
    @Override
    public ServerContext newContext(RequestContext req, SessionContext session) {
        return delegate().newContext(req, session);
    }

    // @see com.redprairie.moca.server.ServerContextFactory#associateContext(com.redprairie.moca.server.exec.RequestContext, com.redprairie.moca.server.exec.SessionContext)

    @Override
    public void associateContext(RequestContext req, SessionContext session) {
        delegate().associateContext(req, session);
    }

    // @see com.redprairie.moca.server.ServerContextFactory#initialize()
    @Override
    public void initialize() throws SystemConfigurationException {
        delegate().initialize();
    }

    // @see com.redprairie.moca.server.ServerContextFactory#restart(boolean)
    @Override
    public void restart(boolean clean) throws MocaException {
        delegate().restart(clean);
    }

    // @see com.redprairie.moca.server.ServerContextFactory#getNativePool()
    @Override
    public NativeProcessPool getNativePool() {
        return delegate().getNativePool();
    }

    // @see com.redprairie.moca.server.ServerContextFactory#getDBAdapter()
    @Override
    public DBAdapter getDBAdapter() {
        return delegate().getDBAdapter();
    }

    // @see com.redprairie.moca.server.ServerContextFactory#getCommandUsage()
    @Override
    public CommandUsage getCommandUsage() {
        return delegate().getCommandUsage();
    }
    
    // @see com.redprairie.moca.server.ServerContextFactory#getHook(java.lang.Class)
    @Override
    public <T> T getHook(Class<T> cls) {
        return delegate().getHook(cls);
    }
    
    // @see com.redprairie.moca.server.ServerContextFactory#close()
    @Override
    public void close() {
        delegate().close();
    }
    
    // @see com.google.common.collect.ForwardingObject#delegate()
    @Override
    protected ServerContextFactory delegate() {
        return _factory;
    }

    private final ServerContextFactory _factory;
}
