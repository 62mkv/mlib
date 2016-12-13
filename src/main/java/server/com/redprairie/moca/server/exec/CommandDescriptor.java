/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.server.exec;

import java.io.Serializable;
import java.util.List;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.server.repository.Command;
import com.redprairie.moca.server.repository.Trigger;

/**
 * Represents a command invocation that has been resolved at run-time to an
 * specific component.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class CommandDescriptor implements ExecutableComponent, Serializable {
    public CommandDescriptor(Command commandDef, List<Trigger> triggers) {
        _commandDef = commandDef;
        _triggers = triggers;
    }
    
    @Override
    public MocaResults execute(ServerContext ctx) throws MocaException {
        return ctx.executeDefinedCommand(_commandDef, _triggers);
    }
    
    private final List<Trigger> _triggers;
    private final Command _commandDef;
    private static final long serialVersionUID = 1L;
}
