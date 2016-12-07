/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

import java.util.ArrayList;
import java.util.List;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.expression.Expression;

/**
 * An abstract base class that evaluates all argument expressions before calling the invoke method.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public abstract class BaseFunction implements MocaFunction {

    // @see com.redprairie.moca.server.expression.function.MocaFunction#invoke(com.redprairie.moca.server.exec.ServerContext, java.util.List)
    @Override
    public MocaValue evaluate(ServerContext ctx, List<Expression> args) throws MocaException {
        List<MocaValue> argValues = new ArrayList<MocaValue>();
        
        for (Expression arg : args) {
            argValues.add(arg.evaluate(ctx));
        }
        
        return invoke(ctx, argValues);
    }
    
    /**
     * Invokes the function implementation with all arguments evaluated.
     * @param ctx The <code>ServerContext</code> to use for execution of the
     *            function, if it needs to get data or execute commands within
     *            the server execution context.
     * @param argValues Pre-evaluated arguments to the function call.
     * @return a single value.
     * @throws MocaException if an error occurred during function execution.
     */
    protected abstract MocaValue invoke(ServerContext ctx, List<MocaValue> argValues) throws MocaException;
}
