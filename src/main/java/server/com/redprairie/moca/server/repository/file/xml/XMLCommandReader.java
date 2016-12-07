/*
 *  $URL$
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

package com.redprairie.moca.server.repository.file.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.server.SecurityLevel;
import com.redprairie.moca.server.parse.MocaParseException;
import com.redprairie.moca.server.parse.MocaParser;
import com.redprairie.moca.server.repository.ArgType;
import com.redprairie.moca.server.repository.ArgumentInfo;
import com.redprairie.moca.server.repository.CFunctionCommand;
import com.redprairie.moca.server.repository.COMCommand;
import com.redprairie.moca.server.repository.Command;
import com.redprairie.moca.server.repository.CommandType;
import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.server.repository.JavaCommand;
import com.redprairie.moca.server.repository.LocalSyntaxCommand;
import com.redprairie.moca.server.repository.TransactionType;
import com.redprairie.moca.server.repository.docs.CommandDocumentation;
import com.redprairie.moca.server.repository.file.CommandReader;
import com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents;
import com.redprairie.moca.server.repository.file.RepositoryReadException;

/**
 * XMLCommandReader
 * Class with static methods to read and write Commands from
 * files. The files hold Command data in XML format.
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
 * @author dinksett
 * @version $Revision$
 */

public class XMLCommandReader implements CommandReader{
    
    public static final String COMMAND_TAG = "command";
    public static final Map<String, ArgType> ARGTYPES = new HashMap<String, ArgType>();
    static {
        ARGTYPES.put("flag", ArgType.FLAG);
        ARGTYPES.put("boolean", ArgType.FLAG);
        ARGTYPES.put("integer", ArgType.INTEGER);
        ARGTYPES.put("int", ArgType.INTEGER);
        ARGTYPES.put("float", ArgType.FLOAT);
        ARGTYPES.put("double", ArgType.FLOAT);
        ARGTYPES.put("binary", ArgType.BINARY);
        ARGTYPES.put("string", ArgType.STRING);
        ARGTYPES.put("char", ArgType.STRING);
        ARGTYPES.put("generic", ArgType.POINTER);
        ARGTYPES.put("pointer", ArgType.POINTER);
        ARGTYPES.put("results", ArgType.RESULTS);
        ARGTYPES.put("object", ArgType.OBJECT);
    }
    
    public XMLCommandReader() {
        _events = null;
    }
    
    public XMLCommandReader(RepositoryReaderEvents events) {
        _events = events;
    }

    public Command read(File xmlFile, ComponentLevel level) throws RepositoryReadException  {
        try {
            Element node = XMLUtils.readNodeFromFile(xmlFile);
            return read(node, level);
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            throw new RepositoryReadException(
                "Unable to read Command: " + e, e);
        }
        catch (SAXException e) {
            throw new RepositoryReadException(
                "XML exception parsing Command: " + e, e);
        }
    }
    
    public Command read(InputStream xmlStream, ComponentLevel level) throws RepositoryReadException {
        try {
            Element node = XMLUtils.readNodeFromStream(xmlStream);
            return read(node, level);
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            throw new RepositoryReadException(
                "Unable to read Command: " + e, e);
        }
        catch (SAXException e) {
            throw new RepositoryReadException(
                "XML exception parsing Command: " + e, e);
        }
    }

