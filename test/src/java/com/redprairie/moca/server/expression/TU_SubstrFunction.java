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

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * This class tests to make sure that substr function will work correctly.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author gvaneyck
 * @version $Revision$
 */
public class TU_SubstrFunction extends AbstractMocaTestCase {

    public void testVariousSubstrCalls() throws MocaException {
        MocaResults res = _moca.executeCommand(
                "publish data where str = '12345'" + 
                "|" +
                "publish data where str1 = substr(@str, 1)" +
                "               and str2 = substr(@str, 1, 5)" +
                "               and str3 = substr(@str, 2)" +
                "               and str4 = substr(@str, 2, 5000)" +
                "               and str5 = substr(@str, 2, 2)" +
                "               and str6 = substr(@str, 2, 0)" +
                "               and str7 = substr(@str, 5000)" +
                "               and str8 = substr(@str, 5000, 2)" +
                "               and str9 = substr(@str, 0)" +
                "               and str10 = substr(@str, 0, 2)" +
                "               and str11 = substr(@str, -2)" +
                "               and str12 = substr(@str, -2, 1)" +
                "               and str13 = substr(@str, -2, 2)" +
                "               and str14 = substr(@str, -2, 5000)" +
                "               and str15 = substr(@str, -5000)" +
                "               and str16 = substr(@str, -5000, 2)" +
                "               and str17 = substr(@str, 1, -1)");
        
        RowIterator rowIter = res.getRows();
        
        assertTrue(rowIter.next());
        
        assertEquals("12345", rowIter.getString("str1"));
        assertEquals("12345", rowIter.getString("str2"));
        assertEquals("2345", rowIter.getString("str3"));
        assertEquals("2345", rowIter.getString("str4"));
        assertEquals("23", rowIter.getString("str5"));
        assertEquals("", rowIter.getString("str6"));
        assertEquals("", rowIter.getString("str7"));
        assertEquals("", rowIter.getString("str8"));
        assertEquals("12345", rowIter.getString("str9"));
        assertEquals("12", rowIter.getString("str10"));
        assertEquals("45", rowIter.getString("str11"));
        assertEquals("4", rowIter.getString("str12"));
        assertEquals("45", rowIter.getString("str13"));
        assertEquals("45", rowIter.getString("str14"));
        assertEquals("", rowIter.getString("str15"));
        assertEquals("", rowIter.getString("str16"));
        assertEquals("", rowIter.getString("str17"));
    }
}

