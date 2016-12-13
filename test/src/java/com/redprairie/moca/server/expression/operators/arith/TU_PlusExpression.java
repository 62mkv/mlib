/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.redprairie.moca.server.expression.operators.arith;

import java.util.Date;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * Test for evaluating division expressions in MOCA.
 */
public class TU_PlusExpression extends AbstractMocaTestCase {

    public void testIntegerAddition() throws MocaException {
        runTest("100", "1", 101);
        runTest("0", "-2", -2);
        runTest("100", "200", 300);
    }
    
    public void testDoubleAddition() throws MocaException {
        runTest("100.6", "1.2", 101.8);
        runTest("0.0", "-2", -2.0);
        runTest("100", "2.2", 102.2);
    }
    
    public void testStringAddition() throws MocaException {
        runTest("'100'", "'12'", 112);
        runTest("'100'", "0", 100);
    }
    
    public void testAdditionWithNull() throws MocaException {
        runTest("100", "null", 100);
        runTest("'101'", "null", 101);
        runTest("null", "102", 102);
        runTest("null", "'102'", 102);
    }
    
    public void testDateAdditionWithNullAndNumber() throws MocaException {
        runTestExpectingDate("date(null)", "2", null);
        runTestExpectingDate("2", "date(null)", null);
    }
    
    public void testDateAdditionWithNumber() throws MocaException {
        runTestExpectingDate("date('20100228120000')", "1", "20100301120000");
        runTestExpectingDate("date('20120228120000')", "2", "20120301120000");
        runTestExpectingDate("date('20091231120000')", "1.5", "20100102000000");
        runTestExpectingDate("1", "date('20100228120000')", "20100301120000");
        runTestExpectingDate("2", "date('20120228120000')", "20120301120000");
        runTestExpectingDate("1.5", "date('20091231120000')", "20100102000000");
    }

    // 
    public void testDateAdditionWithNegativeNumber() throws MocaException {
        runTestExpectingDate("date('20100301120000')", "-1", "20100228120000");
        runTestExpectingDate("date('20120301120000')", "-2", "20120228120000");
        runTestExpectingDate("date('20100102000000')", "-1.5", "20091231120000");
        runTestExpectingDate("-1", "date('20100301120000')", "20100228120000");
        runTestExpectingDate("-2", "date('20120301120000')", "20120228120000");
        runTestExpectingDate("-1.5", "date('20100102000000')", "20091231120000");
    }
    
    public void testDateAdditionOverDst() throws MocaException {
        runTestExpectingDate("date('20100313120000')", "1", "20100314120000");
        runTestExpectingDate("date('20100313120000')", "(2.0/3)", "20100314040000");
        runTestExpectingDate("1", "date('20100313120000')", "20100314120000");
        runTestExpectingDate("(2.0/3)", "date('20100313120000')", "20100314040000");
    }
    
    private void runTest(String a, String b, Object expected) throws MocaException {
        
        MocaResults res = _moca.executeCommand(
            "publish data where result = " + a + " + " + b);
        
        RowIterator rowIter = res.getRows();
        rowIter.next();
        
        if (expected == null) {
            assertNull(rowIter.getValue("result"));
        }
        else {
            assertEquals(expected, rowIter.getValue("result"));
        }
    }
    
    private void runTestExpectingDate(String a, String b, String expectedDate) throws MocaException {
        
        MocaResults res = _moca.executeCommand(
            "publish data where result = " + a + " + " + b);
        
        RowIterator rowIter = res.getRows();
        rowIter.next();
        
        DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYYMMddHHmmss");
        
        if (expectedDate == null) {
            assertNull(rowIter.getValue("result"));
        }
        else {
            Date expected = formatter.parseDateTime(expectedDate).toDate();
            assertEquals(expected, rowIter.getDateTime("result"));
        }
    }
}
