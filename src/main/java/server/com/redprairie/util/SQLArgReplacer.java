/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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

package com.redprairie.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaArgumentException;
import com.redprairie.moca.MocaOperator;
import com.redprairie.moca.MocaRuntimeException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.exec.ArgumentSource;


/**
 * 
 * @variable turns into 'value', where the operator is '='.
 * @-variable turns into var='value', regardless of the operator.
 * @@variable turns into 'env_value'.
 * @*variable turns into value.
 * @+variable turns into var <operator> 'value'.
 * @%variable turns into var like 'value' or var <operator> value'.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */
public class SQLArgReplacer {
    
    
    public SQLArgReplacer(String sql, ArgumentSource ctx) {
        this(sql, ctx, new BindList(), 0);
    }

    public String getSQLString() {
        return _out.toString();
    }
    
    public BindList getBindList() {
        return _bindList;
    }

    /**
     * Private constructor -- this allows for recursive instances of this class that make use of
     * the same bind list and counter
     */
    private SQLArgReplacer(String sql, ArgumentSource ctx, BindList bind, int bindCount) {
        _in = sql;
        _out = new StringBuilder(sql.length());
        _bindList = bind;
        _bindCount = bindCount;
        translate(ctx);
    }
    
    /**
     * Perform the variable substitution on the given string.
     * @param _in the string on which to perform substitution.
     * @return the input string with delimited identifiers substituted with
     *         their values, according to the passed-in ReplacementStrategy.
     */
    private void translate(ArgumentSource ctx) {
        // Don't bother if a null string is passed in.
        if (_in == null)
            return;
        
        ScanMode mode = ScanMode.DEFAULT;
        
        int length = _in.length();

        for (int i = 0; i < length; i++) {
            char c = _in.charAt(i);
            switch(mode) {
            case DEFAULT:
                if (c == '@') {
                    i++;
                    char nextChar = _in.charAt(i);
                    if (nextChar == '*') {
                        _replaceAll(ctx);
                    }
                    else {
                        ReplaceMode replaceMode = ReplaceMode.DEFAULT;
                        if (nextChar == '+') {
                            i++;
                            replaceMode = ReplaceMode.OPER;
                        }
                        else if (nextChar == '%') {
                            i++;
                            replaceMode = ReplaceMode.LIKE;
                        }
                        else if (nextChar == '-') {
                            i++;
                            replaceMode = ReplaceMode.IGNOREOPER;
                        }
                        else if (nextChar == '@') {
                            i++;
                            replaceMode = ReplaceMode.ENV;
                        }
                        
                        StringBuilder varName = new StringBuilder();
                        while (i < length) {
                            c = _in.charAt(i);
                            if (c != '^' && c != ':' && c != '#' && c != '.' && !Character.isJavaIdentifierPart(c)) {
                                break;
                            }
                            varName.append(_in.charAt(i));
                            i++;
                        }
                        
                        _replace(varName.toString(), ctx, replaceMode);
                        
                        i--;
                    }
                }
                else {
                    _out.append(c);
                    if (c == '\'') {
                        mode = ScanMode.SINGLEQUOTE;
                    }
                    else if (c == '\"') {
                        mode = ScanMode.DOUBLEQUOTE;
                    }
                    else if (c == '/' && i < length - 1 && _in.charAt(i+1) == '*') {
                        i++;
                        _out.append('*');
                        mode = ScanMode.COMMENT;
                    }
                }
                break;
            case COMMENT:
                _out.append(c);
                if (c == '*' && i < length - 1 && _in.charAt(i+1) == '/') {
                    i++;
                    _out.append('/');
                    mode = ScanMode.DEFAULT;
                }
                break;
            case SINGLEQUOTE:
                if (c == '\'') {
                    mode = ScanMode.DEFAULT;
                }
                _out.append(c);
                break;
            case DOUBLEQUOTE:
                if (c == '"') {
                    mode = ScanMode.DEFAULT;
                }
                _out.append(c);
                break;
            }
        }
    }
    
