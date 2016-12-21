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

import java.io.IOException;

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
import org.eclipse.jetty.server.Response;

public final class AuthenticationFilter implements Filter {
    
    public void init(FilterConfig filterConfig) {
        _filterConfig = filterConfig;
    }

    public void destroy() {
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
        
        // If wasn't authenticated then lock them out
        if (isSecuredPageRequest(hRequest) && !isAuthenticated) {
            hResponse.sendError(Response.SC_UNAUTHORIZED);
        }
        else {
            chain.doFilter(request, response);
        }
    }

    /**
     * Determines if the current page request is a considered secured page or
     * public page.
     * 
     * @param hRequest The servlet request object.
     * @return True if secure, false if not.
     */
    private boolean isSecuredPageRequest(HttpServletRequest hRequest) {
        String pathInfo = hRequest.getPathInfo();
        _logger.debug("WS request: " + pathInfo);
        // The login page can be accessed
        if (LOGIN_URL.equals(pathInfo)) {
            return false;
        }

        return true;
    }

    private FilterConfig _filterConfig;
    
    private static final String LOGIN_URL = "/login";
    
    private static final Logger _logger = LogManager.getLogger(AuthenticationFilter.class); 
}
