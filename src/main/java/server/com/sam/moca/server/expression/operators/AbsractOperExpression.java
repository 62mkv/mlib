/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20167
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

package com.sam.moca.server.expression.operators;

import com.sam.moca.MocaException;
import com.sam.moca.MocaValue;
import com.sam.moca.server.exec.ServerContext;
import com.sam.moca.server.expression.Expression;

public abstract class AbsractOperExpression implements Expression {
    public AbsractOperExpression(Expression left, Expression right) {
        _left = left;
        _right = right;
    }
    
    public MocaValue evaluate(ServerContext ctx) throws MocaException {
        MocaValue leftValue = _left.evaluate(ctx);
        MocaValue rightValue = _right.evaluate(ctx);
        
        return doOper(leftValue, rightValue);
    }
    
    @Override
    public String toString() {
    	return _left.toString() + operString() + _right.toString();
    }
    
    protected String operString() {
    	return "";
    }
    
    protected abstract MocaValue doOper(MocaValue left, MocaValue right)
        throws MocaException;
    
    private final Expression _left;
    private final Expression _right;
}
