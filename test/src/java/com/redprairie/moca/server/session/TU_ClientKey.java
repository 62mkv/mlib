/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.server.session;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import com.redprairie.moca.client.ConnectionUtils;
import com.redprairie.moca.server.SecurityLevel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;


/**
 * Test for ClientKey and ClientKeyValidator.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_ClientKey {

    @Test
    public void testValidKeyClearText() {
        ClientKeyValidator validator = new ClientKeyValidator("foobar", loadResources("validClearText.properties"));
        String key = ConnectionUtils.generateClientKey("blah", "foobar");
        assertEquals(SecurityLevel.ALL, validator.validate(key));
    }

    @Test
    public void testValidKeySecretText() {
        ClientKeyValidator validator = new ClientKeyValidator("foobar", loadResources("testClients.properties"));
        String key = ConnectionUtils.generateClientKey("foo#SECRET000", "foobar");
        assertEquals(SecurityLevel.ALL, validator.validate(key));
        assertFalse(key.contains("SECRET000"));
    }

    @Test
    public void testInvalidKey() throws Exception {
        ClientKeyValidator validator = new ClientKeyValidator("foobar", loadResources("testClients.properties"));
        String key = ConnectionUtils.generateClientKey("blah", "foobar");
        assertEquals(SecurityLevel.OPEN, validator.validate(key));
    }

    @Test
    public void testInvalidKeyWrongSecretText() {
        ClientKeyValidator validator = new ClientKeyValidator("foobar", loadResources("testClients.properties"));
        String key = ConnectionUtils.generateClientKey("foo#SECRET001", "foobar");
        assertEquals(SecurityLevel.OPEN, validator.validate(key));
    }

    @Test
    public void testInvalidKeyWrongServerKey() {
        ClientKeyValidator validator = new ClientKeyValidator("foobar", loadResources("testClients.properties"));
        String key = ConnectionUtils.generateClientKey("foo#SECRET000", "foobaz");
        assertEquals(SecurityLevel.OPEN, validator.validate(key));
    }

    @Test
    public void testVariousKeysNoProperties() {
        ClientKeyValidator validator = new ClientKeyValidator("foobar", null);
        String key = ConnectionUtils.generateClientKey("blah", "foobar");
        assertEquals(SecurityLevel.ALL, validator.validate(key));
        key = ConnectionUtils.generateClientKey("WXYZ", "XXXX");
        assertEquals(SecurityLevel.ALL, validator.validate(key));
        assertEquals(SecurityLevel.ALL, validator.validate("NOTAREALKEY"));
        assertEquals(SecurityLevel.ALL, validator.validate(""));
        assertEquals(SecurityLevel.ALL, validator.validate(null));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Map<String, String> loadResources(String name) {
        InputStream in = TU_ClientKey.class.getResourceAsStream("resources/" + name);
        try {
            try {
                Properties props = new Properties();
                props.load(in);
                return new LinkedHashMap<String, String>((Map)props);
            }
            finally {
                in.close();
            }
        }
        catch (IOException e) {
            fail("unexpected error: " + e);
        }
        return null;
    }
}