    /**
     * Build a Command object from a DOM node.  This scans through the node tree,
     * building up the Level object as it goes.  The object returned from this
     * method is guaranteed to be a valid Level object.
     * @param node
     * @return
     * @throws RepositoryReadException
     */
    public Command read(Element node, ComponentLevel level) throws RepositoryReadException {
        if (!node.getNodeName().trim().equalsIgnoreCase(COMMAND_TAG)) {
            throw new RepositoryReadException("Unable to find <" + COMMAND_TAG + "> tag");
        }
        
        String type = XMLUtils.readSingleElementValue(node, "type");
        String name = XMLUtils.readSingleElementValue(node, "name");
        
        Command command;
        if (type.equalsIgnoreCase("C Function")) {
            if (level.getLibrary() == null) {
                throw new RepositoryReadException("C component with no library");
            }
            command = readCFunctionCommand(name, node, level);
        }
        else if (type.equalsIgnoreCase("Simple C Function")) {
            if (level.getLibrary() == null) {
                throw new RepositoryReadException("Simple C component with no library");
            }
            command = readSimpleCFunctionCommand(name, node, level);
        }
        else if (type.equalsIgnoreCase("Java Method")) {
            if (level.getPackage() == null) {
                throw new RepositoryReadException("Java component with no package");
            }
            command = readJavaCommand(name, node, level);
        }
        else if (type.equalsIgnoreCase("COM Method")) {
            if (level.getProgid() == null) {
                throw new RepositoryReadException("COM component with no PROGID");
            }
            command = readCOMCommand(name, node, level);
        }
        else if (type.equalsIgnoreCase("Local Syntax")) {
            command = readLocalSyntaxCommand(name, node, level);
        }
        else {
            throw new RepositoryReadException("Unrecognized Command Type: " + type);
        }
        
        command.setDescription(XMLUtils.readSingleElementValue(node, "description"));

        SecurityLevel securityLevel = null;
        
        String securityText = XMLUtils.readSingleElementValue(node, "security-level");
        if (securityText != null) {
            try {
                securityLevel = SecurityLevel.valueOf(securityText.trim().toUpperCase());
            }
            catch (IllegalArgumentException e) {
                throw new RepositoryReadException(
                    "Unrecognized security level: " + securityText + ". " +
                    "Value must be one of " + Arrays.asList(SecurityLevel.values()));
            }
        }

        // It's possible to not have a security Level text, in which case, we
        // just fall back to the old "insecure" flag, which only allows
        // two levels: OPEN and PUBLIC
        if (securityLevel == null) {
            String tmp = XMLUtils.readSingleElementValue(node, "insecure");
            boolean isInsecure = tmp != null && (tmp.equalsIgnoreCase("yes") || tmp.equalsIgnoreCase("true") || tmp.equals("1"));
            
            if (isInsecure) {
                securityLevel = SecurityLevel.OPEN;
            }
            else {
                securityLevel = SecurityLevel.PUBLIC;
            }
        }
        
        command.setSecurityLevel(securityLevel);

        command.setTransactionType(TransactionType.REQUIRED);
        String tmp = XMLUtils.readSingleElementValue(node, "transaction");
        if (tmp != null) {
            if (tmp.equalsIgnoreCase("new") || tmp.equalsIgnoreCase("requiresnew")) {
                command.setTransactionType(TransactionType.REQUIRES_NEW);
            }
            else if (tmp.equalsIgnoreCase("none")) {
                command.setTransactionType(TransactionType.NONE);
            }
        }
        
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
            
            ArgType argType = ARGTYPES.get(typeString.toLowerCase());
            if (argType == null && command.getType() != CommandType.LOCAL_SYNTAX) {
                throw new RepositoryReadException("Unknown argument type: " + typeString);
            }

            boolean required = requiredString.equalsIgnoreCase("yes") || requiredString.equalsIgnoreCase("true") || requiredString.equals("1");
            ArgumentInfo arg = new ArgumentInfo(argName, argAlias, argType, argValue, required);
            arg.setComment(comment);
            
            command.addArgument(arg);
        }
        
        // Documentation
        NodeList docNodes = node.getElementsByTagName("documentation");
        
