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

package com.sam.moca.server.session;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.sam.moca.server.SecurityLevel;


/**
 * Validator for legacy MOCA session keys.  This algorithm conforms with the
 * MOCA session key validator as of version 2009.1.0, and will validate 
 * MOCA session keys going back to version 1.1 of MOCA.
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MocaSessionValidator {
    
    static final String KEY_USER_ID = "uid";
    static final String KEY_SID = "sid";
    static final String KEY_CREATED_DATE = "dt";
    static final String KEY_SECURITY_LEVEL = "sec";
    static final char FIELD_SEPARATOR_CHAR = '|';
    static final String FIELD_SEPARATOR_EXPR = "\\|";
    
    /**
     * Constructor that takes a collection of known valid trusted domains.
     * 
     * @param trustedDomains the collection of trusted domains. If this list is
     * <code>null</code> or empty, then only the blank domain is trusted.
     */
    public MocaSessionValidator(Iterable<String> trustedDomains) {
        if (trustedDomains != null) {
            for (String d : trustedDomains) {
                if (d == null) {
                    continue;
                }
                
                int pound = d.indexOf('#');
                
                if (pound != -1) {
                    String shortDomain = d.substring(0, pound);
                    trustedDomainMap.put(shortDomain, d);
                }
                else {
                    trustedDomainMap.put(d, d);
                }
            }
        }
        
        // In case no domains are trusted, 
        if (trustedDomainMap.isEmpty()) {
            trustedDomainMap.put("", "");
        }
    }
    
    /**
     * Validates the given MOCA session key.  This method validates that (a) the key is
     * valid, and (b) the domain represented in the key is one that we trust.
     * @param key
     * @return
     */
    public SessionToken validate(String key) {
        String[] parts = key.split(";", 3);
        if (parts.length != 3) {
            return null;
        }
        
        String domain = parts[0];
        String userId = parts[1];
        String seed = parts[2];
        
        if (seed.length() < 4) {
            return null;
        }
        else {
            seed = seed.substring(0,4);
        }

        String actualDomain = trustedDomainMap.get(domain);
        
        if (actualDomain == null) {
            return null;
        }
        
        String generated = SessionKey.generateKey(userId, actualDomain, seed);
        if (!generated.equals(key)) {
            return null;
        }
        
        String[] fields = userId.split(FIELD_SEPARATOR_EXPR);
        SessionToken token;
        
        // Legacy mode -- allow a session with a user ID field only.
        if (fields.length == 1) {
            token = new SessionToken(fields[0]);
        }
        else {
            Map<String, String> values = new HashMap<String, String>();
            for (String field : fields) {
                String[] pair = field.split("=", 2);
                if (pair.length != 2) {
                    values.put(KEY_USER_ID, field);
                }
                else {
                    String fieldKey = pair[0];
                    String fieldValue = pair[1];
                    values.put(fieldKey, fieldValue);
                }
            }
            String uid = values.remove(KEY_USER_ID);
            String sid = values.remove(KEY_SID);
            String temp = values.remove(KEY_CREATED_DATE);
            
            Date created = temp == null ? null : new Date(new BigInteger(temp, 36).longValue());
            String securityText = values.remove(KEY_SECURITY_LEVEL);
            
            SecurityLevel security = null;
            
            if (securityText != null) {
                try {
                    security = SecurityLevel.valueOf(securityText);
                }
                catch (IllegalArgumentException e) {
                    // Ignore exceptions
                }
            }
            
            // To support legacy usage, we need to default security level to ALL.
            if (security == null) {
                security = SecurityLevel.ALL;
            }
            
            token = new SessionToken(domain, uid, sid, created, values, security);
        }
        
        return token;
    }
    
    private final Map<String, String> trustedDomainMap = new HashMap<String, String>();
}
