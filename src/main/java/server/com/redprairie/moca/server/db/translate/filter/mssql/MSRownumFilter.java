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

import java.util.List;

import com.redprairie.moca.server.db.translate.ReplacementElement;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TokenType;
import com.redprairie.moca.server.db.translate.filter.RownumFilter;

/**
 * SQL Translation filter for translating rownum expressions in Oracle SQL where
 * clauses into "TOP *" clauses in SQL Server statements.
 * 
 * <b><pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MSRownumFilter extends RownumFilter {
    
    public MSRownumFilter() {
        super();
    }

    @Override
    protected void _addRownumClause(List<SQLElement> output, long rownumValue, int selectElement) {
        // We keep incrementing the select location right away since we don't
        // want to stick it right after the select but make sure it is
        // after the distinct
        while (++selectElement < output.size()) {
            SQLElement element = output.get(selectElement);
            // We continue until we find a non comment element
            if (element.getType() != TokenType.COMMENT) {
                // If the non comment is a word that is distinct than we have
                // to move the top over 1 spot
                if (element.getType() == TokenType.WORD && 
                        element.getValue().equalsIgnoreCase("distinct")) {
                    selectElement++;
                }
                output.add(selectElement, new ReplacementElement("TOP " + rownumValue));
                break;
            }
        }
    }
}

