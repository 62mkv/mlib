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

package com.sam.moca.server.db.translate.filter.mssql;

import java.util.ArrayList;
import java.util.List;

import com.sam.moca.server.db.BindList;
import com.sam.moca.server.db.translate.ReplacementElement;
import com.sam.moca.server.db.translate.SQLElement;
import com.sam.moca.server.db.translate.TokenType;
import com.sam.moca.server.db.translate.TranslationException;
import com.sam.moca.server.db.translate.TranslationOptions;
import com.sam.moca.server.db.translate.filter.TranslationFilter;

/**
 * Filter to translate an empty string to <code>null</code>.
 * 
 * <b><pre>
 * Copyright (c) 20167 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MSEmptyStringFilter implements TranslationFilter {
    public SQLElement[] filter(SQLElement[] input, BindList args,
            TranslationOptions options) throws TranslationException {
        List<SQLElement> output = new ArrayList<SQLElement>();
        
        for (int i = 0; i < input.length; i++) {
            SQLElement element = input[i];
            if ((element.getType() == TokenType.STRING_LITERAL && 
                    element.getValue().equals("''"))
                    || (element.getType() == TokenType.NSTRING_LITERAL && 
                            element.getValue().equals("N''"))) {
                output.add(new ReplacementElement(TokenType.WORD, "cast", element.getLeadingWhitespace()));
                output.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
                output.add(new ReplacementElement(TokenType.WORD, "null", ""));
                output.add(new ReplacementElement(TokenType.WORD, "as", " "));
                output.add(new ReplacementElement(TokenType.WORD, "nvarchar", " "));
                output.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
            }
            else {
                output.add(element);
            }
        }
        
        return output.toArray(new SQLElement[output.size()]);
    }
}
