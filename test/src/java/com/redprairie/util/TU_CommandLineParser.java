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

package com.redprairie.util;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unit tests for ClassUtils
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */

public class TU_CommandLineParser {

    @Test
    public void testSimpleCommand() {
        runTest("Test", "Test");
    }
    
    @Test
    public void testSimpleCommandLine() {
        runTest("Test One, Two", "Test", "One,", "Two");
    }
    
    @Test
    public void testJavaCommandLine() {
        runTest("java -Xmx1024m -Djava.library.path=$MOCADIR/lib com.redprairie.foo.FooClass arg1",
                "java", "-Xmx1024m", "-Djava.library.path=$MOCADIR/lib", "com.redprairie.foo.FooClass", "arg1");
    }
    
    @Test
    public void testQuotedCommandLine() {
        runTest("java -Xmx1024m -Djava.library.path=\"$MOCADIR/lib\" com.redprairie.foo.FooClass arg1",
                "java", "-Xmx1024m", "-Djava.library.path=$MOCADIR/lib", "com.redprairie.foo.FooClass", "arg1");
        runTest("java \"This is a test\"   arg1",
            "java", "This is a test", "arg1");
        runTest("java 'Another \"test\" for you'  arg1",
            "java", "Another \"test\" for you", "arg1");
        runTest("test quotes'\" in the middle\" of'\" another \"arg etc etc",
            "test", "quotes\" in the middle\" of another arg", "etc", "etc");
    }
    
    @Test
    public void testUnbalancedQuotes() {
        try {
            runTest("I said, 'Hello, World",
                    "I", "said,", "Hello, World");
            fail("We should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            // We should have thrown this
        }
    }
    
    @Test
    public void testQuotedCommandLineWithEmptyArg() {
        runTest("test \"testing one two three\" foo \"\" ' ' arg1",
                "test", "testing one two three", "foo", "", " ", "arg1");
    }
    
    @Test
    public void testExtraWhitespace() {
        runTest("test     foo  \t blah\n",
                "test", "foo", "blah");
    }
    
    private void runTest(String line, String... expected) {
        List<String> result = CommandLineParser.split(line);
        assertEquals(Arrays.asList(expected), result);
    }

}
