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

package com.redprairie.moca.components.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.exceptions.InvalidArgumentException;
import com.redprairie.moca.exceptions.MissingArgumentException;
import com.redprairie.moca.util.MocaIOException;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.util.WildcardFilenameFilter;

/**
 * This class handles file and directory manipulations in java.  Operations
 * include things such as
 * TODO currently the methods under here should be limited to user privileges but access is open
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class FileDirectoryService {

    /**
     * This will copy a file from one location to another.
     * This will accept environment variables in the names.
     * @param moca The moca context
     * @param source The source file to copy from, may contain environment 
     *        variables that will be expanded
     * @param destination The destination file to copy to, may contain 
     *        environment variables that will be expanded
     * @throws FileDirectoryServiceException this exception is thrown if the files
     *         don't exist, has problems transferring the files or problems
     *         closing the actual files when finishing
     */
    public void copyFile(MocaContext moca, String source, String destination)
            throws MocaException {
        String sourceReplaced = 
            MocaUtils.expandEnvironmentVariables(moca, source);
        String destinationReplaced = 
            MocaUtils.expandEnvironmentVariables(moca, destination);
        
        // Replace all the slashes with the appropriate system slash
        sourceReplaced = fixSeparators(sourceReplaced);
        
        // Replace all the slashes with the appropriate system slash
        destinationReplaced = fixSeparators(destinationReplaced);

        MocaUtils.copyFile(sourceReplaced, destinationReplaced);
    }
    
    /**
     * This will create the directory specified as well as any parent directories
     * required.  This will accept environment variables in the names.
     * @param moca The moca context
     * @param directory The directory to be created
     */
    public MocaResults createDirectory(MocaContext moca, String directory) 
            throws MocaIOException {     
        String directoryReplaced = 
            MocaUtils.expandEnvironmentVariables(moca, directory);
        
        // Replace all the slashes with the appropriate system slash
        directoryReplaced = fixSeparators(directoryReplaced);
        
        File fileDirectory = new File(directoryReplaced);

        // Try to make the directories, if we couldn't throw an IO Exception
        if (!fileDirectory.exists()) {
            if (!fileDirectory.mkdirs()) {
                throw new MocaIOException("Unable to create directory: " + 
                        directoryReplaced);
            }
        }

        EditableResults res = moca.newResults();   
        res.addColumn("directory", MocaType.STRING);
        res.addRow();
        res.setStringValue("directory", directoryReplaced);
        
        return res;
    }
    
    /**
     * This method will find all the files that exist for the given path name
     * The path can have wildcards such as *.
     * @param moca The moca context
     * @param pathName The path name to the files
     * @param sort Whether or not the names should  be sorted
     * @return The files found sorted by date unless specified in which case by
     *         name
     */
    public MocaResults findFile(MocaContext moca, String pathName, String sort) {
        
        EditableResults retRes = moca.newResults();
        
        retRes.addColumn("filename", MocaType.STRING);
        retRes.addColumn("pathname", MocaType.STRING);
        retRes.addColumn("type", MocaType.STRING);
        
        String expandedPath = MocaUtils.expandEnvironmentVariables(moca, 
                pathName);
        
        // Replace all the slashes with the appropriate system slash
        expandedPath = fixSeparators(expandedPath);
        
        File newFile = new File(expandedPath);
        
        // If it exists just return right away
        if (newFile.exists()) {
            retRes.addRow();
            retRes.setStringValue("filename", newFile.getName());
            retRes.setStringValue("pathname", newFile.getAbsolutePath());
            String type = "UNKNOWN";
            if (newFile.isDirectory()) {
                type = "D";
            }
            else if (newFile.isFile()) {
                type = "F";
            }
            retRes.setStringValue("type", type);
        }
        // This means it wasn't located so lets do a regexp to find it
        else {
            // Take the parent directory and then search in it
            File parentFile = newFile.getParentFile();
            File[] files = parentFile.listFiles(
                    new WildcardFilenameFilter(newFile.getName()));
            
            if (files != null) {
                List<File> sortedFiles = new ArrayList<File>(Arrays.asList(files)); 
                if (sort != null && sort.equalsIgnoreCase("Y")) {
                    // Now sort the list by Names
                    Collections.sort(sortedFiles, new Comparator<File>() {

                        @Override
                        public int compare(File o1, File o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                        
                    });
                }
                else {
                    // Now sort the list by Date
                    Collections.sort(sortedFiles, new Comparator<File>() {

                        @Override
                        public int compare(File o1, File o2) {
                            long thisVal = o1 .lastModified();
                            long anotherVal = o2.lastModified();
                            return (thisVal<anotherVal ? -1 : (thisVal==anotherVal ? 0 : 1));
                        }
                        
                    });
                }
                
                for (File sortedFile : sortedFiles) {
                    retRes.addRow();
                    retRes.setStringValue("filename", sortedFile.getName());
                    retRes.setStringValue("pathname", sortedFile.getAbsolutePath());
                    String type = "UNKNOWN";
                    if (sortedFile.isDirectory()) {
                        type = "D";
                    }
                    else if (sortedFile.isFile()) {
                        type = "F";
                    }
                    retRes.setStringValue("type", type);
                }
            }
        }
        
        return retRes;
    }
    
    /**
     * Performs the "get file" MOCA command.  This will read the existance of
     * a file and if valid will return the full pathname in addition to a byte
     * array containing all the data from the file
     * @param moca The moca context
     * @param filename The file name to find the file from (may contain 
     *        environment variables)
     * @return The result containing the "pathname" and "data"
     * @throws MocaIOException This occurs if the file is larger than 
     *         {@link Integer#MAX_VALUE} or if there was an issue memory
     *         mapping the file into memory
     * @throws GenericException This occurs if the file doesn't exist
     */
    public MocaResults getFile(MocaContext moca, String filename) 
            throws MocaIOException, GenericException {
        
        // Expand the environment variables
        String expandedPath = MocaUtils.expandEnvironmentVariables(moca, 
                filename);
        
        // Replace all the slashes with the appropriate system slash
        expandedPath = fixSeparators(expandedPath);
        
        File newFile = new File(expandedPath);
        
        byte[] byteArray = readFileAsByteArray(newFile, null, null);
        
        EditableResults retRes = moca.newResults();
        
        retRes.addColumn("filename", MocaType.STRING);
        retRes.addColumn("data", MocaType.BINARY);
        
        retRes.addRow();
        
        retRes.setStringValue("filename", newFile.getAbsolutePath());
        retRes.setBinaryValue("data", byteArray);
        
        return retRes;
    }
    
    /**
     * Performs the 'remove directory' moca command.  This will remove the
     * file if it is a directory.  It will also expand environment variables
     * in the path name
     * @param moca The moca context
     * @param pathName The path of the directory to remove 
     * @throws GenericException This is thrown if the file doesn't exist, 
     *         if the pathName doesn't point to a directory, or if there
     *         was some reason the directory could not be deleted
     */
    public void removeDirectory(MocaContext moca, String pathName) 
            throws GenericException {
        
        String expandedPathName = MocaUtils.expandEnvironmentVariables(moca, 
                pathName);
        
        expandedPathName = fixSeparators(expandedPathName);
        
        File dir = new File(expandedPathName);
        
        if (!dir.exists()) {
            throw new GenericException("Directory " + dir.getAbsolutePath() + 
                    " does not exist");
        }
        
        if (!dir.isDirectory()) {
            throw new GenericException("Location " + dir.getAbsolutePath() + 
                    " is not a directory"); 
        }
        
        if (!dir.delete()) {
            throw new GenericException("Directory " + dir.getAbsolutePath() + 
                    " could not be deleted.  Ensure it is empty."); 
        }
    }
    
    /**
     * Performs the 'remove file' moca command.  This will remove the
     * file if it is a file (not directory).  It will also expand environment 
     * variables in the path name
     * @param moca The moca context
     * @param pathName The path name to the file
     * @throws GenericException This is thrown if the file doesn't exist, 
     *         if the pathName doesn't point to a file, or if there
     *         was some reason the file could not be deleted
     */
    public void removeFile(MocaContext moca, String pathName) 
            throws GenericException {
        
        String expandedPathName = MocaUtils.expandEnvironmentVariables(moca, 
                pathName);
        
        expandedPathName = fixSeparators(expandedPathName);
        
        File file = new File(expandedPathName);
        
        // First check if the file exists
        if (!file.exists()) {
            throw new GenericException("File " + file.getAbsolutePath() + 
                    " does not exist");
        }
        
        // Make sure it is a file and not a directory
        if (!file.isFile()) {
            throw new GenericException("Location " + file.getAbsolutePath() + 
                    " is not a directory"); 
        }
        
        // Finally try deleting the file
        if (!file.delete()) {
            throw new GenericException("File " + file.getAbsolutePath() + 
                    " could not be deleted."); 
        }
    }
    
    /**
     * Performs the 'get file size' command.  Will also expand environment
     * variables present in the file name
     * @param moca The moca context
     * @param fileName The file to get the size of
     * @param mode This is to tell whether the file is in binary mode or not
     *        by specifying a b or B as the mode
     * @param encoding This defines what character set the file is known to be
     *        in.  This is only used if the mode is not binary.
     * @return A result set containing the size of the file
     * @throws GenericException This error occurs if the file doesn't exist,
     *         the file is not an actual file, or if the file is too large
     * @throws MocaIOException This error is thrown if there is a problem when
     *         acquiring the line count
     */
    public MocaResults getFileSize(MocaContext moca, String fileName, 
            String mode, String encoding) throws GenericException, MocaIOException {
        
        String expandedPathName = MocaUtils.expandEnvironmentVariables(moca, 
                fileName);
        
        expandedPathName = fixSeparators(expandedPathName);
        
        File file = new File(expandedPathName);
        
        // First check if the file exists
        if (!file.exists()) {
            throw new GenericException("File " + file.getAbsolutePath() + 
                    " does not exist");
        }
        
        // Make sure it is a file and not a directory
        if (!file.isFile()) {
            throw new GenericException("Location " + file.getAbsolutePath() + 
                    " is not a directory"); 
        }
        
        long fileSize = file.length();
        
        // We only support Integer sized file length checks, so throw
        // an error stating so
        if (fileSize > Integer.MAX_VALUE) {
            throw new GenericException("Size of file " + expandedPathName + 
                    " is greater than 2^31 -1 bytes");
        }
        
        int lineCount = 0;
        // If we are not running in Binary mode then get the line count
        if (mode == null || !mode.equalsIgnoreCase("b"))  {
            LineNumberReader reader = null;
            try {
                FileInputStream fis = new FileInputStream(file);
                if (encoding != null && !encoding.isEmpty()) { 
                    reader = new LineNumberReader(new InputStreamReader(fis, 
                        encoding));
                }
                else {
                    reader = new LineNumberReader(new InputStreamReader(fis, "UTF-8"));
                }
                
                while (reader.read() != -1) {
                    // This is just a loop to go through the file to get the
                    // line reader to count for us
                }
                
                // We have to increment by 1 since the last line isn't counted
                lineCount = reader.getLineNumber() + 1;
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new GenericException("File [" + file.getAbsolutePath() + 
                        "] does not exist");
            }
            catch (InterruptedIOException e) {
                throw new MocaInterruptedException(e);
            }
            catch (IOException e) {
                e.printStackTrace();
                throw new MocaIOException("Failed reading from file [" + 
                        file.getAbsolutePath() + "]", e);
            }
            finally {
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        _logger.warn("Problem occurred while closing reader", e);
                    }
                }
            }
        }
        
        EditableResults retRes = moca.newResults();
        
        retRes.addColumn("filename", MocaType.STRING);
        retRes.addColumn("size", MocaType.INTEGER);
        retRes.addColumn("num_lines", MocaType.INTEGER);
        
        retRes.addRow();
        
        retRes.setStringValue("filename", file.getAbsolutePath());
        retRes.setIntValue("size", (int)file.length());
        retRes.setIntValue("num_lines", lineCount);
        
        return retRes;
    }
    
    /**
     * Performs the 'move file' moca command.  It will move the file from the
     * fromPath to the toPath.  It will expand any environment variables.
     * @param moca The moca context
     * @param fromPath The location of the file before moving
     * @param toPath The location of the file when moving
     * @throws GenericException This is thrown if the fromPath file does not 
     *         exist, the toPath file exists, the toPath points to a file
     *         without a containing directory or the rename failed for
     *         some reason
     */
    public void moveFile(MocaContext moca, String fromPath, String toPath) 
            throws GenericException {
        String fromPathReplaced = 
            MocaUtils.expandEnvironmentVariables(moca, fromPath);
        String toPathReplaced = 
            MocaUtils.expandEnvironmentVariables(moca, toPath);
        
        // Replace all the slashes with the appropriate system slash
        fromPathReplaced = fixSeparators(fromPathReplaced);
        
        // Replace all the slashes with the appropriate system slash
        toPathReplaced = fixSeparators(toPathReplaced);
        
        File fromFile = new File(fromPathReplaced);
        
        // We want to do a check to make sure the file we are moving actually
        // exists
        if (!fromFile.exists()) {
            throw new GenericException("The file to move from " + 
                    fromFile.getAbsolutePath() + " doesn't exist");
        }
        
        File toFile = new File(toPathReplaced);
        
        // We want to do a check to make sure where we are moving to doesn't
        // exist first
        if (toFile.exists()) {
            throw new GenericException("Cannot move file over an existing file " + 
                    toFile.getAbsolutePath());
        }
        
        File toFileParent = toFile.getParentFile();
        
        // Lastly make sure the parent of where we are moving to actually exists
        if (!toFileParent.exists()) {
            throw new GenericException("Cannot move file as parent directory" +
            		" does not exist " + toFileParent.getAbsolutePath());
        }
        
        // Now we move the file on top of the other
        if (!fromFile.renameTo(toFile)) {
            throw new GenericException("Cannot move file for some reason " + 
                    toFile.getAbsolutePath());
        }
    }
    
    /**
     * This will read in a given text file and return the name of the text
     * file and it's contents
     * @param moca The moca context
     * @param fileName The name of the file to get
     * @return A result set containing the file name and the data of the text
     *         file
     * @throws MocaIOException This occurs if the file is larger than 
     *         {@link Integer#MAX_VALUE} or if there was an issue memory
     *         mapping the file into memory
     * @throws GenericException This occurs if the file doesn't exist
     */
    public MocaResults getTextFile(MocaContext moca, String fileName) 
            throws GenericException, MocaIOException {
        
        // Expand the environment variables
        String expandedPath = MocaUtils.expandEnvironmentVariables(moca, 
                fileName);
        
        // Replace all the slashes with the appropriate system slash
        expandedPath = fixSeparators(expandedPath);
        
        File newFile = new File(expandedPath);
        
        byte[] byteArray = readFileAsByteArray(newFile, null, null);
        String fileContents;
        try {
            fileContents = new String(byteArray, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new GenericException(e.getMessage());
        }
        
        EditableResults retRes = moca.newResults();
        
        retRes.addColumn("filename", MocaType.STRING);
        retRes.addColumn("data", MocaType.STRING);
        
        retRes.addRow();
        
        retRes.setStringValue("filename", newFile.getAbsolutePath());
        retRes.setStringValue("data", fileContents);
        
        return retRes;
    }
    
    /**
     * This will read in a file and return the file back as a byte array.  You
     * can specify where in the file to begin reading and how many bytes.
     * This will check for existence of file and throw appropriate exceptions if
     * not present.
     * @param fileToRead The file to read, all path values must be expanded and
     *        separators in correct order
     * @param filePosition The position in the file to start, if null beginning
     *        position will be used
     * @param byteTotal How many bytes to read into the array, if null will
     *        read in the remainder of the file.  If greater than bytes left
     *        will just read in everything
     * @return the byte array representing the file data
     * @throws MocaIOException This occurs if the file is larger than 
     *         {@link Integer#MAX_VALUE} or if there was an issue memory
     *         mapping the file into memory
     * @throws GenericException This occurs if the file doesn't exist
     */
    private byte[] readFileAsByteArray(File fileToRead, Integer filePosition, 
            Integer byteTotal) throws GenericException, MocaIOException {
        // If it doesn't exist or it isn't a file (ie. directory) then errors
        if (!fileToRead.exists() || !fileToRead.isFile()) {
            throw new GenericException("File [" + fileToRead.getAbsolutePath() + 
                    "] does not exist or is a directory.");
        }
        
        FileInputStream inStream = null;
        FileChannel inChannel = null;
        byte[] byteArray;
        
        try {
            inStream = new FileInputStream(fileToRead);

            inChannel = inStream.getChannel();
            
            long channelSize = inChannel.size();
            
            // Do a long comparison to make sure the channel isn't too big
            // We can only stuff Integar.MAX_VALUE into the array
            if (channelSize > (long)Integer.MAX_VALUE) {
                throw new MocaIOException("The channel length is too long " +
                                "greater than " + Integer.MAX_VALUE + " bytes " +
                                "for file " + fileToRead.getAbsolutePath());
            }
            
            // The start position is 0 if the file position isn't specified
            // else we use the passed in value
            int mappingStartPosition = filePosition == null ? 0 : filePosition;
            
            // If the start position is actually in the channel size then
            // we can go into it
            if (mappingStartPosition < channelSize) {
                int mappingAvailable = (int) channelSize - mappingStartPosition;
                // If the byte total is not provided the mapping size is the
                // remaining mapping.  If it is provided take the value that
                // is lesser of the mapping available or byte total
                int mappingTotalSize = (byteTotal == null ? mappingAvailable : 
                    (byteTotal > mappingAvailable ? mappingAvailable : byteTotal));
                
                // Now we read from a certain position from the file the
                // total size.
                inChannel.position(mappingStartPosition);
                ByteBuffer byteBuffer = ByteBuffer.allocate(mappingTotalSize);
                inChannel.read(byteBuffer);
                
                // Now we have to get the array from the buffer.  If the buffer
                // is backed by an array just get that out, else we have to
                // create a new array.
                if (byteBuffer.hasArray()) {
                    byteArray = byteBuffer.array();
                }
                else {
                    byteArray = new byte[byteBuffer.limit()];
                    
                    byteBuffer.get(byteArray);   
                }
            }
            else {
                byteArray = new byte[0];
            }
        }
        catch (FileNotFoundException e) {
            // This should never happen since we checked above
            throw new GenericException("File [" + fileToRead.getAbsolutePath() + 
                    "] does not exist");
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new MocaIOException("Error reading file contents", e);
        }
        finally {
            if (inStream != null) {
                try {
                    inStream.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    _logger.warn("Problem occurred while closing stream", e);
                }
            }
            
            if (inChannel != null) {
                try {
                    inChannel.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    _logger.warn("Problem occurred while closing channel", e);
                }
            }
        }
        
        return byteArray;
    }
    
    /**
     * This will read in a file given the parameters of the mode.
     * <br>
     * If the mode is block ('B' or 'b') then it will read in the file as a byte
     * array.  It will use the offset and readSize values to read only parts
     * of the file if desired.
     * <br>
     * If the mode is file ('F' or 'f')
     * @param fileName
     * @param maxLines
     * @param startLine
     * @param lineMode
     * @param offset
     * @param readSize
     * @param encoding The encoding to use for the file when reading
     * @return
     * @throws MocaIOException 
     * @throws GenericException 
     */
    public MocaResults readFile(MocaContext moca, String fileName,
            Integer maxLines, Integer startLine, String lineMode,
            Integer offset, Integer readSize, String encoding) 
            throws GenericException, MocaIOException {
        // Expand the environment variables
        String expandedPath = MocaUtils.expandEnvironmentVariables(moca, 
                fileName);
        
        // Replace all the slashes with the appropriate system slash
        expandedPath = fixSeparators(expandedPath);
        
        File readFile = new File(expandedPath);
        
        EditableResults retRes = moca.newResults();
        
        // This is for block mode
        if (lineMode != null && lineMode.equalsIgnoreCase("b")) {
            // Read in the file at the offset and reading in max amount of size
            byte[] byteArray = readFileAsByteArray(readFile, offset, readSize);
            
            retRes.addColumn("size", MocaType.INTEGER);
            retRes.addColumn("data", MocaType.BINARY);
            
            retRes.addRow();
            
            retRes.setIntValue("size", byteArray.length);
            retRes.setBinaryValue("data", byteArray);
        }        
        // This if we want to read only portions of the file or read line by
        // line go in here
        else {
            BufferedReader reader = null;
            try {
                if (encoding != null && !encoding.isEmpty()) {
                    reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(readFile), encoding));
                }
                else {
                    reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(readFile), "UTF-8"));
                }
                
                int lineCount = 0;
                int lineStart = startLine == null ? 0 : startLine;
                int lineMax = maxLines == null ? Integer.MAX_VALUE : maxLines;
                boolean useString = (lineMode != null && 
                        lineMode.equalsIgnoreCase("f"));
                StringBuilder stringBuilder = new StringBuilder();
                
                if (!useString) {
                    retRes.addColumn("line", MocaType.INTEGER);
                    retRes.addColumn("text", MocaType.STRING);
                }
                
                String line;
                while ((line = reader.readLine()) != null) {
                    // Only do something if the line is after the start
                    if (lineCount >= lineStart) {
                        if (useString) {
                            // If we have appended before put a line break
                            if (stringBuilder.length() > 0) {
                                stringBuilder.append('\n');
                            }
                            stringBuilder.append(line);
                        }
                        else {
                            retRes.addRow();
                            
                            retRes.setIntValue("line", lineCount);
                            retRes.setStringValue("text", line);
                        }
                    }
                    
                    // If we have read in the the lines we want and also
                    // increment the line count before check
                    if (++lineCount >= (lineMax + lineStart)) {
                        break;
                    }
                }
                
                if (useString) {
                    retRes.addColumn("line", MocaType.INTEGER);
                    retRes.addColumn("text", MocaType.STRING);
                    
                    retRes.addRow();
                    
                    retRes.setIntValue("line", lineCount - lineStart);
                    retRes.setStringValue("text", stringBuilder.toString());
                }
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new GenericException("File [" + readFile.getAbsolutePath() + 
                        "] does not exist");
            }
            catch (InterruptedIOException e) {
                throw new MocaInterruptedException(e);
            }
            catch (IOException e) {
                e.printStackTrace();
                throw new MocaIOException("Failed reading from file [" + 
                        readFile.getAbsolutePath() + "]", e);
            }
            finally {
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        _logger.warn("Problem occurred while closing reader", e);
                    }
                }
            }
        }
        
        return retRes;
    }
    
    /**
     * @param moca
     * @param file
     * @param path
     * @param mode
     * @param data
     * @param newLine
     * @param newLineChars
     * @param dataBinary
     * @return
     * @throws MissingArgumentException 
     * @throws InvalidArgumentException 
     * @throws MocaIOException 
     */
    public MocaResults writeOutputFile(MocaContext moca, String file, 
            String path, String mode, String data, String charsetStr,
            String newLine, String newLineChars, byte[] dataBinary) 
            throws MissingArgumentException, InvalidArgumentException, 
            MocaIOException {
        
        Charset charset = Charset.forName("UTF-8");
        // We have to convert the string to binary first if it was provided.
        if (data != null) {
            if (charsetStr != null) {
                charset = Charset.forName(charsetStr);
            }
            dataBinary = data.getBytes(charset);
        }
        // If we didn't get the string or binary we can't proceed.
        else if (dataBinary == null) {
            throw new MissingArgumentException("data");
        }
        
        boolean append = false;
        if (mode != null && mode.trim().length() > 0) {
            char firstChar = mode.charAt(0);
            if (firstChar == 'a' || firstChar == 'A') {
                append = true;
            }
            else if (firstChar == 'w' || firstChar == 'W') {
                append = false;
            }
            else {
                throw new InvalidArgumentException("mode");
            }
        }
        
        StringBuilder sb = new StringBuilder();
        
        if (path != null) {
            sb.append(path);
            _logger.debug(MocaUtils.concat("Path for output file is: ", path));
            sb.append(File.separatorChar);
        }
        
        if (file == null || file.trim().length() == 0) {
            throw new MissingArgumentException("file");
        }
        
        sb.append(file);
        _logger.debug(MocaUtils.concat("File name for output file is: ", file));
        String fileName = MocaUtils.expandEnvironmentVariables(moca, 
                sb.toString());
        
        fileName = fixSeparators(fileName);
        
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(fileName, append);
            // Now we actually write that data to the stream.
            stream.write(dataBinary);
            
            if (newLine != null) {
                char val = newLine.charAt(0);
                
                if (val == 'Y' || val == 'y') {
                    
                    if (newLineChars == null) {
                        newLineChars = "\n";
                    }
                    
                    // Replace all the characters that normally would be
                    // a new line with the new line character.
                    String result = newLineChars.replace("\\r", "\r")
                        .replace("\\n", "\n");
                    stream.write(result.getBytes(charset));
                }
            }
        }
        catch (IOException e) {
            throw new MocaIOException("Problem creating file.", e);
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                }
                catch (IOException e) {
                    _logger.warn("Unable to close file output stream.");
                }
            }
        }
        
        EditableResults retRes = moca.newResults();
        
        retRes.addColumn("filnam", MocaType.STRING);
        retRes.addRow();
        retRes.setStringValue(0, fileName);
        
        return retRes;        
    }
    
    /**
     * This method will replace all separators to be in the correct OS dependant
     * separators
     * @param pathToFix The string to replace
     * @return The string with the correct separators
     */
    private String fixSeparators(String pathToFix) {
        return pathToFix.replaceAll("[/\\\\]", "\\" + File.separator);
    }
    
    private final static Logger _logger = 
        LogManager.getLogger(FileDirectoryService.class);
}
