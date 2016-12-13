/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.server.db.translate.filter;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.util.AbstractMocaTestCase;


/**
 * TODO Class Description
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author mlange
 */
public class TU_FunctionToChar extends AbstractMocaTestCase {
    public void testNumbers() throws MocaException {   
        String sql;
        
        sql = "select to_char(1) result from dual";
        runTest(sql, "result", "1");

        sql = "select to_char(1.2) result from dual";
        runTest(sql, "result", "1.2");

        sql = "select to_char(-1) result from dual";
        runTest(sql, "result", "-1");
        
        sql = "select to_char(-1.2) result from dual";
        runTest(sql, "result", "-1.2");
        
        sql = "select to_char(-123456789, '999999999') result from dual";
        runTest(sql, "result", "-123456789");
        
        sql = "select to_char(123456789, '999999999') result from dual";
        runTest(sql, "result", "123456789");    
        
        sql = "select to_char(+0.1, '90.99') result from dual";
        runTest(sql, "result", "0.10");
        
        sql = "select to_char(-0.2, '90.99') result from dual";
        runTest(sql, "result", "-0.20");
        
        sql = "select to_char(+123.456, '999.999') result from dual";
        runTest(sql, "result", "123.456");
        
        sql = "select to_char(-123.456, '999.999') result from dual";
        runTest(sql, "result", "-123.456");
    } 
    
    public void testDates() throws MocaException {   
        String sql;
        
        sql = "select to_char(to_date('20100123 01:23:45', 'YYYYMMDD HH24:MI:SS'), 'YYYY') result from dual";
        runTest(sql, "result", "2010");
        
        sql = "select to_char(to_date('20100123 01:23:45', 'YYYYMMDD HH24:MI:SS'), 'YYY') result from dual";
        runTest(sql, "result", "010");
        
        sql = "select to_char(to_date('20100123 01:23:45', 'YYYYMMDD HH24:MI:SS'), 'YY') result from dual";
        runTest(sql, "result", "10");
        
        sql = "select to_char(to_date('20100123 01:23:45', 'YYYYMMDD HH24:MI:SS'), 'Y') result from dual";
        runTest(sql, "result", "0");
        
        sql = "select to_char(to_date('20100123 01:23:45', 'YYYYMMDD HH24:MI:SS'), 'Q') result from dual";
        runTest(sql, "result", "1");
        
        sql = "select to_char(to_date('20100123 01:23:45', 'YYYYMMDD HH24:MI:SS'), 'MM') result from dual";
        runTest(sql, "result", "01");      
        
        sql = "select to_char(to_date('20100123 01:23:45', 'YYYYMMDD HH24:MI:SS'), 'MONTH') result from dual";
        runTest(sql, "result", "JANUARY");  
        
        sql = "select to_char(to_date('20100123 01:23:45', 'YYYYMMDD HH24:MI:SS'), 'DD') result from dual";
        runTest(sql, "result", "23");
        
        sql = "select to_char(to_date('20100123 01:23:45', 'YYYYMMDD HH24:MI:SS'), 'DDD') result from dual";
        runTest(sql, "result", "023");
        
        sql = "select to_char(to_date('20100123 01:23:45', 'YYYYMMDD HH24:MI:SS'), 'DAY') result from dual";
        runTest(sql, "result", "SATURDAY");
        
        sql = "select to_char(to_date('20100123 01:23:45', 'YYYYMMDD HH24:MI:SS'), 'HH24') result from dual";
        runTest(sql, "result", "01");
        
        sql = "select to_char(to_date('20100123 01:23:45', 'YYYYMMDD HH24:MI:SS'), 'MI') result from dual";
        runTest(sql, "result", "23");
        
        sql = "select to_char(to_date('20100123 01:23:45', 'YYYYMMDD HH24:MI:SS'), 'SS') result from dual";
        runTest(sql, "result", "45");
    } 
    
    private void runTest(String sql, String colname, String expected) throws MocaException {
        MocaResults results = _moca.executeCommand("[" + sql + "]");

        assertEquals(MocaType.STRING, results.getColumnType(colname));

        RowIterator rowIter = results.getRows();

        assertTrue("There should be 1 row", rowIter.next());
        
        if (expected == null) {
            assertTrue(rowIter.isNull(colname));
        }
        else {
            assertEquals(expected, rowIter.getString(colname).trim());
        }
    }
}