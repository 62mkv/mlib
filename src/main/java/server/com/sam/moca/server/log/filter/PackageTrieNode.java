/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.sam.moca.server.log.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A simple trie that it's keys are strings where the different keys are a form
 * of a package/class name in java which is separate by dots.
 * <p>
 * Currently this doesn't support removals.  But it would be easy enough to add
 * by doing a recursive search and if the node is removed remove any parents that
 * don't have any more nodes left.
 * <p>
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class PackageTrieNode implements PackageLookup {
    // @see com.sam.moca.server.log.filter.PackageLookup#isPackagePresent(java.lang.String)
    @Override
    public boolean isPackagePresent(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            throw new IllegalArgumentException("Package name provided was null or empty!");
        }
        String[] packages = packageName.split(Pattern.quote("."));
        return recursivelyCheckPackageExistance(this, packages, 0);
    }
    
    private static boolean recursivelyCheckPackageExistance(
        PackageTrieNode parentNode, String[] packages, int offset) {
        // If we have no more left to process then it wasn't present
        if (offset >= packages.length) {
            return false;
        }
        PackageTrieNode node = parentNode.getNamedNode(packages[offset]);
        if (node == null) {
            return false;
        }
        // If this node was present then we can stop
        if (node.isPresent) {
            return true;
        }
        return recursivelyCheckPackageExistance(node, packages, ++offset);
    }
    
    public void addPackage(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            throw new IllegalArgumentException("Package name provided was null or empty!");
        }
        String[] packages = packageName.split(Pattern.quote("."));
        recursivelyAddNodes(this, packages, 0);
    }
    
    private static void recursivelyAddNodes(PackageTrieNode parentNode, 
        String[] packages, int offset) {
        if (offset >= packages.length) {
            // The last element means we are present
            parentNode.isPresent = true;
            return;
        }
        PackageTrieNode node = parentNode.getNamedNode(packages[offset]);
        if (node == null) {
            node = new PackageTrieNode();
            parentNode.addNamedNode(packages[offset], node);
        }
        recursivelyAddNodes(node, packages, ++offset);
    }
    
    private PackageTrieNode getNamedNode(String name) {
        return values.get(name);
    }
    
    private void addNamedNode(String name, PackageTrieNode node) {
        values.put(name, node);
    }
    
    private final Map<String, PackageTrieNode> values = 
            new HashMap<String, PackageTrieNode>();
    private boolean isPresent;
}
