/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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
import com.redprairie.moca.server.expression.Expression;

/**
 * Implements the <code>iif</code> MOCA function. Generally, this function
 * takes three arguments. The first argument is evaluated as a boolean. If it
 * returns <code>true</code>, the second argument is returned, otherwise, the
 * third argument is returned.  If only two arguments are passed, and the first
 * argument is <code>false</code>, NULL is returned.
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author derek
 * @version $Revision$
 */
public class ConditionalFunction implements MocaFunction {

    @Override
    public MocaValue evaluate(ServerContext ctx, List<Expression> args) throws MocaException {
        if (args.size() < 2 || args.size() > 3) {
            throw new FunctionArgumentException("expected 2 or 3 arguments");
        }
        
        MocaValue test = args.get(0).evaluate(ctx);
        
        if (test.asBoolean()) {
            return args.get(1).evaluate(ctx);
        }
        else {
            if (args.size() == 3) {
                return args.get(2).evaluate(ctx);
            }
            else {
                return new MocaValue(MocaType.STRING, null); 
            }
        }
    }
}
