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

import java.util.Date;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
public class TU_FunctionToDate extends AbstractMocaTestCase {
    public void testToDate() throws Exception {   
        String sql;
        
        DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyyMMddHHmmss");

        sql = "select to_date('20100123012345', 'YYYYMMDDHH24MISS') result from dual";
        runTest(sql, "result", dateFormat.parseDateTime("20100123012345").toDate());

        sql = "select to_date('20100123 01:23:45', 'YYYYMMDD HH24:MI:SS') result from dual";
        runTest(sql, "result", dateFormat.parseDateTime("20100123012345").toDate());
        
        sql = "select to_date('20100123', 'YYYYMMDD') result from dual";
        runTest(sql, "result", dateFormat.parseDateTime("20100123000000").toDate());

        sql = "select to_date('20100123') result from dual";
        runTest(sql, "result", dateFormat.parseDateTime("20100123000000").toDate());
        
        sql = "select to_date('20100123123456') result from dual";
        runTest(sql, "result", dateFormat.parseDateTime("20100123123456").toDate());
    } 
    
    private void runTest(String sql, String colname, Date expected) throws MocaException {
        MocaResults results = _moca.executeCommand("[" + sql + "]");

        assertEquals(MocaType.DATETIME, results.getColumnType(colname));

        RowIterator rowIter = results.getRows();

        assertTrue("There should be 1 row", rowIter.next());
        assertEquals(expected, rowIter.getDateTime(colname));
    }
}