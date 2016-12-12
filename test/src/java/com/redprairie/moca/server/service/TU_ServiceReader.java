/*
 *  $URL$
 *  $Revision$
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

import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the behavior of the MOCA srevices.xml file reader.
 * 
 * <b><pre>
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author mlange
 * @version $Revision$
 */
public class TU_ServiceReader {
    
    @BeforeClass
    public static void setup() {
        URL url = TU_ServiceReader.class.getResource("resources");
        _cwd = url.getPath();
    }
    
    @Test
    public void testJavaService() throws Exception {     
        String[] proddirs = { _cwd };
        
        Service service = ServiceReader.find("java.dev", proddirs);
        assertNotNull(service);
        
        // General information
        assertEquals("java.dev", service.getName());
        assertEquals("My java display name", service.getDisplayName());
        assertEquals("My java description", service.getDescription());

        // Start information
        StartStopInfo startInfo = service.getStartInfo();   
        assertEquals(startInfo.getClass(), JavaStartStopInfo.class);
        assertEquals("com.redprairie.moca.server.service.Manager.main(start)", startInfo.toString());
        
        // Stop information
        StartStopInfo stopInfo = service.getStopInfo();   
        assertEquals(stopInfo.getClass(), JavaStartStopInfo.class);
        assertEquals("com.redprairie.moca.server.service.Manager.main(stop)", stopInfo.toString());
        
        // Depends on information
        String[] dependsOn = service.getDependsOn();
        assertEquals(1, dependsOn.length);
        assertEquals("mssqlserver", dependsOn[0]);
    }

    @Test
    public void testExeService() throws Exception {        
        String[] proddirs = { _cwd };
        
        Service service = ServiceReader.find("exe.dev", proddirs);
        assertNotNull(service);
        
        // General information
        assertEquals("exe.dev", service.getName());
        assertEquals("My exe display name", service.getDisplayName());
        assertEquals("My exe description", service.getDescription());

        // Start information
        StartStopInfo startInfo = service.getStartInfo();   
        assertEquals(startInfo.getClass(), ExeStartStopInfo.class);
        assertEquals("start.sh foo", startInfo.toString());
        
        // Stop information
        StartStopInfo stopInfo = service.getStopInfo();   
        assertEquals(stopInfo.getClass(), ExeStartStopInfo.class);
        assertEquals("stop.sh foo", stopInfo.toString());
        
        // Depends on information
        String[] dependsOn = service.getDependsOn();
        assertEquals(0, dependsOn.length);
    }
    
    private static String _cwd = null;
}
