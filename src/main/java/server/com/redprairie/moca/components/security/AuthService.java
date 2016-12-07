/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.components.security;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaConstants;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.client.LoginFailedException;
import com.redprairie.moca.client.NotAuthorizedException;
import com.redprairie.moca.server.SecurityLevel;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.db.DBType;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SessionContext;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.session.MocaSessionManager;
import com.redprairie.moca.server.session.MocaSessionUtils;
import com.redprairie.moca.server.session.SessionToken;
import com.redprairie.moca.util.MD5Utils;
import com.redprairie.moca.web.console.Authentication.Role;

/**
 * MOCA Console Authentication Service
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class AuthService {

    public static MocaResults loginUser(MocaContext moca, String username, String password, String clientKey) throws MocaException {

        String sessionKey = null;
        
        MocaSessionManager auth = ServerUtils.globalAttribute(MocaSessionManager.class);

        if (auth != null) {
        	boolean mocalogin = false;
            Map<String, String> env = new LinkedHashMap<String, String>();
            env.put(MocaConstants.WEB_CLIENT_ADDR, moca.getSystemVariable(MocaConstants.WEB_CLIENT_ADDR));
            
            MocaArgument[] args = moca.getArgs();
            for (MocaArgument ma : args){
                final String name = ma.getName();
                final String value = String.valueOf(ma.getValue());
                env.put(name, value);
                
                if (APP_ARG.equals(name) && MOCA_CONSOLE_APP.equals(value)) {
                    mocalogin = true;
                }
            }
            
            Role role = getRole(moca, username);
            if (mocalogin && !(role == Role.CONSOLE_ADMIN || role == Role.CONSOLE_READ)) {
                throw new NotAuthorizedException("User is not authorized to use this service: " + username);
            }
            
            SecurityLevel security = MocaSessionUtils.validateClientKey(clientKey);
            if (security == null) {
                throw new LoginFailedException("Not Authorized");
            }
            sessionKey = auth.createTracked(username, role, env, security);

            if (sessionKey == null) {
                throw new LoginFailedException("unknown error");
            }
        }

        EditableResults res = moca.newResults();
        res.addColumn("usr_id", MocaType.STRING);
        res.addColumn("session_key", MocaType.STRING);
        
        res.addRow();
        res.setStringValue("usr_id", username);
        res.setStringValue("session_key", sessionKey);

        return res;
    }
    
    /**
     * Validates the admin console user configuration throwing a {@link SystemConfigurationException}
     * when the configuration is invalid. Possible invalid configurations are:
     * <p>1) admin-user specified but not admin-password
     * <p>2) admin-password specified but not admin-user
     * <p>3) admin-password specified but it's not hashed via mpasswd starting with |H|
     * @param context The system context that defines the configuration
     * @throws SystemConfigurationException When the configuration is invalid
     */
    public static void validateConsoleAdminConfiguration(SystemContext context) throws SystemConfigurationException {
        String hashedPassword = context.getConfigurationElement(MocaRegistry.REGKEY_SECURITY_ADMIN_PASSWORD);
        String adminUser = getConsoleAdminUser(context);
        if (!StringUtils.isEmpty(adminUser) && StringUtils.isEmpty(hashedPassword)) {
            throw new SystemConfigurationException("The console admin user (security.admin-user) was specified but a password wasn't configured (security.admin-password).");
        }
        else if (!StringUtils.isEmpty(hashedPassword) && StringUtils.isEmpty(adminUser)) {
            throw new SystemConfigurationException("The console admin password (security.admin-password) was specified but a user wasn't configured (security.admin-user).");
        }
        else if (hashedPassword != null && !hashedPassword.startsWith(MD5Utils._hashPipe)) {
            throw new SystemConfigurationException("The console admin password (security.admin-password) cannot be specified in clear text, it must be hashed using the mpasswd utility.");
        }
    }
    
    private static Role getRole(MocaContext moca, String usrId) throws MocaException {
        if (usrId.equals(getConsoleAdminUser())) {
            return getRoleForInternalUser(moca, usrId);
        }
        else {
            return getRoleCommandBased(moca, usrId);
        }
    }
    
    /**
     * Get the Moca Console privileges for the user. Returns the ADMIN privilege role
     * if both ADMIN and READ-ONLY privileges are found.
     * @param moca moca context
     * @param usrId user ID
     * @return MOCA Console role
     * @throws MocaException
     */
    private static Role getRoleCommandBased(MocaContext moca, String usrId) throws MocaException {
        try {
            final MocaResults res2 = moca.executeCommand(
                "get user privileges where usr_id=@usr_id and opt_typ=@opt_typ",
                new MocaArgument("usr_id", usrId),
                new MocaArgument("opt_typ", "P"));
            
            boolean hasRead = false;
            while (res2.next()) {
                final String p = res2.getString("opt_nam");
                if (CONSOLE_ADMIN_PERMISSION.equals(p)) {
                    return Role.CONSOLE_ADMIN;
                }
                if (CONSOLE_READ_PERMISSION.equals(p)) hasRead = true;
            }
            if (hasRead) {
                return Role.CONSOLE_READ;
            }
            else {
                return Role.NO_CONSOLE_ACCESS;
            }
        }
        catch (NotFoundException e) {
            return Role.NO_CONSOLE_ACCESS;
        }
    }

    // For the internal user specified via MOCA registry security.admin-user we don't actually
    // check for the role configuration. For servers with a database defined the internal user
    // gets read only access, when there is no database defined the internal user gets full access.
    private static Role getRoleForInternalUser(MocaContext moca, String usrId) {
        if (isNoDatabaseMode(moca)) {
            return Role.CONSOLE_ADMIN;
        }
        else {
            return Role.CONSOLE_READ;
        }
    }
    
    public static void logoutUser(MocaContext moca) throws MocaException {
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
    
    public static String getConsoleAdminUser() {
        return getConsoleAdminUser(ServerUtils.globalContext());
    }
    
    public static boolean isNoDatabaseMode(MocaContext moca) {
        return DBType.valueOf(moca.getDbType()) == DBType.NONE;
    }
    
    private static String getConsoleAdminUser(SystemContext sys) {
        return sys.getConfigurationElement(MocaRegistry.REGKEY_SECURITY_ADMIN_USER, "");
    }
    
    private static final String CONSOLE_READ_PERMISSION = "optMocaConsoleRead";
    private static final String CONSOLE_ADMIN_PERMISSION = "optMocaConsoleAdmin";
    private static final String MOCA_CONSOLE_APP = "moca-console";
    private static final String APP_ARG = "MOCA_APPL_ID";
}
