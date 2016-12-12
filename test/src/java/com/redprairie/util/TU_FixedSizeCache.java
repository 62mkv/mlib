/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2006
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

package com.redprairie.util;

import java.util.Map;

import junit.framework.TestCase;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2006 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_FixedSizeCache extends TestCase {
    public void testCacheFull() {
        Map<String, String> cache = new FixedSizeCache<String, String>(5);
        cache.put("1", "value 1");
        cache.put("2", "value 2");
        cache.put("3", "value 3");
        cache.put("4", "value 4");
        cache.put("5", "value 5");
        
        assertEquals("value 3", cache.get("3"));
        assertEquals("value 2", cache.get("2"));
        assertEquals("value 4", cache.get("4"));
        assertEquals("value 1", cache.get("1"));
        assertEquals("value 5", cache.get("5"));
        
        cache.put("6", "value 6");
        
        assertEquals("value 1", cache.get("1"));
        assertEquals("value 2", cache.get("2"));
        assertNull(cache.get("3"));
        assertEquals("value 4", cache.get("4"));
        assertEquals("value 5", cache.get("5"));
        assertEquals("value 6", cache.get("6"));
    }
}
