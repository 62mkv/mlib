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

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.expression.Expression;
import com.redprairie.moca.server.expression.operators.AbsractOperExpression;

public class MinusExpression extends AbsractOperExpression {
    public MinusExpression(Expression left, Expression right) {
        super(left, right);
    }
    
    protected MocaValue doOper(MocaValue left, MocaValue right) {
        if (left.getType() == MocaType.DATETIME) {
            if (right.getType() == MocaType.DOUBLE || right.getType() == MocaType.INTEGER) {
                Date d = left.asDate();

                // If the left side is null, return a null result.
                if (d == null) {
                    return new MocaValue(MocaType.DATETIME, null);
                }
                
                LocalDateTime dt = new LocalDateTime(d);
                                
                int wholeDays = right.asInt();
                double dayPart = right.asDouble() - wholeDays;
                int msDiff = (int)(dayPart * 1000.0 * 3600.0 * 24.0);
                
                dt = dt.minusDays(wholeDays).minusMillis(msDiff);                
                
                return new MocaValue(MocaType.DATETIME, dt.toDateTime().toDate());
            }
            else if (right.getType() == MocaType.DATETIME) {
                Date leftDate = left.asDate();
                Date rightDate = right.asDate();
                
                // If either the left side or the right side is null, return null
                if (leftDate == null || rightDate == null) {
                    return new MocaValue(MocaType.DOUBLE, null);
                }
                
                DateTime leftDt = new DateTime(leftDate);
                DateTime rightDt = new DateTime(rightDate);
                
                int fullDays = Days.daysBetween(rightDt, leftDt).getDays();
                
                LocalTime leftTime = new LocalTime(leftDt);
                LocalTime rightTime = new LocalTime(rightDt);
                
                int ms = leftTime.getMillisOfDay() - rightTime.getMillisOfDay();
                double partial = ((double)ms / (1000.0 * 3600.0 * 24.0));
                
                if (partial < 0.0 && leftDate.after(rightDate)) {
                    partial += 1.0;
                }
                else if (partial > 0.0 && rightDate.after(leftDate)) {
                    partial -= 1.0;
                }
                
                double daysDiff = (double) fullDays + partial;
                
                return new MocaValue(MocaType.DOUBLE, daysDiff);
            }
        }
        else {
            if (left.getType() == MocaType.DOUBLE || right.getType() == MocaType.DOUBLE) {
                return new MocaValue(MocaType.DOUBLE, Double.valueOf(left.asDouble() - right.asDouble()));
            }
            else {
                return new MocaValue(MocaType.INTEGER, Integer.valueOf(left.asInt() - right.asInt()));
            }
        }
        return null;
    }

    @Override
    protected String operString() {
        return " - ";
    }

}
