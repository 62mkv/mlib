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
public class TU_FunctionToNumber extends AbstractMocaTestCase {
    public void testToNumber() throws MocaException {   
        String sql;
        
        sql = "select to_number(1) result from dual";
        runTest(sql, "result", 1);

        sql = "select to_number(1.1) result from dual";
        runTest(sql, "result", 1.1);
        
        sql = "select to_number('1') result from dual";
        runTest(sql, "result", 1);
        
        sql = "select to_number('1.2345') result from dual";
        runTest(sql, "result", 1.2345);
        
        sql = "select to_number('') result from dual";
        runTest(sql, "result", null);
        
        sql = "select to_number('123.4', '9999.99') result from dual";
        runTest(sql, "result", 123.4);
    } 
    
    private void runTest(String sql, String colname, int expected) throws MocaException {
        MocaResults results = _moca.executeCommand("[" + sql + "]");

        assertEquals(MocaType.DOUBLE, results.getColumnType(colname));

        RowIterator rowIter = results.getRows();

        assertTrue("There should be 1 row", rowIter.next());
        assertEquals(expected, rowIter.getInt(colname));
    }
   
    private void runTest(String sql, String colname, double expected) throws MocaException {
        MocaResults results = _moca.executeCommand("[" + sql + "]");

        assertEquals(MocaType.DOUBLE, results.getColumnType(colname));

        RowIterator rowIter = results.getRows();

        assertTrue("There should be 1 row", rowIter.next());
        assertEquals(expected, rowIter.getDouble(colname));
    }
    
    private void runTest(String sql, String colname, Object expected) throws MocaException {
        MocaResults results = _moca.executeCommand("[" + sql + "]");

        assertEquals(MocaType.DOUBLE, results.getColumnType(colname));

        RowIterator rowIter = results.getRows();

        assertTrue("There should be 1 row", rowIter.next());
        assertEquals(expected, rowIter.getString(colname));
    }
}