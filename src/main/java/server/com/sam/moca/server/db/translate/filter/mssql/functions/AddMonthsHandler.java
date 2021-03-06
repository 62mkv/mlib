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
 * A function handler designed to deal with the Oracle ADD_MONTHS function, and
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
public class AddMonthsHandler implements FunctionHandler {
    public List<SQLElement> translate(String name, List<List<SQLElement>> args, BindList bindArgs)
            throws TranslationException{
        
        int argCount = args.size();
        if (argCount != 2) {
            throw new TranslationException("ADD_MONTHS requires 2 arguments, received " + argCount);
        }

        List<SQLElement> out = new ArrayList<SQLElement>();

        // the original statement should be like :
        // d, n
        // where d is the beginning date and n is the number of months to add
        //  The result of the conversion is to build a string 
        //  like:
        //  DATEADD(MONTH, n, d)
                 
        out.add(new ReplacementElement(TokenType.WORD, "DATEADD", " "));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        
        out.add(new ReplacementElement(TokenType.WORD, "MONTH", ""));
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        out.addAll(args.get(1));
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        out.addAll(args.get(0));
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));

        return out;
    }
}
