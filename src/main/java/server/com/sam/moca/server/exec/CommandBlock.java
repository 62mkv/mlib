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
import com.sam.moca.MocaResults;
import com.sam.moca.server.expression.Expression;

public class CommandBlock implements ExecutableComponent {
    
    public static enum RemoteType {
        REMOTE, PARALLEL, INPARALLEL
    }
    
    public void setRemoteHost(Expression remoteHost) {
        _remoteHost = remoteHost;
    }
    
    public void setRemoteType(RemoteType type) {
        _remoteType = type;
    }
    
    public void setRemoteText(String text) {
        _text = text;
    }

    public void setSubSequence(CommandSequence subSequence) {
        _subSequence = subSequence;
    }
    
    public void setCommand(CommandUnit command) {
        _command = command;
    }
    
    public MocaResults execute(ServerContext exec) throws MocaException {
        MocaResults result;
        if (_remoteHost != null) {
            String hostString = _remoteHost.evaluate(exec).asString();
            switch (_remoteType) {
            case INPARALLEL:
                result = exec.executeParallel(hostString, _text, true);
                break;
            case PARALLEL:
                result = exec.executeParallel(hostString, _text, false);
                break;
            default:
                result = exec.executeRemote(hostString, _text);
                break;
            }
        }
        else if (_command != null) {
            result = _command.execute(exec);
        }
        else if (_subSequence != null) {
            result = _subSequence.execute(exec);
        }
        else {
            result = exec.newResults(); 
        }
        
        return result;
        
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();
        
        if (_remoteHost != null) {
            tmp.append(_remoteType);
            tmp.append('(');
            tmp.append(_remoteHost);
            tmp.append(')');
            tmp.append(_text);
        }
        else if (_command != null) {
            tmp.append(_command);
        }
        else if (_subSequence != null) {
            tmp.append(_subSequence);
        }
        
        return tmp.toString();
    }
    
    //
    // Implementaion
    //
    private CommandUnit _command;
    private CommandSequence _subSequence;
    private Expression _remoteHost;
    private RemoteType _remoteType;
    private String _text;
}
