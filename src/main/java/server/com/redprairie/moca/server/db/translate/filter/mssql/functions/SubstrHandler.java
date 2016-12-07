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
 * A function handler designed to deal with the Oracle SUBSTR function, and
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
public class SubstrHandler implements FunctionHandler {
    public List<SQLElement> translate(String name, List<List<SQLElement>> args, BindList bindArgs)
            throws TranslationException{
        
        int argCount = args.size();
        if (argCount != 2 && argCount != 3) {
            throw new TranslationException("SUBSTR requires 2 or 3 arguments, received " + argCount);
        }
        List<SQLElement> s = args.get(0);
        List<SQLElement> start = args.get(1);
        List<SQLElement> len = null;
        if (argCount == 3) {
            len = args.get(2);
        }
        
        List<SQLElement> out = new ArrayList<SQLElement>();

        // the original statement should be like :
        // s, start [, len]
        // where s is the string, start is the starting position of the
        // substring, and len is the length of the final string.  
        //  The result of the conversion is to build a string 
        //  like:
        //  SUBSTRING(CONVERT(NVARCHAR, s), start, len)
                 
        out.add(new ReplacementElement("SUBSTRING(CONVERT(NVARCHAR(4000),"));
        out.addAll(s);
        out.add(new ReplacementElement(TokenType.OTHER, "), ", ""));
        out.addAll(start);
        out.add(new ReplacementElement(TokenType.OTHER, ",", ""));
        if (len != null) {
            out.addAll(len);
        }
        else {
            out.add(new ReplacementElement("LEN(CONVERT(NVARCHAR(4000),"));
            out.addAll(s);
            out.add(new ReplacementElement(TokenType.OTHER, "))", ""));
        }
        out.add(new ReplacementElement(TokenType.OTHER, ")", ""));
        
        return out;
    }
}
