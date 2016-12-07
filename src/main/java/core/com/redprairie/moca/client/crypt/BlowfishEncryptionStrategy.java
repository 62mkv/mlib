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

package com.redprairie.moca.client.crypt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.redprairie.moca.client.ProtocolException;

/**
 * An implementation of EncryptionStrategy that utilizes the Blowfish protocol.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2006 RedPrairie Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class BlowfishEncryptionStrategy implements EncryptionStrategy {

    public BlowfishEncryptionStrategy() {
        try {
            blowfish = Cipher.getInstance("Blowfish");
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("unable to initialize protocol",
                e);
        }
        catch (NoSuchPaddingException e) {
            throw new IllegalArgumentException("unable to initialize protocol",
                e);
        }
    }

    @Override
    public byte[] decrypt(byte[] cipher) throws ProtocolException {
        return decrypt(cipher, 0, cipher.length);
    }
    
    @Override
    public byte[] decrypt(byte[] cipher, int offset, int len) throws ProtocolException {
        byte[] out;
        try {
            blowfish.init(Cipher.DECRYPT_MODE, BLOWFISH_KEY);
            out = blowfish.doFinal(cipher, offset, len);
        }
        catch (InvalidKeyException e) {
            throw new ProtocolException("encryption error", e);
        }
        catch (IllegalBlockSizeException e) {
            throw new ProtocolException("encryption error", e);
        }
        catch (BadPaddingException e) {
            throw new ProtocolException("encryption error", e);
        }
        return out;
    }

    @Override
    public byte[] encrypt(byte[] clear) throws ProtocolException {
        return encrypt(clear, 0, clear.length);
    }
    
    @Override
    public byte[] encrypt(byte[] clear, int offset, int len) throws ProtocolException {
        byte[] out;
        try {
            blowfish.init(Cipher.ENCRYPT_MODE, BLOWFISH_KEY);
            out = blowfish.doFinal(clear, offset, len);
        }
        catch (InvalidKeyException e) {
            throw new ProtocolException("encryption error", e);
        }
        catch (IllegalBlockSizeException e) {
            throw new ProtocolException("encryption error", e);
        }
        catch (BadPaddingException e) {
            throw new ProtocolException("encryption error", e);
        }
        return out;
    }

    public byte[] decrypt(byte[] cipher, SecretKeySpec secret)
            throws ProtocolException {
        byte[] out;
        try {
            blowfish.init(Cipher.DECRYPT_MODE, secret);
            out = blowfish.doFinal(cipher);
        }
        catch (InvalidKeyException e) {
            throw new ProtocolException("encryption error", e);
        }
        catch (IllegalBlockSizeException e) {
            throw new ProtocolException("encryption error", e);
        }
        catch (BadPaddingException e) {
            throw new ProtocolException("encryption error", e);
        }
        return out;
    }

    public byte[] encrypt(byte[] clear, SecretKeySpec secret)
            throws ProtocolException {
        byte[] out;
        try {
            blowfish.init(Cipher.ENCRYPT_MODE, secret);
            out = blowfish.doFinal(clear);
        }
        catch (InvalidKeyException e) {
            throw new ProtocolException("encryption error", e);
        }
        catch (IllegalBlockSizeException e) {
            throw new ProtocolException("encryption error", e);
        }
        catch (BadPaddingException e) {
            throw new ProtocolException("encryption error", e);
        }
        return out;
    }

    @Override
    public InputStream getInputWrapper(InputStream in, int max, int mode)
            throws IOException {
        try {
            blowfish.init(Cipher.DECRYPT_MODE, BLOWFISH_KEY);
            int read = 0;
            byte[] input = new byte[max];
            while (read < max) {
                int nbytes = in.read(input, read, max - read);
                read += nbytes;
            }
            return new ByteArrayInputStream(blowfish.doFinal(input));
        }
        catch (InvalidKeyException e) {
            throw new IllegalArgumentException("unexpected key failure", e);
        }
        catch (IllegalBlockSizeException e) {
            throw new IllegalArgumentException("unexpected key failure", e);
        }
        catch (BadPaddingException e) {
            throw new IllegalArgumentException("unexpected key failure", e);
        }
    }

    @Override
    public String getName() {
        return "blowfish";
    }

    //
    // Implementation
    //
    private static final byte[] BLOWFISH_KEY_BYTES = { (byte) 0x7e,
            (byte) 0xc1, (byte) 0xdb, (byte) 0xc5, (byte) 0xa9, (byte) 0x1b,
            (byte) 0x21, (byte) 0xda, (byte) 0x66, (byte) 0x7d, (byte) 0xd4,
            (byte) 0xc6, (byte) 0x01, (byte) 0x80, (byte) 0xff, (byte) 0x5a };
    private static final SecretKeySpec BLOWFISH_KEY = new SecretKeySpec(
        BLOWFISH_KEY_BYTES, "Blowfish");
    private final Cipher blowfish;
}
