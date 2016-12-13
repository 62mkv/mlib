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
 * A function handler designed to deal with the CHR function, 
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
public class ChrHandler implements FunctionHandler {
    public List<SQLElement> translate(String name, List<List<SQLElement>> args, BindList bindArgs)
            throws TranslationException{
        
        int argCount = args.size();
        if (argCount != 1) {
            throw new TranslationException("CHR requires 1 arguments, received " + argCount);
        }
        
        List<SQLElement> arg1 = args.get(0);

        List<SQLElement> out = new ArrayList<SQLElement>();
        
        // (cast(char(arg) as varchar))
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.WORD, "cast", ""));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.WORD, "char", ""));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.addAll(arg1);
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        out.add(new ReplacementElement(TokenType.WORD, "as", " "));
        out.add(new ReplacementElement(TokenType.WORD, "varchar", " "));
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));

        return out;
    }
}
