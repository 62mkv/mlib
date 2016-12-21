/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20167
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

package com.sam.moca.server.exec;

import com.sam.moca.MocaException;
import com.sam.moca.server.repository.Command;

/**
 * Interface for scripting support.
 * 
 * <b><pre>
 * Copyright (c) 20167 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public interface ScriptAdapter {

    /**
     * Compiles a script into compiled form.  The compiled form is something that is specific to the particular script
     * adapter that created it.
     * @param script
     * @param runningCommand
     * @return
     * @throws MocaException
     */
    public CompiledScript compile(String script, Command runningCommand) throws MocaException;
}