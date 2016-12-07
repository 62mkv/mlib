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

import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.expression.Expression;

public class ConcatExpression extends AbsractOperExpression {
    public ConcatExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected MocaValue doOper(MocaValue left, MocaValue right) {
        Object leftValue = left.getValue();
        Object rightValue = right.getValue();
        
        if (leftValue == null && rightValue == null) {
            return new MocaValue(MocaType.STRING, null);
        }
        else {
            StringBuilder result = new StringBuilder();
            if (!left.isNull()) result.append(left.asString());
            if (!right.isNull()) result.append(right.asString());
            return new MocaValue(MocaType.STRING, result.toString());
        }
    }
    
    @Override
    protected String operString() {
        return " || ";
    }
}
