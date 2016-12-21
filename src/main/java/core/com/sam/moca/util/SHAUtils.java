/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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
package com.sam.moca.util;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * 
 * Utility methods to allow common encryption tools to hash a given password
 * using SHA.  This class uses the 512 bit algorithm for it's hashes.
 *
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class SHAUtils {

    /**
     * Our own private base64 encoder...sort of.  This is designed to match 
     * the implementation of the Sam legacy misMD5 base64 encoder.  Note:
     * this implementation of base64 is NOT a real base64 encoder.  It's also not
     * a valid two-way encoder/decoder, since there's no way to reliably determine
     * the length of the original array if the <code>bits</code> argument is not a
     * power of two.  This is effective for storing hashed values, but not much more.
     * 
     * @param in the input array.  This represents the buffer to be encoded. 
     * @param bits the number of bits to use to encode the data in <code>in</code>.
     * <code>bits</code> can be any number from 2 to 6, but the most common values are
     * 4 or 6, for hexadecimal or "legacy" base 64.
     * @return a string representing the encoded form of the buffer.
     */
    public static String encode(byte[] in, int bits) {
        int bitpos, lo, hi;
        char[] charset = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_.".toCharArray();

        if (bits > 6 || bits < 2)
            throw new IllegalArgumentException("invalid encoding: " + bits);

        StringBuilder buf = new StringBuilder();

        for (bitpos = 0; bitpos < (in.length * 8); bitpos += bits) {
            lo = (in[bitpos / 8] & 0xff) >> (bitpos % 8);
            hi = ((bitpos + bits - 1) < (in.length * 8))?
                    (in[(bitpos + bits - 1) / 8] & 0xff) << (8 - bitpos % 8):
                    0;
            buf.append(charset[(lo | hi) & ((1<<bits)-1)]);
        }
        return buf.toString();
    }
    
    /**
     * Hashes the password to SHA-256 format.
     * 
     * @param password The user password.
     * @param seed The password seed.
     * @return A hashed password.
     */
    public static String hashPassword(String password, String seed) {

        if (seed == null) {
            seed = createSeed();
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.update(password.getBytes(_charset));
            digest.update(seed.getBytes(_charset));
            digest.update(_secret);

            return seed + encode(digest.digest(), 5);
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * @param clearTextPassword The password to check against the hashed 
     *        password.
     * @param hashedPassword The hashed password to check against
     * @return whether or not the clear text password matches the hashed value
     * @throws NullPointerException This is thrown if either of the passwords
     *         passed in are null.
     */
    public static boolean validateHashedPassword(String clearTextPassword, 
            String hashedPassword) throws NullPointerException {
        String hashedPasswordIn = null;
        if (hashedPassword.startsWith(_hashPipe)) {
            int seedAndPipeLength = _seedLength + _hashPipe.length();
            // We only do the hash if the length is long enough
            if (hashedPassword.length() > seedAndPipeLength + 
                    _hashPipe.length()) {
                hashedPasswordIn = _hashPipe + hashPassword(clearTextPassword, 
                        hashedPassword.substring(_hashPipe.length(), 
                                seedAndPipeLength));
            }
        }
        else {
            // We only do the hash if the length is long enough.  This could
            // be changed to do just assign in which case would support clear 
            // text password in the value.
            if (hashedPassword.length() > _seedLength) {
                hashedPasswordIn = hashPassword(clearTextPassword, 
                        hashedPassword.substring(0, 
                                _seedLength)); 
            }
        }
        
        return hashedPassword.equals(hashedPasswordIn);
    }

    /**
     * Creates a seed value for the password hash
     * 
     * @return A seed value
     */
    private static String createSeed() {
        int size = _seedLength;

        StringBuilder result = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            result.append(alpha[random.nextInt(alpha.length)]);
        }

        return result.toString();
    }
    
    public static final String _hashPipe = "|S|";

    private static final char[] alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final byte[] _secret = new byte[] { 0x49, (byte) 0xf2, 0x13,
                    0x34, 0x05, 0x26, 0x46, (byte) 0xae, 0x08, (byte) 0xc2, 0x79,
                    (byte) 0xbf, (byte) 0xba, (byte) 0xbc, 0x40, 0x7c };
    private static final Charset _charset = Charset.forName("UTF-8");
    private static final Random random = new Random();
    private static final int _seedLength = 4;
}