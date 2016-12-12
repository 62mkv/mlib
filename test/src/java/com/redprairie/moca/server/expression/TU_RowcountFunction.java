/*
 *  $URL$
 *  $Revision$
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

package com.redprairie.moca.server.expression;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.server.expression.function.FunctionArgumentException;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * This class is to test to make sure that the rowcount function works properly for varying
 * argument types.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_RowcountFunction extends AbstractMocaTestCase {

    public void testRowcountOfNullResults() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "publish data where x = rowcount(NULL)");
        
        RowIterator rowIter = results.getRows();
        
        assertTrue("There should be 1 row.", rowIter.next());
        
        assertEquals(0, rowIter.getInt("x"));
        
        assertFalse("There should be only 1 row.", rowIter.next());
    }
    
    public void testRowcountOfMultiRowResults() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "{[[ x = 100; y = 200 ]] & [[ x = 200; y = 100 ]]} >> res | " +
                "publish data where x = rowcount(@res)");
        
        RowIterator rowIter = results.getRows();
        
        assertTrue("There should be 1 row.", rowIter.next());
        
        assertEquals(2, rowIter.getInt("x"));
        
        assertFalse("There should be only 1 row.", rowIter.next());
    }
    
    public void testRowcountOnNonResultsValue() throws MocaException {
        try {
            MocaResults results = _moca.executeCommand(
                "publish data where res = 'Hello' | " +
                "publish data where x = rowcount(@res)");
            fail("Expected Exception, got " + results.getRowCount() + " rows");
        }
        catch (FunctionArgumentException e) {
            // Normal
        }
    }
    
}
