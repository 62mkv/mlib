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

package com.redprairie.moca.server.db.translate.filter.mssql.functions;

import java.util.ArrayList;
import java.util.List;

import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.ReplacementElement;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TokenType;
import com.redprairie.moca.server.db.translate.TranslationException;
import com.redprairie.moca.server.db.translate.filter.functions.FunctionHandler;

/**
 * A function handler designed to deal with the MOCA DATE_DIFF_DAYS function,
 * and replace it with an appropriate SQL Server construct.
 * 
 * <b><pre>
 * Copyright (c) 20167 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class DateDiffDaysHandler implements FunctionHandler {
    public List<SQLElement> translate(String name, List<List<SQLElement>> args, BindList bindArgs)
            throws TranslationException{
        
        int argCount = args.size();
        if (argCount != 2) {
            throw new TranslationException("DATE_DIFF_DAYS requires 2 arguments, received " + argCount);
        }
        
        List<SQLElement> arg1 = args.get(0);
        List<SQLElement> arg2 = args.get(1);

        List<SQLElement> out = new ArrayList<SQLElement>();
        
        /*
         * The following is a little convoluted, but it's required in order
         * to support date ranges that vary by more than 68 yeras, which will
         * cause an overflow if done via a simple datediff( ) because we require
         * "second" granularity to avoid rounding issues.
         *
         *     (
         *       datediff(dd, arg1, arg2) 
         *       + 
         *       cast(datediff(ss, 
         *                      dateadd(dd, 
         *                              datediff(dd, arg1, arg2), 
         *                              arg1), 
         *             arg2) as float) 
         *       / 86400.0
         *     )
         */
        
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", " "));
        out.add(new ReplacementElement(TokenType.WORD, "datediff", ""));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.WORD, "dd", ""));
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        out.addAll(arg1);
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        out.addAll(arg2);
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        out.add(new ReplacementElement(TokenType.PLUS, "+", ""));
        out.add(new ReplacementElement(TokenType.WORD, "cast", " "));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.WORD, "datediff", ""));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.WORD, "ss", ""));
        out.add(new ReplacementElement(TokenType.COMMA, ",", "")); 
        out.add(new ReplacementElement(TokenType.WORD, "dateadd", ""));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.WORD, "dd", ""));
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        out.add(new ReplacementElement(TokenType.WORD, "datediff", ""));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.WORD, "dd", ""));
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        out.addAll(arg1);
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        out.addAll(arg2);
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        out.addAll(arg1);
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        out.addAll(arg2);
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));  
        out.add(new ReplacementElement(TokenType.WORD, "as", " "));
        out.add(new ReplacementElement(TokenType.WORD, "float", " "));
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        out.add(new ReplacementElement(TokenType.SLASH, "/", ""));
        out.add(new ReplacementElement(TokenType.FLOAT_LITERAL, "86400.0", ""));
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        
        return out;
    }
}
