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
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sam.moca.server.repository.file.RepositoryReadException;

/**
 * XMLUtils
 * 
 * Class that has static methods to make parsing XML easier
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
public class XMLUtils {

    /**
     * Wrapper to readNodeFromFile(File) that accepts a filename instead.
     * 
     * @param filename
     * @return a Node
     * @throws IOException
     * @throws SAXException
     */
    static public Element readNodeFromFile(String filename) throws IOException, SAXException {
        File f = new File(filename);
        return readNodeFromFile(f);
    }

    /**
     * A function to return the root node of an XML document from a file.
     * 
     * @param filename
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static Element readNodeFromFile(File filename) throws IOException, SAXException {
        DocumentBuilder builder = getDocumentBuilder();
        Document d = builder.parse(filename);
        d.setXmlStandalone(true);
        Element docNode = d.getDocumentElement();

        return docNode;
    }

    public static Element readNodeFromString(String xmlDoc) throws SAXException {
        DocumentBuilder builder = getDocumentBuilder();
        try {
            Document d = builder.parse(new InputSource(new StringReader(xmlDoc)));
            d.setXmlStandalone(true);
            Element docNode = d.getDocumentElement();
            return docNode;
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Unable to read string: " + e, e);
        }
    }

    public static Element readNodeFromStream(InputStream xmlStream) throws SAXException,
            IOException {
        DocumentBuilder builder = getDocumentBuilder();
        Document d = builder.parse(new InputSource(xmlStream));
        d.setXmlStandalone(true);
        Element docNode = d.getDocumentElement();
        return docNode;
    }

    public static String readSingleElementValue(Element parent, String tag) throws RepositoryReadException {
        NodeList children = parent.getElementsByTagName(tag);
        if (children.getLength() == 0) {
            return null;
        }
        else if (children.getLength() != 1) {
            throw new RepositoryReadException("Expecting single " + tag + " element");
        }
        return children.item(0).getTextContent();
    }

    public static String readCDataValue(Element parent, String tag) throws RepositoryReadException {
        NodeList children = parent.getElementsByTagName(tag);
        if (children.getLength() == 0) {
            return null;
        }
        else if (children.getLength() != 1) {
            throw new RepositoryReadException("Expecting single " + tag + " element");
        }
        
        Node child = children.item(0).getFirstChild();
        while (child != null && child instanceof Text && child.getTextContent().trim().length() == 0) {
            child = child.getNextSibling();
        }
    
        if (child instanceof CharacterData) {
            return ((CharacterData)child).getData();
        }
        else if (child instanceof Text) {
            return ((Text)child).getTextContent();
        }
        else {
            return null;
        }
    }


    //
    // Implementation
    //
    private static DocumentBuilder getDocumentBuilder() {
        synchronized (XMLUtils.class) {
            if (_factory == null) {
                _factory = DocumentBuilderFactory.newInstance();
                _factory.setCoalescing(false);
                _factory.setExpandEntityReferences(false);
                _factory.setIgnoringComments(false);
                _factory.setIgnoringElementContentWhitespace(true);
                _factory.setNamespaceAware(false);
                _factory.setValidating(false);
            }
        }
        try {
            return _factory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            throw new IllegalArgumentException("Unexpected parser configuration exception", e);
        }

    }

    private static DocumentBuilderFactory _factory;

}
