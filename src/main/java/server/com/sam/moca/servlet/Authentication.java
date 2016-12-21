/*
 *  $URL$
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

package com.sam.moca.servlet;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sam.moca.MocaException;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.exec.SystemContext;
import com.sam.moca.server.session.MocaSessionManager;
import com.sam.moca.server.session.SessionToken;

public final class Authentication {
 
    private Authentication() {
    }
    
    public static boolean handleAuthenticationRequest(HttpServletRequest request)
            throws MocaException, UnsupportedEncodingException {
        
        // Get the client's session id cookie.
        Cookie sessionIdCookie = getCookie(request, _cookieName);
        if (sessionIdCookie == null) {
            _logger.debug("A console session id cookie was not found on the client");
            return false;
        }
        
        // Get the session id from the client's session id cookie.
        String encodedSessionId = sessionIdCookie.getValue();
        if (encodedSessionId == null) {
            _logger.debug("A console session id was not found in the session id cookie");
            return false;
        }
        
        // Decode the session id.
        String sessionId = URLDecoder.decode(encodedSessionId, URL_ENCODING);

        // Get the security domain for this environment and put it into a trusted domain list.
        SystemContext context = ServerUtils.globalContext();
        MocaSessionManager sessionManager = (MocaSessionManager) context.getAttribute(
            MocaSessionManager.class.getName());
        
        // Validate the session id.
        SessionToken authToken = sessionManager.validate(sessionId); 
        if (authToken == null) {
            _logger.debug("WebService session id validation failed");
            return false;
        }
        
        request.setAttribute("moca.AuthToken", authToken);

        _logger.debug("WebService session id validation successful");

        return true;
    }
    
    public static void handleLogin(String sessionId, HttpServletRequest request,
        HttpServletResponse response)
            throws UnsupportedEncodingException {
        String encodedSessionId = URLEncoder.encode(sessionId, URL_ENCODING);
        
        // Create a new session id cookie.
        Cookie cookie = new Cookie(_cookieName, encodedSessionId);
        // We don't set a path on the cookie since we don't know all the
        // context paths that people will deploy
        cookie.setPath(_pathName);
 
        // Set the cookie on the browser.
        response.addCookie(cookie);
    }

    public static void handleLogout(HttpServletRequest request, HttpServletResponse response) { 
        // Create a new session id cookie to force it to be removed on the browser.
        Cookie cookie = new Cookie(_cookieName, null);
        cookie.setPath(_pathName);
        cookie.setMaxAge(0);
        
        // Set the cookie on the browser to force its removal.
        response.addCookie(cookie);
    }
    
    private static Cookie getCookie(HttpServletRequest request, String name) {

        Cookie[] cookies = request.getCookies();

        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie;
                }
            }
        }
        return null;
    }
    
    private static final String URL_ENCODING = "UTF-8";
    
    private static final String _pathName = "/";
    private static final String _cookieName = "MOCA-WS-SESSIONKEY";
    
    private static final Logger _logger = LogManager.getLogger(Authentication.class);
}