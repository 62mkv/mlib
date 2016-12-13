/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.util;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Handles expanding environment variables for the given environment.
 * 
 * Copyright (c) 2013 Sam Corporation All Rights Reserved
 * 
 * @author rrupp
 */
public class EnvironmentExpander {

    /**
     * Expands all the environment variables in the given environment. Modifies
     * the entries in the environment map to the expanded values.
     * 
     * @param env The environment to expand
     */
    public void expand(final Map<String, String> env) {
        for (Entry<String, String> entry : env.entrySet()) {
            String expandedValue = expand(entry.getValue(), entry.getKey(),
                 env);
            // Now we set the entry value back
            entry.setValue(expandedValue);
        }
    }
    
    private String expand(final String value, final String key, final Map<String, String> map) {
        return new VarStringReplacer(new StringReplacer.ReplacementStrategy() {
            @Override
            public String lookup(String varName) {
                // Special case to avoid recursive lookups
                if (varName.equalsIgnoreCase(key)) {
                    return System.getenv(varName);
                }

                String value = map.get(varName);

                if (value == null) {
                    value = System.getenv(varName);
                }
                return expand(value, varName, map);
            }
        }).translate(value);
    }
}
