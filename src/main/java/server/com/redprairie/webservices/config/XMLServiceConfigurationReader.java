/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2006
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

package com.redprairie.webservices.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.util.ArgCheck;

/**
 * Reads the Service Config xml file and build the Service Config Object
 * 
 * <b>
 * 
 * <pre>
 *     Copyright (c) 2005 RedPrairie Corporation
 *     All rights reserved.
 * </pre>
 * 
 * </b>
 * 
 * @author Mohanesha.C
 * @version $Revision$
 */
public class XMLServiceConfigurationReader {
    
    public static final String CONFIG_DTD_SYSTEMID = "service-config.dtd";

    public XMLServiceConfigurationReader(File configFile)
            throws FileNotFoundException {
        this(new BufferedInputStream(new FileInputStream(configFile)));
    }
    
    public XMLServiceConfigurationReader(InputStream in) {
        ArgCheck.notNull(in);
        _in = in;
    }

    /**
     * Parse the Service Config Xml and then builds the Service Config object
     * 
     * @return ServiceConfiguration
     * @throws RPLogicalException
     * @throws ServiceConfigException
     */
    public ServiceConfiguration process() throws ServiceConfigException {
        ServiceConfiguration servConfig = null;
        Document doc = readConfig();

        if (doc != null) {
            servConfig = parseXML(doc);
        }

        return servConfig;
    }

    /**
     * Parse the Document object and then builds the Service Config object
     * 
     * @param Document
     * @return ServiceConfiguration
     * @throws RPLogicalException
     * @throws ServiceConfigException
     */
    private ServiceConfiguration parseXML(Document doc)
            throws ServiceConfigException {
        Element serviceConfigNode = doc.getDocumentElement();
        if (serviceConfigNode == null) {
            throw new ServiceConfigException("service-config not present");
        }

        try {
            ServiceConfiguration config = new ServiceConfiguration();

            NodeList serviceNodeList = serviceConfigNode.getElementsByTagName("service");

            for (int j = 0; j < serviceNodeList.getLength(); j++) {
                Service service = null;
                Node serviceNode = serviceNodeList.item(j);
                service = getService(serviceNode);

                if (service.isGenerate() && service.getOperations().length == 0) {
                    throw new ServiceConfigException("No operations defined");
                }

                config.addService(service);

            } // end initial for loop

            if (config.getServices().length == 0) {
                throw new ServiceConfigException(
                        "service-config has no child nodes");
            }
            return config;
        }
        catch (DOMException e) {
            throw new ServiceConfigException("XML Format error");
        }
    }

    /**
     * Returns Service object which holds details of service,class, package name

import org.apache.logging.log4j.LogManager;     * attributes and colection of opeations
     * 
     * @param Node Service node
     * @return Service
     * @throws RPLogicalException
     * @throws ServiceConfigException
     */
    private Service getService(Node serviceNode) throws ServiceConfigException {
        String serviceName = null;
        String className = null;
        String packageName = null;
        boolean generate = true;
        NamedNodeMap attribs = serviceNode.getAttributes();

        try {
            if (attribs == null || attribs.getLength() == 0) {
                throw new ServiceConfigException(
                        "Attributes not present for service");
            }

            for (int k = 0; k < attribs.getLength(); k++) {
                Node current = attribs.item(k);

                if (current != null && current.getNodeName().equals("name")) {
                    serviceName = current.getNodeValue();
                }
                else if (current != null
                        && current.getNodeName().equals("class")) {
                    className = current.getNodeValue();
                }
                else if (current != null
                        && current.getNodeName().equals("package")) {
                    packageName = current.getNodeValue();
                }
                else if (current != null
                        && current.getNodeName().equals("generate")) {
                    String tmp = current.getNodeValue();
                    generate = !tmp.equalsIgnoreCase("false");
                }

            }

            if (serviceName == null || serviceName.trim().equals("")) {
                throw new ServiceConfigException(
                        " Value not present  for name attribute of service");
            }

            Service service = new Service();
            service.setClassName(className);
            service.setName(serviceName);
            service.setPackageName(packageName);
            service.setGenerate(generate);

            NodeList oprFNList = serviceNode.getChildNodes();

            for (int i = 0; i < oprFNList.getLength(); i++) {
                String methodName = null;
                Operation oprObj = new Operation();
                if (oprFNList.item(i).getNodeName().equals("operation")) {
                    NamedNodeMap attributes = oprFNList.item(i).getAttributes();
                    if (attributes != null && attributes.getLength() == 0) {
                        throw new ServiceConfigException(
                                "Attributes not present for operation");
                    }
                    else if (attributes != null) {

                        for (int k = 0; k < attributes.getLength(); k++) {
                            Node current = attributes.item(k);
                            if (current != null
                                    && current.getNodeName().equals("name")) {
                                methodName = current.getNodeValue();
                            }

                        }

                        if (methodName == null || methodName.trim().equals("")) {
                            throw new ServiceConfigException(
                                    " Value not present  for name attribute of operation ");
                        }

                        oprObj.setName(methodName);
                    }

                    Node oprList = oprFNList.item(i);
                    int type = oprList.getNodeType();

                    // check if element
                    if (type == Node.ELEMENT_NODE) {
                        oprObj = getOperation(oprList.getChildNodes(), oprObj);

                        service.addOperation(oprObj);
                    }
                }
            }
            
            return service;
        }
        catch (DOMException e) {         
            throw new ServiceConfigException("XML Format error");
        }
    }

