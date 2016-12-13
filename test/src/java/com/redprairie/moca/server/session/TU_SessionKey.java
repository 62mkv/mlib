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

package com.redprairie.moca.server.session;

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


/**
 * Test for MocaSession and MocaSessionValidator.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_SessionKey {

    @Test
    public void testValidSessionNoDomain() {
        MocaSessionValidator validator = new MocaSessionValidator(null);
        String key = ";SUPER;LRMm1vcfWVUDG4HL5mrp4c5hl0";
        assertNotNull(validator.validate(key));
    }

    @Test
    public void testValidSessionWithDomain() {
        MocaSessionValidator validator = new MocaSessionValidator(Arrays.asList(new String[] {"DEV"}));
        String key = "DEV;SUPER;UMKJQRcf3qvrAjtoOJANviWxH2";
        assertNotNull(validator.validate(key));
    }

    @Test
    public void testValidSessionWithDomainHidden() {
        MocaSessionValidator validator = new MocaSessionValidator(Arrays.asList(new String[] { "DEV#SECRET" }));

        // This key is only valid with the hidden portion of the domain, known
        // to the server.
        String key = "DEV;SUPER;AibHWfVy9ZSC6IOCzJMzNSztU1";
        assertNotNull(validator.validate(key));

        // This otherwise valid key, which was generated without the hidden part
        // of the domain, is invalid with the domain secret.
        assertNull(validator.validate("DEV;SUPER;UMKJQRcf3qvrAjtoOJANviWxH2"));

        // This key doesn't have any domain information on it.
        assertNull(validator.validate(";SUPER;LRMm1vcfWVUDG4HL5mrp4c5hl0"));
    }

    @Test
    public void testInvalidSession() {
        MocaSessionValidator validator = new MocaSessionValidator(Arrays.asList(new String[] { "DEV#SECRET" }));

        // If the userID changes, the key is invalid.
        String key = "DEV;JOE;AibHWfVy9ZSC6IOCzJMzNSztU1";
        assertNull(validator.validate(key));
        
        // Test an obviously wrong key
        assertNull(validator.validate(";;"));
    }
    
    @Test
    public void testCreateSession() {
        String sessionKey = newSessionKey("USER", "DOMAIN");
        assertNotNull(sessionKey);
        assertNotNull(new MocaSessionValidator(Arrays.asList(new String[] {"DOMAIN"})).validate(sessionKey));
    }
    
    @Test
    public void testCreateSessionNull() {
        String sessionKey = newSessionKey(null, null);
        assertNotNull(sessionKey);
        assertNotNull(new MocaSessionValidator(null).validate(sessionKey));
    }
    
    
    @Test
    public void testNullDomainWithValidSession() {
        String sessionKey = newSessionKey(null, null);
        assertNotNull(sessionKey);
        assertNotNull(new MocaSessionValidator(Arrays.asList((String)null)).validate(sessionKey));
    }
    
    private static String newSessionKey(String name, String domain) {
        return SessionKey.generateKey(name, domain, "GWQ8");
    }
}
