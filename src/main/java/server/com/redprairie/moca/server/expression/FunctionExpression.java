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

package com.redprairie.moca.server.expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.expression.function.Base64DecodeFunction;
import com.redprairie.moca.server.expression.function.Base64EncodeFunction;
import com.redprairie.moca.server.expression.function.CommandFunction;
import com.redprairie.moca.server.expression.function.ConditionalFunction;
import com.redprairie.moca.server.expression.function.ConstantFunction;
import com.redprairie.moca.server.expression.function.CurrentCommandFunction;
import com.redprairie.moca.server.expression.function.DateFunction;
import com.redprairie.moca.server.expression.function.DbDateFunction;
import com.redprairie.moca.server.expression.function.DbTypeFunction;
import com.redprairie.moca.server.expression.function.DecodeFunction;
import com.redprairie.moca.server.expression.function.DoubleFunction;
import com.redprairie.moca.server.expression.function.InstrFunction;
import com.redprairie.moca.server.expression.function.IntFunction;
import com.redprairie.moca.server.expression.function.LRPadFunction;
import com.redprairie.moca.server.expression.function.LRPadFunction.PadType;
import com.redprairie.moca.server.expression.function.LengthFunction;
import com.redprairie.moca.server.expression.function.LowerFunction;
import com.redprairie.moca.server.expression.function.MaxFunction;
import com.redprairie.moca.server.expression.function.MinFunction;
import com.redprairie.moca.server.expression.function.MocaFunction;
import com.redprairie.moca.server.expression.function.NextValFunction;
import com.redprairie.moca.server.expression.function.NvlFunction;
import com.redprairie.moca.server.expression.function.RowcountFunction;
import com.redprairie.moca.server.expression.function.SprintfFunction;
import com.redprairie.moca.server.expression.function.StringFunction;
import com.redprairie.moca.server.expression.function.SubstrFunction;
import com.redprairie.moca.server.expression.function.SysdateFunction;
import com.redprairie.moca.server.expression.function.ToCharFunction;
import com.redprairie.moca.server.expression.function.ToDateFunction;
import com.redprairie.moca.server.expression.function.ToNumberFunction;
import com.redprairie.moca.server.expression.function.TrimFunction;
import com.redprairie.moca.server.expression.function.TypeFunction;
import com.redprairie.moca.server.expression.function.UpperFunction;
import com.redprairie.moca.util.MocaUtils;

/**
 * An expression that represents a MOCA function.  Functions are evaluated in-line as values, and there's
 * limited support for calling user-supplied commands.
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class FunctionExpression implements Expression {
    
    public static FunctionExpression getBuiltInFunction(String name, List<Expression> args) {
        MocaFunction function = _FUNCTIONS.get(name.toLowerCase());
        if (function != null) {
            return new FunctionExpression(name, args, function);
        }
        else {
            return null;
        }
    }
    
    public static FunctionExpression getCommandFunction(String name, List<Expression> args) {
        return new FunctionExpression(name, args, new CommandFunction(name));
    }
    
    private FunctionExpression(String name, List<Expression> args, MocaFunction function) {
        _name = name;
        _args = args;
        _function = function;
    }
    
    public MocaValue evaluate(ServerContext ctx) throws MocaException {
        _LOG.debug(MocaUtils.concat("Evaluating function: ", this));
        return _function.evaluate(ctx, _args);
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();
        tmp.append(_name);
        tmp.append('(');
        boolean firstOne = true;
        for (Expression arg : _args) {
            if (firstOne) {
                firstOne = false;
            }
            else {
                tmp.append(',');
            }
            tmp.append(arg);
        }
        tmp.append(')');
        return tmp.toString();
    }

    
    private final String _name;
    private final List<Expression> _args;
    private final MocaFunction _function;
    
    private final static Map<String, MocaFunction> _FUNCTIONS = new HashMap<String, MocaFunction>();
    static {
        _FUNCTIONS.put("date", new DateFunction());
        _FUNCTIONS.put("string", new StringFunction());
        _FUNCTIONS.put("int", new IntFunction());
        _FUNCTIONS.put("float", new DoubleFunction());
        _FUNCTIONS.put("upper", new UpperFunction());
        _FUNCTIONS.put("lower", new LowerFunction());
        _FUNCTIONS.put("trim", new TrimFunction());
        _FUNCTIONS.put("rtrim", new TrimFunction());
        _FUNCTIONS.put("len", new LengthFunction());
        _FUNCTIONS.put("length", new LengthFunction());
        _FUNCTIONS.put("sprintf", new SprintfFunction());
        _FUNCTIONS.put("iif", new ConditionalFunction());
        _FUNCTIONS.put("decode", new DecodeFunction());
        _FUNCTIONS.put("nvl", new NvlFunction());
        _FUNCTIONS.put("min", new MinFunction());
        _FUNCTIONS.put("max", new MaxFunction());
        _FUNCTIONS.put("substr", new SubstrFunction());
        _FUNCTIONS.put("instr", new InstrFunction());
        _FUNCTIONS.put("rowcount", new RowcountFunction());
        _FUNCTIONS.put("b64decode", new Base64DecodeFunction());
        _FUNCTIONS.put("b64encode", new Base64EncodeFunction());
        _FUNCTIONS.put("sysdate", new SysdateFunction());
        _FUNCTIONS.put("true", new ConstantFunction(MocaType.BOOLEAN, Boolean.TRUE));
        _FUNCTIONS.put("false", new ConstantFunction(MocaType.BOOLEAN, Boolean.FALSE));
        _FUNCTIONS.put("dbtype", new DbTypeFunction());
        _FUNCTIONS.put("dbdate", new DbDateFunction());
        _FUNCTIONS.put("nextval", new NextValFunction());
        _FUNCTIONS.put("type", new TypeFunction());
        _FUNCTIONS.put("command", new CurrentCommandFunction());
        _FUNCTIONS.put("to_date", new ToDateFunction());
        _FUNCTIONS.put("to_char", new ToCharFunction());
        _FUNCTIONS.put("to_number", new ToNumberFunction());
        _FUNCTIONS.put("lpad", new LRPadFunction(PadType.LEFT));
        _FUNCTIONS.put("rpad", new LRPadFunction(PadType.RIGHT));
    }
    
    private static final Logger _LOG = LogManager.getLogger(FunctionExpression.class);
}
