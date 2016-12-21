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

package com.sam.moca.server.exec.builtin;

import java.io.Serializable;

import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.SimpleResults;
import com.sam.moca.server.exec.ExecutableComponent;
import com.sam.moca.server.exec.ServerContext;
import com.sam.moca.util.MocaUtils;

public class CommitBuiltin implements ExecutableComponent, Serializable {
    @Override
    public MocaResults execute(ServerContext ctx) throws MocaException {
        ctx.logDebug(MocaUtils.concat("Executing built-in command: commit"));
        ctx.commit();
        return new SimpleResults();
    }

    private static final long serialVersionUID = 1L;
}