    private void _replace(String name, ArgumentSource ctx, ReplaceMode mode) {
        // First, separate the names from the directives attached to the name.
        Matcher flags = directivePattern.matcher(name);
        if (!flags.matches()) {
            _out.append('@');
            _out.append(name);
            return;
        }

        String varName = flags.group(1);

        // If a single @ appears, put it back into the stream as-is
        if (varName.length() == 0) {
            _out.append('@');
            return;
        }
        
        // We need to deal with variables of the form:
        // @+table.column
        // @+table.column^stackname
        // @+schema.table.column
        // etc.
        String stackName = flags.group(2);
        if (stackName.length() == 0) {
            if (varName.indexOf('.') != -1) {
                String[] qualifiedNames = varName.split("\\.");
                stackName = qualifiedNames[qualifiedNames.length - 1];
            }
            else {
                stackName = varName;
            }
        }

        boolean markUsed = true;
        String directive = flags.group(3);
        if (directive.equalsIgnoreCase("keep")) {
            markUsed = false;
        }

        String cast = flags.group(4).toLowerCase();
        
        if (mode == ReplaceMode.ENV) {
            MocaValue value = new MocaValue(MocaType.STRING, ctx.getSystemVariable(stackName));
            _addValue(value, null, cast);
        }
        else if (mode == ReplaceMode.IGNOREOPER) {
            MocaArgument var = ctx.getVariableAsArgument(stackName, markUsed, false);
            if (var != null) {
                MocaValue value = new MocaValue(var.getType(), var.getValue());
                _addValue(value, null, cast);
            }
            else {
                // Default as String datatype since we can't determine it
                _addValue(null, null, "");
            }
        }
        else if (mode == ReplaceMode.DEFAULT) {
            MocaValue value = ctx.getVariable(stackName, markUsed);
            _addValue(value, stackName, cast);
        }
        else if (mode == ReplaceMode.OPER) {
            MocaArgument var = ctx.getVariableAsArgument(stackName, markUsed, false);
            _addVar(ctx, var, varName, cast);
        }
        else if (mode == ReplaceMode.LIKE) {
            MocaArgument var = ctx.getVariableAsArgument(stackName, markUsed, false);
            
            // If the variable is not null and it is not a like operation and 
            // the value is not null then we can check it
            if (var != null && var.getOper() != MocaOperator.LIKE && 
                    var.getValue() != null) {
                String value = var.getValue().toString();
                
                // We only put the like if the value contains % or _
                if (value != null && (value.contains("%") || 
                    value.contains("_"))) {
                    // We force the operator to be like if everything passed
                    var = new MocaArgument(var.getName(), 
                            MocaOperator.LIKE, var.getType(), var.getValue());
                }
            }
            _addVar(ctx, var, varName, cast);
        }
    }
    
    private void _replaceAll(ArgumentSource ctx) {
        MocaArgument[] args = ctx.getCommandArgs(false, true);
        
        if (args.length == 0) {
            _out.append("1 = 1");
        }
        else {
            for (int i = 0; i < args.length; i++) {
                MocaArgument var = args[i];
                if (i != 0) {
                    _out.append(" and ");
                }
                String varName = var.getName();
                _addVar(ctx, var, varName, "");
            }
        }
    }
    