    /**
     * Constructs Operation object which will be having command,multirow,result
     * class name, collection of input arguments and collection of result fields
     * 
     * @param NodeList  operation nodes
     * @param Operation
     * @return Operation   
     * @throws RPLogicalException
     * @throws ServiceConfigException
     */
    private Operation getOperation(NodeList oprNodes, Operation oprObj)
            throws ServiceConfigException {
        String command = null;
        String multiRow = null;
        String resultClassName = null;
        boolean isMultiRow = true;

        try {

            for (int i = 0; i < oprNodes.getLength(); i++) {
                if (oprNodes.item(i).getNodeName().equals("input")) {
                    Node inpList = oprNodes.item(i);
                    getInputArguments(inpList.getChildNodes(), oprObj);
                }
                else if (oprNodes.item(i).getNodeName().equals("command")) {
                    Node cmdNode = oprNodes.item(i).getFirstChild();
                    if (cmdNode != null) {
                        command = cmdNode.getNodeValue();
                    }

                    if (command == null || command.trim().equals("")) {
                        throw new ServiceConfigException(
                                " Value not present  for command ");
                    }

                    oprObj.setCommand(command);
                }
                else if (oprNodes.item(i).getNodeName().equals("output")) {
                    NamedNodeMap attribs = oprNodes.item(i).getAttributes();
                    if (attribs != null && attribs.getLength() == 0) {
                        throw new ServiceConfigException(
                                "Attributes not present for output");
                    }
                    else if (attribs != null) {

                        for (int h = 0; h < attribs.getLength(); h++) {
                            Node current = attribs.item(h);
                            if (current != null
                                    && current.getNodeName().equals("multirow")) {
                                multiRow = current.getNodeValue();
                                if (multiRow.equals("true")) {
                                    isMultiRow = true;
                                }
                                else {
                                    isMultiRow = false;

                                }

                            }
                            else if (current != null
                                    && current.getNodeName().equals("class")) {
                                resultClassName = current.getNodeValue();

                            }

                        }

                        oprObj.setMultiRowResult(isMultiRow);
                        oprObj.setResultClassName(resultClassName);
                    }
                    Node outList = oprNodes.item(i);
                    getOutputFields(outList.getChildNodes(), oprObj);
                }

            }

        }
        catch (DOMException e) {            
            throw new ServiceConfigException("XML Format error");
        }
        return oprObj;

    }

    /**
     * Returns collection of operation (Input) arguments each object is holds
     * name and type attribute info
     * 
     * @param NodeList input argument nodes
     * @param Operation Input Arguments added to this Operation
     * @throws RPLogicalException
     * @throws ServiceConfigException
     */
    private void getInputArguments(NodeList inputArgNodes, Operation oprObj)
            throws ServiceConfigException {
        String name = null;
        String type = null;
        String nullable = null;
        String column = null;
        boolean isNullable = false;

        try {

            for (int i = 0; i < inputArgNodes.getLength(); i++) {
                if (inputArgNodes.item(i).getNodeName().equals("argument")) {
                    NamedNodeMap attributes = inputArgNodes.item(i)
                            .getAttributes();
                    if (attributes != null && attributes.getLength() == 0) {
                        throw new ServiceConfigException(
                                "Attributes not present for input argument");
                    }
                    else if (attributes != null) {
                        OperationArgument oprArgObj = null;
                        for (int j = 0; j < attributes.getLength(); j++) {
                            Node current = attributes.item(j);
                            if (current.getNodeName().equals("name")) {
                                name = current.getNodeValue();
                            }
                            else if (current.getNodeName().equals("type")) {
                                type = current.getNodeValue();
                            }
                            else if (current.getNodeName().equals("nullable")) {
                                nullable = current.getNodeValue();
                            }
                            else if (current.getNodeName().equals("column")) {
                                column = current.getNodeValue();
                            }

                        }

                        if (name == null || name.trim().equals("")) {
                            throw new ServiceConfigException(
                                    " Value not present  for name attribute of input argument");
                        }

                        if (type == null || type.trim().equals("")) {
                            throw new ServiceConfigException(
                                    " Value not present for type attribute of input argumen");
                        }

                        oprArgObj = new OperationArgument();
                        oprArgObj.setName(name);
                        oprArgObj.setType(type);
                        oprArgObj.setColumn(column);
                        if (nullable != null && nullable.equals("true")) {
                            isNullable = true;
                        }

                        oprArgObj.setNullable(isNullable);

                        oprObj.addOperationArgument(oprArgObj);

                    }

                }
            }

        }
        catch (DOMException e) {
            throw new ServiceConfigException("XML Format error");
        }

    }