        if (docNodes != null && docNodes.getLength() == 1) {
            CommandDocumentation doc = new CommandDocumentation();
            Element docNode = (Element)docNodes.item(0);
            
            try {
                doc.setReturnRows(XMLUtils.readSingleElementValue(docNode, "retrows"));
                doc.setRemarks(XMLUtils.readSingleElementValue(docNode, "remarks"));
            }
            catch (RepositoryReadException e) {
                if (_events != null)
                    _events.reportWarning("Documentation error: " + e);
            }
            
            NodeList tempList = docNode.getElementsByTagName("example");
            if (tempList != null) {
                for (int i = 0; i < tempList.getLength(); i++) {
                    Element example = (Element) tempList.item(i);
                    doc.addExample(example.getTextContent());
                }
            }

            tempList = docNode.getElementsByTagName("seealso");
            if (tempList != null) {
                for (int i = 0; i < tempList.getLength(); i++) {
                    Element reference = (Element) tempList.item(i);
                    doc.addSeeAlso(reference.getAttribute("cref"));
                }
            }
            
            tempList = docNode.getElementsByTagName("called-by");
            if (tempList != null) {
                for (int i = 0; i < tempList.getLength(); i++) {
                    Element reference = (Element) tempList.item(i);
                    doc.addCalledby(reference.getAttribute("cref"));
                }
            }

            tempList = docNode.getElementsByTagName("retcol");
            if (tempList != null) {
                for (int i = 0; i < tempList.getLength(); i++) {
                    Element retcol = (Element) tempList.item(i);
                    String colName = retcol.getAttribute("name");
                    String colType = retcol.getAttribute("type");
                    String desc = retcol.getTextContent();
                    doc.addColumn(colName, colType, desc);
                }
            }
            
            tempList = docNode.getElementsByTagName("exception");
            if (tempList != null) {
                for (int i = 0; i < tempList.getLength(); i++) {
                    Element exception = (Element) tempList.item(i);
                    String value = exception.getAttribute("value");
                    String desc = exception.getTextContent();
                    doc.addError(value, desc);
                }
            }
            
            command.setDocumentation(doc);
        }
        
        
        return command;
    }
    
    //
    // Implementation
    //
    
    protected Command readCFunctionCommand(String name, Element node, ComponentLevel level)
            throws RepositoryReadException {
        CFunctionCommand result = new CFunctionCommand(name, level, CommandType.C_FUNCTION);

        String function = XMLUtils.readSingleElementValue(node, "function");
        
        if (function.length() == 0) {
            throw new RepositoryReadException("Missing function from C command");
        }
        
        result.setFunction(function);
        
        return result;
    }
    
    protected Command readSimpleCFunctionCommand(String name, Element node, ComponentLevel level)
            throws RepositoryReadException {
        CFunctionCommand result = new CFunctionCommand(name, level, CommandType.SIMPLE_C_FUNCTION);

        String function = XMLUtils.readSingleElementValue(node, "function");
        
        if (function.length() == 0) {
            throw new RepositoryReadException("Missing function for C command");
        }
        
        result.setFunction(function);
        
        return result;
    }
    
    protected Command readJavaCommand(String name, Element node, ComponentLevel level)
            throws RepositoryReadException {
        JavaCommand result = new JavaCommand(name, level);
        String classname = XMLUtils.readSingleElementValue(node, "class");
        String method = XMLUtils.readSingleElementValue(node, "method");
        
        if (classname.length() == 0) {
            throw new RepositoryReadException("Missing class for Java command");
        }

        if (method.length() == 0) {
            throw new RepositoryReadException("Missing method for Java command");
        }
        
        result.setClassName(classname);
        result.setMethod(method);
        
        return result;
    }

    protected Command readCOMCommand(String name, Element node, ComponentLevel level)
            throws RepositoryReadException {
        COMCommand result = new COMCommand(name, level);
        String method = XMLUtils.readSingleElementValue(node, "method");
        
        if (method.length() == 0) {
            throw new RepositoryReadException("Missing method for COM command");
        }
        
        result.setMethod(method);

        return result;
    }

    protected Command readLocalSyntaxCommand(String name, Element node, ComponentLevel level)
            throws RepositoryReadException {
        LocalSyntaxCommand result = new LocalSyntaxCommand(name, level);
        String syntax = XMLUtils.readCDataValue(node, "local-syntax");

        if (syntax == null || syntax.length() == 0) {
            throw new RepositoryReadException("Missing syntax for Local Syntax command");
        }
        
        try {
            new MocaParser(syntax).parse();
        }
        catch (MocaParseException e) {
            throw new RepositoryReadException("Local Syntax parse error: " + e, e);
        }


        result.setSyntax(syntax);

        return result;
    }
    
    private RepositoryReaderEvents _events;
}
