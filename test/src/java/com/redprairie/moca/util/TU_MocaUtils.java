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

package com.redprairie.moca.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.mockito.Mockito;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.components.base.TU_FileDirectoryService;
import com.redprairie.moca.util.test.FileCreationTestUtility;

/**
 * Unit test for MocaUtils class.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_MocaUtils extends TestCase {
    public void testCopyResults() {
        EditableResults orig = new SimpleResults();
        orig.addColumn("AAA", MocaType.STRING);
        orig.addColumn("BBB", MocaType.STRING);
        orig.addColumn("AAA", MocaType.INTEGER);
        orig.addRow();
        orig.setStringValue(0, "aaa-000");
        orig.setStringValue(1, "bbb-000");
        orig.setIntValue(2, -5);
        
        EditableResults copy = new SimpleResults();
        MocaUtils.copyResults(copy, orig);
        assertEquals(3, copy.getColumnCount());
        assertEquals("AAA", copy.getColumnName(0));
        assertEquals("BBB", copy.getColumnName(1));
        assertEquals("AAA", copy.getColumnName(2));
        assertTrue(copy.next());
        assertEquals("aaa-000", copy.getString(0));
        assertEquals("bbb-000", copy.getString(1));
        assertEquals(-5, copy.getInt(2));
        assertFalse(copy.next());
    }    
    
    public void testCopyColumns() {
        EditableResults orig = new SimpleResults();
        orig.addColumn("AAA", MocaType.STRING);
        orig.addColumn("BBB", MocaType.STRING);
        orig.addColumn("CCC", MocaType.INTEGER);
        orig.addRow();
        orig.setStringValue(0, "aaa-000");
        orig.setStringValue(1, "bbb-000");
        orig.setIntValue(2, -5);
        
        EditableResults copy = new SimpleResults();
        MocaUtils.copyColumns(copy, orig);
        assertEquals(3, copy.getColumnCount());
        assertEquals("AAA", copy.getColumnName(0));
        assertEquals("BBB", copy.getColumnName(1));
        assertEquals("CCC", copy.getColumnName(2));
        assertEquals(MocaType.STRING, copy.getColumnType(0));
        assertEquals(MocaType.STRING, copy.getColumnType(1));
        assertEquals(MocaType.INTEGER, copy.getColumnType(2));
        assertFalse(copy.next());
    }    
    
    public void testCopyColumnsWithDuplicates() {
        EditableResults orig = new SimpleResults();
        orig.addColumn("AAA", MocaType.STRING);
        orig.addColumn("BBB", MocaType.STRING);
        orig.addColumn("AAA", MocaType.INTEGER);
        
        EditableResults copy = new SimpleResults();
        MocaUtils.copyColumns(copy, orig);
        assertEquals(3, copy.getColumnCount());
        assertEquals("AAA", copy.getColumnName(0));
        assertEquals("BBB", copy.getColumnName(1));
        assertEquals("AAA", copy.getColumnName(2));
        assertEquals(MocaType.STRING, copy.getColumnType(0));
        assertEquals(MocaType.STRING, copy.getColumnType(1));
        assertEquals(MocaType.INTEGER, copy.getColumnType(2));
    }    
    
    public void testCopyColumnsWithoutDuplicates() {
        EditableResults orig = new SimpleResults();
        orig.addColumn("AAA", MocaType.STRING);
        orig.addColumn("BBB", MocaType.STRING);
        orig.addColumn("AAA", MocaType.INTEGER);
        
        EditableResults copy = new SimpleResults();
        MocaUtils.copyColumns(copy, orig, true);
        assertEquals(2, copy.getColumnCount());
        assertEquals("AAA", copy.getColumnName(0));
        assertEquals("BBB", copy.getColumnName(1));
        assertEquals(MocaType.STRING, copy.getColumnType(0));
        assertEquals(MocaType.STRING, copy.getColumnType(1));
    }    
    
    public void testCopyResultsToObject() {
        EditableResults orig = new SimpleResults();
        orig.addColumn("aaa", MocaType.STRING);
        orig.addColumn("bbb", MocaType.STRING);
        orig.addColumn("ccc", MocaType.INTEGER);
        orig.addRow();
        orig.setStringValue("aaa", "aaa-000");
        orig.setStringValue("bbb", "bbb-000");
        orig.setIntValue("ccc", -5);
        
        TestBean[] resultArray = MocaUtils.createObjectArray(TestBean.class, orig);
        assertNotNull(resultArray);
    }
    
    public void testCopyResultsColumn() {
        EditableResults orig = new SimpleResults();
        EditableResults sub = new SimpleResults();
        sub.addColumn("xxx", MocaType.STRING);
        sub.addRow();
        sub.setStringValue("xxx", "xxx-000");
        
        orig.addColumn("aaa", MocaType.STRING);
        orig.addColumn("bbb", MocaType.RESULTS);
        orig.addRow();
        orig.setStringValue("aaa", "aaa-000");
        orig.setResultsValue("bbb", sub);
        
        EditableResults copy = new SimpleResults();

        MocaUtils.copyResults(copy, orig);
        
        assertTrue(copy.containsColumn("aaa"));
        assertTrue(copy.containsColumn("bbb"));
        assertEquals(MocaType.STRING, copy.getColumnType("aaa"));
        assertEquals(MocaType.RESULTS, copy.getColumnType("bbb"));
        assertTrue(copy.next());
        
        assertEquals("aaa-000", copy.getString("aaa"));
        MocaResults subCopy = copy.getResults("bbb");
        assertTrue(subCopy.containsColumn("xxx"));
        assertTrue(subCopy.next());
        assertEquals("xxx-000", subCopy.getString("xxx"));
        
        assertSame(sub, subCopy);
    }
    
    public void testTransformColumns() {
        EditableResults orig = new SimpleResults();
        orig.addColumn("AAA", MocaType.STRING);
        orig.addColumn("BBB", MocaType.STRING);
        orig.addColumn("CCC", MocaType.INTEGER);
        orig.addRow();
        orig.setStringValue(0, "aaa-000");
        orig.setStringValue(1, "bbb-000");
        orig.setIntValue(2, -5);
        
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put("aaa", "foo");
        mapping.put("BBB", "bar");
        mapping.put("cCc", "BAZ");
        
        EditableResults copy = new SimpleResults();
        MocaUtils.transformColumns(copy, orig, false, mapping);
        assertEquals(3, copy.getColumnCount());
        assertEquals("foo", copy.getColumnName(0));
        assertEquals("bar", copy.getColumnName(1));
        assertEquals("BAZ", copy.getColumnName(2));
        assertEquals(MocaType.STRING, copy.getColumnType(0));
        assertEquals(MocaType.STRING, copy.getColumnType(1));
        assertEquals(MocaType.INTEGER, copy.getColumnType(2));
        assertFalse(copy.next());
        
    }
    
    public void testTransformColumnsWithDuplicates() {
        EditableResults orig = new SimpleResults();
        orig.addColumn("AAA", MocaType.STRING);
        orig.addColumn("BBB", MocaType.STRING);
        orig.addColumn("AAA", MocaType.INTEGER);
        orig.addRow();
        orig.setStringValue(0, "aaa-000");
        orig.setStringValue(1, "bbb-000");
        orig.setIntValue(2, -5);
        
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put("aaa", "foo");
        mapping.put("bbb", "bar");
        mapping.put("ccc", "baz");
        
        EditableResults copy = new SimpleResults();
        MocaUtils.transformColumns(copy, orig, false, mapping);
        assertEquals(3, copy.getColumnCount());
        assertEquals("foo", copy.getColumnName(0));
        assertEquals("bar", copy.getColumnName(1));
        assertEquals("foo", copy.getColumnName(2));
        assertEquals(MocaType.STRING, copy.getColumnType(0));
        assertEquals(MocaType.STRING, copy.getColumnType(1));
        assertEquals(MocaType.INTEGER, copy.getColumnType(2));
        assertFalse(copy.next());
        
    }
    

    /**
     * This method tests to make sure that a file will get copied correctly
     * It will read in the file and then delete it again
     * This test is very similar to the FileDirectoryService to test copy file 
     * moca command
     * @see TU_FileDirectoryService#testFileCopy()
     */
    public void testFileCopy() {
        FileCreationTestUtility fileCreationTestUtility = 
            new FileCreationTestUtility();
        String prefix = "unittest";
        String fileInput = "This is a temporary file for testing\nIt will be " +
                "deleted/t so don't worry!\n$!!00testXA~";
        
        File tempFile = fileCreationTestUtility.createTempFileAndInsertValues(
                prefix, fileInput, null);
        
        // Create a target file in the same directory but call it test.copy
        String targetFile = tempFile.getParent() + File.separator + 
                "test.copy";
        try {
            MocaUtils.copyFile(tempFile.getAbsolutePath(), targetFile);
        }
        catch (MocaIOException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception: " + e);
        }
        
        File createdFile = new File(targetFile);
        
        fileCreationTestUtility.compareFileContentsWithString(createdFile, 
                fileInput, null);
    }
    
    /**
     * This will test to make sure that environment variables are expanded
     * correctly
     */
    public void testExpandEnvironmentVariables() {
        String path = "%MOCADIR%\\src\\java";
        MocaContext moca = Mockito.mock(MocaContext.class);
        
        Mockito.when(moca.getSystemVariable("MOCADIR")).thenReturn(
                "c:\\dev\\trunk\\moca");
        
        String translatedString = MocaUtils.expandEnvironmentVariables(moca, 
                path);
        
        assertEquals("Translated string was not correct: ", 
                "c:\\dev\\trunk\\moca\\src\\java", translatedString);
    }
    
    public static class TestBean {
        public String getAaa() {
            return _aaa;
        }
        public void setAaa(String aaa) {
            _aaa = aaa;
        }
        public String getBbb() {
            return _bbb;
        }
        public void setBbb(String bbb) {
            _bbb = bbb;
        }
        public int getCcc() {
            return _ccc;
        }
        public void setCcc(int ccc) {
            _ccc = ccc;
        }

        private String _aaa;
        private String _bbb;
        private int _ccc;
    }
}
