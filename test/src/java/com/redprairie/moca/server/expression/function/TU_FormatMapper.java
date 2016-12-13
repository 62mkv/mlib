/*
 *  $URL$
 *  $Revision$
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

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class TU_FormatMapper {
    @Test
    public void testKnownFormats() {
        FormatMapper mapper = new FormatMapper(DATE_MAPPING);
        
        assertEquals("yyyyMMddHHmmss", mapper.apply("YYYYMMDDHH24MISS"));
        assertEquals("MM/dd/yyyy hh:mm:ss aa", mapper.apply("MM/DD/YYYY HH:MI:SS PM"));
    }
    
    private static final Map<String, String> DATE_MAPPING = new LinkedHashMap<String, String>();
    static {
        DATE_MAPPING.put("YYYY", "yyyy");
        DATE_MAPPING.put("YY", "yy");
        DATE_MAPPING.put("MONTH", "MMMMM");
        DATE_MAPPING.put("MON", "MMM");
        DATE_MAPPING.put("MM", "MM");
        DATE_MAPPING.put("DDD", "DDD");
        DATE_MAPPING.put("DD", "dd");
        DATE_MAPPING.put("HH24", "HH");
        DATE_MAPPING.put("HH12", "hh");
        DATE_MAPPING.put("HH", "hh");
        DATE_MAPPING.put("MI", "mm");
        DATE_MAPPING.put("SSSSS", null);
        DATE_MAPPING.put("SS", "ss");

        DATE_MAPPING.put("DAY", "EEE");
        DATE_MAPPING.put("DY", "EE");
        DATE_MAPPING.put("D", "E");

        DATE_MAPPING.put("IW", "ww");
        DATE_MAPPING.put("WW", "ww");

        DATE_MAPPING.put("PM", "aa");
    }

}
