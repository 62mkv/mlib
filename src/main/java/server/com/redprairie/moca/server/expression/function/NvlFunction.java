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
 * Simulates the Oracle NVL function.  If the first argument
 * is not null, it is returned.  Otherwise, the second argument is
 * returned.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author derek
 * @version $Revision$
 */
public class NvlFunction implements MocaFunction {

    @Override
    public MocaValue evaluate(ServerContext ctx, List<Expression> args) throws MocaException {
        if (args.size() != 2) {
            throw new FunctionArgumentException("expected 2 arguments");
        }
        
        MocaValue test = args.get(0).evaluate(ctx);
        
        if (!test.isNull()) {
            return test;
        }
        else {
            return args.get(1).evaluate(ctx);
        }
    }
    
}
