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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.SimpleResults;

/**
 * Unit tests for ResultMapper class.  Tests creating MocaResults objects
 * from various different objects/priminives. 
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_ResultMapper extends TestCase {

    public void testInt() throws Exception {
        int value = -5;
        MocaResults results = ResultMapper.createResults(Integer.valueOf(value));
        try {
            assertTrue(results.containsColumn("result"));
            assertEquals(0, results.getColumnNumber("result"));
            assertEquals(MocaType.INTEGER, results.getColumnType("result"));
            assertTrue(results.next());
            assertEquals(value, results.getInt("result"));
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }
    
    public void testIntArray() throws Exception {
        int[] values = {1, 1, 2, 3, 5, 8, 13, 21, 34};
        MocaResults results = ResultMapper.createResults(values);
        try {
            assertTrue(results.containsColumn("result"));
            assertEquals(0, results.getColumnNumber("result"));
            assertEquals(MocaType.INTEGER, results.getColumnType("result"));
            for (int i = 0; i < values.length; i++) {
                assertTrue(results.next());
                assertEquals(values[i], results.getInt("result"));
            }
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }

    public void testEmptyIntArray() throws Exception {
        int[] values = new int[0];
        MocaResults results = ResultMapper.createResults(values);
        try {
            assertTrue(results.containsColumn("result"));
            assertEquals(0, results.getColumnNumber("result"));
            assertEquals(MocaType.INTEGER, results.getColumnType("result"));
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }
    
    public void testDouble() throws Exception {
        double value = 4.21;
        MocaResults results = ResultMapper.createResults(Double.valueOf(value));
        try {
            assertTrue(results.containsColumn("result"));
            assertEquals(0, results.getColumnNumber("result"));
            assertEquals(MocaType.DOUBLE, results.getColumnType("result"));
            assertTrue(results.next());
            assertEquals(value, results.getDouble("result"), 0.0);
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }
    
    public void testDoubleArray() throws Exception {
        double[] values = {0.1, Math.E, 2.0, Math.PI};
        MocaResults results = ResultMapper.createResults(values);
        try {
            assertTrue(results.containsColumn("result"));
            assertEquals(0, results.getColumnNumber("result"));
            assertEquals(MocaType.DOUBLE, results.getColumnType("result"));
            for (int i = 0; i < values.length; i++) {
                assertTrue(results.next());
                assertEquals(values[i], results.getDouble("result"), 0.0);
            }
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }

    public void testEmptyDoubleArray() throws Exception {
        double[] values = new double[0];
        MocaResults results = ResultMapper.createResults(values);
        try {
            assertTrue(results.containsColumn("result"));
            assertEquals(0, results.getColumnNumber("result"));
            assertEquals(MocaType.DOUBLE, results.getColumnType("result"));
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }
    
    public void testBoolean() throws Exception {
        boolean value = false;
        MocaResults results = ResultMapper.createResults(Boolean.valueOf(value));
        try {
            assertTrue(results.containsColumn("result"));
            assertEquals(0, results.getColumnNumber("result"));
            assertEquals(MocaType.BOOLEAN, results.getColumnType("result"));
            assertTrue(results.next());
            assertEquals(value, results.getBoolean("result"));
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }
    
    public void testBooleanArray() throws Exception {
        boolean[] values = {true, true, false};
        MocaResults results = ResultMapper.createResults(values);
        try {
            assertTrue(results.containsColumn("result"));
            assertEquals(0, results.getColumnNumber("result"));
            assertEquals(MocaType.BOOLEAN, results.getColumnType("result"));
            for (int i = 0; i < values.length; i++) {
                assertTrue(results.next());
                assertEquals(values[i], results.getBoolean("result"));
            }
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }

    public void testEmptyBooleanArray() throws Exception {
        boolean[] values = new boolean[0];
        MocaResults results = ResultMapper.createResults(values);
        try {
            assertTrue(results.containsColumn("result"));
            assertEquals(0, results.getColumnNumber("result"));
            assertEquals(MocaType.BOOLEAN, results.getColumnType("result"));
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }
    
    public void testBinary() throws Exception {
        byte[] value = "testing".getBytes(Charset.forName("UTF-8"));
        MocaResults results = ResultMapper.createResults(value);
        try {
            assertTrue(results.containsColumn("result"));
            assertEquals(0, results.getColumnNumber("result"));
            assertEquals(MocaType.BINARY, results.getColumnType("result"));
            assertTrue(results.next());
            assertTrue(Arrays.equals(value, (byte[])results.getValue("result")));
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }
    
    public void testBinaryArray() throws Exception {
        byte[][] values = {"test one".getBytes(Charset.forName("UTF-8")), "test two".getBytes(Charset.forName("UTF-8")), {0x02, 0x03}};
        MocaResults results = ResultMapper.createResults(values);
        try {
            assertTrue(results.containsColumn("result"));
            assertEquals(0, results.getColumnNumber("result"));
            assertEquals(MocaType.BINARY, results.getColumnType("result"));
            for (int i = 0; i < values.length; i++) {
                assertTrue(results.next());
                assertTrue(Arrays.equals(values[i], (byte[])results.getValue("result")));
            }
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }

    public void testEmptyBinaryArray() throws Exception {
        byte[][] values = new byte[0][];
        MocaResults results = ResultMapper.createResults(values);
        try {
            assertTrue(results.containsColumn("result"));
            assertEquals(0, results.getColumnNumber("result"));
            assertEquals(MocaType.BINARY, results.getColumnType("result"));
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }
    
    public void testString() throws Exception {
        String value = "testing";
        MocaResults results = ResultMapper.createResults(value);
        try {
            assertTrue(results.containsColumn("result"));
            assertEquals(0, results.getColumnNumber("result"));
            assertEquals(MocaType.STRING, results.getColumnType("result"));
            assertTrue(results.next());
            assertEquals(value, results.getString("result"));
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }
    
    public void testStringArray() throws Exception {
        String[] values = {"test one", "test two"};
        MocaResults results = ResultMapper.createResults(values);
        try {
            assertTrue(results.containsColumn("result"));
            assertEquals(0, results.getColumnNumber("result"));
            assertEquals(MocaType.STRING, results.getColumnType("result"));
            for (int i = 0; i < values.length; i++) {
                assertTrue(results.next());
                assertEquals(values[i], results.getString("result"));
            }
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }
    
    public void testEmptyStringArray() throws Exception {
        String[] values = new String[0];
        MocaResults results = ResultMapper.createResults(values);
        try {
            assertTrue(results.containsColumn("result"));
            assertEquals(0, results.getColumnNumber("result"));
            assertEquals(MocaType.STRING, results.getColumnType("result"));
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }
    
    public void testSimpleResults() throws Exception {
        EditableResults orig = new SimpleResults();
        orig.addColumn("aaa", MocaType.STRING);
        orig.addColumn("bbb", MocaType.INTEGER);
        orig.addRow();
        orig.setStringValue("aaa", "Row 1");
        orig.setIntValue("bbb", -2);
        
        MocaResults results = ResultMapper.createResults(orig);
        try {
            assertTrue(results.containsColumn("aaa"));
            assertTrue(results.containsColumn("bbb"));
            assertEquals(0, results.getColumnNumber("aaa"));
            assertEquals(1, results.getColumnNumber("bbb"));
            assertEquals(MocaType.STRING, results.getColumnType("aaa"));
            assertEquals(MocaType.INTEGER, results.getColumnType("bbb"));
            assertTrue(results.next());
            assertEquals("Row 1", results.getString("aaa"));
            assertEquals(-2, results.getInt("bbb"));
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }
       
    public void testBean() throws Exception {
        MocaResults results = ResultMapper.createResults(new TestBean());
        try {
            assertTrue(results.containsColumn("blah"));
            assertEquals(MocaType.STRING, results.getColumnType("blah"));
            assertTrue(results.next());
            assertEquals("Hello", results.getString("blah"));
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }
    
    public void testBeanArray() throws Exception {
        TestBean[] values = new TestBean[10];
        for (int i = 0; i < values.length; i++) {
            values[i] = new TestBean();
        }
        
        MocaResults results = ResultMapper.createResults(values);
        try {
            assertTrue(results.containsColumn("blah"));
            assertEquals(MocaType.STRING, results.getColumnType("blah"));
            for (int i = 0; i < values.length; i++) {
                assertTrue(results.next());
                assertEquals("Hello", results.getString("blah"));
            }
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }
    
    public void testBeanCollection() throws Exception {
        Collection<TestBean> values = new ArrayList<TestBean>();
        for (int i = 0; i < 10; i++) {
            values.add(new TestBean());
        }
        
        MocaResults results = ResultMapper.createResults(values);
        try {
            assertTrue(results.containsColumn("blah"));
            assertEquals(MocaType.STRING, results.getColumnType("blah"));
            for (TestBean dummy : values) {
                assertTrue(results.next());
                assertEquals(dummy.getBlah(), results.getString("blah"));
            }
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }
    
    public void testEmptyBeanCollection() throws Exception {
        Collection<TestBean> values = new ArrayList<TestBean>();

        MocaResults results = ResultMapper.createResults(values);
        try {
            assertEquals(0, results.getColumnCount());
            assertFalse(results.next());
        }
        finally {
            results.close();
        }
    }
    
    public static class TestBean {
        public String getBlah() {
            return "Hello";
        }
    }

}
