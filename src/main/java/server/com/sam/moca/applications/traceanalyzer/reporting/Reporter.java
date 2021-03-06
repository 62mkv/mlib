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

/**
 * A reporter is used to print an analysis using
 * the TraceAnalysis provided.
 */
public interface Reporter {
    
    /**
     * The name of the section for the reporter
     * @return
     */
    String getSectionName();
    
    /**
     * A description of what the reporter is printing
     * @return
     */
    String getDescription();
    
    /**
     * Prints the report to the print stream using
     * the trace analysis as a data source.
     * @param stream The print stream
     * @param analysis The trace analysis
     */
    void printReport(PrintStream stream, TraceAnalysis analysis);

}
