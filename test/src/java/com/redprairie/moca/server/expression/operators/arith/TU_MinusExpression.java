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
public class TU_MinusExpression extends AbstractMocaTestCase {

    public void testIntegerSubtraction() throws MocaException {
        runTest("100", "1", 99);
        runTest("0", "-2", 2);
        runTest("100", "200", -100);
    }
    
    public void testDateSubtraction() throws MocaException {
        runTest("date('20100101000000')", "date('20100101000000')", 0.0);
        runTest("date('20100101000000')", "date('20091231000000')", 1.0);
        runTest("date('20100101000000')", "date('20090101000000')", 365.0);
    }
    
    public void testDateSubtractionWithPartialDays() throws MocaException {
        runTest("date('20100101000000')", "date('20091231120000')", 0.5);
        runTest("date('20100102000000')", "date('20091231120000')", 1.5);
        runTest("date('20091231120000')", "date('20100101000000')", -0.5);
        runTest("date('20091231120000')", "date('20100102000000')", -1.5);
        
        runTest("date('20100101120000')", "date('20091231000000')", 1.5);
        runTest("date('20100102120000')", "date('20091231000000')", 2.5);
        runTest("date('20091231000000')", "date('20100101120000')", -1.5);
        runTest("date('20091231000000')", "date('20100102120000')", -2.5);
        
        // Three dates.  Each on different days:
        // A: day zero, at noon.
        // B: day one, at 6 am.
        // C: day two, at 6 pm.
        String a = "date('20100401120000')";
        String b = "date('20100402060000')";
        String c = "date('20100403180000')";

        runTest(b, a, 0.75);
        runTest(c, a, 2.25);
        runTest(c, b, 1.5);
        runTest(a, b, -0.75);
        runTest(a, c, -2.25);
        runTest(b, c, -1.5);
    }

    public void testDateSubtractionOverDst() throws MocaException {
        runTest("date('20100314120000')", "date('20100313120000')", 1.0);
    }
    
    public void testDateSubtractionOverLeapYear() throws MocaException {
        runTest("date('20100301120000')", "date('20100228120000')", 1.0);
        runTest("date('20120301120000')", "date('20120228120000')", 2.0);
    }
    
    public void testDateSubtractionWithNull() throws MocaException {
        runTest("date(null)", "date('20100106000000')", null);
        runTest("date('20100106000000')", "date(null)", null);
    }
    
    public void testDateSubtractionWithNullAndNumber() throws MocaException {
        runTestExpectingDate("date(null)", "2", null);
    }
    
    public void testDateSubtractionWithNumber() throws MocaException {
        runTestExpectingDate("date('20100301120000')", "1", "20100228120000");
        runTestExpectingDate("date('20120301120000')", "2", "20120228120000");
        runTestExpectingDate("date('20100102000000')", "1.5", "20091231120000");
    }
    
    public void testDateSubtractionWithNumberOverDst() throws MocaException {
        runTestExpectingDate("date('20100314120000')", "1", "20100313120000");
        runTestExpectingDate("date('20100314120000')", "0.5", "20100314000000");
        runTestExpectingDate("date('20100315120000')", "1.5", "20100314000000");
    }
    
    private void runTest(String a, String b, Object expected) throws MocaException {
        
        MocaResults res = _moca.executeCommand(
            "publish data where result = " + a + " - " + b);
        
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
            "publish data where result = " + a + " - " + b);
        
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
