/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.moca.server.log.filter;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2013 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_PackageTrieNode {
    @Test
    public void testSimpleTrieSuccess() {
        PackageTrieNode node = new PackageTrieNode();
        node.addPackage("com.redprairie");
        
        assertTrue(node.isPackagePresent("com.redprairie"));
        assertTrue(node.isPackagePresent("com.redprairie.foo"));
    }
    
    @Test
    public void testSimpleTrieFailure() {
        PackageTrieNode node = new PackageTrieNode();
        node.addPackage("com.redprairie");
        
        assertFalse(node.isPackagePresent("com"));
        assertFalse(node.isPackagePresent("com.test"));
        assertFalse(node.isPackagePresent("com.foo"));
        assertFalse(node.isPackagePresent("org.junit"));
    }
    
    @Test
    public void testMultipleEntryTrieSuccess() {
        PackageTrieNode node = new PackageTrieNode();
        node.addPackage("com.redprairie");
        node.addPackage("org.junit");
        node.addPackage("org.jgroups");
        
        assertTrue(node.isPackagePresent("org.junit.Assert"));
        assertTrue(node.isPackagePresent("org.jgroups.something.blah.test.FooMan"));
    }
    
    /**
     * Packages are case sensitive so we should abide by that
     */
    @Test
    public void testCaseSensitivity() {
        PackageTrieNode node = new PackageTrieNode();
        node.addPackage("com.redprairie");
        
        assertFalse(node.isPackagePresent("coM.ReDPraiRIE"));
    }
}
