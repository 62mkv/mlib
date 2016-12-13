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

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

/**
 * Test the XMLServiceConfigurationReader object with Service Config xml files
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
public class TU_XMLServiceConfigurationReader extends TestCase {

    /**
     * Parses the normal Service Config Xml with out any errors
     * 
     */
    public void testProcess() throws Exception {
        ServiceConfiguration config = _readConfig("test/service_config.xml");
        
        assertNotNull(config);
        
        Service[] services = config.getServices();
        assertEquals(2, services.length);
        
        assertEquals("myservice", services[0].getName());
        assertEquals("MyService", services[0].getClassName());
        assertEquals("com.redprairie.services.core", services[0].getPackageName());
        Operation[] oper = services[0].getOperations();
        assertEquals(2, oper.length);
        
        // Operation #1
        assertEquals("operation1", oper[0].getName());

        OperationArgument[] args = oper[0].getOperationArguments();
        assertEquals(3, args.length);
        assertEquals("argName", args[0].getName());
        assertEquals("string", args[0].getType());
        assertEquals(false, args[0].isNullable());
        assertEquals("something", args[1].getName());
        assertEquals("integer", args[1].getType());
        assertEquals(false, args[1].isNullable());
        assertEquals("somethingElse", args[2].getName());
        assertEquals("double", args[2].getType());
        assertEquals(false, args[2].isNullable());

        assertEquals(true, oper[0].isMultiRowResult());
        assertEquals("MyResult", oper[0].getResultClassName());

        ResultField[] fields = oper[0].getResultFields();
        assertEquals(4, fields.length);
        assertEquals("field1", fields[0].getName());
        assertEquals("string", fields[0].getType());
        assertEquals(true, fields[0].isNullable());
        assertEquals("field2", fields[1].getName());
        assertEquals("integer", fields[1].getType());
        assertEquals(true, fields[1].isNullable());
        assertEquals("field3", fields[2].getName());
        assertEquals("boolean", fields[2].getType());
        assertEquals(false, fields[2].isNullable());
        assertEquals("field4", fields[3].getName());
        assertEquals("double", fields[3].getType());
        assertEquals(true, fields[3].isNullable());
        
        assertEquals("some MOCA command | some other moca command where x=@something", oper[0].getCommand());
        
        // Operation #2
        
        assertEquals("operation2", oper[1].getName());
        
        args = oper[1].getOperationArguments();
        assertEquals(3, args.length);
        assertEquals("argName2", args[0].getName());
        assertEquals("string", args[0].getType());
        assertEquals(false, args[0].isNullable());
        assertEquals("something2", args[1].getName());
        assertEquals("integer", args[1].getType());
        assertEquals(false, args[1].isNullable());
        assertEquals("somethingElse2", args[2].getName());
        assertEquals("double", args[2].getType());
        assertEquals(false, args[2].isNullable());

        
        assertEquals(true, oper[1].isMultiRowResult());
        assertEquals("MyResult2", oper[1].getResultClassName());

        fields = oper[1].getResultFields();
        assertEquals(4, fields.length);
        assertEquals("field12", fields[0].getName());
        assertEquals("string", fields[0].getType());
        assertEquals(true, fields[0].isNullable());
        assertEquals("field22", fields[1].getName());
        assertEquals("integer", fields[1].getType());
        assertEquals(true, fields[1].isNullable());
        assertEquals("field32", fields[2].getName());
        assertEquals("boolean", fields[2].getType());
        assertEquals(false, fields[2].isNullable());
        assertEquals("field42", fields[3].getName());
        assertEquals("double", fields[3].getType());
        assertEquals(true, fields[3].isNullable());
        
        assertEquals("some MOCA command2 | some other moca command where x=@something", oper[1].getCommand());
        
        assertEquals("myservice1", services[1].getName());
        assertEquals("MyService1", services[1].getClassName());
        assertEquals("com.redprairie.services.core", services[1].getPackageName());
        oper = services[1].getOperations();
        assertEquals("There should be 2 services", 2, oper.length);
    }

    /**
     * Parses the Service Config Xml Removing Service Attribute
     */
    public void testServiceAttribute() throws Exception {
        try {
            _readConfig("test/service_config2.xml");
            fail("expected configuration failure -- missing service name");
        }
        catch (ServiceConfigException e) {
            // Normal
        }
    }

    /**
     * Parses the Service Config Xml Removing Operation Attribute
     */
    public void testOperationAttribute() throws Exception {
        try {
            _readConfig("test/service_config3.xml");
            fail("expected configuration failure -- missing operation name");
        }
        catch (ServiceConfigException e) {
            // Normal
        }
    }

    /**
     * Parses the Service Config Xml Removing Operation Input Argument Attribute
     */
    public void testOperationInputArgumentAttribute()
            throws Exception {
        try {
            _readConfig("test/service_config4.xml");
            fail("expected configuration failure -- missing input argument name");
        }
        catch (ServiceConfigException e) {
            // Normal
        }
    }

    /**
     * Parses the Service Config Xml Removing Command value
     */
    public void testCommand() throws Exception {
        try {
            _readConfig("test/service_config5.xml");
            fail("expected configuration failure -- missing command value");
        }
        catch (ServiceConfigException e) {
            // Normal
        }
    }

    /**
     * Parses the Service Config Xml Removing Output Attribute
     */
    public void testOutputAttribute() throws Exception {
        ServiceConfiguration config = _readConfig("test/service_config6.xml");
        assertNotNull(config);
        // Allow missing class attribute on output result.
    }

    /**
     * Parses the Service Config Xml Removing Output Field Attribute
     */
    public void testOutputFieldAttribute() throws Exception {
        try {
            _readConfig("test/service_config7.xml");
            fail("expected configuration failure -- missing output field name");
        }
        catch (ServiceConfigException e) {
            // Normal
        }
    }

    /**
     * Parses the Service Config Xml Removing All Operations
     */
    public void testOperation() throws Exception {
        try {
            _readConfig("test/service_config8.xml");
            fail("expected configuration failure -- no operations defined");
        }
        catch (ServiceConfigException e) {
            // Normal
        }
    }

    /**
     * Parses the Service Config Xml Removing All Input arguments
     */
    public void testInput() throws Exception {
        ServiceConfiguration config = _readConfig("test/service_config9.xml");
        assertNotNull(config);
    }

    /**
     * Parses the Service Config Xml Removing Command in operation
     */
    public void testCommandElement() throws Exception {
        try {
            _readConfig("test/service_config10.xml");
            fail("expected configuration failure -- missing command");
        }
        catch (ServiceConfigException e) {
            // Normal
        }
    }

    /**
     * Parses the Service Config Xml Removing Output in operation 
     */
    public void testOutput() throws Exception {
        ServiceConfiguration config = _readConfig("test/service_config11.xml");
        assertNotNull(config);
    }

    /**
     * Parses the Service Config Xml Removing All argument elements in input
     */
    public void testInputArgument() throws Exception {
        ServiceConfiguration config = _readConfig("test/service_config12.xml");
        assertNotNull(config);
    }

    /**
     * Parses the Service Config Xml Removing All Fields elements in output
     */
    public void testOuputField() throws Exception {
        ServiceConfiguration config = _readConfig("test/service_config13.xml");
        assertNotNull(config);
    }

    /**
     * Parses the Service Config Xml Removing All Services
     */
    public void testServices() throws Exception {
        try {
            _readConfig("test/service_config14.xml");
            fail("expected configuration failure -- No services present");
        }
        catch (ServiceConfigException e) {
            // Normal
        }
    }
    
    /**
     * Parses the Service Config Xml Removing All Services
     */
    public void testMultipleServiceEntries() throws Exception {
            ServiceConfiguration config = _readConfig("test/service_config15.xml");
            
            assertNotNull(config);
            
            Service[] services = config.getServices();
            assertEquals(1, services.length);
            
            assertEquals("myservice", services[0].getName());
            assertEquals("MyService", services[0].getClassName());
            assertEquals("com.redprairie.services.core", services[0].getPackageName());
            Operation[] oper = services[0].getOperations();
            assertEquals(4, oper.length);
            
            // Operation #1
            assertEquals("operation1", oper[0].getName());

            OperationArgument[] args = oper[0].getOperationArguments();
            assertEquals(3, args.length);
            assertEquals("argName", args[0].getName());
            assertEquals("string", args[0].getType());
            assertEquals(false, args[0].isNullable());
            assertEquals("something", args[1].getName());
            assertEquals("integer", args[1].getType());
            assertEquals(false, args[1].isNullable());
            assertEquals("somethingElse", args[2].getName());
            assertEquals("double", args[2].getType());
            assertEquals(false, args[2].isNullable());

            assertEquals(true, oper[0].isMultiRowResult());
            assertEquals("MyResult", oper[0].getResultClassName());

            ResultField[] fields = oper[0].getResultFields();
            assertEquals(4, fields.length);
            assertEquals("field1", fields[0].getName());
            assertEquals("string", fields[0].getType());
            assertEquals(true, fields[0].isNullable());
            assertEquals("field2", fields[1].getName());
            assertEquals("integer", fields[1].getType());
            assertEquals(true, fields[1].isNullable());
            assertEquals("field3", fields[2].getName());
            assertEquals("boolean", fields[2].getType());
            assertEquals(false, fields[2].isNullable());
            assertEquals("field4", fields[3].getName());
            assertEquals("double", fields[3].getType());
            assertEquals(true, fields[3].isNullable());
            
            assertEquals("some MOCA command | some other moca command where x=@something", oper[0].getCommand());
            
            // Operation #2
            
            assertEquals("operation2", oper[1].getName());
            
            args = oper[1].getOperationArguments();
            assertEquals(3, args.length);
            assertEquals("argName2", args[0].getName());
            assertEquals("string", args[0].getType());
            assertEquals(false, args[0].isNullable());
            assertEquals("something2", args[1].getName());
            assertEquals("integer", args[1].getType());
            assertEquals(false, args[1].isNullable());
            assertEquals("somethingElse2", args[2].getName());
            assertEquals("double", args[2].getType());
            assertEquals(false, args[2].isNullable());

            
            assertEquals(true, oper[1].isMultiRowResult());
            assertEquals("MyResult2", oper[1].getResultClassName());

            fields = oper[1].getResultFields();
            assertEquals(4, fields.length);
            assertEquals("field12", fields[0].getName());
            assertEquals("string", fields[0].getType());
            assertEquals(true, fields[0].isNullable());
            assertEquals("field22", fields[1].getName());
            assertEquals("integer", fields[1].getType());
            assertEquals(true, fields[1].isNullable());
            assertEquals("field32", fields[2].getName());
            assertEquals("boolean", fields[2].getType());
            assertEquals(false, fields[2].isNullable());
            assertEquals("field42", fields[3].getName());
            assertEquals("double", fields[3].getType());
            assertEquals(true, fields[3].isNullable());
            
            assertEquals("some MOCA command2 | some other moca command where x=@something", oper[1].getCommand());

            // Operation #3
            assertEquals("operation3", oper[2].getName());

            // Operation #4
            
            assertEquals("operation4", oper[3].getName());
    }
    
    /**
     * Parses the Service Config Xml Removing All Services
     */
    public void testExternalServiceClass() throws Exception {
            ServiceConfiguration config = _readConfig("test/service_config16.xml");
            
            assertNotNull(config);
            
            Service[] services = config.getServices();
            assertEquals(1, services.length);
            
            assertEquals("myservice", services[0].getName());
            assertEquals("MyServiceCustom", services[0].getClassName());
            assertEquals("com.redprairie.services.core", services[0].getPackageName());
            assertFalse(services[0].isGenerate());
            Operation[] oper = services[0].getOperations();
            assertEquals(0, oper.length);
    }
    
    /**
     * Parses the Service Config Xml Removing All Services
     */
    public void testCommandServices() throws Exception {
        ServiceConfiguration config = _readConfig("test/command.svc.xml");
        assertNotNull(config);
    }
    
    //
    // Implementation
    //
    
    private ServiceConfiguration _readConfig(String resource)
            throws IOException, ServiceConfigException {
        InputStream config = TU_XMLServiceConfigurationReader.class.getResourceAsStream(resource);
        XMLServiceConfigurationReader xmlServReadObj =
                new XMLServiceConfigurationReader(config);
        return xmlServReadObj.process();
        
    }
}
