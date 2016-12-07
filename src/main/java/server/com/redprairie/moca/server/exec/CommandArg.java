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

package com.redprairie.moca.server.exec;

import com.redprairie.moca.MocaOperator;
import com.redprairie.moca.server.expression.Expression;

public class CommandArg {
    public void setName(String name) {
        _name = name;
    }

    public void setTargetName(String targetName) {
        _targetName = targetName;
    }

    public void setOperator(MocaOperator operator) {
        _operator = operator;
    }

    public void setValue(Expression value) {
        _value = value;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return _name;
    }

    /**
     * @return Returns the value.
     */
    public Expression getValue() {
        return _value;
    }

    /**
     * @return Returns the operator.
     */
    public MocaOperator getOperator() {
        return _operator;
    }

    /**
     * @return the target name for this argument
     */
    public String getTargetName() {
        return _targetName;
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();
        if (_targetName != null) {
            tmp.append(_targetName).append('^');
        }
        tmp.append(_name);
        tmp.append(' ');
        tmp.append(_operator);
        tmp.append(' ');
        tmp.append(_value);
        return tmp.toString();
    }

    // Privates
    private String _name;
    private String _targetName;
    private MocaOperator _operator;
    private Expression _value;
}
