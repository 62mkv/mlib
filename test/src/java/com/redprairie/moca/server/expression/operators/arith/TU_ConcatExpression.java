/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.server.expression.operators.arith;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.joda.time.DateTime;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * Test for evaluating division expressions in MOCA.
 */
public class TU_ConcatExpression extends AbstractMocaTestCase {

    public void testConcatNumbers() throws MocaException {
        runTest("1||2", "12");
        runTest("1.3||2", "1.32");
    }
    
    public void testConcatStrings() throws MocaException {
        runTest("'A'||'B'", "AB");
        runTest("'A'||'B'||'C'", "ABC");
        runTest("'A'||''||'C'", "AC");
        runTest("'A'||null||'C'", "AC");
    }
    
    public void testConcatDate() throws MocaException {
        Date dateValue = new DateTime(2009, 2, 3, 11, 15, 21, 0).toDate();
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x", dateValue);
        runTest("''||@x||''", "20090203111521", args);
    }
    
    private void runTest(String expr, String expected) throws MocaException {
        runTest(expr, expected, null);
    }
    private void runTest(String expr, String expected, Map<String, Object> args) throws MocaException {
        
        MocaResults res = _moca.executeCommand(
            "publish data where result = " + expr, args);
        
        RowIterator rowIter = res.getRows();
        rowIter.next();
        
        Assert.assertEquals(expected, rowIter.getString("result"));
    }
    
}
