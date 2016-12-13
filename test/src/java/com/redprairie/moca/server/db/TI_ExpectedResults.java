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

package com.redprairie.moca.server.db;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * Test for evaluating expected results from SQL statements.
 */
public class TI_ExpectedResults extends AbstractMocaTestCase {

    public void testNvlOfNull() throws MocaException {   
        String sql = "[select nvl('', 'xx') foo from dual]";
        runTest(sql, "foo", "xx");
    }
    
    public void testNvlOfNonNull() throws MocaException {   
        String sql = "[select nvl('yy', 'xx') foo from dual]";
        runTest(sql, "foo", "yy");
    }
    
    public void testToCharToDateJulian() throws MocaException {
        String target = "1753-01-01 00:00:00";
        String result = "1753-01-01 00:00:00";
        testJulian(target, result);
    }
    
    public void testToCharToDateJulian1() throws MocaException {
        String target = "2012-01-26 00:00:00";
        String result = "2012-01-26 00:00:00";
        testJulian(target, result);
    }
    
    public void testToCharToDateJulian2() throws MocaException {
        String target = "2012-01-26 00:00:01";
        String result = "2012-01-26 00:00:00";
        testJulian(target, result);
    }
    
    public void testToCharToDateJulian3() throws MocaException {
        String target = "2012-01-26 23:59:59";
        String result = "2012-01-26 00:00:00";
        testJulian(target, result);
    }
    
    public void testToCharToDateJulian4() throws MocaException {
        String target = "2012-01-26 00:01:00";
        String result = "2012-01-26 00:00:00";
        testJulian(target, result);
    }
    
    public void testToCharToDateJulian5() throws MocaException {
        String target = "2012-01-26 00:05:36";
        String result = "2012-01-26 00:00:00";
        testJulian(target, result);
    }
    
    // 5:38 is the date at which the time changes over for subtraction
    public void testToCharToDateJulian6() throws MocaException {
        String target = "2012-01-26 00:05:37";
        String result = "2012-01-26 00:00:00";
        testJulian(target, result);
    }
    
    public void testToCharToDateJulian7() throws MocaException {
        String target = "2012-01-26 23:59:59";
        String result = "2012-01-26 00:00:00";
        testJulian(target, result);
    }
    
    private void testJulian(String target, String result) throws MocaException {
        String sql = 
                "publish data" +
                "  where mydte = to_date('" + target + "', 'YYYY-MM-DD HH24:MI:SS')" +
                "|" +
                "[select to_char(@mydte:date, 'J') mydtechr" +
                "   from dual]" +
                "|" +
                "[select to_date(@mydtechr, 'J') juldte" +
                "   from dual]" +
                "|" +
                "publish data" +
                "  where returneddte = to_char(@juldte, 'YYYY-MM-DD HH24:MI:SS')";
        runTest(sql, "returneddte", result);
    }

    private void runTest(String command, String colname, String expected) throws MocaException {
        MocaResults results = _moca.executeCommand(command);

        assertEquals(MocaType.STRING, results.getColumnType(colname));

        RowIterator rowIter = results.getRows();

        assertTrue("There should be 1 row", rowIter.next());
        assertEquals(expected, rowIter.getString(colname));
    }
}