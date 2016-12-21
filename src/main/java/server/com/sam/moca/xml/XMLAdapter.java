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

package com.sam.moca.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.SAXParseException;

import com.sam.util.FixedSizeCache;

/**
 * An XML adapter to support processing XML. These methods are meant only to be
 * called from mxmllib via JNI, which is why this class is not public.
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
 * @author mlange
 * @version $Revision$
 */
class XMLAdapter {
    public XMLAdapter() throws XMLAdapterException {

        try {
            // Create the actual document builder.
            _builder = _documentBuilderFactory.newDocumentBuilder();
            _builder.setErrorHandler(new XMLAdapterErrorHandler());
        }
        catch (ParserConfigurationException e) {
            throw new XMLAdapterException("Could not construct an XMLAdapter: "
                    + e, e);
        }
    }

    public XMLAdapter(Node node) throws XMLAdapterException {
        this();
        _document = (Document) node;
    }

    public Attr addAttribute(Element parent, String name, String value)
            throws XMLAdapterException {
        Attr attr = null;

        // Add an attribute and get it back for our caller.
        try {
            parent.setAttribute(name, value);
            attr = parent.getAttributeNode(name);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not add attribute: " + e, e);
        }

        return attr;
    }

    public Comment addComment(Node parent, String value)
            throws XMLAdapterException {
        Comment comment = null;

        // Add a comment.
        try {
            comment = _document.createComment(value);
            parent.appendChild(comment);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not add comment: " + e, e);
        }

        return comment;
    }

    public Element addElement(Node parent, String name)
            throws XMLAdapterException {
        Element element = null;

        // Add an element.
        try {
            element = _document.createElement(name);
            parent.appendChild(element);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not add element: " + e, e);
        }

        return element;
    }

    public ProcessingInstruction addProcessingInstruction(Node parent,
                                                          String target,
                                                          String data)
            throws XMLAdapterException {
        ProcessingInstruction pi = null;

        // Add a processing instruction.
        try {
            pi = _document.createProcessingInstruction(target, data);
            parent.appendChild(pi);
        }
        catch (Exception e) {
            throw new XMLAdapterException(
                "Could not add processing instruction: " + e, e);
        }

        return pi;
    }

    public Text addTextNode(Element parent, String value)
            throws XMLAdapterException {
        Text text = null;

        // Add a text node.
        try {
            text = _document.createTextNode(value);
            parent.appendChild(text);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not add text node: " + e, e);
        }

        return text;
    }

    public Node appendChild(Node parent, Node newChild)
            throws XMLAdapterException {

        // Append the child.
        try {
            parent.appendChild(newChild);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not append child: " + e, e);
        }

        return newChild;
    }

    public XMLAdapter applyStylesheetFromFile(String pathname)
            throws XMLAdapterException {
        XMLAdapter xmlWrapper = null;

        // Apply the stylesheet to the XML.
        try {
            // Try to get a cached transformer for this pathname.
            Transformer transformer = (Transformer) _transformerCache
                .get(pathname);

            // Create a new transformer for this pathname and put it into the
            // cache.
            if (transformer == null) {
                transformer = _transformerFactory
                    .newTransformer(new StreamSource(pathname));
                _transformerCache.put(pathname, transformer);
            }

            // Create and input and output source for the transformer.
            DOMSource input = new DOMSource(_document);
            DOMResult output = new DOMResult();

            // Apply the stylesheet to the XML.
            transformer.transform(input, output);

            // Create a new XMLAdapter from the resulting XML.
            xmlWrapper = new XMLAdapter(output.getNode());
        }
        catch (TransformerException e) {
            throw new XMLAdapterException(
                "Could not apply stylesheet from file: " + e, e);
        }

        return xmlWrapper;
    }

