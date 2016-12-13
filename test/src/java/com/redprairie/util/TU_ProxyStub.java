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

package com.redprairie.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;

/**
 * This class is to make sure that ProxyStub behaves desirably
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_ProxyStub {
    @Test
    public void testEqualsBasedOnProxiedObject() {
        Object obj = new Object();
        Object proxy1 = ProxyStub.newProxy(EmptyInterface.class, obj);
        Object proxy2 = ProxyStub.newProxy(EmptyInterface.class, obj);
        
        // Make sure it is true both ways
        assertEquals(proxy1, proxy2);
        assertEquals(proxy2, proxy1);
    }
    
    @Test
    public void testEqualsNotInstanceEqual() {
        Object obj = new Object();
        Object proxy1 = ProxyStub.newProxy(EmptyInterface.class, obj);
        Object proxy2 = ProxyStub.newProxy(EmptyInterface.class, obj);
        
        assertNotSame(proxy1, proxy2);
    }
    
    @Test
    public void testEqualWithoutProxyOnOne() {
        Object obj = new Object();
        Object proxy1 = ProxyStub.newProxy(EmptyInterface.class, obj);
        
        // Make sure we are not equal either way
        assertFalse(obj.equals(proxy1));
        assertFalse(proxy1.equals(obj));
    }
    
    // This interface is just here so this unit test has no other dependencies
    private static interface EmptyInterface {}
    
}
