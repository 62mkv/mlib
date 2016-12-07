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
 * A function handler designed to deal with the Oracle TRUNC function, and
 * replace it with a SQL CASE expression.
 * 
 * <b><pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TruncHandler implements FunctionHandler {
    public List<SQLElement> translate(String name, List<List<SQLElement>> args, BindList bindArgs)
            throws TranslationException{
        
        int argCount = args.size();
        if (argCount < 1 || argCount > 2) {
            throw new TranslationException(name + " requires 1 or 2 arguments, received " + argCount);
        }
        
        List<SQLElement> out = new ArrayList<SQLElement>();
        out.add(new ReplacementElement(TokenType.WORD, "round", " "));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        
        out.addAll(args.get(0));
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        if (argCount == 1) {
            out.add(new ReplacementElement(TokenType.INT_LITERAL, "0", " "));
        }
        else {
            out.addAll(args.get(1));
        }
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        out.add(new ReplacementElement(TokenType.INT_LITERAL, "1", " "));
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        
        return out;
    }
}
