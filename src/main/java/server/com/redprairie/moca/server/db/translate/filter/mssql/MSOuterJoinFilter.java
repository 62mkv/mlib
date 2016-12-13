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

package com.redprairie.moca.server.db.translate.filter.mssql;

import java.util.ArrayList;
import java.util.List;

import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.ReplacementElement;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TokenType;
import com.redprairie.moca.server.db.translate.TranslationException;
import com.redprairie.moca.server.db.translate.TranslationOptions;
import com.redprairie.moca.server.db.translate.filter.TranslationFilter;

/**
 * Filter to to handle Oracle legacy outer join syntax and convert it
 * to legacy SQL Server outer join syntax.
 * 
 * <b><pre>
 * Copyright (c) 20167 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MSOuterJoinFilter implements TranslationFilter {
    public SQLElement[] filter(SQLElement[] input, BindList args,
                               TranslationOptions options)
            throws TranslationException {
        List<SQLElement> output = new ArrayList<SQLElement>();
        boolean isOuterJoin = false;
        int eqPos = -1;
        int eqLevel = 0;
        int parenLevel = 0;

        for (int i = 0; i < input.length; i++) {
            SQLElement element = input[i];

            // If we see an equals operator, check to see if we've already
            // seen an outer join operator.
            if (element.getType() == TokenType.EQ) {
                if (isOuterJoin) {
                    element = new ReplacementElement(TokenType.OTHER, "=*", element.getLeadingWhitespace());
                    isOuterJoin = false;
                }
                else {
                    eqPos = output.size();
                    eqLevel = parenLevel;
                }
            }
            else if (element.getType() == TokenType.LEFTPAREN &&
                     i < (input.length - 2) &&
                     input[i+1].getType() == TokenType.PLUS &&
                     input[i+2].getType() == TokenType.RIGHTPAREN)
            {
                i += 2;
                isOuterJoin = true;
                element = null;
            }
            else if (element.getType() == TokenType.LEFTPAREN) {
                parenLevel++;
            }
            else if (element.getType() == TokenType.RIGHTPAREN) {
                parenLevel--;
            }

            // Add the current element.
            if (element != null) output.add(element);

            // We've hit a condition that causes us to resolve an
            // outstanding outerjoin condition.  This can happen for
            // a number of reasons, but we will look back for an equals
            // operator to adjust.
            if (isOuterJoin && element != null &&
                (parenLevel < eqLevel ||
                 element.getType() == TokenType.END ||
                  element.getType() == TokenType.SEMICOLON ||
                  (element.getType() == TokenType.WORD &&
                   (element.getValue().equalsIgnoreCase("and") ||
                    element.getValue().equalsIgnoreCase("or"))))) {
                
                // If we run into a situation where there's no equals
                // operator, let's treat it as an error.
                if (eqPos < 0) {
                    throw new TranslationException("Unable to find = for outer join operation");
                }

                // Replace the equals operator with something else.
                output.set(eqPos, new ReplacementElement(TokenType.OTHER, "*=", " "));
                
                isOuterJoin = false;
                eqLevel = 0;
                eqPos = -1;
            }
        }

        if (isOuterJoin && eqPos >= 0) {
            output.set(eqPos, new ReplacementElement(TokenType.OTHER, "*=", " "));
        }

        return output.toArray(new SQLElement[output.size()]);
    }
    
}