    public XMLAdapter applyStylesheetFromString(String xsl, String encoding)
            throws XMLAdapterException {
        XMLAdapter xmlWrapper = null;

        // Apply the stylesheet to the XML.
        try {
            // Create a transformer.
            Transformer transformer = _transformerFactory
                .newTransformer(new StreamSource(new ByteArrayInputStream(xsl
                    .getBytes(encoding))));

            // Create and input and output source for the transformer.
            DOMSource input = new DOMSource(_document);
            DOMResult output = new DOMResult();

            // Apply the stylesheet to the XML.
            transformer.transform(input, output);

            // Create a new XMLAdapter from the resulting XML.
            xmlWrapper = new XMLAdapter(output.getNode());
        }
        catch (TransformerException e) {
            throw new XMLAdapterException(
                    "Could not apply stylesheet from file: " + e, e);
        }
        catch (UnsupportedEncodingException e) {
            throw new XMLAdapterException(
                    "Could not apply stylesheet from file: " + e, e);
        }

        return xmlWrapper;
    }

    public Node cloneNode(Node node, boolean deep) throws XMLAdapterException {
        Node clone = null;

        // Clone the node.
        try {
            clone = node.cloneNode(deep);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not clone node: " + e, e);
        }

        return clone;
    }

    public void configure(boolean preserveWhitespace, boolean validate) {

        // Set our private variables.
        _configValidate = validate;
        _configPreserveWhitespace = preserveWhitespace;

        // Turn on validating within the document builder.
        _documentBuilderFactory.setValidating(_configValidate);
    }

    public Attr createAttribute(String name) throws XMLAdapterException {
        Attr attr = null;

        // Make sure a document already exists.
        if (_document == null)
            throw new XMLAdapterException("A document does not exist", null);

        // Create a new attribute.
        try {
            attr = _document.createAttribute(name);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not create attribute: " + e, e);
        }

        return attr;
    }

    public CDATASection createCDATASection(String value)
            throws XMLAdapterException {
        CDATASection cdata = null;

        // Make sure a document already exists.
        if (_document == null)
            throw new XMLAdapterException("A document does not exist", null);

        // Create a new character data section.
        try {
            cdata = _document.createCDATASection(value);
        }
        catch (Exception e) {
            throw new XMLAdapterException(
                "Could not create character data section: " + e, e);
        }

        return cdata;
    }

    public Comment createComment(String value) throws XMLAdapterException {
        Comment cmnt = null;

        // Make sure a document already exists.
        if (_document == null)
            throw new XMLAdapterException("A document does not exist", null);

        // Create a new comment.
        try {
            cmnt = _document.createComment(value);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not create comment: " + e, e);
        }

        return cmnt;
    }

    public Document createDocument() throws XMLAdapterException {

        // Create a new document.
        try {
            _document = _builder.newDocument();
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not create document: " + e, e);
        }

        return _document;
    }

    public DocumentFragment createDocumentFragment() throws XMLAdapterException {
        DocumentFragment fragment = null;
        ;

        // Make sure a document already exists.
        if (_document == null)
            throw new XMLAdapterException("A document does not exist", null);

        // Create a new document fragment.
        try {
            fragment = _document.createDocumentFragment();
        }
        catch (Exception e) {
            throw new XMLAdapterException(
                "Could not create document fragment: " + e, e);
        }

        return fragment;
    }

    public Element createElement(String name) throws XMLAdapterException {
        Element element = null;
        ;

        // Make sure a document already exists.
        if (_document == null)
            throw new XMLAdapterException("A document does not exist", null);

        // Create a new element.
        try {
            element = _document.createElement(name);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not create element: " + e, e);
        }

        return element;
    }

    public ProcessingInstruction createPI(String target, String data)
            throws XMLAdapterException {
        ProcessingInstruction pi = null;

        // Make sure a document already exists.
        if (_document == null)
            throw new XMLAdapterException("A document does not exist", null);

        // Create a new processing instruction.
        try {
            pi = _document.createProcessingInstruction(target, data);
        }
        catch (Exception e) {
            throw new XMLAdapterException(
                "Could not create processing instruction: " + e, e);
        }

        return pi;
    }

    public Text createTextNode(String value) throws XMLAdapterException {
        Text text = null;

        // Make sure a document already exists.
        if (_document == null)
            throw new XMLAdapterException("A document does not exist", null);

        // Create a new text node.
        try {
            text = _document.createTextNode(value);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not create text node: " + e, e);
        }

        return text;
    }

    public void dumpContext() throws XMLAdapterException {
    }

