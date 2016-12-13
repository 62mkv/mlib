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

package com.redprairie.moca.server.expression.operators.arith;

import junit.framework.Assert;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * Test for evaluating mod expressions in MOCA.
 */
public class TU_ModExpression extends AbstractMocaTestCase {

    public void testMod() throws MocaException {
        runTest("13", "4", 1);
        runTest("13.5", "7", 6);
        runTest("'9'", "2.2", 1);
        runTest("'13.2'", "'9.5'", 4);
    }
    
    public void testModOfZero() throws MocaException {
        runTest("0", "3", 0);
        runTest("0.0", "15", 0);
        runTest("'0'", "-20", 0);
        runTest("'0.0'", "'10000'", 0);
    }
    
    public void testModZero() throws MocaException {
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
    
    private void runTest(String n, String d, int expected) throws MocaException {
        
        MocaResults res = _moca.executeCommand(
            "publish data where numerator = " + n + " and denominator = " + d + " " +
            " | " +
            "publish data where result = @numerator % @denominator");
        
        RowIterator rowIter = res.getRows();
        rowIter.next();
        
        Assert.assertEquals(expected, rowIter.getInt("result"));
    }

    private void runTestWithError(String n, String d) throws MocaException {
        
        try {
            MocaResults res = _moca.executeCommand(
                "publish data where numerator = " + n + " and denominator = " + d + " " +
                " | " +
                "publish data where result = @numerator % @denominator");
            RowIterator rowIter = res.getRows();
            rowIter.next();
            
            fail("expected exception, got" + rowIter.getInt("result"));
        }
        catch (DivideByZeroException e) {
            // Normal
        }
        
    }
    
}
