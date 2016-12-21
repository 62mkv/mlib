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

package com.sam.moca.components.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.sam.moca.EditableResults;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.components.base.HttpService;
import com.sam.moca.server.SecurityLevel;
import com.sam.moca.server.session.MocaSessionUtils;
import com.sam.util.Base64;

/**
 * FederatedSecurity helps establish a federated security model that with
 * trusted hosts working through RPWeb.
 * 
 * Copyright (c) 2016 Sam Corporation All Rights Reserved
 * 
 * @author Matt Horner
 */
public class FederatedSecurityComponents {
    public FederatedSecurityComponents() throws NoSuchAlgorithmException {
        _generateEncryptionKey();
    }

    private static void _generateEncryptionKey()
            throws NoSuchAlgorithmException {
        synchronized (FederatedSecurityComponents.class) {
            if (_encryptionKey == null) {
                _encryptionKey = KeyGenerator.getInstance(CRYPTO_ALGORITHM)
                    .generateKey();
            }
        }
    }

    /**
     * Create the token that will be used to validate the user's identity.
     * 
     * @param moca The moca context of this component execution.
     * @return The results containing the token for validation.
     * 
     * @throws Exception
     */
    public MocaResults createFederationToken(MocaContext moca)
            throws MocaException {
        UUID uuid = UUID.randomUUID();

        moca.trace("Generating new SSO Token");

        Cipher cipher;
        try {
            // initialize the cipher to prep for proper encryption
            // of the token before sending across the wire
            cipher = Cipher.getInstance(CRYPTO_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, _encryptionKey);
        }
        catch (Exception e) {
            throw new FederatedSecurityException(e.getMessage());
        }

        // generate the expiration date to control whether this
        // token is valid after a period of time.
        Date date = new Date();
        long expirationTimeStamp = date.getTime() + EXPIRATION_MILLIS;

        // Format the pieces of the token that will be encrypted.
        String token = String.format("%s%s%s", expirationTimeStamp,
            TOKEN_SEPARATOR, uuid.toString());

        // Encode the output in a format that is easily passed through on the
        // wire.
        StringBuilder federationToken = new StringBuilder();
        try {
            InputStream bytesStream = new ByteArrayInputStream(cipher
                .doFinal(token.getBytes(Charset.forName("UTF-8"))));
            Base64.encode(bytesStream, federationToken);
        }
        catch (IOException e) {
            throw new FederatedSecurityException(e.getMessage());
        }
        catch (IllegalBlockSizeException e) {
            throw new FederatedSecurityException(e.getMessage());
        }
        catch (BadPaddingException e) {
            throw new FederatedSecurityException(e.getMessage());
        }

        // Put the user id in the map (database) for retrieval later
        // after the host asks for a validation of the token.
        userMap.put(uuid.toString(), moca.getSystemVariable("usr_id"));

        EditableResults results = moca.newResults();
        results.addColumn("token", MocaType.STRING);
        results.addRow();

        String resultToken = token;
        try {
            resultToken = URLEncoder.encode(federationToken.toString(), URL_ENCODING);
        }
        catch (UnsupportedEncodingException e) {
            // pass the token as specified
        }
        results.setValue("token", resultToken);

        moca.trace("Returning a new token.");
        return results;
    }

    /**
     * In order to complete the transaction a validation of the token and it's
     * parts needs to be completed. If everything checks out then return the
     * user id for the user associated to the passed in token.
     * 
     * @param moca The moca context of this component execution.
     * @param token The token passed as a validation for the user session.
     * @return The moca results with the user id (null/valid user) upon
     *         validation.
     * @throws Exception
     */
    public MocaResults validateFederationToken(MocaContext moca,
                                               String federationToken)
            throws Exception {
        Cipher cipher = Cipher.getInstance(CRYPTO_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, _encryptionKey);

        // decode and decrypt the token to get at the components.
        try {
            federationToken = URLDecoder.decode(federationToken, URL_ENCODING);
        }
        catch (UnsupportedEncodingException e) {
            // pass the token as specified
        }

        byte[] cryptoBytes = Base64.decode(federationToken);
        String token = new String(cipher.doFinal(cryptoBytes), "UTF-8");

        // break apart the token into it's known parts.
        String[] tokenBits = token.split(TOKEN_SEPARATOR);
        long currentMillis = (new Date()).getTime();

        String userId = null;

        // has the token expired yet?
        long expirationTime = Long.parseLong(tokenBits[0], 10);
        if (expirationTime > currentMillis) {
            // token hasn't expired.
            if (userMap.get(tokenBits[1]) != null) {
                moca.trace("Gathering user id from the map for UUID "
                        + tokenBits[1]);
                userId = userMap.get(tokenBits[1]);
                moca.trace("Found " + userId + "for the UUID");
            }
        }

        // clean up the token, it's either expired or consumed.
        userMap.remove(tokenBits[1]);

        // The token has either expired or a bad request was made
        if (userId == null) {
            throw new FederatedSecurityException(
                "The token validation failed, cannot authenticate session.");
        }

        EditableResults results = moca.newResults();
        results.addColumn("userId", MocaType.STRING);
        results.addRow();
        results.setValue("userId", userId);

        return results;
    }

    /**
     * Validate the federale authentication request and create a proper MOCA
     * Session that can be executed against.
     * 
     * @param moca The MOCA Context.
     * @param token The authentication token.
     * @param sessionId The REFS session id.
     * @return MOCA Results with the new session key.
     * @throws MocaException
     */
    public MocaResults validateFederaleAuthenticationRequest(MocaContext moca,
                                                             String token,
                                                             String sessionId)
            throws MocaException {
        HttpService httpService = new HttpService();
        String rpwebURL = moca.getRegistryValue("server.rpweb-url");
        if (rpwebURL == null) {
            throw new FederatedSecurityException(
                "Missing address for authentication validation.");
        }

        rpwebURL += (rpwebURL.endsWith("/") ? "" : "/") + "sso";

        MocaResults res;
        try {
            res = httpService.doHttpRequest(moca, rpwebURL, "POST",
                "Cookie:REFSSessionID=" + sessionId, "", "token=" + token, 
                null);
        }
        catch (MocaException e) {
            // exception occurred while requesting token validation
            moca.logError(e.getMessage());
            throw new FederatedSecurityException(e.toString());
        }
        res.next();

        // check the response from the server make sure it was OK
        if (res.getInt("status") != HttpURLConnection.HTTP_OK) {
            throw new FederatedSecurityException(
                "Request for validation failed with a bad response from server.");
        }

        // validation passed, create the new session key
        String userId = res.getString("body").toUpperCase().trim();
        moca.setSessionAttribute("moca.auth", userId);

        String sessionKey = MocaSessionUtils.newSessionKey(userId, SecurityLevel.ALL);

        // Create a new return structure with the new session information.
        EditableResults results = moca.newResults();
        results.addColumn("session_key", MocaType.STRING);
        results.addColumn("usr_id", MocaType.STRING);
        results.addRow();
        results.setValue("session_key", sessionKey);
        results.setValue("usr_id", userId);

        return results;
    }

    private static final String TOKEN_SEPARATOR = "&&";
    private static final String CRYPTO_ALGORITHM = "DESede";
    private static final String URL_ENCODING = "UTF-8";
    private static final int EXPIRATION_MILLIS = 120000;

    private static Map<String, String> userMap = new ConcurrentHashMap<String, String>();
    private static SecretKey _encryptionKey;
}
