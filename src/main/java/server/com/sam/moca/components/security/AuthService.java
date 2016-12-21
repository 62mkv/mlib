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

package com.sam.moca.components.security;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.sam.moca.EditableResults;
import com.sam.moca.MocaArgument;
import com.sam.moca.MocaConstants;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaRegistry;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.NotFoundException;
import com.sam.moca.client.LoginFailedException;
import com.sam.moca.client.NotAuthorizedException;
import com.sam.moca.server.SecurityLevel;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.SystemConfigurationException;
import com.sam.moca.server.db.DBType;
import com.sam.moca.server.exec.ServerContext;
import com.sam.moca.server.exec.SessionContext;
import com.sam.moca.server.exec.SystemContext;
import com.sam.moca.server.session.MocaSessionManager;
import com.sam.moca.server.session.MocaSessionUtils;
import com.sam.moca.server.session.SessionToken;
import com.sam.moca.util.MD5Utils;
import com.sam.moca.web.console.Authentication.Role;

/**
 * MOCA Console Authentication Service
 * 
 * Copyright (c) 2011 Sam Corporation
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
        String user = getConsoleAdminUser();
        System.out.println("usrId:" + usrId + ",useradmin:" + user + (usrId.equals(user) ? " equals": " not equals"));
        if (usrId.equals(user)) {
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
            
//            final MocaResults res2 = moca.executeCommand(
//                    "publish data where opt_nam ='" + CONSOLE_ADMIN_PERMISSION + "'");
            
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
