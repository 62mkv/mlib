/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

public enum SecurityLevel {
    // Only open (insecure) commands allowed.  This is equivalent to not being logged in
    OPEN, 
    // Public commands only.
    PUBLIC, 
    // Private commands -- usually reserved for use within components
    PRIVATE,
    // Sensitive commands -- potentially dangerous to the system
    ADMIN,
    // SQL
    SQL,
    // SCRIPTS
    SCRIPT,
    // Allow remote execution on other servers
    REMOTE,
    // All levels
    ALL  
}