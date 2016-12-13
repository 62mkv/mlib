/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.components.base;

import org.junit.Test;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * Unit Test for Java Information specifically the list java properties and
 * list java information command.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
public class TU_JavaInformation extends AbstractMocaTestCase{
    
    /***
     * This test is to ensure that we can return java properties
     * using the list java properties component.
     * 
     * @throws MocaException
     */
    @Test
    public void testListJavaProperties() throws MocaException{
        MocaResults res = _moca.executeCommand("list java properties");
        assertTrue("Should have more than one property", res.getRowCount() > 0);
    }
    
    /***
     * This test is to ensure that we can return a specific java property
     * using the list java properties component with a property parameter.
     * 
     * @throws MocaException
     */
    @Test
    public void testGetSpecificJavaProperty() throws MocaException{
        MocaResults res = _moca.executeCommand("list java properties where property='os.arch'");
        assertTrue("Should have only one property", res.getRowCount() == 1);
    }
    
    /***
     * This test is to ensure that list java information returns more than one
     * property.
     * 
     * @throws MocaException
     */
    @Test
    public void testListJavaInformation() throws MocaException{
        MocaResults res = _moca.executeCommand("list java information");
        assertTrue("Should have more than one property", res.getRowCount() > 0);
    }
}
