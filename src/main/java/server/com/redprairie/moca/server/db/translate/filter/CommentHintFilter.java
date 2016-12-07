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

import java.util.ArrayList;
import java.util.List;

import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TokenType;
import com.redprairie.moca.server.db.translate.TranslationException;
import com.redprairie.moca.server.db.translate.TranslationOptions;

/**
 * Filter to translate specially-formatted comments into hints, to be stored for
 * later use by the execution engine.
 * 
 * <b><pre>
 * Copyright (c) 2006 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class CommentHintFilter implements TranslationFilter {
    public SQLElement[] filter(SQLElement[] input, BindList args,
            TranslationOptions options) throws TranslationException {
        List<SQLElement> output = new ArrayList<SQLElement>();
        
        for (SQLElement element : input) {
            if (element.getType() == TokenType.COMMENT) {
                String comment = element.getValue();
                if (comment.startsWith("/*#")) {
                    String text = comment.substring(3, comment.length() - 2).trim();
                    options.addHint(text);
                    // These hints may be used on the fly by other filters, so leave the element alone.
                }
            }
            output.add(element);
        }
        
        return output.toArray(new SQLElement[output.size()]);
    }
}
