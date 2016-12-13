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

package com.redprairie.moca.server.expression.function;

import org.junit.Test;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.server.expression.function.LRPadFunction.PadType;

/**
 * Unit Tests to test the inline LPad functionality.
 * 
 * Copyright (c) 2011 Sam Corporation All Rights Reserved
 * 
 * @author klehrke
 */
public class TU_LPadFunction extends AbstractFunctionTest {

    @Test
    public void testSimpleLPadFunction() throws MocaException {

        final String expected = "0000000001";
        final String passed = "1";
        final int length = 10;
        final String padStr = "0";

        runTestWithObjectArgs(expected, passed, length, padStr);
    }

    @Test
    public void testSimpleLPadFunctionExtraArgument() throws MocaException {

        final String passed = "1";
        final int length = 10;
        final String padStr = "0";
        final String randomExtraArg = "blah";

        // Extra arguments shouldn't affect the results.
        runTestWithObjectArgsExpectingError(ERR_CODE, passed, length, padStr,
            randomExtraArg);
    }

    @Test
    public void testLPadTrim() throws MocaException {

        final String expected = "1234";
        final String passed = "1234abcd789";
        final int length = 4;

        runTestWithObjectArgs(expected, passed, length);
    }

    @Test
    public void testLPadWrongLength() throws MocaException {

        final String passed = "00000000001";
        final String expected = "";
        // Wrong length
        final int length = -1;

        runTestWithObjectArgs(expected, passed, length);
    }

    @Test
    public void testLPadMultiCharPad() throws MocaException {

        final String passed = "1";
        final int length = 10;
        final String expected = "ababababa1";
        final String padStr = "ab";

        runTestWithObjectArgs(expected, passed, length, padStr);
    }

    @Test
    public void testLPadNullStr() throws MocaException {

        final int length = 10;
        // Pad string shouldn't be more than 1 chars.
        final String padStr = "0";

        // passing null as the original string, to test the case when null is
        // passed.
        runTestWithObjectArgs(null, null, length, padStr);
    }

    @Test
    public void testLPadNullPadStr() throws MocaException {

        final String passed = "01";
        final int length = 10;

        // Passing null to test when the pad string is null.
        runTestWithObjectArgsExpectingError(ERR_CODE, passed, length, null);
    }

    @Test
    public void testLPadSameLength() throws MocaException {

        final String expected = "1010";
        final String passed = "1010";
        final int length = 4;
        // Pad string shouldn't be more than 1 chars.
        final String padStr = "0";

        runTestWithObjectArgs(expected, passed, length, padStr);
    }

    @Test
    public void testLPadOneArgument() throws MocaException {

        final String passed = "1010";

        runTestWithObjectArgsExpectingError(ERR_CODE, passed);
    }

    @Test
    public void testLPadZeroLength() throws MocaException {

        final String expected = "";
        final String passed = "1010";
        final int length = 0;

        // We expect a null in this case.
        runTestWithObjectArgs(expected, passed, length);
    }

    @Test
    public void testLPadDefaultSpacePadding() throws MocaException {

        final String expected = "    1";
        final String passed = "1";
        final int length = 5;

        runTestWithObjectArgs(expected, passed, length);
    }

    @Override
    protected BaseFunction getFunction() {
        return new LRPadFunction(PadType.LEFT);
    }

    private final static int ERR_CODE = FunctionArgumentException.CODE;
}
