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

package com.sam.moca.server.expression;

import com.sam.moca.MocaArgument;
import com.sam.moca.MocaException;
import com.sam.moca.MocaType;
import com.sam.moca.MocaValue;
import com.sam.moca.server.exec.ServerContext;

public class ReferenceExpression implements Expression {
    public ReferenceExpression(String name, boolean system, boolean anyOper, boolean markUsed, boolean checkOnly) {
        _name = name;
        _system = system;
        _anyOper = anyOper;
        _checkOnly = checkOnly;
        _markUsed = markUsed;
    }
    
    public MocaValue evaluate(ServerContext ctx) throws MocaException {
        if (_system) {
            return new MocaValue(MocaType.STRING, ctx.getSystemVariable(_name));
        }
        else {
            MocaType type;
            Object value;
            if (_anyOper) {
                MocaArgument tmp = ctx.getVariableAsArgument(_name, _markUsed, false);
                if (_checkOnly) {
                    type = MocaType.BOOLEAN;
                    value = Boolean.valueOf(tmp != null);
                }
                else if (tmp == null) {
                    type = MocaType.STRING;
                    value = null;
                }
                else {
                    type = tmp.getType();
                    value = tmp.getValue();
                }
            }
            else {
                MocaValue tmp = ctx.getVariable(_name, _markUsed);
                if (_checkOnly) {
                    type = MocaType.BOOLEAN;
                    value = Boolean.valueOf(tmp != null);
                }
                else if (tmp == null) {
                    type = MocaType.STRING;
                    value = null;
                }
                else {
                    type = tmp.getType();
                    value = tmp.getValue();
                }
            }
            
            return new MocaValue(type, value);
        }
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("@");
        
        if (_system) {
            out.append('@');
        }
        else if (_anyOper) {
            out.append("+");
        }
        
        out.append(_name);
        
        if (!_markUsed) {
            out.append("#keep");
        }
        if (_checkOnly) {
            out.append("#onstack");
        }
        
        return out.toString();
    }

    private final String _name;
    private final boolean _system;
    private final boolean _anyOper;
    private final boolean _markUsed;
    private final boolean _checkOnly;
}
