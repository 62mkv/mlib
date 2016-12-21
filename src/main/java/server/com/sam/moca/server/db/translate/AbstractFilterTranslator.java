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

package com.sam.moca.server.db.translate;

import com.sam.moca.server.db.BindList;
import com.sam.moca.server.db.DBType;
import com.sam.moca.server.db.translate.filter.TranslationFilter;
import com.sam.util.ArgCheck;

/**
 * Base class for all filter-type translators.  This includes all MOCA DB dialects that support autobind.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public abstract class AbstractFilterTranslator extends BaseDialect {

    /**
     * Constructor that takes a set of filters and a database type.
     */
    protected AbstractFilterTranslator(TranslationFilter[] filters,
                                       TranslationFilter[] noconvFilters,
                                       DBType dbType) {
        super(dbType);
        _filters = filters;
        _noconvFilters = noconvFilters;
    }

    @Override
    public String translateStatement(String sql, BindList args, TranslationOptions options)
            throws TranslationException {
        ArgCheck.notNull(sql, "sql must not be null");
        ArgCheck.notNull(options, "options must not be null");
        
        SQLElement first = new SQLTokenizer(sql).nextToken();
        
        TranslationFilter[] filters;
        
        // Skip processing if the NOCONV indicator is the first token
        if (first.getType() == TokenType.COMMENT &&
            first.getValue().equals(NOCONV_INDICATOR)) {
            sql = sql.substring(first.toString().length());
            filters = _noconvFilters;
        }
        else {
            filters = _filters;
        }
        
        if (filters == null) {
            return sql;
        }
        
        SQLTokenizer tokenizer = new SQLTokenizer(sql);
        SQLElement[] tokens = tokenizer.getAllTokens();

        // Go through the list of filters
        for (TranslationFilter f : filters) {
            tokens = f.filter(tokens, args, options);
        }
        
        return SQLTokenizer.getString(tokens);
    }
    
    //
    // Implementation
    //
    private TranslationFilter[] _filters;
    private TranslationFilter[] _noconvFilters;
}