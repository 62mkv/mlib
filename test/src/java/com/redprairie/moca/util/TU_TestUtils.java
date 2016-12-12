/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.redprairie.moca.util;

import java.util.Locale;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2016 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author j1014071
 */
public class TU_TestUtils {

    @Test
    public void getTestTimeoutValue_Default(){
        assertEquals(50, TestUtils.getTestTimeout(this.getClass(), "FAKE12345", 50));
    }

    @Test
    public void getTestTimeoutValue_Override(){
        TestUtils utils = Mockito.spy(new TestUtils());
        Mockito.when(utils._getEnv(Mockito.matches("TU_TESTUTILS_JOIN_TIMEOUT"))).thenReturn("150");
        assertEquals(150, utils._getTestTimeout(this.getClass(), "JOIN", 20));
        // Verify we mocked correctly
        assertEquals(20, utils._getTestTimeout(this.getClass(), "NOT_JOIN", 20));
    }
    
    @Test
    public void intEnvValue_Default(){
        assertEquals(57, TestUtils.getEnvInteger("NON_EXISTENT_VARIABLE_12345", 57));

        if(System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")) {
            // This is true on my pc, but might be not true on all boxes
            //assertEquals(4, TestUtils.getEnvInteger("NUMBER_OF_PROCESSORS", 1));
            assertTrue(TestUtils.getEnvInteger("NUMBER_OF_PROCESSORS", 0) > 0);
        }
    }

    @Test
    public void intEnvValue_Override(){
        TestUtils utils = Mockito.spy(new TestUtils());
        Mockito.when(utils._getEnv(Mockito.matches("MY_VARIABLE"))).thenReturn("250");
        assertEquals(250, utils._getEnvInteger("MY_VARIABLE", 20));
        // Verify we mocked correctly
        assertEquals(20, utils._getEnvInteger("NOT_MY_VARIABLE", 20));
    }
    
}
