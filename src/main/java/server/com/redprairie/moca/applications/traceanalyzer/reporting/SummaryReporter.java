/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.moca.applications.traceanalyzer.reporting;

import java.io.PrintStream;

import com.redprairie.moca.applications.traceanalyzer.TraceAnalysis;
import com.redprairie.moca.applications.traceanalyzer.TraceAnalyzer;

/**
 * Prints a summary of the statistics gathered in the Trace Analysis
 */
public class SummaryReporter implements Reporter {
    
    public SummaryReporter(long parseStartTime, String file) {
        _parseTimeStart = parseStartTime;
        _file = file;
    }

    @Override
    public String getSectionName() {
        return "Summary";
    }

    @Override
    public String getDescription() {
        return "Printing trace file summary information";
    }

    @Override
    public void printReport(PrintStream stream, TraceAnalysis analysis) {
        stream.println("Took " + TraceAnalyzer.formatTime(System.currentTimeMillis() - _parseTimeStart)
            + " to parse the log file: " + _file);
        stream.println(String.format("Total client/dead time - %s - between %d requests - ignores first and last request", TraceAnalyzer.formatTime(analysis.getTotalClientTime()), analysis.getNumerOfRequests()));
        stream.println(String.format("Total of %d requests for a total time of %s", analysis.getNumerOfRequests(), TraceAnalyzer.formatTime(analysis.getTotalRequestTime())));
        stream.println(String.format("Total command executions - %d - for a total time of %s - %s app time | %s sql time",
            analysis.getNumberOfCommandsExecuted(),
            TraceAnalyzer.formatTime(analysis.getTotalCommandTime()),
            TraceAnalyzer.formatTime(analysis.getTotalCommandAppTime()),
            TraceAnalyzer.formatTime(analysis.getTotalCommandSqlTime())));
        stream.println("Total SQL executions - " + analysis.getNumberOfSqlExecutions() + " - for a total time of " + TraceAnalyzer.formatTime(analysis.getTotalSqlExecutionTime()));
        stream.println();
    }

    private final long _parseTimeStart;
    private final String _file;
}
