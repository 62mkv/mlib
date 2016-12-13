/*
 *  $URL$
 *  $Revision$
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

package com.redprairie.moca.server.expression.function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.DBType;

/**
 * This class tests to make sure that instr function will work correctly.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author gvaneyck
 * @version $Revision$
 */
public class TU_ToNumberFunction extends AbstractFunctionTest {
    
    @Before
    public void setUpOracleCatchAll() throws Exception {
        Mockito.when(_mockContext.getDbType()).thenReturn(DBType.ORACLE);
        Mockito.when(_mockContext.executeSQL(Mockito.anyString(),
            Mockito.any(BindList.class), Mockito.anyBoolean(),
            Mockito.anyBoolean())).thenAnswer(new Answer<MocaResults>() {
                @Override
                public MocaResults answer(InvocationOnMock call)
                        throws Throwable {
                    BindList bindList = (BindList)call.getArguments()[1];
                    bindList.setValue("result", DBCALL_INDICATOR);
                    return new SimpleResults();
                }
            });
    }

    @Test
    public void testNoNumberFormat() throws Exception {
        runTest("902.2", null, 902.2);
        runTest("11", null, 11.0);
        runTest("0", null, 0.0);
        runTest("", null, null);
        runTest(null, null, null);
    }
    
    @Test
    public void testNonNumericValues() throws Exception {
        runTest("1.1 times 2.2", null, 1.1);
        runTest("57chevy", null, 57.0);
        runTest("111.222.333", null, 111.222);
        runTest("+", null, 0.0);
    }
    
    @Test
    public void testKnownNumberFormat() throws Exception {
        runTest("902.2", "999.9", 902.2);
        runTest("11", "99", 11.0);
        runTest("2", "999", 2.0);
        runTest("0", "999", 0.0);
        runTest("", "999.99", null);
        runTest(null, "9.99", null);
    }
    
    @Test
    public void testLeadingZeroFormat() throws Exception {
        runTest("902.2", "099.0", 902.2);
        runTest("11", "09", 11.0);
        runTest("002", "099", 2.0);
        runTest("000", "099", 0.0);
        runTest("", "000.00", null);
        runTest(null, "0.00", null);
    }
    
    @Test
    public void testImpliedDecimalFormat() throws Exception {
        runTest("314", "9V99", 3.14);
        runTest("110002", "999V99", 1100.02);
        runTest("0", "9V999", 0.0);
        runTest("1", "99V9", 0.1);
    }
    
    @Test
    public void testOracleSpecificFormats() throws Exception {
        runTest("$12.34", "L99D99", DBCALL_INDICATOR);
    }
    
    @Override
    protected BaseFunction getFunction() {
        return new ToNumberFunction();
    }

    private void runTest(Object arg, String format, Double expected) throws MocaException {
        if (format == null) {
            runTestWithObjectArgs(expected, arg);
        }
        else {
            runTestWithObjectArgs(expected, arg, format);
        }
    }
    
    private static final double DBCALL_INDICATOR=-999.72;
}
