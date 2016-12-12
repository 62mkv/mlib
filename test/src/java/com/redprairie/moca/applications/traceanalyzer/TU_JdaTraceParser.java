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

import org.junit.Test;

import static org.junit.Assert.*;

public class TU_JdaTraceParser {
    
    @Test
    public void testParsingLine() {
        String line = "2013-11-13 09:48:38,763 INFO  [133 d348] DefaultServerContext [1] Tracing started 11/13/2013 at 09:48:38 []";
        TraceParser parser = new JdaTraceParser();
        assertTrue(parser.isNewLine(line));
        TraceLine parsedLine = parser.parseLine(line);
        assertEquals(133, parsedLine.getThreadId());
        assertEquals("d348", parsedLine.getSessionId());
        assertEquals("DefaultServerContext", parsedLine.getLogger());
        assertEquals("Tracing started 11/13/2013 at 09:48:38", parsedLine.getMessage());
        assertEquals(1, parsedLine.getStackLevel());
        assertEquals("2013-11-13 09:48:38,763", parsedLine.getDateAsString());
    }
    
    @Test
    public void testSmallerThreadId() {
        // Thread ID is fixed length so it can be followed by multiple spaces until the session ID
        String line = "2013-11-13 09:48:46,970 DEBUG [1   d348] CommandDispatcher [0] Server got: set trace where activate = 0 []";
        TraceParser parser = new JdaTraceParser();
        assertTrue(parser.isNewLine(line));
        TraceLine parsedLine = parser.parseLine(line);
        assertEquals(1, parsedLine.getThreadId());
        assertEquals("d348", parsedLine.getSessionId());
        assertEquals("CommandDispatcher", parsedLine.getLogger());
        assertEquals("Server got: set trace where activate = 0", parsedLine.getMessage());
        assertEquals(0, parsedLine.getStackLevel());
        assertEquals("2013-11-13 09:48:46,970", parsedLine.getDateAsString());
    }
    
    @Test
    public void testMultiLine() {
        // Test detecting for new lines when the log message gets split up across multiple lines
        String line1 = "2013-11-13 08:30:27,037 DEBUG [135 b424] JDBCAdapter [2] Executing SQL: select 1";
        String line2 = "         from poldat_view []";
        String line3 = "2013-11-13 08:30:27,037 DEBUG [135 b424] JDBCAdapter [2] XLATE: select :i0";
        
        TraceParser parser = new JdaTraceParser();
        assertTrue(parser.isNewLine(line1));
        assertFalse(parser.isNewLine(line2));
        assertTrue(parser.isNewLine(line3));
        
        TraceLine parsedLine = parser.parseLine(line1 + line2);
        assertEquals(135, parsedLine.getThreadId());
        assertEquals("b424", parsedLine.getSessionId());
        assertEquals("JDBCAdapter", parsedLine.getLogger());
        assertEquals("Executing SQL: select 1         from poldat_view", parsedLine.getMessage());
        assertEquals(2, parsedLine.getStackLevel());
        assertEquals("2013-11-13 08:30:27,037", parsedLine.getDateAsString());
    }
    
    @Test
    public void testLineWithoutLogMessage() {
        
        String line = "2013-11-13 09:48:38,767 INFO  [133 d348] DefaultServerContext [1]  []";
        TraceParser parser = new JdaTraceParser();
        assertTrue(parser.isNewLine(line));
        TraceLine parsedLine = parser.parseLine(line);
        assertNotNull(parsedLine);
        assertEquals(133, parsedLine.getThreadId());
        assertEquals("d348", parsedLine.getSessionId());
        assertEquals("DefaultServerContext", parsedLine.getLogger());
        assertEquals("", parsedLine.getMessage());
        assertEquals(1, parsedLine.getStackLevel());
        assertEquals("2013-11-13 09:48:38,767", parsedLine.getDateAsString());
    }

}
