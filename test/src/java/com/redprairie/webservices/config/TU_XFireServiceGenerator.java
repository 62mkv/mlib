/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.redprairie.webservices.config;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;
import junit.framework.TestCase;



/**
 *  Unit Test for Generating java Source files and descriptor file
 * 
 * <b>
 * 
 * <pre>
 *        Copyright (c) 2016 Sam Corporation
 *        All rights reserved.
 * </pre>
 * 
 * </b>
 * 
 * @author Mohanesha.C
 * @version $Revision$
 */
public class TU_XFireServiceGenerator  extends TestCase{

    /**
     * Test the  output source and config file using ServiceConfiguration
     * object
     * 
     *
     * @throws ServiceConfigException
     * @throws RPLogicalException
     */  
    public void testGenerate() throws ServiceConfigException, IOException {
        
        ServiceConfiguration config[] = new ServiceConfiguration[2];
        XMLServiceConfigurationReader reader = 
            new XMLServiceConfigurationReader(TU_XFireServiceGenerator.class.getResourceAsStream("test/generator.svc.xml"));
        config[0] = reader.process();
        assertNotNull(config[0]);
        
        reader = new XMLServiceConfigurationReader(TU_XFireServiceGenerator.class.getResourceAsStream("test/generator2.svc.xml"));
        config[1] = reader.process();
        assertNotNull(config[1]);
        
        // Make a temporary directory, by using Java to create a temporary file,
        // deleting it, then making a directory with the same name.
        File tmpDir = File.createTempFile("wstest", "dir");
        tmpDir.delete();
        tmpDir.mkdir();

        try {
            File sourcePath = new File(tmpDir, "source");
            sourcePath.mkdir();
            File configPath = new File(tmpDir, "config");
            configPath.mkdir();
            
            // OK, at this point, we have a source and config path.
            XFireServiceGenerator generator =
                    new XFireServiceGenerator(sourcePath,configPath);
            generator.generate(config);
            for (ServiceConfiguration c : config) {
                Service services[] = c.getServices();        
                for(Service s : services) {
                   if(s != null) {
                       Operation operation[] = s.getOperations();
                       
                       Assert.assertEquals(
                               "There should be 2 operations that we added", 
                               2, operation.length);
                       
                       String className = s.getClassName();
                       if (className == null) {
                           className = XFireServiceGenerator.defaultClassName(s.getName());
                       }
                       
                       String pkgName = s.getPackageName();
                       if (pkgName == null) {
                           pkgName = XFireServiceGenerator.defaultPackage();
                       }

                       String javaFilename = className + ".java";
                       String descFilename = "services.xml";
                       String pkgPath =  pkgName.replace(".", File.separator);               
                       assertTrue(new File(sourcePath.getPath() + File.separator + pkgPath + File.separator + javaFilename).exists());
                       assertTrue(new File(configPath.getPath() + File.separator + descFilename).exists());              
                   }  
                }
            }
        }
        finally {
            // TODO tmpDir.delete();
        }
    }
    
    // -----------------------------
    // implementation:
    // -----------------------------

}
