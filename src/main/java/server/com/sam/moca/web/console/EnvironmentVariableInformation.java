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

package com.sam.moca.web.console;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.sam.moca.MocaRegistry;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.exec.SystemContext;

public class EnvironmentVariableInformation {

    public EnvironmentVariableInformation() {
        // Get the system environment variables.
        _map = new TreeMap<String, String>(System.getenv());

        // Get the system context so we can get the environment section of the registry.
        SystemContext ctxt = ServerUtils.globalContext();

        // Iterate through each registry key within this section and add it to
        // the map.
        Map<String, String> sectionMap = ctxt.getConfigurationSection(
            MocaRegistry.REGSEC_ENVIRONMENT, true);
        for (Map.Entry<String, String> key : sectionMap.entrySet()) {
            _map.put(key.getKey(), key.getValue());
        }
    }

    public Map<String, String> getEnvironmentVariables() {
        return _map;
    }

    private final SortedMap<String, String> _map;
}