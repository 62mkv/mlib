/*
 *  $URL$
 *  $Revision$
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
                
package com.sam.moca.client;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sam.moca.MocaArgument;
import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.util.Base64;

/**
 * Connection utilities to assist in common actions 
 * such as logging in as parsing the environment string.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class ConnectionUtils {

    public static void login(MocaConnection conn, String user, String password) throws LoginFailedException {
        ConnectionUtils.login(conn, user, password, "");
    }
    
    public static void login(MocaConnection conn, String user, String password, String clientKey)
            throws LoginFailedException {

        MocaResults res = null;
        try {
            String loginCommand = "login user where usr_id = @usr_id and usr_pswd = @usr_pswd";
            
            MocaArgument userArg = new MocaArgument("usr_id", user);
            MocaArgument passwordArg = new MocaArgument("usr_pswd", password);         
            MocaArgument clientKeyArg = new MocaArgument("client_Key", ConnectionUtils.generateClientKey(clientKey, conn.getServerKey()));
            
            res = conn.executeCommandWithArgs(loginCommand, userArg, passwordArg, clientKeyArg);
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

                Map<String, String> env = conn.getEnvironment();
                if (env == null) {
                    env = new LinkedHashMap<String, String>();
                }

                env.put("LOCALE_ID", locale);
                env.put("USR_ID", user);
                env.put("SESSION_KEY", key);
                conn.setEnvironment(env);
            }
        }
        catch (MocaException e) {
            throw new LoginFailedException(e.getMessage(), e);
        }
        finally {
            if (res != null) res.close();
        }
    }

    public static void logout(MocaConnection conn) throws LogoutFailedException {
        try {
            conn.executeCommand("logout user");
        }
        catch (MocaException e) {
            throw new LogoutFailedException(e.getMessage(), e);
        }
        finally {
            conn.setEnvironment(null);
        }
    }
    
    public static Map<String, String> parseEnvironmentString(String envString) {
        Map<String, String> env = new LinkedHashMap<String, String>();

        if (envString != null) {
            String[] elements = envString.split(":");
            for (int i = 0; i < elements.length; i++) {
                String[] tmp = elements[i].split("=", 2);
                if (tmp.length == 2) {
                    env.put(tmp[0].toUpperCase(), tmp[1]);
                }
            }
        }

        return env;
    }

    public static void confirmDigitalSignature(MocaConnection conn,
                                               String user, String password)
            throws LoginFailedException {

        String safeUser = user.replace("'", "''").toUpperCase();
        String safePassword = password.replace("'", "''");
        String loginCommand = String
            .format(
                "confirm digital signature where usr_id = '%1$s' and usr_pswd = '%2$s'",
                safeUser, safePassword);

        try {
            MocaResults res = conn.executeCommand(loginCommand);
            if (res.next() && res.containsColumn(SIG_KEY.toLowerCase())) {

                String signatureKey = res.getString(SIG_KEY.toLowerCase());

                if (signatureKey != null && !signatureKey.isEmpty()) {
                    Map<String, String> env = conn.getEnvironment();
                    env.put(SIG_KEY, signatureKey);
                    conn.setEnvironment(env);
                    return;
                }
            }

            throw new LoginFailedException(
                "No key value returned from the command");
        }
        catch (MocaException e) {
            throw new LoginFailedException(e.getMessage());
        }
    }

    public static void removeDigitalSignature(MocaConnection conn) {

        Map<String, String> env = conn.getEnvironment();
        
        if (env.containsKey(SIG_KEY)) {
            String key = env.get(SIG_KEY);
            
            if (key.endsWith("#")) {
                env.remove(SIG_KEY);
                conn.setEnvironment(env);
            }
        }       
    }
    
    /**
     * Factory method to produce the appropriate type of connection for the given string.
     * @param hostString a host string appropriate to one of the types of MocaConnection
     * classes. For example, if the string appears to be an HTTP URL, HttpConnection
     * is instantiated.
     * @param env the environment to be associated with the call.
     */
    public static MocaConnection createConnection(String hostString, Map<String, String> env) throws MocaException {
        if (hostString.startsWith("http://") || 
            hostString.startsWith("https://")) {
            return new HttpConnection(hostString, env);
        }
        else {
            String[] fields = hostString.split(":");
            String host = hostString;
            int port = 4500;
            if (fields.length == 2) {
                host = fields[0];
                port = Integer.parseInt(fields[1]);
            }
            return new DirectConnection(host, port, env);
        }
    }

    /**
     * @param env
     */
    static String buildEnvironmentString(Map<String, String> env) {
        // Build an environment string
        StringBuilder sb = new StringBuilder();

        if (env != null) {
            for (Entry<String, String> i : env.entrySet()) {
                if (sb.length() > 0) sb.append(':');
                sb.append(i.getKey().toUpperCase());
                sb.append('=');
                sb.append(i.getValue());
            }
        }

        return sb.toString();
    }
    
    /**
     * Calculate a client key, given a client ID and server ID.
     * @param clientKey
     * @param serverKey
     * @return
     */
    public static String generateClientKey(String clientKey, String serverKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(clientKey.getBytes("UTF-8"));
            digest.update(serverKey.getBytes("UTF-8"));
            byte[] hash = digest.digest();

            int pound = clientKey.indexOf('#');
            String keyName;
            if (pound != -1) {
                keyName = clientKey.substring(0, pound);
            }
            else {
                keyName = clientKey;
            }
            return keyName + '/' + Base64.encode(hash);
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("UTF-8 not supported -- Bad Stuff!");
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unable to find SHA-1 algorithm", e);
        }

    }

    private final static String SIG_KEY = "SIG_KEY";
}
