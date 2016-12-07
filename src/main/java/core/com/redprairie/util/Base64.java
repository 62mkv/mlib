/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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

package com.redprairie.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;

import com.redprairie.moca.MocaInterruptedException;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class Base64 {
    public static String encode(byte[] in) {
        StringBuilder tmp = new StringBuilder();
        try {
            Base64.encode(new ByteArrayInputStream(in), tmp);
        }
        catch (IOException e) {
            // This should never happen with ByteArrayInputStream.
            throw new IllegalArgumentException(e);
        }
        return tmp.toString();
    }

    public static void encode(InputStream raw, Appendable out) throws IOException
    {
        try {
            byte[] tmp = new byte[3];
            while (true)
            {
                int n = raw.read(tmp);
                if (n < 0) break;
                encodeBlock(tmp, n, out);
            }
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
    }
    
    public static byte[] decode(String base64) {
        ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
        StringReader tmpIn = new StringReader(base64);
        try {
            decode(tmpIn, tmpOut);
            return tmpOut.toByteArray();
        }
        catch (IOException e) {
            // Shouldn't happen since we have in-memory streams.
            throw new IllegalArgumentException(e);
        }
    }

    public static void decode(Reader base64, OutputStream out) throws IOException
    {
        try {
            char[] cooked = new char[4];
            outer:
            while (true)
            {
                int length;
                for (length = 0; length < 4; length++) {
                    do {
                        int ch = base64.read();
                        if (ch == -1) break outer;
                        cooked[length] = (char) ch;
                    } while (getValue(cooked[length]) == -1);
                }
                
                if (length != 4) {
                    throw new IOException("Got unexpected base64 buffer");
                }
                
                int block = (getValue(cooked[0]) << 18) | 
                            (getValue(cooked[1]) << 12) | 
                            (getValue(cooked[2]) << 6)  | 
                            (getValue(cooked[3]));
                
                int max;
                if (cooked[2] == '=') max = 1;
                else if (cooked[3] == '=') max = 2;
                else max = 3;
                
                for (int j = 0; j < max; j++)
                    out.write(((block >> (8 * (2 - j))) & 0xff));
            }
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
    }

    private static int getValue(char c)
    {
        if (c >= 'A' && c <= 'Z')
            return c - 'A';
        if (c >= 'a' && c <= 'z')
            return c - 'a' + 26;
        if (c >= '0' && c <= '9')
            return c - '0' + 52;
        if (c == '+')
            return 62;
        if (c == '/')
            return 63;
        if (c == '=')
            return 0;
        return -1;
    }

    private static void encodeBlock(byte[]raw, int length, Appendable out) throws IOException
    {
        int block = 0;
        
        for (int i = 0; i < length; i++)
        {
            byte b = raw[i];
            int neuter = (b < 0) ? b + 256 : b;
            block += neuter << (8 * (2 - i));
        }

        int outsize;
        if (length == 2) outsize = 3;
        else if (length == 1) outsize = 2;
        else outsize = 4;
        
        for (int i = 0; i < 4; i++)
        {
            int sixbit = (block >>> (6 * (3 - i))) & 0x3f;
            if (i < outsize)
                out.append(getChar(sixbit));
            else
                out.append('=');
        }
    }

    private static char getChar(int sixBit)
    {
        if (sixBit >= 0 && sixBit <= 25)
            return (char) ('A' + sixBit);
        if (sixBit >= 26 && sixBit <= 51)
            return (char) ('a' + (sixBit - 26));
        if (sixBit >= 52 && sixBit <= 61)
            return (char) ('0' + (sixBit - 52));
        if (sixBit == 62)
            return '+';
        if (sixBit == 63)
            return '/';
        return '?';
    }

}
