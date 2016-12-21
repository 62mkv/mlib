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

import java.util.Date;

import org.joda.time.LocalDateTime;

import com.sam.moca.MocaType;
import com.sam.moca.MocaValue;
import com.sam.moca.server.expression.Expression;
import com.sam.moca.server.expression.operators.AbsractOperExpression;

public class PlusExpression extends AbsractOperExpression {
    public PlusExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected MocaValue doOper(MocaValue left, MocaValue right) {
        if (left.getType() == MocaType.DATETIME) {
            return addToDate(left, right);
        }
        else if (right.getType() == MocaType.DATETIME) {
            return (addToDate(right, left));
        }
        else {
            if (left.getType() == MocaType.DOUBLE || right.getType() == MocaType.DOUBLE) {
                return new MocaValue(MocaType.DOUBLE, Double.valueOf(left.asDouble() + right.asDouble()));
            }
            else {
                return new MocaValue(MocaType.INTEGER, Integer.valueOf(left.asInt() + right.asInt()));
            }
        }
    }

    @Override
    protected String operString() {
        return " + ";
    }
    
    private MocaValue addToDate(MocaValue date, MocaValue days) {
        Date d = date.asDate();

        // If the left side is null, return a null result.
        if (d == null) {
            return new MocaValue(MocaType.DATETIME, null);
        }
        
        MocaType daysType = days.getType();
        if (daysType == MocaType.INTEGER || daysType == MocaType.DOUBLE) { 
            LocalDateTime dt = new LocalDateTime(d);

            int wholeDays = days.asInt();
            double dayPart = days.asDouble() - wholeDays;
            int msDiff = (int)(dayPart * 1000.0 * 3600.0 * 24.0);
            
            dt = dt.plusDays(wholeDays).plusMillis(msDiff);                
            
            return new MocaValue(MocaType.DATETIME, dt.toDateTime().toDate());
        }
        else {
            return date;
        }
    }

}
