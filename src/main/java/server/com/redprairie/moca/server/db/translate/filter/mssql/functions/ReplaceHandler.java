/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2006
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
 * A function handler designed to deal with the Oracle REPLACE function, and
 * replace it with a SQL Server equivalent expression.
 * 
 * <b><pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class ReplaceHandler implements FunctionHandler {
    public List<SQLElement> translate(String name, List<List<SQLElement>> args, BindList bindArgs)
            throws TranslationException{
        
        int argCount = args.size();
        if (argCount != 2 && argCount != 3) {
            throw new TranslationException("REPLACE requires 2 or 3 arguments, received " + argCount);
        }
        
        List<SQLElement> out = new ArrayList<SQLElement>();
        out.add(new ReplacementElement(TokenType.WORD, name, " "));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        
        out.add(new ReplacementElement(TokenType.WORD, "convert", ""));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.WORD, "varchar", ""));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.INT_LITERAL, "8000", ""));
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        out.addAll(args.get(0));
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        out.add(new ReplacementElement(TokenType.WORD, "convert", ""));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.WORD, "varchar", ""));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.INT_LITERAL, "8000", ""));
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        out.addAll(args.get(1));
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));

        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        if (argCount == 3) {
            out.add(new ReplacementElement(TokenType.WORD, "isnull", ""));
            out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
            out.add(new ReplacementElement(TokenType.WORD, "convert", ""));
            out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
            out.add(new ReplacementElement(TokenType.WORD, "varchar", ""));
            out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
            out.add(new ReplacementElement(TokenType.INT_LITERAL, "8000", ""));
            out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
            out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
            out.addAll(args.get(2));
            out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
            out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
            out.add(new ReplacementElement(TokenType.OTHER, "N''", " "));
            out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        }
        else {
            out.add(new ReplacementElement(TokenType.OTHER, "N''", " "));
        }
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        
        return out;
    }
}
