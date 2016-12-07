/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TokenType;
import com.redprairie.moca.server.db.translate.TranslationException;
import com.redprairie.moca.server.db.translate.TranslationOptions;

/**
 * This class is to make sure when a no bind hint is found that bound variables
 * are not bound.
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class NoBindFilter extends AbstractUnbindFilter {

    /**
     * @param useNStrings
     */
    public NoBindFilter(boolean useNStrings) {
        super(useNStrings);
    }
    
    // @see com.redprairie.moca.server.db.translate.filter.AbstractUnbindFilter#filter(com.redprairie.moca.server.db.translate.SQLElement[], com.redprairie.moca.server.db.BindList, com.redprairie.moca.server.db.translate.TranslationOptions)
    @Override
    public SQLElement[] filter(SQLElement[] input, BindList args,
        TranslationOptions options) throws TranslationException {
        options.addHint("varbind=true");
        return super.filter(input, args, options);
    }

    @Override
    protected void filterCurrentElement(SQLElement[] input, int pos, 
        TranslationOptions options, BindList bindList) {
        SQLElement element = input[pos];
        
        if (element.getType() == TokenType.COMMENT) {
            if (element.getValue().equalsIgnoreCase("/*#nobind*/")) {
                options.addHint("varbind=false");
            }
            else if (element.getValue().equalsIgnoreCase("/*#bind*/")) {
                options.addHint("varbind=true");
            }
        }
        
        // If not binding then do the filter which will unbind them
        if (!Boolean.valueOf(options.getHintValue("varbind"))) {
            super.filterCurrentElement(input, pos, options, bindList);
        }
    }
}
