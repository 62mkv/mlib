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

package com.redprairie.moca.components.base;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.util.AbstractMocaTestCase;
import com.redprairie.moca.util.TU_MocaUtils;
import com.redprairie.moca.util.test.FileCreationTestUtility;

/**
 * This class is to test the moca commands that call into the 
 * FileDirectoryService java component classes
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_FileDirectoryService extends AbstractMocaTestCase {
    /**
     * This method is to test the copy file moca command.
     * @see TU_MocaUtils#testFileCopy()
     */
    public void testFileCopy() {
        FileCreationTestUtility fileCreationTestUtility = 
            new FileCreationTestUtility();
        String prefix = "unittest";
        String fileInput = "This is a temporary file for testing\nIt will be " +
                "deleted/t so don't worry!\n$!!00testXA~";
        
        File tempFile = fileCreationTestUtility.createTempFileAndInsertValues(
                prefix, fileInput, null);
        
        // Create a target file in the same directory but call it test.copy
        String targetFile = tempFile.getParent() + File.separator + 
                "test.copy";
        
        try {
            _moca.executeCommand(
                    "copy file " +
            	    "  where src = '" + tempFile.getAbsolutePath() + "'" + 
            	    "    and dest = '" + targetFile + "'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception: " + e);
        }
        
        File createdFile = new File(targetFile);
        
        fileCreationTestUtility.compareFileContentsWithString(createdFile, 
                fileInput, null);
    }
    
    /**
     * This will test to make sure that directory creation works correctly by
     * creating a directory and sub directory in the temp directory.  It then
     * subsequently tries to delete that directory
     */
    public void testDirectoryCreation() {
        String temporaryDirectory = System.getProperty("java.io.tmpdir");
        
        // Made up a weird name to make sure not to clash
        String directoryToCreate = temporaryDirectory + File.separator + 
                "unit%13MOCANG-13" + File.separator + "test";
        
        // Make sure we can get the temporary directory
        assertNotNull(temporaryDirectory);
        
        try {
            // Create a directory in the temp directory called unit/test
            _moca.executeCommand(
                    "create directory " +
                    "  where directory = '" + directoryToCreate + "'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception: " + e);
        }
        
        File createdDirectory = new File(directoryToCreate);
        
        assertTrue(createdDirectory.exists());
        
        assertTrue(createdDirectory.isDirectory());
        
        // Delete the top directory
        assertTrue("Unable to delete created sub-directory", createdDirectory.delete());
        
        // The parent should be the unit directory we just made
        File unitDirectory = createdDirectory.getParentFile();
        
        // Finally delete the parent as well
        assertTrue("Unable to delete created directory", unitDirectory.delete());
    }
    
    /**
     * This will test a basic file find with a 
     */
    public void testFileFindSingle() {
        String temporaryDirectory = System.getProperty("java.io.tmpdir") + 
                File.separator + "unit%14MOCANG-13";
        
        File tempDir = new File(temporaryDirectory);
        
        // Now make the temporary directory
        if (!tempDir.mkdir()) {
            _logger.debug("Directory [" + tempDir + "] could not be created, " +
                        "could be because it exists already");
        }
        
        // Mark this directory for deletion on exit
        tempDir.deleteOnExit();
        
        File createdFile1 = null;
        File createdFile2 = null;
        File createdFile3 = null;
        try {
            createdFile1 = File.createTempFile("test1", ".test", tempDir);
            createdFile2 = File.createTempFile("test2", ".xml", tempDir);
            createdFile3 = File.createTempFile("test2", ".test", tempDir);
            
            MocaResults results = null;
            try {
                // This should return only the createdFile2
                results = _moca.executeCommand(
                        "find file" +
                        "  where pathname = '" + tempDir + File.separator + createdFile2.getName() + "'" +
                        "    and sort = 'Y'");
            }
            catch (MocaException e) {
                e.printStackTrace();
                fail("Unexpected MOCA Exception encountered: " + e);
            }
            
            RowIterator rowIter = results.getRows();
            
            assertTrue("There should be 1 row", rowIter.next());
            
            assertEquals("The file returned is not correct", 
                createdFile2.getAbsolutePath(), rowIter.getString("pathname"));
            assertEquals("Should have been a file", "F", rowIter.getString("type"));
            
            assertFalse("There should be 1 rows only", rowIter.next());
            
        }
        catch (IOException e) {
            e.printStackTrace();
            fail("Error while creating temporary files: " + e);
        }
        finally {
            if (createdFile1 != null) {
                if (!createdFile1.delete()) {
                    _logger.warn("Could not delete file: " + createdFile1);
                }
            }
            
            if (createdFile2 != null) {
                if (!createdFile2.delete()) {
                    _logger.warn("Could not delete file: " + createdFile2);
                }
            }
            
            if (createdFile3 != null) {
                if (!createdFile3.delete()) {
                    _logger.warn("Could not delete file: " + createdFile3);
                }
            }
            
            if (tempDir != null) {
                if (!tempDir.delete()) {
                    _logger.warn("Could not delete directory: " + tempDir);
                } 
            }
        }
    }
    
    /**
     * This will test a basic file find with a 
     */
    public void testFileFindSorted() {
        String temporaryDirectory = System.getProperty("java.io.tmpdir") + 
                File.separator + "unit%14MOCANG-13";
        
        File tempDir = new File(temporaryDirectory);
        
        // Now make the temporary directory
        if (!tempDir.mkdir()) {
            _logger.debug("Directory [" + tempDir + "] could not be created, " +
            		"could be because it exists already");
        }
        
        // Mark this directory for deletion on exit
        tempDir.deleteOnExit();
        
        File createdFile1 = null;
        File createdFile2 = null;
        File createdFile3 = null;
        try {
            createdFile1 = File.createTempFile("test1", ".test", tempDir);
            createdFile2 = new File(tempDir, "test2.xml");
            createdFile2.createNewFile();
            createdFile3 = new File(tempDir, "test2.test");
            createdFile3.createNewFile();
            
            MocaResults results = null;
            try {
                // This should return only the createdFile2
                results = _moca.executeCommand(
                        "find file" +
                	"  where pathname = '" + tempDir + File.separator + "*t2*.*'" +
                        "    and sort = 'Y'");
            }
            catch (MocaException e) {
                e.printStackTrace();
                fail("Unexpected MOCA Exception encountered: " + e);
            }
            
            RowIterator rowIter = results.getRows();
            
            assertTrue("There should be 2 rows", rowIter.next());
            
            assertEquals("The file returned is not correct", 
                    createdFile3.getAbsolutePath(), rowIter.getString("pathname"));
            assertEquals("Should have been a file", "F", rowIter.getString("type"));
            
           assertTrue("There should be 2 rows", rowIter.next());
            
           assertEquals("The file returned is not correct", 
                    createdFile2.getAbsolutePath(), rowIter.getString("pathname"));
           assertEquals("Should have been a file", "F", rowIter.getString("type"));
            
            assertFalse("There should be 2 rows only", rowIter.next());
            
        }
        catch (IOException e) {
            e.printStackTrace();
            fail("Error while creating temporary files: " + e);
        }
        finally {
            if (createdFile1 != null) {
                if (!createdFile1.delete()) {
                    _logger.warn("Could not delete file: " + createdFile1);
                }
            }
            
            if (createdFile2 != null) {
                if (!createdFile2.delete()) {
                    _logger.warn("Could not delete file: " + createdFile2);
                }
            }
            
            if (createdFile3 != null) {
                if (!createdFile3.delete()) {
                    _logger.warn("Could not delete file: " + createdFile3);
                }
            }
            
            if (tempDir != null) {
                if (!tempDir.delete()) {
                    _logger.warn("Could not delete directory: " + tempDir);
                } 
            }
        }
    }
    
    /**
     * This will test a basic file find to make sure it returns by modification
     * date if name is not specified.
     * @throws InterruptedException 
     */
    public void testFileFindSortedByModificationDate() throws InterruptedException {
        String temporaryDirectory = System.getProperty("java.io.tmpdir") + 
                File.separator + "unit%14MOCANG-13";
        
        File tempDir = new File(temporaryDirectory);
        
        // Now make the temporary directory
        if (!tempDir.mkdir()) {
            _logger.debug("Directory [" + tempDir + "] could not be created, " +
                        "could be because it exists already");
        }
        
        // Mark this directory for deletion on exit
        tempDir.deleteOnExit();
        
        // We have to sleep since the modification date can have varying
        // resolutions in when it updates.  1 second seems to be good on linux
        long sleepTimer = 1000;
        
        File createdFile1 = null;
        File createdDir1 = null;
        File createdFile2 = null;
        File createdFile3 = null;
        try {
            createdFile1 = File.createTempFile("test1", ".test", tempDir);
            Thread.sleep(sleepTimer);
            createdDir1 = new File(tempDir, "dir.test");
            if (!createdDir1.mkdir()) {
                _logger.debug("Directory [" + createdDir1 + "] could not be created, " +
                            "could be because it exists already");
            
            }
            Thread.sleep(sleepTimer);
            // Mark this directory for deletion on exit
            tempDir.deleteOnExit();
            
            createdFile2 = File.createTempFile("test2", ".test", tempDir);
            Thread.sleep(sleepTimer);
            createdFile3 = File.createTempFile("test2", ".xml", tempDir);
            Thread.sleep(sleepTimer);
            
            MocaResults results = null;
            try {
                // This should return only the createdFile2
                results = _moca.executeCommand(
                        "find file" +
                        "  where pathname = '" + tempDir + File.separator + 
                        "*.test'");
            }
            catch (MocaException e) {
                e.printStackTrace();
                fail("Unexpected MOCA Exception encountered: " + e);
            }
            
            RowIterator rowIter = results.getRows();
            
            assertTrue("There should be 3 rows", rowIter.next());
            
            assertEquals("The file returned is not correct", 
                    createdFile1.getAbsolutePath(), rowIter.getString("pathname"));
            assertEquals("Should have been a file", "F", rowIter.getString("type"));
            
            assertTrue("There should be 3 rows", rowIter.next());
            
            assertEquals("The file returned is not correct", 
                    createdDir1.getAbsolutePath(), rowIter.getString("pathname"));
            assertEquals("Should have been a directory", "D", rowIter.getString("type"));
            
            assertTrue("There should be 3 rows", rowIter.next());
            
            assertEquals("The file returned is not correct", 
                    createdFile2.getAbsolutePath(), rowIter.getString("pathname"));
            assertEquals("Should have been a file", "F", rowIter.getString("type"));
            
            assertFalse("There should be 3 rows only", rowIter.next());
            
        }
        catch (IOException e) {
            e.printStackTrace();
            fail("Error while creating temporary files: " + e);
        }
        finally {
            if (createdFile1 != null) {
                if (!createdFile1.delete()) {
                    _logger.warn("Could not delete file: " + createdFile1);
                }
            }
            
            if (createdFile2 != null) {
                if (!createdFile2.delete()) {
                    _logger.warn("Could not delete file: " + createdFile2);
                }
            }
            
            if (createdFile3 != null) {
                if (!createdFile3.delete()) {
                    _logger.warn("Could not delete file: " + createdFile3);
                }
            }
            
            if (createdDir1 != null) {
                if (!createdDir1.delete()) {
                    _logger.warn("Could not delete directory: " + createdDir1);
                } 
            }
            
            if (tempDir != null) {
                if (!tempDir.delete()) {
                    _logger.warn("Could not delete directory: " + tempDir);
                } 
            }
        }
    }
    
    
    /**
     * This tests the get directory name method to make sure that it is working
     * correctly
     */
    public void testGetDirectoryName() {
        // The last directory or file is ignored and it only gets the parent
        MocaResults res = null;
        String tempDir = System.getProperty("java.io.tmpdir");
        try {
            res = _moca.executeCommand(
                    "get directory name " +
                    "  where filnam = '" + tempDir + "/blah/yes/'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected Moca Exception: " + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertNotNull("The value was not in the result set", 
                rowIter.getString("dirname"));
        File targetFile = new File(tempDir + "/blah");
        File createdFile = new File(rowIter.getString("dirname"));
        // We need to make sure that we got a value back and it wasn't zero
        // length
        assertEquals("The command didn't execute correctly", 
                targetFile, createdFile);
        
        assertFalse("We should have only gotten 1 row", rowIter.next());
    }
    
    /**
     * This tests the get directory name method to make sure that it is working
     * correctly
     */
    public void testGetDirectoryNameWithAlias() {
        // The last directory or file is ignored and it only gets the parent
        MocaResults res = null;
        String tempDir = System.getProperty("java.io.tmpdir");
        try {
            res = _moca.executeCommand(
                    "get directory name " +
                    "  where filename = '" + tempDir + "/blah/yes/'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected Moca Exception: " + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertNotNull("The value was not in the result set", 
                rowIter.getString("dirname"));
        File targetFile = new File(tempDir + "/blah");
        File createdFile = new File(rowIter.getString("dirname"));
        // We need to make sure that we got a value back and it wasn't zero
        // length
        assertEquals("The command didn't execute correctly", 
                targetFile, createdFile);
        
        assertFalse("We should have only gotten 1 row", rowIter.next());
    }
    
    /**
     * This method tests to make sure that get file will work
     * @throws UnsupportedEncodingException 
     */
    public void testGetFile() throws UnsupportedEncodingException {
        FileCreationTestUtility fileCreationTestUtility = 
            new FileCreationTestUtility();
        String prefix = "unittest";
        String fileInput = "This is a temporary file for testing\nIt will be " +
                "deleted/t so don't worry!\n$!!00testXA~";
        
        File tempFile = fileCreationTestUtility.createTempFileAndInsertValues(
                prefix, fileInput, null);
        
        MocaResults res = null;
        try {
            res = _moca.executeCommand(
                    "get file " +
                    "  where filename = '" + tempFile.getAbsolutePath() + "'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception: " + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("We should have gotten 1 row back", rowIter.next());
        
        assertEquals("File names should be the same", 
                tempFile.getAbsolutePath(), rowIter.getString("filename"));
        
        Object dataObject = rowIter.getValue("data");
        
        // Check to make sure the object for data is a byte array
        assertTrue(dataObject instanceof byte[]);
        
        byte[] byteArray = (byte[])dataObject;
        
        String fileResult = new String(byteArray, "UTF-8");
        
        assertEquals("The byte array doesn't match", fileInput, fileResult);
        
        assertFalse("We should have gotten 1 row back", rowIter.next());
    }
    
    /**
     * This method is to test to make sure that remove directory actually
     * removes the directory
     */
    public void testRemoveDirectory() {
        String temporaryDirectory = System.getProperty("java.io.tmpdir");
        
        // Made up a weird name to make sure not to clash
        String directoryToCreate = temporaryDirectory + File.separator + 
                "unit%15MOCANG-13";
        
        File tempDir = new File(directoryToCreate);
        
        if(!tempDir.mkdir()) {
            _logger.debug("Directory [" + tempDir + "] could not be created, " +
                "could be because it exists already");
        }
        
        MocaResults res = null;
        try {
            res = _moca.executeCommand(
                    "remove directory " +
                    "  where directory = '" + tempDir.getAbsolutePath() + "'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception: " + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        assertFalse("There should be no rows returned", rowIter.next());
        
        assertFalse("The file should have been removed", tempDir.exists());
    }
    
    /**
     * This method is to test to make sure that remove directory actually
     * removes the directory
     */
    public void testRemoveFile() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", "");
        }
        catch (IOException e1) {
            e1.printStackTrace();
            fail("Unexpected IOException :" + e1);
        }
        
        MocaResults res = null;
        try {
            res = _moca.executeCommand(
                    "remove file " +
                    "  where filename = '" + tempFile.getAbsolutePath() + "'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception: " + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        assertFalse("There should be no rows returned", rowIter.next());
        
        assertFalse("The file should have been removed", tempFile.exists());
    }
    
    /**
     * This tests to make sure that moving a file works correctly
     */
    public void testMoveFile() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("test", "");
        }
        catch (IOException e1) {
            e1.printStackTrace();
            fail("Unexpected IOException :" + e1);
        }
        
        File tempFileMoved = new File(tempFile.getAbsolutePath() + "-moved");
        
        MocaResults res = null;
        try {
            res = _moca.executeCommand(
                    "move file " +
                    "  where from = '" + tempFile.getAbsolutePath() + "'" +
                    "    and to = '" + tempFileMoved.getAbsolutePath() + "'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception: " + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        assertFalse("There should be no rows returned", rowIter.next());
        
        assertFalse("The file should have been removed", tempFile.exists());
        
        assertTrue("The file should have been moved", tempFileMoved.exists());
        
        assertTrue("The moved file should have been a file (not directory)", 
                tempFileMoved.isFile());
        
        // Lastly make sure we can delete the file we created
        assertTrue("We couldn't clean up the moved file", tempFileMoved.delete());
    }
    
    /**
     * This tests to make sure that moving a directory works correctly
     */
    public void testMoveDirectory() {
        String temporaryDirectory = System.getProperty("java.io.tmpdir");
        
        // Made up a weird name to make sure not to clash
        String directoryToCreate = temporaryDirectory + File.separator + 
                "unit%16MOCANG-13";
        
        File tempDir = new File(directoryToCreate);
        
        if(!tempDir.mkdir()) {
            _logger.debug("Directory [" + tempDir + "] could not be created, " +
                "could be because it exists already");
        }
        
        File tempDirMoved = new File(tempDir.getAbsolutePath() + "-moved");
        
        MocaResults res = null;
        try {
            res = _moca.executeCommand(
                    "move file " +
                    "  where from = '" + tempDir.getAbsolutePath() + "'" +
                    "    and to = '" + tempDirMoved.getAbsolutePath() + "'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception: " + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        assertFalse("There should be no rows returned", rowIter.next());
        
        assertFalse("The file should have been removed", tempDir.exists());
        
        assertTrue("The file should have been moved", tempDirMoved.exists());
        
        assertTrue("The moved file should have been a directory", 
                tempDirMoved.isDirectory());
        
        // Lastly make sure we can delete the directory we created
        assertTrue("We couldn't clean up the moved file", tempDirMoved.delete());
    }
    
    /**
     * This method tests to make sure that get file will work
     */
    public void testGetTextFile() {
        FileCreationTestUtility fileCreationTestUtility = 
            new FileCreationTestUtility();
        String prefix = "unittest";
        String fileInput = "This is a temporary file for testing\nIt will be " +
                "deleted/t so don't worry!\n$!!00testXA~";
        
        File tempFile = fileCreationTestUtility.createTempFileAndInsertValues(
                prefix, fileInput, null);
        
        MocaResults res = null;
        try {
            res = _moca.executeCommand(
                    "get text file " +
                    "  where filename = '" + tempFile.getAbsolutePath() + "'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception: " + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("We should have gotten 1 row back", rowIter.next());
        
        assertEquals("File names should be the same", 
                tempFile.getAbsolutePath(), rowIter.getString("filename"));
        
        assertEquals("The byte array doesn't match", fileInput, 
                rowIter.getString("data"));
        
        assertFalse("We should have gotten 1 row back", rowIter.next());
    }
    
    /**
     * This is to test the read file moca command that is doing line reads
     */
    public void testReadFileLines() {
        FileCreationTestUtility fileCreationTestUtility = 
            new FileCreationTestUtility();
        String prefix = "unittest";
        // This string should have 5 lines
        String fileInput = "This is a temporary file for testing\n" +
        		   "It will be deleted/t so don't worry!\n" +
        		   "$!!00testXA!\n" +
        		   "Some other things\n" +
        		   "This is our last line, congrats";
        
        File tempFile = fileCreationTestUtility.createTempFileAndInsertValues(
                prefix, fileInput, null);
        
        MocaResults res = null;
        
        try {
            res = _moca.executeCommand(
                    "read file " +
                    "  where filename = '" + tempFile.getAbsolutePath() + "'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception :" + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        String[] lines = fileInput.split("\n");
        int lineCount = 0;
        
        for (String line : lines) {
            assertTrue("There was an incorrect # of rows", rowIter.next());
            
            assertEquals("The line identifier was incorrect", lineCount, 
                    rowIter.getInt("line"));
            assertEquals("The line didn't match", line, 
                    rowIter.getValue("text"));
            
            lineCount++;
        }
        
        assertFalse("There should not be anymore rows", rowIter.next());
    }
    
    /**
     * This is to test the read file moca command that is doing line reads with
     * a start and max value.  We should only read the 2 lines after the first
     * 2 lines have passed
     */
    public void testReadFileLinesWithLimits() {
        FileCreationTestUtility fileCreationTestUtility = 
            new FileCreationTestUtility();
        String prefix = "unittest";
        // This string should have 5 lines
        String fileInput = "This is a temporary file for testing\n" +
                           "It will be deleted/t so don't worry!\n" +
                           "$!!00testXA!\n" +
                           "Some other things\n" +
                           "This is our last line, congrats";
        
        File tempFile = fileCreationTestUtility.createTempFileAndInsertValues(
                prefix, fileInput, null);
        
        MocaResults res = null;
        
        try {
            res = _moca.executeCommand(
                    "read file " +
                    "  where filename = '" + tempFile.getAbsolutePath() + "'" +
                    "    and start = 2" +
                    "    and max = 2");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception :" + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        String[] lines = fileInput.split("\n");
        int lineCount = 0;
        
        for (String line : lines) {
            // We should only see the 2nd and 3rd lines since it is 0 based
            if (lineCount == 2 || lineCount == 3) {
                assertTrue("There was an incorrect # of rows", rowIter.next());

                assertEquals("The line identifier was incorrect", lineCount,
                        rowIter.getInt("line"));
                assertEquals("The line didn't match", line, 
                        rowIter.getValue("text"));
            }
            
            lineCount++;
        }
        
        assertFalse("There should not be anymore rows", rowIter.next());
    }
    
    /**
     * This is to test the read file moca command that is reading the file
     * into a string
     */
    public void testReadFileString() {
        FileCreationTestUtility fileCreationTestUtility = 
            new FileCreationTestUtility();
        String prefix = "unittest";
        // This string should have 5 lines
        String fileInput = "This is a temporary file for testing\n" +
                           "It will be deleted/t so don't worry!\n" +
                           "$!!00testXA!\n" +
                           "Some other things\n" +
                           "This is our last line, congrats";
        
        File tempFile = fileCreationTestUtility.createTempFileAndInsertValues(
                prefix, fileInput, null);
        
        MocaResults res = null;
        
        try {
            res = _moca.executeCommand(
                    "read file " +
                    "  where filename = '" + tempFile.getAbsolutePath() + "'" +
                    "    and mode = 'F'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception :" + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        
        assertTrue("There should be 1 row", rowIter.next());
        
        assertEquals("The line identifier was incorrect", fileInput.split("\n").length, 
                rowIter.getInt("line"));
        assertEquals("The line didn't match", fileInput, 
                rowIter.getString("text"));
        
        assertFalse("There should not be anymore rows", rowIter.next());
    }
    
    /**
     * This is to test the read file moca command that is reading the file
     * into a string when using a special character set
     */
    public void testReadFileWithMultiByteCharacters() {
        FileCreationTestUtility fileCreationTestUtility = 
            new FileCreationTestUtility();
        String prefix = "unittest";
        // This string should have 5 lines
        String fileInput = "This is a temporary file for testing\n" +
                           "It will be deleted/t so don't worry!\n" +
                           "$!!00testXA!\n" +
                           "Some other things\n" +
                           "This is our last line, congrats\n" +
                           "test 합 완F";
        
        File tempFile = fileCreationTestUtility.createTempFileAndInsertValues(
                prefix, fileInput, "EUC-KR");
        
        MocaResults res = null;
        
        try {
            res = _moca.executeCommand(
                    "read file " +
                    "  where filename = '" + tempFile.getAbsolutePath() + "'" +
                    "    and mode = 'F'" +
                    "    and encoding = 'EUC-KR'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception :" + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        
        assertTrue("There should be 1 row", rowIter.next());
        
        assertEquals("The line identifier was incorrect", fileInput.split("\n").length, 
                rowIter.getInt("line"));
        assertEquals("The line didn't match", fileInput, 
                rowIter.getString("text"));
        
        assertFalse("There should not be anymore rows", rowIter.next());
    }
    
    /**
     * This is to test the read file moca command that is doing line reads with
     * a start and max value.  We should only read the 2 lines after the first
     * 2 lines have passed
     */
    public void testReadFileStringWithLimits() {
        FileCreationTestUtility fileCreationTestUtility = 
            new FileCreationTestUtility();
        String prefix = "unittest";
        // This string should have 5 lines
        String fileInput = "This is a temporary file for testing\n" +
                           "It will be deleted/t so don't worry!\n" +
                           "$!!00testXA!\n" +
                           "Some other things\n" +
                           "This is our last line, congrats";
        
        File tempFile = fileCreationTestUtility.createTempFileAndInsertValues(
                prefix, fileInput, null);
        
        MocaResults res = null;
        int maxCount = 2;
        int start = 2;
        try {
            res = _moca.executeCommand(
                    "read file " +
                    "  where filename = '" + tempFile.getAbsolutePath() + "'" +
                    "    and start = " + start +
                    "    and max = " + maxCount +
                    "    and mode = 'F'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception :" + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        String[] lines = fileInput.split("\n");
        String compareString = "";
        int lineCount = 0;
        
        // Now build a string we can compare with
        for (String line : lines) {
            // We only want to see the lines that are available
            if (lineCount >= start && lineCount < start + maxCount) {
                // We have to make sure to put a new line
                if (compareString.length() > 0) {
                    compareString = compareString.concat("\n");
                }
                compareString = compareString.concat(line);
            }
            
            lineCount++;
        }
        
        assertTrue("There was an incorrect # of rows", rowIter.next());

        assertEquals("The line identifier was incorrect", maxCount,
                rowIter.getInt("line"));
        assertEquals("The line didn't match", compareString, 
                rowIter.getValue("text"));
        
        assertFalse("There should not be anymore rows", rowIter.next());
    }
    
    /**
     * This is to test the read file moca command that is reading the file
     * into a byte array
     * @throws UnsupportedEncodingException 
     */
    public void testReadFileByteArray() throws UnsupportedEncodingException {
        FileCreationTestUtility fileCreationTestUtility = 
            new FileCreationTestUtility();
        String prefix = "unittest";
        // This string should have 5 lines
        String fileInput = "This is a temporary file for testing\n" +
                           "It will be deleted/t so don't worry!\n" +
                           "$!!00testXA!\n" +
                           "Some other things\n" +
                           "This is our last line, congrats";
        
        File tempFile = fileCreationTestUtility.createTempFileAndInsertValues(
                prefix, fileInput, null);
        
        MocaResults res = null;
        
        try {
            res = _moca.executeCommand(
                    "read file " +
                    "  where filename = '" + tempFile.getAbsolutePath() + "'" +
                    "    and mode = 'B'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception :" + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        
        assertTrue("There should be 1 row", rowIter.next());
        
        assertEquals("The line identifier was incorrect", fileInput.getBytes(Charset.forName("UTF-8")).length, 
                rowIter.getInt("size"));
        
        Object dataObject = rowIter.getValue("data");
        
        // Check to make sure the object for data is a byte array
        assertTrue(dataObject instanceof byte[]);
        
        byte[] byteArray = (byte[])dataObject;
        
        assertEquals("The bytes didn't match", fileInput, new String(byteArray, "UTF-8"));
        
        assertFalse("There should not be anymore rows", rowIter.next());
    }
    
    /**
     * This is to test the read file moca command that is reading the file
     * into a byte array
     */
    public void testReadFileByteArrayWithLimits() {
        FileCreationTestUtility fileCreationTestUtility = 
            new FileCreationTestUtility();
        String prefix = "unittest";
        // This string should have 5 lines
        int byteStart = 25;
        int byteRequest = 56;
        String fileInput = "This is a temporary file for testing\n" +
                           "It will be deleted/t so don't worry!\n" +
                           "$!!00testXA!\n" +
                           "Some other things\n" +
                           "This is our last line, congrats";
        
        // First make sure our setup is okay in that String input must be
        // longer than the start of the bytes
        assertFalse(byteStart > fileInput.getBytes(Charset.forName("UTF-8")).length);
        
        int availableBytes = fileInput.getBytes(Charset.forName("UTF-8")).length - byteStart;
        int byteSize = byteRequest > availableBytes ? availableBytes : byteRequest;
        
        File tempFile = fileCreationTestUtility.createTempFileAndInsertValues(
                prefix, fileInput, null);
        
        MocaResults res = null;
        
        try {
            res = _moca.executeCommand(
                    "read file " +
                    "  where filename = '" + tempFile.getAbsolutePath() + "'" +
                    "    and mode = 'B'" +
                    "    and offset = " + byteStart +
                    "    and readsize = " + byteRequest);
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception :" + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        
        assertTrue("There should be 1 row", rowIter.next());
        
        assertEquals("The size identifier was incorrect", byteSize, 
                rowIter.getInt("size"));
        
        Object dataObject = rowIter.getValue("data");
        
        // Check to make sure the object for data is a byte array
        assertTrue(dataObject instanceof byte[]);
        
        ByteBuffer retrievedByteArray = ByteBuffer.wrap((byte[])dataObject);
        
        // Wrap the string but only the bytes starting at where we we wanted
        // to start and go in as much as the size
        ByteBuffer ourByteArray = ByteBuffer.wrap(fileInput.getBytes(Charset.forName("UTF-8")), byteStart, byteSize);
        
        // Now make sure the bytes match but take a substring which should
        // the point into the bytes + how many we read in
        assertEquals("The bytes didn't match", ourByteArray, retrievedByteArray);
        
        assertFalse("There should not be anymore rows", rowIter.next());
    }
    
    /**
     * This is to test the read file moca command that is reading the file
     * into a byte array and giving limits but are too large
     */
    public void testReadFileByteArrayWithLimitsOutOfRange() {
        FileCreationTestUtility fileCreationTestUtility = 
            new FileCreationTestUtility();
        String prefix = "unittest";
        // This string should have 5 lines
        int byteStart = 50;
        // NOTE: this is really high more bytes than the string has
        int byteRequest = 5006;
        String fileInput = "This is a temporary file for testing\n" +
                           "It will be deleted/t so don't worry!\n" +
                           "$!!00testXA!\n" +
                           "Some other things\n" +
                           "This is our last line, congrats";
        
        // First make sure our setup is okay in that String input must be
        // longer than the start of the bytes
        assertFalse(byteStart > fileInput.getBytes(Charset.forName("UTF-8")).length);
        
        int availableBytes = fileInput.getBytes(Charset.forName("UTF-8")).length - byteStart;
        int byteSize = byteRequest > availableBytes ? availableBytes : byteRequest;
        
        File tempFile = fileCreationTestUtility.createTempFileAndInsertValues(
                prefix, fileInput, null);
        
        MocaResults res = null;
        
        try {
            res = _moca.executeCommand(
                    "read file " +
                    "  where filename = '" + tempFile.getAbsolutePath() + "'" +
                    "    and mode = 'B'" +
                    "    and offset = " + byteStart +
                    "    and readsize = " + byteRequest);
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception :" + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        
        assertTrue("There should be 1 row", rowIter.next());
        
        assertEquals("The size identifier was incorrect", byteSize, 
                rowIter.getInt("size"));
        
        Object dataObject = rowIter.getValue("data");
        
        // Check to make sure the object for data is a byte array
        assertTrue(dataObject instanceof byte[]);
        
        ByteBuffer retrievedByteArray = ByteBuffer.wrap((byte[])dataObject);
        
        // Wrap the string but only the bytes starting at where we we wanted
        // to start and go in as much as the size
        ByteBuffer ourByteArray = ByteBuffer.wrap(fileInput.getBytes(Charset.forName("UTF-8")), byteStart, byteSize);
        
        // Now make sure the bytes match but take a substring which should
        // the point into the bytes + how many we read in
        assertEquals("The bytes didn't match", ourByteArray, retrievedByteArray);
        
        assertFalse("There should not be anymore rows", rowIter.next());
    }
    
    /**
     * This will test to make sure getting file size works correctly
     */
    public void testGetFileSize() {
        FileCreationTestUtility fileCreationTestUtility = 
            new FileCreationTestUtility();
        String prefix = "unittest";
        String fileInput = "This is a temporary file for testing\n" +
                           "It will be deleted/t so don't worry!\n" +
                           "$!!00testXA!\n" +
                           "Some other things\n" +
                           "This is our last line, congrats";
        
        // First create a tempory file from the string
        File tempFile = fileCreationTestUtility.createTempFileAndInsertValues(
                prefix, fileInput, null);
        
        MocaResults res = null;
        
        try {
            res = _moca.executeCommand(
                    "get file size" +
                    "  where filename = '" + tempFile.getAbsolutePath() + "'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception :" + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("There should be 1 row", rowIter.next());
        
        assertEquals("The file name didn't match", tempFile.getAbsolutePath(), 
                rowIter.getString("filename"));
        
        assertEquals("The file size doesn't match expected", fileInput.length(),
                rowIter.getInt("size"));
        
        assertEquals("The line count doesn't match",
                fileInput.split("\n").length, rowIter.getInt("num_lines"));
    }
    
    /**
     * This will test to make sure getting file size works correctly with a
     * specified character set
     * @throws UnsupportedEncodingException 
     */
    public void testGetFileSizeWithCharacterSetFailed() throws UnsupportedEncodingException {
        FileCreationTestUtility fileCreationTestUtility = 
            new FileCreationTestUtility();
        String prefix = "unittest";
        String fileInput = "This is a temporary file for testing\n" +
                           "It will be deleted/t so don't worry!\n" +
                           "$!!00testXA!\n" +
                           "Some other things\n" +
                           "This is our last line, congrats";
        
        // First create a tempory file from the string
        File tempFile = fileCreationTestUtility.createTempFileAndInsertValues(
                prefix, fileInput, "UTF-16");
        
        MocaResults res = null;
        
        try {
            res = _moca.executeCommand(
                    "get file size" +
                    "  where filename = '" + tempFile.getAbsolutePath() + "'" +
                    "    and mode = 'B'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception :" + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("There should be 1 row", rowIter.next());
        
        assertEquals("The file name didn't match", tempFile.getAbsolutePath(), 
                rowIter.getString("filename"));
        
        assertEquals("The file size should be the same as it in UTF-16", 
            fileInput.getBytes("UTF-16").length, rowIter.getInt("size"));
    }
    
    /**
     * This will test to make sure getting file size works correctly with a
     * specified character set
     * @throws UnsupportedEncodingException 
     */
    public void testGetFileSizeWithMultiByteCharacterSet() throws UnsupportedEncodingException {
        FileCreationTestUtility fileCreationTestUtility = 
            new FileCreationTestUtility();
        String prefix = "unittest";
        String fileInput = "This is a temporary file for testing\n" +
                           "It will be deleted/t so don't worry!\n" +
                           "$!!00testXA!\n" +
                           "Some other things\n" +
                           "This is our last line, congrats\n" +
                           "FUNKY 합 완";
        
        // First create a tempory file from the string
        File tempFile = fileCreationTestUtility.createTempFileAndInsertValues(
                prefix, fileInput, "EUC-KR");
        
        MocaResults res = null;
        
        try {
            res = _moca.executeCommand(
                    "get file size" +
                    "  where filename = '" + tempFile.getAbsolutePath() + "'" +
                    "    and charset = 'EUC-KR'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception :" + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("There should be 1 row", rowIter.next());
        
        assertEquals("The file name didn't match", tempFile.getAbsolutePath(), 
                rowIter.getString("filename"));
        
        assertEquals("The file size should be the same", 
            fileInput.getBytes("EUC-KR").length, rowIter.getInt("size"));
    }
    
    public void testWriteOutputFileExpansion() throws MocaException {
        String stringToWrite = "This is a test, only a test";
        MocaResults res = _moca.executeCommand(
                "write output file", 
                new MocaArgument("path", "$MOCADIR"),
                new MocaArgument("filename", "testfile.deleteme"),
                new MocaArgument("data", stringToWrite),
                new MocaArgument("newline", "Y"));
        
        assertTrue(res.next());
        String fileName = res.getString("filnam");
        assertNotNull(fileName);
        assertFalse(res.next());
        
        File createdFile = new File(fileName);
        
        FileCreationTestUtility util = new FileCreationTestUtility();
        util.compareFileContentsWithString(createdFile, stringToWrite + "\n", 
            null);
    }
    
    public void testWriteOutputFileExpansionAndNewLineCharacter() throws MocaException {
        String stringToWrite = "This is a test, only a test";
        MocaResults res = _moca.executeCommand(
                "write output file", 
                new MocaArgument("path", System.getProperty("java.io.tmpdir")),
                new MocaArgument("filename", "testfile.deleteme"),
                new MocaArgument("data", stringToWrite),
                new MocaArgument("newline", "Y"),
                new MocaArgument("newline_character", "\\r\\n"));
        
        assertTrue(res.next());
        String fileName = res.getString("filnam");
        assertNotNull(fileName);
        assertFalse(res.next());
        
        File createdFile = new File(fileName);
        
        FileCreationTestUtility util = new FileCreationTestUtility();
        util.compareFileContentsWithString(createdFile, stringToWrite + "\r\n", 
            null);
    }
    
    public void testWriteOutputFileExpansionAndNewLineCharacter2() throws MocaException {
        String stringToWrite = "This is a test, only a test";
        MocaResults res = _moca.executeCommand(
                "write output file", 
                new MocaArgument("path", System.getProperty("java.io.tmpdir")),
                new MocaArgument("filename", "testfile.deleteme"),
                new MocaArgument("data", stringToWrite),
                new MocaArgument("newline", "Y"),
                new MocaArgument("newline_character", "\\n"));
        
        assertTrue(res.next());
        String fileName = res.getString("filnam");
        assertNotNull(fileName);
        assertFalse(res.next());
        
        File createdFile = new File(fileName);
        
        FileCreationTestUtility util = new FileCreationTestUtility();
        util.compareFileContentsWithString(createdFile, stringToWrite + "\n", 
            null);
    }
    
    private static Logger _logger = LogManager.getLogger(
            FileCreationTestUtility.class);
}
