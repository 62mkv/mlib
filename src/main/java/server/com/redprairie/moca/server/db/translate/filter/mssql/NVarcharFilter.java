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

package com.redprairie.moca.server.db.translate.filter.mssql;

import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.ReplacementElement;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TokenType;
import com.redprairie.moca.server.db.translate.TranslationOptions;
import com.redprairie.moca.server.db.translate.filter.UnicodeEnabledTranslationFilter;

/**
 * Filter to translate the varchar data type to nvarchar.  This is mostly for
 * CAST and CONVERT functions.
 * 
 * <b><pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class NVarcharFilter extends UnicodeEnabledTranslationFilter {

    @Override
    public void filterCurrentElement(SQLElement[] input, int pos, 
            TranslationOptions options, BindList bindList) {
        if (input[pos].getType() == TokenType.WORD && 
                input[pos].getValue().equalsIgnoreCase("varchar")) {
            input[pos] = new ReplacementElement(TokenType.WORD, "nvarchar", input[pos].getLeadingWhitespace());
            
            // Deal with varchar(#)
            if (pos + 3 < input.length &&
                    input[pos+1].getType() == TokenType.LEFTPAREN &&
                    input[pos+3].getType() == TokenType.RIGHTPAREN) {
                
                // The max size we can use for nvarchar is 4000, so scale
                // bigger varchars back.
                if (input[pos+2].getType() == TokenType.INT_LITERAL) {
                    int value = Integer.parseInt(input[pos+2].getValue());
                    if (value > 4000) {
                        input[pos+2] = new ReplacementElement(TokenType.INT_LITERAL, "4000", input[pos].getLeadingWhitespace());
                    }
                }
            }
        }
    }
}
