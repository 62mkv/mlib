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

package com.redprairie.moca.components.base;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * This class is to test the session variable code
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_SessionVariableService extends AbstractMocaTestCase {

    /**
     * This method is to test the setting and getting of session variables
     * @throws MocaException 
     */
    public void testSaveGetSessionVariables() throws MocaException {
        String name = "foo";
        String value = "bar";
        MocaResults res = _moca.executeCommand(
                "save session variable" +
                "  where name = '" + name + "'" +
                "    and value = '" + value + "'" + 
                "|" +
                "get session variable" +
                "  where name = '" + name + "'");
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("There were no rows.", rowIter.next());
        
        assertEquals("The session variable was not retrieved", value, 
                rowIter.getString("value"));
        
        assertFalse("There should be only 1 row", rowIter.next());
    }
    
    /**
     * This method is to test the setting and listing of session variables
     * @throws MocaException 
     */
    public void testSaveListSessionVariables() throws MocaException {
        final String[] names = {"var1", "var2", "var3"};
        final String[] values = {"yes", "no", "maybe"};
        int variableCount = 3;
        MocaResults res = _moca.executeCommand(
                "save session variable " +
                "  where name = '" + names[0] + "'" +
                "    and value = '" + values[0] + "'" +
                "|" +
                "save session variable " +
                "  where name = '" + names[1] + "'" +
                "    and value = '" + values[1] + "'" +
                "|" +
                "save session variable " +
                "  where name = '" + names[2] + "'" +
                "    and value = '" + values[2] + "'" +
                "|" +
                "list session variables");
        
        assertEquals("The rows returned don't match", variableCount, res.getRowCount());
        
        RowIterator rowIter = res.getRows();
        
        // Now we loop through how many variables there should be
        for (int i = 0; i < variableCount; i++) {
            assertTrue("There should be a row", rowIter.next());
            
            assertEquals("The name didn't match", names[i], 
                    rowIter.getString("name"));
            assertEquals("The variable didn't match", values[i], 
                    rowIter.getValue("value"));
        }
        
        assertFalse("We should have gone through all rows", rowIter.next());
    }
    
    /**
     * This method is to make sure that when someone tries to retrieve
     * a session variable and it doesn't exist that we throw the correct error.
     * @throws MocaException This is thrown if a problem not expected is
     *         encountered.
     */
    public void testGetSessionVariableNotPresent() throws MocaException {
        try {
            _moca.executeCommand(
                    "get session variable" +
                    "  where name = 'NOT-PRESENT'");
            fail("Test should have thrown a NotFoundException");
        }
        catch (MocaException e) {
            if (!(e instanceof NotFoundException)) {
                throw e;
            }
            // Code should go here.
        }        
    }
}
