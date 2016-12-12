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

package com.redprairie.moca.server.dispatch;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.redprairie.moca.MocaException;

/**
 * Unit tests for ErrorMessageBuilder class.  Tests various error message translation scenarios. 
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_ErrorMessageBuilder extends TestCase {
    public void testNoTranslationNoArgs() {
        TestException ex = new TestException(500, "Test Message");
        ex.addArg("xxx", "abc");

        String result = new ErrorMessageBuilder(ex, new MapMessageResolver()).getMessage();
        
        assertEquals("Test Message", result);
    }

    public void testNoTranslationWithArgs() {
        TestException ex = new TestException(500, "Test Message ^xxx^/^yyy^");
        ex.addArg("xxx", "abc");
        ex.addArg("yyy", "xyz");

        String result = new ErrorMessageBuilder(ex, new MapMessageResolver()).getMessage();
        
        assertEquals("Test Message abc/xyz", result);
    }

    public void testNoTranslationWithWrongCaseArgs() {
        TestException ex = new TestException(500, "Test Message ^xXx^/^YYY^");
        ex.addArg("xxx", "abc");
        ex.addArg("yyy", "xyz");

        String result = new ErrorMessageBuilder(ex, new MapMessageResolver()).getMessage();
        
        assertEquals("Test Message abc/xyz", result);
    }

    public void testNoTranslationWithMissingArgs() {
        TestException ex = new TestException(500, "Test Message ^xxx^/^yyy^");
        ex.addArg("xxx", "abc");

        String result = new ErrorMessageBuilder(ex, new MapMessageResolver()).getMessage();
        
        assertEquals("Test Message abc/^yyy^", result);
    }

    public void testMalformedMessage() {
        TestException ex = new TestException(500, "Test Message ^xxx^/^yyy");
        ex.addArg("xxx", "abc");
        ex.addArg("yyy", "xyz");

        String result = new ErrorMessageBuilder(ex, new MapMessageResolver()).getMessage();
        
        assertEquals("Test Message abc/^yyy", result);
    }

    public void testLookupNoLookupArgs() {
        TestException ex = new TestException(500, "Test Message ^xxx^/^yyy");
        ex.addArg("xxx", "abc");
        ex.addArg("yyy", "xyz");
        
        MapMessageResolver resolver = new MapMessageResolver();
        resolver.add("err500", "translated/500: ^xxx^ ^yyy^");

        String result = new ErrorMessageBuilder(ex, resolver).getMessage();
        
        assertEquals("translated/500: abc xyz", result);
    }

    public void testLookupWithLookupArgs() {
        TestException ex = new TestException(500, "Test Message ^xxx^/^yyy");
        ex.addArg("xxx", "abc");
        ex.addLookupArg("yyy", "xyz");
        
        MapMessageResolver resolver = new MapMessageResolver();
        resolver.add("err500", "translated/500: ^xxx^ ^yyy^");
        resolver.add("xyz", "foobar");

        String result = new ErrorMessageBuilder(ex, resolver).getMessage();
        
        assertEquals("translated/500: abc foobar", result);
    }
    
    public void testTranslationOfDecimal() {
        TestException ex = new TestException(500, "Test Message ^errval^");
        ex.addArg("errval", 9.0);

        String result = new ErrorMessageBuilder(ex, new MapMessageResolver()).getMessage();
        
        assertEquals("Test Message 9", result);
    }
    
    public void testTranslationOfDecimalWithLotsOfPlaces() {
        TestException ex = new TestException(500, "Test Message ^errval^");
        ex.addArg("errval", 329.9939998999194);

        String result = new ErrorMessageBuilder(ex, new MapMessageResolver()).getMessage();
        
        assertEquals("Test Message 329.9939998999194", result);
    }

    static class TestException extends MocaException {
        private static final long serialVersionUID = 1L;
    
        TestException(int code, String message) {
            super(code, message);
        }

        public void addArg(String name, Object value) {
            super.addArg(name, value);
        }

        public void addLookupArg(String name, String value) {
            super.addLookupArg(name, value);
        }
    }

    static class MapMessageResolver implements MessageResolver {
        public String getMessage(String key) {
            return _map.get(key);
        }
        
        public void add(String key, String value) {
            _map.put(key, value);
        }
        
        private final Map<String, String> _map = new HashMap<String, String>();
    }
}