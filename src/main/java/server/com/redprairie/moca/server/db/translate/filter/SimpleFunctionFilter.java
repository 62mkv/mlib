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

package com.redprairie.moca.server.db.translate.filter;

import java.util.Map;

import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.ReplacementElement;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TokenType;
import com.redprairie.moca.server.db.translate.TranslationException;
import com.redprairie.moca.server.db.translate.TranslationOptions;

/**
 * Filter to to handle simple word replacement for SQL functions that have
 * direct equivalents in the target dialect. This differs from SimpleWordFilter
 * in that it checks to make sure that the token following the word is a left
 * parenthesis.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class SimpleFunctionFilter implements TranslationFilter {
    public SimpleFunctionFilter(Map<String, String> translations) {
        _translations = translations;
    }
    
    public SQLElement[] filter(SQLElement[] input, BindList args,
            TranslationOptions options) throws TranslationException {
        
        int wordFound = -1;
        // Only go to the next-to-last input token, since we're looking for two tokens 
        for (int i = 0; i < input.length; i++) {
            SQLElement word = input[i];
            // If we had a word previously and we found a left parenthesis
            // then try to replace it
            if (wordFound > 0 && word.getType() == TokenType.LEFTPAREN) {
                SQLElement previousWord = input[wordFound];
                String translated = _translations.get(previousWord.getValue().toLowerCase());
                if (translated != null) {
                    input[wordFound] = new ReplacementElement(TokenType.WORD,
                            translated, previousWord.getLeadingWhitespace());
                }
            }
            // If we found a word mark its place
            else if (word.getType() == TokenType.WORD) {
                wordFound = i;
            }
            // On anything but a comment then we reset
            else if (word.getType() != TokenType.COMMENT) {
                wordFound = -1;
            }
        }
        
        return input;
    }
    
    private final Map<String, String> _translations;
}
