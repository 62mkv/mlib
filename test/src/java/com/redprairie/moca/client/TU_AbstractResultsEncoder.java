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

package com.redprairie.moca.client;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import junit.framework.TestCase;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.SimpleResults;

/**
 * Tests that ResultsEncoder and ResultsDecoder work together in all legal
 * cases.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public abstract class TU_AbstractResultsEncoder extends TestCase {
    public void testEmptyResults()  throws Exception {
        EditableResults res = new SimpleResults();
        _compareResults(res, _makeCopy(res));
    }
    
    public void testMetadataOnlyNoLengths()  throws Exception {
        EditableResults res = new SimpleResults();
        res.addColumn("aaaaaaa", MocaType.STRING);
        res.addColumn("bbbbbbb", MocaType.BOOLEAN);
        res.addColumn("ccccccc", MocaType.DOUBLE);
        res.addColumn("ddddddd", MocaType.INTEGER);
        res.addColumn("eeeeeee", MocaType.BINARY);
        res.addColumn("fffffff", MocaType.DATETIME);
        res.addColumn("ggggggg", MocaType.RESULTS);
        _compareResults(res, _makeCopy(res));
    }
    
    public void testMetadataOnly()  throws Exception {
        EditableResults res = new SimpleResults();
        _defaultColumns(res);
        _compareResults(res, _makeCopy(res));
    }
    
    public void testNullData()  throws Exception {
        EditableResults res = new SimpleResults();
        _defaultColumns(res);
        res.addRow();
        _compareResults(res, _makeCopy(res));
    }
    
    public void testSingleDataRow()  throws Exception {
        EditableResults res = new SimpleResults();
        _defaultColumns(res);
        res.addRow();
        res.setStringValue("aaaaaaa", "Hello, World");
        res.setBooleanValue("bbbbbbb", true);
        res.setDoubleValue("ccccccc", 68.231);
        res.setIntValue("ddddddd", 123456789);
        byte[] someData = new byte[5000];
        _rand.nextBytes(someData);
        res.setBinaryValue("eeeeeee", someData);
        res.setDateValue("fffffff", new Date());
        EditableResults sub = new SimpleResults();
        _defaultColumns(sub);
        res.setResultsValue("ggggggg", sub);
        _compareResults(res, _makeCopy(res));
    }
    
    public void testMulitpleDataRows()  throws Exception {
        EditableResults res = new SimpleResults();
        _defaultColumns(res);
        for (int i = 0; i < 20; i++) {
            res.addRow();
            res.setStringValue("aaaaaaa", "Hello, World");
            res.setBooleanValue("bbbbbbb", true);
            res.setDoubleValue("ccccccc", 68.231);
            res.setIntValue("ddddddd", 123456789);
            byte[] someData = new byte[5000];
            _rand.nextBytes(someData);
            res.setBinaryValue("eeeeeee", someData);
            res.setDateValue("fffffff", new Date());
            EditableResults sub = new SimpleResults();
            _defaultColumns(sub);
            res.setResultsValue("ggggggg", sub);
        }
        _compareResults(res, _makeCopy(res));
    }
    
    public void testSpecialColumnNames()  throws Exception {
        EditableResults res = new SimpleResults();
        res.addColumn("\"\"\"", MocaType.STRING);
        res.addColumn("<<<", MocaType.STRING);
        res.addColumn("&&&", MocaType.STRING);
        res.addColumn("'''", MocaType.STRING);
        res.addColumn(">>>", MocaType.STRING);
        res.addColumn("<tag attr1='hello' attr2=\"& goodbye\">", MocaType.STRING);
        res.addRow();
        res.setStringValue(0, "Column named \"\"\"");
        res.setStringValue(1, "Column named <<<");
        res.setStringValue(2, "Column named &&&");
        res.setStringValue(3, "Column named '''");
        res.setStringValue(4, "Column >>>");
        res.setStringValue(5, "Column named <tag attr1='hello' attr2=\"& goodbye\">");
        _compareResults(res, _makeCopy(res));
    }
    
    protected abstract MocaResults _makeCopy(MocaResults res) throws Exception;
    
    protected void _compareResults(MocaResults orig, MocaResults copy) {
        if (orig == null) {
            assertNull(copy);
            return;
        }
        else {
            assertNotNull(copy);
        }
        
        assertEquals(orig.getRowCount(), copy.getRowCount());
        int columns = orig.getColumnCount();
        
        for (int c = 0; c < columns; c++) {
            assertEquals("Column " + c, orig.getColumnName(c), copy.getColumnName(c));
            assertEquals("Column " + c, orig.getColumnType(c), copy.getColumnType(c));
            assertEquals("Column " + c, orig.isNullable(c), copy.isNullable(c));
            assertEquals("Column " + c, orig.getMaxLength(c), copy.getMaxLength(c));
        }
        
        while (orig.next()) {
            assertTrue(copy.next());
            for (int c = 0; c < columns; c++) {
                MocaType type = orig.getColumnType(c);
                if (type.equals(MocaType.RESULTS)) {
                    _compareResults(orig.getResults(c), copy.getResults(c));
                }
                else if (type.equals(MocaType.BINARY)) {
                    byte[] origData = (byte[])orig.getValue(c);
                    byte[] copyData = (byte[])copy.getValue(c);
                    assertTrue("Column " + c, Arrays.equals(origData, copyData));
                }
                else if (type.equals(MocaType.UNKNOWN)) {
                    // Ignore unknown data types...
                }
                else if (type.equals(MocaType.DATETIME)) {
                    Date origDate = orig.getDateTime(c);
                    Date copyDate = copy.getDateTime(c);
                    
                    // Throw out the sub-second time on the original. Our
                    // Encoding format destroys that information.
                    if (origDate != null) {
                        Calendar tmp = Calendar.getInstance();
                        tmp.setTime(origDate);
                        tmp.set(Calendar.MILLISECOND, 0);
                        origDate = tmp.getTime();
                    }
                    assertEquals("Column " + c, origDate, copyDate);
                }
                else {
                    assertEquals("Column " + c, orig.getValue(c), copy.getValue(c));
                }
            }
        }
        assertFalse(copy.next());
    }

    //
    // Implementation
    //
    
    private void _defaultColumns(EditableResults res) {
        res.addColumn("aaaaaaa", MocaType.STRING, 100);
        res.addColumn("bbbbbbb", MocaType.BOOLEAN, 1);
        res.addColumn("ccccccc", MocaType.DOUBLE, 8);
        res.addColumn("ddddddd", MocaType.INTEGER, 4);
        res.addColumn("eeeeeee", MocaType.BINARY, 5000);
        res.addColumn("fffffff", MocaType.DATETIME, 14);
        res.addColumn("ggggggg", MocaType.RESULTS, 0);
        
    }
    
    private Random _rand = new Random();
}
