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

package com.sam.moca.web.console;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class AuthenticationFilter implements Filter {
    
    public void init(FilterConfig filterConfig) {
        _filterConfig = filterConfig;
    }

    public void destroy() {
        _filterConfig = null;
    }

    public void doFilter(ServletRequest request, ServletResponse response, 
        FilterChain chain) throws IOException, ServletException {
        
        if (_filterConfig == null) {
            _logger.error("No filter configuration specified.");
            return;
        }
        
        if (!(request instanceof HttpServletRequest) || 
                !(response instanceof HttpServletResponse)) {
            throw new ServletException("AuthenticationFilter only works with " +
                    "http based servlet request/response objects");
        }
        HttpServletRequest hRequest = ((HttpServletRequest) request);
        HttpServletResponse hResponse = ((HttpServletResponse) response);

        boolean isAuthenticated = false;
        try {
            isAuthenticated = Authentication.handleAuthenticationRequest(hRequest);
        }
        catch (Exception e) {
            _logger.error("An error occurred while trying to determine if this console session is authenticated", e);
        }
        
        String contextPath = hRequest.getContextPath();
        final String urlEncodeName = "UTF-8";
        
        if (contextPath == null) {
            contextPath = "";
        }
        
        String queryStr = hRequest.getQueryString();
        String pathInfo = hRequest.getPathInfo(); 
        String requestPath = hRequest.getServletPath() 
                           + (pathInfo == null ? "" : pathInfo)
                           + (queryStr == null ? "" : "?" + queryStr);
        
        if (!isAuthenticated) {
            if (!isSecuredPageRequest(hRequest)) {
                // non-secure resources don't need authentication
                // MUST HAVE RETURN STATEMENT SO THAT WE DONT KEEP FILTERING BELOW
                chain.doFilter(request, response);
                return;
            }
            
            if (isDataRequest(hRequest)) {
                // data requests should set header so that RPWEB stash code can understand it
                hResponse.setHeader(STASH_AUTH_RESP_HEADER, "false");
                return;
            }
            else {
                // otherwise just redirect to login page
                String redirectUrl = String.format("%s%s?redirectUrl=%s%s",
                    contextPath,
                    LOGIN_URL,
                    URLEncoder.encode(contextPath, urlEncodeName),
                    URLEncoder.encode(requestPath, urlEncodeName));
                
                _logger.debug("Redirecting unauthenticated request to: " + redirectUrl);
                hResponse.sendRedirect(redirectUrl);
                return;
            }
        }
        else {
            if (request.getParameter(REDIRECT_URL) != null) {
                // user has authenticated and is waiting to be redirected to their final destination
                String redirectUrl = request.getParameter(REDIRECT_URL);
                _logger.debug("Redirecting authenticated request to: " + redirectUrl);
                hResponse.sendRedirect(redirectUrl);
                return;
            }
            else {
                // access the resource
                chain.doFilter(request, response);
                return;
            }
        }
    }
    
    /**
     * Determines if the request is a data request or an action.
     * We need to know this so we can send a response that stash code will understand.
     * @param hRequest The servlet request object.
     * @return true if data request, false if not.
     */
    private boolean isDataRequest(HttpServletRequest hRequest) {
        final String queryPath = hRequest.getServletPath();
        final Map<String,String[]> paramMap = hRequest.getParameterMap();
        final String[] mCalls = paramMap.get("m");
        
        List<String> mParams = Collections.emptyList();
        if (mCalls != null) {
            mParams = Arrays.asList(mCalls);
        }

        return queryPath.startsWith("/console") 
                && mParams.size() > 0 
                && !mParams.contains("login")
                && !mParams.contains("logout");
    }
    
    /**
     * Determines if the current page request is a considered secured page or
     * public page.
     * 
     * @param hRequest The servlet request object.
     * @return True if secure, false if not.
     */
    private boolean isSecuredPageRequest(HttpServletRequest hRequest) {
        final String queryStr = hRequest.getQueryString();
        final String servletPath = hRequest.getServletPath();
        final String requestPath = servletPath + (queryStr == null ? "" : "?" + queryStr);

        if (servletPath.equals(LOGIN_URL) ||
                requestPath.equals(LOGIN_SERVICE_URL) ||
                // The login js is required to actually render login page
                requestPath.equals(LOGIN_JS) ||
                // Refs info is needed for login purposes
                requestPath.startsWith(REFS_INFO)) {
            return false;
        }

        return true;
    }

    private FilterConfig _filterConfig;
    
    private static final String STASH_AUTH_RESP_HEADER ="authenticated";
    private static final String LOGIN_URL = "/console/login.do";
    private static final String LOGIN_JS = "/console/login.js";
    private static final String REFS_INFO = "/console/refs";
    private static final String LOGIN_SERVICE_URL = "/console?m=login";
    private static final String REDIRECT_URL = "redirectUrl";
    
    private static final Logger _logger = LogManager.getLogger(AuthenticationFilter.class); 
}
