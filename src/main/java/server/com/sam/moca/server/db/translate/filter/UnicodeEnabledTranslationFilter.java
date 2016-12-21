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
import com.sam.moca.server.db.translate.TokenType;
import com.sam.moca.server.db.translate.TranslationException;
import com.sam.moca.server.db.translate.TranslationOptions;

/**
 * This class is to be extended by filters to provide the default behavior
 * so that when a no unicode hint is found that it won't call the filter for
 * each of the sql elements.  This can also be extended so that each element
 * is still passed in except comments so that non unicode operations can be
 * handled as well.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public abstract class UnicodeEnabledTranslationFilter implements TranslationFilter {
    /**
     * This will set the unicode enabled to true so each element will be called
     * unless a hint is found.  Also the call always is set to false so that
     * a hint can properly disbale a filter call.
     */
    public UnicodeEnabledTranslationFilter() {
        this(true, false);
    }
    
    /**
     * @param enabled This will set the default value of whether or not to
     *        filter the current element
     * @param callAnyways If this is true each element will attempted to be 
     *        filtered except comments.
     */
    public UnicodeEnabledTranslationFilter(boolean enabled, boolean callAnyways) {
        _defaultUnicodeEnabled = enabled;
        _callAnyways = callAnyways;
    }
    
    public boolean hasUnicode(TranslationOptions options) {
        return options.hasHint("unicode") ? Boolean.valueOf(options
            .getHintValue("unicode")) : _defaultUnicodeEnabled;
    }
    
    // @see com.sam.moca.server.db.translate.filter.TranslationFilter#filter(com.sam.moca.server.db.translate.SQLElement[], com.sam.moca.server.db.BindList, com.sam.moca.server.db.translate.TranslationOptions)
    @Override
    public SQLElement[] filter(SQLElement[] input, BindList bindList,
            TranslationOptions options) throws TranslationException {
        for (int i = 0; i < input.length; i++) {
            SQLElement element = input[i];
            
            if (element.getType() == TokenType.COMMENT) {
                if (element.getValue().equalsIgnoreCase("/*#nounicode*/")) {
                    options.addHint("unicode=false");
                }
                else if (element.getValue().equalsIgnoreCase("/*#unicode*/")) {
                    options.addHint("unicode=true");
                }
                filterCurrentElement(input, i, options, bindList);
            }
            else if (_callAnyways || hasUnicode(options)) {
                filterCurrentElement(input, i, options, bindList);
            }
        }
        
        return input;
    }

    /**
     * This method is called for each element when unicode translation is
     * enabled or if the extending class always wants this called.
     * @param input The sql element list
     * @param pos The current position in the element list.
     * @param options The translation options
     * @param bindList The bind list
     */
    protected abstract void filterCurrentElement(SQLElement[] input, int pos, 
        TranslationOptions options, BindList bindList);
    
    private final boolean _defaultUnicodeEnabled;
    private final boolean _callAnyways;
}
