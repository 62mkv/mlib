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
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import com.sam.moca.applications.traceanalyzer.Execution;
import com.sam.moca.applications.traceanalyzer.TraceAnalysis;
import com.sam.moca.applications.traceanalyzer.TraceAnalyzer;
import com.sam.moca.applications.traceanalyzer.TracePause;

/**
 * Reports on irregular pauses which are pauses that can't be attributed
 * to SQL statements or anything we already know about. Typically this could be
 * garbage collection pauses or blocking issues.
 */
public class IrregularPausesReporter implements Reporter {

    public IrregularPausesReporter(long pauseThreshold) {
        _pauseThreshold = pauseThreshold;
    }

    @Override
    public String getSectionName() {
        return "Irregular Pauses";
    }

    @Override
    public String getDescription() {
        return String.format("Prints information about pauses in the trace above %d ms that are irregular", _pauseThreshold);
    }

    @Override
    public void printReport(PrintStream stream, TraceAnalysis analysis) {
        Set<TracePause> pauses = new TreeSet<TracePause>(Collections.reverseOrder(Execution.TOTAL_TIME_COMPARATOR));
        pauses.addAll(analysis.getAllPausesAboveThreshold());
        for (TracePause pause : pauses) {
            stream.println(String.format("Irregular pause = %s", TraceAnalyzer.formatTime(pause.getExecutionTime())));
            pause.printExecution(stream);
            stream.println();
        }
    }
    
    private final long _pauseThreshold;
}
