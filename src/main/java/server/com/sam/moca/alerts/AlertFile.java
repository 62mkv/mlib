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

package com.sam.moca.alerts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.Reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sam.moca.MocaInterruptedException;
import com.sam.moca.alerts.util.AlertUtils;

/**
 * Data structure for an alert.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author Brandon Grady
 * @version $Revision$
 */
public class AlertFile {
    
    /**
     * Creates an alert file when the file handle has already been 
     * obtained.
     * 
     * @param file File handle for the file to associate.
     * @throws FileReadException 
     * @throws FileCreationException 
     */
    public AlertFile(File file) 
            throws FileCreationException, FileReadException {
        
        String xmlFileName = null, triggerFileName = null;
        
        // Determine if this is an XML or TRG file.
        if (file.getName().endsWith(XML_EXT)) {
            try {
                xmlFileName = file.getCanonicalPath();
            }
            catch (IOException e) {
                _logger.debug("IO Exception caught while getting "
                        + "XML path: " + e);
                
                throw new FileReadException(e.toString());
            }
            
            _xmlFile = file;
            triggerFileName = xmlFileName.substring(0, xmlFileName.length() 
                    - XML_EXT.length()) + TRG_EXT;
            
            _triggerFile = new File(triggerFileName);
        }
        else if (file.getName().endsWith(TRG_EXT)) {
            try {
                triggerFileName = file.getCanonicalPath();
            }
            catch (IOException e) {
                _logger.debug("IO Exception caught while getting "
                        + "TRG path: " + e);
                
                throw new FileReadException(e.toString());
            }
            
            _triggerFile = file;
            xmlFileName = triggerFileName.substring(0, 
                    triggerFileName.length() - TRG_EXT.length()) 
                    + XML_EXT;
            
            _xmlFile = new File(xmlFileName);
        }
        else {
            _logger.debug("Invalid file name passed in: " 
                    + file.getName());
            throw new FileReadException("Invalid file extension.");
        }

        // If the file passed exists, we aren't trying to read one, 
        // so we're writing one
        if (file.exists()) {
            
            // This will only happen if a TRG file was passed and 
            // its corresponding XML file does not exist.
            if (!_xmlFile.exists()) {
                throw new FileReadException("Invalid XML file "
                        + "passed.");
            }
            
            // Read the contents of the XML file.
            
            BufferedReader reader = null;
            String line;
            StringBuilder str;
            
            try {
                Reader in = new InputStreamReader(new FileInputStream(_xmlFile), "UTF-8");
                reader = new BufferedReader(in);
                str = new StringBuilder();
                
                while ((line = reader.readLine()) != null) {
                    str.append(line);
                    str.append('\n');
                }
            }
            catch (FileNotFoundException e) {
                _logger.debug("Could not open file for reading.");
                throw new FileReadException("FileNotFound exception encountered");
            }
            catch (IOException e) {
                _logger.debug("IO Exception while reading file.");
                throw new FileReadException("IO Exception encountered.");
            }
            finally {
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (IOException e) {
                        _logger.debug("IO Exception while closing file.");
                        throw new FileReadException("IO Exception encountered.");
                    }
                }
            }
            
            _xmlContents = str.toString();
            
        }
        else {
            // This is a new file, so make sure we can write to the 
            // parent directory
            String parentPath = file.getParent();
            File parentDir = new File(parentPath);
            if (!parentDir.canWrite()) {
                _logger.debug("Cannot write to the output "
                        + "directory.");
                throw new FileCreationException();
            }
            _xmlFile = file;
        }
    }

    /**
     * Open an alert file when the filename is known.
     * 
     * @param fileName Absolute filename path to the file.
     * @throws FileReadException 
     * @throws FileCreationException 
     */
    public AlertFile(String fileName) 
            throws FileCreationException, FileReadException {
        
        this(new File(fileName));
    }
    
    /**
     * Open an alert file when the filename is known but not the 
     * path.
     * 
     * @param fileName Filename for the file to be opened.
     * @param appendPath Should the spooler path be appended to the 
     *        file?
     * @throws FileReadException 
     * @throws FileCreationException 
     */
    public AlertFile(String fileName, boolean appendPath) 
            throws FileCreationException, FileReadException {

        this(new File(AlertUtils.getSpoolDir(), fileName));
    }
    
    @Override
    public String toString() {
        try {
            return _xmlFile.getCanonicalPath();
        }
        catch (IOException e) {
            return "Error getting path to file.";
        }
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        
        if (other == null) {
            return false;
        }
        
        if (!(other instanceof AlertFile)) {
            return false;
        }
        
        final AlertFile af = (AlertFile) other;
        
        return (_xmlFile.equals(af._xmlFile) &&
                ((_triggerFile == null && af._triggerFile == null) || 
                 (_triggerFile != null && _triggerFile.equals(af._triggerFile))));
    }
    
    @Override
    public int hashCode() {
        return _xmlFile.hashCode() + 
            (_triggerFile == null ? 0 : _triggerFile.hashCode());
    }
    
    /**
     * Completes the file using the default functionality of moving the 
     * processed file to the processed directory rather than allowing it 
     * to be removed immediately.
     * 
     * @param success
     * @return Success or failure of the operation.
     */
    public boolean complete(boolean success) {
        return complete(success, false);
    }
    
    /**
     * Call when processing on the file has completed.
     * 
     * @param success Was processing successful?
     * @param removeOnSuccess Should the file be removed on a successful transfer?  
     *        If not, it goes to the processed directory.
     * @return Whether the completion action succeeded.
     */
    public boolean complete(boolean success, boolean removeOnSuccess) {
        boolean status = true;

        // If successful, remove the file, otherwise move it to the 
        // BAD directory for reprocessing.
        if (success) {
            if (removeOnSuccess) {
                // Remove the file on successful delivery
                _logger.debug("Alert file is being removed immediately.");
                if (!_xmlFile.delete()) {
                    _logger.debug("Error deleting XML file.");
                    success = false;
                }
            }
            else {
                _logger.debug("Alert file is being moved to the processed directory.");
                File destFile = new File(AlertUtils.getProcessedDir(), 
                        _xmlFile.getName());
                
                if (!_xmlFile.renameTo(destFile)) {
                    _logger.debug("Error moving XML file to processed directory");
                    status = false;
                }
            }
        }
        else {
            File destFile = new File(AlertUtils.getBadDir(), _xmlFile.getName());
            if (!_xmlFile.renameTo(destFile)) {
                _logger.debug("Error moving XML file to error directory.");
                status = false;
            }
        }
        
        // Remove the trigger file
        if (!_triggerFile.delete()) {
            _logger.debug("Error deleting TRG file.");
            status = false;
        }
        
        return status;
    }
    
    /**
     * @return the triggerFile
     */
    public File getTriggerFile() {
        return _triggerFile;
    }
    
    /**
     * @return the xmlFile
     */
    public File getXmlFile() {
        return _xmlFile;
    }
    
    /**
     * @return the xmlContents
     */
    public String getXmlContents() {
        return _xmlContents;
    }
    
    /**
     * Sets the XML contents
     */
    public void setXmlContents(String xml) {
        _xmlContents = xml;
    }
    
    /**
     * Writes the XML file
     * @param writeTrigger Should the trigger file be written as 
     *        well?
     * @throws FileCreationException 
     */
    public void writeXML(boolean writeTrigger) 
            throws FileCreationException {
        
        // Write the XML file
        writeXML();
        
        // If they want the trigger file written, write that too
        if (writeTrigger) {
            writeTRG();
        }
    }
    
    /**
     * Writes the XML file alone, no trigger file
     */
    public void writeXML() throws FileCreationException {
        // First, create the file if it doesn't exist
        if (!_xmlFile.exists()) {
            try {
                if (!_xmlFile.createNewFile()) {
                    _logger.debug("Could not create XML file");
                    throw new FileCreationException(); 
                }
            }
            catch (IOException io) {
                _logger.debug("Exception caught while creating "
                        + "file: " + io.getMessage());
                throw new FileCreationException();
            }
        }
        // Create an output writer.
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(_xmlFile), "UTF-8"));
            
            out.write(_xmlContents);
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            _logger.debug("Error encountered while writing the XML "
                    + "file: " + e);
            throw new FileCreationException();
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                }
            }
            catch (IOException e) {
                _logger.debug("Error encountered while closing the XML "
                        + "file: " + e);
            }
        }
    }
    
    /**
     * Writes the trigger file
     * @throws FileCreationException 
     */
    public void writeTRG() throws FileCreationException {
        if (!_triggerFile.exists()) {
            try {
                if (!_triggerFile.createNewFile()) {
                    _logger.debug("Exception encountered while writing " 
                            + "the TRG file");
                    throw new FileCreationException();
                }
            }
            catch (IOException e) {
                _logger.debug("Exception encountered while writing " 
                        + "the TRG file: " + e);
                throw new FileCreationException();
            }
        }
    }

    // Constants
    public static final String XML_EXT = ".xml";
    public static final String TRG_EXT = ".trg";
    public static final String PROCESSED_DIR_DEFAULT = 
            "$LESDIR/files/emsout/prc";
    public static final String BAD_DIR_DEFAULT = 
            "$LESDIR/files/emsout/bad";
    public static final String SPOOL_DIR_DEFAULT = 
            "$LESDIR/files/emsout";
    
    // Implementation
    private static final Logger _logger = LogManager.getLogger(AlertFile.class);
    
    private File _triggerFile;
    private File _xmlFile;
    private String _xmlContents;
}
