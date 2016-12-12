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

package com.redprairie.moca;

import java.rmi.MarshalledObject;

import junit.framework.TestCase;

/**
 * Unit test for <code>WrappedResults</code>
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All rights reserved.
 * </pre>
 * 
 * </b>
 * 
 * @author Derek Inksetter
 * @version $Revision$
 */
public class TU_MocaException extends TestCase {

    public void testMocaExceptionNoMessage() {
        MocaException m = new MocaException(400);
        assertEquals("Error 400", m.getMessage());
        String stringValue = m.toString();
        assertEquals(MocaException.class.getName() + ": Error 400", stringValue);
    }

    public void testMocaExceptionMissingArguments() {
        MocaException m = new MocaException(400, "test ^foo^ ^bar^");
        assertEquals("test ^foo^ ^bar^", m.getMessage());
        String stringValue = m.toString();
        assertEquals(MocaException.class.getName() + ": test ^foo^ ^bar^", stringValue);
    }

    public void testMocaExceptionWithAllArguments() {
        MocaException m = new MocaException(400, "test ^foo^ ^bar^");
        m.addArg("foo", "xxx");
        m.addArg("bar", "yyy");
        assertEquals("test ^foo^ ^bar^", m.getMessage());
        String stringValue = m.toString();
        assertEquals(MocaException.class.getName() + ": test xxx yyy", stringValue);
    }

    public void testRuntimeExceptionMissingArguments() {
        MocaRuntimeException m = new MocaRuntimeException(400, "test ^foo^ ^bar^");
        assertEquals("test ^foo^ ^bar^", m.getMessage());
        String stringValue = m.toString();
        assertEquals(MocaRuntimeException.class.getName() + ": test ^foo^ ^bar^", stringValue);
    }

    public void testRuntimeExceptionWithAllArguments() {
        MocaRuntimeException m = new MocaRuntimeException(400, "test ^foo^ ^bar^");
        m.addArg("foo", "xxx");
        m.addArg("bar", "yyy");
        assertEquals("test ^foo^ ^bar^", m.getMessage());
        String stringValue = m.toString();
        assertEquals(MocaRuntimeException.class.getName() + ": test xxx yyy", stringValue);
    }

    public void testRuntimeExceptionFromCheckedException() {
        MocaRuntimeException m = new MocaRuntimeException(new MocaException(400));
        assertEquals("Error 400", m.getMessage());
        String stringValue = m.toString();
        assertEquals(MocaRuntimeException.class.getName() + ": Error 400", stringValue);
    }

    public void testRuntimeExceptionFromCheckedExceptionWithArgs() {
        MocaException checked = new MocaException(400, "test ^foo^ ^bar^");
        checked.addArg("foo", "xxx");
        checked.addArg("bar", "yyy");
        MocaRuntimeException m = new MocaRuntimeException(checked);
        assertEquals("test ^foo^ ^bar^", m.getMessage());
        String stringValue = m.toString();
        assertEquals(MocaRuntimeException.class.getName() + ": test xxx yyy", stringValue);
    }

    public void testSerialization() throws Exception {
        MocaException e = new TestException(9032, "xxxValue");
        MocaException serialized = new MarshalledObject<MocaException>(e).get();
        assertEquals(9032, serialized.getErrorCode());
        assertEquals("Test message (^xxx^)", serialized.getMessage());
        assertEquals("xxxValue", serialized.getArgValue("xxx"));
        assertNull(serialized.getResults());
    }
}

class TestException extends MocaException {
    private static final long serialVersionUID = 1L;

    TestException(int code, String arg) {
        super(code, "Test message (^xxx^)");
        addArg("xxx", arg);
    }
}
