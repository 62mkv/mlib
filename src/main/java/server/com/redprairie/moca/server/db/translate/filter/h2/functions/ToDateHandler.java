/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.redprairie.moca.server.db.translate.filter.h2.functions;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.ReplacementElement;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TokenType;
import com.redprairie.moca.server.db.translate.TranslationException;
import com.redprairie.moca.server.db.translate.filter.functions.FunctionHandler;
import com.redprairie.moca.server.expression.function.OracleDBDateFormatToJava;

/**
 * A function handler designed to deal with the Oracle to_number function to
 * replace it with the h2 equivalent
 * 
 * <b><pre>
 * Copyright (c) 20167 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class ToDateHandler implements FunctionHandler {
    public List<SQLElement> translate(String name, List<List<SQLElement>> args, BindList bindArgs)
            throws TranslationException{
        
        int argCount = args.size();
        if (argCount != 1 && argCount != 2) {
            throw new TranslationException("TO_DATE requires 1 or 2 arguments, received " + argCount);
        }

        List<SQLElement> out = new ArrayList<SQLElement>();
        List<SQLElement> valueArg = args.get(0);

        // Pick out the format String
        String format = null;
                 
        out.add(new ReplacementElement(TokenType.WORD, "parsedatetime", " "));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.addAll(valueArg);
        
        if (argCount == 1) {
            String value = getArgValue(valueArg, bindArgs);
            if (value.length() == 8) format = "YYYYMMDD";
            else format = "YYYYMMDDHH24MISS";
        }
        else {
            format = getArgValue(args.get(1), bindArgs);
        }
        format = _formatter.apply(format);
        
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        out.add(new ReplacementElement(TokenType.STRING_LITERAL, "'" + format + "'", " "));
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));

        return out;
    }
    
    private String getArgValue(List<SQLElement> arg, BindList bindArgs)
            throws TranslationException {
        String argValue = null;
        if (arg.size() == 1) {
            SQLElement argElement = arg.get(0);
            if (argElement.getType() == TokenType.STRING_LITERAL) {
                argValue = argElement.getValue();
                argValue = argValue.substring(1, argValue.length() - 1);
            }
            else if (argElement.getType() == TokenType.BIND_VARIABLE) {
                String bindVar = argElement.getValue().substring(1);
                Object bindValue = bindArgs.getValue(bindVar);
                if (bindValue != null) {
                    if (bindValue instanceof String) {
                        argValue = (String)bindArgs.getValue(bindVar);
                    }
                    else if (bindValue instanceof Timestamp) {
                        argValue = new DateTime(((Timestamp) bindValue)
                                .getTime()).toString("YYYYMMddHHmmss");
                    }
                }
            }
            else if (argElement.getType() == TokenType.WORD &&
                argElement.getValue().equalsIgnoreCase("null")) {
                argValue = null; 
            }
            else if (argElement.getType() == TokenType.NSTRING_LITERAL) {
                argValue = argElement.getValue();
                argValue = argValue.substring(2, argValue.length() - 1);
            }
            else {
                throw new TranslationException(
                    "Expected literal or bind variable, received " + argElement);
            }
        }
        else {
            throw new TranslationException(
                "Expected literal or bind variable, received " + arg);
        }
        return argValue;
    }
    
    private static final OracleDBDateFormatToJava _formatter = 
            new OracleDBDateFormatToJava();
}