    public String getAttribute(Element element, String name)
            throws XMLAdapterException {
        String value = null;

        // Get the attribute value.
        try {
            value = element.getAttribute(name);
        }
        catch (Exception e) {
            throw new XMLAdapterException(
                "Could not get attribute value: " + e, e);
        }

        return value;
    }

    public Attr getAttributeNode(Element element, String name)
            throws XMLAdapterException {
        Attr attr = null;

        // Get the attribute node.
        try {
            attr = element.getAttributeNode(name);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get attribute node: " + e,
                e);
        }

        return attr;
    }

    public NamedNodeMap getAttributes(Element element)
            throws XMLAdapterException {
        NamedNodeMap attrs = null;

        // Get the attributes.
        try {
            attrs = element.getAttributes();
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get attributes: " + e, e);
        }

        return attrs;
    }

    public String getAttrName(Attr attr) throws XMLAdapterException {
        String name = null;

        // Get the attribute name.
        try {
            name = attr.getName();
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get attribute name: " + e,
                e);
        }

        return name;
    }

    public boolean getAttrSpecified(Attr attr) throws XMLAdapterException {
        boolean specified = false;

        // Determent if the attribute is specified.
        try {
            specified = attr.getSpecified();
        }
        catch (Exception e) {
            throw new XMLAdapterException(
                "Could not determine if attribute specified: " + e, e);
        }

        return specified;
    }

    public String getAttrValue(Attr attr) throws XMLAdapterException {
        String value = null;

        // Get the attribute value.
        try {
            value = attr.getValue();
        }
        catch (Exception e) {
            throw new XMLAdapterException(
                "Could not get attribute value: " + e, e);
        }

        return value;
    }

    public String getCharacterData(CharacterData cdata)
            throws XMLAdapterException {
        String data = null;

        // Get the character data.
        try {
            data = cdata.getData();
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get character data: " + e,
                e);
        }

        return data;
    }

    public int getCharacterDataLength(CharacterData cdata)
            throws XMLAdapterException {
        int length = -1;

        // Get the character data length.
        try {
            length = cdata.getLength();
        }
        catch (Exception e) {
            throw new XMLAdapterException(
                "Could not get character data length: " + e, e);
        }

        return length;
    }

    public NodeList getChildNodes(Node node) throws XMLAdapterException {
        NodeList nodeList = null;

        // Get the child node list.
        try {
            nodeList = node.getChildNodes();
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get child nodes: " + e, e);
        }

        return nodeList;
    }

    public Document getDocument() throws XMLAdapterException {
        return _document;
    }

    public Element getDocumentElement() throws XMLAdapterException {
        Element element = null;

        // Make sure a document already exists.
        if (_document == null)
            throw new XMLAdapterException("A document does not exist", null);

        // Get the document element.
        try {
            element = _document.getDocumentElement();
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get document element: "
                    + e, e);
        }

        return element;
    }

    public NodeList getElementsByTagName(Element parent, String tagname)
            throws XMLAdapterException {
        NodeList nodeList = null;

        // Get the elements by tag name.
        try {
            nodeList = parent.getElementsByTagName(tagname);
        }
        catch (Exception e) {
            throw new XMLAdapterException(
                "Could not get elements by tag name: " + e, e);
        }

        /*
         * This is annoying, but the XML spec is a little vague on exactly what
         * should be returned if no matching elements have been found. Some
         * implementations return a NodeList with a length of 0 and other
         * implementations return a null. We have pre-existing code that assumes
         * a null will be returned, so we play it safe here and insure that
         * whatever parser is being used under the covers that we do what our
         * caller expects.
         */
        if (nodeList.getLength() == 0) return null;

        return nodeList;
    }

    public Node getFirstChild(Node node) throws XMLAdapterException {
        Node child = null;

        // Get the first child.
        try {
            child = node.getFirstChild();
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get first child: " + e, e);
        }

        return child;
    }


    public Node getItem(Object thing, int index) throws XMLAdapterException {

        if (thing instanceof NamedNodeMap) {
            return getItem((NamedNodeMap) thing, index);
        }
        else {
            return getItem((NodeList) thing, index);
        }
    }

    public Node getLastChild(Node node) throws XMLAdapterException {
        Node child = null;

        // Get the last child.
        try {
            child = node.getLastChild();
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get last child: " + e, e);
        }

        return child;
    }

    public Node getNamedItem(NamedNodeMap nodeMap, String name)
            throws XMLAdapterException {
        Node item = null;

        // Get the named item.
        try {
            item = nodeMap.getNamedItem(name);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get named item: " + e, e);
        }

        return item;
    }

    public Node getNextSibling(Node node) throws XMLAdapterException {
        Node sibling = null;

        // Get the next sibling.
        try {
            sibling = node.getNextSibling();
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get next sibling: " + e, e);
        }

        return sibling;
    }
    
    public int getNodeListLength(Object thing) throws XMLAdapterException {

        if (thing instanceof NamedNodeMap) {
            return getNodeListLength((NamedNodeMap) thing);
        }
        else {
            return getNodeListLength((NodeList) thing);
        }
    }

    public String getNodeName(Node node) throws XMLAdapterException {
        String name = null;

        // Get the node name.
        try {
            name = node.getNodeName();
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get node name: " + e, e);
        }

        return name;
    }

    public int getNodeType(Node node) throws XMLAdapterException {
        int nodeType = -1;

        // Get the node type;
        try {
            nodeType = node.getNodeType();
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get node type: " + e, e);
        }

        return nodeType;
    }

    public String getNodeValue(Node node) throws XMLAdapterException {
        String value = null;

        // Get the node value;
        try {
            value = node.getNodeValue();
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get node value: " + e, e);
        }

        return value;
    }

    public Node getParentNode(Node node) throws XMLAdapterException {
        Node parent = null;

        // Get the parent node.
        try {
            parent = node.getParentNode();
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get parent node: " + e, e);
        }

        return parent;
    }

    public String getPIData(ProcessingInstruction pi)
            throws XMLAdapterException {
        String data = null;

        // Get the processing instruction data.
        try {
            data = pi.getData();
        }
        catch (Exception e) {
            throw new XMLAdapterException(
                "Could not get processing instruction data: " + e, e);
        }

        return data;
    }

    public String getPITarget(ProcessingInstruction pi)
            throws XMLAdapterException {
        String target = null;

        // Get the processing instruction target.
        try {
            target = pi.getTarget();
        }
        catch (Exception e) {
            throw new XMLAdapterException(
                "Could not get processing instruction target: " + e, e);
        }

        return target;
    }

    public Node getPreviousSibling(Node node) throws XMLAdapterException {
        Node sibling = null;

        // Get the previous sibling.
        try {
            sibling = node.getPreviousSibling();
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get previous sibling: "
                    + e, e);
        }

        return sibling;
    }

    public String getTagName(Element element) throws XMLAdapterException {
        String name = null;

        // Get the tag name.
        try {
            name = element.getTagName();
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get tag name: " + e, e);
        }

        return name;
    }

    public boolean hasChildNodes(Node parent) throws XMLAdapterException {
        boolean hasChildNodes = false;

        // Determine if this node has child nodes.
        try {
            hasChildNodes = parent.hasChildNodes();
        }
        catch (Exception e) {
            throw new XMLAdapterException(
                "Could not determine if node has child nodes: " + e, e);
        }

        return hasChildNodes;
    }

    public Node importNode(Node importedNode, boolean deep) throws XMLAdapterException {
        Node node;
        
        // Import the node.
        try {  
            node = _document.importNode(importedNode, deep);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not import node: " + e, e);
        }

        return node;
    }

    
    public Node insertBefore(Node parent, Node newChild, Node refChild)
            throws XMLAdapterException {

        // Insert the node.
        try {
            parent.insertBefore(newChild, refChild);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not insert node: " + e, e);
        }

        return newChild;
    }

    public Document parseFile(String pathname) throws XMLAdapterException {
        InputStream is = null;
        Document document = null;

        // Parse the XML file.
        try {
            is = new FileInputStream(new File(pathname));
            document = _builder.parse(is);
            
            // Strip whitespace if necessary.
            if (!_configPreserveWhitespace) _stripWhitespace(document);
        }
        catch (SAXParseException e) {
            // Build a helpful message for the caller.
            String msg = e.getMessage() + "\n" + "URL: " + e.getSystemId()
                    + "\n" + "Line: " + e.getLineNumber() + ", Position: "
                    + e.getColumnNumber();

            throw new XMLAdapterException(msg, e);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not parse XML file: " + e, e);
        }
        finally {
            if (is != null) {
                try { is.close(); } catch (IOException ignore) { }
            }
        }

        // Keep a reference to this document.
        _document = document;

        return document;
    }
    
    public Document parseString(String xml, String encoding) throws XMLAdapterException {
        Document document = null;

        // Parse the XML.
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes(encoding));
            document = _builder.parse(is);

            // Strip whitespace if necessary.
            if (!_configPreserveWhitespace) _stripWhitespace(document);
        }
        catch (SAXParseException e) {
            // Build a helpful message for the caller.
            String msg = e.getMessage() + "\n" + "Line: " + e.getLineNumber()
                    + ", Position: " + e.getColumnNumber();

            throw new XMLAdapterException(msg, e);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not parse XML string: " + e, e);
        }

        // Keep a reference to this document.
        _document = document;

        return document;
    }

    public void removeAttribute(Element element, String name)
            throws XMLAdapterException {
        // Remove the attribute.
        try {
            element.removeAttribute(name);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not remove attribute: " + e, e);
        }
    }

    public void removeAttributeNode(Element element, Attr attr)
            throws XMLAdapterException {

        // Remove the attribute node.
        try {
            element.removeAttributeNode(attr);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not remove attribute node: "
                    + e, e);
        }
    }

    public void removeChild(Node parent, Node oldChild)
            throws XMLAdapterException {

        // Remove the child.
        try {
            parent.removeChild(oldChild);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not remove child: " + e, e);
        }
    }

    public void removeNamedItem(NamedNodeMap nodeMap, String name)
            throws XMLAdapterException {

        // Remove the named item.
        try {
            nodeMap.removeNamedItem(name);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not remove named item: " + e,
                e);
        }
    }

    public void replaceChild(Node parent, Node newChild, Node oldChild)
            throws XMLAdapterException {

        // Replace the child.
        try {
            parent.replaceChild(newChild, oldChild);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not replace child: " + e, e);
        }
    }

    public void setAttribute(Element element, String name, String value)
            throws XMLAdapterException {
        // Set the attribute.
        try {
            element.setAttribute(name, value);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not set attribute: " + e, e);
        }
    }

    public void setAttributeNode(Element element, Attr attr)
            throws XMLAdapterException {

        // Set the attribute node.
        try {
            element.setAttributeNode(attr);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not set attribute node: " + e,
                e);
        }
    }

    public void setAttrValue(Attr attr, String value)
            throws XMLAdapterException {

        // Set the attribute value.
        try {
            attr.setValue(value);
        }
        catch (Exception e) {
            throw new XMLAdapterException(
                "Could not set attribute value: " + e, e);
        }
    }

    public void setCharacterData(CharacterData cdata, String value)
            throws XMLAdapterException {

        // Set the character data.
        try {
            cdata.setData(value);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not set character data: " + e,
                e);
        }
    }

    public void setNamedItem(NamedNodeMap nodeMap, Node node)
            throws XMLAdapterException {

        // Set the named item.
        try {
            nodeMap.setNamedItem(node);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not set named item: " + e, e);
        }
    }

    public void setNodeValue(Node node, String value)
            throws XMLAdapterException {

        // Set the node value;
        try {
            node.setNodeValue(value);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not set node value: " + e, e);
        }
    }

    public void setPIData(ProcessingInstruction pi, String data)
            throws XMLAdapterException {

        // Set the processing instruction data.
        try {
            pi.setData(data);
        }
        catch (Exception e) {
            throw new XMLAdapterException(
                "Could not set processing instruction data: " + e, e);
        }
    }

    public String toString(Node node) throws XMLAdapterException {
        // Get the document node if necessary.
        if (node == null) node = _document;

        try {
            return _xmlWrapper.toString(node);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not serialize XML: " + e, e);
        }
    }

    public String toStringRaw(Node node) throws XMLAdapterException {
        // Get the document node if necessary.
        if (node == null) node = _document;

        try {
            return _xmlWrapper.toStringRaw(node);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not serialize raw XML: " + e,
                e);
        }
    }

    public void writeFile(Node node, String pathname)
            throws XMLAdapterException {
        // Get the document node if necessary.
        if (node == null) node = _document;

        // Create a writer for the serializer.
        try {
            _xmlWrapper.writeFile(node, pathname);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not serialize XML to file: "
                    + e, e);
        }
    }

    public void writeRawFile(Node node, String pathname)
            throws XMLAdapterException {
        // Get the document node if necessary.
        if (node == null) node = _document;

        // Create a writer for the serializer.
        try {
            _xmlWrapper.writeRawFile(node, pathname);
        }
        catch (Exception e) {
            throw new XMLAdapterException(
                "Could not serialize raw XML to file: " + e, e);
        }
    }

    private void _stripWhitespace(Node node) {
        NodeList list = node.getChildNodes();

        List<Node> childrenNodes = new ArrayList<Node>();
        
        // We must add the notes to a temporary container, since we may be
        // removing them afterwards and we want to make sure to not mess up
        // the ordering of them
        for (int i = 0; i < list.getLength(); i++) {
            childrenNodes.add(list.item(i));
        }
        
        for (Node childNode : childrenNodes) {
            // Remove a node from the parent if it was a text node with nothing
            // in it
            if (childNode.getNodeType() == Node.TEXT_NODE
                    && childNode.getNodeValue().trim().equals("")) {
                node.removeChild(childNode);
            }
            else {
                _stripWhitespace(childNode);
            }
        }
    }

    private Node getItem(NodeList nodeList, int index)
            throws XMLAdapterException {
        Node item = null;

        // Get the item from the node list.
        try {
            item = nodeList.item(index);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get item: " + e, e);
        }

        return item;
    }

    private Node getItem(NamedNodeMap namedNodeMap, int index)
            throws XMLAdapterException {
        Node item = null;

        // Get the item from the named node map.
        try {
            item = namedNodeMap.item(index);
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get item: " + e, e);
        }

        return item;
    }
    
    private int getNodeListLength(NodeList nodeList) throws XMLAdapterException {
        int length = -1;

        // Get the number of nodes in the node list.
        try {
            length = nodeList.getLength();
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get node list length: "
                    + e, e);
        }
        
        return length;
    }
    
    private int getNodeListLength(NamedNodeMap namedNodeMap) throws XMLAdapterException {
        int length = -1;

        // Get the number of nodes in the named node map.
        try {
            length = namedNodeMap.getLength();
        }
        catch (Exception e) {
            throw new XMLAdapterException("Could not get node list length: "
                    + e, e);
        }
        
        return length;
    }
    // Create a fixed-size cache for caching of existing transformers.    
    private static final FixedSizeCache<String, Transformer> _transformerCache = 
        new FixedSizeCache<String, Transformer>(5);

    private static final DocumentBuilderFactory _documentBuilderFactory;
    // Create a transformer factory.
    private static final TransformerFactory _transformerFactory = 
        TransformerFactory.newInstance();
    // Create an XML wrapper to support serialization.
    private static final XMLWrapper _xmlWrapper = new XMLWrapper();
    
    private DocumentBuilder _builder = null;
    private Document _document = null;

    private boolean _configPreserveWhitespace = false;
    private boolean _configValidate = false;
    
    static {
        // Create a document builder factory.
        _documentBuilderFactory = DocumentBuilderFactory.newInstance();

        // Set attributes for the document builder factory.
        _documentBuilderFactory.setCoalescing(false);
        _documentBuilderFactory.setExpandEntityReferences(false);
        _documentBuilderFactory.setIgnoringComments(false);
        _documentBuilderFactory.setIgnoringElementContentWhitespace(true);
        _documentBuilderFactory.setNamespaceAware(false);
        _documentBuilderFactory.setValidating(false);
        _documentBuilderFactory.setXIncludeAware(false);
    }
}