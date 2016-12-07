/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2007
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Simple file I/O operations.
 * 
 * <b><pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dpiessen
 * @version $Revision$
 */
public class IOUtils {

    /**
     * Reads a file's contents into a byte array.  This method should not be used
     * to read very large files, as the entire file is read into memory. 
     * @param pathname The file path to retrieve the file from
     * @param compress If set to true, the returned byte stream is compressed
     * @return a byte array of the returned file
     * @throws IOException
     */
    public static byte[] readFile(File file, boolean compress)
            throws IOException {

        FileInputStream in = new FileInputStream(file);

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            OutputStream out;
            if (compress) {
                out = new GZIPOutputStream(buffer);
            }
            else {
                out = buffer;
            }
            
            copy(in, out);

            out.close();
            return buffer.toByteArray();
        }
        finally {
            try { 
                in.close();
            }
            catch (IOException ignore) {
                // ignore
            }
        }
    }
    
    public static void copy(InputStream in, OutputStream out)
            throws IOException {
        byte[] tmp = new byte[1024];
        while (true) {
            int bytesRead = in.read(tmp, 0, tmp.length);
            if (bytesRead == -1) {
                break;
            }
            out.write(tmp, 0, bytesRead);
        }
        out.flush();
    }
}