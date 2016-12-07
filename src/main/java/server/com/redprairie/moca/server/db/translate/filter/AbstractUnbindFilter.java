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

package com.redprairie.moca.server.db.translate.filter;

import java.util.Date;

import com.redprairie.moca.MocaType;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.ReplacementElement;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TokenType;
import com.redprairie.moca.server.db.translate.TranslationOptions;
import com.redprairie.moca.util.MocaUtils;

/**
 * Filter to automatically insert bind variables in a SQL statement where string
 * or numeric constants had been. The default behavior is to bind variables
 * until "order by" is seen, then stop. This will avoid binding numeric
 * parameters when <code>order by 2, 3</code> is seen, which in normal SQL
 * syntax, means to order by column numbers 2 and 3. Some SQL engines do not
 * allow parameters in that context.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public abstract class AbstractUnbindFilter extends UnicodeEnabledTranslationFilter {

    /**
     * Default constructor.
     */
    public AbstractUnbindFilter(boolean useNStrings) {
        super(useNStrings, true);
    }

    @Override
    protected void filterCurrentElement(SQLElement[] input, int pos, 
        TranslationOptions options, BindList bindList) {
        SQLElement element = input[pos];
        TokenType elementType = element.getType();
        boolean unicodeEnabled = hasUnicode(options);
        if (elementType == TokenType.BIND_VARIABLE) {
            TokenType stringTok = unicodeEnabled ? TokenType.NSTRING_LITERAL
                    : TokenType.STRING_LITERAL;
            String argName = element.getValue().substring(1);

            // If the argument is not in the list, leave it alone. Sometimes we
            // see a bind
            // variable where there isn't really one.
            if (bindList.contains(argName)) {
                MocaType argType = bindList.getType(argName);
                Object value = bindList.getValue(argName);

                // In the case of a null value, we use the word NULL, but only
                // if the type
                // is one that we recognize.
                if (value == null
                        && (argType == MocaType.DOUBLE
                                || argType == MocaType.INTEGER
                                || argType == MocaType.STRING || argType == MocaType.DATETIME)) {
                    element = new ReplacementElement(TokenType.WORD, "NULL",
                            element.getLeadingWhitespace());
                }
                else {
                    switch (argType) {
                    case DOUBLE:
                        element = new ReplacementElement(
                                TokenType.FLOAT_LITERAL, String.valueOf(value),
                                element.getLeadingWhitespace());
                        break;
                    case INTEGER:
                        element = new ReplacementElement(TokenType.INT_LITERAL,
                                String.valueOf(value), element
                                        .getLeadingWhitespace());
                        break;
                    case STRING:
                        element = new ReplacementElement(stringTok, requote(
                                String.valueOf(value), unicodeEnabled), element
                                .getLeadingWhitespace());
                        break;
                    case DATETIME:
                        String dateString;
                        if (value instanceof Date) {
                            dateString = MocaUtils.formatDate((Date) value);
                        }
                        else {
                            dateString = String.valueOf(value);
                        }
                        element = new ReplacementElement(stringTok, requote(
                                dateString, unicodeEnabled), element
                                .getLeadingWhitespace());
                        break;
                    default:
                        // For all other types, leave the bind variable as-is.
                        // This will catch the cases
                        // of bind-by-reference and other situations, even
                        // though those shouldn't ever be
                        // seen from this code.
                        break;
                    }
                }
            }
        }
        input[pos] = element;
    }

    private static String requote(String in, boolean useNStrings) {
        StringBuilder out = new StringBuilder(in.length());

        if (useNStrings) {
            out.append('N');
        }
        out.append('\'');
        int inLength = in.length();
        for (int i = 0; i < inLength; i++) {
            char inChar = in.charAt(i);
            out.append(inChar);
            if (inChar == '\'') {
                out.append('\'');
            }
        }
        out.append('\'');
        return out.toString();
    }
}
