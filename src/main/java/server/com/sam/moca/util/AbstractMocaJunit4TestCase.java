/*
 *  $URL$
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

package com.sam.moca.util;

import org.junit.After;
import org.junit.Before;

/**
 * This is the same as {@link AbstractMocaTestCase} except it uses
 * the JUnit 4 engine so JUnit 4 annotations can be used e.g.
 * <b>@Test</b> on all test methods or testing for expected exceptions
 * with <b>@Test(expected=&ltexception_class&gt)</b><br><br>
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 */
public class AbstractMocaJunit4TestCase extends AbstractBaseMocaTestCase {
    
    @Override
    @Before
    public final void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    @After
    public final void tearDown() throws Exception {
        super.tearDown();
    }

}
