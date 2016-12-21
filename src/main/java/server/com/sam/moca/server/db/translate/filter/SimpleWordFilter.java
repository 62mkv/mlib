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

package com.sam.moca.server.db.translate.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sam.moca.server.db.BindList;
import com.sam.moca.server.db.translate.ReplacementElement;
import com.sam.moca.server.db.translate.SQLElement;
import com.sam.moca.server.db.translate.TokenType;
import com.sam.moca.server.db.translate.TranslationException;
import com.sam.moca.server.db.translate.TranslationOptions;

/**
 * Filter to to handle simple word replacement for SQL functions that have
 * direct equivalents in the target dialect.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class SimpleWordFilter implements TranslationFilter {
    public SimpleWordFilter(Map<String, String> translations) {
        _translations = translations;
    }
    
    public SQLElement[] filter(SQLElement[] input, BindList args,
            TranslationOptions options) throws TranslationException {
        List<SQLElement> output = new ArrayList<SQLElement>();
        
        for (int i = 0; i < input.length; i++) {
            SQLElement element = input[i];
            if (element.getType() == TokenType.WORD) {
                String translated = _translations.get(element.getValue().toLowerCase());
                if (translated != null) {
                    element = new ReplacementElement(TokenType.WORD,
                            translated, element.getLeadingWhitespace());
                }
            }
            output.add(element);
        }
        
        return output.toArray(new SQLElement[output.size()]);
    }
    
    private final Map<String, String> _translations;
}
