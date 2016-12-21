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

package com.sam.moca.server.expression.operators.compare;

import com.sam.moca.MocaType;
import com.sam.moca.MocaValue;
import com.sam.moca.server.expression.Expression;
import com.sam.moca.server.expression.operators.AbsractOperExpression;

public class EqualsExpression extends AbsractOperExpression {
    public EqualsExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    /**
     * Compare two values for equality.  This method is public to expose the logic of the equals operator.  Note
     * that this does not exactly conform to the equals method on MocaValue, but is much more lenient about type
     * conversion.
     * 
     * @param left any MOCA value.  This parameter may not be null.
     * @param right any MOCA value.  This parameter may not be null
     * @return true, if the values are considered equal, according to MOCA's equality logic.
     */
    public static boolean valueEquality(MocaValue left, MocaValue right) {
        if (left.isNull() && right.isNull()) return true;
        if (left.isNull() || right.isNull()) return false;
        
        if (left.getType() == right.getType()) {
            return left.getValue().equals(right.getValue());
        }
        else if (left.getType() == MocaType.STRING || right.getType() == MocaType.STRING) {
            return left.asString().equals(right.asString());
        }
        else if (left.getType() == MocaType.DOUBLE || right.getType() == MocaType.DOUBLE) {
            return Double.doubleToLongBits(left.asDouble()) == Double
                .doubleToLongBits(right.asDouble());
        }
        else if (left.getType() == MocaType.INTEGER || right.getType() == MocaType.INTEGER) {
            return left.asInt() == right.asInt();
        }
        else if (left.getType() == MocaType.BOOLEAN || right.getType() == MocaType.BOOLEAN) {
            return left.asBoolean() == right.asBoolean();
        }
        else {
            return left.asString().equals(right.asString());
        }
    }
    
    protected MocaValue doOper(MocaValue left, MocaValue right) {
        return new MocaValue(MocaType.BOOLEAN, Boolean.valueOf(compare(left,right))); 
    }
    
    protected boolean compare(MocaValue left, MocaValue right) {
        return EqualsExpression.valueEquality(left, right);
    }

    @Override
    protected String operString() {
        return "=";
    }
}
