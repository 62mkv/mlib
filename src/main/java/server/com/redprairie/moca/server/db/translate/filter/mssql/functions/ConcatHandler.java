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
 * A function handler designed to deal with the CONCAT function, 
 * and replace it with an appropriate SQL Server construct.
 * 
 * <b><pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class ConcatHandler implements FunctionHandler {
    public List<SQLElement> translate(String name, List<List<SQLElement>> args, BindList bindArgs)
            throws TranslationException{
        
        int argCount = args.size();
        if (argCount != 2) {
            throw new TranslationException("CONCAT requires 2 arguments, received " + argCount);
        }
        
        List<SQLElement> arg1 = args.get(0);
        List<SQLElement> arg2 = args.get(1);

        List<SQLElement> out = new ArrayList<SQLElement>();
        
        // (CONVERT(VARCHAR(8000), arg1) + CONVERT(VARCHAR(8000),arg2))
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));

        out.add(new ReplacementElement(TokenType.WORD, "convert", ""));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.WORD, "varchar", ""));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.INT_LITERAL, "8000", ""));
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        out.addAll(arg1);
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));

        out.add(new ReplacementElement(TokenType.PLUS, "+", " "));
        
        out.add(new ReplacementElement(TokenType.WORD, "convert", " "));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.WORD, "varchar", ""));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.INT_LITERAL, "8000", ""));
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        out.addAll(arg2);
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));

        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));

        return out;
    }
}
