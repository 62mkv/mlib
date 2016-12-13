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
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * This class is to test the different moca commands for converting columns
 * to either a string or multi column values
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_ConversionService extends AbstractMocaTestCase {

    /**
     * This will test to make sure that calling convert column results to string
     * is working correctly.
     */
    public void testConvertColumnsToString() {
        MocaResults res = null;
        try {
            res = _moca.executeCommand(
                    "[[" +
                    "    res = moca.newResults();" +
                    "    for (i in 0..10) {" +
                    "        res.addRow([col1:'a' + i,col2:'b'])"  +
                    "    }" +
                    "]]" +
                    "|" +
                    "convert column results to string" +
                    "  where resultset = @res" +
                    "    and colnam = 'col1'" +
                    "    and separator = ';'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected Moca Exception encountered: " + e);
        }
        
        RowIterator rowIterator = res.getRows();
        
        // There should be only 1 row
        assertTrue("We didn't get any rows", rowIterator.next());
        // It should return the values back separated by ; in result_string
        assertEquals("a0;a1;a2;a3;a4;a5;a6;a7;a8;a9;a10", 
                rowIterator.getString("result_string"));
        assertFalse("There was more than 1 row returned", rowIterator.next());
    }
    
    /**
     * This will test to make sure that calling convert columns to multicolumn
     * is working correctly.
     */
    public void testConvertColumnsToMultiColumn() {
        MocaResults res = null;
        try {
            res = _moca.executeCommand(
                    "[[" +
                    "    res = moca.newResults();" +
                    "    for (i in 0..10) {" +
                    "        res.addRow([col1:'a' + i,col2:'b'])"  +
                    "    }" +
                    "]]" + 
                    "|" +
                    "convert column to multicolumn" +
                    "  where resultset = @res" +
                    "    and colnam = 'col1'" +
                    "    and column_count = 3");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected Moca Exception encountered: " + e);
        }
        
        RowIterator rowIterator = res.getRows();
        int row = 0;
        
        // This is to check they are returned in the correct order
        // Should be:
        //    a0    a4    a8
        //    a1    a5    a9
        //    a2    a6    a10
        //    a3    a7
        //    a4    a8
        while (rowIterator.next()) {
            assertEquals("a" + row, rowIterator.getString("col1_0"));
            assertEquals("a" + (row + 4), rowIterator.getString("col1_1"));
            if (row + 8 <= 10) { 
                assertEquals("a" + (row + 8), rowIterator.getString("col1_2"));
            }
            row++;
        }
        
    }
    
    /**
     * This method tests the convert list commands to make sure it supports
     * the single quote type correctly. 
     */
    public void testConverListSingleQuoteType() {
        MocaResults res = null;
        try {
            res = _moca.executeCommand(
                    "convert list" +
                    "  where string = 'a,b,c,d'" +
                    "    and type = 'S'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected Moca Exception encountered: " + e);
        }
        
        RowIterator rowIterator = res.getRows();
        
        // There should be only 1 row
        assertTrue("We didn't get any rows", rowIterator.next());
        // It should return the values back separated by ' in retstr
        assertEquals("'a','b','c','d'", rowIterator.getString("retstr"));
        assertEquals(4, rowIterator.getInt("count"));
        assertFalse("There was more than 1 row returned", rowIterator.next());
    }
    
    /**
     * This method tests the convert list commands to make sure it supports
     * the double quote type correctly. 
     */
    public void testConverListDoubleQuoteType() {
        MocaResults res = null;
        try {
            res = _moca.executeCommand(
                    "convert list" +
                    "  where string = 'a,b,c,d'" +
                    "    and type = 'D'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected Moca Exception encountered: " + e);
        }
        
        RowIterator rowIterator = res.getRows();
        
        // There should be only 1 row
        assertTrue("We didn't get any rows", rowIterator.next());
        // It should return the values back separated by " in retstr
        assertEquals("\"a\",\"b\",\"c\",\"d\"", rowIterator.getString("retstr"));
        assertEquals(4, rowIterator.getInt("count"));
        assertFalse("There was more than 1 row returned", rowIterator.next());
    }
    
    /**
     * This method tests the convert list commands to make sure it supports
     * the list type correctly.  This also tests a different separator being :
     */
    public void testConverListListType() {
        MocaResults res = null;
        try {
            res = _moca.executeCommand(
                    "convert list" +
                    "  where string = 'a:b:c:d'" +
                    "    and type = 'L'" +
                    "    and separator = ':'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected Moca Exception encountered: " + e);
        }
        
        RowIterator rowIterator = res.getRows();
        
        // There should be only 1 row
        assertTrue("We didn't get any rows", rowIterator.next());
        // We need to check each row, there should be 4
        assertEquals("a", rowIterator.getString("retstr"));
        assertEquals(1, rowIterator.getInt("count"));
        
        assertTrue("We should have gotten 4 rows", rowIterator.next());
        assertEquals("b", rowIterator.getString("retstr"));
        assertEquals(2, rowIterator.getInt("count"));
        
        assertTrue("We should have gotten 4 rows", rowIterator.next());
        assertEquals("c", rowIterator.getString("retstr"));
        assertEquals(3, rowIterator.getInt("count"));
        
        assertTrue("We should have gotten 4 rows", rowIterator.next());
        assertEquals("d", rowIterator.getString("retstr"));
        assertEquals(4, rowIterator.getInt("count"));
        
        assertFalse("There was more than 5 row returned", rowIterator.next());
    }
    
    public void testConvertListTrim() {
        MocaResults res = null;
        try {
            res = _moca.executeCommand(
                    "convert list" +
                    "  where string = 'a, b, c'" +
                    "    and type = 'S'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected Moca Exception encountered: " + e);
        }
        
        RowIterator rowIterator = res.getRows();

        // There should be only 1 row
        assertTrue("We didn't get any rows", rowIterator.next());
        assertEquals("'a','b','c'", rowIterator.getString("retstr"));
        assertFalse("There was more than 1 row returned", rowIterator.next());
    }
    
    public void testConvertListWithRegexCharacter() {
        MocaResults res = null;
        try {
            res = _moca.executeCommand(
                    "convert list" +
                    "  where string = 'a.b.c'" +
                    "    and type = 'L'" +
                    "    and separator = '.'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected Moca Exception encountered: " + e);
        }
        
        RowIterator rowIterator = res.getRows();
        
        assertTrue("We didn't get any rows", rowIterator.next());
        // We need to check each row, there should be 3
        assertEquals("a", rowIterator.getString("retstr"));
        assertEquals(1, rowIterator.getInt("count"));
        
        assertTrue("We should have gotten 3 rows", rowIterator.next());
        assertEquals("b", rowIterator.getString("retstr"));
        assertEquals(2, rowIterator.getInt("count"));
        
        assertTrue("We should have gotten 3 rows", rowIterator.next());
        assertEquals("c", rowIterator.getString("retstr"));
        assertEquals(3, rowIterator.getInt("count"));
        
        assertFalse("There was more than 3 rows returned", rowIterator.next());
    }
}
