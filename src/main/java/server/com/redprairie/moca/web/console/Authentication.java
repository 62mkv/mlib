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

package com.redprairie.moca.web.console;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaConstants;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.client.LoginFailedException;
import com.redprairie.moca.client.NotAuthorizedException;
import com.redprairie.moca.components.security.AuthService;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.LocalSessionContext;
import com.redprairie.moca.server.exec.RequestContext;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SessionContext;
import com.redprairie.moca.server.exec.SessionType;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.session.MocaSessionManager;
import com.redprairie.moca.server.session.SessionData;
import com.redprairie.moca.server.session.SessionToken;
import com.redprairie.moca.util.MD5Utils;
import com.redprairie.util.ArgCheck;

public final class Authentication {
 
    public static enum Role { CONSOLE_ADMIN, CONSOLE_READ, NO_CONSOLE_ACCESS };
    
    /**
     * Represents the different authentication request types
     */
    public static enum RequestType {
        CONSOLE ("consoleLogin"),
        JMX ("jmxLogin"),
        WS_JMX ("jmxWsLogin");
        
        RequestType(String sessionPrefix) {
            _sessionPrefix = sessionPrefix;
        }
        
        private final String _sessionPrefix;
    };
    
    private Authentication() {
    }
    
    /**
     * Logins from a HTTP request validating the the user is an admin user. This will either
     * check if the given user is the admin-user as specified in the MOCA registry otherwise it will
     * use "login user" to authenticate the user and verify they have console admin privileges.
     * @param request         The http request
     * @param systemContext   The system context
     * @param contextFactory  The server context factory to bootstrap the request
     * @param usrId           The user ID to login with
     * @param password        The password to login with
     * @param requestType     The authentication request type
     * @return                The generated session ID
     * @throws MocaException  On authentication failure
     */
    public static String loginFromHttpRequest(HttpServletRequest request,
                                              SystemContext systemContext, ServerContextFactory contextFactory,
                                              String usrId, String password, RequestType requestType) throws MocaException {
        return login(
            systemContext, contextFactory,
            usrId, password, requestType,
            request.getRemoteAddr());
    }
    
