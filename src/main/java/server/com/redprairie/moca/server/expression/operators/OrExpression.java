/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2007
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

package com.redprairie.moca.server.expression.operators;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.expression.Expression;

public class OrExpression implements Expression {
    public OrExpression(Expression left, Expression right) {
        _left = left;
        _right = right;
    }
    
    public MocaValue evaluate(ServerContext ctx) throws MocaException {
        MocaValue leftValue = _left.evaluate(ctx);
        boolean result;
        
        if (leftValue.asBoolean()) {
            result = true;
        }
        else {
            MocaValue rightValue = _right.evaluate(ctx);
            result = rightValue.asBoolean();
        }
        
        return new MocaValue(MocaType.BOOLEAN, Boolean.valueOf(result));
    }
    
    @Override
    public String toString() {
        return _left.toString() + " OR " + _right.toString();
    }
    
    private final Expression _left;
    private final Expression _right;    
}
