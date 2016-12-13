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

package com.redprairie.moca.server;

import java.security.Permission;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * This is to test to make sure the security manager is working correctly.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_SecurityManager {
    @BeforeClass
    public static void setupSecurityManager() {
        assertNull("There shouldn't be a security manager", 
                System.getSecurityManager());
        
        System.setSecurityManager(new DelegatedSecurityManager(
                new MocaSecurityManager()));
    }
    
    @AfterClass
    public static void tearDownSecurityManager() {
        // Now we want to remove the security manager so that everything else
        // is okay
        SecurityManager manager = System.getSecurityManager();
        
        if (manager instanceof DelegatedSecurityManager) {
            ((DelegatedSecurityManager) manager)._manager = null;
        }
        
        System.setSecurityManager(null);
    }

    public void testNoAllowOfSystemExit() {
        System.err.println("Attempting to call System.exit.");
        System.err.println("If the system went away this unit test failed");
        
        try {
            System.exit(10);
            fail("We should have received a security exception");
        }
        catch (SecurityException ex) {
            // We should have received a security exception
        }
    }
    
    @Test
    public void testNoAllowOfSettingSecurityManager() {
        try {
            System.setSecurityManager(new SecurityManager());
            fail("We should have received a security exception");
        }
        catch (SecurityException ex) {
            // We should have received a security exception
        }
    }
    
    private static class DelegatedSecurityManager extends SecurityManager {
        public DelegatedSecurityManager(SecurityManager manager) {
            _manager = manager;
        }
        
        // @see java.lang.SecurityManager#checkExit(int)
        @Override
        public void checkExit(int status) {
            if (_manager != null) {
                _manager.checkExit(status);
            }
        }

        // @see java.lang.SecurityManager#checkPermission(java.security.Permission, java.lang.Object)
        @Override
        public void checkPermission(Permission perm, Object context) {
            if (_manager != null) {
                _manager.checkPermission(perm, context);
            }
        }

        // @see java.lang.SecurityManager#checkPermission(java.security.Permission)
        @Override
        public void checkPermission(Permission perm) {
            if (_manager != null) {
                _manager.checkPermission(perm);
            }
        }

        SecurityManager _manager;
    }
}