    /**
     * Logins from a HTTP request validating the the user is an admin user. This will either
     * check if the given user is the admin-user as specified in the MOCA registry otherwise it will
     * use "login user" to authenticate the user and verify they have console admin privileges.
     * @param systemContext   The system context
     * @param contextFactory  The server context factory to bootstrap the request
     * @param usrId           The user ID to login with
     * @param password        The password to login with
     * @param requestType     The request type
     * @param requestAddress  The client address which this request originated from
     * @return                The generated session ID
     * @throws MocaException  On authentication failure
     */
    public static String login(SystemContext systemContext, ServerContextFactory contextFactory,
                               String usrId, String password, RequestType requestType,
                               String requestAddress) throws MocaException {
        ArgCheck.notNull(systemContext, "The system context must be provided");
        ArgCheck.notNull(contextFactory, "The context factory must be provided");
        ArgCheck.notNull(requestType, "The request type must be specified");
        ArgCheck.notNull(requestAddress, "The request address must be provided");
        
        // First we need to create a session so we can call 
        // secure commands (e.g. get user in role)
        RequestContext requestContext = new RequestContext(
            Collections.singletonMap(MocaConstants.WEB_CLIENT_ADDR, requestAddress));
        String contextSessionId = requestType._sessionPrefix + "-" + Integer.toHexString(
            System.identityHashCode(requestContext));
        final SessionContext sessionContext = new LocalSessionContext(
            contextSessionId, SessionType.SERVER);
        // Authenticate our session
        final ServerContext context = contextFactory.newContext(requestContext, sessionContext);
        // Set up the new context on this thread.
        ServerUtils.setCurrentContext(context);
        String sessionId;
        try {
            // Now actually login and verify the user has an appropriate admin role
            sessionId = login(requestType, context, usrId, password);
            final boolean authenticated = (sessionId != null);
            if (authenticated) {
                final Role role = getUserRole(sessionId);
                if (role != Role.CONSOLE_ADMIN && role != Role.CONSOLE_READ) {
                    throw new NotAuthorizedException("The user: " + usrId + " does not have MOCA admin access");
                }
            }
        }
        finally {
            final MocaSessionManager sessionManager = 
                    (MocaSessionManager) systemContext.getAttribute(MocaSessionManager.class.getName());
            cleanUp(context, sessionManager, null, sessionContext);
        }
        
        return sessionId;
    }

    
    /**
     * Authenticates with the server using the provided ServerContext. If successful
     * the session key will be returned.
     * @param requestType the type of request for the authentication
     * @param context The server context
     * @param usrId The user ID to login with
     * @param password The password to login with
     * @return The generated session key
     * @throws LoginFailedException When the provided credentials are invalid
     * @throws MocaException When an unexpected server error occurs
     */
    static String login(RequestType requestType, ServerContext context, String usrId, String password)
            throws LoginFailedException, MocaException {
        MocaResults res;
        
        // When the internal MOCA console user is specified
        // we validate the credentials internally rather than
        // calling "login user" which would go to the database
        // This is to allow for scenarios where database access may
        // be down or the MOCA server is configured to run without a database
        // but we still need console and JMX access
        if (usrId.equals(AuthService.getConsoleAdminUser())) {
            res = handleInternalLogin(requestType, context, usrId, password);
        }
        else if (AuthService.isNoDatabaseMode(context.getComponentContext())) {
            // If this is no database mode they have to specify the admin-user which we know they didn't at this point
            _logger.info(String.format("Tried to login with the user [%s] that doesn't match the admin user.", usrId));
            throw new LoginFailedException("The provided user doesn't match the admin user.");
        }
        else {
            res = handleCommandBasedLogin(context, usrId, password);
        }
        
        if (res.next() && res.containsColumn("session_key")) {
            return res.getString("session_key");
        }
        else {
            throw new LoginFailedException("Unable to login with the provided credentials.");
        }
    }
    
    
    private static void cleanUp(ServerContext context,
                             MocaSessionManager sessionManager,
                             SessionToken sessionToken,
                             SessionContext sessionContext) {
        // Clean up our session. We don't need it beyond
        // this one time login call.
        if (context != null) {
            context.close();
        }
        if (sessionManager != null && sessionToken != null) {
            sessionManager.close(sessionToken);
        }
        if (sessionContext != null) {
            sessionContext.setSessionToken(null);
        }
        ServerUtils.removeCurrentContext();
    }
    
    public static boolean handleAuthenticationRequest(HttpServletRequest request)
            throws UnsupportedEncodingException {
        return handleAuthenticationRequest(request, _cookieName);
    }
    
    public static boolean handleAuthenticationRequest(HttpServletRequest request, String sessionCookieName)
            throws UnsupportedEncodingException {
        
        // Get the client's session id cookie.
        Cookie sessionIdCookie = getCookie(request, sessionCookieName);
        if (sessionIdCookie == null) {
            _logger.debug("A console session id cookie was not found on the client");
            return false;
        }
        
        // Get the session id from the client's session id cookie.
        String sessionId = getSessionId(sessionIdCookie);
        if (sessionId == null) return false;
        
        // Get the security domain for this environment and put it into a trusted domain list.
        SystemContext context = ServerUtils.globalContext();
        MocaSessionManager sessionManager = (MocaSessionManager) context.getAttribute(
            MocaSessionManager.class.getName());
        
        // Validate the session id.
        SessionToken authToken = sessionManager.validate(sessionId); 
        if (authToken == null) {
            _logger.debug("Console session id validation failed");
            return false;
        }

        request.setAttribute("moca.AuthToken", authToken);

        _logger.debug("Console session id validation successful");

        return true;
    }
    
