/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.sam.moca.server.legacy;

import com.sam.moca.server.profile.CommandPath;

/**
 * Moca Server Adapter that is aware of Context specific calls
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public interface MocaContextServerAdapter extends MocaServerAdapter {
    /**
     * Returns the current command path of the moca execution engine.
     * @return The command path object
     */
    public CommandPath getCurrentCommandPath();
}