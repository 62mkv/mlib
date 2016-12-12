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

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.util.Base64;

/**
 * This class is to make sure that when doing red prairie blowfish
 * encode, that it can be properly decoded.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_BlowfishUtils {
    @BeforeClass
    public static void setupClass() throws SystemConfigurationException {
        ServerUtils.setupDaemonContext(TU_BlowfishUtils.class.getName(), true);
    }
    
    @Test
    public void testEncodeAndDecodeCCallsCorrectly() 
            throws NoSuchAlgorithmException, IOException, MocaException {
        String originalString = "This is the original string, don't mess with.";
        String CencodedString = encodeCSide(originalString);
        String encodedString = BlowfishUtils.encodePassword(originalString);
        
        String decodedString = decodeCSide(encodedString);
        String CdecodedString = decodeCSide(CencodedString);
        
        Assert.assertEquals(originalString, decodedString);
        Assert.assertEquals(originalString, CdecodedString);
    }
    
    @Test
    public void testEncodeAndDecodeJavaCallsCorrectly() 
            throws NoSuchAlgorithmException, IOException, MocaException {
        String originalString = "This is the original string, don't mess with.";
        String CencodedString = encodeCSide(originalString);
        String encodedString = BlowfishUtils.encodePassword(originalString);
        
        String JAVAdecodedString = decodeValue(encodedString);
        String JAVACdecodedString = decodeValue(CencodedString);
        
        Assert.assertEquals(originalString, JAVAdecodedString);
        Assert.assertEquals(originalString, JAVACdecodedString);
    }
    
    
    private String encodeCSide(String password) throws MocaException {
        MocaContext moca = MocaUtils.currentContext();
        
        HashMap<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("password", password);
        
        MocaResults res = moca.executeCommand("test blowfish encode", arguments);
        
        res.next();
        
        return res.getString(0);
    }
    
    private String decodeCSide(String encoded) throws MocaException {
        MocaContext moca = MocaUtils.currentContext();
        
        HashMap<String, Object> arguments = new HashMap<String, Object>();
        arguments.put("encoded", encoded);
        
        MocaResults res = moca.executeCommand("test blowfish decode", arguments);
        
        res.next();
        
        return res.getString(0);
    }
    
    /**
     * This is just a copy of the code from the RegistryReader where it decodes
     * the string from the registry.
     * @param encoded
     * @return
     */
    private String decodeValue(String encoded) {
        // Blowfish encoding ins indicated by a prefix |B|
        if (encoded == null || !encoded.startsWith("|B|")) {
            return encoded;
        }
        else {
            String beforeDecode = encoded.substring(3);
            byte[] raw = Base64.decode(beforeDecode);
            Cipher blowfish;
            try {
                blowfish = Cipher.getInstance("Blowfish");
                byte[] keyBytes = new byte[16];
                System.arraycopy(raw, 0, keyBytes, 0, 16);            
                SecretKeySpec key = new SecretKeySpec(keyBytes, "Blowfish");
                byte[] dataBytes = new byte[raw.length - 16];
                System.arraycopy(raw, 16, dataBytes, 0, raw.length - 16);

                blowfish.init(Cipher.DECRYPT_MODE, key);
                byte[] clearBytes = blowfish.doFinal(dataBytes);
                
                // Special case -- if the string contains a 0 as the last byte,
                // remove it.  This is a remnant of the C convention of using
                // null-terminated strings and encoding them with the null for
                // convenience of memory allocation.
                if (clearBytes.length > 0 && clearBytes[clearBytes.length - 1] == (byte)0) {
                    return new String(clearBytes, 0, clearBytes.length - 1, Charset.forName("UTF-8"));
                }
                else {
                    return new String(clearBytes, Charset.forName("UTF-8"));
                }
            }
            catch (GeneralSecurityException e) {
                throw new IllegalArgumentException("unable to decode key", e);
            }
            
        }
    }
}
