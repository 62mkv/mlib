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

package com.redprairie.moca.xml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import junit.framework.TestCase;

import org.junit.Assert;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Unit tests for the XMLAdapter class.
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author mlange
 * @version $Revision$
 */
public class TU_XMLAdapter extends TestCase {

    public void testConstructor() throws XMLAdapterException {
        XMLAdapter wrapper = new XMLAdapter();
        assertNotNull(wrapper);
    }

    public void testAddAttribute() throws XMLAdapterException {
        XMLAdapter wrapper = new XMLAdapter();
        Document doc = wrapper.createDocument();
        Element e = wrapper.addElement(doc, "foo");
        wrapper.addAttribute(e, "bar", "baz");
        assertEquals("<foo bar=\"baz\"/>\n", wrapper.toString(doc));
    }

    public void testApplyStylesheetFromFile() {

        try {
            String pathname = null;
            XMLAdapter xmlWrapper = new XMLAdapter();

            pathname = getResourcePathname(xmlStylesheetTestResource);
            xmlWrapper.parseFile(pathname);

            pathname = getResourcePathname(xslStylesheetTestResource);
            XMLAdapter htmlWrapper = xmlWrapper.applyStylesheetFromFile(pathname);

            String html = htmlWrapper.toString(null);
            Assert.assertNotNull(html);
        }
        catch (XMLAdapterException e) {
            fail("applyStylesheetFromString() failed: " + e);
        }
    }

    public void testApplyStylesheetFromString() {

        try {
            String xsl = null;
            String pathname = null;
            XMLAdapter xmlWrapper = new XMLAdapter();

            pathname = getResourcePathname(xmlStylesheetTestResource);
            xmlWrapper.parseFile(pathname);

            xsl = getResourceContents(xslStylesheetTestResource);

            XMLAdapter htmlWrapper = xmlWrapper.applyStylesheetFromString(xsl, "UTF-8");

            String html = htmlWrapper.toString(null);
            Assert.assertNotNull(html);
        }
        catch (XMLAdapterException e) {
            fail("applyStylesheetFromString() failed: " + e);
        }
    }

    public void testCreateElement() {

        try {
            XMLAdapter wrapper = new XMLAdapter();

            wrapper.createDocument();
            wrapper.createElement("foo");
        }
        catch (XMLAdapterException e) {
            fail("XMLAdapter.createElement(String) failed: " + e);
        }

    }

    public void testGetAttributes() {

        try {
            XMLAdapter wrapper = new XMLAdapter();

            String pathname = getResourcePathname(xmlTestResource);
            Document document = wrapper.parseFile(pathname);
            Element element = document.getDocumentElement();

            NamedNodeMap nodeMap = wrapper.getAttributes(element);

            int numAttrs = nodeMap.getLength();
            Assert.assertTrue(
                    "There shouldn't be any attributes on the top element", 
                    numAttrs == 0);

        }
        catch (XMLAdapterException e) {
            fail("XMLAdapter.getAttributes(Element) failed: " + e);
        }

    }
    public void testGetCharacterData() {

        try {
            String pathname = getResourcePathname(xmlTestResource);


            XMLAdapter wrapper = new XMLAdapter();
            Document document = wrapper.parseFile(pathname);
            Assert.assertNotNull(document);
            Element element = wrapper.getDocumentElement();
            NodeList nodeList = wrapper.getElementsByTagName(element,
                "local-syntax");

            int nodeListLength = wrapper.getNodeListLength(nodeList);
            Assert.assertTrue("Node list was empty", nodeListLength > 0);

            Node node = wrapper.getItem(nodeList, 0);

            node = wrapper.getFirstChild(node);

            int nodeType = wrapper.getNodeType(node);
            
            Assert.assertEquals(Node.CDATA_SECTION_NODE, nodeType);

            String value = wrapper.getCharacterData((CharacterData) node);
            Assert.assertNotNull(value);
        }
        catch (XMLAdapterException e) {
            fail("XMLAdapter.getCharacterData(Node) failed " + e);
        }

    }

    public void testParseFile() {

        try {
            String pathname = getResourcePathname(xmlTestResource);


            XMLAdapter wrapper = new XMLAdapter();
            Document document = wrapper.parseFile(pathname);
            
            failOnTextNodePresent(document);

        }
        catch (XMLAdapterException e) {
            fail("XMLAdapter.parseFile(String) failed: " + e);
        }

    }
    
    private void failOnTextNodePresent(Node node) {
        NodeList list = node.getChildNodes();
        
        // Loop through all the present nodes and fail if we find an empty text
        // node.  If not go to it's child node to check until we run out
        for (int i = 0; i < list.getLength(); i++) {
            Node childNode = list.item(i);
            // Remove a node from the parent if it was a text node with nothing
            // in it
            if (childNode.getNodeType() == Node.TEXT_NODE
                    && childNode.getNodeValue().trim().equals("")) {
                fail("There was a text node present on node: " + childNode + 
                        " having a parent of: " + node);
            }
            else {
                failOnTextNodePresent(childNode);
            }
        }
    }

    public void testParseString() {

        try {
            String xml = getResourceContents(xmlTestResource);


            XMLAdapter wrapper = new XMLAdapter();
            Document document = wrapper.parseString(xml, "UTF-8");
            Assert.assertNotNull(document);
        }
        catch (XMLAdapterException e) {
            fail("XMLAdapter.parseString(String) failed: " + e);
        }

    }

    public void testRawString() {

        try {
            String pathname = getResourcePathname(xmlTestResource);


            XMLAdapter wrapper = new XMLAdapter();
            Document document = wrapper.parseFile(pathname);
            String xml = wrapper.toStringRaw(document);
            Assert.assertNotNull(xml);
        }
        catch (XMLAdapterException e) {
            fail("XMLAdapter.rawString(Document) failed: " + e);
        }

        try {
            String pathname = getResourcePathname(xmlTestResource);


            XMLAdapter wrapper = new XMLAdapter();
            Document document = wrapper.parseFile(pathname);
            String xml = wrapper.toStringRaw(document);
            Assert.assertNotNull(xml);

        }
        catch (XMLAdapterException e) {
            fail("XMLAdapter.rawString(null) failed: " + e);
        }

    }

    public void testString() {

        try {
            String pathname = getResourcePathname(xmlTestResource);

            XMLAdapter wrapper = new XMLAdapter();
            Document document = wrapper.parseFile(pathname);
            String xml = wrapper.toString(document);
            Assert.assertNotNull(xml);
        }
        catch (XMLAdapterException e) {
            fail("XMLAdapter.string(Document) failed: " + e);
        }

    }

    private String getResourceContents(String resource) {
        StringBuilder contents = new StringBuilder();

        BufferedReader input = null;
        try {
            String line = null;

            input = new BufferedReader(new InputStreamReader(TU_XMLAdapter.class
                .getResourceAsStream(resource), "UTF-8"));

            while ((line = input.readLine()) != null) {
                contents.append(line);
                contents.append(System.getProperty("line.separator"));
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            try {
                if (input != null) {
                    // flush and close both "input" and its underlying
                    // FileReader
                    input.close();
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return contents.toString();
    }

    private String getResourcePathname(String resource) {
        
        URL url = TU_XMLAdapter.class.getResource(resource);
        return url.getPath();
    }

    private final static String xmlTestResource = "test/test.xml";

    private final static String xmlStylesheetTestResource = "test/stylesheet.xml";
    private final static String xslStylesheetTestResource = "test/stylesheet.xsl";
}

