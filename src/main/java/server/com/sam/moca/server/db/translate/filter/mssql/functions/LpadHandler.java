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
 * A function handler designed to deal with the Oracle LPAD function, and
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
public class LpadHandler implements FunctionHandler {
    public List<SQLElement> translate(String name, List<List<SQLElement>> args, BindList bindArgs)
            throws TranslationException{
        
        int argCount = args.size();
        if (argCount != 2 && argCount != 3) {
            throw new TranslationException("LPAD requires 2 or 3 arguments, received " + argCount);
        }
        List<SQLElement> s1 = args.get(0);
        List<SQLElement> n = args.get(1);
        List<SQLElement> s2;
        if (argCount == 2) {
            s2 = new ArrayList<SQLElement>();
            s2.add(new ReplacementElement(TokenType.STRING_LITERAL, "' '", ""));
        }
        else {
            s2 = args.get(2);
        }
        
        
        
        List<SQLElement> out = new ArrayList<SQLElement>();

        // The original statement should be like :
        //  s1, n, s2
        //  where s1 is the string, n is the total length of
        //  the final string and s2 is what to pad.  
        //
        // if the length of the s1 is greater than n, then
        // no padding will added, and s1 will be trimmed to length of zero.
        //
        // Note: this function rely on the fact that if a negative number is
        // given to the replicate function, null is returned.
        //
        // The result of the conversion is to build a string 
        // like:
        // LEFT(REPLICATE(CONVERT(NVARCHAR, s2), n), 
        //     CASE SIGN(n-LEN(CONVERT(NVARCHAR,s1)))
        //     WHEN 1 THEN n-LEN(CONVERT(NVARCHAR, s1))
        //   ELSE 0) + LEFT(CONVERT(NVARCHAR, s1), n)
                 
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", " "));

        out.add(new ReplacementElement(TokenType.OTHER, "LEFT(REPLICATE(CONVERT(NVARCHAR(4000),", ""));
        out.addAll(s2);
        out.add(new ReplacementElement("),"));
        out.addAll(n);
        out.add(new ReplacementElement("),"));
        out.add(new ReplacementElement("CASE SIGN("));
        out.addAll(n);
        out.add(new ReplacementElement("-LEN(CONVERT(NVARCHAR(4000),"));
        out.addAll(s1);
        out.add(new ReplacementElement("))) WHEN 1 THEN "));
        out.addAll(n);
        out.add(new ReplacementElement("-LEN(CONVERT(NVARCHAR(4000), "));
        out.addAll(s1);
        out.add(new ReplacementElement(")) ELSE 0 END) + LEFT (CONVERT(NVARCHAR(4000), "));
        out.addAll(s1);
        out.add(new ReplacementElement("),"));
        out.addAll(n);
        out.add(new ReplacementElement(")"));

        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        
        return out;
    }
}
