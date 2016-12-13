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

package com.redprairie.moca.server.db.translate.filter.mssql;

import java.util.HashMap;
import java.util.Map;

import com.redprairie.moca.server.db.translate.filter.SimpleWordFilter;

/**
 * Filter to to handle simple word replacement for SQL functions that have
 * direct equivalents in the target dialect.
 * 
 * <b><pre>
 * Copyright (c) 20167 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MSSimpleWordFilter extends SimpleWordFilter {
    public MSSimpleWordFilter() {
        super(_TRANSLATIONS);
    }
    
    private static final Map<String, String> _TRANSLATIONS = 
            new HashMap<String, String> ();
    static {
        _TRANSLATIONS.put("sysdate", "CONVERT(DATETIME, CONVERT(VARCHAR,GETDATE(),20), 20)");
     }
}
