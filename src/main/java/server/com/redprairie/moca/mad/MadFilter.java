/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.util.MD5Utils;
import com.redprairie.moca.web.console.Authentication;
import com.redprairie.util.Base64;

/**
 * A servlet filter which uses the same login mechanism as JMX
 * 
 * Copyright (c) 2012 RedPrairie Corporation All Rights Reserved
 * 
 * @author klucas
 */
public class MadFilter implements Filter {
    
    public static final String SESSION_COOKIE_NAME = "wsJmxSession";
    
    // @see javax.servlet.Filter#destroy()
    @Override
    public void destroy() {

    }

    // @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
    // javax.servlet.ServletResponse, javax.servlet.FilterChain)
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain filterChain) throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest)
                || !(response instanceof HttpServletResponse)) {
            throw new ServletException("AuthenticationFilter only works with "
                    + "http based servlet request/response objects");
        }

        HttpServletRequest hRequest = ((HttpServletRequest) request);
        HttpServletResponse hResponse = ((HttpServletResponse) response);

        boolean isAuthorized = _authHandler.preCheckAuthentication(hRequest);

        if (!isAuthorized) {
            final String basicHeader = hRequest.getHeader(AUTH_HEADER);
            if (basicHeader != null && basicHeader.startsWith(BASIC_HEADER_PREFIX)) {
                String base64Login = basicHeader.substring(BASIC_HEADER_PREFIX.length());

                String login = new String(Base64.decode(base64Login), "UTF-8");

                String[] loginFields = login.split(":");

                if (loginFields.length != 2) {
                    _logger.trace("Invalid authorization header.");
                }
                else {
                    final String user = loginFields[0];
                    final String password = loginFields[1];
                    try {
                        isAuthorized = _authHandler.authenticate(hRequest, hResponse, user, password);
                    }
                    catch (MocaException e) {
                        _logger.warn("Unable to authorize JMX webservice request for user [{}] with error: {}", user, e.getMessage());
                    }
                }
            }
            else {
                _logger.trace("Invalid authorization header.");
            }
        }

        if (!isAuthorized) {
            hResponse.setHeader("WWW-Authenticate",
                "Basic realm=\"MOCA JMX Web Service\"");
            hResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        else {
            filterChain.doFilter(request, response);
        }
    }

    // @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        setSystemContext(ServerUtils.globalContext());
    }

    /**
     * Set the SystemContext. Used for testing.
     * 
     * @param context
     */
    void setSystemContext(SystemContext context) {
        _context = context;
        ServerContextFactory contextFactory = (ServerContextFactory) _context.getAttribute(ServerContextFactory.class.getName());
        if (contextFactory != null) {
            _authHandler = new MocaAuthenticationHandler(_context, contextFactory);
        }
        else {
            // Means we're running as something that isn't the MOCA server itself
            _authHandler = new AdminHashAuthenticationHandler(_context);
        }
    }
    
    /**
     * Handles JMX webservice authentication
     */
    private interface AuthenticationHandler {
        
        /**
         * A precheck for authentication before the user/password is looked for. This can be
         * used for validating session cookies on the request
         * @param request The request
         * @return Whether or not the request is already authenticated
         * @throws IOException
         */
        boolean preCheckAuthentication(HttpServletRequest request) throws IOException;
        
        /**
         * Authenticates the request given the user/password
         * @param request  The request
         * @param response The response object to optionally modify
         * @param user     The user
         * @param password The password
         * @return
         * @throws IOException
         * @throws MocaException
         */
        boolean authenticate(HttpServletRequest request,
                             HttpServletResponse response,
                             String user, String password) throws IOException, MocaException;
        
    }
    
    /**
     * This authentication handler should be used if the MadFilter is actually being used
     * as part of the MOCA server. It uses the same authentication as the MOCA console so you can
     * either login in as the admin-user specified in the MOCA registry or via a standard MCS
     * level user who has console admin or console read only access. Additionally, this supports
     * setting a cookie after authentication is successful for subsequent requests.
     */
    private static class MocaAuthenticationHandler implements AuthenticationHandler {
        
        MocaAuthenticationHandler(SystemContext sysContext, ServerContextFactory contextFactory) {
            _sysContext = sysContext;
            _contextFactory = contextFactory;
        }

        @Override
        public boolean preCheckAuthentication(HttpServletRequest request) throws IOException {
            return Authentication.handleAuthenticationRequest(request, SESSION_COOKIE_NAME);
        }

        @Override
        public boolean authenticate(HttpServletRequest request,
                                    HttpServletResponse response,
                                    String user, String password) throws IOException, MocaException {
            final String sessionId = Authentication.loginFromHttpRequest(
                request, _sysContext, _contextFactory,
                user, password, Authentication.RequestType.WS_JMX);
            Authentication.setupCookie(sessionId, request, response, SESSION_COOKIE_NAME, request.getContextPath());
            return true;
        }
        
        private final SystemContext _sysContext;
        private final ServerContextFactory _contextFactory;
    }
    
    /**
     * This authentication handler should be used if the MadFilter is used by another product group but
     * the product using this is not inside the MOCA server itself e.g. this is used by the MTF server for
     * WM. This assumes that the task at least knows about the location of the MOCA registry which it then
     * uses to validate against the MOCA registry's admin-password.
     * In the future this should go away and they should provide their own implementation.
     */
    private static class AdminHashAuthenticationHandler implements AuthenticationHandler {
        
        AdminHashAuthenticationHandler(SystemContext sysContext) {
            _sysContext = sysContext;
        }

        @Override
        public boolean preCheckAuthentication(HttpServletRequest request)
                throws IOException {
            return false;
        }

        @Override
        public boolean authenticate(HttpServletRequest request,
                                 HttpServletResponse response, String user,
                                 String password) throws IOException,
                MocaException {
            final String adminPassword = _sysContext.getConfigurationElement(MocaRegistry.REGKEY_SECURITY_ADMIN_PASSWORD);
            if (adminPassword == null) {
                _logger.warn("Attempted to login to JMX webservice but admin-password is not specified in the MOCA registry");
                return false;
            }
            else {
                return MD5Utils.validateHashedPassword(password, adminPassword);
            }
        }

        private final SystemContext _sysContext;
    }

    private SystemContext _context;
    private AuthenticationHandler _authHandler;

    public static final String AUTH_HEADER = "Authorization";
    private final String BASIC_HEADER_PREFIX = "Basic ";
    private static final Logger _logger = LogManager.getLogger(MadFilter.class);

}
