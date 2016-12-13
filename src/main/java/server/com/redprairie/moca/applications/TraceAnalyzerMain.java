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

package com.redprairie.moca.applications;

import com.redprairie.moca.applications.traceanalyzer.TraceAnalyzer;

/**
 * Analyzes trace files.
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TraceAnalyzerMain {
        
    /**
     * Prints out the report to standard out
     * Format is:
     * java com.redprairie.moca.applications.TraceAnalyzerMain <file>
     * @param args Arguments to the main method, see format
     */
    public static void main(String args[]) {
        // TODO - use the standard command line options class
        if (args.length != 1) {
            System.out.println("Usage: java com.redprairie.moca.applications.TraceAnalyzerMain <file>");
            return;
        }
        
        TraceAnalyzer analyzer = new TraceAnalyzer(args[0]);
        try {
            analyzer.printReport(System.out);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
