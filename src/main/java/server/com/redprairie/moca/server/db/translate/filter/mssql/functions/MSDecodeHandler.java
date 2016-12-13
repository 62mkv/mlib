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

package com.redprairie.moca.server.db.translate.filter.mssql.functions;

import java.util.Arrays;
import java.util.List;

import com.redprairie.moca.server.db.translate.ReplacementElement;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TokenType;
import com.redprairie.moca.server.db.translate.filter.functions.DecodeHandler;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class MSDecodeHandler extends DecodeHandler {

    protected void castFinalNull(int lastResultsPosition, List<SQLElement> out) {
        out.remove(lastResultsPosition);
        out.addAll(lastResultsPosition, Arrays.asList(
            new ReplacementElement(TokenType.WORD, "cast", " "),
            new ReplacementElement(TokenType.LEFTPAREN, "(", ""),
            new ReplacementElement(TokenType.WORD, "null", ""),
            new ReplacementElement(TokenType.WORD, "as", " "),
            new ReplacementElement(TokenType.WORD, "varchar", " "),
            new ReplacementElement(TokenType.RIGHTPAREN, ") ", ""))
        );
    }

}
