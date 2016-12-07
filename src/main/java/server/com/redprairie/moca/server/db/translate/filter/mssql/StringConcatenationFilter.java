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
import com.redprairie.moca.server.db.translate.TranslationException;
import com.redprairie.moca.server.db.translate.TranslationOptions;
import com.redprairie.moca.server.db.translate.filter.TranslationFilter;

/**
 * Filter to translate the string concatenation operator for Oracle (||) to the
 * appropriate one for SQL Server (+)
 * 
 * <b><pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class StringConcatenationFilter implements TranslationFilter {
    public SQLElement[] filter(SQLElement[] input, BindList args,
            TranslationOptions options) throws TranslationException {
        
        for (int i = 0; i < input.length; i++) {
            if (input[i].getType() == TokenType.CONCAT) {
                input[i] = new ReplacementElement(TokenType.PLUS, "+", input[i].getLeadingWhitespace());
            }
        }
        
        return input;
    }
}
