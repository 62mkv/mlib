/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.server.expression.function;

import org.junit.Test;

import com.redprairie.moca.MocaException;

/**
 * This class tests to make sure that instr function will work correctly.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author gvaneyck
 * @version $Revision$
 */
public class TU_TrimFunction extends AbstractFunctionTest {

    @Test
    public void testSimpleValues() throws MocaException {
        runTest(null, null);
        runTest("", "");
        runTest("A", "A ");
    }
    
    @Test
    public void testValuesWithLeadingSpaces() throws MocaException {
        runTest("  X", "  X   ");
        runTest(" A B", " A B ");
    }
    
    @Test
    public void testValuesWithOnlySpaces() throws MocaException {
        runTest("", "       ");
    }
    
    @Test
    public void testValuesWithNonSpaceWhitespace() throws MocaException {
        runTest("This is a test", "This is a test\n   \r\n");
    }
    
    protected void runTest(String expected, String arg) throws MocaException {
        runTestWithObjectArgs(expected, arg);
    }
    
    @Override
    protected BaseFunction getFunction() {
        return new TrimFunction();
    }
}
