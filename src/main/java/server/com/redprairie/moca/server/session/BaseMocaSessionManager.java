/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.redprairie.moca.server.SecurityLevel;
import com.redprairie.moca.web.console.Authentication.Role;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public abstract class BaseMocaSessionManager implements MocaSessionManager {
    
    public BaseMocaSessionManager(String myDomain, String[] trustedDomains,
                                      int remoteWindowSecs, boolean allowLegacySessions) {
        if (myDomain == null) {
            _myDomain = "";
        }
        else {
            _myDomain = myDomain;
        }
        
        List<String> domains = new ArrayList<String>();
        domains.add(_myDomain);
        if (trustedDomains != null) {
            domains.addAll(Arrays.asList(trustedDomains));
        }
        _validator = new MocaSessionValidator(domains);
        _remoteWindow = (long)remoteWindowSecs * 1000L;
        _allowLegacySessions = allowLegacySessions;
        
    }
    
    @Override
    public String createRemote(String userId) {
        // TODO Auto-generated method stub
        return create(userId, null, SessionType.REMOTE, null, SecurityLevel.ALL);
    }
    
    @Override
    public String createTracked(String userId, Role role, Map<String, String> env, SecurityLevel security) {
        // TODO Auto-generated method stub
        return create(userId, role,  SessionType.TRACKED, env, security);
    }
    
    private String create(String userId, Role role, SessionType type, Map<String, String> env, SecurityLevel security) {
        // Allow null to be the same as an empty value
        String domain = _myDomain;
        StringBuilder userField = new StringBuilder();
        userField.append(MocaSessionValidator.KEY_USER_ID)
                 .append('=')
                 .append(userId);
        String sessionId = null;
        if (type == SessionType.TRACKED) {
            sessionId = UUID.randomUUID().toString();
            userField.append(MocaSessionValidator.FIELD_SEPARATOR_CHAR);
            userField.append(MocaSessionValidator.KEY_SID)
                     .append('=')
                     .append(sessionId);
        }
        userField.append(MocaSessionValidator.FIELD_SEPARATOR_CHAR);
        userField.append(MocaSessionValidator.KEY_CREATED_DATE)
                 .append('=')
                 .append(BigInteger.valueOf(System.currentTimeMillis()).toString(36));
        
        userField.append(MocaSessionValidator.FIELD_SEPARATOR_CHAR);
        userField.append(MocaSessionValidator.KEY_SECURITY_LEVEL).append('=')
                 .append(security.toString());

        if (domain == null) domain = "";
        
        StringBuilder seed = new StringBuilder();
        
        for (int i = 0; i < 4; i++) {
            seed.append(SEED_CHARSET[RAND.nextInt(SEED_CHARSET.length)]);
        }
        
        String key = SessionKey.generateKey(userField.toString(), domain, seed.toString());
        
        if (sessionId != null) {
            // Save the session ID into our ID repository
            saveSession(sessionId, new SessionData(userId, role, sessionId, new Date(), env));
        }
        
        return key;
    }
    
    @Override
    public SessionToken validate(String tokenString) {
        SessionToken temp = _validator.validate(tokenString);
        
        if (temp == null) return null;
        
        String sessionId = temp.getSessionId();
        
        // If they passed a session ID, check our repository
        if (sessionId == null) {
            // Check creation date
            Date created = temp.getCreatedDate();
            if (created == null) {
                // If we allow legacy sessions, the session is still good.
                if (_allowLegacySessions) {
                    return temp;
                }
                else {
                    return null;
                }
            }
            
            long elapsed = System.currentTimeMillis() - created.getTime();
            if (_remoteWindow != 0L && elapsed > _remoteWindow) {
                return null;
            }
        }
        else {
            if (!checkSession(sessionId)) {
                return null;
            }
            
        }
        return temp;
    }
    
    @Override
    public void close(SessionToken token) {
        String sessionId = token.getSessionId();
        if (sessionId != null) {
            removeSession(sessionId);
        }
    }

    protected abstract boolean checkSession(String sessionId);
    protected abstract void removeSession(String sessionId);
    public abstract SessionData getSessionData(String sessionKey);
    protected abstract void saveSession(String sessionId, SessionData data);
    
    private final String _myDomain;
    private final MocaSessionValidator _validator;
    private final long _remoteWindow;
    private final boolean _allowLegacySessions;

    private static final char[] SEED_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_.".toCharArray();
    private static final Random RAND = new Random();
}
