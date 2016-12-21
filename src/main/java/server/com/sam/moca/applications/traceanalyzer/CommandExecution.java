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

package com.sam.moca.applications.traceanalyzer;

import java.io.PrintStream;

/**
 * Represents the timespan/execution for a command
 * being called. Provides various statistics around the command
 * execution such as self app time (time spent in application code) vs self SQL
 * time (time spent in SQL statements called directly by this command) vs inherited times
 * (time spent by sub executions).
 */
public class CommandExecution extends Execution {    

    CommandExecution(String command, TraceLine startLine, Execution parentExecution) {
        super(startLine, parentExecution);
        this._command = command;
    }
    
    @Override
    public String getExecutionType() {
        return "COMMAND";
    }

    @Override
    public String getBeginLineDetails() {
        return String.format("[%s] start - Self time: (%s|%s|%d%%|%s) - Inherited time: (%s|%s|%d%%|%s) - Sub commands/sql (%d|%d|%d|%d)",
            _command.substring(0, _command.length() > 100 ? 100 : _command.length()),
            TraceAnalyzer.formatTime(getSelfTime()), TraceAnalyzer.formatTime(_sqlTimeSelf),
            getSelfSqlPercentage(), TraceAnalyzer.formatTime(getTotalSelfTime()),
            TraceAnalyzer.formatTime(getInheritedTotalAppTime()), TraceAnalyzer.formatTime(_sqlTimeTotal),
            getTotalSqlPercentage(), TraceAnalyzer.formatTime(getExecutionTime()),
            getNumberOfSubCommands(), getNumberOfSubSql(),
            getNumberOfInheritedSubCommands(), getNumberOfInheritedSubSql());
    }
    
    public String getCommand() {
        return _command;
    }
    
    public int getTotalSqlPercentage() {
        return (int) ((_sqlTimeTotal / (getExecutionTime() * 1.0)) * 100);
    }
    
    public int getSelfSqlPercentage() {
        return (int) ((_sqlTimeSelf / (getTotalSelfTime() * 1.0)) * 100);
    }
    
    public long getSelfSqlTime() {
        return _sqlTimeSelf;
    }
    
    public long getTotalSelfTime() {
        return this.getSelfTime() + _sqlTimeSelf;
    }
    
    public long getInheritedTotalAppTime() {
        return _appTimeTotal;
    }
    
    public int getNumberOfSubCommands() {
        return _numSubCommands;
    }
    
    public int getNumberOfSubSql() {
        return _numSubSql;
    }
    
    public int getNumberOfInheritedSubCommands() {
        return _numInheritedCommands;
    }
    
    public int getNumberOfInheritedSubSql() {
        return _numInheritedSql;
    }

    @Override
    public String getEndLineDetails() {
        return String.format("[%s] end", _command);
    }
    
    @Override
    public void done(TraceLine endLine) {
        super.done(endLine);
        for (Execution exec : _subExecutions) {
            if (exec instanceof SqlExecution) {
                _sqlTimeSelf += exec.getExecutionTime();
                _numSubSql++;
            }
            else if (exec instanceof CommandExecution) {
                CommandExecution commandExec = (CommandExecution) exec;
                _sqlTimeTotal += commandExec._sqlTimeTotal;
                _appTimeTotal += commandExec._appTimeTotal;
                
                // Count this sub command + its own sub commands
                _numSubCommands++;
                _numInheritedCommands += commandExec._numInheritedCommands;
                _numInheritedSql += commandExec._numInheritedSql;
            }
        }
        
        _sqlTimeTotal += _sqlTimeSelf;
        _appTimeTotal += getSelfTime();
        _numInheritedCommands += _numSubCommands;
        _numInheritedSql += _numSubSql;
    }
    
    public void printSqlStatements(PrintStream stream) {
        printExecutionStart(stream, false);
        for (Execution exec : _subExecutions) {
            if (exec instanceof SqlExecution) {
                exec.printExecution(stream);
            }
        }
        printExecutionEnd(stream, false);
    }
    
    private final String _command;
    private long _sqlTimeSelf;
    private long _sqlTimeTotal;
    private long _appTimeTotal;
    private int _numSubCommands;
    private int _numSubSql;
    private int _numInheritedCommands;
    private int _numInheritedSql;
}
