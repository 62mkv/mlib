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

package com.redprairie.moca.server.legacy;

import java.rmi.MarshalledObject;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaException.Args;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.server.db.BindList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * This class tests some of the functionalilty of the NativeReturnStruct class.  Notably, it tests the serialization
 * of that class.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_NativeReturnStruct {
    @BeforeClass
    public static void setUpResultsClass() {
        _savedResultsClass = NativeReturnStruct.getResultsClass();
        NativeReturnStruct.setResultsClass(SimpleResults.class);
    }
    
    @AfterClass
    public static void resetResultsClass() {
        NativeReturnStruct.setResultsClass(_savedResultsClass);
    }
    
    @Before
    public void setUp() {
        // By default, we create instances of SimpleResults
        _resultsClassToCreate = SimpleResults.class;
    }
    
    @Test 
    public void testSerialization() throws Exception {
        EditableResults res = createResults();
        res.addColumn("A", MocaType.STRING);
        res.addRow();
        res.setStringValue("A", "TEST STRING 123");

        BindList testList = new BindList();
        testList.add("XXX", MocaType.STRING, "Hello XXX");
        
        NativeReturnStruct ret = new NativeReturnStruct(404, res, "TEST MESSAGE", true, null, testList);
        
        NativeReturnStruct out = new MarshalledObject<NativeReturnStruct>(ret).get();
        
        MocaResults outRes = out.getResults();
        assertTrue(outRes.containsColumn("A"));
        assertEquals(MocaType.STRING, outRes.getColumnType("A"));
        assertTrue(outRes.next());
        assertEquals("TEST STRING 123", outRes.getString("a"));
        
        assertEquals(404, out.getErrorCode());
        assertEquals("TEST MESSAGE", out.getMessage());
        assertTrue(out.isMessageResolved());

        Args[] args = out.getArgs();
        assertNull(args);
        
        // No reference arguments, so no bind list needed
        BindList outList = out.getBindList();
        assertNull(outList);
    }

    @Test 
    public void testSerializationWithException() throws Exception {
        
        NativeReturnStruct ret = new NativeReturnStruct(new NativeException("FOO"));
        NativeReturnStruct out = new MarshalledObject<NativeReturnStruct>(ret).get();
        
        assertEquals(NativeException.CODE, out.getErrorCode());
        assertEquals(NativeException.MESSAGE, out.getMessage());
        Args[] args = out.getArgs();
        assertEquals(1, args.length);
        assertEquals("ARG", args[0].getName());
        assertEquals("FOO", args[0].getValue());
    }
    
    @Test 
    public void testSerializationWithBindReference() throws Exception {
        EditableResults res = createResults();
        BindList bind = new BindList();
        bind.add("XXX", MocaType.STRING_REF, "Hello XXX");
        
        NativeReturnStruct ret = new NativeReturnStruct(res, bind);
        NativeReturnStruct out = new MarshalledObject<NativeReturnStruct>(ret).get();
        
        // After serialization, the bind list will be its own instance.
        bind.setValue("XXX", "Goodbye XXX");
        
        // No reference arguments, so no bind list needed
        BindList outList = out.getBindList();
        assertEquals("Hello XXX", outList.getValue("XXX"));
    }
    
    @Test 
    public void testSerializationWithBigStrings() throws Exception {
        // Test a bunch of serialization sizes.
        testSerializationWithStringData(0);
        testSerializationWithStringData(1);
        testSerializationWithStringData(100);
        testSerializationWithStringData(1000);
        testSerializationWithStringData(10000);
        testSerializationWithStringData(30000);
        testSerializationWithStringData(3291924);
    }
    
    @Test
    public void testSimpleToWrappedSerialization() throws Exception {
        NativeReturnStruct.setResultsClass(WrappedResults.class);
        try {
            testSerialization();
            testSerializationWithBigStrings();
            testSerializationWithBindReference();
            testSerializationWithException();
        }
        finally {
            NativeReturnStruct.setResultsClass(SimpleResults.class);
        }
    }
    
    @Test
    public void testWrappedToSimpleSerialization() throws Exception {
        _resultsClassToCreate = WrappedResults.class;
        try {
            testSerialization();
            testSerializationWithBigStrings();
            testSerializationWithBindReference();
            testSerializationWithException();
        }
        finally {
            _resultsClassToCreate = SimpleResults.class;
        }
    }
    
    //
    // Implementation
    //
    private void testSerializationWithStringData(int size) throws Exception {
        String bigString = getRandomString(size);
        
        EditableResults res = createResults();
        res.addColumn("foo", MocaType.STRING);
        res.addRow();
        res.setStringValue("foo", bigString);
        NativeReturnStruct ret = new NativeReturnStruct(res);
        NativeReturnStruct out = new MarshalledObject<NativeReturnStruct>(ret).get();
        
        MocaResults outRes = out.getResults();
        assertTrue("String Size = " + size, outRes.next());
        String result = outRes.getString("foo");
        assertEquals("String Size = " + size, bigString, result);
    }
    
    private String getRandomString(int size) {
        int length = STRING_CHARSET.length;
        StringBuilder buf = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            buf.append(STRING_CHARSET[_random.nextInt(length)]); // Avoid surrogate pairs
        }
        
        return buf.toString();
    }
    
    public static class NativeException extends MocaException {
        private static final long serialVersionUID = 1L;
        
        public static final int CODE = 445;
        public static final String MESSAGE = "TEST MESSAGE ^ARG^";
        public NativeException(String arg) {
            super(CODE, MESSAGE);
            addArg("ARG", arg);
        }
    }
    
    private EditableResults createResults() throws Exception {
        return _resultsClassToCreate.newInstance();
    }
    
    private static Class<? extends EditableResults> _savedResultsClass;
    private static final char[] STRING_CHARSET;

    static {
        StringBuilder buf = new StringBuilder();
        // Make sure we don't put a null in the string.
        for (int i = 1; i < 0xD700; i++) {
            char c = (char)i;
            // Certain characters are not defined or only appear in surrogate pairs.  Let's avoid those for this test.
            if (Character.isDefined(c) && !Character.isLowSurrogate(c) && !Character.isHighSurrogate(c)) {
                buf.append((char)i);
            }
        }
        STRING_CHARSET = buf.toString().toCharArray();
    }
    
    private Random _random = new Random();
    private Class<? extends EditableResults> _resultsClassToCreate;
}
