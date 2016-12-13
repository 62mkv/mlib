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

package com.redprairie.util.test;


/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TestInterfaceContainer {
    public static interface TestInterface {
    }

    public static interface TestInterface2 {
    }
    
    public static class TestImpl1 implements TestInterface {
    }
    
    public static class TestImpl2 extends TestImpl1 {
    }
    
    public static class TestImpl3 extends TestImpl2 implements TestInterface2 {
    }
    
    private static class TestPrivateClass {
        private static interface TestInterface3 { }
    }
    
    public static class TestImpl4 extends TestImpl2 implements TestPrivateClass.TestInterface3 { }
}
