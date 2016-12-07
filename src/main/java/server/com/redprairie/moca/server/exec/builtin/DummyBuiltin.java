/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.server.exec.builtin;

import java.io.Serializable;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.server.exec.ExecutableComponent;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.util.MocaUtils;

public class DummyBuiltin implements ExecutableComponent, Serializable {
    public DummyBuiltin(String name) {
        _name = name;
    }
    @Override
    public MocaResults execute(ServerContext ctx) throws MocaException {
        ctx.logDebug(MocaUtils.concat("Executing built-in command: " + _name));
        return new SimpleResults();
    }
    private final String _name;
    
    private static final long serialVersionUID = 1L;
}