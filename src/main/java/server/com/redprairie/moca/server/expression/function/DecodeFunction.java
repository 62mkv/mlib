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

import java.util.Iterator;
import java.util.List;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.expression.Expression;
import com.redprairie.moca.server.expression.operators.compare.EqualsExpression;

/**
 * Simulates the Oracle DECODE function.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author derek
 * @version $Revision$
 */
public class DecodeFunction implements MocaFunction {

    @Override
    public MocaValue evaluate(ServerContext ctx, List<Expression> args) throws MocaException {
        if (args.size() < 2) {
            throw new FunctionArgumentException("expected at least 2 arguments");
        }
        
        Iterator<Expression> i = args.iterator();
        Expression sourceExpr = i.next();
        MocaValue source = sourceExpr.evaluate(ctx);
        
        do {
            Expression testExpr = i.next();
            MocaValue test = testExpr.evaluate(ctx);
            if (i.hasNext()) {
                Expression result = i.next();
                
                if (EqualsExpression.valueEquality(source, test)) {
                    return result.evaluate(ctx);
                }
            }
            else {
                return test;
            }
        } while (i.hasNext());

        return new MocaValue(source.getType(), null);
    }
}
