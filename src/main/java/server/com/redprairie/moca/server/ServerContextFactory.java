/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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

package com.redprairie.moca.server;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.server.db.DBAdapter;
import com.redprairie.moca.server.exec.RequestContext;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SessionContext;
import com.redprairie.moca.server.legacy.NativeProcessPool;
import com.redprairie.moca.server.profile.CommandUsage;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public interface ServerContextFactory {
    public ServerContext newContext(RequestContext req, SessionContext session);
    
    /**
     * This method is to be used as a hook point to link together the request,
     * server and server context factory to the thread if an existing server
     * context was present while being called.
     * @param req
     * @param session
     */
    public void associateContext(RequestContext req, SessionContext session);
    
    /**
     * This needs to be called before anyone makes any calls into any 
     * ServerContext that is retrieved from this factory.
     * @throws SystemConfigurationException 
     */
    public void initialize() throws SystemConfigurationException;
    
    /**
     * This method can be called to restart the context factory to a state 
     * similar to what it was in when it was first initialized.  It can chosen
     * whether the restart is done cleanly in which case resources will be waited
     * upon to clean up or to do it immediately.
     * @param clean whether to initiate restart cleanly.
     * @throws MocaException If a problem occurs while restarting.
     */
    public void restart(boolean clean) throws MocaException;
    
    /**
     * This will return the native pool that is associated with this Factory.
     * This pool stores information as to how many available, running native
     * processes there.
     * @return The native process pool associated with this factory.
     */
    public NativeProcessPool getNativePool();
    
    /**
     * This will return the database adapter that is associated with this Factory.
     * The database adapter provides a means to get database connection statistics.
     * @return The database adapter associated with this factory.
     */
    public DBAdapter getDBAdapter();
    
    /**
     * Returns the current CommandUsage collector.  The instance of CommandUsage
     * can be queried for status to provide current command usage information.
     * 
     * @return the CommandUsage instance associated with this factory.
     */
    public CommandUsage getCommandUsage();

    /**
     * Get a hook from the server configuration.
     * @param cls the type of the hook to look up.
     * @return an implementation of the passed class.
     */
    public <T> T getHook(Class<T> cls);
    
    /**
     * This is called to shutdown all resources this factory currently holds
     * onto.
     */
    public void close();
}
