/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.redprairie.moca.server.expression.function;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * This is a format mapper that will change an oracle specific to_date format
 * to the java.util.SimpleDateFormat format.
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class OracleDBDateFormatToJava extends FormatMapper {

    /**
     * @param mappings
     */
    public OracleDBDateFormatToJava() {
        super(_mappings);
    }

    private final static Map<String, String> _mappings = new LinkedHashMap<String, String>();
    static {
        _mappings.put("YYYY", "yyyy");
        _mappings.put("YY", "yy");
        _mappings.put("MONTH", "MMMMM");
        _mappings.put("MON", "MMM");
        _mappings.put("MM", "MM");
        _mappings.put("DDD", "DDD");
        _mappings.put("DD", "dd");
        _mappings.put("HH24", "HH");
        _mappings.put("HH12", "hh");
        _mappings.put("HH", "hh");
        _mappings.put("MI", "mm");
        _mappings.put("SSSSS", null);
        _mappings.put("D", null);
        _mappings.put("J", null);
        _mappings.put("SS", "ss");

        _mappings.put("DAY", "EEEE");
        _mappings.put("DY", "EEE");

        _mappings.put("IW", "ww");
        _mappings.put("WW", "ww");

        _mappings.put("PM", "aa");
    }
}
