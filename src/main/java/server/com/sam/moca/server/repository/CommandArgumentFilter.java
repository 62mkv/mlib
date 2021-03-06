/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
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

package com.sam.moca.server.repository;

import com.sam.moca.server.exec.DefaultServerContext;

/**
 * Instances of classes that implement this interface are used to filter
 * command arguments.  These instances are used to filter command arguments in 
 * the <code>listCommandArguments</code> method of class 
 * <code>DefaultServerContext</code>.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 * @see DefaultServerContext#listCommandArguments(com.sam.moca.MocaContext, CommandFilter)
 */
public interface CommandArgumentFilter {
    /**
     * This method will be called upon a CommandArgumentFilter while determining
     *  whether a command argument should be returned or not.
     * @param componentLevel The component level to check
     * @param commandName The command name to check
     * @param argumentName The argument name to check
     * @return whether or not to accept this command argument
     */
    public boolean accept(String componentLevel, String commandName, 
            String argumentName);
}
