/*
*  $URL: svn://localhost/prod/moca/trunk/src/java/server/com/redprairie/moca/server/profile/CommandPath.java $
*  $Revision: 468346 $
*  $Author: klehrke $
*  $Date: 2012-11-29 16:49:57 -0600 (Thu, 29 Nov 2012) $
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sam.moca.server.repository.Command;
import com.sam.moca.server.repository.Trigger;

/**
* Encapsulates the current execution path.  An object of this class provides
* a list of Command objects to which it is
* 
* Copyright (c) 2016 Sam Corporation
* All Rights Reserved
* 
* @author dinksett
*/
public class CommandPath implements Serializable {
    
    private static final long serialVersionUID = -3529923551225949418L;
    
    public static CommandPath forCommand(CommandPath previous, Command cmd) {
        return new CommandPath(previous, new CommandElement(cmd));
    }
    
    public static CommandPath forSQL(CommandPath previous) {
        return new CommandPath(previous, SQLElement.INSTANCE);
    }
    
    public static CommandPath forSQL(CommandPath previous, String profile) {
        return new CommandPath(previous, new SQLElement ( profile ));
    }
    
    public static CommandPath forTrigger(CommandPath previous, Trigger trigger) {
        return new CommandPath(previous, new TriggerElement(trigger));
    }
    
    public static CommandPath forScript(CommandPath previous) {
        return new CommandPath(previous, ScriptElement.INSTANCE);
    }
    
    public static CommandPath forRemote(CommandPath previous, String host) {
        return new CommandPath(previous, new RemoteElement(host));
    }
    
    /**
     * @return Returns the command.
     */
    public List<CommandPathElement> getCommands() {
        List<CommandPathElement> result;
        if (_previous == null) {
            result = new ArrayList<CommandPathElement>();
        }
        else {
            result = _previous.getCommands();
            result.add(_command);
        }
        
        return result;
    }
    
    public CommandPathElement getTop() {
        return _command;
    }
    
    // @see java.lang.Object#equals(java.lang.Object)
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof CommandPath)) return false;
        
        CommandPath other = (CommandPath) obj;
        
        for (CommandPath me = this; me != null; me = me._previous, other = other._previous) {
            if (other == null || !me._command.equals(other._command)) {
                return false;
            }
        }
        
        return (other == null);
    }
    
    // @see java.lang.Object#hashCode()
    @Override
    public int hashCode() {
        int hash = 0;
        
        for (CommandPath me = this; me != null; me = me._previous) {
            hash = hash * 37 + me._command.hashCode();
        }

        return hash;
    }
    
    // @see java.lang.Object#toString()
    
    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();
        buildString(tmp, this);
        return tmp.toString();
    }
    
    private CommandPath(CommandPath previous, CommandPathElement command) {
        _command = command;
        _previous = previous;
    }

    private void buildString(StringBuilder out, CommandPath level) {
        if (level._previous != null) {
           buildString(out, level._previous);
           out.append("->");
        }
        out.append(level._command);
    }
    
    private final CommandPathElement _command;
    private final CommandPath _previous;
}