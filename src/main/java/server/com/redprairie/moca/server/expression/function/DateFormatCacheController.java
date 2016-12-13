/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.server.expression.function;

import java.text.SimpleDateFormat;

import com.redprairie.moca.cache.BaseCacheController;

/**
 * A cache controller for the to_date/to_char formatter mapped to SimpleDateFormat
 * objects.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
class DateFormatCacheController extends BaseCacheController<String, SimpleDateFormat> {
    @Override
    public SimpleDateFormat loadEntry(String key) {
        String sdfFormat = _mapper.apply(key);
        if (sdfFormat != null) {
            try {
                return new SimpleDateFormat(sdfFormat);
            }
            catch (IllegalArgumentException e) {
                // Ignore malformed input -- use other means
            }
        }
        return null;
    }
    
    private final static FormatMapper _mapper = new OracleDBDateFormatToJava();

}
