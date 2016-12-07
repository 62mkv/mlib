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
import com.redprairie.moca.applications.traceanalyzer.TraceAnalysis;
import com.redprairie.moca.applications.traceanalyzer.TraceAnalyzer;

/**
 * A reporter that reports all execution gaps that can be attributed to
 * client/network/user interaction time which the server side trace doesn't
 * have visibility on.
 */
public class ClientTimeReporter implements Reporter {

    @Override
    public String getSectionName() {
        return "Client Processing/Network/User time";
    }

    @Override
    public String getDescription() {
        return "Printing client/network/user interaction dead time";
    }

    @Override
    public void printReport(PrintStream stream, TraceAnalysis analysis) {
        for (ClientExecution exec : analysis.getAllClientExecutionGaps()) {
            printDeadTime(exec, stream);
        }
        
        stream.println();
    }
    
    static void printDeadTime(ClientExecution exec, PrintStream stream) {
        stream.println(String.format("Dead time/client processing - %s", TraceAnalyzer.formatTime(exec.getExecutionTime())));
        stream.println(exec.getStartLine());
        stream.println(exec.getEndLine() + "\n");
    }

}
