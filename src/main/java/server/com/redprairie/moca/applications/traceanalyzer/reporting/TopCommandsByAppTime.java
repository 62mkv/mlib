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

package com.redprairie.moca.applications.traceanalyzer.reporting;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.redprairie.moca.applications.traceanalyzer.CommandExecution;
import com.redprairie.moca.applications.traceanalyzer.Execution;
import com.redprairie.moca.applications.traceanalyzer.TraceAnalysis;
import com.redprairie.moca.applications.traceanalyzer.TraceAnalyzer;

/**
 * Prints out the top commands by self application processing
 * time and excludes commands below a certain millisecond threshold.
 */
public class TopCommandsByAppTime implements Reporter {
    
    public TopCommandsByAppTime(long excludeBelowMs) {
        _excludeBelowMs = excludeBelowMs;
    }

    @Override
    public String getSectionName() {
        return "Top Slowest Commands by Self Application Time";
    }

    @Override
    public String getDescription() {
        return String.format("Reports the top slowest commands by self application time (excludes commands below %d ms)", _excludeBelowMs);
    }

    @Override
    public void printReport(PrintStream stream, TraceAnalysis analysis) {
        List<CommandExecution> commandExecutions = new ArrayList<CommandExecution>(analysis.getAllCommandExecutions());
        Collections.sort(commandExecutions, Collections.reverseOrder(Execution.SELF_TIME_COMPARATOR));
        for (CommandExecution command : commandExecutions) {
            if (command.getSelfTime() >= _excludeBelowMs) {
                stream.println("Command Application Time = " + TraceAnalyzer.formatTime(command.getSelfTime()));
                command.printExecution(stream);
                stream.println();
            }
        }
    }

    private final long _excludeBelowMs;

}
