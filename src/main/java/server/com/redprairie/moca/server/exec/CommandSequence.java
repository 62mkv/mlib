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

package com.redprairie.moca.server.exec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;

public class CommandSequence implements Serializable, ExecutableComponent {

    public List<CommandStream> getStreams() {
        return _streams;
    }
    
    public void addStream(CommandStream stream) {
        _streams.add(stream);
    }
    
    public MocaResults execute(ServerContext exec) throws MocaException {
        MocaResults res = null;
        
        for (Iterator<CommandStream> i = _streams.iterator(); i.hasNext();) {
            CommandStream stream = i.next();
            res = stream.execute(exec);
            
            // If there's more to execute, we clear the current stack level's
            // error state.
            if (i.hasNext()) {
                exec.clearError();
            }
        }
        return res;
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder("{");
        for (Iterator<CommandStream> i = _streams.iterator(); i.hasNext();) {
            CommandStream stream = i.next();
            tmp.append(stream);
            if (i.hasNext()) tmp.append("; ");
        }
        tmp.append('}');
        return tmp.toString();
    }
    

    // Private members
    private final List<CommandStream> _streams = new ArrayList<CommandStream>();
    private static final long serialVersionUID = 6843629792467515246L;
}
