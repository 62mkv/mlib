/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the EnvironmentExpander class
 * 
 * Copyright (c) 2013 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TU_EnvironmentExpander {
    
    @Test
    public void testMultipleEnvironmentExpansion() {
        EnvironmentExpander expander = new EnvironmentExpander();
        Map<String, String> env = new HashMap<String, String>();
        String mocaDir = "c:/dev/trunk/moca";
        String mcsDir = "c:/dev/trunk/mcs";
        env.put("BASEDIR", "c:/dev/trunk");
        env.put("MOCADIR", "%BASEDIR%/moca");
        env.put("MCSDIR", "%BASEDIR%/mcs");
        env.put("CLASSPATH", "%MCSDIR%/lib" + File.pathSeparator + "%MOCADIR%/lib" + File.pathSeparator + "%CLASSPATH%");
        String expectedClasspath = mcsDir + "/lib" + File.pathSeparator + mocaDir + "/lib" + File.pathSeparator +
                (System.getenv("CLASSPATH") == null ? "%CLASSPATH%" : System.getenv("CLASSPATH"));
        expander.expand(env);
        
        assertEquals(mocaDir, env.get("MOCADIR"));
        assertEquals(mcsDir, env.get("MCSDIR"));
        assertEquals(expectedClasspath, env.get("CLASSPATH"));       
    }
    
    /**
     * Tests that the %CLASSPATH% in our CLASSPATH environment variable gets
     * resolved using the system level environment variable
     */
    @Test
    public void testSimpleRecursiveExpansion() {
        EnvironmentExpander expander = new EnvironmentExpander();
        Map<String, String> env = new HashMap<String, String>();
        env.put("CLASSPATH", "my/classpath" + File.pathSeparator + "%CLASSPATH%");
        expander.expand(env);
        String expected = "my/classpath" + File.pathSeparator + 
                (System.getenv("CLASSPATH") == null ? "%CLASSPATH%" : System.getenv("CLASSPATH"));
        assertEquals(expected, env.get("CLASSPATH"));
    }
    
    /**
     * Tests a expanded a recursive environment variable inside of another
     * environment variable
     */
    @Test
    public void testRecursiveReferenceFromOtherVariable() {
        EnvironmentExpander expander = new EnvironmentExpander();
        Map<String, String> env = new HashMap<String, String>();
        String myOtherClassPathVal = "my/other/classpath" + File.pathSeparator + "%CLASSPATH%";
        String myClassPathVal = "my/classpath" + File.pathSeparator + "%CLASSPATH%";
        env.put("MYOTHERCLASSPATH", myOtherClassPathVal);
        env.put("CLASSPATH", myClassPathVal);
        expander.expand(env);
        String expectedClasspathExpanded = "my/classpath" + File.pathSeparator + 
                (System.getenv("CLASSPATH") == null ? "%CLASSPATH%" : System.getenv("CLASSPATH"));
        String expectedOtherExpanded = "my/other/classpath" + File.pathSeparator + expectedClasspathExpanded;
        
        assertEquals(expectedClasspathExpanded, env.get("CLASSPATH"));
        assertEquals(expectedOtherExpanded, env.get("MYOTHERCLASSPATH"));
        
        // Test the order of evaluation doesn't matter either
        env = new HashMap<String, String>();
        env.put("CLASSPATH", myClassPathVal);
        env.put("MYOTHERCLASSPATH", myOtherClassPathVal);
        expander.expand(env);
        assertEquals(expectedClasspathExpanded, env.get("CLASSPATH"));
        assertEquals(expectedOtherExpanded, env.get("MYOTHERCLASSPATH"));
    }
}
