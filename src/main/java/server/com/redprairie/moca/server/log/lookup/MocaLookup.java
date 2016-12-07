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

package com.redprairie.moca.server.log.lookup;

import org.apache.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.StrLookup;

import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.SystemContext;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2013 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
@Plugin(name = "moca", category = "Lookup")
public class MocaLookup implements StrLookup {

    // @see org.apache.logging.log4j.core.lookup.StrLookup#lookup(java.lang.String)

    @Override
    public String lookup(String arg0) {
        _log.info("Looking up: " + arg0);
        SystemContext ctx = ServerUtils.globalContext();
        String returnStr = ctx.getVariable(arg0);
        _log.info("Resolved to: " + returnStr);
        return returnStr;
    }

    // @see org.apache.logging.log4j.core.lookup.StrLookup#lookup(org.apache.logging.log4j.core.LogEvent, java.lang.String)

    @Override
    public String lookup(LogEvent arg0, String arg1) {
        return lookup(arg1);
    }
    
    private static final Logger _log = LogManager.getLogger(MocaLookup.class);
}
