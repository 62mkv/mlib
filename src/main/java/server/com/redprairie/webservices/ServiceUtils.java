/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2006
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

package com.redprairie.webservices;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.advice.ForwardingMocaConnection;
import com.redprairie.moca.client.ConnectionFailedException;
import com.redprairie.moca.client.ConnectionUtils;
import com.redprairie.moca.client.LoginFailedException;
import com.redprairie.moca.client.MocaConnection;
import com.redprairie.util.FixedSizeCache;

/**
 * Simple utility methods to help with web service configuration.
 * 
 * <b><pre>
 * Copyright (c) 2006 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class ServiceUtils {
    public static final int DEFAULT_CACHE_SIZE = 20;
    
    public static synchronized MocaConnection getConnection() throws MocaException {
        
        if (_hostConnection == null) {
            Properties configProperties = new Properties();
            String cacheSizeString = null;
            int cacheSize = DEFAULT_CACHE_SIZE; 
            
            InputStream configIn = ServiceUtils.class.getResourceAsStream("/META-INF/moca-config.properties");
            if (configIn != null) {
                try {
                    configProperties.load(configIn);
                    _hostConnection = configProperties.getProperty("server");
                    if (_hostConnection == null) {
                        String host = configProperties.getProperty("host");
                        String port = configProperties.getProperty("port");
                        if (host != null && port != null) {
                            _hostConnection = host + ":" + port;
                        }
                    }
                    cacheSizeString = configProperties.getProperty("cacheSize");
                }
                catch (InterruptedIOException e) {
                    throw new MocaInterruptedException(e);
                }
                catch (IOException e) {
                    throw new ConnectionFailedException("unable to read configuration", e);
                }
                finally {
                    try { configIn.close(); } catch (IOException ignore) { }
                }
            }
            
            if (_hostConnection == null) {
                throw new ConnectionFailedException("missing host defintion");
            }
            
            if (cacheSizeString != null) {
                try {
                    cacheSize = Integer.parseInt(cacheSizeString);
                }
                catch (NumberFormatException e) {
                    throw new ConnectionFailedException("invalid cache size: " + cacheSizeString);
                }
            }
            _sessionCache = new FixedSizeCache<UserToken, Map<String, String>>(cacheSize);
        }
        
        MocaConnection conn = ConnectionUtils.createConnection(_hostConnection, null);
        return conn;
    }
    
    private static MocaServiceConnection getMocaServiceConnection(UserToken token) throws MocaException {
        MocaConnection mocaConn = getConnection();
        return new MocaServiceConnection(mocaConn, token);
    }
    
    public static MocaConnection getConnection(UserToken token) throws MocaException {
        MocaConnection conn = getMocaServiceConnection(token);
        
        if (token != null) {
            Map<String, String> env = _sessionCache.get(token);
            if (env == null) {
                login(conn, token);
            }
        }
        
        return conn;
    }

    private static void login(MocaConnection conn, UserToken token)
            throws LoginFailedException {
        Map<String, String> env = _sessionCache.get(token);
        String user = token.getUsername();
        String password = token.getPassword();
        if (user != null && password != null) {
            String clientKey = "webservices#ejfdilhkbobccfjckiievytjsrmssq";
            String loginCommand = "login webservice";
            
            
            MocaResults res = null;
            try {
                MocaArgument userArg = new MocaArgument("usr_id", user);
                MocaArgument passwordArg = new MocaArgument("usr_pswd", password);
                MocaArgument clientKeyArg = new MocaArgument("client_Key",
                    ConnectionUtils.generateClientKey(clientKey,
                        conn.getServerKey()));

                res = conn.executeCommandWithArgs(loginCommand, userArg,
                    passwordArg, clientKeyArg);
                if (res.next()) {
                    String locale = null;
                    String key = null;

                    if (res.containsColumn("locale_id")) {
                        locale = res.getString("locale_id");
                    }

                    if (res.containsColumn("session_key")) {
                        key = res.getString("session_key");
                    }

                    if (locale == null) locale = "US_ENGLISH";
                    if (key == null) key = "";

                    env = new LinkedHashMap<String, String>();

                    env.put("LOCALE_ID", locale);
                    env.put("USR_ID", user);
                    env.put("SESSION_KEY", key);
                    _sessionCache.put(token, env);
                }
            }
            catch (MocaException e) {
                throw new LoginFailedException(e.getMessage(), e);
            }
            finally {
                if (res != null) res.close();
            }
        }
        conn.setEnvironment(env);
    }
    
    public static String quote(String arg) {
        if (arg == null) {
            return null;
        }
        else {
            return "'" + arg.replace("'", "''") + "'";
        }
    }
    
    public static MocaResults execute(UserToken token, String command) throws ServiceException {
        MocaConnection conn = null;
        try {
            conn = getConnection(token);
            return conn.executeCommand(command);
        }
        catch (MocaInterruptedException e) {
            // If we were interrupted just let it go up.
            throw e;
        }
        catch (Exception e) {
            throw new ServiceException(e);
        }
    }
    
    private static String _hostConnection = null;
    private static Map<UserToken, Map<String, String>> _sessionCache;
    
    /***
     * 
     * A static inner class that extends <code>MocaConnection</code>
     * to provide extra logic to re login for  web services.
     * 
     * Copyright (c) 2012 RedPrairie Corporation
     * All Rights Reserved
     * 
     * @author klehrke
     */
    private static final class MocaServiceConnection extends
            ForwardingMocaConnection {

        private final UserToken _token;

        /**
         * @param conn
         */
        public MocaServiceConnection(MocaConnection conn, UserToken token) {
            super(conn);
            _token = token;
        }

        @Override
        public MocaResults executeCommand(String command) throws MocaException {
            try {
                return super.executeCommand(command);
            }
            catch (MocaException e) {
                if (e.getErrorCode() == 523) {
                    login(delegate(), _token);
                    return super.executeCommand(command);
                }
                throw e;
            }
        }
        
        @Override
        public MocaResults executeCommandWithArgs(String command,
                                                  MocaArgument... args)
                throws MocaException {
            try {
                return super.executeCommandWithArgs(command, args);
            }
            catch (MocaException e) {
                if (e.getErrorCode() == 523) {
                    login(delegate(), _token);
                    return super.executeCommandWithArgs(command, args);
                }
                throw e;
            }
        }

        @Override
        public MocaResults executeCommandWithContext(String command,
                                                     MocaArgument[] args,
                                                     MocaArgument[] commandArgs)
                throws MocaException {
            try {
                return super.executeCommandWithContext(command, args,
                    commandArgs);
            }
            catch (MocaException e) {
                if (e.getErrorCode() == 523) {
                    login(delegate(), _token);
                    return super.executeCommandWithContext(command, args,
                        commandArgs);
                }
                throw e;
            }
        }
    }
}
