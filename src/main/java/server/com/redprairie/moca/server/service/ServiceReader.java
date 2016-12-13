/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.server.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.registry.RegistryReader;
import com.redprairie.util.StringReplacer;
import com.redprairie.util.VarStringReplacer;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author mlange
 */
public class ServiceReader {
    
    private ServiceReader() {
    }
    
    
    public static Service find(String serviceName) throws ServiceReaderException {      
        
        // Validate our arguments.
        if (serviceName == null || serviceName.isEmpty())
            throw new IllegalArgumentException("The service name cannot be null or empty");   
        
        // Get the environment name from the service name.
        String environmentName = ServiceTools.getEnvironmentName(serviceName);
        
        // Get the environment and values from the registry that we need.
        String[] proddirs = null;  
        Map<String, String> environment = null;
        try {
            environment = ServiceTools.getEnvironment(environmentName);               
            String registryPathname = environment.get("MOCA_REGISTRY");    
            RegistryReader registryReader = new RegistryReader(new File(registryPathname));
            String value = registryReader.getConfigurationElement(MocaRegistry.REGKEY_SERVER_PROD_DIRS, true);
            proddirs = value.split(File.pathSeparator);
        }
        catch (SystemConfigurationException e) {
            throw new ServiceReaderException(e.getMessage(), e);
        }
        catch (ServiceManagerException e) {
            throw new ServiceReaderException(e.getMessage(), e);
        }
        
        return find(serviceName, proddirs);
    }
    
    public static Service find(String serviceName, String[] proddirs) throws ServiceReaderException {
        
        // Validate our arguments.
        if (serviceName == null || serviceName.isEmpty())
            throw new IllegalArgumentException("The service name cannot be null or empty");          
        if (proddirs == null || proddirs.length == 0)
            throw new IllegalArgumentException("The product directory list cannot be null or empty");  

        // Get the application name from the service name.
        String applicationName = ServiceTools.getApplicationName(serviceName);
        
        Service service = null;
        
        /* 
         * Now loop through all the configuration directories so we can find
         * which ones contain the xml and add it to our list.  We do this in the reverse
         * order to allow the same service to be defined at multiple levels where higher
         * levels append to and/or override lower levels. 
         */ 
        for (int i = proddirs.length; i > 0; i--) {          
            String proddir = proddirs[i-1] + File.separator + "data" + File.separator + "services.xml";
            
            // Find the service elements with a matching application name.
            Node[] nodes;
            try {    
                Document document = parseServicesFile(proddir);
                nodes = getNodesFromXPath(document, SERVICE_XPATH); 
            }
            catch (FileNotFoundException e) {
                // Ignore
                continue;
            }
            catch (ServiceReaderException e) {
                throw new ServiceReaderException(e.getMessage(), e);
            }
            
            // Find an entry associated with this service.
            for (Node node : nodes) {            
                String value = getChildStringValue(node, APPLICATION_TAG);
                if (value == null) {
                    throw new ServiceReaderException("Invalid or missing <" + APPLICATION_TAG + "> element in " + proddir);
                }
                else if (value.equalsIgnoreCase(applicationName)) {
                    if (service == null)
                        service = getService(serviceName, node);
                    else
                        updateService(service, node);
                }
            }
        }      
        
        if (service == null)     
            throw new ServiceReaderException("Could not find definition for service");
        
        return service;
    }
    
    private static Service getService(String application, Node parent) throws ServiceReaderException {
        
        // Display name (required)
        String displayName = getChildStringValue(parent, DISPLAY_NAME_TAG);
        if (displayName == null || displayName.isEmpty()) 
            throw new ServiceReaderException("Invalid or missing <" + DISPLAY_NAME_TAG + "> element");
        
        // Description (required)
        String description = getChildStringValue(parent, DESCRIPTION_TAG);
        if (description == null || description.isEmpty()) 
            throw new ServiceReaderException("Invalid or missing <" + DESCRIPTION_TAG + "> element");
        
        // Start information (required)
        StartStopInfo startInfo = getStartStopInfo(parent, START_TAG);
        if (startInfo == null) 
            throw new ServiceReaderException("Invalid or missing <" + START_TAG + "> element");
        
        // Stop information (required)
        StartStopInfo stopInfo = getStartStopInfo(parent, STOP_TAG);
        if (stopInfo == null) 
            throw new ServiceReaderException("Invalid or missing <" + STOP_TAG + "> element");
        
        // Depends on (optional)
        String[] dependsOn = getDependsOn(parent, DEPENDS_ON_TAG);
        
        return new Service(application, displayName, description, startInfo, stopInfo, dependsOn);
    }
    
