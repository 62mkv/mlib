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
import java.util.List;

import com.redprairie.moca.applications.traceanalyzer.TraceAnalysis;

/**
 * Given a list of reporters prints out a table of
 * contents to the print stream for the whole report.
 */
public class TableOfContentsReporter implements Reporter {
    
    public TableOfContentsReporter(List<Reporter> reporters) {
        StringBuilder sections = new StringBuilder();
        int section = 1;
        for (Reporter reporter : reporters) {
            sections.append(section).append(". ").append(reporter.getSectionName()).append("\n");
            section++;
        }
        
        _description = sections.toString();
    }

    @Override
    public String getSectionName() {
        return "Table of Contents";
    }

    @Override
    public String getDescription() {
        return _description;
    }

    @Override
    public void printReport(PrintStream stream, TraceAnalysis analysis) {
    }

    private final String _description;
}
