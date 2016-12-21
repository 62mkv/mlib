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

package com.sam.moca.server;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permission;
import java.security.Principal;
import java.util.Set;

import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;


/**
 * This is the standard Moca Security Manager.  It is used to prevent operations
 * that should never be called on a running moca server such as System.exit();
 * 
 * We want to allow users to provide their own security manager or
 * even to call System.exit in their own server or client side applications.
 * Thus this should only ever been installed when MOCA is the driving factor
 * for the process executable.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class MocaSecurityManager extends SecurityManager {

    // @see java.lang.SecurityManager#checkExit(int)
    @Override
    public void checkExit(int status) {
        /** 
         * Right now only an exit can happen if it looks like the subject
         * executing it is from JMX with a name of 
         * {@link MocaServiceFunctions#_SHUTDOWNUSER}
         */ 
        AccessControlContext acc = AccessController.getContext();
        Subject subject = Subject.getSubject(acc);
        
        if (subject == null) {
            throw new SecurityException("Someone tried to exit the system with a " +
                    "status of " + status + " which is not allowed on the " +
                    "running moca server");
        }
        
        // Retrieve JMXPrincipal from Subject
        Set<JMXPrincipal> principals = subject.getPrincipals(JMXPrincipal.class);
        if (principals == null || principals.isEmpty()) {
            throw new SecurityException("Someone tried to exit the system with a " +
                    "status of " + status + " which is not allowed on the " +
                    "running moca server");
        }
        Principal principal = principals.iterator().next();
        String identity = principal.getName();
        if (!identity.equals(MocaServiceFunctions._SHUTDOWNUSER)) {
            throw new SecurityException("Someone tried to exit the system with a " +
            		"status of " + status + " which is not allowed on the " +
            	        "running moca server");
        }
        
        super.checkExit(status);
    }

    // @see java.lang.SecurityManager#checkPermission(java.security.Permission, java.lang.Object)
    @Override
    public void checkPermission(Permission perm, Object context) {
        // We want to let all permissions as normal, unless a person wants to
        // set the security manager; we don't want them to overwrite it.
        if (perm.getName().equals("setSecurityManager")) {
            throw new SecurityException("An attempt to change the running " +
            		"security manager for Java has been blocked");
        }
    }

    // @see java.lang.SecurityManager#checkPermission(java.security.Permission)
    @Override
    public void checkPermission(Permission perm) {
        // We want to let all permissions as normal, unless a person wants to
        // set the security manager; we don't want them to overwrite it.
        if (perm.getName().equals("setSecurityManager")) {
            throw new SecurityException("An attempt to change the running " +
                        "security manager for Java has been blocked");
        }
    }
}