    private static void updateService(Service service, Node parent) throws ServiceReaderException {
        
        // Display name (required)
        String displayName = getChildStringValue(parent, DISPLAY_NAME_TAG);
        if (displayName != null && !displayName.isEmpty()) 
            service.setDisplayName(displayName);
        
        // Description (required)
        String description = getChildStringValue(parent, DESCRIPTION_TAG);
        if (description != null && !description.isEmpty()) 
            service.setDescription(description);
        
        // Start information (required)
        StartStopInfo startInfo = getStartStopInfo(parent, START_TAG);
        if (startInfo != null) 
            service.setStartInfo(startInfo);
        
        // Stop information (required)
        StartStopInfo stopInfo = getStartStopInfo(parent, STOP_TAG);
        if (stopInfo != null) 
            service.setStopInfo(stopInfo);
        
        // Depends on (optional)
        String[] dependsOn = getDependsOn(parent, DEPENDS_ON_TAG);
        if (dependsOn.length > 0)
            service.setDependsOn(dependsOn);
    }
    
    private static StartStopInfo getStartStopInfo(Node parent, String tag) throws ServiceReaderException {
        
        Node[] nodes = getNodesFromXPath(parent, tag);
        if (nodes.length == 0)
            throw new ServiceReaderException("Missing <" + tag + "> element");
        else if (nodes.length > 1)
            throw new ServiceReaderException("More than one <" + tag + "> element was found");
        
        Node match = nodes[0];
        
        StartStopInfo startStopInfo = null; 
        
        // Mode
        String mode = getChildStringValue(match, MODE_TAG);        
        if (mode == null || mode.isEmpty())
            throw new ServiceReaderException("Invalid or missing <" + MODE_TAG + "> element");
        
        // Everything else
        if (mode.equalsIgnoreCase("exe"))
            startStopInfo = getExeModeStartStopInfo(match); 
        else if (mode.equalsIgnoreCase("java"))
            startStopInfo = getJavaModeStartStopInfo(match); 
        else if (mode.equalsIgnoreCase("java32"))
            startStopInfo = getJava32ModeStartStopInfo(match); 
        else
            throw new ServiceReaderException("Unknown <" + MODE_TAG + "> value: " + mode);
        
        // Debug
        Node[] debug = getNodesFromXPath(match, DEBUG_TAG);
        if (debug.length > 0)
            startStopInfo.setDebug();
        
        return startStopInfo;
    }
    
    private static StartStopInfo getExeModeStartStopInfo(Node parent) throws ServiceReaderException {

        // Command
        String command = getChildStringValue(parent, COMMAND_TAG);        
        if (command == null || command.isEmpty())
            throw new ServiceReaderException("Invalid <" + COMMAND_TAG + "> element");
        
        return new ExeStartStopInfo(command);
    }
    
    private static StartStopInfo getJavaModeStartStopInfo(Node parent) throws ServiceReaderException {
        
        // Class (required)
        String clazz = getChildStringValue(parent, CLASS_TAG);        
        if (clazz == null || clazz.isEmpty())
            throw new ServiceReaderException("Invalid or missing <" + CLASS_TAG + "> element");
        
        // Arguments (optional)
        String args[] = getChildStringValues(parent, ARGUMENT_TAG);
        
        return new JavaStartStopInfo(clazz, args);
    }
    
    private static StartStopInfo getJava32ModeStartStopInfo(Node parent) throws ServiceReaderException {
        
        // Class (required)
        String clazz = getChildStringValue(parent, CLASS_TAG);        
        if (clazz == null || clazz.isEmpty())
            throw new ServiceReaderException("Invalid or missing <" + CLASS_TAG + "> element");
        
        // Arguments (optional)
        String args[] = getChildStringValues(parent, ARGUMENT_TAG);
        
        return new Java32StartStopInfo(clazz, args);
    }
    
    private static String[] getDependsOn(Node parent, String tag) throws ServiceReaderException {
        List<String> list = new ArrayList<String>();
        
        Node[] nodes = getNodesFromXPath(parent, tag);
        if (nodes.length == 0)
            return list.toArray(new String[list.size()]);
        else if (nodes.length > 1)
            throw new ServiceReaderException("More than one <" + tag + "> element was found");
        
        Node match = nodes[0];
        
        String[] applications = getChildStringValues(match, DEPENDS_ON_APPLICATION_TAG);  
        for (String application : applications)
            list.add(application + "." + System.getenv("MOCA_ENVNAME"));
        
        String[] services = getChildStringValues(match, DEPENDS_ON_SERVICE_TAG);  
        for (String service : services)
            list.add(service);       
                
        return list.toArray(new String[list.size()]);              
    }
    
