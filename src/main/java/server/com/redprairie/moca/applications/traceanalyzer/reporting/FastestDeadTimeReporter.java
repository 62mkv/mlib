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

import com.redprairie.moca.applications.traceanalyzer.ClientExecution;
import com.redprairie.moca.applications.traceanalyzer.Execution;
import com.redprairie.moca.applications.traceanalyzer.TraceAnalysis;
import com.redprairie.moca.applications.traceanalyzer.TraceAnalyzer;

/**
 * Prints the smallest gaps in execution between the client and server.
 */
public class FastestDeadTimeReporter implements Reporter {
    
    public FastestDeadTimeReporter(int topX) {
        _topX = topX;
    }

    @Override
    public String getSectionName() {
        return "Fastest Client Time Gaps";
    }

    @Override
    public String getDescription() {
        return String.format("Returns the top %d smallest client gaps in time." + 
                   "%nThis can sometimes be used as an indicator that client/network processing is very slow if the smallest gaps%n are still very large", _topX);
    }

    @Override
    public void printReport(PrintStream stream, TraceAnalysis analysis) {
        List<ClientExecution> clientGaps = new ArrayList<ClientExecution>(analysis.getAllClientExecutionGaps());
        Collections.sort(clientGaps, Execution.TOTAL_TIME_COMPARATOR);
        for (int i = 0; i < _topX && i < clientGaps.size(); i++) {
            ClientExecution exec = clientGaps.get(i);
            stream.println("Client/network time = " + TraceAnalyzer.formatTime(exec.getExecutionTime()));
            exec.printExecution(stream);
            stream.println();
        }
    }
    
    private final int _topX;

}
