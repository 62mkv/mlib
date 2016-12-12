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

import junit.framework.TestCase;

/**
 * Unit test for Options class.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_Options extends TestCase {
    public void testNoOptionsNoArgs() throws OptionsException {
        Options opt = Options.parse("", new String[0]);
        String[] remaining = opt.getRemainingArgs();
        assertNotNull(remaining);
        assertEquals(0, remaining.length);
    }
    
    public void testNoOptionsExtraArgs() throws OptionsException  {
        Options opt = Options.parse("", new String[] {"abc", "123"});
        String[] remaining = opt.getRemainingArgs();
        assertNotNull(remaining);
        assertEquals(2, remaining.length);
        assertEquals("abc", remaining[0]);
        assertEquals("123", remaining[1]);
    }
    
    public void testNoOptionsWrongOptionPassed() throws OptionsException  {
        try {
            Options.parse("", new String[] {"-f", "abc"});
            fail("Expected Exception");
        }
        catch (OptionsException e) {
            // Normal
        }
    }
    
    public void testSimpleOptionNoArgs() throws OptionsException  {
        Options opt = Options.parse("f", new String[0]);
        String[] remaining = opt.getRemainingArgs();
        assertFalse(opt.isSet('f'));
        assertNull(opt.getArgument('f'));
        assertNotNull(remaining);
        assertEquals(0, remaining.length);
    }

    public void testSimpleOptionArgOnly() throws OptionsException  {
        Options opt = Options.parse("f", new String[] {"-f"});
        String[] remaining = opt.getRemainingArgs();
        assertTrue(opt.isSet('f'));
        assertNull(opt.getArgument('f'));
        assertNotNull(remaining);
        assertEquals(0, remaining.length);
    }

    public void testSimpleOptionExtraArg() throws OptionsException  {
        Options opt = Options.parse("f", new String[] {"-f", "abc", "123"});
        String[] remaining = opt.getRemainingArgs();
        assertTrue(opt.isSet('f'));
        assertNull(opt.getArgument('f'));
        assertNotNull(remaining);
        assertEquals(2, remaining.length);
        assertEquals("abc", remaining[0]);
        assertEquals("123", remaining[1]);
    }

    public void testSimpleOptionWrongOptionPassed() throws OptionsException  {
        try {
            Options.parse("f", new String[] {"-f", "-a"});
            fail("Expected Exception");
        }
        catch (OptionsException e) {
            // Normal
        }
    }
    
    public void testComplexOptions() throws OptionsException  {
        Options opt = Options.parse("abct:o:",
                new String[] {"-act*", "-o", "optionarg", "remaining"});
        assertTrue(opt.isSet('a'));
        assertNull(opt.getArgument('a'));
        assertTrue(opt.isSet('c'));
        assertNull(opt.getArgument('c'));
        assertTrue(opt.isSet('t'));
        assertEquals("*", opt.getArgument('t'));
        assertTrue(opt.isSet('o'));
        assertEquals("optionarg", opt.getArgument('o'));
        String[] remaining = opt.getRemainingArgs();
        assertNotNull(remaining);
        assertEquals(1, remaining.length);
        assertEquals("remaining", remaining[0]);
    }

    public void testEndOfOptionsMarker() throws OptionsException  {
        Options opt = Options.parse("abct:o:",
                new String[] {"-a", "-c", "-t*", "--", "-o", "optionarg", "remaining"});
        assertTrue(opt.isSet('a'));
        assertTrue(opt.isSet('c'));
        assertTrue(opt.isSet('t'));
        assertEquals("*", opt.getArgument('t'));
        assertFalse(opt.isSet('o'));
        String[] remaining = opt.getRemainingArgs();
        assertNotNull(remaining);
        assertEquals(3, remaining.length);
        assertEquals("-o", remaining[0]);
        assertEquals("optionarg", remaining[1]);
        assertEquals("remaining", remaining[2]);
    }

    public void testNoArgumentForOption() throws OptionsException  {
        try {
            Options.parse("ab:", new String[] {"-a", "-b"});
            fail("Expected Exception");
        }
        catch (OptionsException e) {
            // Normal
        }
    }

    public void testOptionalArgumentFlag() throws OptionsException{
    	Options opt = Options.parse("t;o", new String[] {"-t", "-o"});
    	assertTrue(opt.isSet('t'));
    	assertTrue(opt.isSet('o'));
    	assertNull("Expected Null", opt.getArgument('t'));
    	
    }
    public void testOptionalArgumentFlag2() throws OptionsException{
    	Options opt = Options.parse("t;o", new String[] {"-t","*", "-o"});
    	assertTrue(opt.isSet('t'));
    	assertTrue(opt.isSet('o'));
    	assertEquals("*", opt.getArgument('t'));
    }
    
    public void testOptionalArgumentFlag3() throws OptionsException{
    	Options opt = Options.parse("t;o", new String[] {"-t*", "-o"});
    	assertTrue(opt.isSet('t'));
    	assertTrue(opt.isSet('o'));
    	assertEquals("*", opt.getArgument('t'));
    }
    public void testOptionalArgumentFlag4() throws OptionsException{
    	Options opt = Options.parse("t;o", new String[] {"-t"});
    	assertTrue(opt.isSet('t'));
    	assertFalse(opt.isSet('o'));
    	assertNull("Expected Null", opt.getArgument('t'));
    }
    
    
}