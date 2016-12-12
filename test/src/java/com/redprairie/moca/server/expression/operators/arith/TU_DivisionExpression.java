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

import junit.framework.Assert;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * Test for evaluating division expressions in MOCA.
 */
public class TU_DivisionExpression extends AbstractMocaTestCase {

    public void testDivisionWithIntegers() throws MocaException {
        runTest("16", "1", 16.0);
        runTest("16.0", "1.0", 16.0);
        runTest("'16'", "'1'", 16.0);
        runTest("'16.0'", "'1.0'", 16.0);
    }
    
    public void testDivisionOfZero() throws MocaException {
        runTest("0", "1", 0.0);
        runTest("0.0", "0.004", 0.0);
        runTest("'0'", "-20", -0.0);
        runTest("0", "'10000'", 0.0);
    }
    
    public void testDivisionByZero() throws MocaException {
        runTestWithError("100", "0");
        runTestWithError("-100", "0");
        runTestWithError("0", "0");
        runTestWithError("100.0", "0.0");
        runTestWithError("-100.0", "0.0");
        runTestWithError("0.0", "0.0");
        runTestWithError("'100.0'", "'0.0'");
        runTestWithError("'-100'", "'0'");
        runTestWithError("'0'", "'0'");
    }
    
    private void runTest(String n, String d, double expected) throws MocaException {
        
        MocaResults res = _moca.executeCommand(
            "publish data where numerator = " + n + " and denominator = " + d + " " +
            " | " +
            "publish data where result = @numerator / @denominator");
        
        RowIterator rowIter = res.getRows();
        rowIter.next();
        
        Assert.assertEquals(expected, rowIter.getDouble("result"));
    }

    private void runTestWithError(String n, String d) throws MocaException {
        
        try {
            MocaResults res = _moca.executeCommand(
                "publish data where numerator = " + n + " and denominator = " + d + " " +
                " | " +
                "publish data where result = @numerator / @denominator");
            RowIterator rowIter = res.getRows();
            rowIter.next();
            
            fail("expected exception, got" + rowIter.getDouble("result"));
        }
        catch (DivideByZeroException e) {
            // Normal
        }
        
    }
    
}
