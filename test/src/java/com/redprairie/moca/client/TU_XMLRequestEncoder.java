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

package com.sam.moca.client;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.sam.moca.MocaArgument;
import com.sam.moca.MocaOperator;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.SimpleResults;
import com.sam.moca.util.MocaUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_XMLRequestEncoder {

    /**
     * Test method for {@link com.sam.moca.client.XMLRequestEncoder#encodeRequest(java.lang.String, java.lang.String, java.lang.String, boolean, com.sam.moca.MocaArgument[], com.sam.moca.MocaArgument[], java.lang.Appendable)}.
     */
    @Test
    public void testEncodeSimpleRequest() throws Exception {
        StringBuilder buffer = new StringBuilder();
        XMLRequestEncoder.encodeRequest("foo bar", null, null, true, false, null, null, buffer);
        assertEquals("<moca-request autocommit=\"true\"><query>foo bar</query></moca-request>", buffer.toString());
    }

    /**
     * Test method for {@link com.sam.moca.client.XMLRequestEncoder#buildXMLEnvironmentString(java.util.Map)}.
     */
    @Test
    public void testEncodeDecodeComplexRequest() throws Exception {
        StringBuilder buffer = new StringBuilder();
        
        Map<String, String> env = new HashMap<String, String>();
        env.put("a", "AAAA Value");
        env.put("b", "b value");
        env.put("c", "");
        String envString = XMLRequestEncoder.buildXMLEnvironmentString(env);
        
        MocaArgument[] context = new MocaArgument[] {
                new MocaArgument("www", "string value"),
                new MocaArgument("xxx", MocaOperator.LT, MocaType.DOUBLE, 3.14),
                new MocaArgument("yyy", MocaType.INTEGER, null),
                new MocaArgument("aaa", MocaType.BOOLEAN, false),
                new MocaArgument("bbb", MocaType.DATETIME, MocaUtils.parseDate("2010404010913"))
        };
        
        MocaArgument[] args = new MocaArgument[] {
                new MocaArgument("zzz", MocaOperator.LIKE, MocaType.STRING, "WM%")
        };
        
        XMLRequestEncoder.encodeRequest("do something where x = @+xxx and @*", "session-00001", envString, false, false, context, args, buffer);
        System.out.println("encoded = [" + buffer + "]");
        XMLRequestDecoder decoder = new XMLRequestDecoder(new ByteArrayInputStream(buffer.toString().getBytes(Charset.forName("UTF-8"))));
        decoder.decode();
        assertEquals("do something where x = @+xxx and @*", decoder.getQuery());
        assertEquals("session-00001", decoder.getSessionId());
        assertFalse(decoder.isAutoCommit());
        
        Map<String, String> resultEnv = decoder.getEnv();
        
        Map<String, String> upperEnv = new HashMap<String, String>();
        upperEnv.put("A", "AAAA Value");
        upperEnv.put("B", "b value");
        upperEnv.put("C", "");
                
        assertEquals(upperEnv, resultEnv);
        
        List<MocaArgument> tmp = decoder.getContext();
        assertEquals(Arrays.asList(context), tmp);
        tmp = decoder.getArgs();
        assertEquals(Arrays.asList(args), tmp);
    }
    
    /**
     * Test method for {@link com.sam.moca.client.XMLRequestEncoder#buildXMLEnvironmentString(java.util.Map)}.
     */
    @Test
    public void testEncodeDecodeResultRequest() throws Exception {
        StringBuilder buffer = new StringBuilder();
        
        SimpleResults innerResults = new SimpleResults();
        
        innerResults.addColumn("aaa", MocaType.BINARY);
        innerResults.addColumn("bbb", MocaType.BOOLEAN);
        innerResults.addColumn("ccc", MocaType.DATETIME);
        innerResults.addColumn("ddd", MocaType.DOUBLE);
        innerResults.addColumn("eee", MocaType.INTEGER);
        innerResults.addColumn("ggg", MocaType.STRING);
        
        innerResults.addRow();
        
        innerResults.setBinaryValue(0, new byte[]{0xf, 0xb, 0x3});
        innerResults.setBooleanValue(1, false);
        innerResults.setDateValue(2, new Date());
        innerResults.setDoubleValue(3, 23.4);
        innerResults.setIntValue(4, 14421);
        innerResults.setStringValue(5, "testfoo");
        
        SimpleResults outerResults = new SimpleResults();
        
        outerResults.addColumn("rrr", MocaType.RESULTS);
        outerResults.addColumn("sss", MocaType.BINARY);
        
        outerResults.addRow();
        
        outerResults.setResultsValue(0, innerResults);
        outerResults.setBinaryValue(1, new byte[]{0xf, 0xc, 0x3, 0x5});
        
        MocaArgument[] context = new MocaArgument[] {
                new MocaArgument("results", MocaType.RESULTS, outerResults),
                new MocaArgument("binary", MocaType.BINARY, new byte[]{0x3, 0x4, 0x2})
        };
        
        MocaArgument[] args = new MocaArgument[] {
                new MocaArgument("zzz", MocaOperator.LIKE, MocaType.STRING, "WM%")
        };
        
        XMLRequestEncoder.encodeRequest("do something where x = @+xxx and @*", "session-00001", 
            null, false, false, context, args, buffer);
        
        System.out.println("encoded = [" + buffer + "]");
        XMLRequestDecoder decoder = new XMLRequestDecoder(new ByteArrayInputStream(buffer.toString().getBytes(Charset.forName("UTF-8"))));
        decoder.decode();
        assertEquals("do something where x = @+xxx and @*", decoder.getQuery());
        assertEquals("session-00001", decoder.getSessionId());
        assertFalse(decoder.isAutoCommit());
        
        List<MocaArgument> tmp = decoder.getContext();
        assertEquals(2, tmp.size());
        MocaArgument arg = tmp.get(0);
        MocaResults res = (MocaResults)arg.getValue();
        
        _compareResults(outerResults, res);
        
        MocaArgument arg2 = tmp.get(1);
        
        assertEquals(0, ((byte[])arg2.getValue()).length);
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
}
