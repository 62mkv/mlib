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

package com.redprairie.moca.client;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaOperator;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.util.DateUtils;
import com.redprairie.util.Base64;

/**
 * A class to decode an XML request into its component parts.
 * 
 * <b><pre>
 * Copyright (c) 2006 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class XMLRequestDecoder {
    
    public XMLRequestDecoder(InputStream in) throws ProtocolException {
        try {
            _xml = _streamFactory.createXMLStreamReader(in, "UTF-8");
        }
        catch (XMLStreamException e) {
            throw new ProtocolException("unable to create XML reader: ", e);
        }
        catch (FactoryConfigurationError e) {
            throw new ProtocolException("unable to create XML reader: ", e);
        }
    }
    
    public void decode() throws ProtocolException {
        try {
            Map<String, ElementProcessor> processors = new HashMap<String, ElementProcessor>();
            
            processors.put("query", new QueryProcessor());
            processors.put("args", new ArgProcessor());
            processors.put("context", new ArgProcessor());
            processors.put("environment", new EnvironmentProcessor());
            processors.put("session", new SessionProcessor());
            
            int tag = _xml.nextTag();
            if (tag != XMLStreamConstants.START_ELEMENT) {
                throw new ProtocolException("No Start Element");
            }
            
            if (!_xml.getLocalName().equals("moca-request")) {
                throw new ProtocolException("Expecting moca-request tag");
            }

            String autoCommitAttr = _xml.getAttributeValue(null, "autocommit");
            if (autoCommitAttr != null) {
                _autoCommit = Boolean.valueOf(autoCommitAttr);
            }
            
            String remoteAttr = _xml.getAttributeValue(null, "remote");
            if (remoteAttr != null) {
                _remote = Boolean.valueOf(remoteAttr);
            }
            
            while (_xml.nextTag() == XMLEvent.START_ELEMENT) {
                ElementProcessor processor = processors.get(_xml.getLocalName());
                if (processor != null) {
                    processor.process();
                }
                else {
                    throw new ProtocolException("Invalid tag encountered: " + _xml.getName());
                }
            }
        }
        catch (XMLStreamException e) {
            throw new ProtocolException("Error Interpreting XML Results", e);
        }

    }
    
    /**
     * Returns the session ID seen when parsing the response.  This may return <code>null</code>
     * if no session ID was returned from the <code>decodeResponse</code> method, or if the
     * <code>decodeResponse</code> method was never called.
     * 
     * @return Returns the sessionID.
     */
    public String getSessionId() {
        return _sessionId;
    }
    
    public String getQuery() {
        return _query;
    }

    public Map<String, String> getEnv() {
        return _env;
    }

    public List<MocaArgument> getContext() {
        return _context;
    }

    public List<MocaArgument> getArgs() {
        return _args;
    }

    public boolean isAutoCommit() {
        return _autoCommit;
    }

    public boolean isRemote() {
        return _remote;
    }


    private abstract class ElementProcessor {
        
        public abstract void process() throws ProtocolException, XMLStreamException;
    }
    
    private class SessionProcessor extends ElementProcessor {
        // @see com.redprairie.moca.client.XMLRequestDecoder.ElementProcessor#process()
        @Override
        public void process() throws ProtocolException, XMLStreamException {
            _sessionId = _xml.getAttributeValue(null, "id");
            if (XMLEvent.END_ELEMENT != _xml.nextTag()) {
                throw new ProtocolException("session -- no end element");
            }
        }
    }
    
    private class EnvironmentProcessor extends ElementProcessor {
        // @see com.redprairie.moca.client.XMLRequestDecoder.ElementProcessor#process()
        @Override
        public void process() throws ProtocolException, XMLStreamException {
            while ((XMLEvent.START_ELEMENT == _xml.nextTag())) {
                String name = _xml.getAttributeValue(null, "name");
                String value = _xml.getAttributeValue(null, "value");
                _env.put(name, value);
                if ((XMLEvent.END_ELEMENT != _xml.nextTag())) {
                    throw new ProtocolException("env/var -- no end element");
                }
            }
            
            if (XMLEvent.END_ELEMENT != _xml.getEventType()) {
                throw new ProtocolException("session -- no close element: [" + _xml.getText() + "]");
            }
        }
    }
    
    private class ArgProcessor extends ElementProcessor {
        // @see com.redprairie.moca.client.XMLRequestDecoder.ElementProcessor#process()
        @Override
        public void process() throws ProtocolException, XMLStreamException {
            List<MocaArgument> argList;
            if (_xml.getLocalName().equals("context")) {
                argList = _context; 
            }
            else {
                argList = _args;
            }
            while ((XMLEvent.START_ELEMENT == _xml.nextTag())) {
                processField(argList);
            }
        }
            
        private void processField(List<MocaArgument> args) throws ProtocolException, XMLStreamException {
            String name = _xml.getAttributeValue(null, "name");
            String typeStr = _xml.getAttributeValue(null, "type");
            String operStr = _xml.getAttributeValue(null, "oper");
            String isNullStr = _xml.getAttributeValue(null, "null");
            
            boolean isNull = (isNullStr != null) ? Boolean.parseBoolean(isNullStr) : false;
            MocaType type = (typeStr != null) ? MocaType.valueOf(typeStr) : MocaType.STRING;
            MocaOperator oper = (operStr != null) ? MocaOperator.valueOf(operStr) : MocaOperator.EQ;
            
            StringBuilder valueText = new StringBuilder();
            outer:
            while (_xml.hasNext()) {
                switch (_xml.next()) {
                case XMLEvent.CHARACTERS:
                case XMLEvent.CDATA:
                    valueText.append(_xml.getText());
                    break;
                case XMLEvent.START_ELEMENT:
                    throw new ProtocolException("unexpected start tag");
                case XMLEvent.END_ELEMENT:
                case XMLEvent.END_DOCUMENT:
                    break outer;
                }
            }
            
            Object value = null;
            if (!isNull) {
                switch (type) {
                case BOOLEAN:
                    value = Boolean.valueOf(valueText.toString());
                    break;
                case DOUBLE:
                    value = Double.valueOf(valueText.toString());
                    break;
                case INTEGER:
                    value = Integer.valueOf(valueText.toString());
                    break;
                case STRING:
                    value = valueText.toString();
                    break;
                case DATETIME:
                    value = DateUtils.parseDate(valueText.toString());
                    break;
                case RESULTS:
                    value = new XMLResultsDecoder(new StringReader(
                        valueText.toString())).decode();
                    break;
                case BINARY:
                    value = Base64.decode(valueText.toString());
                    break;
                }
            }
            
            args.add(new MocaArgument(name, oper, type, value));
        }
    }
    
    private class QueryProcessor extends ElementProcessor {
        // @see com.redprairie.moca.client.XMLRequestDecoder.ElementProcessor#process()
        @Override
        public void process() throws ProtocolException, XMLStreamException {
            StringBuilder commandText = new StringBuilder();
            outer:
            while (_xml.hasNext()) {
                switch (_xml.next()) {
                case XMLEvent.CHARACTERS:
                case XMLEvent.CDATA:
                    commandText.append(_xml.getText());
                    break;
                case XMLEvent.END_ELEMENT:
                case XMLEvent.END_DOCUMENT:
                    break outer;
                }
            }
            _query = commandText.toString();
        }
    }
    
    //
    // Implementation
    //
    private static final XMLInputFactory _streamFactory = XMLInputFactory.newInstance();
    private final XMLStreamReader _xml;
    
    private String _sessionId;
    private String _query;
    private Map<String, String> _env = new LinkedHashMap<String, String>();
    private List<MocaArgument> _context = new ArrayList<MocaArgument>();
    private List<MocaArgument> _args = new ArrayList<MocaArgument>();
    private boolean _autoCommit = true;
    private boolean _remote = false;
}
