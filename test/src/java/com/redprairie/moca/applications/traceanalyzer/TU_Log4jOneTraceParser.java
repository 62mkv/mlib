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

package com.redprairie.moca.applications.traceanalyzer;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the Log4j one trace line parsing via the regex.
 */
public class TU_Log4jOneTraceParser {
    
    @Test
    public void testParseLine() {
        TraceParser parser = new Log4jOneTraceParser();
        TraceLine parsedLine = parser.parseLine(
                   "25  4f67 D 17:52:05,737 (DefaultSer) [5] Resuming execution of MCSbase/get policy areas");
        assertEquals(25, parsedLine.getThreadId());
        assertEquals("4f67", parsedLine.getSessionId());
        assertEquals("D", parsedLine.getLogLevel());
        assertEquals("DefaultSer", parsedLine.getLogger());
        assertEquals(5, parsedLine.getStackLevel());
        assertEquals("Resuming execution of MCSbase/get policy areas", parsedLine.getMessage());
        
    }
    
    @Test
    public void testParseLine2() {
        TraceParser parser = new Log4jOneTraceParser();
        TraceLine parsedLine = parser.parseLine(
                   "27  28ce D 14:00:56,087 (JDBCAdapte) [1] UNBIND: select les_usr_ath.*,  moca_util.date_diff_days(sysdate,   les_usr_ath.pswd_chg_dat) pswd_expir   from les_usr_ath  where (usr_id = 'SUPER' or UPPER(login_id) = 'SUPER')");
        assertEquals(27, parsedLine.getThreadId());
        assertEquals("28ce", parsedLine.getSessionId());
        assertEquals("JDBCAdapte", parsedLine.getLogger());
        assertEquals(1, parsedLine.getStackLevel());
        assertEquals("UNBIND: select les_usr_ath.*,  moca_util.date_diff_days(sysdate,   les_usr_ath.pswd_chg_dat) pswd_expir   from les_usr_ath  where (usr_id = 'SUPER' or UPPER(login_id) = 'SUPER')", parsedLine.getMessage());
        
    }
    
    @Test
    public void testLineMissingStackLevel() {
        TraceParser parser = new Log4jOneTraceParser();
        TraceLine parsedLine = parser.parseLine("27  28ce D 14:00:56,087 (CommandDis) Creating new ServerContext");
        assertEquals(27, parsedLine.getThreadId());
        assertEquals("28ce", parsedLine.getSessionId());
        assertEquals("CommandDis", parsedLine.getLogger());
        assertEquals("Creating new ServerContext", parsedLine.getMessage());
        assertEquals(0, parsedLine.getStackLevel());
    }
    
    @Test
    public void testLineWithLargerThreadId() {
        // Larger thread ID means only once space between the thread ID and session ID since it's min of 3 but no max length.
        TraceParser parser = new Log4jOneTraceParser();
        TraceLine parsedLine = parser.parseLine("13104 95b2 I 14:53:42,604 (DefaultSer) [2] Tracing started 10/21/2013 at 14:53:42");
        assertEquals(13104, parsedLine.getThreadId());
        assertEquals("95b2", parsedLine.getSessionId());
        assertEquals("DefaultSer", parsedLine.getLogger());
        assertEquals("Tracing started 10/21/2013 at 14:53:42", parsedLine.getMessage());
        assertEquals(2, parsedLine.getStackLevel());
        
    }
    
    @Test
    public void testSingleDigitThreadId() {
        // Single digit thread ID means 1 for the digit + 2 spaces to hit the 3 minimum width
        TraceParser parser = new Log4jOneTraceParser();
        TraceLine parsedLine = parser.parseLine("1   95b2 I 14:53:42,604 (DefaultSer) [2] Tracing started 10/21/2013 at 14:53:42");
        assertEquals(1, parsedLine.getThreadId());
        assertEquals("95b2", parsedLine.getSessionId());
        assertEquals("DefaultSer", parsedLine.getLogger());
        assertEquals("Tracing started 10/21/2013 at 14:53:42", parsedLine.getMessage());
        assertEquals(2, parsedLine.getStackLevel());
    }

}
