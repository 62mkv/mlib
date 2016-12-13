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

package com.redprairie.moca.server.expression;

import java.util.Date;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.server.expression.function.FunctionArgumentException;
import com.redprairie.moca.util.AbstractMocaTestCase;

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
public class TU_DateFunction extends AbstractMocaTestCase {

    public void testNullDate() throws MocaException {
        runTest("null", null);
    }
    
    public void testNormalDate() throws Exception {
        DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        runTest("'20020711112345'", dateFormat.parseDateTime("2002-07-11 11:23:45").toDate());
    }

    public void testDateInvalidString() throws MocaException {
        try {
            runTest("'2009093004xx31'", null);
            fail("expected exception");
        }
        catch (FunctionArgumentException e) {
            //Normal
        }
    }

    public void testDateShortString() throws MocaException {
        try {
            runTest("'20090930'", null);
            fail("expected exception");
        }
        catch (FunctionArgumentException e) {
            //Normal
        }
    }

    private void runTest(String arg, Date expected) throws MocaException {
        MocaResults res = _moca.executeCommand("publish data where result = date(" + arg + ")");
    
        RowIterator rowIter = res.getRows();
        assertTrue(rowIter.next());
        assertEquals(MocaType.DATETIME, res.getColumnType("result"));
        Date result = rowIter.getDateTime("result");
        
        assertEquals(expected, result);
    }
}
