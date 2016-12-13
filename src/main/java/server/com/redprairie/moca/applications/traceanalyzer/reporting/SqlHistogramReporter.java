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

import com.redprairie.moca.applications.traceanalyzer.SqlExecution;
import com.redprairie.moca.applications.traceanalyzer.TraceAnalysis;

/**
 * Prints a SQL histogram (break down of SQL requests time in buckets)
 */
public class SqlHistogramReporter implements Reporter {

    @Override
    public String getSectionName() {
        return "SQL Times Histogram";
    }

    @Override
    public String getDescription() {
        return "A histogram breakdown of SQL query times";
    }

    @Override
    public void printReport(PrintStream stream, TraceAnalysis analysis) {
        int under10ms = 0;
        int under100ms = 0;
        int under1000ms = 0;
        int over1s = 0;
        
        for (SqlExecution sql : analysis.getAllSqlExecutions()) {
            if (sql.getExecutionTime() < 10) {
                under10ms++;
            }
            else if (sql.getExecutionTime() < 100) {
                under100ms++;
            }
            else if (sql.getExecutionTime() < 1000) {
                under1000ms++;
            }
            else {
                over1s++;
            }
        }
        
        stream.println(String.format(" 0ms   ---> 10ms\t | %d (%.2f%%)", under10ms, getPercent(under10ms, analysis)));
        stream.println(String.format(" 10ms  ---> 100ms\t | %d (%.2f%%)", under100ms, getPercent(under100ms, analysis)));
        stream.println(String.format(" 100ms ---> 1000ms\t | %d (%.2f%%)", under1000ms, getPercent(under1000ms, analysis)));
        stream.println(String.format(" 1000ms >  \t\t\t | %d (%.2f%%)", over1s, getPercent(over1s, analysis)));
        stream.println();
    }
    
    private double getPercent(int value, TraceAnalysis analysis) {
        return ((value * 1.0)/analysis.getNumberOfSqlExecutions()) * 100;
    }

}
