/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.server.expression.function;

import java.util.List;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.exec.ServerContext;

/**
 * Left or right pad a string in the same way as a database does it.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
public class LRPadFunction extends BaseFunction {
    
    public static enum PadType {LEFT, RIGHT};
    
    public LRPadFunction(PadType type) {
        _type = type;
    }

    // @see com.redprairie.moca.server.expression.function.BaseFunction#invoke(com.redprairie.moca.server.exec.ServerContext, java.util.List)
    @Override
    protected MocaValue invoke(ServerContext ctx, List<MocaValue> argValues)
            throws MocaException {
        
        if (argValues.size() < 2) {
            throw new FunctionArgumentException(
                "expected two or three arguments");
        }
        
        if (argValues.size() > 3) {
            throw new FunctionArgumentException(
            "expected two or three arguments");
        }

        if (argValues.get(0).isNull()) {
            return new MocaValue(MocaType.STRING, null);
        }

        String value = argValues.get(0).asString();
        
        int len = argValues.get(1).asInt();
        if (len < 0) {
            len = 0;
        }
        
        String padExpr = " ";
        if (argValues.size() > 2) {
            padExpr = argValues.get(2).asString();
            if (padExpr == null || padExpr.isEmpty()) {
                throw new FunctionArgumentException(
                "pad expression must not be null");
            }
        }
        
        if (len < value.length()) {
            return new MocaValue(MocaType.STRING, value.substring(0, len));
        }
        else if (len == value.length()) {
            return new MocaValue(MocaType.STRING, value);
        }
        else {
            StringBuilder buf = new StringBuilder();
            if (_type == PadType.LEFT) {
                int need = len - value.length();
                while (buf.length() < need) {
                    buf.append(padExpr);
                }
                buf.delete(need, buf.length());
                buf.append(value);
            }
            else {
                buf.append(value);
                int need = len;
                while (buf.length() < need) {
                    buf.append(padExpr);
                }
                buf.delete(need, buf.length());
            }
            
            return new MocaValue(MocaType.STRING, buf.toString());
        }
        
    }

    private final PadType _type;
}
