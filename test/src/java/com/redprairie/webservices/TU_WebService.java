/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.webservices;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.util.ResponseUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * A test class to test web services.
 * 
 * Copyright (c) 2012 Sam Corporation All Rights Reserved
 * 
 * @author klehrke
 */
public class TU_WebService {

    /***
     * This is a test that test web services calls, but more specifically that
     * we can set session variables through login user using LoginService. Then
     * being able to retrieve them through MocaContext.getSystemVariable.
     * 
     * 
     * @throws MocaException
     * @throws IOException
     * @throws URISyntaxException
     */

    private static final CookieManager cookies = new CookieManager(null,
        CookiePolicy.ACCEPT_ALL);
    private static final String url = ServerUtils.globalContext()
        .getConfigurationElement(MocaRegistry.REGKEY_SERVER_URL);

    @BeforeClass
    public static void beforeClass() throws MocaException, IOException,
            URISyntaxException {
        login();
    }

    public static void login() throws MocaException, IOException, URISyntaxException {

        BufferedReader rd = null;
        OutputStream loginOutputStream = null;
        OutputStreamWriter loginOutputStreamWriter = null;
        HttpURLConnection loginConnection = null;

        URI webURI = new URI(url);
        String line = null;
        try {

            // Make a call to the login service.
            final URL loginWebUrl = new URL(url.replace("service", "ws/auth/login"));
            loginConnection = (HttpURLConnection) loginWebUrl.openConnection();
            loginConnection.setDoOutput(true);
            loginConnection.setRequestMethod("POST");
            loginConnection.setRequestProperty("content-type", "application/json");
            loginConnection.connect();
            loginOutputStream = loginConnection.getOutputStream();
            loginOutputStreamWriter = new OutputStreamWriter(loginOutputStream, "UTF-8");
            final String data = "{ \"usr_id\" : \"super\", \"usr_pswd\" : \"super\", "
                    + "\"warehouse_id\" : \"1\", \"extra_arg\" : \"extra_arg\", \"client_key\" : \"client\", \"client_key\" : \"random_string\" }";
            loginOutputStreamWriter.write(data);
            loginOutputStreamWriter.flush();

            rd = new BufferedReader(new InputStreamReader(
                loginConnection.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();

            while ((line = rd.readLine()) != null) {
                sb.append(line + '\n');
            }

            assertEquals(200, loginConnection.getResponseCode());
            cookies.put(webURI, loginConnection.getHeaderFields());
        }
        finally {
            try {
                if (rd != null) {
                    rd.close();
                }
            }
            finally {
                try {
                    if (loginOutputStream != null) {
                        loginOutputStream.close();
                    }
                }
                finally {
                    try {
                        if (loginOutputStreamWriter != null) {
                            loginOutputStreamWriter.close();
                        }
                    }
                    finally {
                        if (loginConnection != null) {
                            loginConnection.disconnect();
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testCheckClientKey() throws URISyntaxException, IOException {

        // Make a call to a test service to ensure we can get to a
        // session variable we have set.
        final URL varWebUrl = new URL(url.replace("service", "ws/test/var/client_key"));

        final HttpURLConnection varConnection = (HttpURLConnection) varWebUrl
            .openConnection();
        varConnection.setDoOutput(true);
        varConnection.addRequestProperty("Accept", "application/xml");
        putCookiesInRequest(varConnection, new URI(url));
        String line = null;
        BufferedReader varRd = null;
        StringBuilder varSb = new StringBuilder();
        try {
            varConnection.connect();
            varRd = new BufferedReader(new InputStreamReader(
                varConnection.getInputStream(), "UTF-8"));
            line = "";
            while ((line = varRd.readLine()) != null) {
                varSb.append(line);
            }
        }
        finally {
            try {
                if (varRd != null) varRd.close();
            }
            finally {
                varConnection.disconnect();
            }
        }
        // We don't want client_key coming back from the environment. //safest.
        assertEquals("", varSb.toString());

    }

    @Test
    public void testCheckWebClientAddr() throws URISyntaxException, IOException {

        // Make a call to a test service to ensure we can get to a
        // session variable we have set.
        final URL varWebUrl = new URL(url.replace("service",
            "ws/test/var/WEB_CLIENT_ADDR"));

        final HttpURLConnection varConnection = (HttpURLConnection) varWebUrl
            .openConnection();
        varConnection.setDoOutput(true);
        varConnection.addRequestProperty("Accept", "application/xml");
        putCookiesInRequest(varConnection, new URI(url));
        String line = null;
        BufferedReader varRd = null;
        StringBuilder varSb = new StringBuilder();
        try {
            varConnection.connect();
            varRd = new BufferedReader(new InputStreamReader(
                varConnection.getInputStream(), "UTF-8"));
            line = "";
            while ((line = varRd.readLine()) != null) {
                varSb.append(line);
            }
        }
        finally {
            try {
                if (varRd != null) varRd.close();
            }
            finally {
                varConnection.disconnect();
            }
        }
        // get back a variable we know we should be setting. WEB_CLIENT_ADDR
        assertEquals("127.0.0.1",
            varSb.toString().replace("<string>", "").replace("</string>", ""));

    }

    @Test
    public void testCheckWarehouseId() throws URISyntaxException, IOException {
        // Make a call to a test service to ensure we can get to a
        // session variable we have set.
        final URL wareWebUrl = new URL(url.replace("service", "ws/test/var/warehouse_id"));
        final HttpURLConnection warehouseConnection = (HttpURLConnection) wareWebUrl
            .openConnection();
        warehouseConnection.setDoOutput(true);
        warehouseConnection.addRequestProperty("Accept", "application/xml");
        putCookiesInRequest(warehouseConnection, new URI(url));
        BufferedReader warehouseReader = null;
        String line = null;
        StringBuilder varSb = new StringBuilder();
        try {
            warehouseConnection.connect();
            warehouseReader = new BufferedReader(new InputStreamReader(
                warehouseConnection.getInputStream(), "UTF-8"));
            line = "";
            while ((line = warehouseReader.readLine()) != null) {
                varSb.append(line);
            }
        }
        finally {
            try {
                if (warehouseReader != null) warehouseReader.close();
            }
            finally {
                warehouseConnection.disconnect();
            }
        }
        // get back a variable we set, warehouse_id
        assertEquals("1",
            varSb.toString().replace("<string>", "").replace("</string>", ""));

    }

    @Test
    public void testNotFoundException() throws IOException, URISyntaxException {
        // Make a call to a test service to ensure we can get to a
        // session variable we have set.
        final URL varWebUrl = new URL(url.replace("service",
            "ws/test/getObjectNotFound/object"));
        final HttpURLConnection varConnection = (HttpURLConnection) varWebUrl
            .openConnection();
        varConnection.setDoOutput(true);
        varConnection.addRequestProperty("Accept", "application/xml");
        putCookiesInRequest(varConnection, new URI(url));
        varConnection.connect();
        varConnection.disconnect();
        // we get a NotFoundException, make sure it's a 404 response code.
        assertEquals(404, varConnection.getResponseCode());
    }

    @Test
    public void testUniqueConstraintException() throws IOException, URISyntaxException {
        // Make a call to a test service to ensure we can get to a
        // session variable we have set.
        final URL varWebUrl = new URL(url.replace("service",
            "ws/test/getObjectConstraint/object"));
        final HttpURLConnection varConnection = (HttpURLConnection) varWebUrl
            .openConnection();
        varConnection.setDoOutput(true);
        varConnection.addRequestProperty("Accept", "application/xml");
        putCookiesInRequest(varConnection, new URI(url));
        varConnection.connect();
        varConnection.disconnect();
        // we get a UniqueConstraintException, make sure it's a 409 response
        // code.
        assertEquals(409, varConnection.getResponseCode());
    }

    @Test
    public void testTracing() throws IOException, URISyntaxException, ParseException, InterruptedException {
        SystemContext ctx = ServerUtils.globalContext();
        // Make a call to a test service to ensure we can get to a
        // session variable we have set.
        final URL varWebUrl = new URL(url.replace("service", "ws/admin/tasks"));
        final HttpURLConnection varConnection = (HttpURLConnection) varWebUrl
            .openConnection();
        varConnection.setDoOutput(true);
        varConnection.addRequestProperty("Accept", "application/xml");
        varConnection.addRequestProperty("moca-tracefile", "testlogfile.log");
        putCookiesInRequest(varConnection, new URI(url));
        File traceFile = null;
        try {
            varConnection.connect();
            varConnection.disconnect();
            assertEquals(200, varConnection.getResponseCode());
            traceFile = new File(ctx.getVariable("LESDIR"), "log/testlogfile.log");
            assertTrue("Trace file should've existed: " + traceFile.getAbsolutePath(),
                traceFile.exists());
        }
        finally {
        	if (traceFile != null && traceFile.exists()) {
        		int deleteAttemptCount = 0;
        		while (!traceFile.delete() && deleteAttemptCount < 1000) {
        			Thread.sleep(10);
        			deleteAttemptCount++;
        		}
        	assertFalse("Trace file should not exist deletion failed: " + traceFile.getAbsolutePath(),
        	    traceFile.exists());
        	}

        }

    }

    @Test
    public void testDateFormatJson() throws IOException, URISyntaxException,
            ParseException {
        // Make a call to a test service to ensure we can get to a
        // session variable we have set.
        final URL varWebUrl = new URL(url.replace("service", "") + "ws/test/getTestDate");
        final HttpURLConnection varConnection = (HttpURLConnection) varWebUrl
            .openConnection();
        varConnection.setRequestMethod("POST");
        varConnection.setRequestProperty("content-type", "application/json");
        varConnection.setDoOutput(true);
        varConnection.addRequestProperty("Accept", "application/json");
        putCookiesInRequest(varConnection, new URI(url));
        varConnection.connect();
        OutputStream dateOutputStream = null;
        BufferedReader rd = null;
        OutputStreamWriter dateOutputStreamWriter = null;
        String line = null;
        Date date = new Date();
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter pattern = DateTimeFormat.forPattern(
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(
            DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));
        final String data = "\"" + pattern.print(date.getTime()) + "\"";
        try {
            dateOutputStream = varConnection.getOutputStream();
            dateOutputStreamWriter = new OutputStreamWriter(dateOutputStream, "UTF-8");

            dateOutputStreamWriter.write(data);
            dateOutputStreamWriter.flush();

            rd = new BufferedReader(new InputStreamReader(varConnection.getInputStream(),
                "UTF-8"));

            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
        }
        finally {
            try {
                if (rd != null) {
                    rd.close();
                }
            }
            finally {
                try {
                    if (dateOutputStream != null) dateOutputStream.close();
                }
                finally {
                    if (dateOutputStreamWriter != null) dateOutputStreamWriter.close();
                }
            }
        }
        String returnedDate = sb.toString().replace("\"", "");
        Date dt = pattern.parseDateTime(returnedDate).toDate();
        assertEquals(Long.valueOf(date.getTime()), Long.valueOf(dt.getTime()));

    }

    @Test
    public void testDateFormatXml() throws IOException, URISyntaxException,
            ParseException {
        // Make a call to a test service to ensure we can get to a
        // session variable we have set.
        final URL varWebUrl = new URL(url.replace("service", "") + "ws/test/getTestDate");
        final HttpURLConnection varConnection = (HttpURLConnection) varWebUrl
            .openConnection();
        varConnection.setRequestMethod("POST");
        varConnection.setRequestProperty("content-type", "application/xml");
        varConnection.setDoOutput(true);
        varConnection.addRequestProperty("Accept", "application/xml");
        putCookiesInRequest(varConnection, new URI(url));
        varConnection.connect();
        OutputStream dateOutputStream = null;
        BufferedReader rd = null;
        OutputStreamWriter dateOutputStreamWriter = null;
        String line = null;
        Date date = new Date();
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter pattern = DateTimeFormat.forPattern(
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(
            DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));
        final String data = "<date>" + pattern.print(date.getTime()) + "</date>";
        try {
            dateOutputStream = varConnection.getOutputStream();
            dateOutputStreamWriter = new OutputStreamWriter(dateOutputStream, "UTF-8");

            dateOutputStreamWriter.write(data);
            dateOutputStreamWriter.flush();

            rd = new BufferedReader(new InputStreamReader(varConnection.getInputStream(),
                "UTF-8"));

            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
        }
        finally {
            try {
                if (rd != null) {
                    rd.close();
                }
            }
            finally {
                try {
                    if (dateOutputStream != null) dateOutputStream.close();
                }
                finally {
                    if (dateOutputStreamWriter != null) dateOutputStreamWriter.close();
                }
            }
        }
        String returnedDate = sb.toString().replace("<date>", "").replace("</date>", "");
        Date dt = pattern.parseDateTime(returnedDate).toDate();
        assertEquals(Long.valueOf(date.getTime()), Long.valueOf(dt.getTime()));
    }

    @Test
    public void testWarehouseIdRequestOverride() throws URISyntaxException, IOException {
        // Make a call to a test service to ensure we can get to a
        // session variable we have set.
        final URL wareWebUrl = new URL(url.replace("service", "ws/test/var/warehouse_id"));
        final HttpURLConnection warehouseConnection = (HttpURLConnection) wareWebUrl
            .openConnection();
        warehouseConnection.setDoOutput(true);
        warehouseConnection.addRequestProperty("Accept", "application/xml");
        warehouseConnection.addRequestProperty("moca-request",
            ResponseUtils.encodeHeader("warehouse_id=newId"));
        putCookiesInRequest(warehouseConnection, new URI(url));
        BufferedReader warehouseReader = null;
        String line = null;
        StringBuilder varSb = new StringBuilder();
        try {
            warehouseConnection.connect();
            warehouseReader = new BufferedReader(new InputStreamReader(
                warehouseConnection.getInputStream(), "UTF-8"));
            line = "";
            while ((line = warehouseReader.readLine()) != null) {
                varSb.append(line);
            }
        }
        finally {
            try {
                if (warehouseReader != null) warehouseReader.close();
            }
            finally {
                warehouseConnection.disconnect();
            }
        }
        // get back a variable we set, warehouse_id
        assertEquals("newId",
            varSb.toString().replace("<string>", "").replace("</string>", ""));

    }

    @Test
    public void testArgRequestOverride() throws URISyntaxException, IOException {
        // Make a call to a test service to ensure we can get to a
        // session variable we have set.
        final URL wareWebUrl = new URL(url.replace("service", "ws/test/var/extra_arg"));
        final HttpURLConnection warehouseConnection = (HttpURLConnection) wareWebUrl
            .openConnection();
        warehouseConnection.setDoOutput(true);
        warehouseConnection.addRequestProperty("Accept", "application/xml");
        warehouseConnection.addRequestProperty("moca-request",
            ResponseUtils.encodeHeader("warehouse_id=newId,extra_arg=newArg"));
        putCookiesInRequest(warehouseConnection, new URI(url));
        BufferedReader warehouseReader = null;
        String line = null;
        StringBuilder varSb = new StringBuilder();
        try {
            warehouseConnection.connect();
            warehouseReader = new BufferedReader(new InputStreamReader(
                warehouseConnection.getInputStream(), "UTF-8"));
            line = "";
            while ((line = warehouseReader.readLine()) != null) {
                varSb.append(line);
            }
        }
        finally {
            try {
                if (warehouseReader != null) warehouseReader.close();
            }
            finally {
                warehouseConnection.disconnect();
            }
        }
        // get back a variable we set, warehouse_id
        assertEquals("newArg",
            varSb.toString().replace("<string>", "").replace("</string>", ""));
    }

    @Test
    public void testMocaException() throws URISyntaxException, IOException {
        final URL endpointUrl = new URL(url.replace("service",
            "ws/test/getMocaException/512"));
        final HttpURLConnection serverConnection = (HttpURLConnection) endpointUrl
            .openConnection();
        putCookiesInRequest(serverConnection, new URI(url));
        String mocaStatus = null;
        try {
            serverConnection.connect();
            mocaStatus = serverConnection.getHeaderField("moca-status");
        }
        finally {
            serverConnection.disconnect();
        }
        // We get back our error code.
        assertEquals("512", mocaStatus);
    }
    
    @Test
    public void testAnotherMocaException() throws URISyntaxException,
            IOException {
        final URL endpointUrl = new URL(url.replace("service",
            "ws/test/getMocaException/523"));
        final HttpURLConnection serverConnection = (HttpURLConnection) endpointUrl
            .openConnection();
        putCookiesInRequest(serverConnection, new URI(url));
        String mocaStatus = null;
        try {
            serverConnection.connect();
            mocaStatus = serverConnection.getHeaderField("moca-status");
        }
        finally {
            serverConnection.disconnect();
        }

        // We get back our error code.
        assertEquals("523", mocaStatus);
    }
    
    @Test
    public void testNoMocaErrorCode() throws URISyntaxException, IOException {
        // Make a call to a test service to ensure we can get to a
        // session variable we have set.
        final URL endpointUrl = new URL(url.replace("service", "ws/test/noop"));
        final HttpURLConnection serverConnection = (HttpURLConnection) endpointUrl
            .openConnection();
        putCookiesInRequest(serverConnection, new URI(url));
        String mocaStatus = null;
        try {
            serverConnection.connect();
            mocaStatus = serverConnection.getHeaderField("moca-status");
        }
        finally {
            serverConnection.disconnect();
        }

        // We get back our error code.
        assertEquals(null, mocaStatus);
    }

    @Test
    public void testGetMadFactory() throws URISyntaxException, IOException,
            ClassNotFoundException {
        // Make a call to a test service to make sure we get back our present
        // instance's MadFactory.
        final URL webUrl = new URL(url.replace("service", "ws/test/getMadFactory"));
        final HttpURLConnection conn = (HttpURLConnection) webUrl.openConnection();
        conn.setDoOutput(true);
        conn.addRequestProperty("Accept", "application/xml");
        putCookiesInRequest(conn, new URI(url));
        BufferedReader reader = null;
        String line = null;
        StringBuilder response = new StringBuilder();
        try {
            conn.connect();
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),
                "UTF-8"));
            line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        finally {
            try {
                if (reader != null) reader.close();
            }
            finally {
                conn.disconnect();
            }
        }

        // Trim the XML off of the returned class name string...
        String className = response.toString().replace("<", "").replace("/>", "");
        // Make sure that the class name that is returned from the request is
        // assignable from MadFactory.class
        assertTrue(MadFactory.class.isAssignableFrom(Class.forName(className)));
    }

    private void putCookiesInRequest(HttpURLConnection varConnection, URI webURI)
            throws IOException {
        Map<String, List<String>> cookiesToSet = cookies.get(webURI,
            varConnection.getRequestProperties());
        if (cookiesToSet != null) {
            for (Map.Entry<String, List<String>> cookie : cookiesToSet.entrySet()) {
                for (String value : cookie.getValue()) {
                    varConnection.addRequestProperty(cookie.getKey(), value);
                }
            }
        }
    }
}