    private void _addVar(ArgumentSource ctx, MocaArgument var, String name, String cast) {
        
        if (var == null) {
            _out.append("1 = 1");
        }
        else if (var.getOper() == MocaOperator.RAWCLAUSE) {
            SQLArgReplacer scanner = new SQLArgReplacer(String.valueOf(var.getValue()), ctx, _bindList, _bindCount);
            _bindCount = scanner._bindCount;
            _out.append(scanner.getSQLString());
        }
        else if (var.getOper() == MocaOperator.NAMEDCLAUSE) {
            _out.append(name);
            _out.append(' ');
            SQLArgReplacer scanner = new SQLArgReplacer(String.valueOf(var.getValue()), ctx, _bindList, _bindCount);
            _bindCount = scanner._bindCount;
            _out.append(scanner.getSQLString());
        }
        else {
            // First, check the type:
            if (var.getType() == MocaType.OBJECT || var.getType() == MocaType.UNKNOWN) {
                throw new MocaRuntimeException(new MocaArgumentException("Unknown parameter type for: " + name + ": " + var.getType()));
            }
            
            MocaOperator varOper = var.getOper();
            boolean castNameDate = false;
            if ((varOper == MocaOperator.LIKE || varOper == MocaOperator.NOTLIKE) && cast.equals("date")) {
                castNameDate = true;
                _out.append("TO_CHAR(");
                _out.append(name);
                _out.append(", 'YYYYMMDDHH24MISS') ");
            }
            else {
                _out.append(name);
                _out.append(' ');
            }
            
            if (var.getOper() == MocaOperator.ISNULL || var.getOper() == MocaOperator.NOTNULL) {
                _out.append(var.getOper().getSQLForm());
            }
            else {
                Object value = var.getValue();
                
                if ((value == null || value.equals("")) && 
                    (var.getOper() == MocaOperator.EQ || var.getOper() == MocaOperator.NE)) {
                    if (var.getOper() == MocaOperator.EQ) {
                        _out.append("is null");
                    }
                    else {
                        _out.append("is not null");
                    }
                }
                else {
                    _out.append(var.getOper().getSQLForm());
                    _out.append(' ');
                    String bindName = "var" + _bindCount++;
                    if (cast.equals("date")) {
                        if (castNameDate) {
                            _out.append(':');
                            _out.append(bindName);
                        }
                        else {
                            _out.append("TO_DATE(:");
                            _out.append(bindName);
                            _out.append(", 'YYYYMMDDHH24MISS')");
                        }
                        _bindList.add(bindName, MocaType.STRING, 
                            new MocaValue(var.getType(), var.getValue()).asString());
                    }
                    else {
                        _out.append(':');
                        _out.append(bindName);
                        _bindList.add(bindName, var.getType(), value);
                    }
                }
            }
        }
    }
    
    private void _addValue(MocaValue var, String varName, String cast) {
        if (cast.equals("raw")) {
            _out.append((var == null || var.isNull()) ? "" : var.asString());
        }
        else {
            // First, check the type:
            if (var != null && (var.getType() == MocaType.OBJECT || var.getType() == MocaType.UNKNOWN)) {
                throw new MocaRuntimeException(new MocaArgumentException("Unknown parameter type: " + var.getType()));
            }
            
            String bindName;
            if (varName == null) {
                bindName = "var" + _bindCount++;
            }
            else {
                bindName = "var_" + varName;
            }
            
            if (cast.equals("date")) {
                if (var != null) {
                    _out.append("TO_DATE(:" + bindName + ", 'YYYYMMDDHH24MISS')");
                    _bindList.add(bindName, MocaType.STRING,
                        var.isNull() ? null : var.asString());
                }
                else {
                    _out.append(':');
                    _out.append(bindName);
                    _bindList.add(bindName, MocaType.DATETIME, null);
                }
            }
            else {
                _out.append(':');
                _out.append(bindName);
                // If the variable is null then we just bind it as a string null
                // value
                if (var == null) {
                    _bindList.add(bindName, MocaType.STRING, null);
                }
                // If the variable value is null then use the type specified
                else if (var.isNull()) {
                    _bindList.add(bindName, var.getType(), null);
                }
                // Else we bind the value
                else {
                    _bindList.add(bindName, var.getType(), var.getValue());
                }
            }
        }
    }
    
    private static enum ScanMode {
        DEFAULT, SINGLEQUOTE, DOUBLEQUOTE, COMMENT
    }
    
    private static enum ReplaceMode {
        DEFAULT, ENV, OPER, LIKE, IGNOREOPER
    }
    
    private static Pattern directivePattern = Pattern.compile("^([^:#^]*)\\^?([^:#^]*)\\#?([^:#^]*)\\:?([^:#^]*)$");
    private final String _in;
    private final StringBuilder _out;
    private final BindList _bindList;
    private int _bindCount;
}
