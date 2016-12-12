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

import org.joda.time.DateTime;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
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
public class TU_FunctionMonthsBetween extends AbstractMocaTestCase {
    public void testMonthsBetween() throws MocaException {   
        String sql;
        
        sql = "select months_between(sysdate, sysdate) result from dual";
        runTest(sql, "result", 0);
        
        DateTime now = new DateTime();
        DateTime then = now.minusDays(60);
        
        // We have to find the correct difference of months.  Since we could
        // be crossing a year we have to make sure to normalize it if so by
        // adding 12 months to it.
        int monthDifference = (monthDifference = now.getMonthOfYear()
                - then.getMonthOfYear()) < 0 ? monthDifference + 12
                : monthDifference;
        // There is technically still a race condition if the month of year
        // changes after we calculate it but before we execute that this could
        // fail when going to a month that differs in the number of months when
        // subtracting 60 days.
        sql = "select round(months_between(sysdate, sysdate-/*=moca_util.days(*/ 60 /*=)*/)) result from dual";
        runTest(sql, "result", monthDifference);

        sql = "select round(months_between(sysdate-/*=moca_util.days(*/ 60 /*=)*/, sysdate)) result from dual";
        runTest(sql, "result", -monthDifference);
    } 
    
    private void runTest(String sql, String colname, int expected) throws MocaException {
        MocaResults results = _moca.executeCommand("[" + sql + "]");

        RowIterator rowIter = results.getRows();

        assertTrue("There should be 1 row", rowIter.next());
        assertEquals(expected, rowIter.getInt(colname));
    }
}