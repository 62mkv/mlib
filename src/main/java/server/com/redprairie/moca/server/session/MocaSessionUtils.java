/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.server.session;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.server.SecurityLevel;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SessionContext;
import com.redprairie.moca.server.exec.SystemContext;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class MocaSessionUtils {
    
    public static final String AUTH_ENV_KEY = "SESSION_KEY";
    
    /**
     * Validates the session key, as passed in the environment.  Note, this method
     * modifies the passed-in environment map.  It removes the session key from
     * the map.  This makes the session key invisible to any downstream components.
     * @param session
     * @param envMap
     */
    public static boolean validateSession(SessionContext session,
                                          Map<String, String> envMap,
                                          MocaSessionManager mgr) {
        boolean isValid = false;
        
        // Check to see if we've already authenticated.
        SessionToken authToken = session.getSessionToken();
        if (authToken != null) {
            isValid = true;
        }
        else {
            // If not, validate the session key and configure the authentication
            // If the request came in with a valid server session key, we need to consider the user authenticated.
            String sessionKey = envMap.remove(AUTH_ENV_KEY);
            if (sessionKey != null) {
                
                authToken = mgr.validate(sessionKey);

                // If the session is valid, set up authentication for this
                // session.
                if (authToken != null) {
                    _logger.debug("SESSION_KEY validated");
                    session.setSessionToken(authToken);
                    isValid = true;
                }
                else {
                    _logger.debug("Invalid SESSION_KEY [" + sessionKey + "]");
                }
            }
            else {
                _logger.debug("SESSION_KEY not present");
            }
        }
        
        return isValid;
    }
       
    public static String newSessionKey(String userId, SecurityLevel security) {
        MocaSessionManager auth = ServerUtils.globalAttribute(MocaSessionManager.class);
        if (auth != null) {
            String sessionKey = auth.createTracked(userId, null, null, security);
            return sessionKey;
        }
        else {
            return "NA";
        }
    }
    
    public static String newRemoteKey(String userId) {
        MocaSessionManager auth = ServerUtils.globalAttribute(MocaSessionManager.class);
        if (auth != null) {
            String sessionKey = auth.createRemote(userId);
            return sessionKey;
        }
        else {
            return "REMOTE";
        }
    }
    
    public static void invalidateSession() {
        ServerContext ctx = ServerUtils.getCurrentContext();
        if (ctx == null) return;
        
        SessionContext session = ctx.getSession();
        if (session == null) return;
        
        SessionToken token = session.getSessionToken();
        if (token != null) {
            MocaSessionManager auth = ServerUtils.globalAttribute(MocaSessionManager.class);
            if (auth != null) {
                auth.close(token);
            }
            session.setSessionToken(null);
        }
    }
    
    public static SecurityLevel validateClientKey(String clientKey) {

        SystemContext ctx = ServerUtils.globalContext();

        ClientKeyValidator validator = ServerUtils.globalAttribute(ClientKeyValidator.class);
        if (validator == null) {
            synchronized (MocaSessionUtils.class) {
                Map<String, String> clientConfig = new LinkedHashMap<String, String>();
                clientConfig.put("mcsframework#gokpmqoetfnsasqffhkofreoybugag", null);
                clientConfig.put("mtfframework#gjbsvoihtprkditielxrspobaxjnwi", null);
                clientConfig.put("waffle#csfnuwppiikvyspnhkmwiabenwimpo", null);
                clientConfig.put("rpweb#omygjdaxpynjldbvqhfpgyajhtkrwv", "PUBLIC");
                clientConfig.put("mtk#qaxlasqndahomyxlmoafectfefigfi", null);
                clientConfig.put("webservices#ejfdilhkbobccfjckiievytjsrmssq", null);
                clientConfig.put("mload#isfmwfbwptbbcksxiulhnrxoktvbpg", null);
                Map<String, String> registryConfig = ctx.getConfigurationSection(MocaRegistry.REGSEC_CLIENTS, false);
                
                for (Map.Entry<String, String> e : registryConfig.entrySet()) {
                    clientConfig.put(e.getKey().toLowerCase(), e.getValue());
                }
                
                String serverKey = ctx.getConfigurationElement(MocaRegistry.REGKEY_SECURITY_SERVER_KEY);
                if (serverKey == null) serverKey = ctx.getConfigurationElement(MocaRegistry.REGKEY_CLUSTER_NAME);
                if (serverKey == null) serverKey = "";

                validator = new ClientKeyValidator(serverKey, clientConfig);
                ctx.putAttribute(ClientKeyValidator.class.getName(), validator);
            }
        }
        
        String key = clientKey;
        SecurityLevel security = validator.validate(key);
        return security;
    }
    
    private static final Logger _logger = LogManager.getLogger(MocaSessionUtils.class);
}
