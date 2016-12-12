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

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.redprairie.util.test.TestInterfaceContainer.TestImpl1;
import com.redprairie.util.test.TestInterfaceContainer.TestImpl2;
import com.redprairie.util.test.TestInterfaceContainer.TestImpl3;
import com.redprairie.util.test.TestInterfaceContainer.TestImpl4;
import com.redprairie.util.test.TestInterfaceContainer.TestInterface;
import com.redprairie.util.test.TestInterfaceContainer.TestInterface2;

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
public class TU_ClassUtils extends TestCase {

    public void testNoSuperclass() {
        Object result =  ClassUtils.instantiateClass(
                "java.lang.Object", null);
        assertNotNull(result);
    }

    public void testValidSuperclass() {
        Object result = ClassUtils.instantiateClass(
                "java.util.ArrayList", List.class); 
        assertNotNull(result);
        assertTrue(result instanceof List);
    }
    
    public void testNonDefaultConstructor() {
        String homeDir = System.getProperty("user.dir");
        Object result = ClassUtils.instantiateClass(
                "java.io.File", File.class,
                new Class[] {String.class},
                new Object[] {homeDir}, null); 
        assertNotNull(result);
        assertTrue(result instanceof File);
        assertEquals(homeDir, ((File)result).getPath());
    }

    public void testInvalidSuperclass() {
        try {
            ClassUtils.instantiateClass("java.util.ArrayList", Map.class);

            fail("expected IllegalArgumentException");
        }
        catch(IllegalArgumentException e) {
            // Normal
        }       
    }

    public void testClassNotFound() {
        try {
            ClassUtils.instantiateClass("java.lang.NotARealClass", null);

            fail("expected IllegalArgumentException");
        }
        catch(IllegalArgumentException e) {
            assertTrue(e.getCause() instanceof ClassNotFoundException);
        }
    }

    public void testAbstractClass() {
        try {
            ClassUtils.instantiateClass(AbstractTestClass.class.getName(), AbstractTestClass.class);

            fail("expected IllegalArgumentException");
        }
        catch(IllegalArgumentException e) {
            // Normal
        }       
    }

    public void testNoDefaultConstructor() {
        try {
            // File does not have a default constructor
            ClassUtils.instantiateClass("java.io.File", File.class);

            fail("expected IllegalArgumentException");
        }
        catch(IllegalArgumentException e) {
            assertTrue(e.getCause() instanceof NoSuchMethodException);
        }       
    }
    public void testNoSpecifiedConstructor() {
        try {
            // File does not have a (String, String, String) constructor
            ClassUtils.instantiateClass("java.io.File", File.class,
                    new Class[] {String.class, String.class, String.class},
                    new Object[] {"", "", ""}, null);

            fail("expected IllegalArgumentException");
        }
        catch(IllegalArgumentException e) {
            assertTrue(e.getCause() instanceof NoSuchMethodException);
        }       
    }
    
    public void testConstructorException() {
        try {
            // File does not have a (String, String, String) constructor
            ClassUtils.instantiateClass(TestClass.class.getName(), TestClass.class);

            fail("expected IllegalArgumentException");
        }
        catch(IllegalArgumentException e) {
            assertTrue(e.getCause() instanceof NullPointerException);
        }       
    }
    
    public void testNoInterfaces() {
        Class<?>[] interfaces = ClassUtils.getInterfaces(TestClass.class);
        assertEquals(0, interfaces.length);
    }
    
    public void testDirectInterface() {
        Class<?>[] interfaces = ClassUtils.getInterfaces(TestImpl1.class);
        assertEquals(1, interfaces.length);
        assertTrue(Arrays.asList(interfaces).contains(TestInterface.class));
    }
    
    public void testIndirectInterface() {
        Class<?>[] interfaces = ClassUtils.getInterfaces(TestImpl2.class);
        assertEquals(1, interfaces.length);
        assertTrue(Arrays.asList(interfaces).contains(TestInterface.class));
    }
    
    public void testIndirectAndDirectInterfaces() {
        Class<?>[] interfaces = ClassUtils.getInterfaces(TestImpl3.class);
        assertEquals(2, interfaces.length);
        assertTrue(Arrays.asList(interfaces).contains(TestInterface.class));
        assertTrue(Arrays.asList(interfaces).contains(TestInterface2.class));
    }
    
    public void testObscuredInterfaces() {
        Class<?>[] interfaces = ClassUtils.getInterfaces(TestImpl4.class);
        assertEquals(2, interfaces.length);
        assertTrue(Arrays.asList(interfaces).contains(TestInterface.class));
//        assertTrue(Arrays.asList(interfaces).contains(TestPrivateClass.TestInterface3.class));
    }
    
    public static class TestClass {
        public TestClass() {
            throw new NullPointerException();
        }
    }
    
    public abstract class AbstractTestClass {
        public AbstractTestClass() {
            
        }
    }

}
