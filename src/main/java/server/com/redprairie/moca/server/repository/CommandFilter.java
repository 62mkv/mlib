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

package com.redprairie.moca.server.repository;

import com.redprairie.moca.server.exec.DefaultServerContext;

/**
 * Instances of classes that implement this interface are used to filter
 * commands.  These instances are used to filter commands in the 
 * <code>listCommands</code> method of class <code>DefaultServerContext</code>.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 * @see DefaultServerContext#listCommands(com.redprairie.moca.MocaContext, CommandFilter)
 */
public interface CommandFilter {
    /**
     * This method will be called upon a CommandFilter while determining whether
     * a command should be returned or not.
     * @param commandName The command name to check
     * @param componentLevel The component level to check
     * @return whether or not to accept this command
     */
    public boolean accept(String commandName, String componentLevel);
}
