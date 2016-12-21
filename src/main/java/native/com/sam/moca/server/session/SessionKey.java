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

package com.sam.moca.server.session;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.sam.moca.util.MD5Utils;

/**
 * Generates MOCA session keys for use in communication with legacy MOCA servers.
 * 
 * @author dinksett
 * @version $Revision$
 */
public class SessionKey {
    
    // Package access
    public static String generateKey(String userId, String domain, String seed) {
        if (domain == null) domain = "";
        if (userId == null) userId = "";
        
        StringBuilder tmp = new StringBuilder();
        tmp.append(domain);
        tmp.append('+');
        tmp.append(userId);
        tmp.append('-');
        tmp.append(seed.subSequence(0, 4));
        tmp.append('$');
        
        /* Keep this as a broken-out string, to (slightly) obfuscate the bytecode */
        tmp.append('r');
        tmp.append('e');
        tmp.append('l');
        tmp.append('a');
        tmp.append('x');
        tmp.append('/');
        tmp.append('d');
        tmp.append('o');
        tmp.append('n');
        tmp.append('(');
        tmp.append('t');
        tmp.append('@');
        tmp.append('w');
        tmp.append('o');
        tmp.append('r');
        tmp.append('r');
        tmp.append('y');
        tmp.append('&');
        tmp.append('h');
        tmp.append('a');
        tmp.append('v');
        tmp.append('e');
        tmp.append('>');
        tmp.append('a');
        tmp.append('?');
        tmp.append('h');
        tmp.append('o');
        tmp.append('m');
        tmp.append('e');
        tmp.append('*');
        tmp.append('b');
        tmp.append('r');
        tmp.append('e');
        tmp.append('w');
        
        int pound = domain.indexOf('#');
        if (pound != -1) {
            domain = domain.substring(0, pound);
        }
        
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] data = tmp.toString().getBytes("UTF-8");
            md5.update(data);
            byte[] hash = md5.digest();
            return domain + ';' + userId + ';' + seed.substring(0, 4)+ MD5Utils.encode(hash, 6);
        }
        catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("UTF-8 not supported -- Bad Stuff!");
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unable to find MD5 algorithm", e);
        }
    }   
}
