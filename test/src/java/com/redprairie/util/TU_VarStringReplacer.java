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
public class TU_VarStringReplacer extends TestCase {
    
    public TU_VarStringReplacer() {
        Map<String, String> mapping = new HashMap<String, String>();
        mapping.put("a", "value of a");
        mapping.put("b", "value of b");
        mapping.put("abcdefghijk", "value number 3");
        replacer = new VarStringReplacer(new MapReplacementStrategy(mapping));
    }
    
    public void testNoReplacement() {
        assertEquals("", replacer.translate(""));
        assertEquals("Test", replacer.translate("Test"));
    }
    
    public void testNoValue() {
        assertEquals("%nothere%", replacer.translate("%nothere%"));
        assertEquals("%notclosed", replacer.translate("%notclosed"));
        assertEquals("$nothere", replacer.translate("$nothere"));
        assertEquals("$nothere abc", replacer.translate("$nothere abc"));
        assertEquals("$etcvalue of a", replacer.translate("$etc$a"));
    }
    
    public void testSimpleReplacement() {
        assertEquals("value of a", replacer.translate("$a"));
        assertEquals("value of a", replacer.translate("${a}"));
        assertEquals("value of a", replacer.translate("%a%"));
    }
    
    public void testInlineReplacement() {
        assertEquals("value of b foo", replacer.translate("$b foo"));
        assertEquals("value of b foo", replacer.translate("${b} foo"));
        assertEquals("value of b foo", replacer.translate("%b% foo"));
        assertEquals("barvalue of a foo", replacer.translate("bar$a foo"));
        assertEquals("barvalue of a foo", replacer.translate("bar${a} foo"));
        assertEquals("barvalue of a foo", replacer.translate("bar%a% foo"));
    }
    
    public void testAdjacentReplacement() {
        assertEquals("value of avalue of b", replacer.translate("$a$b"));
        assertEquals("value of avalue of b", replacer.translate("%a%%b%"));
        assertEquals("value of avalue of b", replacer.translate("${a}$b"));
        assertEquals("This is value number 3, mkay?", replacer.translate("This is %abcdefghijk%, mkay?"));
    }
    
    public void testMixedUpVariables() {
        assertEquals("%a$b%${b%a}", replacer.translate("%a$b%${b%a}"));
        assertEquals("$$$$$$", replacer.translate("$$$$$$"));
        assertEquals("%%%", replacer.translate("%%%"));
        assertEquals("%%", replacer.translate("%%"));
    }
    
    private VarStringReplacer replacer; 

}