    public static String getSessionId(Cookie cookie) throws UnsupportedEncodingException {
        // Get the session id from the client's session id cookie.
        String encodedSessionId = cookie.getValue();
        if (encodedSessionId == null) {
            _logger.debug("A console session id was not found in the session id cookie");
            return null;
        }
        
        // Decode the session id.
        String sessionId = URLDecoder.decode(encodedSessionId, URL_ENCODING);
        
        return sessionId;
    }
    
    /***
     * Uses the given session id to get the session token to get
     * the role from the session.
     * @param sessionId
     * @return
     */
    public static Role getUserRole(String sessionId) {
        SystemContext context = ServerUtils.globalContext();
        MocaSessionManager sessionManager = (MocaSessionManager) context
                .getAttribute(MocaSessionManager.class.getName());
        SessionToken tk = sessionManager.validate(sessionId);
        if (tk != null) {
            SessionData sd = sessionManager.getSessionData(tk.getSessionId());
            return sd.getRole();
        }
        else {
            return null;
        }
    }
    
    /***
     * Uses the given session token to get
     * the role from the session.
     * @param sessionId
     * @return
     */
    public static Role getUserRole(SessionToken tk) {
        SystemContext context = ServerUtils.globalContext();
        MocaSessionManager sessionManager = (MocaSessionManager) context
                .getAttribute(MocaSessionManager.class.getName());
        if (tk != null) {
            SessionData sd = sessionManager.getSessionData(tk.getSessionId());
            return sd.getRole();
        }
        else {
            return null;
        }
    }
    
    public static void removeSession(String sessionId)
            throws UnsupportedEncodingException {
        
        SystemContext context = ServerUtils.globalContext();
        MocaSessionManager sessionManager = (MocaSessionManager) context.getAttribute(
            MocaSessionManager.class.getName());
        // We only validate using legacy method, so just remove this from
        // the persistent store
        SessionToken token = sessionManager.validate(sessionId);
        if (token != null) {
            sessionManager.close(token);
        }
    }
    
    public static void setupCookie(String sessionId, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        setupCookie(sessionId, request, response, _cookieName, _cookiePath);
    }
    
    public static void setupCookie(String sessionId, HttpServletRequest request, HttpServletResponse response,
                                   String sessionCookieName, String sessionCookiePath) throws UnsupportedEncodingException {
        String encodedSessionId = URLEncoder.encode(sessionId, URL_ENCODING);
        
        // Get the cookie domain to use.
        String cookieDomain = getCookieDomain(request);
        
        // Create a new session id cookie.
        Cookie cookie = new Cookie(sessionCookieName, encodedSessionId);
        cookie.setPath(sessionCookiePath);
        cookie.setDomain(cookieDomain);
        
        if( request.isSecure()) {
            cookie.setSecure(true);
        }
 
        // Set the cookie on the browser.
        response.addCookie(cookie);
    }

    public static void handleLogout(HttpServletRequest request, HttpServletResponse response) { 
        
        /***
         * Now that we have active sessions, if we log out
         * try to remove the session.
         */
        Cookie sessionIdCookie = getCookie(request, _cookieName);
        if (sessionIdCookie != null) {
            // Get the session id from the client's session id cookie.
            try {
                String sessionId = getSessionId(sessionIdCookie);
                if(sessionId != null) {
                    removeSession(sessionId);
                }
            }
            catch (UnsupportedEncodingException e) {
                //If this happens, we have other problems...
                _logger.debug(e);
            }
        }
        
        // Get the cookie domain to use.
        String cookieDomain = getCookieDomain(request);
        
        // Create a new session id cookie to force it to be removed on the browser.
        Cookie cookie = new Cookie(_cookieName, null);
        cookie.setPath(_cookiePath);
        cookie.setDomain(cookieDomain);
        cookie.setMaxAge(0);
        
        // Set the cookie on the browser to force its removal.
        response.addCookie(cookie);
    }
    
