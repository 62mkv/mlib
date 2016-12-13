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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

import static org.junit.Assert.fail;

/**
 * This class tests to make sure that to_char function will work correctly.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author gvaneyck
 * @version $Revision$
 */
public class TU_ToCharFunction extends AbstractFunctionTest {
    
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
    public void testNullDate() throws MocaException {
        runTest(null, null, null);
        runTest(null, "YYYYMMDDHH24MISS", null);
        runTest(null, "MM/DD/YYYY HH24:MI:SS", null);
    }
    
    @Test
    public void testNullNumber() throws MocaException {
        runTest(null, null, null);
        runTest(null, "0000", null);
        runTest(null, "00000000000000", null);
    }

    @Test
    public void testNoDateFormats() throws Exception {
        runTest(_knownDate, null, "20020711112345");
    }

    @Test
    public void testKnownDateFormatStrings() throws MocaException {
        runTest(_knownDate, "YYYYMMDDHH24MISS", "20020711112345");
        runTest(_knownDate, "yyyy-mm-dd hh24:mi:ss", "2002-07-11 11:23:45");
        runTest(_knownDate, "MM/DD/YYYY", "07/11/2002");
        runTest(_knownDate, "MMDDYYYY", "07112002");
        runTest(_knownDate, "DD", "11");
        runTest(_knownDate, "DAY", "Thursday");
        runTest(_knownDate, "Mon", "Jul");
        runTest(_knownDate, "HH24MISS", "112345");
        runTest(_knownDate, "HHMISS", "112345");
        runTest(_knownDate, "YYYYMM", "200207");
        runTest(_knownDate, "YYYYMMDD", "20020711");
        runTest(_knownDate, "YYYYDDD", "2002192");
    }

    @Test
    public void testKnownNumberFormatStrings() throws MocaException {
        runTest(321, "0000", "0321");
        runTest(-11, "0000", "-0011");
        runTest(17, "S999.00", " +17.00");
        runTest(-27, "S999.00", " -27.00");
        runTest(42.3, "0000000000000000", "0000000000000042");
        runTest("92", "0000000000000000", "0000000000000092");
    }
    
    @Test
    public void testNumberFormatWithImpliedDecimals() throws MocaException {
        runTest(17, "0999V99", " 001700");
    }
    
    @Test
    public void testInvalidDate() throws MocaException {
        String invalidDateString = ":20100101080000";
        runTest(invalidDateString, null, invalidDateString);
        try {
            runTest(invalidDateString, "YYYYMMDDHH24MISS", null);
            fail("Should have thrown a FunctionArgumentException");
        }
        catch (FunctionArgumentException e) {
            // Should go here
        }
        try {
            runTest(invalidDateString, "MM/DD/YYYY HH24:MI:SS", null);
            fail("Should have thrown a FunctionArgumentException");
        }
        catch (FunctionArgumentException e) {
            // Should go here
        }
    }
    
    @Test
    public void testOracleConversion() throws MocaException {
        String invalidDateString = ":20100101080000";
        try {
            runTest("1234", "YYYYMMDDHH24MISS", null);
            fail("Should have thrown a FunctionArgumentException");
        }
        catch (FunctionArgumentException e) {
            // Should go here
        }
        try {
            runTest(invalidDateString, "MM/DD/YYYY HH24:MI:SS", null);
            fail("Should have thrown a FunctionArgumentException");
        }
        catch (FunctionArgumentException e) {
            // Should go here
        }
    }
    
    private static SimpleDateFormat _formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); 
    
    private static Date _knownDate;
    static {
        try {
            _knownDate = _formatter.parse("2002-07-11 11:23:45.000");
        }
        catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void runTest(Object arg, String format, String expected) throws MocaException {
        
        if (format != null) {
            runTestWithObjectArgs(expected, arg, format);
        }
        else {
            runTestWithObjectArgs(expected, arg);
        }
    }
    
    // @see com.redprairie.moca.server.expression.function.AbstractFunctionTest#getFunction()
    @Override
    protected BaseFunction getFunction() {
        return new ToCharFunction();
    }
    
    
    private static final String DBCALL_INDICATOR="[XXX]";
}
