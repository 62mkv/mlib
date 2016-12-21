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

package com.sam.moca.components.base;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sam.moca.EditableResults;
import com.sam.moca.FeatureNotImplementedException;
import com.sam.moca.MocaArgument;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaInterruptedException;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;

/**
 * This class handles http component requests
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class HttpService {

    /**
     * This will do an http request given the values provided
     * @param moca the Moca Context
     * @param url The url to send/receive the http request from
     * @param method The request method
     * @param headers
     * @param body
     * @param nameValuePairs
     * @param encoding The encoding to send the characters for the request
     * @throws HttpServiceException
     * @throws FeatureNotImplementedException This is thrown if a mode other
     *         than POST is used
     */
    public MocaResults doHttpRequest(MocaContext moca, String url, String method, 
            String headers, String body, String nameValuePairs, String encoding) 
            throws MocaException {
        URL requestURL;
        try {
            requestURL = new URL(url);
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            throw new HttpServiceException("URL provided cannot be resolved", e);
        }
        
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) requestURL.openConnection();
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new HttpServiceException("Error opening connection to url " + 
                    requestURL.getPath(), e);
        }
        
        try {
            if (method == null) {
                _logger.debug("Request method not provided defaulting to " + 
                        _defaultRequestMethod);
                conn.setRequestMethod(_defaultRequestMethod);
            }
            else {
                // We don't support any request methods but post
                if (!method.equalsIgnoreCase(_defaultRequestMethod)) {
                    throw new FeatureNotImplementedException("Request method " +
                    		method.toUpperCase() + " is not supported.  " +
                    		"Only " + _defaultRequestMethod + " is supported");
                }
                // It requires all methods to be in upper case
                conn.setRequestMethod(method.toUpperCase());
            }
        }
        catch(ProtocolException e) {
            e.printStackTrace();
            throw new HttpServiceException("Error setting request method", e);
        }
        
        conn.setDoOutput(true);
        boolean contentTypeProvided = false;
        
        // Now lets go through the headers
        if (headers != null) {
            String[] splitHeaders = headers.split("&");
            
            for (String headerNameValue : splitHeaders) {
                int firstColon = headerNameValue.indexOf(':');
                // Make sure a colon exists and that it isn't the last character
                if (firstColon != -1 && firstColon < headerNameValue.length() - 1) {
                    String key = headerNameValue.substring(0, firstColon);
                    String value = headerNameValue.substring(firstColon + 1);
                    conn.addRequestProperty(key, value);
                    
                    if (key.equalsIgnoreCase("content-type")) {
                        contentTypeProvided = true;
                    }
                }
            }
        }
        
        // TODO maybe make the user agent information better
        conn.addRequestProperty("User-Agent", "MOCA\\1.1");
        
        // If the content wasn't provided then we have to see if we can pick
        // one that makes sense
        if (!contentTypeProvided) {
            // If body is provided set it to simple text
            if (body != null && body.trim().length() > 0) {
                conn.addRequestProperty("content-type", "text/plain");
            }
            // Else set it to url encoded
            else {
                conn.addRequestProperty("content-type", 
                        "application/x-www-form-urlencoded");
            }
        }
        
        BufferedWriter queryUrl;
        try {
            encoding = encoding != null ? encoding : "UTF-8";
            OutputStream formStream = conn.getOutputStream();
            queryUrl = new BufferedWriter(new OutputStreamWriter(
                formStream, encoding));
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new HttpServiceException("Could not get stream from http" +
            		" connection " + url + " to write to ", e);
        }
        
        try {
            // If the body is provided use it over every thing else
            if (body != null && body.trim().length() > 0) {
                queryUrl.write(body);
                _logger.debug("Wrote: " + body);
            }
            else if (nameValuePairs != null && nameValuePairs.trim().length() > 0) {
                String[] splitNameValuePairs = nameValuePairs.split("&");
                boolean firstPass = true;
                
                for (String nameValue : splitNameValuePairs) {
                    // If it is not the first pass we want to put an ampersand
                    if (!firstPass) {
                        queryUrl.write('&');   
                    }
                    
                    StringBuilder parameter = new StringBuilder();
                    String key;
                    String value;
                    int firstEqual = nameValue.indexOf('=');
                    // If equal exists or is last character than just use
                    // the nameValue
                    if (firstEqual == -1 || firstEqual == (nameValue.length() - 1)) {
                        // Remove any equals which could be in last place
                        key = nameValue.replace("=", "");
                        value = "";
                    }
                    // If equals exists take each side of it
                    else {
                        key = nameValue.substring(0, firstEqual);
                        value = nameValue.substring(firstEqual + 1);
                    }
                    
                    parameter.append(URLEncoder.encode(key, "UTF-8"));
                    parameter.append('=');
                    parameter.append(URLEncoder.encode(value, "UTF-8"));
                    
                    queryUrl.write(parameter.toString());
                    _logger.debug("Wrote :" + parameter.toString());
                    
                    firstPass = false;
                }
            }
            // Else we default to all the arguments on the stack
            else {
                MocaArgument[] args = moca.getArgs();
                boolean firstPass = true;
                
                // For each argument pass in the value
                for (MocaArgument arg : args) {
                    // If the type is unknown, results, generic, or binary
                    // we can't support that
                    if (Arrays.asList(
                            new MocaType[] {
                                    MocaType.UNKNOWN, MocaType.RESULTS,
                                    MocaType.GENERIC, MocaType.BINARY,
                                    MocaType.OBJECT
                            }).contains(arg.getType())) {
                        _logger.debug("Can't handle data type (" + 
                                arg.getType() + ") for arg [" + arg.getName() + 
                                "]");
                        continue;
                    }
                    
                    // If it is not the first pass we want to put an ampersand
                    if (!firstPass) {
                        queryUrl.write('&');   
                    }
                    
                    StringBuilder parameter = new StringBuilder(); 
                    
                    parameter.append(URLEncoder.encode(arg.getName(), "UTF-8"));
                    parameter.append('=');
                    
                    // If the value is provided write it to the stream
                    if (arg.getValue() != null) {
                        String valueToWrite = arg.getValue().toString();
                        parameter.append(URLEncoder.encode(valueToWrite, "UTF-8"));
                    }
                    
                    queryUrl.write(parameter.toString());
                    _logger.debug("Wrote :" + parameter.toString());
                    
                    firstPass = false;
                }
            }
            
            // Now flush all the data across
            queryUrl.flush();
            
            EditableResults results = moca.newResults();
            
            results.addColumn("status", MocaType.INTEGER);
            results.addColumn("reason", MocaType.STRING);
            results.addColumn("body", MocaType.STRING);
            results.addColumn("content-type", MocaType.STRING);
            
            results.addRow();
            
            // Wait for our response
            int httpStatus = conn.getResponseCode();
            
            if (httpStatus != HttpURLConnection.HTTP_OK) {
                results.setIntValue("status", httpStatus);
                results.setStringValue("reason", conn.getResponseMessage());
                results.setStringValue("content-type", conn.getContentType());
                
                return results;
            }
            
            BufferedReader resultReader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"));
            
            try {
                
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = resultReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                
                results.setIntValue("status", httpStatus);
                results.setStringValue("reason", conn.getResponseMessage());
                results.setStringValue("body", stringBuilder.toString());
                results.setStringValue("content-type", conn.getContentType());
                
                return results;
            }
            finally {
                // Close the result stream
                try {
                    resultReader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    _logger.warn("Could not close connection to web server at " +
                                url, e);
                }
            }
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new HttpServiceException("Error communicating to http" +
            		" connection " + url, e);
        }
        finally {
            if (queryUrl != null) {
                try {
                    queryUrl.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    _logger.warn("Could not close connection to web server at " +
                    		url, e);
                }
            }
        }
    }
    
    private static final String _defaultRequestMethod = "POST";
    
    Logger _logger = LogManager.getLogger(HttpService.class);
}
