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

import com.sam.moca.applications.traceanalyzer.TraceAnalysis;
import com.sam.moca.applications.traceanalyzer.TraceLine;

/**
 * A report that prints out all logged lines that were logged with
 * a warning level or higher log level.
 */
public class AllErrorLinesReporter implements Reporter {

    @Override
    public String getSectionName() {
        return "All Error Trace Lines";
    }

    @Override
    public String getDescription() {
        return "Prints all the lines in the trace file that were logged as errors (warning or higher log level)";
    }

    @Override
    public void printReport(PrintStream stream, TraceAnalysis analysis) {
        for (TraceLine line : analysis.getAllErrorLines()) {
            stream.println(line.getFullLine());
            stream.println();
        }
    }

}