    /**
     * Returns collection of Result Fields each object is holds name,type and
     * nullable attribute info
     * 
     * @param NodeList  Output Field argument nodes
     * @param Operation Output Fields added to this object
     * @throws ServiceConfigException
     */
    private void getOutputFields(NodeList outputFieldNodes, Operation oprObj)
            throws ServiceConfigException {

        try {
            for (int i = 0; i < outputFieldNodes.getLength(); i++) {
                if (outputFieldNodes.item(i).getNodeName().equals("field")) {
                    NamedNodeMap attributes = outputFieldNodes.item(i)
                            .getAttributes();
                    if (attributes != null && attributes.getLength() == 0) {
                        throw new ServiceConfigException(
                                "Attributes not present for output field");
                    }
                    else if (attributes != null) {
                        ResultField resultField = null;

                        String name = null;
                        String type = null;
                        String column = null;
                        boolean nullable = false;
                        String nullableStr = null;

                        for (int j = 0; j < attributes.getLength(); j++) {
                            Node current = attributes.item(j);
                            if (current.getNodeName().equals("name")) {
                                name = current.getNodeValue();
                            }
                            else if (current.getNodeName().equals("column")) {
                                column = current.getNodeValue();
                            }
                            else if (current.getNodeName().equals("type")) {
                                type = current.getNodeValue();
                            }
                            else if (current.getNodeName().equals("nullable")) {
                                nullableStr = current.getNodeValue();
                                if (nullableStr != null) {
                                    if (nullableStr.equals("true")) {
                                        nullable = true;
                                    }
                                    else {
                                        nullable = false;
                                    }

                                }
                            }
                        }

                        if (name == null || name.trim().equals("")) {
                            throw new ServiceConfigException(
                                    " Value not present  for name attribute of output field");
                        }

                        if (type == null || type.trim().equals("")) {
                            throw new ServiceConfigException(
                                    " Value not present for type attribute of output field");
                        }

                        if (nullableStr == null
                                || nullableStr.trim().equals("")) {
                            throw new ServiceConfigException(
                                    " Value not present for nullable attribute of output field");
                        }

                        resultField = new ResultField();
                        resultField.setName(name);
                        resultField.setType(type);
                        resultField.setColumn(column);
                        resultField.setNullable(nullable);

                        oprObj.addResultField(resultField);
                    }

                }

            }
        }
        catch (DOMException e) {
            throw new ServiceConfigException("XML Format error");
        }

    }

    /**
     * Returns Document Object which is built after parsing config xml
     * 
     * @param File  Service Config xml file
     * @return Document Object     
     * @throws ServiceConfigException
     * 
     */
    private Document readConfig() throws ServiceConfigException {
        Document doc = null;

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(true);
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            EntityResolver serviceResolver = new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException ,IOException {
                    if (systemId.endsWith(CONFIG_DTD_SYSTEMID)) {
                        try {
                            InputStream in = getClass().getResourceAsStream("resources/service-config.dtd");
                            return new InputSource(in);
                        }
                        catch (Exception e) {
                            // do nothing
                        }
                    }
                    return null;
                };
            };
            db.setEntityResolver(serviceResolver);
            
            ErrorHandler errorHandler = new DefaultHandler() {
                public void error(org.xml.sax.SAXParseException e)
                        throws SAXException {
                    throw e;
                }
            };
            
            db.setErrorHandler(errorHandler);
            
            doc = db.parse(_in);
        }
        catch (ParserConfigurationException e) {
            throw new ServiceConfigException("XML Parser error", e);
        }
        catch (SAXException e) {
            throw new ServiceConfigException("XML Format error", e);
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            throw new ServiceConfigException("Service Config File IO error", e);
        }
        finally {
            try {
                _in.close();
            }
            catch (IOException e) {
                // Ignore
            }
        }

        return doc;
    }

    // -----------------------------
    // implementation:
    // -----------------------------

    private InputStream _in = null;
}
