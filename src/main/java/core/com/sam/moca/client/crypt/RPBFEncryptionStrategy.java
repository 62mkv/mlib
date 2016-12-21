/*
 *  $RCSfile: $
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

package com.sam.moca.client.crypt;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An implementation of EncryptionStrategy that implements the Sam
 * "bit flip" protocol.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class RPBFEncryptionStrategy implements EncryptionStrategy {

    @Override
    public byte[] decrypt(byte[] cipher) {
        return decrypt(cipher, 0, cipher.length);
    }
    
    @Override
    public byte[] decrypt(byte[] cipher, int offset, int length) {
        byte[] out = new byte[length];
        for (int i = 0; i < length; i++) {
            out[i] = (byte)(cipher[i + offset] ^ ((i % 255)+1));
        }
        return out;
    }

    @Override
    public byte[] encrypt(byte[] clear) {
        return encrypt(clear, 0, clear.length);
    }
    
    @Override
    public byte[] encrypt(byte[] clear, int offset, int length) {
        byte[] out = new byte[length];
        for (int i = 0; i < length; i++) {
            out[i] = (byte)(clear[i + offset] ^ ((i % 255)+1));
        }
        return out;
    }
    
    @Override
    public InputStream getInputWrapper(InputStream in, int max, int mode) {
        return new WrapperInputStream(in);
    }

    @Override
    public String getName() {
        return "rpbf";
    }
    
    //
    // Implementation
    //
    private static class WrapperInputStream extends FilterInputStream {
        public WrapperInputStream(InputStream in) {
            super(in);
        }
        
        // @see java.io.FilterInputStream#read()
        @Override
        public int read() throws IOException {
            int orig = super.read();
            if (orig == -1) return orig;
            
            return (byte)(orig ^ ((_count++ % 255) + 1));
        }
        
        // @see java.io.FilterInputStream#read(byte[], int, int)
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int nbytes = super.read(b, off, len);
            for (int i = 0; i < nbytes; i++) {
                b[i + off] = (byte)(b[i + off] ^ ((_count++ % 255)+1));
            }
            return nbytes;
        }
        
        // @see java.io.FilterInputStream#skip(long)
        @Override
        public long skip(long n) throws IOException {
            long nbytes = super.skip(n);
            _count += nbytes;
            return nbytes;
        }
        
        private int _count = 0;
    }
    
}
