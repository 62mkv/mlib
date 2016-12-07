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

package com.redprairie.moca.server.expression.operators.arith;

import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.expression.Expression;
import com.redprairie.moca.server.expression.operators.AbsractOperExpression;

public class ModExpression extends AbsractOperExpression {
    public ModExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected MocaValue doOper(MocaValue left, MocaValue right)
            throws DivideByZeroException {
        int leftNum = left.asInt();
        int rightNum = right.asInt();

        if (rightNum == 0) {
            throw new DivideByZeroException();
        }

        return new MocaValue(MocaType.INTEGER, Integer.valueOf(leftNum % rightNum));
    }
    
    @Override
    protected String operString() {
        return " % ";
    }

}
