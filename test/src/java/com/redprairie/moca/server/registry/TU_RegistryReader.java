/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.redprairie.moca.server.registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import com.redprairie.moca.server.SystemConfigurationException;

/**
 * Test cases for RegistryReader
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_RegistryReader extends TestCase {
    public void testEmptyRegistry() throws Exception {
        RegistryReader reg = readFromResource("test/blank.registry");
        assertNull(reg.getConfigurationElement("conmgr.port"));
        assertEquals("4500", reg.getConfigurationElement("conmgr.port", "4500"));
    }
    
    public void testFullRegistry() throws Exception {
        RegistryReader reg = readFromResource("test/full.registry");
        assertEquals("20002", reg.getConfigurationElement("conmgr.port"));
        assertEquals("ZWIMH EJ5E1 R2MN4 DKQPU RH58S G7RG6", reg.getConfigurationElement("license.key"));
        assertEquals("wmdqa", reg.getConfigurationElement("server.dbuser"));
        assertEquals("wmdqa", reg.getConfigurationElement("server.dbpass"));
    }
    
    public void testEnvironmentVariable() throws Exception {
        // get PATH since should always exist
        String pathVar = System.getenv("PATH");
        RegistryReader reg = readFromResource("test/env.registry");

        assertEquals("value", reg.getVariable("FOO"));
        assertEquals("BAR is [value]", reg.getVariable("BAR"));
        assertEquals("baz is not (BAR is [value])", reg.getVariable("BAZ"));
        // tests that getVariable gets the value for PATH from the system
        assertEquals(pathVar, reg.getVariable("path"));
        // tests that PATH gets expanded 
        assertEquals("PATH:" + pathVar, reg.getVariable("MIXED"));
        assertEquals("ZZZ -- ${ZZZ}", reg.getVariable("BAD"));
    }
   
    public void testEnvironmentVariableWithRecursion() throws Exception {
        RegistryReader reg = readFromResource("test/env2.registry");
        assertEquals("value xyz + $XYZ", reg.getVariable("xyz"));
    }
    
    public void testJavaExpansion() throws Exception {
        RegistryReader reg = readFromResource("test/java.registry");
        assertEquals("/path/to/java value", reg.getConfigurationElement("test.command-a"));
        assertEquals("/path/to/32bit/java value", reg.getConfigurationElement("test.command-b"));
    }
   
    public void testJavaExpansionDefault() throws Exception {
        RegistryReader reg = readFromResource("test/nojava.registry");
        assertEquals("java value", reg.getConfigurationElement("test.command-a"));
        assertEquals("java value", reg.getConfigurationElement("test.command-b"));
    }
    
    //We have to test some includes in our registry.
    public void testVariableWithInclude() throws Exception {
        String resource = "test/registryWithInclude";
        RegistryReader reg = readFromResource(resource);

        //Overridden by the registry that does imports
        assertEquals("true", reg.getConfigurationElement("server.compression"));
        
        assertEquals("javaPath", reg.getConfigurationElement("java.vm"));
        assertEquals("3600", reg.getConfigurationElement("security.session-key-timeout"));
        
        //Overridden by each registry where the post import is last.
        assertEquals("4600", reg.getConfigurationElement("server.port"));
    }
   
    private RegistryReader readFromResource(String resource) throws IOException, SystemConfigurationException {
        InputStream in = TU_RegistryReader.class.getResourceAsStream(resource);
        try {
            RegistryReader reader = new RegistryReader(new InputStreamReader(in, "UTF-8"));
            return reader;
        }
        finally {
            if (in != null) in.close();
        }
    }

}
