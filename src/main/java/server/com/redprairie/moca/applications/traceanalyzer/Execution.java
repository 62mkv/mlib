/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.moca.applications.traceanalyzer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;


/**
 * Represents a gap in time in a trace file referred to as an
 * execution where we have a starting trace line and an ending trace line.
 * Executions can then have sub executions (executions called inside of them) and
 * they also can keep reference to their parent execution (what called them).
 */
public abstract class Execution {
    
    Execution(TraceLine startLine, Execution parentExecution) {
        this._startLine = startLine;
        _parentExecution = parentExecution;
    }
    
    public void done(TraceLine endLine) {
        this._endLine = endLine;
    }
    
    public long getExecutionTime() {
        return _endLine.getDate().getTime() - _startLine.getDate().getTime();
    }
    
    public TraceLine getStartLine() {
        return _startLine;
    }
    
    public TraceLine getEndLine() {
        return _endLine;
    }
    
    public Execution getParentExecution() {
        return _parentExecution;
    }
    
    public Execution getRootExecution() {
        if (getParentExecution() == null) {
            return this;
        }
        else {
            Execution last = getParentExecution();
            Execution temp = null;
            // Work up the root
            while ((temp = last.getParentExecution()) != null) {
                last = temp;
            }
            
            return last;
        }
    }
    
    public int getExecutionPercentRelativeToParent() {
        if (getParentExecution() == null) {
            return 100;
        }
        else {
            long parentTime = getParentExecution().getExecutionTime();
            return (int) (((getExecutionTime() / (parentTime * 1.0))) * 100);
        }
    }
    
    public int getExecutionPercentRelativeToRoot() {
        long rootExecTime = getRootExecution().getExecutionTime();
        return (int) (((getExecutionTime() / (rootExecTime * 1.0))) * 100);
    }
    
    public Execution getHighestSubExecution() {
        if (_subExecutions.size() == 0) return null;
        
        TreeSet<Execution> sorted = new TreeSet<Execution>(Collections.reverseOrder(TOTAL_TIME_COMPARATOR));
        sorted.addAll(_subExecutions);
        return sorted.first();
    }
    
    public synchronized long getSelfTime() {
        if (_selfTime == -1) {
            long subTime = 0;
            for (Execution exec : _subExecutions) {
                subTime += exec.getExecutionTime();
            }
            _selfTime = getExecutionTime() - subTime;
        }
        
        return _selfTime;
    }
    
    public abstract String getExecutionType();
    
    public abstract String getBeginLineDetails();
    
    public abstract String getEndLineDetails();
    
    public List<Execution> getSubExecutions() {
        return Collections.unmodifiableList(_subExecutions);
    }
    
    public void printExecution(PrintStream stream) {
        printExecutionStart(stream, false);
        printExecutionEnd(stream, false);
    }

    public void printExecutionTree(PrintStream stream) {
        printExecutionTree(stream, 0);
    }
    
    public void printExecutionTree(PrintStream stream, long executionThresholdMs) {
        printExecutionStart(stream, true);
        for (Execution subExecution : _subExecutions) {
            if (executionThresholdMs <= 0 || subExecution.getExecutionTime() >= executionThresholdMs) {
                subExecution.printExecutionTree(stream, executionThresholdMs);
            }
        }
        printExecutionEnd(stream, true);         
    }
    
    public void printExecutionTreeHotspots(PrintStream stream, int percentOfParent) {
        printExecutionStart(stream, true);
        for (Execution sub : getSubExecutionsAbovePercent(percentOfParent)) {
            stream.println(String.format("%s Relative to root (%d%%)",
                sub.getPrintLine(true), sub.getExecutionPercentRelativeToRoot()));
            sub.printExecutionTreeHotspots(stream, percentOfParent);
        }
        printExecutionEnd(stream, true);
    }
    
    public List<Execution> getSubExecutionsAbovePercent(int percent) {
        List<Execution> executions = new ArrayList<Execution>();
        for (Execution sub : _subExecutions) {
            if (sub.getExecutionPercentRelativeToParent() >= percent) {
                executions.add(sub);
            }
        }
        
        return executions;
    }
    
    // TODO - fix date parsing here
    protected void printExecutionStart(PrintStream stream, boolean indentLevel) {
        stream.println(String.format("%s [%s] - %s - %s", getPrintLine(indentLevel).toString(),
            getExecutionType(), _startLine.getDateAsString(),
            getBeginLineDetails()));
    }
    
    protected void printExecutionEnd(PrintStream stream, boolean indentLevel) {
        String endLineDetails = getEndLineDetails();
        if (endLineDetails != null) {
            stream.println(String.format("%s [%s] - %s - %s", getPrintLine(indentLevel).toString(),
                getExecutionType(), _endLine.getDateAsString(),
                endLineDetails));
        } 
    }
    
    void addSubExecution(Execution execution) {
        _subExecutions.add(execution);
    }
    
    private StringBuilder getPrintLine(boolean indentLevel) {
        StringBuilder printLine = new StringBuilder().append(_startLine.getStackLevel()).append(" ");
        
        if (indentLevel) {
            for (int i = 0; i < _startLine.getStackLevel(); i++) {
                printLine.append("-");
            }
        }
        
        return printLine;
    }
    
    public static final Comparator<Execution> SELF_TIME_COMPARATOR = new Comparator<Execution>() {

        @Override
        public int compare(Execution o1, Execution o2) {
            // TODO: Remove cast to int
            return (int) (o1.getSelfTime() - o2.getSelfTime());
        }
    };
    
    public static final Comparator<Execution> TOTAL_TIME_COMPARATOR = new Comparator<Execution>() {

        @Override
        public int compare(Execution o1, Execution o2) {
            // TODO: Remove cast to int
            return (int) (o1.getExecutionTime() - o2.getExecutionTime());
        }
    };
    
    private final TraceLine _startLine;
    private TraceLine _endLine;
    private long _selfTime = -1;
    
    private final Execution _parentExecution;
    protected final List<Execution> _subExecutions = new ArrayList<Execution>();
}

