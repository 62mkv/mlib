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

package com.redprairie.moca.server.legacy;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.repository.CommandRepository;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public interface NativeAdapterFactory {
    NativeLibraryAdapter getNativeAdapter(ServerContext ctx) throws MocaException;
    
    /**
     * This method has to be called before anyone retrieves a new native
     * adapter
     * @throws MocaException If any problem occurs in initialization
     */
    public void initialize() throws MocaException;
    
    /**
     * This will reset the native adapter factory to how it would be when it
     * was first initialized.  Whether or not to do a clean restart can be
     * dictated through the boolean argument.
     * @param clean whether or not to restart cleanly.
     * @param repos A command repository to be used for future process initializations.
     */
    public void restart(boolean clean, CommandRepository repos);
    
    /**
     * This is called to tell the NativeAdapterFactory to close all resources
     * related to it.
     */
    public void close();
    
    /**
     * Retrieves the native process pool that this adapter factory uses
     * @return The pool this factory is using
     */
    public NativeProcessPool getNativeProcessPool();
}
