/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.sam.moca.server.repository.file.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sam.moca.MocaInterruptedException;
import com.sam.moca.server.repository.ComponentLevel;
import com.sam.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents;
import com.sam.moca.server.repository.file.LevelReader;
import com.sam.moca.server.repository.file.RepositoryReadException;
import com.sam.util.ArgCheck;

/**
 * XMLLevelReader
 *
 * Class to hold static method to read and write XML Level definition files.
 *
 * <b>
 *
 * <pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre>
 *
 * </b>
 *
 * @author cjolly
 * @version $Revision$
 */

public class XMLLevelReader implements LevelReader{
    
    public static final String LEVEL_TAG = "component-level";
        
    public XMLLevelReader(RepositoryReaderEvents events) {
        _events = events;
    }

    /**
     * Reads the level file and returns a Level object.
     *
     * @param lvlfile
     * @return Level
     * @throws RepositoryReadException
     */
    public ComponentLevel read(File lvlfile) throws RepositoryReadException {
        ArgCheck.notNull(lvlfile);

        try {
            Element node = XMLUtils.readNodeFromFile(lvlfile);
            return read(node);
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            throw new RepositoryReadException(
                "Unable to read Level file: " + lvlfile.getPath() + ": " + e, e);
        }
        catch (SAXException e) {
            throw new RepositoryReadException(
                "XML exception parsing file: " + lvlfile.getPath() + ": " + e, e);
        }
    }
    
    public ComponentLevel read(InputStream xmlInput) throws RepositoryReadException {
        try {
            Element node = XMLUtils.readNodeFromStream(xmlInput);
            return read(node);
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            throw new RepositoryReadException(
                "Unable to read Level: " + e, e);
        }
        catch (SAXException e) {
            throw new RepositoryReadException(
                "XML exception parsing Level: " + e, e);
        }
    }
    
    public ComponentLevel read(String xmlString) throws RepositoryReadException {
        try {
            Element node = XMLUtils.readNodeFromString(xmlString);
            return read(node);
        }
        catch (SAXException e) {
            throw new RepositoryReadException(
                "XML exception parsing level: " + e, e);
        }
    }
    
    /**
     * Build a Level object from a DOM node.  This scans through the node tree,
     * building up the Level object as it goes.  The object returned from this
     * method is guaranteed to be a valid Level object.
     * @param node
     * @return
     * @throws RepositoryReadException
     */
    public ComponentLevel read(Element node) throws RepositoryReadException {
        if (!node.getNodeName().trim().equalsIgnoreCase(LEVEL_TAG)) {
            throw new RepositoryReadException("Unable to find <" + LEVEL_TAG + "> tag");
        }
        
        String name = XMLUtils.readSingleElementValue(node, "name");
        String description = XMLUtils.readSingleElementValue(node, "description");
        String sortSequence = XMLUtils.readSingleElementValue(node, "sort-sequence");
        String packageName = XMLUtils.readSingleElementValue(node, "package");
        String progId = XMLUtils.readSingleElementValue(node, "program-id");
        String library = XMLUtils.readSingleElementValue(node, "library");
        String editable = XMLUtils.readSingleElementValue(node, "editable");
        
        // Check to make sure required fields are present.
        if (name == null) {
            throw new RepositoryReadException("missing name element in level");
        }
        
        // Create a new ComponentLevel object
        ComponentLevel level = new ComponentLevel(name);
        
        level.setDescription(description);
        
        if (sortSequence != null) {
            try {
                level.setSortseq(Integer.parseInt(sortSequence));
            }
            catch (NumberFormatException e) {
                throw new RepositoryReadException("sort-sequence must be a number (" +
                    sortSequence + "): e", e);
            }
        }
        
        if (packageName != null && packageName.length() != 0) {
            level.setPackage(packageName);
        }
        
        if (progId != null && progId.length() != 0) {
            level.setProgid(progId);
        }
        
        if (library != null && library.length() != 0) {
            level.setLibrary(library);
        }
        
        if (library == null && packageName == null && progId == null) {
            _events.reportWarning("No library. package, or prog ID");
        }
        
        if (editable != null && (editable.equalsIgnoreCase("yes") || editable.equalsIgnoreCase("true"))) {
        	level.setEditable(true);
        }
        
        return level;
    }    
    
    //
    // implementation
    //
    private RepositoryReaderEvents _events;
}
