/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2007
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

package com.redprairie.moca.server.exec;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;

public class CommandGroup implements Serializable, ExecutableComponent {
    
    public List<CommandStatement> getStatements() {
        return _statements;
    }
    
    public void addStatement(CommandStatement stmt) {
        _statements.add(stmt);
    }
    
    public MocaResults execute(ServerContext exec) throws MocaException {
        exec.setCommand(this);

        // Special case -- if a single command, don't bother accumulating results.
        if (_statements.size() == 1) {
            return _statements.get(0).execute(exec);
        }
        else {
            ResultsAccumulator accumulator = new ResultsAccumulator();
            
            for (CommandStatement statement: _statements){ 
                MocaResults res = statement.execute(exec);
                accumulator.addResults(res);
            }
            
            return accumulator.getResults();
        }
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();
        for (Iterator<CommandStatement> i = _statements.iterator(); i.hasNext();) {
            CommandStatement stmt =i.next();
            tmp.append(stmt);
            if (i.hasNext()) tmp.append(" & ");
        }
        return tmp.toString();
    }
    
    // private members
    private List<CommandStatement> _statements = new ArrayList<CommandStatement>();
    private static final long serialVersionUID = 8933270048714560737L;
}