    private static String getChildStringValue(Node parent, String tag) throws ServiceReaderException {
        
        Node[] nodes = getNodesFromXPath(parent, tag);
        if (nodes.length == 0)
            return null;  
        else if (nodes.length > 1)
            throw new ServiceReaderException("More than one <" + tag + "> element was found");   
        
        return expandEnvironmentVariables(nodes[0].getTextContent());
    }
    
    private static String[] getChildStringValues(Node parent, String tag) throws ServiceReaderException {
        List<String> list = new ArrayList<String>();
        
        Node[] nodes = getNodesFromXPath(parent, tag);        
        for (Node node : nodes) {              
            String value = node.getTextContent();
            if (value != null && !value.isEmpty()) 
                list.add(expandEnvironmentVariables(value));
        }
        
        return list.toArray(new String[list.size()]);
    }
    
    private static Node[] getNodesFromXPath(Node parent, String path) throws ServiceReaderException {      
        
        XPathFactory factory = getXPathFactory();
        XPath xpath = factory.newXPath();

        NodeList nodeList = null;       
        try {
            XPathExpression expr = xpath.compile(path);
            nodeList = (NodeList) expr.evaluate(parent, XPathConstants.NODESET);
        }
        catch (XPathExpressionException e) {
            throw new ServiceReaderException("Could not evaluation xpath expression", e);
        }
        
        List<Node> list = new ArrayList<Node>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            list.add(nodeList.item(i));
        }

        return list.toArray(new Node[list.size()]); 
    }   
    
    private static Document parseServicesFile(String pathname) throws ServiceReaderException, FileNotFoundException {
        
        // Make sure the file exists.
        File file = new File(pathname);
        if (!file.exists())
            throw new FileNotFoundException();
        
        Document document = null;
        try {            
            DocumentBuilder builder = getDocumentBuilder();
            document = builder.parse(file);
            document.setXmlStandalone(true);
        }
        catch (IOException e) {
            throw new ServiceReaderException("Could not open services file: " + file.getPath(), e);
        }
        catch (SAXException e) {
            throw new ServiceReaderException("Could not parse services file: " + file.getPath(), e);
        }
        
        return document;
    }
    
    private static DocumentBuilder getDocumentBuilder() {
        
        synchronized (ServiceReader.class) {
            if (_documentBuilderFactory == null) {
                _documentBuilderFactory = DocumentBuilderFactory.newInstance();
                _documentBuilderFactory.setCoalescing(false);
                _documentBuilderFactory.setExpandEntityReferences(false);
                _documentBuilderFactory.setIgnoringComments(false);
                _documentBuilderFactory.setIgnoringElementContentWhitespace(true);
                _documentBuilderFactory.setNamespaceAware(false);
                _documentBuilderFactory.setValidating(false);
            }
        }
        
        try {
            return _documentBuilderFactory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            throw new IllegalArgumentException("Unexpected parser configuration exception", e);
        }
    }
    
    private static XPathFactory getXPathFactory() {
  
        synchronized (ServiceReader.class) {
            if (_xpathFactory == null)
                _xpathFactory = XPathFactory.newInstance();
        }
        
        return _xpathFactory;
    }
    
    private static String expandEnvironmentVariables(String value) {       
        if (value == null || value.isEmpty())
            return value;
        
        return new VarStringReplacer(new StringReplacer.ReplacementStrategy() {
            @Override
            public String lookup(String name) {
                return System.getenv(name);
            }
        }).translate(value);
    }
    
    private static DocumentBuilderFactory _documentBuilderFactory;
    private static XPathFactory _xpathFactory;
    
    // XPaths
    public static final String SERVICE_XPATH = "/services/service";
    
    // Tag names
    public static final String APPLICATION_TAG = "application";
    public static final String DESCRIPTION_TAG = "description";
    public static final String DISPLAY_NAME_TAG = "displayName";
    public static final String START_TAG = "start";
    public static final String STOP_TAG = "stop";
    public static final String DEBUG_TAG = "debug";
    public static final String MODE_TAG = "mode";
    public static final String COMMAND_TAG = "command";
    public static final String CLASS_TAG = "class";
    public static final String ARGUMENT_TAG = "argument";
    public static final String DEPENDS_ON_TAG = "dependsOn";
    public static final String DEPENDS_ON_APPLICATION_TAG = "application";
    public static final String DEPENDS_ON_SERVICE_TAG = "service";
}