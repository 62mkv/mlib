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

package com.redprairie.moca.client.crypt;

import java.io.IOException;
import java.io.InputStream;

import com.redprairie.moca.client.ProtocolException;

/**
 * An interface describing the encryption mechanism to be used for the MOCA
 * protocol encryption.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public interface EncryptionStrategy {
    
    public static final int ENCRYPT_MODE = 1;
    public static final int DECRYPT_MODE = 2;
    
    /**
     * Returns the agreed-upon name for this encryption method.  This must
     * match the encryption methods available in the MOCA server code base.
     * @return the name of this encryption approach.
     */
    public String getName();
    
    /**
     * Returns the encrypted form of the given data.
     * @param clear the original data to be encrypted.
     * @param offset the position in the array of the data to be encrypted.
     * @param len the length of the data to be encrypted.
     * @return a byte array (possibly the identical one) that contains the
     * encrypted form of the given data.
     */
    public byte[] encrypt(byte[] clear, int offset, int len) throws ProtocolException;
    
    /**
     * Returns the encrypted form of the given data.
     * @param clear the original data to be encrypted.
     * @param clear
     * @return a byte array (possibly the identical one) that contains the
     * encrypted form of the given data.
     * @throws ProtocolException
     */
    public byte[] encrypt(byte[] clear) throws ProtocolException;

    /**
     * Returns the decrypted form of the given data.
     * @param cipher the encrypted data to be dencrypted.
     * @param offset the position in the array of the data to be decrypted.
     * @param len the length of the data to be decrypted.
     * @return a byte array (possibly the identical one) that contains the
     * decrypted form of the given (encrypted) data.
     */
    public byte[] decrypt(byte[] cipher, int offset, int len) throws ProtocolException;
    
    /**
     * Returns the decrypted form of the given data.
     * @param cipher the encrypted data to be dencrypted.
     * @return a byte array (possibly the identical one) that contains the
     * decrypted form of the given (encrypted) data.
     */
    public byte[] decrypt(byte[] cipher) throws ProtocolException;
    
    /**
     * Returns an InputStream that wraps the given input stream with one
     * that encrypts or decrypts the data read from the given input stream. 
     * @param in an input stream which contains the data to be read.
     * @param max the maximum number of bytes to read encrypted.
     * @param mode either ENCRYPT_MODE or DECRYPT_MODE
     * @return an instance of InputStream that will return bytes read from the
     * underlying stream after processing them with the encryption strategy.
     */
    public InputStream getInputWrapper(InputStream in, int max, int mode) throws IOException;
}
