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
import com.redprairie.moca.RowIterator;

public class CommandStream implements Serializable, ExecutableComponent {
    public List<CommandGroup> getGroups() {
        return _groups;
    }
    
    public void addGroup(CommandGroup group) {
        _groups.add(group);
    }

    public MocaResults execute(ServerContext exec) throws MocaException {
        return executeOnStack(exec, 0);
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();
        for (Iterator<CommandGroup> i = _groups.iterator(); i.hasNext();) {
            CommandGroup group = i.next();
            tmp.append(group);
            if (i.hasNext()) tmp.append(" | ");
        }
        return tmp.toString();
    }

    
    // Private members
    
    private MocaResults executeOnStack(ServerContext exec, int stackLevel) throws MocaException {

        // Create a new stack frame in the current execution engine.
        exec.pushStack();
        
        try {
            // Grab the next command group to execute
            CommandGroup group = _groups.get(stackLevel);
            
            // Fire it off
            MocaResults res = group.execute(exec);
            
            // If there are more groups to execute, we need to crank up the stack level
            if (stackLevel < _groups.size() - 1) {
                
                // Within this stack level, set the result columns.
                exec.setColumns(res);
                
                // If there are no rows, we should still execute once.
                if (res.getRowCount() == 0) {
                    res = executeOnStack(exec, stackLevel + 1);
                }
                else {
                    // Execute multiple times, accumulating the results into a single
                    // result variable.
                    ResultsAccumulator accumulator = new ResultsAccumulator();
                    
                    // Execute once per row.
                    RowIterator rows = res.getRows();
                    // We have to use has next so we don't accidently increment
                    // the row iterator
                    while (rows.hasNext()) {
                        rows.next();
                        
                        // To push the row's data onto the stack, tell the context
                        // about the current row's data.
                        exec.setRow(rows);
                        
                        MocaResults tmpRes = executeOnStack(exec, stackLevel + 1);
                        
                        // Merge results -- if the original results are passed back, use them as-is.
                        accumulator.addResults(tmpRes);
                    }
                    
                    res = accumulator.getResults();
                }
            }
            
            return res;
        }
        finally {
            exec.popStack(true);
        }
    }
    private List<CommandGroup> _groups = new ArrayList<CommandGroup>();
    private static final long serialVersionUID = -3964154980691694469L;
}
