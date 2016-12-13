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

import com.redprairie.moca.applications.traceanalyzer.Execution;
import com.redprairie.moca.applications.traceanalyzer.SqlExecution;
import com.redprairie.moca.applications.traceanalyzer.TraceAnalysis;
import com.redprairie.moca.applications.traceanalyzer.TraceAnalyzer;

/**
 * Prints the top SQL statements from the trace analysis
 * above a certain millisecond threshold.
 */
public class TopSqlStatementsReporter implements Reporter {
    
    public TopSqlStatementsReporter(long excludeBelowMs) {
        _excludeBelowMs = excludeBelowMs;
    }

    @Override
    public String getSectionName() {
        return String.format("Top Slowest SQL Statements");
    }


    @Override
    public String getDescription() {
        return String.format("Reports the top slowest SQL statements (filters out queries below %d ms)", _excludeBelowMs);
    }

    @Override
    public void printReport(PrintStream stream, TraceAnalysis analysis) {
        List<SqlExecution> sqlExecutions = new ArrayList<SqlExecution>(analysis.getAllSqlExecutions());
        Collections.sort(sqlExecutions, Collections.reverseOrder(Execution.TOTAL_TIME_COMPARATOR));
        for (SqlExecution exec : sqlExecutions) {
            if (exec.getExecutionTime() >= _excludeBelowMs) {
                stream.println("SQL statement time = " + TraceAnalyzer.formatTime(exec.getExecutionTime()));
                exec.printExecution(stream);
                stream.println(String.format("Calling command: %s", exec.getCallingCommand()));
                stream.println();
            }
        }
    }

    private final long _excludeBelowMs;
}
