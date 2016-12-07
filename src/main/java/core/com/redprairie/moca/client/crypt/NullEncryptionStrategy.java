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

import java.io.InputStream;

/**
 * An encryption strategy class that doesn't perform any encryption.
 * 
 * <b><pre>
 * Copyright (c) 2006 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class NullEncryptionStrategy implements EncryptionStrategy {

    @Override
    public byte[] decrypt(byte[] cipher) {
        return cipher;
    }

    @Override
    public byte[] decrypt(byte[] cipher, int offset, int length) {
        if (length == cipher.length && offset == 0) {
            return cipher;
        }
        else {
            byte[] out = new byte[length];
            System.arraycopy(cipher, offset, out, 0, length);
            return out;
        }
    }

    @Override
    public byte[] encrypt(byte[] clear) {
        return clear;
    }

    @Override
    public byte[] encrypt(byte[] clear, int offset, int length) {
        if (length == clear.length && offset == 0) {
            return clear;
        }
        else {
            byte[] out = new byte[length];
            System.arraycopy(clear, offset, out, 0, length);
            return out;
        }
    }

    @Override
    public InputStream getInputWrapper(InputStream in, int max, int mode) {
        return in;
    }

    @Override
    public String getName() {
        return "";
    }
}
