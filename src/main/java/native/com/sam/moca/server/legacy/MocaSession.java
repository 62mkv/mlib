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

package com.sam.moca.server.legacy;

import java.util.Random;

import com.sam.moca.server.session.SessionKey;

/**
 * Used to allow C-based components to generate an old-style session key.
 * This approach should be discouraged, as it bypasses the session manager,
 * which means these keys may become invalid in the future.
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MocaSession {
    @Deprecated
    public static final String AUTH_SESSION_ATTR = "moca.auth";
    
    @Deprecated
    public static String newSessionKey(String userId, String domain) {
        StringBuilder seed = new StringBuilder();
        
        for (int i = 0; i < 4; i++)
            seed.append(SEED_CHARSET[RAND.nextInt(SEED_CHARSET.length)]);
        
        // Allow null to be the same as an empty value
        if (userId == null) userId = "";
        if (domain == null) domain = "";
        
        return SessionKey.generateKey(userId, domain, seed.toString());
    }
    
    private static final char[] SEED_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_.".toCharArray();
    private static final Random RAND = new Random();
}
