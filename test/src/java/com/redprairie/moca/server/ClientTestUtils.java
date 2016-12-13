/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.redprairie.moca.server;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.client.ConnectionUtils;
import com.redprairie.moca.client.HttpConnection;
import com.redprairie.moca.client.MocaConnection;

/**
 * Utility methods for client integration tests
 */
public class ClientTestUtils {

    /**
     * The user name to use when authenticating for integration tests
     */
    public static final String LOGIN_USER = "SUPER";

    /**
     * The password to use when authenticating for integration tests
     */
    public static final String LOGIN_PASS = "SUPER";

    private ClientTestUtils() {}

    /**
     * Generates a random file name with the given extension
     * @param extension The extension to use e.g. "csv" or "xml" or "log"
     * @return The random file name
     * @throws IOException
     */
    public static String generateRandomFileName(String extension) throws IOException {
        return "testing" + System.nanoTime() + "." + extension;
    }

    /**
     * Establishes and authenticates a new connection for testing
     * @return
     * @throws MocaException
     */
    public static MocaConnection newConnection() throws MocaException {
        return newConnection(Collections.<String, String>emptyMap());
    }

    /**
     * Establishes and authenticates a new connection for testing
     * and bootstraps the given environment variables
     * @param env The environment
     * @return
     * @throws MocaException
     */
    public static MocaConnection newConnection(Map<String, String> env) throws MocaException {
        MocaConnection conn = new HttpConnection(SERVICE_URL, env);
        ConnectionUtils.login(conn, LOGIN_USER, LOGIN_PASS, "test");
        return conn;
    }

    private static final String SERVICE_URL =
            ServerUtils.globalContext().getConfigurationElement(MocaRegistry.REGKEY_SERVER_URL);
}
