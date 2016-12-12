/*
// *$URL$
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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * This is a class to test the file utility class methods
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author dpiessen
 * @version $Revision$
 */
public class TU_IOUtils extends TestCase {

    /**
     * Tests the getFile routine by creating a temporary file and then using the
     * method to get it.
     */
    public void testGetFile() {
        File tmpFile = null;
        try {

            tmpFile = createTmpFile();

            byte[] data = IOUtils.readFile(tmpFile, false);

            assertEquals(5000, data.length);

        }
        catch (IOException e) {
            fail("File Create Failure");
        }
        finally {

            if (tmpFile != null && tmpFile.exists()) {
                if (!tmpFile.delete()) {
                    System.err.println("Couldn't delete file :" + tmpFile);
                }
            }

        }

    }

    /**
     * Tests the getFile routine by creating a temporary file and then using the
     * method to get it.
     */
    public void testGetFileCompressed() throws IOException {
        File tmpFile = null;
        try {

            tmpFile = createTmpFile();
            byte[] data = IOUtils.readFile(tmpFile, true);

            assertTrue(data.length < 5000);
        }
        finally {
            if (tmpFile != null && tmpFile.exists()) {
                if (!tmpFile.delete()) {
                    System.err.println("Couldn't delete file :" + tmpFile);
                }
            }
        }

    }

    /**
     * Tests the getFile routine to ensure it throws the proper exception if a
     * file is not found
     */
    public void testGetFileNotFound() throws IOException {
        
        try {
            IOUtils.readFile(new File("dlskfhsns.tst"), true);
            fail("Expected exception");
        }
        catch (FileNotFoundException e) {
            // normal
        }
    }
    
    /**
     * Creates a temporary file used to test get file methods and compression.
     * 
     * @return The File object for a temporary file
     * @throws IOException
     */
    private static File createTmpFile() throws IOException {
        File tmpFile = File.createTempFile(_fileName, _fileSuffix);

        FileOutputStream fstream = new FileOutputStream(tmpFile);
        DataOutputStream out = null;
        
        try {
            out = new DataOutputStream(fstream);
    
            byte[] someData = new byte[5000];
    
            // Writing the same char since compression doesn't work
            // well on random bytes
            for (int i = 0; i < 5000; i++)
                someData[i] = 'k';
    
            out.write(someData);
        }
        finally {
            if (out != null) {
                // Close the output stream
                out.close();
            }
        }

        return tmpFile;
    }

    private static String _fileName = "test";
    private static String _fileSuffix = ".dll";
}
