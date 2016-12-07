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

package com.redprairie.moca.applications.traceanalyzer;

/**
 * A trace parser handles parsing lines from a trace file.
 * Implement this interface to provide parsing for different log file formats.
 */
interface TraceParser {
    
    /**
     * Indicates the given line is a new line in the trace file
     * @param line The line
     * @return
     */
    boolean isNewLine(String line);
    
    /**
     * Generates a TraceLine given the line
     * @param line The line to generate the parsed TraceLine from.
     * @return The TraceLine
     */
    TraceLine parseLine(String line);

}
