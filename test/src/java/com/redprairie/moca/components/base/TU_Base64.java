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

package com.sam.moca.components.base;

import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.util.AbstractMocaTestCase;

/**
 * This class is to test the components that deal with base 64 encoding
 * and decoding
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_Base64 extends AbstractMocaTestCase {
    /**
     * This method tests a complex encoding to ensure validity of the base64
     * encoding command
     */
    public void testComplexEncode() {
        MocaResults res = null;
        try {
            res = _moca.executeCommand(
                    "[[bin_data = '" + _decoded + "'.getBytes()]]" +
                    "|" +
                    "encode to base64" +
                    "  where bin_data = @bin_data");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected Moca Exception encountered: " + e);
        }
        
        assertTrue("There were no rows.", res.next());
        
        assertEquals("The encoding failed", _encoded, res.getString("base64_encoded"));
    }
    
    /**
     * This method tests a complex decoding to ensure validity of the base64
     * decoding command
     */
    public void testComplexDecode() {
        MocaResults res = null;
        try {
            res = _moca.executeCommand(
                    "decode from base64" +
                    "  where string = '" + _encoded + "'" +
                    "|" +
                    "[[decoded = new String(base64_decoded).toString()]]");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected Moca Exception encountered: " + e);
        }
        
        assertTrue("There were no rows.", res.next());
        
        assertEquals("The decoding failed", _decoded, res.getString("decoded"));
    }
    
    private final static String _encoded = 
            "TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZ"
            + "WFzb24sIGJ1dCBieSB0aGlzIHNpbmd1bGFyIHBhc3Npb24gZnJvbS"
            + "BvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2YgdGhlIG1"
            + "pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBp"
            + "biB0aGUgY29udGludWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyY"
            + "XRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRoZSBzaG9ydCB2ZW"
            + "hlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=";
    
    private final static String _decoded = 
            "Man is distinguished, not only by his reason, "
            + "but by this singular passion from other animals, "
            + "which is a lust of the mind, that by a perseverance "
            + "of delight in the continued and indefatigable "
            + "generation of knowledge, exceeds the short vehemence "
            + "of any carnal pleasure.";
}
