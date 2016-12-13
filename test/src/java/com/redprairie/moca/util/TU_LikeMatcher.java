/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20167
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

package com.redprairie.moca.util;

import junit.framework.TestCase;

/**
 * @author derek
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TU_LikeMatcher extends TestCase {

    /**
     * Constructor for FunkyMatcherTest.
     * @param arg0
     */
    public TU_LikeMatcher(String name) {
        super(name);
    }

    public void testEmptyMatch() {
        LikeMatcher m = new LikeMatcher("");
        assertTrue(m.match(""));
        assertFalse(m.match("a"));
    }

    public void testExactMatch() {
        LikeMatcher m = new LikeMatcher("testing");
        assertTrue(m.match("testing"));
        assertFalse(m.match(""));
        assertFalse(m.match("wrong"));
        assertFalse(m.match("containstesting"));
        assertFalse(m.match("testingcontained"));
        assertFalse(m.match("atestingexample"));
        assertFalse(m.match("testing*"));
    }

    public void testWildcardAtStart() {
        LikeMatcher m = new LikeMatcher("%a");
        assertTrue(m.match("testa"));
        assertTrue(m.match("a"));
        assertTrue(m.match("ba"));
        assertTrue(m.match("aaaa"));
        assertFalse(m.match(""));
        assertFalse(m.match("b"));
        assertFalse(m.match("ab"));
        assertFalse(m.match("bab"));
    }

    public void testWildcardAtEnd() {
        LikeMatcher m = new LikeMatcher("a%");
        assertTrue(m.match("atest"));
        assertTrue(m.match("a"));
        assertTrue(m.match("ab"));
        assertTrue(m.match("aaaa"));
        assertFalse(m.match(""));
        assertFalse(m.match("b"));
        assertFalse(m.match("ba"));
        assertFalse(m.match("bab"));
    }

    public void testWildcardInMiddle() {
        LikeMatcher m = new LikeMatcher("a%b");
        assertTrue(m.match("atestb"));
        assertTrue(m.match("ab"));
        assertTrue(m.match("aaaabbb"));
        assertTrue(m.match("ababab"));
        assertFalse(m.match(""));
        assertFalse(m.match("ba"));
        assertFalse(m.match("bab"));
        assertFalse(m.match("x"));
    }

    public void testSingleCharWildcard() {
        LikeMatcher m = new LikeMatcher("_");
        assertTrue(m.match("a"));
        assertTrue(m.match("b"));
        assertTrue(m.match("*"));
        assertTrue(m.match("?"));
        assertFalse(m.match(""));
        assertFalse(m.match("aa"));
        assertFalse(m.match("bb"));
    }

    public void testSingleCharWildcard_MultipleTimes() {
        LikeMatcher m = new LikeMatcher("___");
        assertTrue(m.match("aaa"));
        assertTrue(m.match("abc"));
        assertTrue(m.match("***"));
        assertTrue(m.match("..."));
        assertFalse(m.match(""));
        assertFalse(m.match("aa"));
        assertFalse(m.match("123456789"));
    }

    public void testOverlappingPattern() {
        LikeMatcher m = new LikeMatcher("%aaa_bbb%");
        assertTrue(m.match("aaaxbbb"));
        assertTrue(m.match("testingaaaaabbbc"));
        assertTrue(m.match("*aaafooledyouaaabbbaaaabbb*"));
        assertFalse(m.match(""));
        assertFalse(m.match("aaabbb"));
        assertFalse(m.match("xaaabbb"));
        assertFalse(m.match("123456789"));
        
        m = new LikeMatcher("aaa%aab");
        assertFalse(m.match("aaab"));
    }

}