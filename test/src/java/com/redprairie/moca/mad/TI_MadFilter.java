/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2014
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

package com.redprairie.moca.mad;

import javax.management.MalformedObjectNameException;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.jolokia.client.J4pClient;
import org.jolokia.client.J4pClientBuilder;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.exception.J4pRemoteException;
import org.jolokia.client.request.J4pReadRequest;
import org.jolokia.client.request.J4pReadResponse;
import org.junit.Test;

import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.server.ServerUtils;

import static org.junit.Assert.*;

/**
 * Integration tests for the MadFilter which works in conjunction with
 * Jolokia to provide basic authentication.
 * 
 * @author rrupp
 */
public class TI_MadFilter {
    
    private static final String WS_JMX_URL = ServerUtils.globalContext()
                                                        .getConfigurationElement(MocaRegistry.REGKEY_SERVER_URL)
                                                        .replace("/service", "/ws/jmx");
    
    /**
     * Tests not providing credentials which should result in a not authorized exception (401)
     * @throws J4pException
     * @throws MalformedObjectNameException
     */
    @Test
    public void testUnauthorized() throws J4pException, MalformedObjectNameException {
        J4pClient j4pClient = new J4pClient(WS_JMX_URL);
        testInvalidPassword(j4pClient);
    }
    
    /**
     * Tests invalid credentials for the internal admin user specified by the MOCA registry
     * settings security.admin-user and security.admin-password. This test requires the
     * security.admin-user registry entry is setup.
     * @throws J4pException
     * @throws MalformedObjectNameException
     */
    @Test
    public void testInvalidInternalAuthorization()  throws J4pException, MalformedObjectNameException {
        J4pClient j4pClient = new J4pClientBuilder()
                                    .url(WS_JMX_URL)
                                    .user(getInternalAdminUser())
                                    .password("invalidpassword")
                                    .build();
        
        testInvalidPassword(j4pClient);
    }
    
    /**
     * Tests providing credentials with a non-internal/admin user, this should end up calling "login user"
     * however with just MOCA for our unit tests the credentials don't actually get validated here (done at MCS layer).
     * This basically just makes sure authentication happens and the session cookie is bootstrapped for subsequent requests.
     * @throws J4pException
     * @throws MalformedObjectNameException
     */
    @Test
    public void testGetValueWithRegularAuthorization() throws J4pException, MalformedObjectNameException {
        J4pClient j4pClient = new J4pClientBuilder()
                                    .url(WS_JMX_URL)
                                    .user("SUPER")
                                    .password("SUPER")
                                    .build();
        testAbleToAuthenticateAndRetrieve(j4pClient);
    }
    
    /**
     * Tests getting a value using the internal admin-user authentication specified in the MOCA registry, test
     * assumes admin-user = admin and admin-password = password
     * @throws J4pException
     * @throws MalformedObjectNameException
     */
    @Test
    public void testGetValueWithInternalAdminAuthorization() throws J4pException, MalformedObjectNameException {
        J4pClient j4pClient = new J4pClientBuilder()
                                    .url(WS_JMX_URL)
                                    .user(getInternalAdminUser())
                                    .password("password")
                                    .build();
        testAbleToAuthenticateAndRetrieve(j4pClient);
    }
    
    // Tests that the client can read some attributes and that the session cookie is bootstrapped
    private void testAbleToAuthenticateAndRetrieve(J4pClient j4pClient) throws MalformedObjectNameException, J4pException {
        assertNull(getSessionCookie(j4pClient));
        validateOperatingSystemObjectName(j4pClient);
        // After authentication the MadFilter should've setup a session cookie for subsequent requests
        validateSessionCookieExists(j4pClient);
        validateOperatingSystemObjectName(j4pClient);
    }
    
    private void testInvalidPassword(J4pClient j4pClient) throws J4pException, MalformedObjectNameException {
        try {
            validateOperatingSystemObjectName(j4pClient);
            fail("Should have received a unauthorized exception (401)");
        }
        catch (J4pRemoteException expected) {
            assertEquals(401, expected.getStatus());
            assertNull(getSessionCookie(j4pClient));
        }
    }
    
    // Simply tests reading the Operating System mbean and the ObjectName attribute on it (which should always be the
    // same as the mbean's object name so it's a consistent thing we can validate with.
    private void validateOperatingSystemObjectName(J4pClient j4pClient) throws J4pException, MalformedObjectNameException {
        J4pReadRequest req = new J4pReadRequest("java.lang:type=OperatingSystem", "ObjectName");
        req.setPath("objectName");
        J4pReadResponse resp = j4pClient.execute(req);
        assertEquals("java.lang:type=OperatingSystem", resp.getValue());
    }
    
    private void validateSessionCookieExists(J4pClient j4pClient) {
        Cookie sessionCookie = getSessionCookie(j4pClient);
        assertNotNull(sessionCookie);
        assertEquals("/ws/jmx", sessionCookie.getPath());
    }
    
    private Cookie getSessionCookie(J4pClient j4pClient) {
        // Find the session cookie set by the filter, have to downcast to AbstractHttpClient
        // to be able to access cookies.
        AbstractHttpClient httpClient = (AbstractHttpClient) j4pClient.getHttpClient();
        Cookie sessionCookie = null;
        for (Cookie cookie : httpClient.getCookieStore().getCookies()) {
            if (cookie.getName().equals(MadFilter.SESSION_COOKIE_NAME)) {
                sessionCookie = cookie;
            }
        }
        
        return sessionCookie;
    }
    
    private String getInternalAdminUser() {
        final String adminUser = ServerUtils.globalContext()
                .getConfigurationElement(MocaRegistry.REGKEY_SECURITY_ADMIN_USER);
        assertNotNull("The security.admin-user must be specified for this test in the MOCA registry",
            adminUser);
        return adminUser;
    }

}
