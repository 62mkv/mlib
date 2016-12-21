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

package com.sam.moca.server.db.translate.filter.mssql.functions;

import java.util.ArrayList;
import java.util.List;

import com.sam.moca.server.db.BindList;
import com.sam.moca.server.db.translate.ReplacementElement;
import com.sam.moca.server.db.translate.SQLElement;
import com.sam.moca.server.db.translate.TokenType;
import com.sam.moca.server.db.translate.TranslationException;
import com.sam.moca.server.db.translate.filter.functions.FunctionHandler;

/**
 * A function handler designed to deal with the Oracle INSTR function, and
 * replace it with a SQL Server equivalent expression.
 * 
 * <b><pre>
 * Copyright (c) 20167 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class InstrHandler implements FunctionHandler {
    public List<SQLElement> translate(String name, List<List<SQLElement>> args, BindList bindArgs)
            throws TranslationException{
        
        int argCount = args.size();
        if (argCount != 2 && argCount != 3) {
            throw new TranslationException("INSTR requires 2 or 3 arguments, received " + argCount);
        }

        List<SQLElement> out = new ArrayList<SQLElement>();

        // the original statement should be like :
        // s1, s2[, start]
        // where s1 is the string, s2 is the string to search for,
        // and start is the starting position of the search.  
        //  The result of the conversion is to build a string 
        //  like:
        //  CHARINDEX(CONVERT(VARCHAR, s2), CONVERT(VARCHAR, s1)[, start])
                 
        out.add(new ReplacementElement(TokenType.WORD, "CHARINDEX", " "));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        
        out.add(new ReplacementElement(TokenType.WORD, "CONVERT", ""));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.WORD, "VARCHAR", ""));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.INT_LITERAL, "8000", ""));
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        out.addAll(args.get(1));
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));

        out.add(new ReplacementElement(TokenType.WORD, "CONVERT", ""));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.WORD, "VARCHAR", ""));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.INT_LITERAL, "8000", ""));
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        out.addAll(args.get(0));
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));

        if (argCount == 3) {
            out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
            out.addAll(args.get(2));
        }
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));

        return out;
    }
}
