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

package com.redprairie.moca.server.db.translate.filter.functions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.ReplacementElement;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TokenType;

/**
 * A function handler designed to deal with the Oracle Decode function, and
 * replace it with a SQL CASE expression.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class DecodeHandler implements FunctionHandler {
    public List<SQLElement> translate(String name, List<List<SQLElement>> args, BindList bindArgs) {
        boolean nullResultsOnly = true;
        int lastResultsPosition = 0;
        List<SQLElement> out = new ArrayList<SQLElement>();
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", " "));
        out.add(new ReplacementElement(TokenType.WORD, "case", ""));
        
        Iterator<List<SQLElement>> i = args.iterator();
        
        List<SQLElement> expr = i.next();
        while (i.hasNext()) {
            List<SQLElement> arg = i.next();
            if (i.hasNext()) {
                out.add(new ReplacementElement(TokenType.WORD, "when ", " "));
                out.addAll(expr);
                if (arg.size() == 1 && 
                        (arg.get(0).getValue().equalsIgnoreCase("null") ||
                         arg.get(0).getValue().equalsIgnoreCase("''"))) {
                    out.add(new ReplacementElement(TokenType.WORD, "is", " "));
                    out.add(new ReplacementElement(TokenType.WORD, "null", " "));
                    out.add(new ReplacementElement(TokenType.WORD, "then ", " "));
                }
                else {
                    out.add(new ReplacementElement(TokenType.EQ, "=", " "));
                    out.addAll(arg);
                    out.add(new ReplacementElement(TokenType.WORD, "then ", " "));
                }
                arg = i.next();
            }
            else {
                out.add(new ReplacementElement(TokenType.WORD, "else ", " "));
                
            }
            
            if (nullResultsOnly && arg.size() == 1 && arg.get(0).getValue().equalsIgnoreCase("null")) {
                lastResultsPosition = out.size();
            }
            else {
                nullResultsOnly = false;
            }
            out.addAll(arg);
        }
        
        out.add(new ReplacementElement(TokenType.WORD, "end", " "));
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        
        if (nullResultsOnly) {
            castFinalNull(lastResultsPosition, out);
        }
        
        return out;
    }

    protected void castFinalNull(int lastResultsPosition, List<SQLElement> out) {
        // Default implementation -- do nothing.
    }
    
}
