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

import com.sam.moca.server.db.BindList;
import com.sam.moca.server.db.translate.SQLElement;
import com.sam.moca.server.db.translate.TranslationException;
import com.sam.moca.server.db.translate.TranslationOptions;

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
 * Copyright (c) 20167 Sam Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class UnbindFilter extends AbstractUnbindFilter {

    /**
     * Default constructor.
     */
    public UnbindFilter(boolean useNStrings) {
        super(useNStrings);
    }
    
    // @see com.sam.moca.server.db.translate.filter.mssql.UnicodeEnabledTranslationFilter#filter(com.sam.moca.server.db.translate.SQLElement[], com.sam.moca.server.db.BindList, com.sam.moca.server.db.translate.TranslationOptions)
    public SQLElement[] filter(SQLElement[] input, BindList args,
                               TranslationOptions options)
            throws TranslationException {
        // If the options aren't even enabled don't worry
        if (!options.doUnbind()) {
            return input;
        }
        
        return super.filter(input, args, options);
    }
}
