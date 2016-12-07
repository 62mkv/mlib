/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.redprairie.moca.client.ProtocolException;
import com.redprairie.moca.client.crypt.BlowfishEncryptionStrategy;
import com.redprairie.util.Base64;

/**
 * This class contains functions for use with blowfish encoding passwords.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class BlowfishUtils {
    /**
     * Encoding Algorithm
     *
     *     password p1 = "foo"
     *     date d1 = "2008/03/25 11:22:33"
     *     blowfish key k1 = MD5(d1)
     *     blowfish text b1 = BlowfishEncode(key = k1, text = p1)
     *     concatentated text c1 = "<k1><b1>"
     *     base64 text b64 = B64Encode(c1)
     *     password e1 = "|B|<b64>"
     *     
     * @param password The password to encode
     * @return The blowfish encoded password
     * @throws ProtocolException 
     * @throws NoSuchAlgorithmException 
     * @throws IOException 
     */
    public static String encodePassword(String password) throws ProtocolException,
            NoSuchAlgorithmException, IOException {
        // First we get the date time as a MD5 hash code value; we use this
        // as the blow fish key
        MessageDigest md = MessageDigest.getInstance("MD5");
        
        String dateTimeString = _dateFormat.print(new Date().getTime());
        
        byte[] dateBytes = dateTimeString.getBytes(Charset.forName("UTF-8"));
        
        byte[] blowKey = md.digest(dateBytes);
        
        BlowfishEncryptionStrategy encryption = new BlowfishEncryptionStrategy();
        
        // We are just using the default charset, which will almost always be
        // UTF-8
        byte[] input = password.getBytes(Charset.forName("UTF-8"));
        
        // Now we actually encrypt the password using the current date as
        // the key
        byte[] encodedBytes = encryption.encrypt(input, 
                new SecretKeySpec(blowKey, "Blowfish"));
        
        byte[] byteBuffer = new byte[blowKey.length + encodedBytes.length];
        
        // Now we copy the 2 byte arrays into the larger one to encode as base
        // 64 afterwards so we can return our encoded value
        System.arraycopy(blowKey, 0, byteBuffer, 0, blowKey.length);
        System.arraycopy(encodedBytes, 0, byteBuffer, blowKey.length, 
                encodedBytes.length);
        
        StringWriter stringWriter = new StringWriter();
        
        Base64.encode(new ByteArrayInputStream(byteBuffer), stringWriter);
        
        // We put a |B| at the front of the base 64 encoded password to ensure
        // that we know when decoding that is our same format
        return "|B|" + stringWriter.toString();
    }

    private static final DateTimeFormatter _dateFormat = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");
}
