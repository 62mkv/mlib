/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.server.db.translate.filter;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.util.AbstractMocaTestCase;


/**
 * TODO Class Description
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author mlange
 */
public class TU_FunctionReplace extends AbstractMocaTestCase {
    public void testReplace() throws MocaException {   
        String sql;
        
        sql = "select replace('foobar', 'foo', 'fu') result from dual";
        runTest(sql, "result", "fubar");

        sql = "select replace('foobar', 'foobar', 'fubar') result from dual";
        runTest(sql, "result", "fubar");
        
        sql = "select replace('foobar', 'fuu', 'fu') result from dual";
        runTest(sql, "result", "foobar");
        
        sql = "select replace('foobar', 'foobarx', 'foobar') result from dual";
        runTest(sql, "result", "foobar");
        
        sql = "select replace(null, 'foo', 'fu') result from dual";
        runTest(sql, "result", null);
        
        sql = "select replace('', 'foo', 'fu') result from dual";
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