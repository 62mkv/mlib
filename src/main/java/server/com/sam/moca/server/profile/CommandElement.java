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

package com.sam.moca.server.profile;

import com.sam.moca.server.repository.Command;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class CommandElement implements CommandPathElement {
    private static final long serialVersionUID = -4264092055006669723L;

    public CommandElement(Command command) {
        _command = command;
    }
    
    @Override
    public String getGroup() {
        return _command.getLevel().getName();
    }
    
    @Override
    public String getName() {
        return _command.getName();
    }
    
    @Override
    public String getType() {
        return _command.getType().toString();
    }
    
    @Override
    public String toString() {
        return _command.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CommandElement)) return false;
        
        return ((CommandElement)obj)._command.equals(_command);
    }
    
    @Override
    public int hashCode() {
        return _command.hashCode();
    }
    
    private final Command _command;
}
