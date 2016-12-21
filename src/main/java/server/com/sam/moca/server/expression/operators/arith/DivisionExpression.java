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

package com.sam.moca.server.expression.operators.arith;

import com.sam.moca.MocaType;
import com.sam.moca.MocaValue;
import com.sam.moca.server.expression.Expression;
import com.sam.moca.server.expression.operators.AbsractOperExpression;

public class DivisionExpression extends AbsractOperExpression {
    public DivisionExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected MocaValue doOper(MocaValue left, MocaValue right)
            throws DivideByZeroException {
        double leftNum = left.asDouble();
        double rightNum = right.asDouble();
        
        if (rightNum == 0.0) {
            throw new DivideByZeroException();
        }
        
        return new MocaValue(MocaType.DOUBLE, Double.valueOf(leftNum / rightNum));
    }
    
    @Override
    protected String operString() {
        return " / ";
    }

}
