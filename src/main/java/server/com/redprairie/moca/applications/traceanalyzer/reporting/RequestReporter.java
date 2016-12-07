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

import com.redprairie.moca.applications.traceanalyzer.RequestExecution;
import com.redprairie.moca.applications.traceanalyzer.TraceAnalysis;
import com.redprairie.moca.applications.traceanalyzer.TraceAnalyzer;

/**
 * Reports on all the requests made from the client to
 * the server in order of execution and lists stats such
 * as how long the request took.
 */
public class RequestReporter implements Reporter {

    @Override
    public String getSectionName() {
        return "All Requests";
    }


    @Override
    public String getDescription() {
        return "Prints all requests and the associated stats but without any details (see Execution Tree Report)";
    }


    @Override
    public void printReport(PrintStream stream, TraceAnalysis analysis) {
        for (RequestExecution exec : analysis.getAllRequests()) {
            stream.println("Request time = " + TraceAnalyzer.formatTime(exec.getExecutionTime()));
            exec.printExecution(stream);
            stream.println();
        }
    }

}
