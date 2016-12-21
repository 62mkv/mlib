/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.sam.moca.server.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.sam.moca.client.ConnectionUtils;
import com.sam.moca.server.SecurityLevel;

/**
 * Validates incoming client keys, according to rules specified in
 * a properties object.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class ClientKeyValidator {
    
    public ClientKeyValidator(String serverKey, Map<String, String> config) {
        // Default to anonymous client access, unless told otherwise.
        SecurityLevel anonymousLevel = SecurityLevel.ALL;
        
        if (config != null) {
            for (Map.Entry<String, String> entry : config.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                SecurityLevel l;
                if (value != null) {
                    value = value.trim().toUpperCase();
                    if (value.equals("NONE")) {
                        l = null;
                    }
                    else if (value.equals("")) {
                        l = SecurityLevel.ALL;
                    }
                    else {
                        l = SecurityLevel.valueOf(value);
                    }
                }
                else {
                    l = SecurityLevel.ALL;
                }
                
                // Check for special key: *
                if (key.equals("*")) {
                    anonymousLevel = l;
                }
                else {
                    ClientInfo info = new ClientInfo();
                    info.key = key;
                    
                    int pound = key.indexOf('#');
                    if (pound != -1) {
                        info.shortName = key.substring(0, pound);
                    }
                    else {
                        info.shortName = key;
                    }
    
                    info.security = l;
                    
                    _allowedClients.add(info);
                }
            }
        }
        
        _serverKey = serverKey;
        _anonymousSecurity = anonymousLevel;
    }

    
    public SecurityLevel validate(String key) {
        if (key == null) {
            return _anonymousSecurity;
        }
        
        String[] parts = key.split("/", 2);
        if (parts.length != 2) {
            return _anonymousSecurity;
        }
        
        String name = parts[0];
        
        for (ClientInfo client : _allowedClients) {
            if (client.shortName.equals(name)) {
                String actualKey = client.key;
        
                String generated = ConnectionUtils.generateClientKey(actualKey, _serverKey);
        
                if (generated.equals(key)) {
                    return client.security;
                }
            }
        }
        
        return _anonymousSecurity; 
    }

    private static class ClientInfo {
        private String key;
        private String shortName;
        private SecurityLevel security;
    }
    
    private final Collection<ClientInfo> _allowedClients = new ArrayList<ClientInfo>();
    private final SecurityLevel _anonymousSecurity;
    private final String _serverKey;
}
