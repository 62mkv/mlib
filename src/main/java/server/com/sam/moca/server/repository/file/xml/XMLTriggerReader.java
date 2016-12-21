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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sam.moca.MocaInterruptedException;
import com.sam.moca.server.repository.ArgType;
import com.sam.moca.server.repository.ArgumentInfo;
import com.sam.moca.server.repository.ComponentLevel;
import com.sam.moca.server.repository.MocaTrigger;
import com.sam.moca.server.repository.Trigger;
import com.sam.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents;
import com.sam.moca.server.repository.file.RepositoryReadException;
import com.sam.moca.server.repository.file.TriggerReader;

/**
 * XMLTriggerFile -
 *  A class to handle the reading and writing of Trigger definition files.
 *  Currently the Triggers are defined in XML.
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

public class XMLTriggerReader implements TriggerReader {
    
    public static final String TRIGGER_TAG = "trigger";
    
    public XMLTriggerReader() {
        _events = null;
    }
    
    public XMLTriggerReader(RepositoryReaderEvents events) {
        _events = events;
    }
    
    public Trigger read(File xmlFile, ComponentLevel level) throws RepositoryReadException {
        try {
            Element node = XMLUtils.readNodeFromFile(xmlFile);
            MocaTrigger trigger = read(node, level);
            trigger.setFile(xmlFile);
            return trigger;
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            throw new RepositoryReadException(
                "Unable to read Trigger: " + e, e);
        }
        catch (SAXException e) {
            throw new RepositoryReadException(
                "XML exception parsing Trigger: " + e, e);
        }
    }

    public Trigger read(InputStream xmlInput, ComponentLevel level) throws RepositoryReadException {
        try {
            Element node = XMLUtils.readNodeFromStream(xmlInput);
            return read(node, level);
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            throw new RepositoryReadException(
                "Unable to read Trigger: " + e, e);
        }
        catch (SAXException e) {
            throw new RepositoryReadException(
                "XML exception parsing Trigger: " + e, e);
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
    protected MocaTrigger read(Element node, ComponentLevel level) throws RepositoryReadException {
        if (!node.getNodeName().trim().equalsIgnoreCase(TRIGGER_TAG)) {
            throw new RepositoryReadException("Unable to find <" + TRIGGER_TAG + "> tag");
        }
        
        String name = XMLUtils.readSingleElementValue(node, "name");
        String command = XMLUtils.readSingleElementValue(node, "on-command");
        
        // Check to make sure required fields are present.
        if (name == null) {
            throw new RepositoryReadException("missing name element in trigger");
        }

        MocaTrigger trigger = new MocaTrigger(name, command);
        trigger.setSortSequence(level.getSortseq());
        
        String disable = XMLUtils.readSingleElementValue(node, "disable");
        
        boolean disabled = false;
        // If the disable value is set and is yes or 1 then we disable it
        // Disable takes precedence over enable if it is set to true
        if (disable != null && (disable.equalsIgnoreCase("yes") || 
                disable.equals("1") || disable.equalsIgnoreCase("true"))) {
            disabled = true;
        }
        else {
        
            String enable = XMLUtils.readSingleElementValue(node, "enable");
            // If the enable value is set and is no or 0 then we disable it
            if (enable != null && (enable.equalsIgnoreCase("no") || 
                    enable.equals("0") || enable.equalsIgnoreCase("false"))) {
                disabled = true;
            }
        }
        
        trigger.setDisabled(disabled);
        
        String syntax = XMLUtils.readCDataValue(node, "local-syntax");
        
        trigger.setSyntax(syntax);
        
        String fireSequence = XMLUtils.readSingleElementValue(node, 
                "fire-sequence");
        if (fireSequence != null) {
            try {
                trigger.setFireSequence(Integer.parseInt(fireSequence));
            }
            catch (NumberFormatException e) {
                throw new RepositoryReadException("fire-sequence must be a number (" +
                    fireSequence + "): e", e);
            }
        }
        // If the fire sequence is null and syntax is not null it is invalid.
        else if (syntax != null) {
            throw new RepositoryReadException(
                "fire-sequence is required if local-syntax is provided.");
        }

        // Documentation
        NodeList args = (NodeList) node.getElementsByTagName("argument");
        
        for (int i = 0; i < args.getLength(); i++) {
            Element argNode = (Element) args.item(i);
            
            String argName = argNode.getAttribute("name").trim();
            String argAlias = argNode.getAttribute("alias").trim();
            String typeString = argNode.getAttribute("datatype");
            
            String argValue = null;
            if (argNode.hasAttribute("default-value")) {
                argValue = argNode.getAttribute("default-value");
            }
            
            String requiredString = argNode.getAttribute("required");
            String comment = argNode.getTextContent();
            
            ArgType argType = XMLCommandReader.ARGTYPES.get(typeString.toLowerCase());

            boolean required = requiredString.equalsIgnoreCase("yes") || requiredString.equalsIgnoreCase("true") || requiredString.equals("1");
            ArgumentInfo arg = new ArgumentInfo(argName, argAlias, argType, argValue, required);
            arg.setComment(comment);
            
            trigger.addArgument(arg);
        }
        
        return trigger;
    }
    
    //
    // implementation
    //
    @SuppressWarnings("unused")
    private RepositoryReaderEvents _events;
}
