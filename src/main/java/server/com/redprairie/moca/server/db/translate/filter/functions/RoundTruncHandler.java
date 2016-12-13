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
import java.util.List;

import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.ReplacementElement;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TokenType;
import com.redprairie.moca.server.db.translate.TranslationException;

/**
 * A function handler designed to deal with the ROUND or TRUNC Decode function, and
 * add a final parameter (implied on Oracle) to indicate the scope of the rounding.
 * 
 * <b><pre>
 * Copyright (c) 20167 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class RoundTruncHandler implements FunctionHandler {
    public List<SQLElement> translate(String name, List<List<SQLElement>> args, BindList bindArgs)
            throws TranslationException{
        
        int argCount = args.size();
        if (argCount < 1 || argCount > 3) {
            throw new TranslationException(name + " requires 1, 2 or 3 arguments, received " + argCount);
        }
        
        List<SQLElement> out = new ArrayList<SQLElement>();
        out.add(new ReplacementElement(TokenType.WORD, name, " "));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        
        out.addAll(args.get(0));
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        if (argCount == 1) {
            out.add(new ReplacementElement(TokenType.INT_LITERAL, "0", " "));
        }
        else {
            out.addAll(args.get(1));
            if (argCount == 3) {
                out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
                out.addAll(args.get(2));
            }
        }
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        
        return out;
    }
}
