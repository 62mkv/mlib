/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.redprairie.util;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_Base64 extends TestCase {
    public void testEncode() throws Exception {
        byte[] byteArray = {
                (byte) 0x19, (byte) 0xf2, (byte) 0x23, (byte) 0x34,
                (byte) 0xd5, (byte) 0x2a, (byte) 0x46, (byte) 0xbe
        };

        StringBuilder out = new StringBuilder();
        Base64.encode(new ByteArrayInputStream(byteArray), out);
        assertEquals("GfIjNNUqRr4=", out.toString());
    }

    public void testDecode() throws Exception {
        byte[] byteArray = {
                (byte) 0x19, (byte) 0xf2, (byte) 0x23, (byte) 0x34,
                (byte) 0xd5, (byte) 0x2a, (byte) 0x46, (byte) 0xbe
        };
        
        byte[] decoded = Base64.decode("GfIjNNUqRr4=");

        assertEquals(byteArray.length, decoded.length);
        assertTrue(Arrays.equals(byteArray, decoded));
        if (!Arrays.equals(byteArray, decoded)) {
            for (int i = 0; i < byteArray.length; i++) {
                assertEquals("difference at byte " + i, byteArray[i], decoded[i]);
            }
        }
    }

    public void testEncodeDecodeString() throws Exception {
        String testString = 
            "Man is distinguished, not only by his reason, but by this singular passion from other animals, " +
            "which is a lust of the mind, that by a perseverance of delight in the continued and indefatigable " +
            "generation of knowledge, exceeds the short vehemence of any carnal pleasure.";
        
        String testOutput = 
            "TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlz" +
            "IHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2Yg" +
            "dGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGlu" +
            "dWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRo" +
            "ZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=";
        
        StringBuilder out = new StringBuilder();
        Base64.encode(new ByteArrayInputStream(testString.getBytes("ASCII")), out);
        
        assertEquals(testOutput, out.toString());
        
        assertEquals(testString, new String(Base64.decode(testOutput), "UTF-8"));
    }
}
