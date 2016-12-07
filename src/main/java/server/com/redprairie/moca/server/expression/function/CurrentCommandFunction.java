/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.server.expression.function;

import java.util.List;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.exec.ServerContext;

/**
 * This function will return back to the caller the most recently passed 
 * initiated statement. 
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class CurrentCommandFunction extends BaseFunction {

    // @see com.redprairie.moca.server.expression.function.MocaFunction#invoke(com.redprairie.moca.server.exec.ServerContext, java.util.List)
    @Override
    protected MocaValue invoke(ServerContext ctx, List<MocaValue> args)
            throws MocaException {
        if (args.size() != 0) {
            throw new FunctionArgumentException("expected no arguments");
        }
        
        return new MocaValue(MocaType.STRING, ctx.getLastStatementInitiated());
    }

}
