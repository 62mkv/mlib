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

package com.redprairie.moca;

import java.util.Collections;
import java.util.Comparator;

import com.redprairie.moca.util.MocaUtils;

/**
 * Unit test for <code>WrappedResults</code>
 *
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */
public class TU_SimpleResults extends TU_AbstractEditableResults {
    /**
     * Test basic data sorting by string column.  Note that this test relies
     * heavily on the specific values of the data in the "sample" test data set.
     */
    public void testSortResultsByStringColumn() {
        SimpleResults res = new SimpleResults();
        _setupColumns(res);
        _fillInSampleResults(res);
        res.sort("columnOne");
        assertTrue(res.next());
        assertNull(res.getString("columnOne"));
        assertTrue(res.next());
        assertEquals("Blah ", res.getString("columnOne"));
        assertTrue(res.next());
        assertEquals("Hello", res.getString("columnOne"));
        assertFalse(res.next());
    }

    /**
     * Test basic data sorting by integer column.  Note that this test relies
     * heavily on the specific values of the data in the "sample" test data set.
     */
    public void testSortResultsByIntColumn() {
        SimpleResults res = new SimpleResults();
        _setupColumns(res);
        _fillInSampleResults(res);
        res.sort("columnThree");
        assertTrue(res.next());
        assertNull(res.getString("columnOne"));
        assertTrue(res.next());
        assertEquals("Hello", res.getString("columnOne"));
        assertTrue(res.next());
        assertEquals("Blah ", res.getString("columnOne"));
        assertFalse(res.next());
    }

    /**
     * Test basic data sorting by double column.  Note that this test relies
     * heavily on the specific values of the data in the "sample" test data set.
     */
    public void testSortResultsByDoubleColumn() {
        SimpleResults res = new SimpleResults();
        _setupColumns(res);
        _fillInSampleResults(res);
        res.sort("columnFour");
        assertTrue(res.next());
        assertNull(res.getString("columnOne"));
        assertTrue(res.next());
        assertEquals("Blah ", res.getString("columnOne"));
        assertTrue(res.next());
        assertEquals("Hello", res.getString("columnOne"));
        assertFalse(res.next());
    }

    /**
     * Test basic data sorting by double column.  Note that this test relies
     * heavily on the specific values of the data in the "sample" test data set.
     */
    public void testSortResultsByMultipleColumns() {
        SimpleResults res = new SimpleResults();
        res.addColumn("a", MocaType.STRING);
        res.addColumn("b", MocaType.STRING);
        res.addColumn("c", MocaType.INTEGER);
        res.addRow();
        res.setStringValue("a", "aaa");
        res.setStringValue("b", "333");
        res.setIntValue("c", 100);
        res.addRow();
        res.setStringValue("a", "aaa");
        res.setStringValue("b", "222");
        res.setIntValue("c", 200);
        res.addRow();
        res.setStringValue("a", "bbb");
        res.setStringValue("b", "222");
        res.setIntValue("c", 300);

        res.sort(new String[] {"a", "b"});
        
        assertTrue(res.next());
        assertEquals(200, res.getInt("c"));
        assertTrue(res.next());
        assertEquals(100, res.getInt("c"));
        assertTrue(res.next());
        assertEquals(300, res.getInt("c"));

        res.sort(new String[] {"b", "a"});
        
        assertTrue(res.next());
        assertEquals(200, res.getInt("c"));
        assertTrue(res.next());
        assertEquals(300, res.getInt("c"));
        assertTrue(res.next());
        assertEquals(100, res.getInt("c"));

        res.sort(new String[] {"b", "a"},
                 new Comparator[] {null, Collections.reverseOrder()});
        
        assertTrue(res.next());
        assertEquals(300, res.getInt("c"));
        assertTrue(res.next());
        assertEquals(200, res.getInt("c"));
        assertTrue(res.next());
        assertEquals(100, res.getInt("c"));
    }
    
    /**
     * This test was made because when previously when setRow was used
     * the editing pointer wasn't moved to that row as well so you could edit the wrong row
     */
    public void testSetRowAndModify() {
        SimpleResults res = newRowData();
        
        // Do 8 iterations so we're pointing at the 8th row at this point
        for (int i = 0; i < 8; i++) {
            res.next();
        }
        
        // Reset to row 3 (zero based), this is where the failure occurs before because
        // the edit row was still pointing at row 8
        res.setRow(2);
        
        // This was actually setting the value in row 8 instead of row 3
        res.setStringValue("rowNumber", "modified");
        res.reset();
        
        // Go back to the third row
        for (int i = 0; i < 3; i++) {
            res.next();
        }
        
        // 3rd row should be modified (previously the 8th row was getting modified)
        assertEquals("modified", res.getString("rowNumber"));
    }
    
    /**
     * This test was made because previously when you called reset() the editing pointer
     * wasn't reset so you could modify the row you were on previously when really
     * an exception should occur because you have to call next() to point at the first row then modify that row
     */
    public void testResetAndModify() {
        SimpleResults res = newRowData();
        
        // Do 8 iterations so we're pointing at the 8th row at this point
        for (int i = 0; i < 8; i++) {
            res.next();
        }
        
        // Reset the result set then try to modify it right away
        // Previously this would work without an exception and you would modify the 8th row 
        // because the edit pointer wasn't reset
        res.reset();
        try {
            res.setStringValue("rowNumber", "thisshouldfail");
            fail("IllegalStateException should have occurred because we just reset" + 
                 "  the result set so we're not pointing at anything!");
            
        }
        catch (IllegalStateException expected) {}
        
    }

    protected EditableResults _createNewResults() {
        return new SimpleResults();
    }
    
    // Creates a SimpleResults with column "rowNumber" and data with row1 - row10
    private SimpleResults newRowData() {
        SimpleResults source = new SimpleResults();
        source.addColumn("rowNumber", MocaType.STRING);
        for (int i = 1; i <= 10; i++) {
            source.addRow();
            source.setStringValue("rowNumber", "row" + i);
        }
        
        SimpleResults res = new SimpleResults();
        MocaUtils.copyResults(res, source);
        
        return res;
    }
}
