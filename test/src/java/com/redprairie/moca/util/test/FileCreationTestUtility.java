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

package com.redprairie.moca.util.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Random;

import junit.framework.Assert;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileCreationTestUtility {
    
    /**
     * This will create a temporary file with the given prefix.  It will then
     * write into the file given the values provided.  It will return the file
     * after marking the file for deletion upon exit of the JVM.
     * @param prefix The prefix to give the temporary file
     * @param values The values to stuff in the temporary file after creating
     * @param charset The character set to write the file in
     * @return the file pointer that we just created.  This will also fail
     *         junits if there is some kind of IOException while creating
     *         the temporary file or their were issues writing to the
     *         temporary file.  Also if either argument is null
     *         will cause assertion failure
     */
    public File createTempFileAndInsertValues(String prefix, String values,
        String charset) {
        
        Assert.assertNotNull("Prefix cannot be null", prefix);
        Assert.assertNotNull("Values cannot be null", values);
        
        File tempFile = null;
        try {
            tempFile = File.createTempFile(prefix, null);
            // Register file deletion on exit
            tempFile.deleteOnExit();
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception creating temporary file: " + e);
        }
        
        BufferedWriter out = null;
        
        try {
            charset = charset != null ? charset : Charset.defaultCharset().displayName();
            out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tempFile), charset));
            
            out.write(values);
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Unexpected exception writing to temporary file: " + e);
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException ignore) {
                    ignore.printStackTrace();
                }
            }
        }
        
        return tempFile;
    }
    
    /**
     * This will compare the contents of the passed in file with the contents
     * of the string itself.  It will verify the file exists and that we can
     * read it. It will delete the file after comparison.
     * @param fileName The full location of the file
     * @param comparedValue
     * @param charset The character set the file is in
     * @return this will throw junit failures if: file doesn't exists, file 
     *         object is not physically a file, we cannot read the file, the
     *         file has a different amount of characters than the string or
     *         if the characters do not exactly match.  Also if there is any
     *         type of IO exception while processing the file it will result
     *         in a failure on the junit.  Also if either argument is null
     *         will cause assertion failure
     */
    public void compareFileContentsWithString(File createdFile, 
            String comparedValue, String charset) {
        
        Assert.assertNotNull("File cannot be null", createdFile);
        Assert.assertNotNull("Compared Values cannot be null", comparedValue);
        
        Assert.assertTrue("Copy didn't create anything", createdFile.exists());

        // Since we know the file exists, mark for deletion
        createdFile.deleteOnExit();
        
        BufferedReader in = null;
        try {

            Assert.assertTrue("Copy created something but not a file", createdFile.isFile());

            Assert.assertTrue("Copy created something we couldn't read", createdFile.canRead());
            
            charset = charset != null ? charset : Charset.defaultCharset().displayName();
            
            in = new BufferedReader(new InputStreamReader(
                new FileInputStream(createdFile), charset));
            CharBuffer originalBuffer = CharBuffer.wrap(comparedValue.toCharArray());
            CharBuffer copyBuffer = CharBuffer.allocate(comparedValue.length());
            // Copy the values from the file into the char buffer
            int readCount = in.read(copyBuffer);
            
            Assert.assertEquals("We couldn't read in correct number of characters " +
                        "from the file", originalBuffer.length(), readCount);
            
            // Set the positions on the buffers to be zero, this way
            // the equals can work for the buffers
            originalBuffer.position(0);
            copyBuffer.position(0);
            
            // Compare the 2 buffers, they should be identical
            Assert.assertEquals("The characters read in don't match",
                    originalBuffer, copyBuffer);
            
            Assert.assertEquals("Was not at the end of the file!", -1, in.read());
        }
        catch (FileNotFoundException e) {
            // This should not happen as we checked above
            e.printStackTrace();
            Assert.fail("The file was not found: " + e);
        }
        catch (IOException e) {
            e.printStackTrace();
            Assert.fail("There was an error reading from the buffer: " + e);
        }
        finally {
            
            if (!createdFile.delete()) {
                _logger.warn("Could not delete file: " + createdFile);
            }
            
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
    
    public String getRandomString(int characters) {
        StringBuilder buf = new StringBuilder(characters);
        for (int i = 0; i < characters; i++) {
            buf.append((char)_random.nextInt(0xD800)); // Avoid surrogate pairs
        }
        
        return buf.toString();
    }
    
    private static final Logger _logger = LogManager.getLogger(
            FileCreationTestUtility.class);
    private static final Random _random = new Random();
}
