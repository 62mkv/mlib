/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.sam.moca.client;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.sam.moca.MocaArgument;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.RowIterator;
import com.sam.moca.SimpleResults;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_NormalizedContextEncoder {
    public void testEncodeDecodeResultRequest(Charset charset) throws Exception {
        StringBuilder buffer = new StringBuilder();
        
        SimpleResults innerResults = new SimpleResults();
        
        innerResults.addColumn("aaa", MocaType.BINARY);
        innerResults.addColumn("bbb", MocaType.BOOLEAN);
        innerResults.addColumn("ccc", MocaType.DATETIME);
        innerResults.addColumn("ddd", MocaType.DOUBLE);
        innerResults.addColumn("eee", MocaType.INTEGER);
        innerResults.addColumn("ggg", MocaType.STRING);
        
        innerResults.addRow();
        
        innerResults.setBinaryValue(0, new byte[] { 0xf, 0xb, 0x3, 0x73, 0x64,
                0x1f, 0x23, 0x4, 0x2, 1, 20, (byte) 224, (byte) 225,
                (byte) 226, (byte) 227, (byte) 228, (byte) 229, (byte) 230,
                (byte) 231, (byte) 232, (byte) 233, (byte) 234, (byte) 235,
                (byte) 236, (byte) 237, (byte) 238, (byte) 239, (byte) 240,
                (byte) 241, (byte) 242, (byte) 243 });
        innerResults.setBooleanValue(1, false);
        innerResults.setDateValue(2, new Date());
        innerResults.setDoubleValue(3, 23.4);
        innerResults.setIntValue(4, 14421);
        // Note this contains a character that requires more than one byte
        innerResults.setStringValue(5, "testfooë");
        
        SimpleResults outerResults = new SimpleResults();
        
        outerResults.addColumn("rrr", MocaType.RESULTS);
        outerResults.addColumn("sss", MocaType.BINARY);
        
        outerResults.addRow();
        
        outerResults.setResultsValue(0, innerResults);
        outerResults.setBinaryValue(1, new byte[]{0xe, 0xc, 0x3, 0x5});
        
        MocaArgument[] context = new MocaArgument[] {
                new MocaArgument("results", MocaType.RESULTS, outerResults),
                new MocaArgument("binary", MocaType.BINARY, new byte[] { 0x73,
                        0x64, 0x1f, 0x4, 0x2 })
        };
        
        MocaArgument[] args = new MocaArgument[] {
                new MocaArgument("integer", MocaType.INTEGER, 23),
                new MocaArgument("string", MocaType.STRING, "moca")
        };
        
        NormalizedContextEncoder encoder = new NormalizedContextEncoder(charset.name());
        encoder.encodeContext(context, buffer);
        buffer.append("|");
        encoder.encodeContext(args, buffer);
        
        System.out.println("encoded = [" + buffer + "]");
        NormalizedContextDecoder decoder = new NormalizedContextDecoder(
            buffer.toString(), charset);
        decoder.decode();
        
        List<MocaArgument> tmp = decoder.getContext();
        assertEquals(2, tmp.size());
        MocaArgument arg = tmp.get(0);
        MocaResults res = (MocaResults)arg.getValue();
        
        _compareResults(outerResults, res);
        
        assertEquals(0, ((byte[])tmp.get(1).getValue()).length);
        
        List<MocaArgument> returnArgs = decoder.getArgs();
        assertEquals(2, returnArgs.size());
        arg = returnArgs.get(0);
        assertEquals(args[0], arg);
        
        arg = returnArgs.get(1);
        assertEquals(args[1], arg);
    }
    
    @Test
    public void testWithWindows1254() throws Exception {
        testEncodeDecodeResultRequest(Charset.forName("windows-1254"));
    }
    
    @Test
    public void testWithUTF8() throws Exception {
        testEncodeDecodeResultRequest(Charset.forName("UTF-8"));
    }
    
    @Test
    public void testEmptyContext() throws Exception {
        StringBuilder buffer = new StringBuilder();
        Charset utf8 = Charset.forName("UTF-8");
        
        NormalizedContextEncoder encoder = new NormalizedContextEncoder(utf8.name());
        encoder.encodeContext(new MocaArgument[]{}, buffer);
        buffer.append("|");
        encoder.encodeContext(new MocaArgument[]{}, buffer);
        
        NormalizedContextDecoder decoder = new NormalizedContextDecoder(
            buffer.toString(), utf8);
        
        decoder.decode();
        
        assertTrue(decoder.getArgs().isEmpty());
        
        assertTrue(decoder.getContext().isEmpty());
    }
    
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
        
        RowIterator origIterator = orig.getRows();
        RowIterator copyIterator = copy.getRows();
        while (origIterator.next()) {
            assertTrue(copyIterator.next());
            for (int c = 0; c < columns; c++) {
                MocaType type = orig.getColumnType(c);
                if (type.equals(MocaType.RESULTS)) {
                    _compareResults(origIterator.getResults(c), copyIterator.getResults(c));
                }
                else if (type.equals(MocaType.BINARY)) {
                    byte[] origData = (byte[])origIterator.getValue(c);
                    byte[] copyData = (byte[])copyIterator.getValue(c);
                    assertTrue("Column " + c, Arrays.equals(origData, copyData));
                }
                else if (type.equals(MocaType.UNKNOWN)) {
                    // Ignore unknown data types...
                }
                else if (type.equals(MocaType.DATETIME)) {
                    Date origDate = origIterator.getDateTime(c);
                    Date copyDate = copyIterator.getDateTime(c);
                    
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
                    assertEquals("Column " + c, origIterator.getValue(c), copyIterator.getValue(c));
                }
            }
        }
        assertFalse(copyIterator.next());
    }
}