    /**
     * Authentication via MOCA command call of "login user". This command will typically be
     * overridden by downstream products (MCS etc.) and will validate against a database and such.
     * @param context The MOCA Server Context
     * @param user The user
     * @param password The password
     * @return A result set containing the generated session key
     * @throws MocaException When unable to login
     */
    private static MocaResults handleCommandBasedLogin(ServerContext context, String user, String password) throws MocaException {
        return context.getComponentContext().executeCommand(
                    LOGIN_USER_COMMAND,
                    new MocaArgument("usr_id", user),
                    new MocaArgument("password", password),
                    new MocaArgument("MOCA_APPL_ID", "moca-console"));
    }
    
    /**
     * Internal authentication that just checks against the hashed password
     * specified in the MOCA registry
     * @param user The user
     * @param password The password
     * @return A result set containing the generated session key
     * @throws MocaException When unable to login
     */
    private static MocaResults handleInternalLogin(RequestType requestType, ServerContext context, String user, String password) throws MocaException {
        // Get the hashed password from the registry.
        SystemContext sys = ServerUtils.globalContext();
        String hashedPassword = sys.getConfigurationElement(MocaRegistry.REGKEY_SECURITY_ADMIN_PASSWORD);
        
        // Validate the password that the user provided.
        if (MD5Utils.validateHashedPassword(password, hashedPassword)) {
            // Create a new session id
            MocaResults res = AuthService.loginUser(context.getComponentContext(), user, password, null);
            _logger.info(String.format("Logged in with the internal admin user from IP [%s] with request type [%s]",
                context.getSystemVariable(MocaConstants.WEB_CLIENT_ADDR), requestType));
            return res;
        }
        else {
            throw new LoginFailedException("The password provided does not match");
        }
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
    
    /*
     * The idea here is to make sure we choose the right cookie domain based on 
     * both the cookie domain defined in the registry as well as the host
     * that the request came from.
     * 
     * If the host that the request came from is in the same domain as the domain
     * defined in the registry then we'll use the registry value.
     * 
     * However, if it isn't then we'll instead not even set the cookie domain, which
     * as a result will set the cookie domain to whatever the host is that issued
     * the request.
     */
    private static String getCookieDomain(HttpServletRequest request) {
        
        try {
            URL url = new URL(request.getRequestURL().toString());
            String requestHost = url.getHost();
            if (requestHost.endsWith(_defaultCookieDomain))
                return _defaultCookieDomain;
        }
        catch (MalformedURLException e) {
            // This should never occur given that we're getting the URL from
            // HttpServletRequest.
            _logger.error("Console request URL was malformed.", e);
        }
        
        return "";   
    }
    
	public static String getCookieName() {
		return _cookieName;
	}
    
    private static final String URL_ENCODING = "UTF-8";
    
    static {    
        SystemContext sys = ServerUtils.globalContext();    

        // Set the cookie name.
        try {
            String clusterName = sys.getConfigurationElement(MocaRegistry.REGKEY_CLUSTER_NAME);
            if (clusterName == null || clusterName.isEmpty())
                _cookieName = "ConsoleSessionId";
            else
                _cookieName = URLEncoder.encode("ConsoleSessionId-" + clusterName, URL_ENCODING);
        }
        catch (UnsupportedEncodingException e) {
            throw new ExceptionInInitializerError(e);
        } 
        
        // Set the default cookie domain.
        _defaultCookieDomain = sys.getConfigurationElement(MocaRegistry.REGKEY_CLUSTER_COOKIE_DOMAIN, "");
    }  
 
    static final String LOGIN_USER_COMMAND = "login user where usr_id=@usr_id and password=@password and @+MOCA_APPL_ID";
    private static final String _cookieName;
    private static final String _defaultCookieDomain;
    private static final String _cookiePath = "/console";
    
    private static final Logger _logger = LogManager.getLogger(Authentication.class);
}