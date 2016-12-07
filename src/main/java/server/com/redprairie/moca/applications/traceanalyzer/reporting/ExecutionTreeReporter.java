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

import com.redprairie.moca.applications.traceanalyzer.ClientExecution;
import com.redprairie.moca.applications.traceanalyzer.Execution;
import com.redprairie.moca.applications.traceanalyzer.TraceAnalysis;
import com.redprairie.moca.applications.traceanalyzer.TraceAnalyzer;

/**
 * Prints out the full execution tree and client/network time
 * for each request in the trace.
 */
public class ExecutionTreeReporter implements Reporter {

    @Override
    public String getSectionName() {
        return "Request Execution Tree";
    }

    @Override
    public String getDescription() {
        return "Printing execution tree for requests and client/dead time";
    }

    @Override
    public void printReport(PrintStream stream, TraceAnalysis analysis) {
        for (Execution execution : analysis.getAllExecutions()) {
            if (execution instanceof ClientExecution) {
                ClientTimeReporter.printDeadTime((ClientExecution) execution, stream);
            }
            else {
                stream.println("Request time = " + TraceAnalyzer.formatTime(execution.getExecutionTime()));
                execution.printExecutionTree(stream);
                stream.println();
            }
        }
    }

}
