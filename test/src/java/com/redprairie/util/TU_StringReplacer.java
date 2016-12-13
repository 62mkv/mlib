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

package com.redprairie.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.redprairie.util.test.MapReplacementStrategy;

/**
 * Unit tests for StringReplacer utility class.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */
public class TU_StringReplacer extends TestCase {
    
    public void testSingleCharDelimiter() {
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put("a", "value of a");
        mapping.put("b", "value of b");
        mapping.put("abcdefghijk", "value number 3");
        StringReplacer replacer = new StringReplacer('%', new MapReplacementStrategy(mapping));

        assertEquals("", replacer.translate(""));
        assertEquals("Test", replacer.translate("Test"));
        assertEquals("%nothere%", replacer.translate("%nothere%"));
        assertEquals("%notclosed", replacer.translate("%notclosed"));
        assertEquals("value of a", replacer.translate("%a%"));
        assertEquals("value of b", replacer.translate("%b%"));
        assertEquals("value of avalue of b", replacer.translate("%a%%b%"));
        assertEquals("This is value number 3, mkay?", replacer.translate("This is %abcdefghijk%, mkay?"));
    }
    
    public void testMalformedTemplate() {
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put("a", "value of a");
        mapping.put("b", "value of b");
        StringReplacer replacer = new StringReplacer('^', new MapReplacementStrategy(mapping));
        assertEquals("Test value of a ^b", replacer.translate("Test ^a^ ^b"));
    }
}
