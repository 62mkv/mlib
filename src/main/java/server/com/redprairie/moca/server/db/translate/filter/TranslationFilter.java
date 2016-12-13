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

package com.redprairie.moca.server.db.translate.filter;

import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TranslationException;
import com.redprairie.moca.server.db.translate.TranslationOptions;

/**
 * Interface to act as a SQL translation filter.  Translation filters
 * perform simple operations between input and output arrays of SQL elements
 * (tokens).  The SQLTranslator class uses these filters to effect a complete
 * translation of one SQL dialect into another.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public interface TranslationFilter {
    /**
     * Apply this translation filter to the SQL elements given.  The
     * <code>bindList</code> and <code>options</code> objects may be
     * modified to affect the behavior of either the SQL execution engine
     * or further translation filters.
     * @param input the SQL tokens making up this SQL statement.
     * @param bindList an instance of <code>BindList</code> that can be
     * queried for data to facilitate a filter, or can be added into to
     * cause a new bind variable to be available to the SQL processor.
     * @param options an object representing SQL execution options that 
     * will be handled by the exection engine.  This object can be modified by
     * this filter.  
     * @return an array of <code>SQLElement</code> objects.  It is acceptable
     * to return <code>input</code>.  No assumption is made that these arrays
     * are not identical.
     * @throws TranslationException
     */
    public SQLElement[] filter(SQLElement[] input, BindList bindList,
            TranslationOptions options) throws TranslationException;
}
