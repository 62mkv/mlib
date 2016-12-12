/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.server.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

import com.redprairie.moca.server.SystemConfigurationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * Tests the behavior of the MOCA rptab file parser.
 * 
 * <b><pre>
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author mlange
 * @version $Revision$
 */
public class TU_RptabReader {
    
    @BeforeClass
    public static void setup() throws SystemConfigurationException, InterruptedException {
        URL url = TU_RptabReader.class.getResource("resources");
        _cwd = url.getPath();     
    }

    /*
     * Test that a SystemConfigurationException is raised when no rptab file exists.
     */ 
    
    @Test
    public void testNoRptabFile() {
        
        String envname = "foo";     
        String rptabPathname = _cwd + File.separator + "rptab";
        
        // Make sure we removed the registry file
        removeFile(rptabPathname);
        
        try {
            new RptabReader(envname, rptabPathname);
            fail("Expected SystemConfigurationException");
        }
        catch (SystemConfigurationException e) {
            // Expected
        }  
    }  

    
    /*
     * Test that a SystemConfigurationException is raised when a
     * registry file doesn't exist.
     */
    
    @Test
    public void testNoRegistryFile() throws Exception {
        
        String envname = "good";
        String lesdir = _cwd;
        String registry = "registry";
        
        String rptabPathname = _cwd + File.separator + "rptab";
        String registryPathname = _cwd + File.separator + "data" + File.separator + registry;
        
        // Create an rptab file that we'll use for this test.
        createRptabFile(rptabPathname, envname, lesdir, null);
        
        // Make sure we removed the registry file
        removeFile(registryPathname);

        try {
            new RptabReader(envname, rptabPathname);
            fail("Expected SystemConfigurationException");
        }
        catch (SystemConfigurationException e) {
            // Expected
        }  
        finally {
            removeFile(rptabPathname);
        }
    }  
    
    /*
     * Test that it uses the default LESDIR/data/registry file if a registry
     * file is not defined in the rptab file.
     */
    
    @Test
    public void testDefaultRegistry() throws Exception {
        
        String envname = "good";
        String lesdir = _cwd;
        String registry = "registry";
        
        String rptabPathname = _cwd + File.separator + "rptab";
        String registryPathname = _cwd + File.separator + "data" + File.separator + registry;
        
        // Create an rptab and registry file that we'll use for this test.
        // good;<cwd>
        createRptabFile(rptabPathname, envname, lesdir, null);
        createFile(registryPathname);
        
        try {
            RptabReader reader = new RptabReader(envname, rptabPathname);
            assertEquals(envname, reader.getEnvironmentName());  
            assertEquals(lesdir, reader.getLesdir()); 
            assertEquals(registryPathname, reader.getRegistryPathname());
        }
        catch (SystemConfigurationException e) {
            fail(e.getMessage());
        }  
        finally {
            removeFile(rptabPathname);
            removeFile(registryPathname);
        }
    }  

    /*
     * Test that it uses the registry file pathname as it is defined
     * in the rptab file.
     */
    
    @Test
    public void testDefinedRegistry() throws Exception {
        
        String envname = "good";
        String lesdir = _cwd;
        String registry = "registry.foo";
        
        String rptabPathname = _cwd + File.separator + "rptab";
        String registryPathname = _cwd + File.separator + "data" + File.separator + registry;
        
        // Create an rptab and registry file that we'll use for this test.
        // good;<cwd>;<cwd>\data\registry.foo
        createRptabFile(rptabPathname, envname, lesdir, registryPathname);
        createFile(registryPathname);
        
        try {
            RptabReader reader = new RptabReader(envname, rptabPathname);
            assertEquals(envname, reader.getEnvironmentName());  
            assertEquals(lesdir, reader.getLesdir()); 
            assertEquals(registryPathname, reader.getRegistryPathname());
        }
        catch (SystemConfigurationException e) {
            fail(e.getMessage());
        }  
        finally {
            removeFile(rptabPathname);
            removeFile(registryPathname);
        }
    }  
    
    private void createRptabFile(String pathname, String envname, String lesdir, String regfile) throws IOException {
        
        String field1 = (envname == null) ? "" : envname;
        String field2 = (envname == null) ? "" : lesdir;
        String field3 = (regfile == null) ? "" : regfile;
        
        // Create the parent directory if necessary.
        File file = new File(pathname);
        File dir = new File(file.getParent());
        if (!dir.exists())
            dir.mkdir();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(pathname), "UTF-8"));
        //PrintWriter out = new PrintWriter(new FileWriter(pathname));
        out.println(field1 + File.pathSeparator + field2 + File.pathSeparator + field3);
        out.close(); 
    }

    private void createFile(String pathname) throws IOException {     
        
        // Create the parent directory if necessary.
        File file = new File(pathname);
        File dir = new File(file.getParent());
        if (!dir.exists())
            dir.mkdir();
        
        // Create the actual file.
        file.createNewFile();
    }
    
    private void removeFile(String pathname) {     
        File file = new File(pathname);
        file.delete();
    }
    
    private static String _cwd = null;
}
