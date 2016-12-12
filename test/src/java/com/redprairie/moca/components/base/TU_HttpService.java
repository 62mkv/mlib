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

package com.redprairie.moca.components.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.client.TU_HttpConnection;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.util.MocaUtils;

import static org.junit.Assert.assertFalse;

/**
 * This class is to test the HttpService methods.  This test uses a hard-coded
 * port and will fail if anyone is listening on that port
 * 
 * @see #_PORT
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_HttpService {
    
    /**
     * This is to be ran after all the tests
     * @throws SystemConfigurationException 
     */
    @BeforeClass public static void beforeTests() throws SystemConfigurationException {
        _server = new Server(_PORT);
        ServletContextHandler root = new ServletContextHandler(_server,"/");
        
        root.addServlet(new ServletHolder(new HttpTestServlet()), 
                _PATH_SPEC);
        
        try {
            _server.start();
        }
        catch (Exception e) {
            e.printStackTrace();
            junit.framework.Assert.fail("Server failed to start :" + e);
        }
        
        ServerUtils.setupDaemonContext(TU_HttpService.class.getName(), true);
    }
    
    /**
     * This is to be invoked after all the tests are done
     */
    @AfterClass public static void afterTests() {
        try {
            _server.stop();
        }
        catch (Exception e) {
            e.printStackTrace();
            junit.framework.Assert.fail("Server failed to stop :" + e);
        }
    }
    
    /**
     * This is to reset the static variables as well as set the body
     * @param newBody The new body value
     */
    private static void resetStaticStuff(String newBody) {
        _headers = new HashMap<String, String>();
        _parameters = new HashMap<String, String>();
        _body = newBody;
    }
    
    /**
     * This is ran before every test
     * @throws SystemConfigurationException 
     */
    @Before public void initialize() throws SystemConfigurationException {
        resetStaticStuff("");
        
        _moca = MocaUtils.currentContext();
    }
    
    /**
     * This is ran after every test
     * @throws MocaException
     */
    @After public void cleanup() throws MocaException {
        _moca.rollback();
    }

    /**
     * This is to test do http request with a body
     * @throws MocaException 
     * @throws Exception
     */
    @Test public void testDoHttpRequestWithBody() throws MocaException {
        
        resetStaticStuff("You should receive this\n" +
                "If not we will fail\n" +
                "This is the last line");
        
        MocaResults res = _moca.executeCommand(
                    "do http request" +
                    "  where url = 'http://localhost:" + _PORT + _PATH_SPEC + "'" +
                    "    and body = '" + _body + "'");
        
        RowIterator rowIter = res.getRows();
        
        junit.framework.Assert.assertTrue(rowIter.next());
        
        junit.framework.Assert.assertEquals("There was an error", 200, rowIter
                .getInt("status"));
        junit.framework.Assert.assertEquals("There was an error", "OK", rowIter
                .getString("reason"));
        String returnBody = rowIter.getString("body");
        // Check to see if the return was success value
        if (!_SUCCESS.equals(returnBody)) {
            junit.framework.Assert.fail(returnBody);
        }
        junit.framework.Assert.assertEquals("The content type doesn't match",
                _CONTENT_TYPE, rowIter.getString("content-type"));
    }

    /**
     * This is to test do http request with a name value pair
     * @throws Exception
     */
    @Test public void testDoHttpRequestWithValuePairs() throws Exception {
        
        _parameters.put("name", "Henry + Doug");
        _parameters.put("commit", "true");
        _parameters.put("foo", "bar");
        
        StringBuilder stringBuilder = new StringBuilder();
        
        // Now put all the entries as a string
        for (Entry<String, String> entry : _parameters.entrySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append('&');
            }
            stringBuilder.append(entry.getKey());
            stringBuilder.append('=');
            stringBuilder.append(entry.getValue());
        }
        
        MocaResults res = _moca.executeCommand(
                    "do http request" +
                    "  where url = 'http://localhost:" + _PORT + _PATH_SPEC + "'" +
                    "    and namevaluepairs = '" + stringBuilder.toString() + "'");
        
        RowIterator rowIter = res.getRows();
        
        junit.framework.Assert.assertTrue(rowIter.next());
        
        junit.framework.Assert.assertEquals("There was an error", 200, rowIter
                .getInt("status"));
        junit.framework.Assert.assertEquals("There was an error", "OK", rowIter
                .getString("reason"));
        String returnBody = rowIter.getString("body");
        // Check to see if the return was success value
        if (!_SUCCESS.equals(returnBody)) {
            junit.framework.Assert.fail(returnBody);
        }
        junit.framework.Assert.assertEquals("The content type doesn't match",
                _CONTENT_TYPE, rowIter.getString("content-type"));
    }
    
    /**
     * This is to test do http request with a where clause
     * @throws Exception
     */
    @Test public void testDoHttpRequestWithWhereClause() throws Exception {
        
        _parameters.put("name", "Henry + Doug");
        _parameters.put("commit", "true");
        _parameters.put("foo", "bar");
        
        StringBuilder whereClause = new StringBuilder();
        
        // Add the all the values to the where clause
        for (Entry<String, String> entry : _parameters.entrySet()) {
            whereClause.append(" and ");
            whereClause.append(entry.getKey());
            whereClause.append("='");
            whereClause.append(entry.getValue());
            whereClause.append('\'');
        }
        
        MocaResults res = _moca.executeCommand(
                    "do http request" +
                    "  where url = 'http://localhost:" + _PORT + _PATH_SPEC + "'" +
                    whereClause.toString());
        
        RowIterator rowIter = res.getRows();
        
        junit.framework.Assert.assertTrue(rowIter.next());
        
        junit.framework.Assert.assertEquals("There was an error", 200, rowIter
                .getInt("status"));
        junit.framework.Assert.assertEquals("There was an error", "OK", rowIter
                .getString("reason"));
        String returnBody = rowIter.getString("body");
        // Check to see if the return was success value
        if (!_SUCCESS.equals(returnBody)) {
            junit.framework.Assert.fail(returnBody);
        }
        junit.framework.Assert.assertEquals("The content type doesn't match",
                _CONTENT_TYPE, rowIter.getString("content-type"));
    }
    
    /**
     * This is to test to make sure that header information is properly passed
     * @throws MocaException 
     */
    @Test public void testDoHttpRequestWithHeaders() throws MocaException {
        _headers.put("name", "Henry + Doug");
        _headers.put("commit", "true");
        _headers.put("foo", "bar");
        
        StringBuilder stringBuilder = new StringBuilder();
        
        // Now put all the entries as a string
        for (Entry<String, String> entry : _headers.entrySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append('&');
            }
            stringBuilder.append(entry.getKey());
            stringBuilder.append(':');
            stringBuilder.append(entry.getValue());
        }
        
        MocaResults res = _moca.executeCommand(
                    "do http request" +
                    "  where url = 'http://localhost:" + _PORT + _PATH_SPEC + "'" +
                    "    and headers = '" + stringBuilder.toString() + "'");
        
        RowIterator rowIter = res.getRows();
        
        junit.framework.Assert.assertTrue(rowIter.next());
        
        junit.framework.Assert.assertEquals("There was an error", 200, rowIter
                .getInt("status"));
        junit.framework.Assert.assertEquals("There was an error", "OK", rowIter
                .getString("reason"));
        String returnBody = rowIter.getString("body");
        // Check to see if the return was success value
        if (!_SUCCESS.equals(returnBody)) {
            junit.framework.Assert.fail(returnBody);
        }
        junit.framework.Assert.assertEquals("The content type doesn't match",
                _CONTENT_TYPE, rowIter.getString("content-type"));
    }
    
    @Test 
    public void testDoHttpRequestWrongLocation() throws MocaException {
        
        MocaResults res = _moca.executeCommand(
                    "do http request" +
                    "  where url = 'http://localhost:" + _PORT + _PATH_SPEC + "/wrong'");
        
        RowIterator rowIter = res.getRows();
        
        junit.framework.Assert.assertTrue(rowIter.next());
        
        junit.framework.Assert.assertEquals("There was an error", 404, rowIter
                .getInt("status"));
        junit.framework.Assert.assertEquals("There was an error", "Not Found", rowIter
                .getString("reason"));
        
        assertFalse(rowIter.next());
    }
    
    /**
     * This is just a test servlet to make sure that data is getting passed
     * correctly to it
     * 
     * <b><pre>
     * Copyright (c) 2009 RedPrairie Corporation
     * All Rights Reserved
     * </pre></b>
     * 
     * @author wburns
     * @version $Revision$
     */
    private static class HttpTestServlet extends HttpServlet {
        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp)
                throws ServletException, IOException {
            StringBuilder errorReason = new StringBuilder();
            
            // If we sent headers make sure they are present
            if (_headers.size() > 0) {
                for (Entry<String, String> entry : _headers.entrySet()) {
                    String headerValue = req.getHeader(entry.getKey());
                    
                    // If the value doesn't match the header we wanted make sure
                    // to put the problem in the string
                    if (!entry.getValue().equals(headerValue)) {
                        errorReason.append("Header Mismatch for " + 
                                entry.getKey() + ": Expect[" + entry.getValue() + 
                                "] Got[" + headerValue + "] ||");
                    }
                }
            }
            
            Map<String, String[]> parameters = req.getParameterMap();
            
            // If we should check the body then look at it first
            if (_body.trim().length() > 0) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(req.getInputStream(), "UTF-8"));
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    
                    // Read in until we get to the end of the line
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (stringBuilder.length() > 0) {
                            stringBuilder.append('\n');
                        }
                        stringBuilder.append(line);
                    }
                    
                    // Now compare the values to see if they are the same
                    if (!stringBuilder.toString().equals(_body)) {
                        errorReason.append("Stream read differs "
                                + "from desired: Expect["
                                + stringBuilder.toString() + "] Got[" + _body
                                + "]");
                    }
                }
                finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            // If we sent parameters make sure to check them
            else if (_parameters.size() > 0) {
                
                // First check to make sure that the parameter sizes match
                if (_parameters.size() != parameters.size()) {
                    errorReason.append("There was a mismatch in " +
                    		"the parameter count: Expect#[" + _parameters.size()
                                    + "] Got#[" + parameters.size() + "] ||");
                }
                
                for (Entry<String, String> entry : _parameters.entrySet()) {
                    String parameterValue  = req.getParameter(entry.getKey());
                    
                    if (parameterValue != null) {
                        // If the value doesn't match the parameter we wanted make 
                        // sure to put the problem in the string
                        if (!parameterValue.equals(entry.getValue())) {
                            errorReason.append("Parameter Mismatch for "
                                            + entry.getKey() + ": Expect["
                                            + parameterValue + "] Got["
                                            + entry.getValue() + "] ||");
                        }   
                    }
                    else {
                        errorReason.append("Parameter was not passed [" + 
                                entry.getKey() + "] ||");
                    }
                }
            }
            
            // Now send the response
            resp.setContentType(_CONTENT_TYPE);
            
            PrintWriter writer = resp.getWriter();
            
            // If there is an error then display that, else do success
            writer.write(errorReason.length() > 0 ? errorReason.toString() : 
                _SUCCESS);
        }
        
        private static final long serialVersionUID = 5336431887693621611L;
    }

    protected MocaContext _moca;
    
    private static Map<String, String> _parameters;
    private static Map<String, String> _headers;
    private static String _body;
    
    private static Server _server;
    private final static int _PORT = 4496;
    private final static String _SUCCESS = "This worked";
    private final static String _CONTENT_TYPE = "application/xml; charset=UTF-8";
    private final static String _PATH_SPEC = "/" + TU_HttpConnection.class.getCanonicalName();
}
