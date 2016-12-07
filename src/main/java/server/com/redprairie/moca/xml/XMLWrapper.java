/*  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * MOCA components to process XML. These components are meant only to be called
 * from mxmllib via JNI, which is why this class is not public.
 * 
 * Component libraries or libraries written in Java should call JAXP directly
 * rather than using this class.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author mlange
 * @version $Revision$
 */
public class XMLWrapper {
    public XMLWrapper() {
    }

    private void _serialize(PrintWriter writer, Node node, boolean raw) {
        boolean debug = false;

        int nodeType = node.getNodeType();

        switch (nodeType) {
        case Node.ELEMENT_NODE:
            if (debug) writer.print("#ELEMENT# ");
            writer.print('<');
            writer.print(node.getNodeName());

            // Get each attribute for this element.
            NamedNodeMap attrs = node.getAttributes();
            int attrsLength = attrs.getLength();

            // Cycle through each attribute.
            for (int i = 0; i < attrsLength; i++) {
                Attr attr = (Attr) attrs.item(i);
                writer.print(' ');
                writer.print(attr.getNodeName());
                writer.print("=\"");
                if (raw)
                    writer.print(attr.getNodeValue());
                else
                    _serializeEntityReference(writer, attr.getNodeValue());
                writer.print("\"");
            }

            // We want to close out this element right away if it does not have any children.
            Node firstChild = node.getFirstChild();
            if (firstChild == null) {
                writer.print("/>\n");
                break;
            }

            // We don't want to add a linefeed if the child node is a text node.        
            int firstChildNodeType = firstChild.getNodeType();
            if (firstChildNodeType == Node.TEXT_NODE)
                writer.print(">");
            else
                writer.print(">\n");

            // Cycle through each child of this node.
            NodeList children = node.getChildNodes();
            if (children != null) {
                int len = children.getLength();

                for (int i = 0; i < len; i++) {
                    _serialize(writer, children.item(i), raw);
                }
            }

            if (debug) writer.print("#ELEMENT# ");
            writer.print("</");
            writer.print(node.getNodeName());
            writer.print(">\n");
            break;

        case Node.TEXT_NODE:
            if (debug) writer.print("#TEXT# ");
            if (raw)
                writer.print(node.getNodeValue());
            else
                _serializeEntityReference(writer, node.getNodeValue());
            break;

        case Node.CDATA_SECTION_NODE:
            if (debug) writer.print("#CDATA# ");
            writer.print("<![CDATA[");
            writer.print(node.getNodeValue());
            writer.print("]]>\n");
            break;

        case Node.DOCUMENT_NODE:
            Node child = node.getFirstChild();
            while (child != null) {
                _serialize(writer, child, raw);
                child = child.getNextSibling();
            }
            break;

        case Node.PROCESSING_INSTRUCTION_NODE:
            if (debug) writer.print("#PI# ");
            writer.print("<?");
            writer.print(node.getNodeName());
            String data = node.getNodeValue();
            if (data != null && data.length() > 0) {
                writer.print(' ');
                writer.print(data);
            }
            writer.print("?>\n\n");
            break;

        case Node.COMMENT_NODE:
            if (debug) writer.print("#COMMENT# ");
            writer.print("<!--");
            writer.print(node.getNodeValue());
            writer.print("-->\n");
            break;
        }
    }

    private void _serializeEntityReference(PrintWriter writer, String value) {
        int length = (value != null) ? value.length() : 0;

        for (int i = 0; i < length; i++) {
            char ch = value.charAt(i);
            switch (ch) {
            case '<':
                writer.append("&lt;");
                break;
            case '>':
                writer.append("&gt;");
                break;
            case '&':
                writer.append("&amp;");
                break;
            case '\'':
                writer.append("&apos;");
                break;
            case '"':
                writer.append("&quot;");
                break;
            default:
                writer.append(ch);
            }
        }
    }

    public String toString(Node node) throws XMLWrapperException {

        try {
            // Create a writer for the serializer.
            StringWriter writer = new StringWriter();
            PrintWriter out = new PrintWriter(writer);

            // Serialize the XML.
            _serialize(out, node, false);

            // Close the writer and get the serialized XML.
            out.close();
            return writer.toString();
        }
        catch (Exception e) {
            throw new XMLWrapperException("Could not serialize XML: " + e, e);
        }
    }

    public String toStringRaw(Node node) throws XMLWrapperException {
        try {
            // Create a writer for the serializer.
            StringWriter writer = new StringWriter();
            PrintWriter out = new PrintWriter(writer);

            // Serialize the XML.
            _serialize(out, node, true);

            // Close the writer and get the serialized XML.
            out.close();
            return writer.toString();
        }
        catch (Exception e) {
            throw new XMLWrapperException("Could not serialize raw XML: " + e,
                e);
        }
    }

    public void writeFile(Node node, String pathname)
            throws XMLWrapperException {
        PrintWriter out = null;

        // Create a writer for the serializer.
        try {
            out = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(new File(pathname)), 
                    "UTF-8"));

            // Serialize the XML.
            _serialize(out, node, false);
        }
        catch (IOException e) {
            throw new XMLWrapperException("Could not serialize XML to file: "
                    + e, e);
        }
        finally {
            // Close the writer.
            if (out != null) out.close();
        }
    }

    public void writeRawFile(Node node, String pathname)
            throws XMLWrapperException {
        PrintWriter out = null;

        // Create a writer for the serializer.
        try {
            out = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(new File(pathname)), 
                "UTF-8"));

            // Serialize the XML.
            _serialize(out, node, true);
        }
        catch (IOException e) {
            throw new XMLWrapperException(
                "Could not serialize raw XML to file: " + e, e);
        }
        finally {
            // Close the writer.
            if (out != null) out.close();
        }
    }
}
