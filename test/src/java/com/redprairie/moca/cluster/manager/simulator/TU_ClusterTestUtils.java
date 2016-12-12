/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2015
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

package com.redprairie.moca.cluster.manager.simulator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for cluster test utils
 * 
 * Copyright (c) 2015 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author mdobrinin
 */
public class TU_ClusterTestUtils {

    @Test
    public void testSetComparisons() {
        final ClusterManager mm = Mockito.mock(ClusterManager.class);
        final Set<Object> standard = new HashSet<Object>();
        final Set<Object> variant = new HashSet<Object>();
        final ClusterTestUtils util = new ClusterTestUtils(System.out, mm);
        util.assertSetContentsEqual("TEST", standard, variant);
        
        // sets are the same
        standard.add("^A");
        variant.add("^A");
        util.assertSetContentsEqual("TEST", standard, variant);
        
        // now variant set has an extra
        variant.add("^B");
        AssertionError e = null;
        try {
            util.assertSetContentsEqual("TEST", standard, variant);
        }
        catch (AssertionError expected) {
            e = expected;
        }
        assertNotNull("Set contents should be different", e);
        assertTrue(e.getMessage().contains("INCLUDED EXTRA"));
        assertTrue(e.getMessage().contains("^B"));
        assertFalse(e.getMessage().contains("SHOULD HAVE"));
    }

    @Test
    public void testMapComparisons() {
        final ClusterManager mm = Mockito.mock(ClusterManager.class);
        final Map<Object, Object> constant = new HashMap<Object, Object>();
        final Map<Object, Object> variant = new HashMap<Object, Object>();
        final ClusterTestUtils util = new ClusterTestUtils(System.out, mm);
        AssertionError e = null;
        util.assertMapContentsEqual("TEST", constant, variant);
        
        // this should match
        constant.put("^A", 1);
        variant.put("^A", 1);
        util.assertMapContentsEqual("TEST", constant, variant);
        
        // the variant map has a wrong mapping
        constant.clear();
        variant.clear();
        constant.put("^A", 1);
        variant.put("^A", 2);
        e = null;
        try {
            util.assertMapContentsEqual("TEST", constant, variant);
        }
        catch (AssertionError expected) {
            e = expected;
        }
        assertNotNull("Map contents should be different", e);
        assertTrue(e.getMessage().contains("HAD INCORRECT VALUE"));
        assertTrue(e.getMessage().contains("KEY {^A}"));
        assertFalse(e.getMessage().contains("INCLUDED EXTRA"));
        assertFalse(e.getMessage().contains("SHOULD HAVE"));
        
        // the variant mapping is missing a key and includes a wrong one at the same time
        constant.clear();
        variant.clear();
        constant.put("^A", 1);
        variant.put("^B", 3);
        e = null;
        try {
            util.assertMapContentsEqual("TEST", constant, variant);
        }
        catch (AssertionError expected) {
            e = expected;
        }
        assertNotNull("Map contents should be different", e);
        assertTrue(e.getMessage().contains("INCLUDED EXTRA: {[^B|3]}"));
        assertTrue(e.getMessage().contains("SHOULD HAVE INCLUDED: {[^A|1]}"));
        assertFalse(e.getMessage().contains("HAD INCORRECT VALUE"));
    }
}
