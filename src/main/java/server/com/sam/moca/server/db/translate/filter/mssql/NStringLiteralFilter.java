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

import com.sam.moca.server.db.BindList;
import com.sam.moca.server.db.translate.ReplacementElement;
import com.sam.moca.server.db.translate.SQLElement;
import com.sam.moca.server.db.translate.TokenType;
import com.sam.moca.server.db.translate.TranslationOptions;
import com.sam.moca.server.db.translate.filter.UnicodeEnabledTranslationFilter;

/**
 * Filter to translate VARCHAR literals of the form ('VALUE') into NVARCHAR
 * literals of the form (N'VALUE').  Certain unicode characters cannot appear
 * in varchar literals, so, for safety, we translate all literals into
 * nvarchars.
 * 
 * <b><pre>
 * Copyright (c) 20167-2009 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class NStringLiteralFilter extends UnicodeEnabledTranslationFilter {

    @Override
    public void filterCurrentElement(SQLElement[] input, int pos,
            TranslationOptions options, BindList bindList) {
        if (input[pos].getType() == TokenType.STRING_LITERAL) {
            String whitespace = input[pos].getLeadingWhitespace().isEmpty() ? " " : input[pos].getLeadingWhitespace();
            input[pos] = new ReplacementElement(TokenType.NSTRING_LITERAL, "N" + input[pos].getValue(), whitespace);
        }
    }
}
