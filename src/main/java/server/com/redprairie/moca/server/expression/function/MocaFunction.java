/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.redprairie.moca.server.expression.function;

import java.util.List;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.expression.Expression;

/**
 * The basic built-in function definition.  Functions are handed a list of expressions as arguments, and must 
 * evaluate them based on the needs of the specific function.  Most functions will extends the abstract class
 * <code>BaseFunction</code> to facilitate the evaluation of function arguments.
 * 
 * @see BaseFunction
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author derek
 * @version $Revision$
 */
public interface MocaFunction {
    
    /**
     * Call this function. When this method is called, all arguments will have
     * been evaluated. The value of those arguments are passed into the
     * <code>invoke</code> method as the <code>args</code> parameter.
     * 
     * @param ctx The <code>ServerContext</code> to use for execution of the
     *            function, if it needs to get data or execute commands within
     *            the server execution context.
     * @param args Arguments to the function call.
     * @return a single value.
     * @throws MocaException if an error occurs.
     */
    public MocaValue evaluate(ServerContext ctx, List<Expression> args) throws MocaException;
}
