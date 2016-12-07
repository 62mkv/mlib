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
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import com.redprairie.moca.applications.traceanalyzer.Execution;
import com.redprairie.moca.applications.traceanalyzer.RequestExecution;
import com.redprairie.moca.applications.traceanalyzer.TraceAnalysis;
import com.redprairie.moca.applications.traceanalyzer.TraceAnalyzer;

/**
 * Prints all requests that were above a certain millisecond threshold.
 */
public class SlowRequestsReporter implements Reporter {
    
    public SlowRequestsReporter(long thresholdInMs) {
        _thresholdInMs = thresholdInMs;
    }

    @Override
    public String getSectionName() {
        return "Noteable Requests";
    }

    @Override
    public String getDescription() {
        return String.format("Printing requests that took at least %d ms to complete",
            _thresholdInMs);
    }

    @Override
    public void printReport(PrintStream stream, TraceAnalysis analysis) {
        Set<RequestExecution> requests = new TreeSet<RequestExecution>(Collections.reverseOrder(Execution.TOTAL_TIME_COMPARATOR));
        requests.addAll(analysis.getAllRequests());
        for (RequestExecution exec : requests) {
            if (exec.getExecutionTime() >= _thresholdInMs) {
                stream.println(String.format("Request time = %s", TraceAnalyzer.formatTime(exec.getExecutionTime())));
                exec.printExecution(stream);
                stream.println();
            }
        }
    }
    
    private final long _thresholdInMs;
}
