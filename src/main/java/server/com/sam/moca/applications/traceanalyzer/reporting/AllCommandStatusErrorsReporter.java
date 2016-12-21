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
import com.sam.util.Pair;

/**
 * A reporter that prints out all commands that ended in a non eOK or -1403 status.
 */
public class AllCommandStatusErrorsReporter implements Reporter {

    @Override
    public String getSectionName() {
        return "All Command Status Errors";
    }

    @Override
    public String getDescription() {
        return "Prints all commands that ended in an error status (excludes -1403)";
    }

    @Override
    public void printReport(PrintStream stream, TraceAnalysis analysis) {
        for (Pair<TraceLine, TraceLine> lines : analysis.getAllCommandErrorStatusLines()) {
            stream.println(lines.getFirst().getFullLine());
            stream.println(lines.getSecond().getFullLine());
            stream.println();
        }
    }

}
