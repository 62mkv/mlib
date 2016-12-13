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

package com.redprairie.moca.server.expression.operators.compare;

import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.expression.Expression;
import com.redprairie.moca.server.expression.operators.AbsractOperExpression;

public class LessThanExpression extends AbsractOperExpression {
    public LessThanExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected MocaValue doOper(MocaValue left, MocaValue right) {
        return new MocaValue(MocaType.BOOLEAN, Boolean.valueOf(compare(left,right))); 
    }
    
    private boolean compare(MocaValue left, MocaValue right) {
        return (left.compareTo(right) < 0);
    }
    
    @Override
    protected String operString() {
        return "<";
    }
}
