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

import java.nio.charset.Charset;
import java.util.Date;

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
public abstract class TU_AbstractModifiableResults extends TU_AbstractResults {

    public void testWrongDatatype() {
        ModifiableResults results = _createEmptyResults();
        results.addRow();
        try {
            results.setIntValue("columnOne", 100);
            fail("Expected exception");
        }
        catch (IllegalArgumentException e) {
            // Normal
        }
        try {
            results.setDoubleValue("columnOne", 1.5);
            fail("Expected exception");
        }
        catch (IllegalArgumentException e) {
            // Normal
        }
        try {
            results.setDateValue("columnThree", new Date());
            fail("Expected exception");
        }
        catch (IllegalArgumentException e) {
            // Normal
        }
        try {
            results.setBooleanValue("columnOne", false);
            fail("Expected exception");
        }
        catch (IllegalArgumentException e) {
            // Normal
        }
        try {
            results.setBinaryValue("columnOne", new byte[0]);
            fail("Expected exception");
        }
        catch (IllegalArgumentException e) {
            // Normal
        }
    }
    
    public void testNoCurrentRowAfterEmptyResultsCreation() {
        MocaResults results = _createEmptyResults();
        try {
            // Verify that the "current row" pointer is the last row we added
            String value = results.getString(0);
            fail("Expected exception...got: " + value);
        }
        catch (IllegalStateException e) {
            // Normal
        }
        finally {
            results.close();
        }
    }

    public void testAddRowsWithinIteration() {
        ModifiableResults results = _createEmptyResults();
        _fillInSampleResults(results);
        _verifySampleData(results, false);
        _fillInSampleResults(results);
        _verifySampleData(results, false);
        assertTrue(results.next());
    }
    
    public void testModifyDataWithinIteration() {
        ModifiableResults results = _createEmptyResults();
        _fillInSampleResults(results);
        _fillInSampleResults(results);
        _verifySampleData(results, false);
        results.setStringValue("columnOne", "Blah ");
        results.setDateValue("columnTwo", null);
        results.setIntValue("columnThree", 900);
        results.setDoubleValue("columnFour", Math.E);
        results.setBooleanValue("columnFive", false);
        results.setBinaryValue("columnSix", "foo".getBytes(Charset.forName("UTF-8")));
        results.setValue("columnOne", null);
        results.setValue("columnTwo", null);
        results.setValue("columnThree", null);
        results.setValue("columnFour", null);
        results.setValue("columnFive", null);
        results.setValue("columnSix", null);
        _verifySampleData(results, true);
    }
    
    public void testRemoveAllRows() {
        ModifiableResults results = _createEmptyResults();
        _fillInSampleResults(results);
        assertTrue(results.next());
        results.removeRow();
        assertTrue(results.next());
        results.removeRow();
        assertTrue(results.next());
        results.removeRow();
        assertFalse(results.next());
    }
    
    public void testRemoveInitialRows() {
        ModifiableResults results = _createEmptyResults();
        _fillInSampleResults(results);
        _fillInSampleResults(results);
        assertTrue(results.next());
        results.removeRow();
        assertTrue(results.next());
        results.removeRow();
        assertTrue(results.next());
        results.removeRow();
        _verifySampleData(results, true);
    }
    
    public void testRemoveMiddleRows() {
        ModifiableResults results = _createEmptyResults();
        _fillInSampleResults(results);
        _fillInSampleResults(results);
        _fillInSampleResults(results);
        _verifySampleData(results, false);
        assertTrue(results.next());
        results.removeRow();
        assertTrue(results.next());
        results.removeRow();
        assertTrue(results.next());
        results.removeRow();
        _verifySampleData(results, true);
    }
    
    public void testNoCurrentRowAfterRemove() {
        ModifiableResults results = _createEmptyResults();
        _fillInSampleResults(results);
        assertTrue(results.next());
        results.removeRow();
        try {
            results.getString(0);
            fail("expected exception");
        }
        catch (IllegalStateException e) {
            // normal
        }
        
    }
    
    public void testNoCurrentEditRowAfterRemove() {
        ModifiableResults results = _createEmptyResults();
        _fillInSampleResults(results);
        assertTrue(results.next());
        results.removeRow();
        try {
            results.setStringValue("columnOne", "XXX");
            fail("expected exception");
        }
        catch (IllegalStateException e) {
            // normal
        }
        
    }
    
    public void testResultsColumn() {
        ModifiableResults results = _createEmptyNestedResults();
        MocaResults data = _createSampleResults();
        results.addRow();
        results.setValue("results", data);
        try {
            assertTrue(results.next());
            _verifySampleData((MocaResults)results.getValue("results"));
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }
    
    public void testDefaultNullValues() {
        ModifiableResults results = _createEmptyResults();
        results.addRow();
        _testNullResults(results);
    }
    
    public void testNullValuesSetWithSetValueMethod() {
        ModifiableResults results = _createEmptyResults();
        results.addRow();
        
        // Use the setValue method
        results.setValue("columnOne", null);
        results.setValue("columnTwo", null);
        results.setValue("columnThree", null);
        results.setValue("columnFour", null);
        results.setValue("columnFive", null);
        results.setValue("columnSix", null);
        _testNullResults(results);
    }

    public void testNullValuesSetWithTypeSpecificMethods() {
        ModifiableResults results = _createEmptyResults();
        results.addRow();
      
        results.setStringValue("columnOne", null);
        results.setDateValue("columnTwo", null);
        results.setBinaryValue("columnSix", null);
        _testNullResults(results);
    }
    
    //
    // Subclass interface
    //
    protected abstract ModifiableResults _createEmptyResults();
    
    protected abstract ModifiableResults _createEmptyNestedResults();
    
    protected void _fillInSampleResults(ModifiableResults results) {
        Date now = new Date();
        results.addRow();
        results.setStringValue("columnOne", "Hello");
        results.setDateValue("columnTwo", now);
        results.setIntValue("columnThree", 1);
        results.setDoubleValue("columnFour", 3.142);
        results.setBooleanValue("columnFive", true);
        results.setBinaryValue("columnSix", new byte[] {7, 6, 5, 4, 3, 2, 1, 0, 127});
        results.addRow();
        results.setStringValue("columnOne", "Blah ");
        results.setDateValue("columnTwo", new Date(now.getTime() + 3600000));
        results.setIntValue("columnThree", 900);
        results.setDoubleValue("columnFour", Math.E);
        results.setBooleanValue("columnFive", false);
        results.setBinaryValue("columnSix", "foo".getBytes(Charset.forName("UTF-8")));
        results.addRow();
    }
    
    protected MocaResults _createSampleResults() {
        ModifiableResults results = _createEmptyResults();
        _fillInSampleResults(results);
        return results;
    }
    
    protected void _testNullResults(ModifiableResults results) {
        assertTrue(results.next());
        assertNull(results.getString("columnOne"));
        assertNull(results.getDateTime("columnTwo"));
        assertEquals(0, results.getInt("columnThree"));
        assertEquals(0.0, results.getDouble("columnFour"), 0.0);
        assertFalse(results.getBoolean("columnFive"));
        assertNull(results.getValue("columnSix"));
    }

}
