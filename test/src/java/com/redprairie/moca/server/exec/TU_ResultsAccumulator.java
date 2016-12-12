/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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

package com.redprairie.moca.server.exec;

import org.junit.Test;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.server.TypeMismatchException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for ResultsAccumulator class.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_ResultsAccumulator {

    @Test
    public void testIdenticalMetadata() throws Exception {
        EditableResults initialResults = new SimpleResults();
        initialResults.addColumn("foo", MocaType.INTEGER);
        initialResults.addColumn("bar", MocaType.STRING);
        initialResults.addRow();
        initialResults.setIntValue("foo", -3);
        initialResults.setStringValue("bar", "blah");

        EditableResults additionalResults = new SimpleResults();
        additionalResults.addColumn("foo", MocaType.INTEGER);
        additionalResults.addColumn("bar", MocaType.STRING);
        additionalResults.addRow();
        additionalResults.setIntValue("foo", 4);
        additionalResults.setStringValue("bar", "xxxx");
        
        ResultsAccumulator accum = new ResultsAccumulator();
        
        accum.addResults(initialResults);
        accum.addResults(additionalResults);
        
        MocaResults results = accum.getResults();
        
        assertEquals(2, results.getColumnCount());
        assertEquals(2, results.getRowCount());
        
        assertTrue(results.containsColumn("foo"));
        assertTrue(results.containsColumn("bar"));
        assertEquals(MocaType.INTEGER, results.getColumnType("foo"));
        assertEquals(MocaType.STRING, results.getColumnType("bar"));
        assertTrue(results.next());
        assertEquals(-3, results.getInt("foo"));
        assertEquals("blah", results.getString("bar"));
        assertTrue(results.next());
        assertEquals(4, results.getInt("foo"));
        assertEquals("xxxx", results.getString("bar"));
        assertFalse(results.next());
    }

    @Test
    public void testSameMetadataDifferentOrder() throws Exception {
        EditableResults initialResults = new SimpleResults();
        initialResults.addColumn("foo", MocaType.INTEGER);
        initialResults.addColumn("bar", MocaType.STRING);
        initialResults.addRow();
        initialResults.setIntValue("foo", -3);

        initialResults.setStringValue("bar", "blah");
        EditableResults additionalResults = new SimpleResults();
        additionalResults.addColumn("bar", MocaType.STRING);
        additionalResults.addColumn("foo", MocaType.INTEGER);
        additionalResults.addRow();
        additionalResults.setIntValue("foo", 4);
        additionalResults.setStringValue("bar", "xxxx");
        
        ResultsAccumulator accum = new ResultsAccumulator();
        
        accum.addResults(initialResults);
        accum.addResults(additionalResults);
        
        MocaResults results = accum.getResults();
        
        assertEquals(2, results.getColumnCount());
        assertEquals(2, results.getRowCount());
        
        assertTrue(results.containsColumn("foo"));
        assertTrue(results.containsColumn("bar"));
        assertEquals(MocaType.INTEGER, results.getColumnType("foo"));
        assertEquals(MocaType.STRING, results.getColumnType("bar"));
        assertTrue(results.next());
        assertEquals(-3, results.getInt("foo"));
        assertEquals("blah", results.getString("bar"));
        assertTrue(results.next());
        assertEquals(4, results.getInt("foo"));
        assertEquals("xxxx", results.getString("bar"));
        assertFalse(results.next());
    }

    @Test
    public void testPromotionIntToString() throws Exception {
        EditableResults initialResults = new SimpleResults();
        initialResults.addColumn("foo", MocaType.INTEGER);
        initialResults.addColumn("bar", MocaType.STRING);
        initialResults.addRow();
        initialResults.setIntValue("foo", -3);
        initialResults.setStringValue("bar", "blah");

        EditableResults additionalResults = new SimpleResults();
        additionalResults.addColumn("foo", MocaType.STRING);
        additionalResults.addColumn("bar", MocaType.STRING);
        additionalResults.addRow();
        additionalResults.setStringValue("foo", "ooo");
        additionalResults.setStringValue("bar", "xxxx");
        
        ResultsAccumulator accum = new ResultsAccumulator();
        
        accum.addResults(initialResults);
        accum.addResults(additionalResults);
        
        MocaResults results = accum.getResults();
        
        assertEquals(2, results.getColumnCount());
        assertEquals(2, results.getRowCount());
        
        assertTrue(results.containsColumn("foo"));
        assertTrue(results.containsColumn("bar"));
        assertEquals(MocaType.STRING, results.getColumnType("foo"));
        assertEquals(MocaType.STRING, results.getColumnType("bar"));
        assertTrue(results.next());
        assertEquals("-3", results.getString("foo"));
        assertEquals("blah", results.getString("bar"));
        assertTrue(results.next());
        assertEquals("ooo", results.getString("foo"));
        assertEquals("xxxx", results.getString("bar"));
        assertFalse(results.next());
    }

    @Test
    public void testPromotionIntToDouble() throws Exception {
        EditableResults initialResults = new SimpleResults();
        initialResults.addColumn("foo", MocaType.DOUBLE);
        initialResults.addColumn("bar", MocaType.STRING);
        initialResults.addRow();
        initialResults.setDoubleValue("foo", -3);
        initialResults.setStringValue("bar", "blah");

        EditableResults additionalResults = new SimpleResults();
        additionalResults.addColumn("foo", MocaType.INTEGER);
        additionalResults.addColumn("bar", MocaType.INTEGER);
        additionalResults.addRow();
        additionalResults.setIntValue("foo", 4);
        additionalResults.setIntValue("bar", -9);
        
        ResultsAccumulator accum = new ResultsAccumulator();
        
        accum.addResults(initialResults);
        accum.addResults(additionalResults);
        
        MocaResults results = accum.getResults();
        
        assertEquals(2, results.getColumnCount());
        assertEquals(2, results.getRowCount());
        
        assertTrue(results.containsColumn("foo"));
        assertTrue(results.containsColumn("bar"));
        assertEquals(MocaType.DOUBLE, results.getColumnType("foo"));
        assertEquals(MocaType.STRING, results.getColumnType("bar"));
        assertTrue(results.next());
        assertEquals(-3.0, results.getDouble("foo"), 0.0);
        assertEquals("blah", results.getString("bar"));
        assertTrue(results.next());
        assertEquals(4.0, results.getDouble("foo"), 0.0);
        assertEquals("-9", results.getString("bar"));
        assertFalse(results.next());
    }

    @Test
    public void testPromotionNotPossible() throws Exception {
        EditableResults initialResults = new SimpleResults();
        initialResults.addColumn("foo", MocaType.STRING);
        initialResults.addColumn("bar", MocaType.STRING);
        initialResults.addRow();
        initialResults.setStringValue("foo", "xxx");
        initialResults.setStringValue("bar", "blah");

        EditableResults additionalResults = new SimpleResults();
        additionalResults.addColumn("foo", MocaType.STRING);
        additionalResults.addColumn("bar", MocaType.OBJECT);
        additionalResults.addRow();
        additionalResults.setStringValue("foo", "blah");
        additionalResults.setValue("bar", new Object());
        
        ResultsAccumulator accum = new ResultsAccumulator();
        
        accum.addResults(initialResults);
        try {
            accum.addResults(additionalResults);
            fail("Expected Type Mismatch");
        }
        catch (TypeMismatchException e) {
            // Normal
        }
    }

    
    @Test
    public void testDuplicateColumn() throws Exception {
        EditableResults initialResults = new SimpleResults();
        initialResults.addColumn("foo", MocaType.STRING);
        initialResults.addColumn("foo", MocaType.STRING);
        initialResults.addRow();
        initialResults.setStringValue(0, "foo");
        initialResults.setStringValue(1, "foo too");

        EditableResults additionalResults = new SimpleResults();
        additionalResults.addColumn("foo", MocaType.STRING);
        additionalResults.addColumn("foo", MocaType.STRING);
        additionalResults.addRow();
        additionalResults.setStringValue(0, "bar");
        additionalResults.setStringValue(1, "bar too");
        
        ResultsAccumulator accum = new ResultsAccumulator();
        
        accum.addResults(initialResults);
        accum.addResults(additionalResults);
        
        MocaResults results = accum.getResults();
        
        assertEquals(2, results.getColumnCount());
        assertEquals(2, results.getRowCount());
        
        assertTrue(results.containsColumn("foo"));
        assertEquals(MocaType.STRING, results.getColumnType(0));
        assertEquals(MocaType.STRING, results.getColumnType(1));
        assertEquals("foo", results.getColumnName(0));
        assertEquals("foo", results.getColumnName(1));
        assertTrue(results.next());
        assertEquals("foo", results.getValue(0));
        assertEquals("foo too", results.getValue(1));
        assertTrue(results.next());
        assertEquals("bar", results.getValue(0));
        assertEquals("bar too", results.getValue(1));
        assertFalse(results.next());
    }

    @Test
    public void testDuplicateColumnMoreColumnsForFirst() throws Exception {
        EditableResults initialResults = new SimpleResults();
        initialResults.addColumn("foo", MocaType.STRING);
        initialResults.addColumn("foo", MocaType.STRING);
        initialResults.addRow();
        initialResults.setStringValue(0, "foo");
        initialResults.setStringValue(1, "foo too");

        EditableResults additionalResults = new SimpleResults();
        additionalResults.addColumn("foo", MocaType.STRING);
        additionalResults.addRow();
        additionalResults.setStringValue(0, "bar");
        
        ResultsAccumulator accum = new ResultsAccumulator();
        
        accum.addResults(initialResults);
        accum.addResults(additionalResults);
        
        MocaResults results = accum.getResults();
        
        assertEquals(2, results.getColumnCount());
        assertEquals(2, results.getRowCount());
        
        assertTrue(results.containsColumn("foo"));
        assertEquals(MocaType.STRING, results.getColumnType(0));
        assertEquals(MocaType.STRING, results.getColumnType(1));
        assertEquals("foo", results.getColumnName(0));
        assertEquals("foo", results.getColumnName(1));
        assertTrue(results.next());
        assertEquals("foo", results.getValue(0));
        assertEquals("foo too", results.getValue(1));
        assertTrue(results.next());
        assertEquals("bar", results.getValue(0));
        assertNull(results.getValue(1));
        assertFalse(results.next());
    }
    
    @Test
    public void testDuplicateColumnsWithDifferentTypes() throws Exception {
        EditableResults initialResults = new SimpleResults();
        initialResults.addColumn("foo", MocaType.INTEGER);
        initialResults.addColumn("foo", MocaType.STRING);
        initialResults.addRow();
        initialResults.setIntValue(0, 100);
        initialResults.setStringValue(1, "foo too");

        EditableResults additionalResults = new SimpleResults();
        additionalResults.addColumn("foo", MocaType.INTEGER);
        additionalResults.addColumn("foo", MocaType.STRING);
        additionalResults.addRow();
        additionalResults.setIntValue(0, 101);
        additionalResults.setStringValue(1, "bar too");
        
        ResultsAccumulator accum = new ResultsAccumulator();
        
        accum.addResults(initialResults);
        accum.addResults(additionalResults);
        
        MocaResults results = accum.getResults();
        
        assertEquals(2, results.getColumnCount());
        assertEquals(2, results.getRowCount());
        
        assertTrue(results.containsColumn("foo"));
        assertEquals(MocaType.INTEGER, results.getColumnType(0));
        assertEquals(MocaType.STRING, results.getColumnType(1));
        assertEquals("foo", results.getColumnName(0));
        assertEquals("foo", results.getColumnName(1));
        assertTrue(results.next());
        assertEquals(100, results.getValue(0));
        assertEquals("foo too", results.getValue(1));
        assertTrue(results.next());
        assertEquals(101, results.getValue(0));
        assertEquals("bar too", results.getValue(1));
        assertFalse(results.next());
    }
    
    @Test
    public void testDuplicateColumnMultiples() throws Exception {
        EditableResults initialResults = new SimpleResults();
        initialResults.addColumn("foo", MocaType.STRING);
        initialResults.addColumn("foo", MocaType.STRING);
        initialResults.addColumn("foo", MocaType.STRING);
        initialResults.addRow();
        initialResults.setStringValue(0, "foo");
        initialResults.setStringValue(1, "foo too");
        initialResults.setStringValue(2, "foo three");

        EditableResults additionalResults = new SimpleResults();
        additionalResults.addColumn("foo", MocaType.STRING);
        additionalResults.addColumn("foo", MocaType.STRING);
        additionalResults.addColumn("foo", MocaType.STRING);
        additionalResults.addRow();
        additionalResults.setStringValue(0, "bar");
        additionalResults.setStringValue(1, "bar too");
        additionalResults.setStringValue(2, "bar three");
        
        ResultsAccumulator accum = new ResultsAccumulator();
        
        accum.addResults(initialResults);
        accum.addResults(additionalResults);
        
        MocaResults results = accum.getResults();
        
        assertEquals(3, results.getColumnCount());
        assertEquals(2, results.getRowCount());
        
        assertTrue(results.containsColumn("foo"));
        assertEquals(MocaType.STRING, results.getColumnType(0));
        assertEquals(MocaType.STRING, results.getColumnType(1));
        assertEquals(MocaType.STRING, results.getColumnType(2));
        assertEquals("foo", results.getColumnName(0));
        assertEquals("foo", results.getColumnName(1));
        assertEquals("foo", results.getColumnName(2));
        assertTrue(results.next());
        assertEquals("foo", results.getValue(0));
        assertEquals("foo too", results.getValue(1));
        assertEquals("foo three", results.getValue(2));
        assertTrue(results.next());
        assertEquals("bar", results.getValue(0));
        assertEquals("bar too", results.getValue(1));
        assertEquals("bar three", results.getValue(2));
        assertFalse(results.next());
    }
}