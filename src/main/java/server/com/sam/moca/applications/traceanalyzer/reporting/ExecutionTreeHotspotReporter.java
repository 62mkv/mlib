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

package com.sam.moca.applications.traceanalyzer.reporting;

import java.io.PrintStream;

import com.sam.moca.applications.traceanalyzer.RequestExecution;
import com.sam.moca.applications.traceanalyzer.TraceAnalysis;

/**
 * Prints hotspots in the execution trees
 */
public class ExecutionTreeHotspotReporter implements Reporter {
    
    public ExecutionTreeHotspotReporter(long minimumMs, int percentOfParent) {
        _minimumMs = minimumMs;
        _percentOfParent = percentOfParent;
    }

    @Override
    public String getSectionName() {
        return "Execution Tree Hotspots";
    }

    @Override
    public String getDescription() {
        return "Prints hotspots in execution trees";
    }

    @Override
    public void printReport(PrintStream stream, TraceAnalysis analysis) {
        for (RequestExecution request : analysis.getAllRequests()) {
            if (request.getExecutionTime() >= _minimumMs) {
                request.printExecutionTreeHotspots(stream, _percentOfParent);
                stream.println();
            }
        }
    }
    
    private final long _minimumMs;
    private final int _percentOfParent;
}
