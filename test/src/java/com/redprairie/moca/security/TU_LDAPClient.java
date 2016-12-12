/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2007
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

package com.redprairie.moca.security;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import junit.framework.TestCase;

/**
 * Unit tests for the LDAPClient class.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author mlange
 * @version $Revision$
 */
public class TU_LDAPClient extends TestCase {

    // anonymous bind no longer supported

    public void testAnonymousBindFailure() {
        
        // Construct an LDAP client.
        try {
            new LDAPClient("foo");
        }
        catch (IllegalArgumentException e) {
            // Normal
            return;
        }
        
        fail("Anonymous bind should not be supported.");
    }

    public void testBindSuccess() {
        LDAPClient client = null;

        // Construct an LDAP client.
        try {
            client = new LDAPClient(URL, BIND_DN, BIND_PASSWORD);
        }
        catch (IllegalArgumentException e) {
            fail("Could not construct LDAP client: " + e);
        }

        // Bind to the LDAP server.
        try {
            client.bind();
        }
        catch (LDAPClientBindException e) {
            fail("LDAP bind failure: " + e);
        }
    }

    public void testBindFailure() {
        LDAPClient client = null;

        // Construct an LDAP client.
        try {
            client = new LDAPClient("foo", BIND_DN, BIND_PASSWORD);
        }
        catch (IllegalArgumentException e) {
            fail("Could not construct LDAP client: " + e);
        }

        // Bind to the LDAP server.
        try {
            client.bind();
        }
        catch (LDAPClientBindException e) {
            // Normal
        }
    }

    public void testAuthenticationSuccess() {

        final String USERNAME = BIND_DN;
        final String PASSWORD = BIND_PASSWORD;

        LDAPClient client = null;

        // Construct an LDAP client.
        try {
            client = new LDAPClient(URL, BIND_DN, BIND_PASSWORD);
        }
        catch (IllegalArgumentException e) {
            fail("Could not construct LDAP client: " + e);
        }

        // Bind to the LDAP server.
        try {
            client.bind();
        }
        catch (LDAPClientBindException e) {
            fail("LDAP bind failure: " + e);
        }

        // Authenticate the user.
        try {
            client.authenticateUser(USERNAME, PASSWORD);
        }
        catch (LDAPClientAuthenticationException e) {
            fail("LDAP authentication failure " + e);
        }
    }

    // Invalid username
    public void testAuthenticationAFailure() {

        final String USERNAME = "foo";
        final String PASSWORD = BIND_PASSWORD;

        LDAPClient client = null;

        // Construct an LDAP client.
        try {
            client = new LDAPClient(URL, BIND_DN, BIND_PASSWORD);
        }
        catch (IllegalArgumentException e) {
            fail("Could not construct LDAP client: " + e);
        }

        // Bind to the LDAP server.
        try {
            client.bind();
        }
        catch (LDAPClientBindException e) {
            fail("LDAP bind failure: " + e);
        }

        // Authenticate the user.
        try {
            client.authenticateUser(USERNAME, PASSWORD);
            fail("Expected an LDAPClientAuthenticationException to be thrown");
        }
        catch (LDAPClientAuthenticationException e) {
            // Normal
        }
    }

    // Invalid password
    public void testAuthenticationBFailure() {

        LDAPClient client = null;

        // Construct an LDAP client.
        try {
            client = new LDAPClient(URL, BIND_DN, BIND_PASSWORD);
        }
        catch (IllegalArgumentException e) {
            fail("Could not construct LDAP client: " + e);
        }

        // Bind to the LDAP server.
        try {
            client.bind();
        }
        catch (LDAPClientBindException e) {
            fail("LDAP bind failure: " + e);
        }

        // Authenticate the user.
        try {
            String username = BIND_DN;
            String wrongPassword = "foo";

            client.authenticateUser(username, wrongPassword);
            fail("Expected an LDAPClientAuthenticationException to be thrown");
        }
        catch (LDAPClientAuthenticationException e) {
            // Normal
        }
    }

    public void testSearch1ASuccess() {

        final String SEARCH_FILTER = "(sAMAccountName=mlange)";

        LDAPClient client = null;

        // Construct an LDAP client.
        try {
            client = new LDAPClient(URL, BIND_DN, BIND_PASSWORD);
        }
        catch (IllegalArgumentException e) {
            fail("Could not construct LDAP client: " + e);
        }

        // Bind to the LDAP server.
        try {
            client.bind();
        }
        catch (LDAPClientBindException e) {
            fail("LDAP bind failure: " + e);
        }

        // Search the LDAP server.
        try {
            Attributes attrs = client.search(null, SEARCH_FILTER);

            // We should have matching attributes.
            if (attrs.size() == 0)
                fail("LDAP search returned no matching attributes - it should have");
        }
        catch (LDAPClientSearchException e) {
            fail("LDAP search failure: " + e);
        }
    }

