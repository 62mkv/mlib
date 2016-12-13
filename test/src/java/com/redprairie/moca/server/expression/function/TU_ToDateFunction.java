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

import java.sql.SQLException;
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
import com.redprairie.moca.MocaType;
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
public class TU_ToDateFunction extends AbstractFunctionTest {

    @Test
    public void testNullDate() throws MocaException {
        runTest(null, null, null);
        runTest(null, "'YYYYMMDDHH24MISS'", null);
        runTest(null, "'MM/DD/YYYY HH24:MI:SS'", null);
    }
    
    @Test
    public void testNoDateFormats() throws Exception {
        runTest("20020711112345", null, _knownDate);
    }

    @Test
    public void testNumericArgumentWithNoDateFormat() throws Exception {
        runTestWithObjectArgs(_knownDay, 20020711);
        runTestWithObjectArgs(_knownDate, 20020711112345.0);
    }

    @Test
    public void testKnownDateFormatStrings() throws MocaException {
        runTest("20020711112345", "YYYYMMDDHH24MISS", _knownDate);
        runTest("2002-07-11 11:23:45", "yyyy-mm-dd hh24:mi:ss", _knownDate);
        runTest("20020711", "YYYYMMDD", _knownDay);
        runTest("2002192", "YYYYDDD", _knownDay);
    }

    @Before
    public void setupDbIntercept() throws Exception {
        // This method tests that "bad" date values get sent to the database
        Mockito.when(_mockContext.getDbType()).thenReturn(DBType.ORACLE);
        Mockito.when(_mockContext.executeSQL(Mockito.anyString(),
            Mockito.any(BindList.class), Mockito.anyBoolean(),
            Mockito.anyBoolean())).thenAnswer(new Answer<MocaResults>() {
                @Override
                public MocaResults answer(InvocationOnMock call)
                        throws Throwable {
                    SimpleResults res = new SimpleResults();
                    res.addColumn("result", MocaType.DATETIME);
                    res.addRow();
                    res.setValue("result", _dbGeneratedDate);
                    return res;
                }
            });
    }

    @Test
    public void testBadDateValues() throws MocaException, SQLException {
        runTestExpectingDB("'200207ABCD2345'", "'YYYYMMDDHH24MISS'");
        runTestExpectingDB("'2002-07-11X11:23:45'", "'yyyy-mm-dd hh24:mi:ss'");
        runTestExpectingDB("'200207ZZ'", "'YYYYMMDD'");
    }

    @Test
    public void testShortDateFormatWithLongDateValue() throws MocaException, SQLException {
        runTest("20020711112345", "YYYYMMDD", _knownDay);
    }

    @Test
    public void testLongDateFormatWithShortDateValue() throws MocaException, SQLException {
        runTest("20020711", "YYYYMMDDHH24MISS", _knownDay);
    }
    
    @Test
    public void testShortDateValue() throws Exception {
        runTest("20020711", null, _knownDay);
        runTestExpectingError("2002-07-11", null);
        runTestExpectingError("200207111443", null);
    }

    private void runTestExpectingError(String arg, String format) throws MocaException {
        if (format != null) {
            runTestWithObjectArgsExpectingError(FunctionArgumentException.CODE, arg, format);
        }
        else {
            runTestWithObjectArgsExpectingError(FunctionArgumentException.CODE, arg);
        }
    }
    
    private void runTest(String arg, String format, Date expected) throws MocaException {
        
        if (format != null) {
            runTestWithObjectArgs(expected, arg, format);
        }
        else {
            runTestWithObjectArgs(expected, arg);
        }
    }
    
    private void runTestExpectingDB(String arg, String format) throws MocaException {
        runTestWithObjectArgs(_dbGeneratedDate, arg, format);
    }
    
    private static SimpleDateFormat _formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); 
    
    private static Date _knownDate;
    private static Date _knownDay;
    private static Date _dbGeneratedDate;
    static {
        try {
            _knownDate = _formatter.parse("2002-07-11 11:23:45.000");
            _knownDay = _formatter.parse("2002-07-11 00:00:00.000");
            _dbGeneratedDate = _formatter.parse("2947-07-13 14:13:92.182");
        }
        catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected BaseFunction getFunction() {
        return new ToDateFunction();
    }
    
}
