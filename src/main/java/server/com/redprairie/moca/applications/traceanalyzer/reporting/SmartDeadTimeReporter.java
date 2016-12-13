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
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import com.redprairie.moca.applications.traceanalyzer.ClientExecution;
import com.redprairie.moca.applications.traceanalyzer.Execution;
import com.redprairie.moca.applications.traceanalyzer.TraceAnalysis;
import com.redprairie.moca.applications.traceanalyzer.TraceAnalyzer;

/**
 * A reporter that attempts to total up client/network time by inspecting
 * client/dead time that is suspected of being attributed to the client/network (rather
 * than waiting on user interaction). Lists the probability that the detection is correct.
 */
public class SmartDeadTimeReporter implements Reporter {

    @Override
    public String getSectionName() {
        return "Detected Client/Network time";
    }

    @Override
    public String getDescription() {
        return "Looks at gaps between requests that are likely to be caused by client or network processing " +
               "time rather than by waiting for user interaction to occur. These gaps are typically under 3 seconds";
    }

    @Override
    public void printReport(PrintStream stream, TraceAnalysis analysis) {
        Set<ClientExecution> veryHighExecs = new TreeSet<ClientExecution>(Collections.reverseOrder(Execution.TOTAL_TIME_COMPARATOR));
        Set<ClientExecution> highExecs = new TreeSet<ClientExecution>(Collections.reverseOrder(Execution.TOTAL_TIME_COMPARATOR));
        Set<ClientExecution> mediumExecs = new TreeSet<ClientExecution>(Collections.reverseOrder(Execution.TOTAL_TIME_COMPARATOR));
        Set<ClientExecution> lowExecs = new TreeSet<ClientExecution>(Collections.reverseOrder(Execution.TOTAL_TIME_COMPARATOR));
        Set<ClientExecution> veryLowExecs = new TreeSet<ClientExecution>(Collections.reverseOrder(Execution.TOTAL_TIME_COMPARATOR));
        
        for (ClientExecution exec : analysis.getAllClientExecutionGaps()) {
            if (exec.getExecutionTime() < 100) {
                veryHighExecs.add(exec);
            }
            else if (exec.getExecutionTime() < 300) {
                highExecs.add(exec);
            }
            else if (exec.getExecutionTime() < 1000) {
                mediumExecs.add(exec);
            }
            else if (exec.getExecutionTime() < 3000){
                lowExecs.add(exec);
            }
            else {
                veryLowExecs.add(exec);
            }
        }
        
        long veryHighTime = 0, highTime = 0, mediumTime = 0, lowTime = 0, veryLowTime = 0;
        
        stream.println("Request gaps with a low probability of being client/network requests >= 1000ms gap\n");
        stream.println("Cross check the request gap with the requesting client to determine");
        for (ClientExecution exec : veryLowExecs) {
            printExecution(stream, exec);
            veryLowTime += exec.getExecutionTime();
        }
        
        stream.println("Request gaps with a low probability of being client/network requests - 1000ms <= request gap < 3000ms\n");
        stream.println("Cross check the request gap with the requesting client to determine");
        for (ClientExecution exec : lowExecs) {
            printExecution(stream, exec);
            lowTime += exec.getExecutionTime();
        }
        
        stream.println("Request gaps with a medium probability of being client/network requests 300ms <= request gap < 1000ms\n");
        for (ClientExecution exec : mediumExecs) {
            printExecution(stream, exec);
            mediumTime += exec.getExecutionTime();
        }
        
        stream.println("Request gaps with a high probability of being client/network requests 100ms <= request gap < 300ms\n");
        for (ClientExecution exec : highExecs) {
            printExecution(stream, exec);
            highTime += exec.getExecutionTime();
        }
        
        stream.println("Request gaps with a very high probability of being client/network requests - request gap < 100ms\n");
        for (ClientExecution exec : veryHighExecs) {
            printExecution(stream, exec);
            veryHighTime += exec.getExecutionTime();
        }
        
        stream.println("Summary statistics:");
        stream.println(String.format("Very High probability gaps (%d) = %s", veryHighExecs.size(),
            TraceAnalyzer.formatTime(veryHighTime)));
        stream.println(String.format("High probability gaps (%d) = %s (%s)", highExecs.size(),
            TraceAnalyzer.formatTime(highTime),
            TraceAnalyzer.formatTime(highTime+=veryHighTime)));
        stream.println(String.format("Medium probability gaps (%d) = %s (%s)", mediumExecs.size(),
            TraceAnalyzer.formatTime(mediumTime),
            TraceAnalyzer.formatTime(mediumTime+=highTime)));
        stream.println(String.format("Low probability gaps (%d) = %s (%s)", lowExecs.size(),
            TraceAnalyzer.formatTime(lowTime),
            TraceAnalyzer.formatTime(lowTime+=mediumTime)));
        stream.println(String.format("Very Low probability gaps (%d) = %s (%s)", veryLowExecs.size(), TraceAnalyzer.formatTime(veryLowTime),
            TraceAnalyzer.formatTime(veryLowTime + lowTime)));
    }

    private void printExecution(PrintStream stream, ClientExecution exec) {
        stream.println(String.format("Client/Network gap = %s",
            TraceAnalyzer.formatTime(exec.getExecutionTime())));
        exec.printExecution(stream);
        stream.println();
    } 
    
}