    // Invalid search base.
    public void testSearch1AFailure() {

        final String SEARCH_FILTER = "(cn=Foo)";

        LDAPClient client = null;

        // Construct an LDAP client.
        try {
            client = new LDAPClient(URL, BIND_DN, BIND_PASSWORD);
        }
        catch (IllegalArgumentException e) {
            fail("Could not construct LDAP client: " + e);
        }

        // Bind to the LDAP server.
        try {
            client.bind();
        }
        catch (LDAPClientBindException e) {
            fail("LDAP bind failure: " + e);
        }

        // Search the LDAP server.
        try {
            Attributes attrs = client.search(null, SEARCH_FILTER);

            // We should not have any matching attributes.
            if (attrs.size() != 0)
                fail("LDAP search returned matching attributes - it should not have");
        }
        catch (LDAPClientSearchException e) {
            fail("LDAP search failure: " + e);
        }
    }

    public void testSearch2ASuccess() {

        final String SEARCH_FILTER = "(cn=LDAPUsage)";
        final String SEARCH_ATTR = "cn";

        LDAPClient client = null;

        // Construct an LDAP client.
        try {
            client = new LDAPClient(URL, BIND_DN, BIND_PASSWORD);
        }
        catch (IllegalArgumentException e) {
            fail("Could not construct LDAP client: " + e);
        }

        // Bind to the LDAP server.
        try {
            client.bind();
        }
        catch (LDAPClientBindException e) {
            fail("LDAP bind failure: " + e);
        }

        // Search the LDAP server.
        try {
            Attributes attrs = client.search(null, SEARCH_FILTER,
                SEARCH_ATTR);

            // We should have matching attributes.
            if (attrs.size() == 0)
                fail("LDAP search returned no matching attributes - it should have");
        }
        catch (LDAPClientSearchException e) {
            fail("LDAP search failure: " + e);
        }
    }

    // Invalid search base.
    public void testSearch2AFailure() {

        final String SEARCH_FILTER = "(cn=Foo)";
        final String SEARCH_ATTR = "cn";

        LDAPClient client = null;

        // Construct an LDAP client.
        try {
            client = new LDAPClient(URL, BIND_DN, BIND_PASSWORD);
        }
        catch (IllegalArgumentException e) {
            fail("Could not construct LDAP client: " + e);
        }

        // Bind to the LDAP server.
        try {
            client.bind();
        }
        catch (LDAPClientBindException e) {
            fail("LDAP bind failure: " + e);
        }

        // Bind to the LDAP server.
        try {
            Attributes attrs = client.search(null, SEARCH_FILTER,
                SEARCH_ATTR);

            // We should not have any matching attributes.
            if (attrs.size() != 0)
                fail("LDAP search returned matching attributes - it should not have");
        }
        catch (LDAPClientSearchException e) {
            fail("LDAP search failure: " + e);
        }
    }

    // Invalid search filter.
    public void testSearch2BFailure() {

        final String SEARCH_FILTER = "(cn=LDAPUsage)";
        final String SEARCH_ATTR = "foo";

        LDAPClient client = null;

        // Construct an LDAP client.
        try {
            client = new LDAPClient(URL, BIND_DN, BIND_PASSWORD);
        }
        catch (IllegalArgumentException e) {
            fail("Could not construct LDAP client: " + e);
        }

        // Bind to the LDAP server.
        try {
            client.bind();
        }
        catch (LDAPClientBindException e) {
            fail("LDAP bind failure: " + e);
        }

        // Bind to the LDAP server.
        try {
            Attributes attrs = client.search(null, SEARCH_FILTER,
                SEARCH_ATTR);

            // We should not have any matching attributes.
            if (attrs.size() != 0)
                fail("LDAP search returned matching attributes - it should not have");
        }
        catch (LDAPClientSearchException e) {
            fail("LDAP search failure: " + e);
        }
    }

    public void testGetInformation() {

        LDAPClient client = null;

        // Construct an LDAP client.
        try {
            client = new LDAPClient(URL, BIND_DN, BIND_PASSWORD);
        }
        catch (IllegalArgumentException e) {
            fail("Could not construct LDAP client: " + e);
        }

        // Bind to the LDAP server.
        try {
            client.bind();
        }
        catch (LDAPClientBindException e) {
            fail("LDAP bind failure: " + e);
        }

        // Get information about the LDAP server.
        try {
            Attributes attrs = client.getInformation();

            client.printAttributes(attrs);
        }
        catch (NamingException e) {
            fail("LDAP get information failure printing attributes " + e);
        }
        catch (LDAPClientException e) {
            fail("LDAP get information failure: " + e);
        }
    }

    private final static String URL = "ldap://wiadc4:3268/OU=WIA,DC=DOM1,DC=REDPRAIRIE,DC=com";
    private final static String BIND_DN = "cn=Development LDAP Usage,ou=wia users,ou=wia,dc=dom1,dc=redprairie,dc=com";
    private final static String BIND_PASSWORD = "S3c#r1ty";
}