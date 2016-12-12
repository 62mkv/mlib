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
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author mlange
 */
public class TU_FunctionAddMonths extends AbstractMocaTestCase {
    public void testAddMonths() throws Exception {   
        String sql;
        DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyyMMdd");
        sql = "select add_months(to_date('20100101', 'yyyymmdd'), 0) result from dual";
        runTest(sql, "result", dateFormat.parseDateTime("20100101").toDate());
        
        sql = "select add_months(to_date('20100101', 'yyyymmdd'), 2) result from dual";
        runTest(sql, "result", dateFormat.parseDateTime("20100301").toDate());
        
        sql = "select add_months(to_date('20100101', 'yyyymmdd'), -2) result from dual";
        runTest(sql, "result", dateFormat.parseDateTime("20091101").toDate());
    } 
    
    private void runTest(String sql, String colname, Date expected) throws MocaException {
        MocaResults results = _moca.executeCommand("[" + sql + "]");

        assertEquals(MocaType.DATETIME, results.getColumnType(colname));

        RowIterator rowIter = results.getRows();

        assertTrue("There should be 1 row", rowIter.next());
        assertEquals(expected, rowIter.getDateTime(colname));
    }
}