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
public class TU_FunctionSubstr extends AbstractMocaTestCase {
    public void testSubstr() throws MocaException {   
        String sql;
        
        sql = "select substr('foobar', 4) result from dual";
        runTest(sql, "result", "bar");
    
        sql = "select substr('foobar', 1, 3) result from dual";
        runTest(sql, "result", "foo");
        
        sql = "select substr('foobar', 1, 10) result from dual";
        runTest(sql, "result", "foobar");
        
        sql = "select substr('', 1, 1) result from dual";
        runTest(sql, "result", null);
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