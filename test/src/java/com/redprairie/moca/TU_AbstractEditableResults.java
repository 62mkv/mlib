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

import com.redprairie.moca.server.legacy.GenericPointer;
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
public abstract class TU_AbstractEditableResults extends TU_AbstractModifiableResults {
    public void testAddColumn() {
        MocaResults data = _createSampleResults();
        EditableResults res = _createNewResults();
        MocaUtils.copyResults(res, data);
        res.addColumn("added_col", MocaType.STRING);
        res.reset();
        for (int i = 0; res.next(); i++) {
            res.setStringValue("added_col", "Row " + i);
        }
        res.reset();
        for (int i = 0; res.next(); i++) {
            assertEquals("Row " + i, res.getString("added_col"));
        }
    }
    
    public void testAddNotNullColumn() {
        MocaResults data = _createSampleResults();
        EditableResults res = _createNewResults();
        MocaUtils.copyResults(res, data);
        res.addColumn("default_col", MocaType.STRING);
        res.addColumn("null_col", MocaType.STRING, 30, true);
        res.addColumn("notnull_col", MocaType.STRING, 30, false);
        res.reset();
        assertTrue(res.isNullable("default_col"));
        assertTrue(res.isNullable("null_col"));
        assertFalse(res.isNullable("notnull_col"));
    }
    
    public void testAddColumnToNonEmptyResults() {
        EditableResults res = _createNewResults();
        res.addRow();
        res.addColumn("col1", MocaType.STRING);
        res.setStringValue("col1", "col1_value");
        assertTrue(res.next());
        assertEquals(1, res.getRowCount());
        assertEquals(1, res.getColumnCount());
        assertEquals("col1_value", res.getString("col1"));
    }
    
    public void testGenericColumn() {
        EditableResults res = _createNewResults();
        res.addColumn("generic", MocaType.GENERIC);
        res.addRow();
        res.setValue("generic", new GenericPointer(187));
        assertTrue(res.next());
        assertEquals(new GenericPointer(187), res.getValue("generic"));
        assertEquals(1, res.getColumnCount());
        
        res.addRow();
        try {
            res.setValue("generic", 187);
            fail("Expected Exception adding generic pointer as number");
        }
        catch (IllegalArgumentException e) {
            // Normal
        }
    }
    
    //
    // Subclass interface
    //
    protected abstract EditableResults _createNewResults();
    
    protected ModifiableResults _createEmptyResults() {
        EditableResults res = _createNewResults();
        _setupColumns(res);
        return res;
    }
    
    protected ModifiableResults _createEmptyNestedResults() {
        EditableResults results = _createNewResults();
        _setupColumns(results);
        results.addColumn("results", MocaType.RESULTS);
        return results;
    }
    
    protected EditableResults _setupColumns(EditableResults results) {
        results.addColumn("columnOne", MocaType.STRING);
        results.addColumn("columnTwo", MocaType.DATETIME);
        results.addColumn("columnThree", MocaType.INTEGER);
        results.addColumn("columnFour", MocaType.DOUBLE);
        results.addColumn("columnFive", MocaType.BOOLEAN);
        results.addColumn("columnSix", MocaType.BINARY);
        return results;
    }
}
