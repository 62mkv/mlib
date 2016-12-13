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
import java.util.Arrays;

import junit.framework.TestCase;

import org.junit.Assert;

/**
 * Unit test for the AbstractResults class.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public abstract class TU_AbstractResults extends TestCase {

    public void testMetadata() {
        MocaResults results = _createSampleResults();
        try {
            _verifyMetadata(results);
        }
        finally {
            results.close();
        }
    }

    public void testNoCurrentRowAfterCreation() {
        MocaResults results = _createSampleResults();
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

    public void testNoCurrentRowAfterReset() {
        MocaResults results = _createSampleResults();
        
        try {
            results.reset();
    
            try {
                String value = results.getString(0);
                fail("Expected exception...got: " + value);
            }
            catch (IllegalStateException e) {
                // Normal
            }
        }
        finally {
            results.close();
        }
    }

    public void testGetAllRows() {
        MocaResults results = _createSampleResults();
        try {
            int counter = 0;
            
            while (results.next()) {
                counter++;
            }
            
            assertEquals(3, counter);
    
            // Now, there should be no current row.
            try {
                String value = results.getString("columnOne");
                fail("Expected exception...got: " + value);
            }
            catch (IllegalStateException e) {
                // Normal
            }
        }
        finally {
            results.close();
        }
    }

    public void testSampleData() {
        MocaResults results = _createSampleResults();
        try {
            _verifySampleData(results);
            assertEquals(3, results.getRowCount());
        }
        finally {
            results.close();
        }
    }

    public void testMultiplePasses() {
        MocaResults results = _createSampleResults();
        try {
            while (results.next()) {
                // do nothing, this time around...
            }
            
            results.reset();
            _verifySampleData(results);
        }
        finally {
            results.close();
        }
    }

    public void testNextAfterLastRow() {
        MocaResults results = _createSampleResults();
        try {
            while (results.next()) {
                // do nothing, this time around...
            }
            
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }

    public void testCaseInsensitiveColumns() {
        MocaResults results = _createSampleResults();
        try {
            int one = results.getColumnNumber("columnOne");
            int two = results.getColumnNumber("columntwo");
            int three = results.getColumnNumber("columnThree");
            assertEquals(one, results.getColumnNumber("columnone"));
            assertEquals(two, results.getColumnNumber("COLUMNTWO"));
            assertEquals(three, results.getColumnNumber("ColumnTHREE"));
            
            assertTrue(results.next());
            
            assertEquals("Hello", results.getString("columnone"));
            assertEquals("Hello", results.getString("COLUMNONE"));
            assertEquals("Hello", results.getString("ColumnOne"));
        }
        finally {
            results.close();
        }
    }

    public void testNonExistentColumns() {
        MocaResults results = _createSampleResults();
        try {
            results.next();
            int column = results.getColumnNumber("columnDoesNotExist");
            assertEquals(-1, column);
            assertFalse(results.containsColumn("columnDoesNotExist"));
            
            try {
                String value = results.getString("columnDoesNotExist");
                fail("expected exception, got " + value);
            }
            catch (ColumnNotFoundException e) {
                // Normal
            }
        }
        finally {
            results.close();
        }
    }

    public void testActivityAfterClose() {
        MocaResults results = _createSampleResults();
        results.close();
        // It is acceptable for close to cause further calls to throw
        // IllegalStateException.  It is also acceptable for it to have no
        // visible effect.
        try {
            int num = results.getColumnNumber("columnOne");
            Assert.assertTrue("The column wasn't found", num >= 0);
            // Normal
        }
        catch (IllegalStateException e) {
            // Normal
        }
    }

    public void testMultipleClose() {
        MocaResults results = _createSampleResults();
        results.close();
        results.close();
    }
    
    public void testGetAsDifferentTypes() {
        MocaResults results = _createSampleResults();
        results.next();
        assertEquals("1", results.getString("columnThree"));
        assertEquals("3.142", results.getString("columnFour"));
        assertEquals("true", results.getString("columnFive"));
        assertEquals(3, results.getInt("columnFour"));
        assertEquals(1.0, results.getDouble("columnThree"), 0.0);
        assertEquals(true, results.getBoolean("columnThree"));
        results.close();
    }
    
    //
    // Subclass interface
    //

    protected void _verifySampleData(MocaResults results) {
        _verifySampleData(results, true);
    }
    
    protected void _verifySampleData(MocaResults results, boolean checkEOF) {
        assertNotNull("null result set passed", results);
        assertTrue(results.next());
        
        if (_checkColumnPosition()) {
            assertEquals("Hello", results.getString(0));
            // TODO compare datetime types
            assertEquals(1, results.getInt(2));
            assertEquals(3.142, results.getDouble(3), 0.0);
            assertEquals(true, results.getBoolean(4));
            assertTrue(Arrays.equals(new byte[] {7, 6, 5, 4, 3, 2, 1, 0, 127},
                    (byte[]) results.getValue(5)));
        }

        assertEquals("Hello", results.getString("columnOne"));
        // TODO compare datetime types
        assertEquals(1, results.getInt("columnThree"));
        assertEquals(3.142, results.getDouble("columnFour"), 0.0);
        assertEquals(true, results.getBoolean("columnFive"));
        assertTrue(Arrays.equals(new byte[] {7, 6, 5, 4, 3, 2, 1, 0, 127},
                   (byte[]) results.getValue("columnSix")));
    
        assertTrue(results.next());

        if (_checkColumnPosition()) {
            assertEquals("Blah ", results.getString(0));
            // TODO compare datetime types
            assertEquals(900, results.getInt(2));
            assertEquals(Math.E, results.getDouble(3), 0.0);
            assertEquals(false, results.getBoolean(4));
            assertTrue(Arrays.equals("foo".getBytes(Charset.forName("UTF-8")),
                    (byte[]) results.getValue(5)));
        }

        assertEquals("Blah ", results.getString("columnOne"));
        // TODO compare datetime types
        assertEquals(900, results.getInt("columnThree"));
        assertEquals(Math.E, results.getDouble("columnFour"), 0.0);
        assertEquals(false, results.getBoolean("columnFive"));
        assertTrue(Arrays.equals("foo".getBytes(Charset.forName("UTF-8")),
                   (byte[]) results.getValue("columnSix")));
    
        assertTrue(results.next());

        if (_checkColumnPosition()) {
            assertTrue(results.isNull(0));
            assertNull(results.getValue(0));
            assertNull(results.getString(0));
            assertTrue(results.isNull(1));
            assertNull(results.getValue(1));
            assertNull(results.getDateTime(1));
            assertTrue(results.isNull(2));
            assertNull(results.getValue(2));
            assertTrue(results.isNull(3));
            assertNull(results.getValue(3));
            assertTrue(results.isNull(4));
            assertNull(results.getValue(4));
            assertTrue(results.isNull(5));
            assertNull(results.getValue(5));
        }

        assertTrue(results.isNull("columnOne"));
        assertNull(results.getValue("columnOne"));
        assertNull(results.getString("columnOne"));
        assertTrue(results.isNull("columnTwo"));
        assertNull(results.getValue("columnTwo"));
        assertNull(results.getDateTime("columnTwo"));
        assertTrue(results.isNull("columnThree"));
        assertNull(results.getValue("columnThree"));
        assertTrue(results.isNull("columnFour"));
        assertNull(results.getValue("columnFour"));
        assertTrue(results.isNull("columnFive"));
        assertNull(results.getValue("columnFive"));
        assertTrue(results.isNull("columnSix"));
        assertNull(results.getValue("columnSix"));
        if (checkEOF) {
            assertFalse(results.next());
        }
    }

    protected void _verifyMetadata(MocaResults results) {
        assertEquals(6, results.getColumnCount());
        int one = results.getColumnNumber("columnOne");
        int two = results.getColumnNumber("columntwo");
        int three = results.getColumnNumber("columnThree");
        int four = results.getColumnNumber("columnFour");
        int five = results.getColumnNumber("columnFive");
        int six = results.getColumnNumber("columnSix");

        assertTrue(results.containsColumn("columnOne"));
        assertTrue(results.containsColumn("columnTwo"));
        assertTrue(results.containsColumn("columnThree"));
        assertTrue(results.containsColumn("columnFour"));
        assertTrue(results.containsColumn("columnFive"));
        assertTrue(results.containsColumn("columnSix"));
        
        if (_checkColumnPosition()) {
            assertEquals(0, one);
            assertEquals(1, two);
            assertEquals(2, three);
            assertEquals(3, four);
            assertEquals(4, five);
            assertEquals(5, six);
        }
        else {
            assertTrue(one != -1);
            assertTrue(two != -1);
            assertTrue(three != -1);
            assertTrue(four != -1);
            assertTrue(five != -1);
            assertTrue(six != -1);
        }
        
        assertEquals("columnOne", results.getColumnName(one));
        assertEquals("columnTwo", results.getColumnName(two));
        assertEquals("columnThree", results.getColumnName(three));
        assertEquals("columnFour", results.getColumnName(four));
        assertEquals("columnFive", results.getColumnName(five));
        assertEquals("columnSix", results.getColumnName(six));
        
        assertEquals(MocaType.STRING, results.getColumnType(one));
        assertEquals(MocaType.DATETIME, results.getColumnType(two));
        assertEquals(MocaType.INTEGER, results.getColumnType(three));
        assertEquals(MocaType.DOUBLE, results.getColumnType(four));
        assertEquals(MocaType.BOOLEAN, results.getColumnType(five));
        assertEquals(MocaType.BINARY, results.getColumnType(six));

        assertEquals(MocaType.STRING, results.getColumnType("columnOne"));
        assertEquals(MocaType.DATETIME, results.getColumnType("columnTwo"));
        assertEquals(MocaType.INTEGER, results.getColumnType("columnThree"));
        assertEquals(MocaType.DOUBLE, results.getColumnType("columnFour"));
        assertEquals(MocaType.BOOLEAN, results.getColumnType("columnFive"));
        assertEquals(MocaType.BINARY, results.getColumnType("columnSix"));
    }

    /**
     * Returns a MocaResults object for testing. Subclasses should return
     * an object that that has the following characteristics:
     * Six columns, named columnOne through columnSix, with the following
     * datatypes:
     * <table border=1>
     * <tr>
     *  <th>columnOne (String)</td>
     *  <th>columnTwo (DateTime)</td>
     *  <th>columnThree (Integer)</td>
     *  <th>columnFour (Double)</td>
     *  <th>columnFive (Boolean)</td>
     *  <th>columnSix (Binary)</td>
     * </th>
     * <tr>
     *  <td>"Hello"</td>
     *  <td>(current time)</td>
     *  <td>1</td>
     *  <td>3.142</td>
     *  <td>true</td>
     *  <td>[0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00, 0x7f]</td>
     * </tr>
     * <tr>
     *  <td>"Blah "</td>
     *  <td>(current time + 1 hour)</td>
     *  <td>900</td>
     *  <td>(Math.E)</td>
     *  <td>false</td>
     *  <td>"foo".getBytes()</td>
     * </tr>
     * <tr>
     *  <td>null</td>
     *  <td>null</td>
     *  <td>null</td>
     *  <td>null</td>
     *  <td>null</td>
     *  <td>null</td>
     * </tr>
     * </table>
     * @return
     */
    protected abstract MocaResults _createSampleResults();
    
    protected boolean _checkColumnPosition() {
        return true;
    }
}
